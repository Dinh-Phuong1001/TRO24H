using Microsoft.EntityFrameworkCore;
using UniStay.Api.Models;

namespace UniStay.Api
{
    public class UniStayDbContext : DbContext
    {
        public UniStayDbContext(DbContextOptions<UniStayDbContext> options) : base(options) { }

        public DbSet<User> Users => Set<User>();
        public DbSet<Room> Rooms => Set<Room>();
        public DbSet<SavedRoom> SavedRooms => Set<SavedRoom>();
        public DbSet<ViewHistory> ViewHistories => Set<ViewHistory>();
        public DbSet<ChatSession> ChatSessions => Set<ChatSession>();
        public DbSet<Message> Messages => Set<Message>();
    }
}
