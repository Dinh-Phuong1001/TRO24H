# KẾ HOẠCH PHÂN CÔNG CÔNG VIỆC & TIẾN ĐỘ DỰ ÁN TRO24H
**Học phần:** Triển khai dự án phần mềm (Mã chuyên đề: ST-01)  
**Dự án:** Hệ thống ứng dụng di động tìm kiếm và quản lý phòng trọ sinh viên TRO24H  
**Mô hình phát triển:** Scrum / Agile (2 tuần / Sprint)

---

## 1. CƠ CẤU NHÓM & VAI TRÒ DỰ ÁN

| STT | Họ và tên | Vai trò chính | Nhiệm vụ đảm nhiệm |
|:---:|:---|:---|:---|
| 1 | **Trưởng nhóm (PM)** | Project Manager & Tech Lead | • Lập kế hoạch dự án, phân rã công việc (WBS).<br>• Quản lý tiến độ Sprint, họp Daily Standup.<br>• Phê duyệt kiến trúc hệ thống và giải quyết rủi ro kỹ thuật. |
| 2 | **Lập trình viên Mobile (Lead)** | Android Native Developer | • Thiết lập kiến trúc Clean Architecture + MVVM trên Android Studio.<br>• Lập trình các màn hình chính: Splash, Login, Register, Home, RoomDetail.<br>• Tích hợp thư viện Retrofit, OkHttp, Coil, ViewBinding. |
| 3 | **Lập trình viên Mobile 2** | Android Feature Developer | • Lập trình tính năng Tìm kiếm thời gian thực & Bộ lọc nâng cao (Filter).<br>• Lập trình giao diện Chat 1-1 (`ChatActivity`, `ChatMessageAdapter`, `ChatListAdapter`).<br>• Xử lý lưu trữ phiên người dùng với `UserSessionManager` (DataStore). |
| 4 | **Lập trình viên Backend** | Backend & API Developer | • Xây dựng máy chủ ASP.NET Core 8.0 Web API.<br>• Viết các Controller: `AuthController`, `RoomController`, `ChatController`.<br>• Tích hợp tài liệu Swagger UI, xử lý CORS và phân quyền dữ liệu. |
| 5 | **Kỹ sư Cơ sở dữ liệu & DevOps** | Database Admin & DevOps | • Thiết kế lược đồ CSDL MS SQL Server (6 bảng quan hệ, khóa ngoại).<br>• Viết script nạp dữ liệu mẫu (Seed Data) và tối ưu hóa chỉ mục (Indexes).<br>• Triển khai Backend và Database lên hạ tầng đám mây (MonsterASP Cloud). |
| 6 | **Kiểm thử viên (QA/QC)** | Tester & Technical Writer | • Viết tài liệu Kế hoạch kiểm thử (Test Plan) và thiết kế Test Cases.<br>• Kiểm thử chức năng API qua Postman, kiểm thử giao diện trên thiết bị thật.<br>• Soạn thảo Tài liệu đặc tả yêu cầu (SRS) và Báo cáo tổng kết dự án. |

---

## 2. MA TRẬN PHÂN CÔNG CÔNG VIỆC (RACI MATRIX)
* **R (Responsible):** Người trực tiếp thực hiện công việc.
* **A (Accountable):** Người chịu trách nhiệm cao nhất về kết quả.
* **C (Consulted):** Người được tham vấn chuyên môn.
* **I (Informed):** Người được thông báo kết quả.

| Nhóm công việc | Trưởng nhóm (PM) | Dev Mobile | Dev Backend | Database/DevOps | Tester / QA |
|:---|:---:|:---:|:---:|:---:|:---:|
| 1. Khảo sát & Viết tài liệu đặc tả (PRD/SRS) | **A** | C | C | C | **R** |
| 2. Thiết kế Kiến trúc hệ thống & DB Schema | **A** | C | **R** | **R** | I |
| 3. Xây dựng RESTful Web API (.NET 8) | A | C | **R** | C | I |
| 4. Xây dựng Giao diện & Xử lý luồng Android | A | **R** | C | I | I |
| 5. Tích hợp Module Chat 1-1 thời gian thực | A | **R** | **R** | C | I |
| 6. Triển khai Cloud (Hosting & Database) | A | I | C | **R** | I |
| 7. Kiểm thử tích hợp & Bắt lỗi ngoại lệ | A | C | C | I | **R** |
| 8. Đóng gói APK & Viết báo cáo nghiệm thu | **A** | **R** | I | I | **R** |

---

## 3. TIẾN ĐỘ THỰC HIỆN THEO CHU KỲ SPRINT (SCRUM)

### 🟢 Sprint 1: Khởi động dự án & Thiết lập cơ sở hạ tầng (Tuần 1)
* **Mục tiêu:** Hoàn thiện đặc tả yêu cầu, thiết lập khung sườn mã nguồn.
* **Công việc cụ thể:**
  - [x] Soạn thảo `01_PRD.md` (Product Requirements Document).
  - [x] Khởi tạo project Android Studio (Kotlin, Min SDK 24, Target SDK 34).
  - [x] Cấu hình Clean Architecture (di, domain, data, presentation).
  - [x] Khởi tạo dự án Backend Web API (`UniStay.Api` - ASP.NET Core 8.0 C#).
* **Đầu ra (Deliverables):** Mã nguồn khung sườn trên Git, tài liệu PRD.

### 🟢 Sprint 2: Thiết kế Cơ sở dữ liệu & Xây dựng Core API (Tuần 2 - 3)
* **Mục tiêu:** CSDL hoạt động ổn định, các API cốt lõi được xây dựng xong.
* **Công việc cụ thể:**
  - [x] Thiết kế 6 bảng CSDL: `Users`, `Rooms`, `SavedRooms`, `ViewHistories`, `ChatSessions`, `Messages`.
  - [x] Thiết lập Entity Framework Core 8 và DbContext.
  - [x] Viết `AuthController` (Đăng ký, Đăng nhập, giải mã thông tin).
  - [x] Viết `RoomController` (Lấy danh sách phòng, chi tiết phòng, tìm kiếm theo trường ĐH).
  - [x] Cấu hình Swagger OpenAPI UI để kiểm thử trực quan.
* **Đầu ra:** 15 API chạy thử nghiệm đạt chuẩn trên môi trường phát triển cục bộ.

### 🟢 Sprint 3: Phát triển Module Xác thực & Quản lý phiên (Tuần 4)
* **Mục tiêu:** Người dùng đăng ký, đăng nhập và phân quyền chuẩn xác trên Android.
* **Công việc cụ thể:**
  - [x] Thiết kế giao diện XML: `LoginActivity`, `RegisterActivity`, `SplashActivity`.
  - [x] Tích hợp `UserSessionManager` lưu thông tin vào DataStore Preferences.
  - [x] Xử lý điều hướng tự động: Tự vào màn hình chính nếu phiên còn hiệu lực.
  - [x] Phân chia luồng giao diện theo Role: Sinh viên (Student) & Chủ trọ (Landlord).
* **Đầu ra:** Module Authentication hoàn thiện 100%, không phát sinh lỗi bảo mật.

### 🟢 Sprint 4: Phát triển Luồng Khám phá & Chi tiết phòng trọ (Tuần 5 - 6)
* **Mục tiêu:** Sinh viên tìm kiếm, lọc và xem thông tin phòng trọ mượt mà.
* **Công việc cụ thể:**
  - [x] Xây dựng màn hình `HomeFragment` với `RecyclerView` hiển thị thẻ phòng trọ.
  - [x] Tích hợp `RoomAdapter` và thư viện tải ảnh `Coil`.
  - [x] Lập trình tính toán số chỗ trống tự động: `Trống (Max - Current)/Max`.
  - [x] Lập trình bộ lọc đa tiêu chí: Lọc theo trường ĐH (UTC, FTU, BKHN,...), khoảng giá, tiện ích.
  - [x] Xây dựng màn hình `RoomDetailActivity`: Xem thông tin chi tiết, liên hệ chủ trọ, nút Chat.
* **Đầu ra:** Trải nghiệm duyệt và lọc phòng đạt tốc độ 60fps trên điện thoại Android.

### 🟢 Sprint 5: Phát triển Module Tin nhắn 1-1 & Quản lý Chủ trọ (Tuần 7)
* **Mục tiêu:** Trao đổi tin nhắn trực tiếp giữa hai bên và chủ trọ quản lý bài đăng.
* **Công việc cụ thể:**
  - [x] Xây dựng `ChatController` hỗ trợ khởi tạo phiên chat theo từng phòng cụ thể.
  - [x] Xây dựng `ChatActivity`, `ChatListAdapter`, `ChatMessageAdapter` trên Android.
  - [x] Hiển thị tin nhắn dạng bong bóng đối xứng (người gửi bên phải, người nhận bên trái).
  - [x] Xây dựng chức năng thêm/sửa phòng và cập nhật số người đang ở dành cho chủ trọ.
* **Đầu ra:** Luồng chat thông suốt, tin nhắn được lưu trữ bền vững trong cơ sở dữ liệu.

### 🟢 Sprint 6: Triển khai Cloud 24/7, Kiểm thử & Đóng gói (Tuần 8)
* **Mục tiêu:** Hệ thống online toàn diện, xuất bản APK và nghiệm thu dự án.
* **Công việc cụ thể:**
  - [x] Triển khai MS SQL Server lên Cloud (`db69654.databaseasp.net`), nạp 20 bản ghi mẫu.
  - [x] Triển khai ASP.NET Core API lên Cloud MonsterASP (`http://tro24h.runasp.net`).
  - [x] Đồng bộ toàn bộ `BASE_URL` trong mã nguồn Android sang domain máy chủ Cloud.
  - [x] Kiểm thử toàn diện trên thiết bị thật kết nối 4G/5G độc lập.
  - [x] Soạn thảo tài liệu đặc tả hoàn chỉnh `BaoCao_DacTaYeuCau_Tro24H_Moi.docx`.
  - [x] Xuất bản file cài đặt APK và đẩy toàn bộ mã nguồn lên kho lưu trữ GitHub.
* **Đầu ra:** Dự án hoạt động 100% online, sẵn sàng bảo vệ đồ án / bài tập lớn.

---

## 4. TIÊU CHÍ HOÀN THÀNH (DEFINITION OF DONE - DOD)
1. **Mã nguồn:** Không có lỗi cú pháp (Syntax error), tuân thủ Clean Architecture và định dạng chuẩn (Clean Code).
2. **Cơ sở dữ liệu:** Ràng buộc khóa ngoại toàn vẹn 100%, có dữ liệu mẫu đầy đủ cho tất cả các bảng.
3. **Backend API:** Toàn bộ API phản hồi mã HTTP chuẩn (200, 400, 401, 404, 500), có tài liệu Swagger trực quan.
4. **Ứng dụng di động:** Cài đặt và hoạt động tốt trên các thiết bị Android từ phiên bản 7.0 đến 14, không phụ thuộc vào mạng nội bộ (LAN).
5. **Tài liệu bàn giao:** Đầy đủ PRD, Kiến trúc hệ thống, Tài liệu đặc tả yêu cầu, Báo cáo phân công công việc.
