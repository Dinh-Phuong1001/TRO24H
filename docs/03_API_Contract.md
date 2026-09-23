# API Contract & Data Mapping - Hệ Thống TRO24H

Tài liệu này đặc tả chi tiết toàn bộ các mô hình dữ liệu (Data Models) và danh sách Endpoints RESTful API của hệ thống **TRO24H**.

- 🌐 **Cloud Base URL:** `http://tro24h.runasp.net/`
- 📑 **Swagger OpenAPI UI:** `http://tro24h.runasp.net/swagger/index.html`

---

## 1. Mô hình Dữ liệu (Domain Data Models)

### 1.1. User Model (Người dùng)
```kotlin
data class User(
    val userId: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: String, // "Student" hoặc "Landlord"
    val avatarUrl: String? = null
)
```

### 1.2. Room Model (Phòng trọ)
```kotlin
data class Room(
    val roomId: String,
    val title: String,
    val description: String?,
    val address: String,
    val district: String?,
    val targetUniversity: String?,
    val price: Double,
    val area: Double,
    val roomType: String?, // "Tro", "ChungCuMini", "O_Ghep"
    val amenities: String?, // Chuỗi tiện ích: "Wifi, DieuHoa, NongLanh, TuDo"
    val imageUrls: String?, // Danh sách URL ảnh phân cách bởi dấu phẩy
    val contactPhone: String,
    val landlordId: String,
    val isAvailable: Boolean = true,
    val createdAt: String? = null
)
```

### 1.3. Chat & Message Model (Tin nhắn & Phiên trò chuyện)
```kotlin
data class ChatSession(
    val sessionId: String,
    val studentId: String,
    val landlordId: String,
    val roomId: String?,
    val lastMessage: String?,
    val lastMessageTime: String?,
    val unreadCount: Int = 0,
    val otherUserName: String? = null,
    val otherUserAvatar: String? = null
)

data class Message(
    val messageId: String,
    val sessionId: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val sentAt: String,
    val isRead: Boolean = false
)
```

---

## 2. Danh sách Endpoints RESTful API

### 2.1. Phân hệ Xác thực & Tài khoản (`AuthController`, `UsersController`)
| Phương thức | Endpoint | Mô tả chức năng | Request Body | Response mong đợi |
|:---:|---|---|---|---|
| `POST` | `/api/auth/login` | Đăng nhập tài khoản | `{ "email": "...", "password": "..." }` | `{ "user": User, "token": "..." }` |
| `POST` | `/api/auth/register` | Đăng ký tài khoản mới | `{ "fullName": "...", "email": "...", "password": "...", "phoneNumber": "...", "role": "Student/Landlord", "otp": "..." }` | `{ "user": User, "token": "..." }` |
| `POST` | `/api/auth/request-otp` | Gửi mã OTP xác thực email | `{ "email": "..." }` | `{ "message": "OTP đã được gửi" }` |
| `POST` | `/api/auth/change-password` | Đổi mật khẩu tài khoản | `{ "userId": "...", "oldPassword": "...", "newPassword": "..." }` | `200 OK` |
| `POST` | `/api/auth/forgot-password-otp`| Yêu cầu OTP quên mật khẩu | `{ "email": "..." }` | `{ "message": "OTP đã được gửi" }` |
| `POST` | `/api/auth/reset-password` | Đặt lại mật khẩu mới | `{ "email": "...", "otp": "...", "newPassword": "..." }` | `{ "message": "Thành công" }` |
| `PUT` | `/api/users/{id}` | Cập nhật thông tin cá nhân | `{ "fullName": "...", "phoneNumber": "...", "avatarUrl": "..." }` | `User` |
| `DELETE`| `/api/users/{id}` | Xóa tài khoản người dùng | - | `200 OK` |

---

### 2.2. Phân hệ Phòng trọ (`RoomsController`)
| Phương thức | Endpoint | Mô tả chức năng | Tham số / Body | Response |
|:---:|---|---|---|---|
| `GET` | `/api/rooms` | Lấy danh sách & Tìm kiếm phòng | `?university=...&district=...&minPrice=...&maxPrice=...&userId=...` | `List<Room>` |
| `GET` | `/api/rooms/{id}` | Lấy chi tiết phòng theo ID | Path: `{id}` | `Room` |
| `POST` | `/api/rooms` | Chủ trọ đăng bài cho thuê | `Room` | `Room` (vừa tạo) |
| `DELETE`| `/api/rooms/{id}` | Chủ trọ xóa bài đăng phòng | Path: `{id}`, Query: `?userId=...` | `200 OK` |

---

### 2.3. Phân hệ Phòng yêu thích & Lịch sử xem (`SavedRooms`, `HistoryController`)
| Phương thức | Endpoint | Mô tả chức năng | Tham số / Body | Response |
|:---:|---|---|---|---|
| `GET` | `/api/saved-rooms` | Lấy danh sách phòng đã lưu | `?userId=...` | `List<Room>` |
| `POST` | `/api/saved-rooms` | Lưu phòng vào mục yêu thích | `?userId=...&roomId=...` | `SavedRoom` |
| `DELETE`| `/api/saved-rooms` | Bỏ lưu phòng yêu thích | `?userId=...&roomId=...` | `200 OK` |
| `GET` | `/api/view-history` | Xem lịch sử các phòng đã duyệt | `?userId=...` | `List<Room>` |
| `POST` | `/api/view-history` | Ghi nhận 1 lượt xem phòng | `?userId=...&roomId=...` | `200 OK` |

---

### 2.4. Phân hệ Tin nhắn Chat 1-1 (`ChatController`)
| Phương thức | Endpoint | Mô tả chức năng | Tham số / Body | Response |
|:---:|---|---|---|---|
| `GET` | `/api/chat/sessions` | Lấy danh sách các cuộc trò chuyện | `?userId=...` | `List<ChatSession>` |
| `GET` | `/api/chat/messages` | Lấy lịch sử tin nhắn của 1 phiên | `?sessionId=...` | `List<Message>` |
| `POST` | `/api/chat/messages` | Gửi tin nhắn mới | `Message` | `Message` |
| `POST` | `/api/chat/read/{sessionId}` | Đánh dấu đã đọc tin nhắn | Path: `{sessionId}`, Query: `?userId=...` | `200 OK` |

---

### 2.5. Phân hệ Tải lên tệp tin (`UploadController`)
| Phương thức | Endpoint | Mô tả chức năng | Content-Type | Response |
|:---:|---|---|---|---|
| `POST` | `/api/upload` | Tải lên ảnh phòng trọ hoặc avatar | `multipart/form-data` | `{ "imageUrl": "http://tro24h.runasp.net/uploads/..." }` |
