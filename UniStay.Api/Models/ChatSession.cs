using System.ComponentModel.DataAnnotations;

namespace UniStay.Api.Models
{
    public class ChatSession
    {
        [Key]
        public string SessionId { get; set; } = string.Empty;
        public string StudentId { get; set; } = string.Empty;
        public string LandlordId { get; set; } = string.Empty;
        public string RoomId { get; set; } = string.Empty;
        public string RoomTitle { get; set; } = string.Empty;
        public string LandlordName { get; set; } = string.Empty;
        public string LandlordPhone { get; set; } = string.Empty;
        public string LastMessage { get; set; } = string.Empty;
        public long LastUpdated { get; set; }
        public int StudentUnreadCount { get; set; } = 0;
        public int LandlordUnreadCount { get; set; } = 0;

        [System.ComponentModel.DataAnnotations.Schema.NotMapped]
        public string? StudentAvatarUrl { get; set; }
        [System.ComponentModel.DataAnnotations.Schema.NotMapped]
        public string? LandlordAvatarUrl { get; set; }
        [System.ComponentModel.DataAnnotations.Schema.NotMapped]
        public string? StudentName { get; set; }
    }
}
