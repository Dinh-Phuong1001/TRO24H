using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using UniStay.Api.Models;

namespace UniStay.Api.Controllers
{
    [ApiController]
    public class ChatController : ControllerBase
    {
        private readonly UniStayDbContext _context;

        public ChatController(UniStayDbContext context)
        {
            _context = context;
        }

        [HttpGet("api/chat/sessions")]
        public async Task<IActionResult> GetChatSessions([FromQuery] string userId)
        {
            var sessions = await _context.ChatSessions
                .Where(s => s.StudentId == userId || s.LandlordId == userId)
                .ToListAsync();
                
            foreach (var session in sessions)
            {
                var student = await _context.Users.FindAsync(session.StudentId);
                var landlord = await _context.Users.FindAsync(session.LandlordId);
                
                if (student != null)
                {
                    session.StudentAvatarUrl = student.AvatarUrl;
                    session.StudentName = student.FullName;
                }
                if (landlord != null)
                {
                    session.LandlordAvatarUrl = landlord.AvatarUrl;
                }
            }

            return Ok(sessions);
        }

        [HttpGet("api/chat/messages")]
        public async Task<IActionResult> GetChatMessages([FromQuery] string sessionId)
        {
            var messages = await _context.Messages
                .Where(m => m.SessionId == sessionId)
                .OrderBy(m => m.Timestamp)
                .ToListAsync();
                
            return Ok(messages);
        }

        [HttpPost("api/chat/messages")]
        public async Task<IActionResult> SendChatMessage([FromBody] Message message)
        {
            // Kiểm tra session tồn tại chưa
            var session = await _context.ChatSessions.FindAsync(message.SessionId);
            if (session == null)
            {
                // Nếu chưa có session (tin nhắn đầu tiên), tạo session mới
                // Yêu cầu FE phải gửi thông tin hợp lệ hoặc tạm sinh ngẫu nhiên
                session = new ChatSession
                {
                    SessionId = message.SessionId,
                    StudentId = message.SenderId,
                    LandlordId = string.IsNullOrEmpty(message.LandlordId) ? "UNKNOWN" : message.LandlordId,
                    RoomId = string.IsNullOrEmpty(message.RoomId) ? "UNKNOWN" : message.RoomId,
                    RoomTitle = string.IsNullOrEmpty(message.RoomTitle) ? "Không rõ" : message.RoomTitle,
                    LandlordName = string.IsNullOrEmpty(message.LandlordName) ? "Chủ trọ" : message.LandlordName,
                    LandlordPhone = message.LandlordPhone ?? "",
                    LastMessage = message.Content,
                    LastUpdated = message.Timestamp
                };
                _context.ChatSessions.Add(session);
            }
            else
            {
                // Cập nhật thông tin nếu trước đó bị thiếu (session cũ)
                if (string.IsNullOrEmpty(session.LandlordName) || session.LandlordName == "Chủ trọ")
                {
                    session.LandlordName = string.IsNullOrEmpty(message.LandlordName) ? "Chủ trọ" : message.LandlordName;
                }
                if (string.IsNullOrEmpty(session.RoomTitle) || session.RoomTitle == "Không rõ")
                {
                    session.RoomTitle = string.IsNullOrEmpty(message.RoomTitle) ? "Không rõ" : message.RoomTitle;
                }
                if (string.IsNullOrEmpty(session.LandlordPhone))
                {
                    session.LandlordPhone = message.LandlordPhone ?? "";
                }
                if (session.LandlordId == "UNKNOWN" && !string.IsNullOrEmpty(message.LandlordId) && message.LandlordId != "UNKNOWN")
                {
                    session.LandlordId = message.LandlordId;
                }
                if (session.RoomId == "UNKNOWN" && !string.IsNullOrEmpty(message.RoomId) && message.RoomId != "UNKNOWN")
                {
                    session.RoomId = message.RoomId;
                }

                session.LastMessage = message.Content;
                session.LastUpdated = message.Timestamp;
                _context.ChatSessions.Update(session);
            }

            if (session.StudentId == message.SenderId)
            {
                // Student gửi tin nhắn -> tăng unread của Landlord
                session.LandlordUnreadCount++;
            }
            else
            {
                // Landlord gửi tin nhắn -> tăng unread của Student
                session.StudentUnreadCount++;
            }

            // session object is already tracked by EF Core, so changes to it will be saved.

            _context.Messages.Add(message);
            await _context.SaveChangesAsync();
            return Ok(message);
        }

        [HttpPost("api/chat/read/{sessionId}")]
        public async Task<IActionResult> MarkAsRead(string sessionId, [FromQuery] string userId)
        {
            var session = await _context.ChatSessions.FindAsync(sessionId);
            if (session == null) return NotFound();

            if (session.StudentId == userId)
            {
                session.StudentUnreadCount = 0;
            }
            else if (session.LandlordId == userId)
            {
                session.LandlordUnreadCount = 0;
            }

            _context.ChatSessions.Update(session);
            await _context.SaveChangesAsync();
            return Ok();
        }
    }
}
