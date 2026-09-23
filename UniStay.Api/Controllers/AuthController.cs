using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using UniStay.Api.Models;
using System.Security.Cryptography;
using System.Text;
using System.Net;
using System.Net.Mail;
using System.Collections.Concurrent;

namespace UniStay.Api.Controllers
{
    [ApiController]
    public class AuthController : ControllerBase
    {
        private readonly UniStayDbContext _context;
        
        // Lưu trữ OTP tạm thời: Email -> (OTP, ExpiryTime, LastSentTime)
        private static readonly ConcurrentDictionary<string, (string Otp, DateTime Expiry, DateTime LastSent)> _otpStore = new();

        public AuthController(UniStayDbContext context)
        {
            _context = context;
        }

        [HttpPost("api/auth/request-otp")]
        public async Task<IActionResult> RequestOtp([FromBody] RequestOtpRequest request)
        {
            if (await _context.Users.AnyAsync(u => u.Email == request.Email))
            {
                return BadRequest(new { Message = "Email đã tồn tại" });
            }

            // Kiểm tra cooldown 1 phút
            if (_otpStore.TryGetValue(request.Email, out var existingOtp))
            {
                var timeSinceLastSent = (DateTime.UtcNow - existingOtp.LastSent).TotalSeconds;
                if (timeSinceLastSent < 60)
                {
                    return BadRequest(new { Message = $"Vui lòng đợi {60 - (int)timeSinceLastSent} giây trước khi yêu cầu mã mới" });
                }
            }

            // Generate 6-digit OTP
            var otp = new Random().Next(100000, 999999).ToString();
            _otpStore[request.Email] = (otp, DateTime.UtcNow.AddMinutes(5), DateTime.UtcNow);

            try
            {
                var smtpClient = new SmtpClient("smtp.gmail.com")
                {
                    Port = 587,
                    Credentials = new NetworkCredential("timtro24h@gmail.com", "coskowefeojktnbt"),
                    EnableSsl = true,
                };
                
                var mailMessage = new MailMessage
                {
                    From = new MailAddress("timtro24h@gmail.com", "Tro24H"),
                    Subject = "Mã xác nhận đăng ký Tro24H",
                    Body = $"Mã OTP xác nhận đăng ký của bạn là: {otp}. Mã này có hiệu lực trong 5 phút.",
                    IsBodyHtml = false,
                };
                mailMessage.To.Add(request.Email);

                smtpClient.Send(mailMessage);
                Console.WriteLine($"[OTP SENT TO {request.Email}]: {otp}");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[SMTP ERROR] Could not send email: {ex.Message}");
                // Fallback: If email fails, print OTP to console for debugging
                Console.WriteLine($"[FALLBACK OTP FOR {request.Email}]: {otp}");
            }

            return Ok(new { Message = "OTP đã được gửi" });
        }

        [HttpPost("api/auth/register")]
        public async Task<IActionResult> Register([FromBody] RegisterRequest request)
        {
            if (await _context.Users.AnyAsync(u => u.Email == request.Email))
            {
                return BadRequest(new { Message = "Email đã tồn tại" });
            }

            if (!_otpStore.TryGetValue(request.Email, out var otpData))
            {
                return BadRequest(new { Message = "Vui lòng yêu cầu mã OTP trước" });
            }

            if (DateTime.UtcNow > otpData.Expiry)
            {
                _otpStore.TryRemove(request.Email, out _);
                return BadRequest(new { Message = "Mã OTP đã hết hạn" });
            }

            if (otpData.Otp != request.Otp)
            {
                return BadRequest(new { Message = "Mã OTP không chính xác" });
            }

            // Xác thực thành công, xóa OTP khỏi bộ nhớ
            _otpStore.TryRemove(request.Email, out _);

            var user = new User
            {
                UserId = Guid.NewGuid().ToString(),
                FullName = request.FullName,
                Email = request.Email,
                PasswordHash = ComputeSha256Hash(request.Password),
                Role = request.Role,
                PhoneNumber = request.PhoneNumber,
                CreatedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            return Ok(new AuthResponse { Token = "fake-jwt-token", User = user });
        }

        [HttpPost("api/auth/login")]
        public async Task<IActionResult> Login([FromBody] LoginRequest request)
        {
            var hash = ComputeSha256Hash(request.Password);
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == request.Email && u.PasswordHash == hash);

            if (user == null)
            {
                return Unauthorized(new { Message = "Email hoặc mật khẩu không đúng" });
            }

            return Ok(new AuthResponse { Token = "fake-jwt-token", User = user });
        }

        [HttpPost("api/auth/change-password")]
        public async Task<IActionResult> ChangePassword([FromBody] ChangePasswordRequest request)
        {
            var user = await _context.Users.FindAsync(request.UserId);
            if (user == null)
            {
                return NotFound(new { Message = "Người dùng không tồn tại" });
            }

            var oldHash = ComputeSha256Hash(request.OldPassword);
            if (user.PasswordHash != oldHash)
            {
                return BadRequest(new { Message = "Mật khẩu cũ không chính xác" });
            }

            user.PasswordHash = ComputeSha256Hash(request.NewPassword);
            await _context.SaveChangesAsync();

            return Ok(new { Message = "Đổi mật khẩu thành công" });
        }

        [HttpPost("api/auth/forgot-password-otp")]
        public async Task<IActionResult> RequestForgotPasswordOtp([FromBody] RequestOtpRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.Email))
            {
                return BadRequest(new { Message = "Vui lòng nhập email" });
            }

            var cleanEmail = request.Email.Trim().ToLower();
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email.ToLower() == cleanEmail);
            if (user == null)
            {
                return BadRequest(new { Message = "Email không tồn tại trong hệ thống" });
            }

            // Kiểm tra cooldown 1 phút
            if (_otpStore.TryGetValue(cleanEmail, out var existingOtp))
            {
                var timeSinceLastSent = (DateTime.UtcNow - existingOtp.LastSent).TotalSeconds;
                if (timeSinceLastSent < 60)
                {
                    return BadRequest(new { Message = $"Vui lòng đợi {60 - (int)timeSinceLastSent} giây trước khi yêu cầu mã mới" });
                }
            }

            // Generate 6-digit OTP
            var otp = new Random().Next(100000, 999999).ToString();
            _otpStore[cleanEmail] = (otp, DateTime.UtcNow.AddMinutes(5), DateTime.UtcNow);

            try
            {
                var smtpClient = new SmtpClient("smtp.gmail.com")
                {
                    Port = 587,
                    Credentials = new NetworkCredential("timtro24h@gmail.com", "coskowefeojktnbt"),
                    EnableSsl = true,
                };
                
                var mailMessage = new MailMessage
                {
                    From = new MailAddress("timtro24h@gmail.com", "Tro24H"),
                    Subject = "Mã xác nhận đặt lại mật khẩu Tro24H",
                    Body = $"Mã OTP đặt lại mật khẩu của bạn là: {otp}. Mã này có hiệu lực trong 5 phút.",
                    IsBodyHtml = false,
                };
                mailMessage.To.Add(cleanEmail);

                smtpClient.Send(mailMessage);
                Console.WriteLine($"[FORGOT OTP SENT TO {cleanEmail}]: {otp}");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[SMTP ERROR] Could not send email: {ex.Message}");
                Console.WriteLine($"[FALLBACK FORGOT OTP FOR {cleanEmail}]: {otp}");
            }

            return Ok(new { Message = "Mã OTP đã được gửi đến email của bạn" });
        }

        [HttpPost("api/auth/reset-password")]
        public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.Email))
            {
                return BadRequest(new { Message = "Vui lòng nhập email" });
            }

            var cleanEmail = request.Email.Trim().ToLower();
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email.ToLower() == cleanEmail);
            if (user == null)
            {
                return BadRequest(new { Message = "Email không tồn tại trong hệ thống" });
            }

            if (!_otpStore.TryGetValue(cleanEmail, out var otpData))
            {
                return BadRequest(new { Message = "Vui lòng yêu cầu mã OTP trước" });
            }

            if (DateTime.UtcNow > otpData.Expiry)
            {
                _otpStore.TryRemove(cleanEmail, out _);
                return BadRequest(new { Message = "Mã OTP đã hết hạn" });
            }

            if (otpData.Otp != request.Otp)
            {
                return BadRequest(new { Message = "Mã OTP không chính xác" });
            }

            // OTP đúng, xóa OTP khỏi bộ nhớ
            _otpStore.TryRemove(cleanEmail, out _);

            user.PasswordHash = ComputeSha256Hash(request.NewPassword);
            await _context.SaveChangesAsync();

            return Ok(new { Message = "Đặt lại mật khẩu thành công" });
        }

        private static string ComputeSha256Hash(string rawData)
        {
            using (SHA256 sha256Hash = SHA256.Create())
            {
                byte[] bytes = sha256Hash.ComputeHash(Encoding.UTF8.GetBytes(rawData));
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < bytes.Length; i++)
                {
                    builder.Append(bytes[i].ToString("x2"));
                }
                return builder.ToString();
            }
        }
    }

    public class ChangePasswordRequest
    {
        public string UserId { get; set; } = string.Empty;
        public string OldPassword { get; set; } = string.Empty;
        public string NewPassword { get; set; } = string.Empty;
    }
}
