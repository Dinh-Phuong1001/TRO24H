# 📖 HƯỚNG DẪN CÔNG DỤNG & CÁCH SỬ DỤNG SWAGGER API (TRO24H)

> **Học phần:** Triển khai dự án phần mềm (Mã chuyên đề: ST-01)  
> **Dự án:** Ứng dụng tìm kiếm và quản lý phòng trọ sinh viên TRO24H  
> **Địa chỉ Swagger UI trực tuyến:** [http://tro24h.runasp.net/swagger/index.html](http://tro24h.runasp.net/swagger/index.html)  
> **Mã nguồn kết nối Android:** `app/src/main/java/com/unistay/android/data/remote/RetrofitClient.kt`

---

## 📌 1. SWAGGER UI LÀ GÌ & CÔNG DỤNG TRONG DỰ ÁN

### 1.1. Định nghĩa
**Swagger UI** là một công cụ mã nguồn mở dựa trên chuẩn **OpenAPI Specification (OAS 3.0)**, được tích hợp trực tiếp vào máy chủ Backend ASP.NET Core 8.0 của TRO24H. Nó tự động phân tích các Controller (`AuthController`, `RoomsController`, `ChatController`) để sinh ra một giao diện Web trực quan.

### 1.2. Các công dụng cốt lõi
1. **Tài liệu API sống (Interactive Living Documentation):**
   - Không cần viết tài liệu word thủ công dễ bị lỗi thời. Khi backend thêm hoặc sửa API, giao diện Swagger tự động cập nhật ngay lập tức.
   - Hiển thị đầy đủ đường dẫn URL, kiểu dữ liệu gửi đi (Request Body) và cấu trúc dữ liệu trả về (Response JSON).
2. **Kiểm thử máy chủ độc lập (API Testing & Debugging):**
   - Cho phép lập trình viên và Tester chạy thử nghiệm trực tiếp các chức năng (Đăng ký, Đăng nhập, Lọc phòng, Gửi tin nhắn) ngay trên trình duyệt web.
   - **Không cần bật Android Studio** và **không cần cài đặt phần mềm bên thứ ba như Postman**.
3. **Cầu nối chuẩn hóa giữa Mobile (Android) và Backend (.NET 8):**
   - Giúp lập trình viên Mobile nhìn vào đây để viết đúng các hàm trong `UniStayApi.kt` và cấu hình trong `RetrofitClient.kt`.
4. **Vũ khí ghi điểm khi bảo vệ Đồ án / Bài tập lớn:**
   - Chứng minh hệ thống được xây dựng theo kiến trúc Client - Server chuẩn công nghiệp.
   - Thao tác gửi/nhận dữ liệu thực tế trước mặt hội đồng giảng viên để chứng minh dữ liệu được lưu bền vững vào CSDL MS SQL Server Cloud.

---

## 🎨 2. Ý NGHĨA CÁC MÀU SẮC & PHƯƠNG THỨC HTTP TRÊN SWAGGER

Mỗi API trên màn hình Swagger được gắn một màu sắc và phương thức chuẩn RESTful:

| Phương thức | Màu hiển thị | Bản chất kỹ thuật | Ứng dụng thực tế trong TRO24H |
|:---:|:---:|:---|:---|
| **POST** | 🟢 **Xanh lá cây** | **Tạo mới hoặc gửi dữ liệu** cần xử lý bảo mật lên server. | • `/api/auth/register`: Đăng ký tài khoản mới.<br>• `/api/auth/login`: Xác thực đăng nhập.<br>• `/api/rooms`: Chủ trọ đăng bài phòng mới.<br>• `/api/chat/send`: Gửi tin nhắn mới. |
| **GET** | 🔵 **Xanh dương** | **Lấy và truy vấn dữ liệu** từ server về máy (không làm thay đổi dữ liệu server). | • `/api/rooms`: Lấy danh sách bài đăng phòng.<br>• `/api/rooms/{id}`: Xem chi tiết 1 phòng trọ.<br>• `/api/chat/sessions`: Lấy danh sách hội thoại. |
| **PUT / PATCH**| 🟠 **Màu cam** | **Cập nhật / chỉnh sửa** dữ liệu đã tồn tại trên CSDL. | • `/api/auth/change-password`: Đổi mật khẩu.<br>• `/api/rooms/{id}`: Chủ trọ sửa thông tin phòng.<br>• Đánh dấu tin nhắn đã đọc (`markAsRead`). |
| **DELETE** | 🔴 **Màu đỏ** | **Xóa vĩnh viễn** dữ liệu khỏi hệ thống. | • `/api/rooms/{id}`: Xóa bài đăng phòng trọ.<br>• Xóa phiên trò chuyện hoặc danh sách xem. |

---

## 🛠️ 3. HƯỚNG DẪN TỪNG BƯỚC CÁCH TEST TRỰC TIẾP TRÊN SWAGGER

### 👉 Kịch bản 1: Kiểm tra lấy danh sách & tìm kiếm phòng trọ (`GET /api/rooms`)
1. Truy cập [http://tro24h.runasp.net/swagger/index.html](http://tro24h.runasp.net/swagger/index.html).
2. Cuộn chuột xuống nhóm **Rooms**, bấm vào thanh màu xanh: `GET /api/rooms`.
3. Bấm nút **Try it out** ở góc trên bên phải.
4. Nhập tham số lọc thử nghiệm (hoặc để trống để lấy tất cả):
   - `university`: Nhập `UTC` (Đại học GTVT) hoặc `FTU` (Ngoại thương).
   - `priceMax`: Nhập `3500000` (tìm phòng dưới 3.5 triệu).
5. Bấm nút màu xanh lớn: **Execute**.
6. Cuộn xuống xem ô **Responses**:
   - Thấy **Code 200** kèm mảng JSON chứa danh sách các phòng trọ thực tế, địa chỉ, số điện thoại chủ trọ và link ảnh -> **API hoạt động hoàn hảo**.

---

### 👉 Kịch bản 2: Kiểm tra Đăng nhập tài khoản (`POST /api/auth/login`)
1. Bấm vào thanh màu xanh lá: `POST /api/auth/login` trong nhóm **Auth**.
2. Bấm nút **Try it out**.
3. Tại ô soạn thảo **Request body**, dán thông tin tài khoản mẫu:
   ```json
   {
     "email": "student@unistay.com",
     "password": "Password123@"
   }
   ```
4. Bấm nút **Execute**.
5. Kết quả trả về tại ô **Response body**:
   ```json
   {
     "success": true,
     "message": "Đăng nhập thành công!",
     "data": {
       "userId": 1,
       "fullName": "Nguyễn Văn Sinh Viên",
       "role": "Student",
       "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
     }
   }
   ```

---

## 🚦 4. BẢNG MÃ TRẠNG THÁI PHẢN HỒI (HTTP STATUS CODES)

Khi bấm **Execute**, Swagger sẽ hiển thị mã trạng thái (Code) giúp bạn nhận biết kết quả:

* 🟢 **200 OK:** Yêu cầu thành công, dữ liệu trả về chính xác.
* 🟢 **201 Created:** Tạo mới bản ghi thành công (thường gặp khi đăng ký tài khoản hoặc đăng bài mới).
* 🟡 **400 Bad Request:** Lỗi do dữ liệu gửi lên (thiếu trường bắt buộc, sai định dạng email, mật khẩu không đủ độ dài).
* 🔴 **401 Unauthorized:** Lỗi xác thực (sai mật khẩu hoặc chưa truyền Token xác thực).
* 🔴 **404 Not Found:** Không tìm thấy dữ liệu (ví dụ: tìm kiếm ID phòng trọ không tồn tại trong CSDL).
* ⚫ **500 Internal Server Error:** Lỗi phía máy chủ Backend hoặc đứt kết nối CSDL MS SQL Server Cloud.

---

## 📱 5. CÁCH ỨNG DỤNG ANDROID KẾT NỐI VÀO SWAGGER QUA `RetrofitClient.kt`

Trong mã nguồn Android `app/src/main/java/com/unistay/android/data/remote/RetrofitClient.kt`, ứng dụng giao tiếp với máy chủ Swagger thông qua cấu hình sau:

```kotlin
object RetrofitClient {
    // 1. Địa chỉ gốc trỏ thẳng tới máy chủ Swagger Cloud
    private const val BASE_URL = "http://tro24h.runasp.net/"

    // 2. Hàm cấu hình OkHttpClient đặc biệt (Bypass SSL / Trust All Certificates)
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            // Tạo TrustManager tin cậy tất cả chứng chỉ
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            
            return OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true } // Chấp nhận mọi tên miền máy chủ
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    // 3. Khởi tạo đối tượng UniStayApi theo cơ chế Singleton (Lazy Initialization)
    val instance: UniStayApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create()) // Tự động chuyển JSON thành Data Class Kotlin
            .build()
            .create(UniStayApi::class.java)
    }
}
```

### 🔍 Giải thích kỹ thuật đoạn code trên:
1. `BASE_URL = "http://tro24h.runasp.net/"`: Là địa chỉ máy chủ Cloud MonsterASP nơi API đang chạy 24/7. Mọi API trên Swagger (như `/api/auth/login`) khi Android gọi sẽ tự động được ghép thành: `http://tro24h.runasp.net/api/auth/login`.
2. `getUnsafeOkHttpClient()`: Khi kết nối mạng di động (4G/Wifi), nhiều mạng chặn hoặc cảnh báo chứng chỉ SSL tự ký của môi trường dev/staging. Đoạn code này tạo một `TrustManager` tin cậy giúp ứng dụng Android luôn gửi và nhận dữ liệu mượt mà, **không bao giờ bị lỗi `SSLHandshakeException` hay `CertPathValidatorException`**.
3. `GsonConverterFactory.create()`: Tự động phân giải dữ liệu JSON hiển thị trên Swagger thành các Model Class của Kotlin (`User`, `Room`, `Message`) một cách tự động và chuẩn xác.
4. `by lazy`: Cơ chế trì hoãn khởi tạo, chỉ tạo kết nối khi ứng dụng thực sự gọi API đầu tiên, giúp tiết kiệm bộ nhớ RAM cho điện thoại.

---

## 🎓 6. KỊCH BẢN TRẢ LỜI CÂU HỎI KHI BẢO VỆ ĐỒ ÁN / BÀI TẬP LỚN

> **Câu hỏi 1 của Thầy/Cô:** *"Em hãy trình bày cấu trúc Backend của ứng dụng và cho xem các API đã xây dựng?"*  
> **Trả lời:**  
> *"Dạ thưa Thầy/Cô, hệ thống Backend của nhóm em được xây dựng bằng ASP.NET Core 8.0 Web API và tích hợp sẵn công cụ Swagger UI chuẩn OpenAPI 3.0 tại địa chỉ `http://tro24h.runasp.net/swagger`. Tại đây, nhóm đã thiết kế và hiện thực đầy đủ 15 RESTful endpoints chia làm các phân hệ: Xác thực (Auth), Quản lý bài đăng phòng trọ (Rooms) và Nhắn tin 1-1 trực tiếp (Chat)."*

> **Câu hỏi 2 của Thầy/Cô:** *"Làm sao chứng minh Backend hoạt động độc lập và dữ liệu được lưu thật vào CSDL?"*  
> **Trả lời:**  
> *(Mở trình duyệt vào link Swagger)*:  
> *"Em xin phép thao tác trực tiếp trên Swagger UI: Em thực hiện gọi API `POST /api/auth/login` hoặc `GET /api/rooms` bằng tính năng 'Try it out' -> 'Execute'. Kết quả trả về mã phản hồi HTTP 200 OK cùng dữ liệu JSON thời gian thực được truy vấn trực tiếp từ máy chủ MS SQL Server Cloud `db69654.databaseasp.net` mà không cần thông qua ứng dụng di động."*
