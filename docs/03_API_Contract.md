# API Contract & Data Mapping - UniStay System

Tài liệu này xác định các mô hình dữ liệu (Data Models) ở phía Client và các Endpoints API cần thiết. Đồng thời **nêu rõ các thay đổi/chỉnh sửa cần thực hiện ở Backend ASP.NET Core** để đáp ứng được ứng dụng Android.

## 1. Yêu cầu chung với Backend (Đọc kỹ phần này)
Để ứng dụng Android (Mobile Native) hoạt động chuẩn mực với Backend, Backend cần thay đổi các cơ chế sau:
- **[QUAN TRỌNG] Chuyển đổi Auth từ Cookie sang JWT Token:** Mobile app không thể (và không nên) dùng Cookie như Web truyền thống. Backend cần trả về một chuỗi `JWT Token` ở API Login. Các API yêu cầu xác thực sau đó, Mobile sẽ truyền token này vào HTTP Header: `Authorization: Bearer <token>`.
- **Trả về JSON, không trả về View (HTML):** Các chức năng như Chat, Xem phòng, nếu hiện tại Backend đang dùng cơ chế render MVC View thì phải viết thêm các Route API trả về định dạng JSON thuần.
- **Chuẩn hóa Error Response:** Backend nên thống nhất một format lỗi, ví dụ: `{ "status": 400, "message": "Email đã tồn tại", "data": null }`.

---

## 2. Data Mapping (Domain Models - Kotlin Data Class)

### 2.1. User Model
```kotlin
data class User(
    val userId: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: String // "Student" hoặc "Landlord"
)
```

### 2.2. Room Model
```kotlin
data class Room(
    val roomId: String,
    val roomCode: String,
    val title: String,
    val address: String,
    val basePrice: Double,
    val targetUniversity: String,
    val distanceToCampusKm: Double,
    val status: String, // "Available", "Full", "Hidden"
    val maxOccupancy: Int,
    val currentOccupancy: Int,
    val imageUrls: List<String>, // Đổi từ 1 image thành List nếu có thể
    val amenities: List<String>, // VD: ["Wifi", "Điều hòa", "Nóng lạnh"]
    val contactPhone: String,
    val landlordId: String,
    val isTransfer: Boolean = false, // Đánh dấu phòng pass
    val transferByStudentId: String? = null // ID của sinh viên pass phòng (nếu có)
)
```

### 2.3. ChatMessage Model
```kotlin
data class ChatMessage(
    val messageId: String,
    val roomId: String?,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val sentAt: Long // Epoch time để Mobile dễ parse
)
```

---

## 3. Danh sách API (API Contract)

Dưới đây là các API mà Android App sẽ sử dụng qua Retrofit. Các đường dẫn có prefix `/api/`.

### 3.1. Authentication (Xác thực)
| HTTP Method | Endpoint | Mô tả | Request Body | Response Body mong đợi | Chú ý (Cần sửa ở BE) |
|---|---|---|---|---|---|
| `POST` | `/api/auth/login` | Đăng nhập | `{email, password}` | `{ token: "jwt...", user: { ... } }` | **Cần thêm API này trả về JWT.** |
| `POST` | `/api/auth/register` | Đăng ký | `{email, password, fullName, phone, role}` | `{ success: true, message: "..." }` | Trả về JSON, không redirect. |

### 3.2. Room (Phòng trọ)
| HTTP Method | Endpoint | Mô tả | Request Query/Body | Response Body mong đợi | Chú ý (Cần sửa ở BE) |
|---|---|---|---|---|---|
| `GET` | `/api/rooms` | Lấy danh sách & Tìm kiếm phòng | `?university=...&isTransfer=...` | `List<Room>` | Hỗ trợ filter theo isTransfer (phòng pass). |
| `GET` | `/api/rooms/{id}` | Lấy chi tiết phòng | - | `Room` | Trả về chi tiết dạng JSON. |
| `POST` | `/api/rooms` | (Landlord/Student) Đăng phòng | `RoomDto` | `Room` (trả về phòng vừa tạo) | Yêu cầu Header Auth. Chấp nhận Student nếu isTransfer = true. |
| `PUT` | `/api/rooms/{id}` | (Landlord/Student) Sửa phòng | `RoomDto` | `Room` | Yêu cầu Header Auth: Bearer Token. |
| `GET` | `/api/rooms/my-rooms`| (Landlord/Student) Lấy phòng đã đăng | - | `List<Room>` | Lấy danh sách dựa theo Token truyền lên (áp dụng cả phòng pass). |

### 3.3. Chat (Nhắn tin)
*Lưu ý: Nếu làm chat realtime tốt nhất BE nên dùng **SignalR (C#)** kết hợp với API lấy lịch sử cũ.*
| HTTP Method | Endpoint | Mô tả | Request Query/Body | Response Body mong đợi | Chú ý (Cần sửa ở BE) |
|---|---|---|---|---|---|
| `GET` | `/api/chat/conversations`| Lấy ds người đã nhắn tin | - | `List<ConversationDto>` | Group tin nhắn theo người gửi. |
| `GET` | `/api/chat/history/{userId}`| Lấy lịch sử chat với 1 user | - | `List<ChatMessage>` | - |
| `POST` | `/api/chat/send` | Gửi tin nhắn (Nên dùng SignalR thay thế) | `{ receiverId, content }` | `ChatMessage` | - |

### 3.4. UniBot (Chatbot)
| HTTP Method | Endpoint | Mô tả | Request Body | Response Body mong đợi |
|---|---|---|---|---|
| `POST` | `/api/unibot/ask` | Gửi câu hỏi cho bot | `{ query: "Tìm phòng rẻ nhất" }` | `{ reply: "Có 3 phòng...", rooms: [List<Room>] }` | Cần trả về cả text và mảng object nếu có gợi ý. |
