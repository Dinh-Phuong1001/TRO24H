using System.ComponentModel.DataAnnotations;

namespace UniStay.Api.Models
{
    public class ViewHistory
    {
        [Key]
        public int HistoryId { get; set; }
        public string UserId { get; set; } = string.Empty;
        public string RoomId { get; set; } = string.Empty;
        public long ViewedAt { get; set; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
    }
}
