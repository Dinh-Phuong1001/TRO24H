using System.ComponentModel.DataAnnotations;

namespace UniStay.Api.Models
{
    public class Room
    {
        [Key]
        public string RoomId { get; set; } = string.Empty;
        public string RoomCode { get; set; } = string.Empty;
        public string Title { get; set; } = string.Empty;
        public string Address { get; set; } = string.Empty;
        public decimal BasePrice { get; set; }
        public string TargetUniversity { get; set; } = string.Empty;
        public decimal DistanceToCampusKm { get; set; }
        public string Status { get; set; } = "Còn phòng";
        public int MaxOccupancy { get; set; }
        public int CurrentOccupancy { get; set; }
        public string? ImageUrl { get; set; }
        public string? Amenities { get; set; }
        public string? ContactPhone { get; set; }
        public string? Description { get; set; }
        public string RoomType { get; set; } = "Phòng trọ khép kín";
        public string UserId { get; set; } = string.Empty; // Landlord ID

        [System.ComponentModel.DataAnnotations.Schema.NotMapped]
        public string? OwnerName { get; set; }
    }
}
