# System Architecture & Project Structure - Ứng Dụng TRO24H

## 1. Kiến trúc hệ thống tổng thể (Overall Architecture)
Hệ thống **TRO24H** được phát triển theo mô hình phân tán **Client - Server**:
- **Client (Mobile App):** Xây dựng trên nền tảng Android Native (Kotlin) áp dụng mô hình kiến trúc **Clean Architecture** kết hợp **MVVM (Model - View - ViewModel)**.
- **Server (Backend API):** Xây dựng trên nền tảng **ASP.NET Core 8.0 Web API** cung cấp các RESTful endpoints chuẩn hóa.
- **Cơ sở dữ liệu (Database):** **Microsoft SQL Server** lưu trữ tập trung trên nền tảng đám mây MonsterASP.

```text
[ Android Client (TRO24H App) ]
         │ (HTTP REST / JSON)
         ▼
[ ASP.NET Core 8.0 Web API ] (http://tro24h.runasp.net)
         │ (Entity Framework Core 8)
         ▼
[ MS SQL Server Database ] (db69654.databaseasp.net)
```

---

## 2. Kiến trúc Ứng dụng Android (Clean Architecture + MVVM)

Hệ thống mã nguồn Android được phân tách rõ ràng thành 3 tầng chính:

```text
com.unistay.android
│
├── di/                             # Tầng Dependency Injection & Service Locator
│   ├── AppModule.kt                # Cung cấp Context, DataStore, Gson
│   ├── NetworkModule.kt            # Cung cấp OkHttpClient, Retrofit, ApiService
│   └── DatabaseModule.kt           # Cung cấp Room Database cục bộ
│
├── domain/                         # Tầng Nghiệp vụ cốt lõi (Domain Layer)
│   ├── model/                      # Các thực thể nghiệp vụ (User, Room, ChatSession, Message, SavedRoom, ViewHistory)
│   └── repository/                 # Giao diện trừu tượng hóa truy xuất dữ liệu (UserRepository, RoomRepository)
│
├── data/                           # Tầng Dữ liệu (Data Layer)
│   ├── remote/                     # Tương tác với Web API Backend
│   │   ├── api/                    # Retrofit Api Service (UniStayApi)
│   │   └── RetrofitClient.kt       # Cấu hình Base URL (http://tro24h.runasp.net/)
│   ├── local/                      # Lưu trữ cục bộ trên thiết bị
│   │   ├── dao/                    # Data Access Objects (UserDao, RoomDao, ChatDao)
│   │   ├── datastore/              # Quản lý phiên đăng nhập (UserSessionManager)
│   │   └── AppDatabase.kt          # Database Room cục bộ
│   └── repository/                 # Triển khai thực tế các Repository Interfaces
│
└── presentation/                   # Tầng Giao diện & Trạng thái người dùng (UI Layer)
    ├── auth/                       # Đăng nhập (LoginActivity), Đăng ký (RegisterActivity), Điều khoản (TermsActivity)
    ├── home/                       # Trang chủ (HomeFragment), Danh sách phòng (RoomAdapter), Chi tiết phòng (RoomDetailActivity)
    ├── search/                     # Màn hình tìm kiếm (SearchFragment), Kết quả tìm kiếm (SearchResultActivity)
    ├── create/                     # Đăng tin phòng trọ cho Chủ trọ (CreateRoomFragment)
    ├── chat/                       # Danh sách tin nhắn (ChatListFragment), Nhắn tin 1-1 (ChatActivity, ChatMessageAdapter)
    ├── profile/                    # Trang cá nhân (ProfileFragment), Sửa hồ sơ (EditProfileActivity), Cài đặt (SettingsActivity)
    │                               # Phòng đã lưu (SavedRoomsActivity), Lịch sử xem (ViewHistoryActivity), Phòng của tôi (MyRoomsActivity)
    └── MainActivity.kt             # Điểm điều hướng chính chứa BottomNavigationView
```

---

## 3. Kiến trúc Backend Web API (ASP.NET Core 8.0)

Mã nguồn máy chủ `UniStay.Api` được tổ chức tinh gọn và bảo mật:
- **`Controllers/`**:
  - `AuthController.cs`: Tiếp nhận xử lý Đăng ký, Đăng nhập, Quản lý tài khoản.
  - `RoomController.cs`: Tiếp nhận xử lý Danh sách phòng, Tìm kiếm theo trường/giá, Chi tiết phòng, Đăng tin, Lưu phòng, Lịch sử xem.
  - `ChatController.cs`: Tiếp nhận xử lý Khởi tạo phiên chat, Lấy danh sách hội thoại, Gửi và nhận tin nhắn.
- **`Data/`**:
  - `UniStayDbContext.cs`: Kế thừa `DbContext` của Entity Framework Core, ánh xạ 6 bảng vào CSDL.
  - Thực thể Entities: `User`, `Room`, `SavedRoom`, `ViewHistory`, `ChatSession`, `Message`.
- **`Program.cs`**: Cấu hình CORS mở, cấu hình tài liệu Swagger UI, ánh xạ chuỗi kết nối Cloud Database.

---

## 4. Công nghệ & Thư viện chủ đạo
| Hạng mục | Công nghệ sử dụng | Mục đích |
|---|---|---|
| **Ngôn ngữ Mobile** | Kotlin (1.9+) | Phát triển ứng dụng Android Native hiện đại, an toàn bộ nhớ. |
| **Kiến trúc UI** | MVVM + StateFlow | Quản lý trạng thái UI trực quan, phản ứng nhanh với dữ liệu bất đồng bộ. |
| **Giao tiếp mạng** | Retrofit 2 + OkHttp 3 | Gửi nhận dữ liệu JSON với Backend qua giao thức HTTP/HTTPS. |
| **Tải & Cache ảnh** | Coil Image Loader | Tải ảnh phòng trọ và avatar mượt mà với cơ chế cache tự động. |
| **Lưu phiên làm việc** | Jetpack DataStore | Lưu trữ an toàn UserId, Email, FullName và Role của người dùng. |
| **Backend Framework**| ASP.NET Core 8.0 Web API | Xử lý yêu cầu với hiệu năng cao, độ trễ thấp. |
| **ORM Database** | Entity Framework Core 8 | Tương tác CSDL thông qua LINQ, tối ưu hóa truy vấn. |
| **Cloud Hosting** | MonsterASP Cloud | Máy chủ triển khai Web API và CSDL MSSQL hoạt động 24/7. |
