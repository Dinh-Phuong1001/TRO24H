using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace UniStay.Api.Migrations
{
    /// <inheritdoc />
    public partial class AddUnreadCounts : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "LandlordUnreadCount",
                table: "ChatSessions",
                type: "int",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.AddColumn<int>(
                name: "StudentUnreadCount",
                table: "ChatSessions",
                type: "int",
                nullable: false,
                defaultValue: 0);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "LandlordUnreadCount",
                table: "ChatSessions");

            migrationBuilder.DropColumn(
                name: "StudentUnreadCount",
                table: "ChatSessions");
        }
    }
}
