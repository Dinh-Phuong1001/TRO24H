# System Architecture & Project Structure - UniStay Android App

## 1. Kiến trúc tổng thể (Overall Architecture)
Dự án áp dụng mô hình **Clean Architecture** kết hợp **MVVM (Model-View-ViewModel)**. Điều này giúp tách biệt UI, logic nghiệp vụ (Business Logic) và quản lý dữ liệu (Data Management) thành các tầng riêng biệt, giúp code dễ test, dễ bảo trì và mở rộng.

### Phân lớp (Layers)
- **Domain Layer:** Chứa các business logic cốt lõi. Lớp này độc lập hoàn toàn với Android framework (chỉ dùng Kotlin thuần). Chứa các Entity, Repository Interfaces và Use Cases (Interactors).
- **Data Layer:** Chịu trách nhiệm lấy/lưu dữ liệu từ các nguồn (Network/Local). Triển khai các Repository Interfaces của Domain. Sử dụng Retrofit cho API, OkHttp cho Interceptor, và EncryptedSharedPreferences cho Local Storage.
- **Presentation Layer (UI):** Chứa các Activity, Fragment, và ViewModel. ViewModel giao tiếp với Domain Layer thông qua Use Cases. Dữ liệu được đẩy về View thông qua Kotlin `StateFlow` hoặc `LiveData`.

## 2. Cây thư mục dự án (Project Structure)
Dự án sẽ được cấu trúc theo Layer kết hợp Feature bên trong (Package by Layer/Feature). Đây là phương pháp phổ biến và tối ưu nhất cho Android App.

```text
com.unistay.android
│
├── di/                     # Dependency Injection modules (Dagger Hilt)
│   ├── NetworkModule.kt
│   ├── RepositoryModule.kt
│   └── AppModule.kt
│
├── domain/                 # Domain Layer (Kotlin thuần, không có Android framework dependency)
│   ├── model/              # Domain Models (User, Room, ChatMessage)
│   ├── repository/         # Interfaces (IUserRepository, IRoomRepository, IChatRepository)
│   └── usecase/            # Các Use Cases (LoginUseCase, GetRoomsUseCase, SendMessageUseCase...)
│
├── data/                   # Data Layer
│   ├── remote/             # Network Code
│   │   ├── api/            # Retrofit API Interfaces (AuthApi, RoomApi, ChatApi)
│   │   ├── dto/            # Data Transfer Objects (LoginResponse, RoomDto)
│   │   └── interceptor/    # AuthInterceptor (Gắn JWT vào header request)
│   ├── local/              # Local Storage Code
│   │   └── datastore/      # EncryptedSharedPreferences (lưu JWT Token)
│   └── repository/         # Implementation của các Repository interfaces (UserRepositoryImpl)
│
├── presentation/           # Presentation Layer (UI & ViewModels)
│   ├── base/               # Base Activity, Base Fragment, Base ViewModel
│   ├── auth/               # Feature: Xác thực (Login/Register)
│   │   ├── LoginFragment.kt
│   │   └── AuthViewModel.kt
│   ├── student/            # Feature: Luồng của Sinh viên
│   │   ├── home/           # Tìm kiếm, lọc phòng
│   │   ├── detail/         # Chi tiết phòng
│   │   ├── chatbot/        # UniBot Chat
│   │   └── passroom/       # Quản lý & đăng bài pass phòng
│   ├── landlord/           # Feature: Luồng của Chủ trọ
│   │   ├── myrooms/        # Danh sách phòng của tôi
│   │   └── manage/         # Thêm, sửa phòng
│   ├── chat/               # Feature: Chat 1-1 (Dùng chung cho cả 2 Role)
│   └── common/             # Các UI components dùng chung (Custom Views, Adapters)
│
└── utils/                  # Các tiện ích chung (Constants, Extensions, Mappers)
```

## 3. Các Công nghệ & Thư viện sử dụng
- **Ngôn ngữ:** Kotlin
- **Kiến trúc:** MVVM + Clean Architecture
- **Dependency Injection:** Dagger Hilt
- **Network:** Retrofit2, OkHttp3, Gson/Moshi
- **Coroutines & Flow:** Xử lý bất đồng bộ và Reactive streams
- **Navigation Component:** Quản lý luồng chuyển màn hình (Single Activity Architecture)
- **Image Loading:** Glide hoặc Coil (Coil được khuyến nghị vì code hoàn toàn bằng Kotlin)
- **Storage:** EncryptedSharedPreferences (Lưu trữ Token bảo mật)

## 4. Quản lý trạng thái (State Management)
- Trong ViewModel, sử dụng `MutableStateFlow` và `StateFlow` để biểu diễn UI State.
- State thường được bọc trong một class `Resource` hoặc `Result` (Sealed Class) có 3 trạng thái: `Loading`, `Success(data)`, `Error(message)`.
- UI (Fragment/Activity) sẽ collect flow này để cập nhật giao diện (Hiển thị ProgressBar, show thông báo lỗi, render danh sách...).

## 5. Xử lý Token & API Security
- **AuthInterceptor:** Mọi HTTP Request gọi qua Retrofit (ngoại trừ Login/Register) sẽ tự động được gắn Header `Authorization: Bearer <JWT_Token>`.
- **Token Expiration Handling:** Nếu nhận response `401 Unauthorized`, ứng dụng cần xử lý đá văng người dùng về màn hình Login hoặc tự động gọi API Refresh Token (nếu backend hỗ trợ).
