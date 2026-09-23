# Product Requirements Document (PRD) - UniStay Android App

## 1. Tổng quan dự án (Project Overview)
**UniStay** là hệ thống quản lý và tìm kiếm phòng trọ dành cho sinh viên. Ứng dụng Android (Native Client) sẽ đóng vai trò là điểm chạm trực tiếp với người dùng, kết nối Sinh viên có nhu cầu thuê trọ với các Chủ trọ thông qua các tính năng tìm kiếm, gợi ý và trao đổi trực tiếp (Chat).

## 2. Đối tượng người dùng (Target Audience)
Hệ thống phân chia thành 2 vai trò (Role) cốt lõi:
1. **Sinh viên (Student):** Người dùng tìm kiếm phòng trọ. Ưu tiên các tính năng tìm kiếm nhanh, lọc theo vị trí (trường đại học) và giá cả, xem hình ảnh phòng và liên hệ chủ trọ nhanh chóng.
2. **Chủ trọ (Landlord):** Người cung cấp phòng trọ. Ưu tiên sự đơn giản trong việc tạo/quản lý bài đăng phòng, cập nhật tình trạng phòng và tương tác với người có nhu cầu thuê.

---

## 3. User Stories & Use Cases

### 3.1. Dành cho Sinh viên (Student)
| ID | Tính năng | User Story | Use Case (Hành động cụ thể) |
|---|---|---|---|
| STU-01 | **Authentication** | Là sinh viên, tôi muốn tạo tài khoản và đăng nhập an toàn vào ứng dụng để trải nghiệm các tính năng cá nhân hóa. | - Đăng ký tài khoản (Role: Student).<br>- Đăng nhập bằng Email/Password.<br>- Đăng xuất. |
| STU-02 | **Khám phá & Tìm kiếm** | Là sinh viên, tôi muốn xem danh sách các phòng trọ đang trống và lọc chúng theo các tiêu chí cụ thể để tìm được phòng ưng ý. | - Xem danh sách phòng (Home Feed).<br>- Lọc phòng theo: Trường Đại học mục tiêu, Khoảng cách (km), Mức giá, Tiện ích, **Phòng Pass**.<br>- Tìm kiếm phòng theo mã (RoomCode) hoặc khu vực. |
| STU-03 | **Chi tiết phòng** | Là sinh viên, tôi muốn xem đầy đủ thông tin về một căn phòng trước khi quyết định liên hệ. | - Xem hình ảnh (ImageUrl), giá cả (BasePrice), địa chỉ, tiện ích (Amenities), sức chứa (Occupancy).<br>- Kiểm tra trạng thái phòng (Còn chỗ/Hết chỗ). |
| STU-04 | **Chat với Chủ trọ** | Là sinh viên, tôi muốn nhắn tin trực tiếp với chủ trọ để hỏi thêm thông tin hoặc hẹn lịch xem phòng. | - Bấm "Liên hệ/Chat" từ màn hình chi tiết phòng.<br>- Gửi/Nhận tin nhắn văn bản.<br>- Xem lại lịch sử các cuộc hội thoại. |
| STU-05 | **Chatbot (UniBot)** | Là sinh viên, tôi muốn có một trợ lý ảo hỗ trợ gợi ý phòng nhanh dựa trên nhu cầu của tôi mà không cần tự tìm kiếm thủ công. | - Mở giao diện chat với UniBot.<br>- Gửi yêu cầu (VD: "Tìm phòng gần Bách Khoa giá dưới 3 triệu").<br>- Nhận kết quả gợi ý. |
| STU-06 | **Pass lại phòng** | Là sinh viên, tôi muốn đăng thông tin nhượng lại (pass) phòng trọ mà tôi đang thuê cho người khác. | - Tạo bài đăng pass phòng (điền thông tin cơ bản, hình ảnh).<br>- Xem và quản lý các phòng mình đang pass.<br>- Chat với người có nhu cầu thuê lại. |

### 3.2. Dành cho Chủ trọ (Landlord)
| ID | Tính năng | User Story | Use Case (Hành động cụ thể) |
|---|---|---|---|
| LND-01 | **Authentication** | Là chủ trọ, tôi muốn đăng nhập vào hệ thống để quản lý các bất động sản của mình. | - Đăng ký tài khoản (Role: Landlord).<br>- Đăng nhập / Đăng xuất. |
| LND-02 | **Quản lý phòng trọ** | Là chủ trọ, tôi muốn đăng thông tin về các phòng trọ của mình lên hệ thống để sinh viên có thể tiếp cận. | - Đăng tin mới (Tạo Room với các thông tin: Address, Price, Images, TargetUniversity, MaxOccupancy, v.v.).<br>- Xem danh sách phòng do chính mình đã đăng. |
| LND-03 | **Cập nhật trạng thái** | Là chủ trọ, tôi muốn dễ dàng cập nhật thông tin phòng (ví dụ: đã cho thuê hết) để không bị làm phiền. | - Chỉnh sửa thông tin phòng (Update Room).<br>- Cập nhật số người ở hiện tại (CurrentOccupancy).<br>- Đổi trạng thái (Status: Đang trống, Đã đầy, Tạm ẩn). |
| LND-04 | **Tương tác khách hàng**| Là chủ trọ, tôi muốn nhận và phản hồi tin nhắn của sinh viên quan tâm đến phòng của tôi. | - Nhận thông báo tin nhắn mới.<br>- Phản hồi tin nhắn của sinh viên.<br>- Quản lý danh sách sinh viên đang liên hệ. |

---

## 4. Luồng màn hình chính (Screen Flow / User Journey)

### 4.1. Luồng Xác thực (Chung cho 2 Roles)
- **Splash Screen** -> Kiểm tra JWT Token lưu trong EncryptedSharedPreferences.
  - *Nếu Token hợp lệ:* Giải mã Token lấy Role -> Chuyển hướng tới **Main Screen** tương ứng.
  - *Nếu Token hết hạn/Chưa có:* Chuyển tới **Login Screen**.
- **Login Screen** <-> **Register Screen** (Lựa chọn Role khi đăng ký).

### 4.2. Luồng Sinh viên (Student App Navigation)
Sử dụng **Bottom Navigation** với 3 Tab chính:

1. **Tab "Home" (Tìm kiếm phòng):**
   - Mặc định hiển thị danh sách các phòng trọ gợi ý / mới nhất (Recycler View).
   - Thanh Search Bar ở trên cùng + Nút **Filter**.
   - Bấm Filter -> Mở **Filter BottomSheet** (Chọn Trường, Slider Giá, v.v.).
   - Bấm vào một Item Phòng -> Chuyển sang **Room Detail Screen**.
   - Tại **Room Detail Screen** -> Bấm nút [Chat với chủ trọ] -> Chuyển sang màn hình **1-1 Chat Screen**.

2. **Tab "Messages" (Tin nhắn):**
   - Hiển thị danh sách các cuộc hội thoại gần đây (Inbox).
   - Nút Floating Action Button (FAB) hình Robot -> Bấm vào để chuyển sang màn hình **UniBot Chat Screen**.
   - Bấm vào một hội thoại bất kỳ -> Chuyển sang màn hình **1-1 Chat Screen**.

3. **Tab "Profile" (Cá nhân):**
   - Hiển thị thông tin user (Avatar, FullName, Email).
   - **Quản lý phòng Pass:** Nơi sinh viên đăng bài và quản lý các phòng đang pass.
   - Lịch sử xem phòng / Phòng đã lưu (Tính năng mở rộng sau này).
   - Nút Đăng xuất.

### 4.3. Luồng Chủ trọ (Landlord App Navigation)
Sử dụng **Bottom Navigation** với 3 Tab chính:

1. **Tab "My Rooms" (Quản lý phòng):**
   - Hiển thị danh sách các phòng mà chủ trọ đang sở hữu (List/Grid View).
   - Nút FAB `[+]` -> Chuyển sang màn hình **Create/Add Room Screen** (Form nhập thông tin phòng).
   - Bấm vào một phòng -> Chuyển sang màn hình **Room Detail (Edit Mode)** để sửa thông tin hoặc cập nhật trạng thái.

2. **Tab "Messages" (Tin nhắn):**
   - Tương tự sinh viên, hiển thị danh sách các sinh viên đang liên hệ hỏi phòng.
   - Bấm vào để **Chat 1-1** giải đáp thắc mắc.
   - *(Chủ trọ không cần nút chat với UniBot).*

3. **Tab "Profile" (Cá nhân):**
   - Hiển thị thông tin liên hệ (ContactPhone, Email, FullName) để sinh viên có thể thấy.
   - Nút Đăng xuất.
