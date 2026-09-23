# Product Requirements Document (PRD) - Ứng Dụng TRO24H

## 1. Tổng quan dự án (Project Overview)
**TRO24H** là giải pháp công nghệ toàn diện hỗ trợ sinh viên và người thuê trọ tìm kiếm phòng trọ an toàn, giá cả minh bạch và gần các cụm trường Đại học / Cao đẳng. Đồng thời, ứng dụng cung cấp cho các Chủ nhà trọ một nền tảng quản lý tin đăng, tiếp cận khách hàng tiềm năng và trao đổi trực tiếp qua tính năng Nhắn tin (Chat 1-1).

Hệ thống bao gồm:
- **Ứng dụng di động (Android Native - Kotlin):** Điểm chạm tương tác trực tiếp của người dùng (Sinh viên và Chủ trọ).
- **Máy chủ ứng dụng (ASP.NET Core 8.0 Web API):** Xử lý nghiệp vụ, quản lý dữ liệu tập trung và bảo mật.
- **Cơ sở dữ liệu đám mây (Microsoft SQL Server):** Lưu trữ quan hệ chuẩn hóa 6 bảng dữ liệu.

---

## 2. Đối tượng người dùng (Target Audience & Actors)
Hệ thống phân chia quyền hạn rõ ràng thành 3 nhóm đối tượng:

1. **Sinh viên / Người đi thuê (Student):**
   - Tìm kiếm phòng trọ nhanh chóng theo khu vực trường học, mức giá và tiện ích.
   - Xem chi tiết hình ảnh, giá thuê, địa chỉ, thông tin tiện nghi.
   - Lưu trữ các phòng trọ yêu thích (`SavedRooms`) và xem lại lịch sử các phòng đã duyệt (`ViewHistory`).
   - Nhắn tin trực tiếp 1-1 với Chủ trọ để giải đáp thắc mắc hoặc hẹn lịch xem phòng.
   - *Ghi chú:* Sinh viên không có quyền đăng tin cho thuê phòng.

2. **Chủ nhà trọ (Landlord):**
   - Đăng tin cho thuê phòng trọ mới với đầy đủ mô tả, giá cả, tiện ích và hình ảnh.
   - Quản lý danh sách các phòng trọ do mình đăng (`MyRooms`), cập nhật thông tin hoặc trạng thái phòng (Đang trống, Đã cho thuê).
   - Tiếp nhận và phản hồi tin nhắn của sinh viên quan tâm đến phòng trọ.

3. **Khách vãng lai (Guest):**
   - Xem danh sách phòng nổi bật tại trang chủ. Khi cần xem thông tin liên hệ, lưu phòng hoặc nhắn tin, hệ thống sẽ yêu cầu Đăng nhập / Đăng ký.

---

## 3. Danh sách User Stories & Use Cases chi tiết

### 3.1. Phân hệ Xác thực & Tài khoản (Authentication)
| Mã US | Tên tính năng | User Story | Mô tả hành động (Use Case) |
|---|---|---|---|
| **AUTH-01** | Đăng ký tài khoản | Là người dùng mới, tôi muốn tạo tài khoản với vai trò rõ ràng để sử dụng đúng quyền hạn. | Điền Họ tên, Email, Số điện thoại, Mật khẩu và chọn vai trò: **Sinh viên** hoặc **Chủ trọ**. |
| **AUTH-02** | Đăng nhập hệ thống | Là người dùng, tôi muốn đăng nhập an toàn bằng Email và Mật khẩu. | Nhập Email/Password -> Xác thực mật khẩu băm -> Lưu thông tin phiên vào DataStore (`UserSessionManager`). |
| **AUTH-03** | Đăng xuất | Là người dùng, tôi muốn đăng xuất khỏi thiết bị. | Xóa dữ liệu phiên làm việc cục bộ, quay về màn hình Đăng nhập hoặc Khách. |
| **AUTH-04** | Quản lý thông tin cá nhân | Là người dùng, tôi muốn cập nhật thông tin và đổi mật khẩu. | Cập nhật Họ tên, Số điện thoại, Ảnh đại diện (`EditProfileActivity`), Đổi mật khẩu tài khoản. |

### 3.2. Phân hệ Sinh viên (Student Module)
| Mã US | Tên tính năng | User Story | Mô tả hành động (Use Case) |
|---|---|---|---|
| **STU-01** | Trang chủ & Khám phá | Là sinh viên, tôi muốn xem các phòng trọ mới nhất và phòng nổi bật gần trường đại học. | Xem danh sách phòng (Home Feed), hiển thị ảnh bìa, giá thuê (VNĐ/tháng), địa chỉ, khoảng cách. |
| **STU-02** | Tìm kiếm & Bộ lọc động | Là sinh viên, tôi muốn tìm phòng theo tên trường, khoảng giá hoặc tiện ích cụ thể. | Nhập từ khóa tìm kiếm; Áp dụng bộ lọc (Trường ĐH, Mức giá từ - đến, tiện ích điều hòa, wifi, nóng lạnh...). |
| **STU-03** | Xem chi tiết phòng | Là sinh viên, tôi muốn xem đầy đủ thông tin phòng trọ trước khi quyết định liên hệ. | Xem danh sách ảnh phòng, mô tả chi tiết, tiện nghi, thông tin chủ trọ. Các nút hành động: Gọi điện, Nhắn tin, Lưu phòng. |
| **STU-04** | Quản lý phòng đã lưu | Là sinh viên, tôi muốn lưu lại các phòng ưng ý để so sánh. | Bấm nút Yêu thích (Bookmark); xem lại toàn bộ phòng đã lưu trong mục `SavedRoomsActivity`. |
| **STU-05** | Lịch sử xem phòng | Là sinh viên, tôi muốn xem lại các phòng mình vừa ghé xem gần đây. | Hệ thống tự động ghi nhận khi vào xem chi tiết; sinh viên có thể xem lại tại `ViewHistoryActivity`. |
| **STU-06** | Nhắn tin với Chủ trọ | Là sinh viên, tôi muốn nhắn tin trực tiếp với chủ phòng để hỏi thêm thông tin. | Bấm "Nhắn tin" từ trang chi tiết phòng -> Mở màn hình chat 1-1 (`ChatActivity`) trao đổi trực tiếp. |

### 3.3. Phân hệ Chủ trọ (Landlord Module)
| Mã US | Tên tính năng | User Story | Mô tả hành động (Use Case) |
|---|---|---|---|
| **LND-01** | Đăng tin phòng trọ mới | Là chủ trọ, tôi muốn đăng tin cho thuê phòng để sinh viên có thể tiếp cận. | Mở màn hình `CreateRoomFragment`, nhập: Tiêu đề, Địa chỉ, Trường ĐH mục tiêu, Giá thuê, Diện tích, Tiện ích, Link ảnh. |
| **LND-02** | Quản lý danh sách phòng | Là chủ trọ, tôi muốn xem lại tất cả các bài đăng của mình. | Xem danh sách phòng đã đăng tại `MyRoomsActivity`; kiểm tra lượt quan tâm và trạng thái phòng. |
| **LND-03** | Chỉnh sửa & Cập nhật trạng thái | Là chủ trọ, tôi muốn cập nhật tình trạng phòng khi đã có người thuê. | Sửa thông tin giá/mô tả; cập nhật trạng thái: Đang trống (Available) hoặc Đã cho thuê (Full). |
| **LND-04** | Tiếp nhận tin nhắn khách hàng | Là chủ trọ, tôi muốn xem danh sách khách thuê đang liên hệ và trả lời tin nhắn. | Truy cập Hộp thư (`ChatListFragment`), xem tin nhắn từ các sinh viên, mở `ChatActivity` để phản hồi. |

---

## 4. Luồng điều hướng người dùng (User Navigation Flow)

### 4.1. Điều hướng theo Vai trò (Role-based Navigation)
Ứng dụng sử dụng thanh điều hướng dưới cùng (**Bottom Navigation Bar**) với các tab được tối ưu hóa:

- **Dành cho Sinh viên:**
  1. 🏠 **Trang chủ (Home):** Danh sách phòng mới, thanh tìm kiếm nhanh, danh mục phòng theo trường.
  2. 🔍 **Tìm kiếm (Search):** Tìm kiếm chuyên sâu theo từ khóa và bộ lọc đa tiêu chí.
  3. 💬 **Tin nhắn (Messages):** Danh sách các cuộc trò chuyện đang có với chủ trọ (kèm huy hiệu tin chưa đọc).
  4. 👤 **Cá nhân (Profile):** Thông tin tài khoản, Phòng đã lưu, Lịch sử xem phòng, Đổi mật khẩu, Đăng xuất.

- **Dành cho Chủ trọ:**
  1. 🏠 **Trang chủ (Home):** Xem giao diện hiển thị chung của toàn hệ thống.
  2. ➕ **Đăng tin (Create Room):** Form đăng bài cho thuê phòng mới.
  3. 💬 **Tin nhắn (Messages):** Quản lý các tin nhắn hỏi phòng từ sinh viên.
  4. 👤 **Cá nhân (Profile):** Quản lý danh sách phòng đã đăng (`MyRooms`), Thông tin chủ nhà, Đăng xuất.

---

## 5. Yêu cầu phi chức năng (Non-Functional Requirements)
1. **Bảo mật (Security):** Mật khẩu người dùng được băm an toàn; phiên người dùng quản lý qua DataStore mã hóa.
2. **Hiệu năng & Tốc độ (Performance):** Tải danh sách phòng nhanh chóng, ảnh được cache mượt mà qua thư viện Coil, không gây giật lag UI khi cuộn RecyclerView.
3. **Tính khả dụng (Usability):** Giao diện chuẩn Material 3, màu sắc hiện đại, hỗ trợ tiếng Việt có dấu hoàn chỉnh và thông báo lỗi rõ ràng.
