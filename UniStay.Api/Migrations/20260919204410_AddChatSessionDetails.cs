using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace UniStay.Api.Migrations
{
    /// <inheritdoc />
    public partial class AddChatSessionDetails : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<string>(
                name: "LandlordName",
                table: "ChatSessions",
                type: "nvarchar(max)",
                nullable: false,
                defaultValue: "");

            migrationBuilder.AddColumn<string>(
                name: "LandlordPhone",
                table: "ChatSessions",
                type: "nvarchar(max)",
                nullable: false,
                defaultValue: "");

            migrationBuilder.AddColumn<string>(
                name: "RoomTitle",
                table: "ChatSessions",
                type: "nvarchar(max)",
                nullable: false,
                defaultValue: "");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "LandlordName",
                table: "ChatSessions");

            migrationBuilder.DropColumn(
                name: "LandlordPhone",
                table: "ChatSessions");

            migrationBuilder.DropColumn(
                name: "RoomTitle",
                table: "ChatSessions");
        }
    }
}
