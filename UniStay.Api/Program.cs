using Microsoft.EntityFrameworkCore;
using UniStay.Api;
using Microsoft.Extensions.FileProviders;
using System.IO;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Add DbContext
builder.Services.AddDbContext<UniStayDbContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection"), sqlOptions => sqlOptions.UseCompatibilityLevel(120)));

// Add CORS
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll",
        policyBuilder =>
        {
            policyBuilder.AllowAnyOrigin()
                         .AllowAnyMethod()
                         .AllowAnyHeader();
        });
});

var app = builder.Build();

// Cấu hình rõ ràng để luôn truy cập được thư mục wwwroot
var wwwrootPath = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot");
if (!Directory.Exists(wwwrootPath))
{
    Directory.CreateDirectory(wwwrootPath);
}
app.UseStaticFiles(new StaticFileOptions
{
    FileProvider = new PhysicalFileProvider(wwwrootPath),
    RequestPath = ""
});

// Xóa trắng dữ liệu giả mạo (Chỉ chạy 1 lần)
using (var scope = app.Services.CreateScope())
{
    var dbContext = scope.ServiceProvider.GetRequiredService<UniStayDbContext>();
    var fakeRooms = dbContext.Rooms.Where(r => r.RoomCode == "R01" || r.RoomCode == "R02" || r.RoomCode == "R03");
    if (fakeRooms.Any())
    {
        dbContext.Rooms.RemoveRange(fakeRooms);
        dbContext.SaveChanges();
    }
}

app.UseSwagger();
app.UseSwaggerUI();

// app.UseHttpsRedirection();
app.UseCors("AllowAll");
app.UseAuthorization();
app.MapControllers();

app.Run();