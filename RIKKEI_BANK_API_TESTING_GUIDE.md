# Rikkei Bank Manager API - Báo cáo kiểm tra API và hướng dẫn test hoàn chỉnh

## 1. Kết luận sau khi kiểm tra project

Project hiện tại đã có phần lớn API cốt lõi của hệ thống ngân hàng Rikkei Bank:

- Auth: đăng nhập, refresh token, logout, quên mật khẩu, reset mật khẩu.
- User: Admin CRUD user, Staff xem user.
- KYC: Customer upload hồ sơ, Staff duyệt/từ chối.
- Account: Customer xem tài khoản/số dư/đổi PIN, Staff tạo tài khoản/khóa mở tài khoản.
- Transaction: Customer chuyển tiền và xem sao kê.
- Audit: AOP tự ghi log khi chuyển tiền thành công hoặc thất bại.
- Security: Stateless, JWT, blacklist token, phân quyền ADMIN / STAFF / CUSTOMER.

Tuy nhiên để test API hoàn chỉnh, cần bổ sung hoặc kiểm tra thêm các phần sau:

1. Cần có đầy đủ `build.gradle`, `settings.gradle`, `src/main/resources/application.properties`.
2. Cần có cấu hình JWT: `app.jwt.secret`, `app.jwt.access-token-expiration-ms`, `app.jwt.refresh-token-expiration-ms`.
3. Cần có cấu hình upload file local hoặc Cloudinary.
4. Nên thêm API xem `audit_logs`, vì hiện tại audit log chỉ kiểm tra được bằng database.
5. Nên thêm API đăng ký khách hàng public nếu muốn đúng hoàn toàn FR-04 “Đăng ký mở tài khoản”. Hiện tại luồng test phải tạo user bằng Admin hoặc dùng seed user.
6. Nên sửa logout để thiếu header trả đúng 401 thay vì lỗi thiếu request header.
7. Nên bổ sung kiểm tra pattern PIN trong `TransferRequest`.
8. Nên bổ sung endpoint xem chi tiết tài khoản nếu muốn test dễ hơn.

---

## 2. Cấu trúc API hiện tại trong project

### 2.1. Auth API

Base path:

```http
/api/auth
/api/v1/auth
```

| Method | Endpoint                    | Quyền           | Chức năng                                    |
| ------ | --------------------------- | --------------- | -------------------------------------------- |
| POST   | `/api/auth/login`           | Public          | Đăng nhập, cấp access token và refresh token |
| POST   | `/api/auth/refresh`         | Public          | Đổi refresh token lấy access token mới       |
| POST   | `/api/auth/logout`          | Có Bearer token | Đưa access token vào blacklist               |
| POST   | `/api/auth/forgot-password` | Public          | Tạo reset token demo                         |
| PATCH  | `/api/auth/reset-password`  | Public          | Đổi mật khẩu bằng reset token                |

### 2.2. Admin User API

Base path:

```http
/api/v1/admin/users
```

| Method | Endpoint                             | Quyền | Chức năng                                          |
| ------ | ------------------------------------ | ----- | -------------------------------------------------- |
| GET    | `/api/v1/admin/users?page=0&size=10` | ADMIN | Lấy danh sách user phân trang bằng JPQL Projection |
| GET    | `/api/v1/admin/users/{id}`           | ADMIN | Xem chi tiết user                                  |
| POST   | `/api/v1/admin/users`                | ADMIN | Tạo user mới                                       |
| PUT    | `/api/v1/admin/users/{id}`           | ADMIN | Cập nhật user                                      |
| PATCH  | `/api/v1/admin/users/{id}/status`    | ADMIN | Khóa/mở user                                       |
| DELETE | `/api/v1/admin/users/{id}`           | ADMIN | Soft delete bằng cách set active = false           |

### 2.3. Staff User API

Base path:

```http
/api/v1/staff/users
```

| Method | Endpoint                             | Quyền        | Chức năng          |
| ------ | ------------------------------------ | ------------ | ------------------ |
| GET    | `/api/v1/staff/users?page=0&size=10` | ADMIN, STAFF | Lấy danh sách user |
| GET    | `/api/v1/staff/users/{id}`           | ADMIN, STAFF | Xem chi tiết user  |

### 2.4. Customer KYC API

Base path:

```http
/api/v1/customer/kyc
```

| Method | Endpoint                      | Quyền    | Content-Type        | Chức năng        |
| ------ | ----------------------------- | -------- | ------------------- | ---------------- |
| POST   | `/api/v1/customer/kyc/upload` | CUSTOMER | multipart/form-data | Upload hồ sơ KYC |

### 2.5. Staff KYC API

Base path:

```http
/api/v1/staff/kyc
```

| Method | Endpoint                                   | Quyền        | Chức năng                        |
| ------ | ------------------------------------------ | ------------ | -------------------------------- |
| GET    | `/api/v1/staff/kyc/pending?page=0&size=10` | ADMIN, STAFF | Lấy danh sách KYC đang chờ duyệt |
| PATCH  | `/api/v1/staff/kyc/{kycId}/approve`        | ADMIN, STAFF | Duyệt KYC                        |
| PATCH  | `/api/v1/staff/kyc/{kycId}/reject`         | ADMIN, STAFF | Từ chối KYC                      |

### 2.6. Customer Account API

Base path:

```http
/api/v1/customer/accounts
```

| Method | Endpoint                                        | Quyền    | Chức năng                          |
| ------ | ----------------------------------------------- | -------- | ---------------------------------- |
| GET    | `/api/v1/customer/accounts`                     | CUSTOMER | Xem tài khoản của chính mình       |
| GET    | `/api/v1/customer/accounts/{accountId}/balance` | CUSTOMER | Xem số dư tài khoản của chính mình |
| PATCH  | `/api/v1/customer/accounts/{accountId}/pin`     | CUSTOMER | Đổi PIN giao dịch                  |

### 2.7. Staff Account API

Base path:

```http
/api/v1/staff/accounts
```

| Method | Endpoint                                | Quyền        | Chức năng                     |
| ------ | --------------------------------------- | ------------ | ----------------------------- |
| GET    | `/api/v1/staff/accounts?page=0&size=10` | ADMIN, STAFF | Xem danh sách tài khoản       |
| POST   | `/api/v1/staff/accounts`                | ADMIN, STAFF | Tạo tài khoản cho user đã KYC |
| PATCH  | `/api/v1/staff/accounts/{id}/status`    | ADMIN, STAFF | Khóa/mở tài khoản             |

### 2.8. Customer Transaction API

Base path:

```http
/api/v1/customer
```

| Method | Endpoint                                                            | Quyền    | Chức năng               |
| ------ | ------------------------------------------------------------------- | -------- | ----------------------- |
| POST   | `/api/v1/customer/transactions/transfer`                            | CUSTOMER | Chuyển tiền             |
| GET    | `/api/v1/customer/accounts/{accountId}/transactions?page=0&size=10` | CUSTOMER | Xem sao kê DEBIT/CREDIT |

---

## 3. Những file cấu hình bắt buộc phải có để chạy và test

### 3.1. `settings.gradle`

Nếu project chưa có file này thì tạo ở thư mục gốc:

```gradle
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = 'rikkei-bank-manager'
```

### 3.2. `build.gradle`

Nếu dùng Gradle, cần có đầy đủ dependency như sau:

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.0'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.re'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-aop'

    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

    implementation 'com.cloudinary:cloudinary-http44:1.38.0'

    runtimeOnly 'com.mysql:mysql-connector-j'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

Ghi chú:

- Nếu bạn dùng Spring Boot 4.x thì một số import Jackson có thể dùng `tools.jackson.databind.ObjectMapper`.
- Nếu dùng Spring Boot 3.x thì nên sửa import trong `UnauthorizedEntryPoint` và `ForbiddenAccessDeniedHandler` thành:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
```

### 3.3. `application.properties`

Tạo file:

```text
src/main/resources/application.properties
```

Nội dung đề xuất:

```properties
spring.application.name=rikkei-bank

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/rikkei_bank?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Server
server.port=8080

# JWT
app.jwt.secret=rikkei-bank-secret-key-must-be-at-least-32-characters-long
app.jwt.access-token-expiration-ms=300000
app.jwt.refresh-token-expiration-ms=86400000

# Upload file eKYC
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB

# Storage: local hoặc cloudinary
app.storage.type=local
app.storage.local-upload-dir=uploads/kyc

# Cloudinary chỉ dùng khi app.storage.type=cloudinary
app.storage.cloudinary.cloud-name=YOUR_CLOUD_NAME
app.storage.cloudinary.api-key=YOUR_API_KEY
app.storage.cloudinary.api-secret=YOUR_API_SECRET
```

Không nên commit mật khẩu MySQL thật lên GitHub.

---

## 4. Dữ liệu seed hiện tại

Project có `DataInitializer`, khi chạy app sẽ tự tạo:

### 4.1. Role

| Role     |
| -------- |
| ADMIN    |
| STAFF    |
| CUSTOMER |

### 4.2. User demo

| Username  | Password    | Role     | KYC  |
| --------- | ----------- | -------- | ---- |
| admin     | admin123    | ADMIN    | true |
| staff     | staff123    | STAFF    | true |
| customer1 | customer123 | CUSTOMER | true |
| customer2 | customer123 | CUSTOMER | true |

### 4.3. Account demo

| User      | Account Number | PIN    |    Balance |
| --------- | -------------- | ------ | ---------: |
| customer1 | 100000001      | 123456 | 10,000,000 |
| customer2 | 100000002      | 123456 |  1,000,000 |

---

## 5. Thứ tự test API hoàn chỉnh bằng Postman

Nên test đúng thứ tự này để không bị rối token và quyền.

```text
1. Login admin
2. Admin xem danh sách user
3. Admin tạo customer mới nếu muốn test user mới
4. Login customer1
5. Customer1 xem tài khoản
6. Customer1 xem số dư
7. Customer1 chuyển tiền cho customer2
8. Customer1 xem sao kê thấy DEBIT
9. Login customer2
10. Customer2 xem sao kê thấy CREDIT
11. Login staff
12. Staff xem KYC pending
13. Customer upload KYC nếu test user chưa KYC
14. Staff approve KYC
15. Staff tạo account cho user đã KYC
16. Customer đổi PIN
17. Customer chuyển tiền bằng PIN mới
18. Test lỗi thiếu tiền để nhận 409
19. Test sai quyền để nhận 403
20. Logout rồi gọi lại API cũ để nhận 401
```

---

## 6. Biến Postman nên tạo

Tạo Environment tên `Rikkei Bank Local`:

| Variable           | Initial value           | Current value           |
| ------------------ | ----------------------- | ----------------------- |
| baseUrl            | `http://localhost:8080` | `http://localhost:8080` |
| adminToken         | rỗng                    | rỗng                    |
| staffToken         | rỗng                    | rỗng                    |
| customer1Token     | rỗng                    | rỗng                    |
| customer2Token     | rỗng                    | rỗng                    |
| refreshToken       | rỗng                    | rỗng                    |
| customer1AccountId | rỗng                    | rỗng                    |
| customer2AccountId | rỗng                    | rỗng                    |
| kycId              | rỗng                    | rỗng                    |
| resetToken         | rỗng                    | rỗng                    |

---

## 7. Test chi tiết từng API

## 7.1. Login Admin

### Request

```http
POST {{baseUrl}}/api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

### Expected

Status:

```text
200 OK
```

Response:

```json
{
  "success": true,
  "message": "Login successfully",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "tokenType": "Bearer",
    "expiresInMs": 300000,
    "userId": 1,
    "username": "admin",
    "roleName": "ADMIN"
  }
}
```

### Postman Tests script

```javascript
const json = pm.response.json();
pm.test("Login admin OK", function () {
  pm.response.to.have.status(200);
  pm.expect(json.success).to.eql(true);
});
pm.environment.set("adminToken", json.data.accessToken);
pm.environment.set("refreshToken", json.data.refreshToken);
```

---

## 7.2. Login Staff

```http
POST {{baseUrl}}/api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "username": "staff",
  "password": "staff123"
}
```

Tests script:

```javascript
const json = pm.response.json();
pm.environment.set("staffToken", json.data.accessToken);
pm.environment.set("refreshToken", json.data.refreshToken);
```

---

## 7.3. Login Customer 1

```http
POST {{baseUrl}}/api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "username": "customer1",
  "password": "customer123"
}
```

Tests script:

```javascript
const json = pm.response.json();
pm.environment.set("customer1Token", json.data.accessToken);
pm.environment.set("refreshToken", json.data.refreshToken);
```

---

## 7.4. Login Customer 2

```http
POST {{baseUrl}}/api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "username": "customer2",
  "password": "customer123"
}
```

Tests script:

```javascript
const json = pm.response.json();
pm.environment.set("customer2Token", json.data.accessToken);
pm.environment.set("refreshToken", json.data.refreshToken);
```

---

## 7.5. Login sai mật khẩu

```http
POST {{baseUrl}}/api/auth/login
Content-Type: application/json
```

Body:

```json
{
  "username": "admin",
  "password": "sai_password"
}
```

Expected:

```text
401 Unauthorized
```

Response lỗi:

```json
{
  "timestamp": "...",
  "status": 401,
  "error": "Unauthorized",
  "message": "Username or password is incorrect",
  "path": "/api/auth/login"
}
```

---

## 7.6. Refresh token

```http
POST {{baseUrl}}/api/auth/refresh
Content-Type: application/json
```

Body:

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

Expected:

```text
200 OK
```

Tests script:

```javascript
const json = pm.response.json();
pm.environment.set("customer1Token", json.data.accessToken);
pm.environment.set("refreshToken", json.data.refreshToken);
```

Lưu ý: code hiện tại revoke refresh token cũ sau khi refresh, nên mỗi lần refresh phải lưu refresh token mới.

---

## 7.7. Logout

```http
POST {{baseUrl}}/api/auth/logout
Authorization: Bearer {{customer1Token}}
```

Expected:

```text
200 OK
```

Sau khi logout, dùng lại token cũ gọi API protected phải ra:

```text
401 Unauthorized
```

### Nên sửa nhỏ trong code

Hiện tại controller đang viết:

```java
public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorizationHeader);
```

Nếu không gửi header, Spring có thể trả lỗi thiếu header. Nên sửa thành:

```java
public ResponseEntity<ApiResponse<Void>> logout(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
    authService.logout(authorizationHeader);
    return ResponseEntity.ok(ApiResponse.success("Logout successfully", null));
}
```

---

## 7.8. Forgot password

```http
POST {{baseUrl}}/api/auth/forgot-password
Content-Type: application/json
```

Body:

```json
{
  "email": "customer1@rikkeibank.com"
}
```

Expected:

```text
200 OK
```

Response sẽ có `resetToken` demo.

Tests script:

```javascript
const json = pm.response.json();
pm.environment.set("resetToken", json.data.resetToken);
```

---

## 7.9. Reset password

```http
PATCH {{baseUrl}}/api/auth/reset-password
Content-Type: application/json
```

Body:

```json
{
  "resetToken": "{{resetToken}}",
  "newPassword": "customer456"
}
```

Expected:

```text
200 OK
```

Sau đó login lại bằng mật khẩu mới.

---

# 8. Admin User API

## 8.1. Admin lấy danh sách user

```http
GET {{baseUrl}}/api/v1/admin/users?page=0&size=10
Authorization: Bearer {{adminToken}}
```

Expected:

```text
200 OK
```

Kiểm tra response có dạng `Page`:

```json
{
  "success": true,
  "message": "Fetched users successfully",
  "data": {
    "content": [],
    "pageable": {},
    "totalElements": 4,
    "totalPages": 1
  }
}
```

### Test sai quyền

Dùng token customer gọi endpoint này:

```http
GET {{baseUrl}}/api/v1/admin/users?page=0&size=10
Authorization: Bearer {{customer1Token}}
```

Expected:

```text
403 Forbidden
```

---

## 8.2. Admin xem chi tiết user

```http
GET {{baseUrl}}/api/v1/admin/users/1
Authorization: Bearer {{adminToken}}
```

Expected:

```text
200 OK
```

Nếu id không tồn tại:

```http
GET {{baseUrl}}/api/v1/admin/users/99999
Authorization: Bearer {{adminToken}}
```

Expected:

```text
404 Not Found
```

---

## 8.3. Admin tạo user mới

```http
POST {{baseUrl}}/api/v1/admin/users
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```

Body tạo customer chưa KYC:

```json
{
  "username": "customer3",
  "password": "customer123",
  "phoneNumber": "0900000005",
  "email": "customer3@rikkeibank.com",
  "roleName": "CUSTOMER"
}
```

Expected:

```text
201 Created
```

Nếu tạo trùng username hoặc email:

```text
409 Conflict
```

---

## 8.4. Admin cập nhật user

```http
PUT {{baseUrl}}/api/v1/admin/users/3
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```

Body:

```json
{
  "phoneNumber": "0911111111",
  "email": "customer1_new@rikkeibank.com",
  "roleName": "CUSTOMER"
}
```

Expected:

```text
200 OK
```

### Test logic bảo vệ: Không được update Admin khác

Thử dùng `adminToken` sửa thông tin của một ADMIN khác (như username `staff` hoặc `admin` khác nếu có):
Expected: `403 Forbidden` với message: `Cannot modify another admin account`.

---

## 8.5. Admin khóa/mở user

```http
PATCH {{baseUrl}}/api/v1/admin/users/3/status
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```

Body khóa:

```json
{
  "active": false
}
```

Expected:

```text
200 OK
```

Sau khi khóa, user đó login phải lỗi 401 với message `User account is locked`. (Thay vì lỗi 500 như cũ).

### Test logic bảo vệ: Admin không tự khóa mình và không khóa Admin khác

- **Tự khóa chính mình**: Điền id của chính admin hiện tại đang gọi API. Expected: `400 Bad Request` với message `You cannot change your own account status`.
- **Khóa ADMIN khác**: Điền id của admin khác. Expected: `403 Forbidden` với message `Cannot modify another admin account status`.

---

## 8.6. Admin xóa user

```http
DELETE {{baseUrl}}/api/v1/admin/users/3
Authorization: Bearer {{adminToken}}
```

Expected:

```text
204 No Content
```

Lưu ý: code hiện tại không xóa vật lý mà set `active = false`.

### Test logic bảo vệ: Admin không tự xóa mình và không xóa Admin khác

- **Tự xóa chính mình**: Expected `400 Bad Request` với message `You cannot delete your own account`.
- **Xóa ADMIN khác**: Expected `403 Forbidden` với message `Cannot delete another admin account`.

---

# 9. Staff User API

## 9.1. Staff lấy danh sách user

```http
GET {{baseUrl}}/api/v1/staff/users?page=0&size=10
Authorization: Bearer {{staffToken}}
```

Expected:

```text
200 OK
```

**Chú ý cập nhật phân quyền**: Hiện nay, API này đã được chặn chỉ trả về người dùng thuộc nhóm `CUSTOMER`. Staff sẽ không thể nhìn thấy Admin hoặc Staff khác qua list này.

## 9.2. Staff xem chi tiết user

```http
GET {{baseUrl}}/api/v1/staff/users/3
Authorization: Bearer {{staffToken}}
```

Expected:

```text
200 OK
```

Customer gọi staff API phải ra:

```text
403 Forbidden
```

### Test logic bảo vệ: Tham chiếu chéo tới Admin

Nếu Staff dùng `staffToken` cố tình gọi lấy chi tiết User của một ID thuộc về nhóm `ADMIN`:
Expected: `403 Forbidden` với message `Staff can only view customer details`.

---

# 10. KYC API

## 10.1. Customer upload KYC

```http
POST {{baseUrl}}/api/v1/customer/kyc/upload
Authorization: Bearer {{customer1Token}}
Content-Type: multipart/form-data
```

Trong Postman chọn tab `Body` → `form-data`:

| Key      | Type | Value                     |
| -------- | ---- | ------------------------- |
| fullName | Text | Nguyen Van A              |
| idNumber | Text | 001201000003              |
| dob      | Text | 2001-01-01                |
| sex      | Text | MALE                      |
| address  | Text | Ha Noi                    |
| file     | File | chọn ảnh jpg/png hoặc pdf |

Expected:

```text
200 OK
```

Response có `status = PENDING`.

### Test file quá 5MB

Upload file lớn hơn 5MB. Expected:

```text
400 Bad Request
```

### Test sai định dạng

Upload file `.txt`. Expected:

```text
400 Bad Request
```

---

## 10.2. Staff xem KYC pending

```http
GET {{baseUrl}}/api/v1/staff/kyc/pending?page=0&size=10
Authorization: Bearer {{staffToken}}
```

Expected:

```text
200 OK
```

Lấy `kycId` từ response để approve/reject.

---

## 10.3. Staff approve KYC

```http
PATCH {{baseUrl}}/api/v1/staff/kyc/{{kycId}}/approve
Authorization: Bearer {{staffToken}}
```

Expected:

```text
200 OK
```

Sau approve:

- `kyc_profiles.status = CONFIRM`
- `users.is_kyc = true`

---

## 10.4. Staff reject KYC

```http
PATCH {{baseUrl}}/api/v1/staff/kyc/{{kycId}}/reject
Authorization: Bearer {{staffToken}}
```

Expected:

```text
200 OK
```

Sau reject:

- `kyc_profiles.status = REJECT`
- `users.is_kyc = false`

---

# 11. Account API

## 11.1. Customer xem tài khoản của mình

```http
GET {{baseUrl}}/api/v1/customer/accounts
Authorization: Bearer {{customer1Token}}
```

Expected:

```text
200 OK
```

Response demo:

```json
{
  "success": true,
  "message": "Fetched accounts successfully",
  "data": [
    {
      "id": 1,
      "accountNumber": "100000001",
      "balance": 10000000.0,
      "currency": "VND",
      "active": true,
      "userId": 3,
      "username": "customer1"
    }
  ]
}
```

Tests script để lưu account id:

```javascript
const json = pm.response.json();
if (json.data && json.data.length > 0) {
  pm.environment.set("customer1AccountId", json.data[0].id);
}
```

---

## 11.2. Customer xem số dư

```http
GET {{baseUrl}}/api/v1/customer/accounts/{{customer1AccountId}}/balance
Authorization: Bearer {{customer1Token}}
```

Expected:

```text
200 OK
```

Nếu customer1 xem tài khoản của customer2:

```text
403 Forbidden
```

---

## 11.3. Staff xem danh sách account

```http
GET {{baseUrl}}/api/v1/staff/accounts?page=0&size=10
Authorization: Bearer {{staffToken}}
```

Expected:

```text
200 OK
```

ADMIN cũng gọi được endpoint này vì rule security là `hasAnyRole("ADMIN", "STAFF")`.

---

## 11.4. Staff tạo account cho user đã KYC

```http
POST {{baseUrl}}/api/v1/staff/accounts
Authorization: Bearer {{staffToken}}
Content-Type: application/json
```

Body:

```json
{
  "userId": 3,
  "currency": "VND",
  "transactionPin": "123456",
  "initialBalance": 500000
}
```

Expected:

```text
201 Created
```

Nếu user chưa KYC:

```text
400 Bad Request
```

---

## 11.5. Staff khóa/mở account

```http
PATCH {{baseUrl}}/api/v1/staff/accounts/1/status
Authorization: Bearer {{staffToken}}
Content-Type: application/json
```

Body khóa:

```json
{
  "active": false
}
```

Expected:

```text
200 OK
```

Sau khi khóa, chuyển tiền từ account đó phải lỗi.

---

## 11.6. Customer đổi PIN

```http
PATCH {{baseUrl}}/api/v1/customer/accounts/{{customer1AccountId}}/pin
Authorization: Bearer {{customer1Token}}
Content-Type: application/json
```

Body:

```json
{
  "oldPin": "123456",
  "newPin": "654321"
}
```

Expected:

```text
200 OK
```

Sau đó chuyển tiền phải dùng PIN mới `654321`.

Nếu oldPin sai:

```text
403 Forbidden
```

---

# 12. Transaction API

## 12.1. Customer chuyển tiền thành công

```http
POST {{baseUrl}}/api/v1/customer/transactions/transfer
Authorization: Bearer {{customer1Token}}
Content-Type: application/json
```

Body nếu PIN vẫn là `123456`:

```json
{
  "fromAccountNumber": "100000001",
  "toAccountNumber": "100000002",
  "amount": 500000,
  "description": "Chuyen tien test",
  "transactionPin": "123456"
}
```

Nếu bạn đã đổi PIN thành `654321`, body phải là:

```json
{
  "fromAccountNumber": "100000001",
  "toAccountNumber": "100000002",
  "amount": 500000,
  "description": "Chuyen tien test",
  "transactionPin": "654321"
}
```

Expected:

```text
200 OK
```

Response:

```json
{
  "success": true,
  "message": "Transferred money successfully",
  "data": {
    "transactionCode": "TXN-...",
    "fromAccountNumber": "100000001",
    "toAccountNumber": "100000002",
    "amount": 500000,
    "description": "Chuyen tien test",
    "status": "SUCCESS",
    "createdAt": "..."
  }
}
```

Sau API này cần kiểm tra:

- Account nguồn bị trừ tiền.
- Account đích được cộng tiền.
- Bảng `transactions` có record `SUCCESS`.
- Bảng `audit_logs` có record `SUCCESS`.

---

## 12.2. Chuyển tiền thiếu số dư

```http
POST {{baseUrl}}/api/v1/customer/transactions/transfer
Authorization: Bearer {{customer1Token}}
Content-Type: application/json
```

Body:

```json
{
  "fromAccountNumber": "100000001",
  "toAccountNumber": "100000002",
  "amount": 999999999999,
  "description": "Test thieu so du",
  "transactionPin": "123456"
}
```

Expected:

```text
409 Conflict
```

Sau API này cần kiểm tra:

- Số dư không đổi.
- Bảng `transactions` không có giao dịch SUCCESS mới.
- Bảng `audit_logs` có record `FAILED`.

---

## 12.3. Chuyển tiền sai PIN

Body:

```json
{
  "fromAccountNumber": "100000001",
  "toAccountNumber": "100000002",
  "amount": 100000,
  "description": "Test sai PIN",
  "transactionPin": "000000"
}
```

Expected:

```text
403 Forbidden
```

---

## 12.4. Chuyển tiền từ account không thuộc user

Login customer2 nhưng dùng source account của customer1:

```http
POST {{baseUrl}}/api/v1/customer/transactions/transfer
Authorization: Bearer {{customer2Token}}
Content-Type: application/json
```

Body:

```json
{
  "fromAccountNumber": "100000001",
  "toAccountNumber": "100000002",
  "amount": 100000,
  "description": "Hack test",
  "transactionPin": "123456"
}
```

Expected:

```text
403 Forbidden
```

---

## 12.5. Tự chuyển cho chính mình

```json
{
  "fromAccountNumber": "100000001",
  "toAccountNumber": "100000001",
  "amount": 100000,
  "description": "Tu chuyen",
  "transactionPin": "123456"
}
```

Expected:

```text
400 Bad Request
```

---

# 13. Statement API

## 13.1. Customer xem sao kê

```http
GET {{baseUrl}}/api/v1/customer/accounts/{{customer1AccountId}}/transactions?page=0&size=10
Authorization: Bearer {{customer1Token}}
```

Expected:

```text
200 OK
```

Nếu account là nguồn tiền, response phải có:

```json
{
  "type": "DEBIT"
}
```

Nếu account là người nhận, response phải có:

```json
{
  "type": "CREDIT"
}
```

## 13.2. Customer2 xem sao kê CREDIT

Đầu tiên lấy account của customer2:

```http
GET {{baseUrl}}/api/v1/customer/accounts
Authorization: Bearer {{customer2Token}}
```

Lưu `id` vào `customer2AccountId`.

Sau đó gọi:

```http
GET {{baseUrl}}/api/v1/customer/accounts/{{customer2AccountId}}/transactions?page=0&size=10
Authorization: Bearer {{customer2Token}}
```

Expected: giao dịch nhận tiền có `type = CREDIT`.

## 13.3. Xem sao kê account người khác

Customer1 gọi sao kê account của customer2:

```http
GET {{baseUrl}}/api/v1/customer/accounts/{{customer2AccountId}}/transactions?page=0&size=10
Authorization: Bearer {{customer1Token}}
```

Expected:

```text
403 Forbidden
```

---

# 14. SQL kiểm tra sau khi test

Mở MySQL và chạy:

```sql
USE rikkei_bank;

SELECT id, username, email, is_active, is_kyc, role_id
FROM users;

SELECT id, account_number, balance, active, user_id
FROM accounts;

SELECT id, transaction_code, from_account_id, to_account_id, amount, status, created_at
FROM transactions
ORDER BY created_at DESC;

SELECT id, username, action, from_account_number, to_account_number, amount, status, message, execution_time_ms, created_at
FROM audit_logs
ORDER BY created_at DESC;

SELECT id, token, revoked, expiry_date, user_id
FROM refresh_tokens
ORDER BY id DESC;

SELECT id, expiry_date, blacklisted_at, user_id
FROM token_blacklist
ORDER BY id DESC;
```

---

# 15. API nên thêm để test hoàn chỉnh hơn

## 15.1. Nên thêm API xem Audit Log

Hiện tại project có AOP ghi `audit_logs`, nhưng chưa có controller để xem audit log bằng API. Nếu chấm demo trên Postman thì nên thêm.

### Tạo file

```text
src/main/java/com/re/rikkei_bank_manager/audit/controller/AdminAuditController.java
```

### Code đề xuất

```java
package com.re.rikkei_bank_manager.audit.controller;

import com.re.rikkei_bank_manager.audit.entity.AuditLog;
import com.re.rikkei_bank_manager.audit.repository.AuditLogRepository;
import com.re.rikkei_bank_manager.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditController {
    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.success("Fetched audit logs successfully", auditLogRepository.findAll(pageable))
        );
    }
}
```

Test:

```http
GET {{baseUrl}}/api/v1/admin/audit-logs?page=0&size=10
Authorization: Bearer {{adminToken}}
```

Expected:

```text
200 OK
```

Lưu ý: Nếu muốn chuẩn hơn nữa thì tạo DTO riêng, không trả entity trực tiếp.

---

## 15.2. Nên thêm API đăng ký Customer public

Yêu cầu FR-04 là đăng ký mở tài khoản/upload eKYC. Project hiện tại chưa có public register, nên khi test phải dùng user seed hoặc Admin tạo user.

Nếu muốn đầy đủ hơn, thêm:

```http
POST /api/v1/auth/register
```

Body:

```json
{
  "username": "customer4",
  "password": "customer123",
  "phoneNumber": "0900000006",
  "email": "customer4@rikkeibank.com"
}
```

Luồng:

```text
Register customer
→ Login customer
→ Upload KYC
→ Staff approve
→ Staff create account
→ Customer transfer
```

---

## 15.3. Nên thêm API xem account detail cho Staff/Admin

Hiện tại có list account, nhưng nếu muốn test nhanh từng account thì thêm:

```http
GET /api/v1/staff/accounts/{id}
```

Dùng để xem trạng thái `active`, balance và user sở hữu.

---

## 15.4. Nên thêm validation PIN trong TransferRequest

Hiện tại `TransferRequest` có:

```java
@NotBlank private String transactionPin;
```

Nên sửa thành:

```java
@NotBlank
@Pattern(regexp = "^[0-9]{6}$", message = "must contain exactly 6 digits")
private String transactionPin;
```

Như vậy gửi PIN sai định dạng sẽ trả 400 trước khi vào service.

---

# 16. Checklist test bắt buộc theo yêu cầu dự án

| STT | Test case                           | API                                               | Expected |
| --: | ----------------------------------- | ------------------------------------------------- | -------- |
|   1 | Login đúng                          | POST `/api/auth/login`                            | 200      |
|   2 | Login sai mật khẩu                  | POST `/api/auth/login`                            | 401      |
|   3 | Không gửi token                     | GET `/api/v1/customer/accounts`                   | 401      |
|   4 | Customer gọi admin API              | GET `/api/v1/admin/users`                         | 403      |
|   5 | Admin lấy user phân trang           | GET `/api/v1/admin/users?page=0&size=10`          | 200      |
|   6 | Tạo user trùng email                | POST `/api/v1/admin/users`                        | 409      |
|   7 | Customer upload KYC                 | POST `/api/v1/customer/kyc/upload`                | 200      |
|   8 | Upload file quá 5MB                 | POST `/api/v1/customer/kyc/upload`                | 400      |
|   9 | Staff approve KYC                   | PATCH `/api/v1/staff/kyc/{id}/approve`            | 200      |
|  10 | Staff tạo account cho user chưa KYC | POST `/api/v1/staff/accounts`                     | 400      |
|  11 | Customer xem account của mình       | GET `/api/v1/customer/accounts`                   | 200      |
|  12 | Customer xem account người khác     | GET `/api/v1/customer/accounts/{id}/balance`      | 403      |
|  13 | Chuyển tiền thành công              | POST `/api/v1/customer/transactions/transfer`     | 200      |
|  14 | Chuyển tiền thiếu số dư             | POST `/api/v1/customer/transactions/transfer`     | 409      |
|  15 | Chuyển tiền sai PIN                 | POST `/api/v1/customer/transactions/transfer`     | 403      |
|  16 | Tự chuyển cùng account              | POST `/api/v1/customer/transactions/transfer`     | 400      |
|  17 | Sao kê account gửi                  | GET `/api/v1/customer/accounts/{id}/transactions` | DEBIT    |
|  18 | Sao kê account nhận                 | GET `/api/v1/customer/accounts/{id}/transactions` | CREDIT   |
|  19 | Logout                              | POST `/api/auth/logout`                           | 200      |
|  20 | Dùng token cũ sau logout            | GET protected API                                 | 401      |
|  21 | Audit log success                   | DB hoặc API audit                                 | SUCCESS  |
|  22 | Audit log failed                    | DB hoặc API audit                                 | FAILED   |

---

# 17. Lỗi thường gặp và cách fix

## 17.1. Lỗi `Could not resolve placeholder 'app.jwt.secret'`

Nguyên nhân: thiếu cấu hình JWT trong `application.properties`.

Fix:

```properties
app.jwt.secret=rikkei-bank-secret-key-must-be-at-least-32-characters-long
app.jwt.access-token-expiration-ms=300000
app.jwt.refresh-token-expiration-ms=86400000
```

---

## 17.2. Lỗi `The import com.cloudinary cannot be resolved`

Nguyên nhân: Gradle chưa có dependency Cloudinary.

Fix trong `build.gradle`:

```gradle
implementation 'com.cloudinary:cloudinary-http44:1.38.0'
```

Sau đó chạy:

```powershell
.\gradlew.bat clean build --refresh-dependencies -x test
```

---

## 17.3. Lỗi `Cannot resolve method setUserDetailsService in DaoAuthenticationProvider`

Nguyên nhân: Spring Security bản mới không dùng kiểu cũ.

Code đúng:

```java
@Bean
public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
}
```

---

## 17.4. Gọi API bị 401

Kiểm tra:

- Đã login chưa?
- Header có đúng chưa?

```http
Authorization: Bearer <accessToken>
```

- Token có hết hạn chưa?
- Token có bị logout blacklist chưa?

---

## 17.5. Gọi API bị 403

Nguyên nhân thường gặp:

- Đúng token nhưng sai role.
- CUSTOMER gọi `/api/v1/admin/**`.
- STAFF gọi API customer.
- Customer xem account không thuộc mình.

---

## 17.6. Upload KYC bị 400

Kiểm tra:

- Body phải là `form-data`, không phải JSON.
- Key file phải đúng là `file`.
- File nhỏ hơn 5MB.
- File là jpg, jpeg, png hoặc pdf.

---

## 17.7. Chuyển tiền bị 403

Kiểm tra:

- User đã KYC chưa?
- Source account có thuộc user đang login không?
- PIN có đúng không?
- Account có bị khóa không?

---

## 17.8. Chuyển tiền bị 409

Nguyên nhân đúng theo nghiệp vụ: số dư không đủ.

---

# 18. Kịch bản demo chấm điểm hoàn chỉnh

## Kịch bản 1: Auth và phân quyền

1. Login admin thành công.
2. Login customer1 thành công.
3. Không gửi token gọi `/api/v1/customer/accounts` → 401.
4. Customer gọi `/api/v1/admin/users` → 403.
5. Admin gọi `/api/v1/admin/users` → 200.

## Kịch bản 2: KYC

1. Login customer1.
2. Upload KYC multipart.
3. Login staff.
4. Staff xem pending KYC.
5. Staff approve KYC.
6. Kiểm tra DB `users.is_kyc = true`.

## Kịch bản 3: Account

1. Staff tạo account cho user đã KYC.
2. Customer xem list account.
3. Customer xem balance.
4. Customer đổi PIN.
5. Staff khóa account.
6. Customer chuyển tiền từ account bị khóa → lỗi.

## Kịch bản 4: Transfer và Audit

1. Customer1 xem số dư ban đầu.
2. Customer1 chuyển 500000 cho customer2.
3. Customer1 xem số dư giảm.
4. Customer2 xem số dư tăng.
5. Customer1 xem sao kê thấy DEBIT.
6. Customer2 xem sao kê thấy CREDIT.
7. Kiểm tra `transactions.status = SUCCESS`.
8. Kiểm tra `audit_logs.status = SUCCESS`.
9. Test chuyển tiền thiếu số dư → 409.
10. Kiểm tra `audit_logs.status = FAILED`.

## Kịch bản 5: Logout và blacklist

1. Login customer1.
2. Gọi API protected thành công.
3. Logout.
4. Dùng lại access token cũ gọi API protected.
5. Expected: 401.
6. Kiểm tra DB bảng `token_blacklist` có token vừa logout.

---

# 19. Đánh giá mức độ hoàn chỉnh so với SRS

| Yêu cầu                     | Trạng thái hiện tại      | Ghi chú                               |
| --------------------------- | ------------------------ | ------------------------------------- |
| RESTful API                 | Đạt                      | Endpoint có `/api/v1`                 |
| Stateless Backend           | Đạt                      | SecurityConfig dùng STATELESS         |
| JWT Access Token            | Đạt                      | `JwtService`                          |
| Refresh Token               | Đạt                      | `RefreshToken` + repository           |
| Token Blacklist             | Đạt                      | `TokenBlacklist`                      |
| Role ADMIN/STAFF/CUSTOMER   | Đạt                      | RoleName đúng ngân hàng               |
| BCrypt password             | Đạt                      | User password encode                  |
| BCrypt transaction PIN      | Đạt                      | Account PIN encode                    |
| BigDecimal money            | Đạt                      | Account.balance và transaction.amount |
| Transfer @Transactional     | Đạt                      | `TransactionService.transfer`         |
| Chống double-spending       | Đạt                      | PESSIMISTIC_WRITE + lock 2 account    |
| AOP Audit Log               | Đạt                      | `AuditAspect`                         |
| eKYC upload 5MB             | Đạt                      | Validate size + multipart             |
| Staff approve KYC           | Đạt                      | approve/reject                        |
| Statement DEBIT/CREDIT      | Đạt                      | map theo from/to account              |
| JPQL Constructor Projection | Đạt                      | `UserRepository.findAllProjected`     |
| GlobalExceptionHandler      | Đạt                      | Có response lỗi chuẩn                 |
| API xem Audit Log           | Thiếu                    | Nên thêm để test trên Postman         |
| Public register customer    | Thiếu                    | Nên thêm nếu muốn đúng FR-04 đầy đủ   |
| Postman collection          | Chưa thấy trong file zip | Nên tạo collection theo guide này     |

---

# 20. Việc nên làm tiếp theo

Thứ tự xử lý nên làm:

1. Bổ sung `build.gradle`, `settings.gradle`, `application.properties` nếu project chưa có.
2. Chạy app cho hết lỗi startup.
3. Test login trước.
4. Test phân quyền 401/403.
5. Test account và transfer bằng seed data.
6. Test statement DEBIT/CREDIT.
7. Kiểm tra audit log bằng SQL.
8. Thêm API `/api/v1/admin/audit-logs` để test audit dễ hơn.
9. Thêm public register nếu muốn demo luồng khách hàng mới từ đầu.
10. Tạo Postman collection theo các endpoint trong tài liệu này.
