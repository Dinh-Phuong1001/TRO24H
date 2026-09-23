# Implementation Plan (Lộ trình phát triển code) - UniStay Android

Tài liệu này chia nhỏ quá trình code Android App thành các giai đoạn (Sprints) hợp lý để dễ dàng theo dõi và kiểm thử.

## Giai đoạn 1: Setup & Cơ sở hạ tầng (1 - 2 ngày)
**Mục tiêu:** Cấu hình khung sườn cho ứng dụng, thiết lập Clean Architecture.
- Khởi tạo project Android Studio (Kotlin, Min SDK 24+).
- Setup các thư viện cần thiết (build.gradle): Retrofit, OkHttp, Dagger Hilt, Coroutines, Navigation Component, Glide/Coil.
- Cấu hình cây thư mục theo chuẩn (di, domain, data, presentation).
- Thiết lập Dagger Hilt: Tạo `AppModule`, `NetworkModule` (Cấu hình Base URL cho Retrofit).
- Thiết lập `AuthInterceptor` cơ bản (chuẩn bị cho việc gắn Token sau này).

## Giai đoạn 2: Data Layer & Domain Layer cơ bản (2 - 3 ngày)
**Mục tiêu:** Xây dựng Data Models, API Interfaces và Repositories.
- Khai báo các Domain Models (User, Room, ChatMessage).
- Khai báo các DTOs (Data Transfer Objects) tương ứng cho request/response mạng.
- Viết các API Interfaces (`AuthApi`, `RoomApi`).
- Triển khai Repositories (`AuthRepositoryImpl`, `RoomRepositoryImpl`).
- Viết các UseCases lõi (`LoginUseCase`, `GetRoomsUseCase`).

## Giai đoạn 3: Authentication Feature (2 - 3 ngày)
**Mục tiêu:** Hoàn thiện luồng Đăng nhập/Đăng ký.
- Thiết kế UI (XML Layouts) cho màn hình Splash, Login, Register.
- Xây dựng `AuthViewModel` kết nối với `LoginUseCase`.
- Xử lý lưu JWT Token vào `EncryptedSharedPreferences` sau khi login thành công.
- Cập nhật lại `AuthInterceptor` để đọc Token từ Local Storage và nhét vào Header.
- Phân luồng điều hướng: Sau khi login, kiểm tra Role để chuyển tới Home của Student hoặc MyRooms của Landlord.

## Giai đoạn 4: Student Flow - Khám phá phòng trọ & Pass phòng (4 - 6 ngày)
**Mục tiêu:** Sinh viên có thể xem, lọc phòng và đăng tin pass phòng.
- Xây dựng Main Activity với Bottom Navigation (Home, Chat, Profile).
- Màn hình **Home:** Hiển thị danh sách phòng (RecyclerView), tích hợp Pagination. Thêm filter cho "Phòng Pass".
- Tính năng **Filter:** Tạo BottomSheet để lọc theo Giá, Trường ĐH, Loại phòng.
- Màn hình **Room Detail:** Hiển thị hình ảnh chi tiết, mô tả, tiện ích. Bố trí nút "Chat với Chủ trọ" (hoặc Sinh viên pass phòng).
- **Tính năng Pass phòng:** Thêm UI cho màn hình tạo bài pass phòng và danh sách quản lý phòng pass trong tab Profile.

## Giai đoạn 5: Landlord Flow - Quản lý phòng trọ (3 - 4 ngày)
**Mục tiêu:** Chủ trọ có thể đăng và quản lý phòng.
- Xây dựng luồng Main Activity riêng cho Landlord.
- Màn hình **My Rooms:** Hiển thị các phòng của chính chủ trọ đó (Gọi API `/my-rooms`).
- Màn hình **Create Room:** Form nhập thông tin phòng mới (Validation dữ liệu).
- Màn hình **Edit Room:** Sửa trạng thái (Còn/Hết phòng) và cập nhật thông tin.

## Giai đoạn 6: Chat 1-1 & Chatbot UniBot (3 - 5 ngày)
**Mục tiêu:** Xử lý tương tác thời gian thực hoặc polling.
- Thiết kế UI cho màn hình Chat (Bong bóng tin nhắn của mình ở bên phải, người kia bên trái).
- Tích hợp SignalR Client (Nếu Backend dùng SignalR) hoặc gọi API Polling (gọi định kỳ mỗi vài giây) để lấy tin nhắn mới.
- Xây dựng màn hình **Inbox:** Danh sách các cuộc hội thoại.
- Xây dựng màn hình **UniBot:** Form chat riêng gọi API của Bot, render tin nhắn Bot trả lời.

## Giai đoạn 7: Hoàn thiện & Đánh bóng (2 ngày)
- Bắt và hiển thị lỗi (Error handling) thân thiện: Mất mạng, Lỗi server 500, Token hết hạn.
- Thêm hiệu ứng UI (Skeleton Loading, Swipe to Refresh).
- Tạo icon app, chuẩn bị file APK.
