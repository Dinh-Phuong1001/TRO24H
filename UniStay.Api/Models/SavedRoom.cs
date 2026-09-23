using System.ComponentModel.DataAnnotations;

namespace UniStay.Api.Models
{
    public class SavedRoom
    {
        [Key]
        public int SaveId { get; set; }
        public string UserId { get; set; } = string.Empty;
        public string RoomId { get; set; } = string.Empty;
        public long SavedAt { get; set; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
    }
}
