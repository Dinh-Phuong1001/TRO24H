using System.ComponentModel.DataAnnotations;

namespace UniStay.Api.Models
{
    public class Message
    {
        [Key]
        public int MessageId { get; set; }
        public string SessionId { get; set; } = string.Empty;
        public string SenderId { get; set; } = string.Empty;
        public string Content { get; set; } = string.Empty;
        public long Timestamp { get; set; }

        [System.ComponentModel.DataAnnotations.Schema.NotMapped]
        public string? RoomId { get; set; }
        [System.ComponentModel.DataAnnotations.Schema.NotMapped]
        public string? LandlordId { get; set; }
        [System.ComponentModel.DataAnnotations.Schema.NotMapped]
        public string? RoomTitle { get; set; }
        [System.ComponentModel.DataAnnotations.Schema.NotMapped]
        public string? LandlordName { get; set; }
        [System.ComponentModel.DataAnnotations.Schema.NotMapped]
        public string? LandlordPhone { get; set; }
    }
}
