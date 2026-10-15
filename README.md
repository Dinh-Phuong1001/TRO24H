# 🏠 TRO24H - Ứng Dụng Tìm Kiếm & Quản Lý Phòng Trọ Sinh Viên

> **Học phần:** Triển khai dự án phần mềm (Mã chuyên đề: ST-01)  
> **Mô hình quản lý:** **Mô hình Thác Nước (Waterfall Model - 6 Giai đoạn)**  
> **Hệ sinh thái:** Android Native (Kotlin) + Backend ASP.NET Core 8.0 + MS SQL Server Cloud

---

## 📌 1. Giới thiệu dự án
**TRO24H** là giải pháp công nghệ toàn diện giúp sinh viên và người đi thuê dễ dàng tìm kiếm nhà trọ, phòng ở ghép an toàn, xác thực gần các trường Đại học/Cao đẳng. Đồng thời, hệ thống cung cấp công cụ đăng tin, quản lý tin đăng và liên lạc tức thì (Chat 1-1) cho chủ nhà trọ.

* 🌐 **Backend & Swagger API Cloud:** [http://tro24h.runasp.net/swagger/index.html](http://tro24h.runasp.net/swagger/index.html)
* 🗄️ **Database Server:** MS SQL Server Cloud (`db69654.databaseasp.net`)
* 📦 **Bản phát hành APK v1.0.0:** [GitHub Releases v1.0.0](https://github.com/Dinh-Phuong1001/TRO24H/releases/tag/v1.0.0)

---

## 🛠️ 2. Công nghệ sử dụng (Tech Stack)

### 📱 Mobile App (Android Native)
- **Ngôn ngữ & Nền tảng:** Kotlin (Min SDK 24, Target SDK 34).
- **Kiến trúc:** Clean Architecture + MVVM (Model - View - ViewModel).
- **Giao diện & UI/UX:** Material Design 3, ViewBinding, RecyclerView, Custom Dialogs.
- **Xử lý bất đồng bộ & Mạng:** Kotlin Coroutines & StateFlow, Retrofit 2, OkHttp3 (HttpLoggingInterceptor).
- **Tải ảnh & Bộ nhớ tạm:** Coil Image Loader.
- **Quản lý phiên & Dữ liệu:** Android Jetpack DataStore / SharedPreferences (`UserSessionManager`).

### ⚙️ Backend Web API (.NET 8)
- **Framework:** ASP.NET Core 8.0 Web API.
- **ORM & Data Access:** Entity Framework Core 8, LINQ.
- **Tài liệu API & Kiểm thử:** Swagger / OpenAPI UI.
- **Bảo mật & Cấu hình:** CORS Policy, Password Hash, Base URL Config.

### ☁️ Cơ sở dữ liệu & Hạ tầng (Cloud & DevOps)
- **CSDL:** Microsoft SQL Server (6 bảng quan hệ chuẩn hóa: Users, Rooms, SavedRooms, ViewHistories, ChatSessions, Messages).
- **Cloud Hosting:** MonsterASP Cloud Hosting (.NET Core runtime + MSSQL).
- **Quản lý mã nguồn & Tiến độ:** Git & GitHub (Quản lý 6 Milestones theo Mô hình Thác Nước).

---

## 👥 3. Phân công công việc & Cơ cấu đội ngũ

Dự án được phân rã công việc dựa trên **Ma trận trách nhiệm (RACI Matrix)** và triển khai theo **Mô hình Thác Nước (Waterfall Model)** qua 6 giai đoạn tuần tự:

| STT | Vai trò | Nhiệm vụ chính đảm nhiệm |
|:---:|:---|:---|
| 1 | **Project Manager & Architect** | Lập kế hoạch dự án tổng thể, phân rã WBS, điều phối 6 giai đoạn Thác Nước, kiểm soát Phase Exit Gate và duyệt kiến trúc. |
| 2 | **Android Lead Developer** | Xây dựng khung kiến trúc Clean Architecture, Base Activity, Module Xác thực & Trang chủ. |
| 3 | **Android Feature Developer** | Phát triển chức năng Tìm kiếm nâng cao, Bộ lọc (Filter), Module Chat 1-1 (Ghim, Chặn, Xóa), Quản lý tài khoản. |
| 4 | **Backend Developer** | Xây dựng RESTful Web API (.NET 8), Auth/Room/Chat Controllers, Swagger documentation. |
| 5 | **Database & DevOps Engineer** | Thiết kế CSDL MSSQL chuẩn 3NF, cấu hình Index/Khóa ngoại, triển khai API & CSDL lên Cloud Server. |
| 6 | **QA / Tester & Tech Writer** | Soạn thảo Test Plan, kiểm thử hồi quy/API, lập dự toán ngân sách và hoàn thiện Báo cáo đặc tả SRS. |

📄 *Chi tiết xem tại:* [docs/05_PhanCongCongViec_TienDo.md](docs/05_PhanCongCongViec_TienDo.md)

---

## 📅 4. Quá trình phát triển qua 6 Giai đoạn Thác Nước (Waterfall Phases)

- **🌊 Giai đoạn 1: Phân tích & Đặc tả yêu cầu (Requirements Analysis)**
  - Khảo sát thực tế sinh viên & chủ trọ, lập Use Case, viết tài liệu PRD/SRS và chốt phạm vi dự án (Scope Freeze).
- **🌊 Giai đoạn 2: Thiết kế hệ thống & Cơ sở dữ liệu (System & UI/UX Design)**
  - Thiết kế kiến trúc Clean Architecture + MVVM, thiết kế Schema CSDL 6 bảng MS SQL Server, đặc tả hợp đồng 15 RESTful API endpoints.
- **🌊 Giai đoạn 3: Hiện thực hóa & Lập trình (Implementation / Coding)**
  - Lập trình Backend Web API (.NET 8) và Ứng dụng Android Native (Kotlin): Splash, Auth, Home, Filter, Bảng dự toán chi phí trọ, Google Maps, Chat 1-1 cử chỉ vuốt thông minh.
- **🌊 Giai đoạn 4: Kiểm thử phần mềm (Testing & Verification)**
  - Kiểm thử 15 API qua Postman/Swagger, kiểm thử tích hợp trên thiết bị thật, kiểm thử xử lý ngoại lệ mất mạng và fix 100% bug.
- **🌊 Giai đoạn 5: Triển khai Đám mây & Đóng gói phát hành (Deployment & Release)**
  - Triển khai MSSQL và API lên hạ tầng MonsterASP Cloud 24/7 (`http://tro24h.runasp.net`). Xuất bản Release APK v1.0.0 và tạo mã QR tải nhanh.
- **🌊 Giai đoạn 6: Nghiệm thu & Bàn giao dự án (Maintenance & Handover)**
  - Đánh giá chất lượng toàn diện, bàn giao bộ hồ sơ kỹ thuật (PRD, Kiến trúc, Tiến độ Thác Nước, Dự toán ngân sách) và bảo vệ đồ án BTL.

---

## 📂 5. Cấu trúc thư mục dự án

```text
TRO24H/
├── app/                             # Mã nguồn ứng dụng Android (Kotlin)
│   ├── src/main/java/com/unistay/android/
│   │   ├── data/                    # Data Layer: Api, Model, Repository
│   │   ├── di/                      # Dependency Injection / Factory
│   │   └── presentation/            # UI Layer: Activities, Adapters, ViewModels
│   │       ├── auth/                # Màn hình Đăng nhập / Đăng ký
│   │       ├── home/                # Trang chủ & Danh sách phòng
│   │       ├── detail/              # Chi tiết phòng trọ
│   │       ├── search/              # Tìm kiếm & Bộ lọc
│   │       ├── chat/                # Danh sách đoạn chat & Nhắn tin 1-1
│   │       └── profile/             # Thông tin cá nhân & Quản lý
│   └── src/main/res/                # Layout XML, Drawables, Values
│
├── UniStay.Api/                     # Mã nguồn Backend ASP.NET Core 8.0
│   ├── Controllers/                 # AuthController, RoomController, ChatController
│   ├── Data/                        # AppDbContext, Entity Models
│   ├── Program.cs                   # Cấu hình Services, CORS, Swagger
│   └── appsettings.json             # Chuỗi kết nối Database Cloud
│
├── docs/                            # Toàn bộ tài liệu quy trình & kỹ thuật
│   ├── 01_PRD.md                    # Product Requirements Document
│   ├── 02_System_Architecture.md    # Kiến trúc hệ thống
│   ├── 03_API_Contract.md           # Đặc tả các Endpoint RESTful API
│   ├── 04_Implementation_Plan.md    # Kế hoạch kỹ thuật
│   ├── 05_PhanCongCongViec_TienDo.md# Phân công công việc (RACI) & Tiến độ Thác Nước (Waterfall)
│   └── 06_DuToanChiPhi_NganSach.md  # Báo cáo dự toán chi phí & ngân sách dự án
│
├── BaoCao_DacTaYeuCau_Tro24H_Moi.docx # Báo cáo đặc tả yêu cầu hoàn chỉnh (DOCX)
├── ThongTin_CoSoDuLieu_Tro24H.doc     # Tài liệu thiết kế CSDL
└── README.md                        # Giới thiệu & Hướng dẫn dự án
```

---

## 🚀 6. Hướng dẫn cài đặt & Chạy ứng dụng

### 1. Ứng dụng Android
1. Mở thư mục gốc dự án bằng **Android Studio** (Hedgehog hoặc mới hơn).
2. Chờ Gradle đồng bộ các thư viện (Sync Project with Gradle Files).
3. Đảm bảo cấu hình URL trong `com.unistay.android.data.api.RetrofitClient` trỏ về API Cloud:
   ```kotlin
   private const val BASE_URL = "http://tro24h.runasp.net/"
   ```
4. Chọn thiết bị ảo (Emulator) hoặc máy thật (bật USB Debugging) và nhấn **Run 'app'** (`Shift + F10`).

### 2. Backend ASP.NET Core (.NET 8 SDK)
1. Cài đặt **.NET 8.0 SDK**.
2. Mở terminal tại thư mục `UniStay.Api`:
   ```bash
   cd UniStay.Api
   dotnet restore
   dotnet run
   ```
3. Truy cập Swagger UI tại: `http://localhost:5000/swagger` (hoặc URL hiển thị trên terminal).

---

## 📑 7. Tài liệu & Báo cáo đồ án
Tất cả các tài liệu phục vụ báo cáo môn học đã được chuẩn hóa và lưu trữ tại thư mục gốc và thư mục `docs/`:
- 📄 **Báo cáo đồ án đặc tả yêu cầu:** `BaoCao_DacTaYeuCau_Tro24H_Moi.docx`
- 📑 **Kế hoạch phân công & Tiến độ Thác Nước:** [docs/05_PhanCongCongViec_TienDo.md](docs/05_PhanCongCongViec_TienDo.md)
- 💰 **Dự toán chi phí & Ngân sách dự án:** [docs/06_DuToanChiPhi_NganSach.md](docs/06_DuToanChiPhi_NganSach.md)
- 📐 **Kiến trúc hệ thống & Thiết kế API:** [docs/02_System_Architecture.md](docs/02_System_Architecture.md) & [docs/03_API_Contract.md](docs/03_API_Contract.md)
