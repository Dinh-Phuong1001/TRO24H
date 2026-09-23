using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using UniStay.Api.Models;

namespace UniStay.Api.Controllers
{
    [ApiController]
    public class HistoryController : ControllerBase
    {
        private readonly UniStayDbContext _context;

        public HistoryController(UniStayDbContext context)
        {
            _context = context;
        }

        [HttpGet("api/view-history")]
        public async Task<IActionResult> GetViewHistory([FromQuery] string userId)
        {
            var historyRoomIds = await _context.ViewHistories
                .Where(h => h.UserId == userId)
                .OrderByDescending(h => h.ViewedAt)
                .Select(h => h.RoomId)
                .Distinct()
                .ToListAsync();

            var rooms = new List<Room>();
            foreach (var roomId in historyRoomIds)
            {
                var room = await _context.Rooms.FindAsync(roomId);
                if (room != null)
                {
                    rooms.Add(room);
                }
            }
            return Ok(rooms);
        }

        [HttpPost("api/view-history")]
        public async Task<IActionResult> RecordViewHistory([FromQuery] string userId, [FromQuery] string roomId)
        {
            var history = new ViewHistory
            {
                UserId = userId,
                RoomId = roomId,
                ViewedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
            };
            
            _context.ViewHistories.Add(history);
            await _context.SaveChangesAsync();
            return Ok(history);
        }
    }
}
