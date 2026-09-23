using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using UniStay.Api.Models;

namespace UniStay.Api.Controllers
{
    [ApiController]
    public class RoomsController : ControllerBase
    {
        private readonly UniStayDbContext _context;

        public RoomsController(UniStayDbContext context)
        {
            _context = context;
        }

        [HttpDelete("api/nukechats")]
        public async Task<IActionResult> NukeChats()
        {
            _context.Messages.RemoveRange(_context.Messages);
            _context.ChatSessions.RemoveRange(_context.ChatSessions);
            await _context.SaveChangesAsync();
            return Ok("Nuked");
        }

        [HttpGet("api/rooms")]
        public async Task<IActionResult> GetRooms(
            [FromQuery] string? university = null,
            [FromQuery] string? district = null,
            [FromQuery] double? minPrice = null,
            [FromQuery] double? maxPrice = null,
            [FromQuery] string? userId = null,
            [FromQuery] string? roomType = null)
        {
            var query = _context.Rooms.AsQueryable();

            if (!string.IsNullOrEmpty(university))
            {
                // In database the university name might be exact like "UTC" or "FTU"
                query = query.Where(r => r.TargetUniversity == university);
            }
            if (!string.IsNullOrEmpty(district))
            {
                query = query.Where(r => r.Address.Contains(district));
            }
            if (minPrice.HasValue)
            {
                query = query.Where(r => (double)r.BasePrice >= minPrice.Value);
            }
            if (maxPrice.HasValue)
            {
                query = query.Where(r => (double)r.BasePrice <= maxPrice.Value);
            }
            if (!string.IsNullOrEmpty(userId))
            {
                query = query.Where(r => r.UserId == userId);
            }
            if (!string.IsNullOrEmpty(roomType) && !roomType.StartsWith("Chọn") && !roomType.StartsWith("Tất cả"))
            {
                query = query.Where(r => r.RoomType == roomType);
            }

            var rooms = await query.ToListAsync();
            
            var userIds = rooms.Select(r => r.UserId).Distinct().ToList();
            var users = await _context.Users
                .Where(u => userIds.Contains(u.UserId))
                .ToDictionaryAsync(u => u.UserId, u => u.FullName);

            foreach (var room in rooms)
            {
                if (users.TryGetValue(room.UserId, out var name))
                {
                    room.OwnerName = name;
                }
            }

            return Ok(rooms);
        }

        [HttpGet("api/rooms/{id}")]
        public async Task<IActionResult> GetRoomById(string id)
        {
            var room = await _context.Rooms.FindAsync(id);
            if (room == null) return NotFound();

            var user = await _context.Users.FirstOrDefaultAsync(u => u.UserId == room.UserId);
            if (user != null)
            {
                room.OwnerName = user.FullName;
            }

            return Ok(room);
        }

        [HttpPost("api/rooms")]
        public async Task<IActionResult> CreateRoom([FromBody] Room room)
        {
            room.RoomId = Guid.NewGuid().ToString();
            _context.Rooms.Add(room);
            await _context.SaveChangesAsync();
            return Ok(room);
        }

        [HttpPost("api/saved-rooms")]
        public async Task<IActionResult> SaveRoom([FromQuery] string userId, [FromQuery] string roomId)
        {
            if (await _context.SavedRooms.AnyAsync(s => s.UserId == userId && s.RoomId == roomId))
            {
                return BadRequest("Đã lưu phòng này rồi");
            }

            var savedRoom = new SavedRoom
            {
                UserId = userId,
                RoomId = roomId,
                SavedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
            };
            _context.SavedRooms.Add(savedRoom);
            await _context.SaveChangesAsync();
            return Ok(savedRoom);
        }

        [HttpGet("api/saved-rooms")]
        public async Task<IActionResult> GetSavedRooms([FromQuery] string userId)
        {
            var savedRoomIds = await _context.SavedRooms
                .Where(s => s.UserId == userId)
                .Select(s => s.RoomId)
                .ToListAsync();

            var rooms = await _context.Rooms
                .Where(r => savedRoomIds.Contains(r.RoomId))
                .ToListAsync();

            return Ok(rooms);
        }

        [HttpDelete("api/saved-rooms")]
        public async Task<IActionResult> UnsaveRoom([FromQuery] string userId, [FromQuery] string roomId)
        {
            var savedRoom = await _context.SavedRooms
                .FirstOrDefaultAsync(s => s.UserId == userId && s.RoomId == roomId);

            if (savedRoom != null)
            {
                _context.SavedRooms.Remove(savedRoom);
                await _context.SaveChangesAsync();
            }
            return Ok();
        }

        [HttpDelete("api/rooms/{id}")]
        public async Task<IActionResult> DeleteRoom(string id, [FromQuery] string userId)
        {
            var room = await _context.Rooms.FindAsync(id);
            if (room == null) return NotFound();

            if (room.UserId != userId)
            {
                return Forbid(); // Trả về 403 Forbidden nếu không phải người đăng
            }

            // Xóa các saved rooms liên quan đến phòng này để tránh lỗi khóa ngoại
            var savedRooms = await _context.SavedRooms.Where(s => s.RoomId == id).ToListAsync();
            _context.SavedRooms.RemoveRange(savedRooms);

            // Xóa phòng
            _context.Rooms.Remove(room);
            await _context.SaveChangesAsync();

            return Ok();
        }
    }
}
