# Kế Hoạch Kỹ Thuật & Lộ Trình Phát Triển - Ứng Dụng TRO24H

Tài liệu này xác định lộ trình kỹ thuật phát triển toàn diện hệ sinh thái **TRO24H** (Bao gồm Android Native Client và Backend ASP.NET Core 8.0 Web API).

---

## Giai đoạn 1: Thiết lập Cơ sở hạ tầng & Cấu trúc Dự án
**Mục tiêu:** Hoàn thiện khung sườn, quy chuẩn kiến trúc và kết nối môi trường.
- **Android Client:**
  - Khởi tạo dự án Android Studio (Kotlin, Min SDK 24, Target SDK 34).
  - Tích hợp các thư viện: Retrofit 2, OkHttp 3, ViewBinding, Coil, Jetpack DataStore, Coroutines & Flow.
  - Phân chia cấu trúc Clean Architecture: `di`, `domain`, `data`, `presentation`.
  - Cấu hình Retrofit trỏ tới máy chủ API: `http://tro24h.runasp.net/`.
- **Backend API:**
  - Khởi tạo dự án ASP.NET Core 8.0 Web API.
  - Tích hợp Entity Framework Core 8 và Swagger OpenAPI.

---

## Giai đoạn 2: Thiết kế Cơ sở dữ liệu & Xây dựng Core API
**Mục tiêu:** Hoàn thiện 6 bảng CSDL và các API dịch vụ.
- Thiết kế 6 bảng quan hệ: `Users`, `Rooms`, `SavedRooms`, `ViewHistories`, `ChatSessions`, `Messages`.
- Cấu hình chuỗi kết nối và sinh CSDL trên Microsoft SQL Server Cloud.
- Viết `AuthController`: Đăng nhập, Đăng ký kèm mã OTP, Đổi mật khẩu, Quản lý tài khoản.
- Viết `RoomsController`: Lấy danh sách, Lọc đa tiêu chí, Thêm bài đăng, Xóa bài đăng.
- Viết `HistoryController` & `SavedRooms`: Xử lý lưu phòng và lịch sử xem.
- Viết `UploadController`: Tiếp nhận upload hình ảnh phòng trọ.

---

## Giai đoạn 3: Phân hệ Xác thực & Quản lý Tài khoản Mobile
**Mục tiêu:** Người dùng đăng ký, đăng nhập và phân quyền rõ ràng.
- Xây dựng giao diện: `SplashActivity`, `LoginActivity`, `RegisterActivity`, `TermsActivity`.
- Xử lý phân luồng theo vai trò: **Sinh viên** hoặc **Chủ trọ**.
- Lưu trữ phiên đăng nhập bền vững với `UserSessionManager` (DataStore).
- Xây dựng màn hình `EditProfileActivity`, `SettingsActivity` và BottomSheet Đổi mật khẩu.

---

## Giai đoạn 4: Phân hệ Tìm kiếm & Xem Chi tiết Phòng trọ
**Mục tiêu:** Sinh viên duyệt tìm và xem phòng trực quan.
- **Màn hình Home (`HomeFragment`):** Hiển thị danh sách phòng mới nhất, danh mục theo trường ĐH qua `RoomAdapter`.
- **Màn hình Search (`SearchFragment`, `SearchResultActivity`):** Bộ lọc đa năng theo giá, trường học, khu vực, tiện ích.
- **Màn hình Room Detail (`RoomDetailActivity`):** Hiển thị chi tiết hình ảnh, địa chỉ, mức giá, nút gọi điện, nút lưu phòng và nút chat trực tiếp với chủ trọ.
- Tự động gọi API ghi nhận lịch sử vào `ViewHistory`.

---

## Giai đoạn 5: Phân hệ Chủ trọ & Quản lý Đăng tin
**Mục tiêu:** Chủ trọ đăng và quản lý phòng trọ thuận tiện.
- **Màn hình Create Room (`CreateRoomFragment`):** Form nhập thông tin phòng, chọn tiện ích, tải lên ảnh phòng trọ.
- **Màn hình My Rooms (`MyRoomsActivity`):** Danh sách các phòng do chính chủ trọ đăng bài, cho phép cập nhật hoặc xóa bài đăng.

---

## Giai đoạn 6: Phân hệ Nhắn tin Trực tiếp (Chat 1-1)
**Mục tiêu:** Kết nối trao đổi tức thì giữa Sinh viên và Chủ nhà.
- **Màn hình Inbox (`ChatListFragment`):** Hiển thị danh sách các phiên trò chuyện, tin nhắn mới nhất và huy hiệu đếm tin chưa đọc (Unread badge).
- **Màn hình Chat 1-1 (`ChatActivity`):** Hiển thị danh sách tin nhắn gửi/nhận (`ChatMessageAdapter`), gửi tin nhắn mới và cập nhật trạng thái đã đọc (`markAsRead`).

---

## Giai đoạn 7: Triển khai Đám mây & Đóng gói Nghiệm thu
**Mục tiêu:** Hệ thống hoạt động trực tuyến 24/7 và sẵn sàng báo cáo.
- Triển khai máy chủ Web API và CSDL MSSQL lên MonsterASP Cloud (`http://tro24h.runasp.net`).
- Kiểm thử tích hợp toàn diện trên các thiết bị Android vật lý.
- Đóng gói file cài đặt APK.
- Soạn thảo tài liệu báo cáo đặc tả SRS hoàn chỉnh (`BaoCao_DacTaYeuCau_Tro24H_Moi.docx`).
