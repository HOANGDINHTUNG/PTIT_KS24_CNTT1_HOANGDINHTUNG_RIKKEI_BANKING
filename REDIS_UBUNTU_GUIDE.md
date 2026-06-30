# Hướng dẫn Cài đặt và Chạy Redis trên hệ điều hành Ubuntu (hoặc WSL)

Để chức năng Token Blacklist mới của Backend hoạt động, hệ thống yêu cầu một phiên bản Redis Database chạy ở máy tại port `6379`. Dưới đây là các bước chi tiết để bạn cài đặt:

## 1. Cài đặt Redis bằng `apt`

Mở ứng dụng Terminal của Ubuntu, sau đó chạy lần lượt các lệnh sau:

Cập nhật danh sách gói phần mềm:

```bash
sudo apt update
```

Cài đặt package redis-server:

```bash
sudo apt install redis-server -y
```

## 2. Kiểm tra trạng thái và Khởi chạy

Kiểm tra xem file cấu hình đã được tạo chưa và Redis đã chạy chưa:

```bash
sudo systemctl status redis-server
```

_(Nếu bạn thấy chữ `active (running)` màu xanh thì nghĩa là nó đang chạy)_

Trường hợp nó chưa chạy, bạn hãy bắt đầu nó bằng lệnh sau:

```bash
sudo systemctl start redis-server
```

_(Tùy chọn) Để Redis tự khởi động lại mỗi khi bạn mở máy:_

```bash
sudo systemctl enable redis-server
```

## 3. Kiểm tra tính năng hoạt động

Gõ lệnh sau để mở công cụ giao tiếp (CLI) của Redis:

```bash
redis-cli
```

Nếu màn hình hiện `127.0.0.1:6379>`, bạn hãy gõ:

```bash
ping
```

Nếu Redis trả về `PONG`, nghĩa là bạn đã cài đặt thành công 100%! Bấm `Ctrl+C` hoặc gõ `exit` để thoát.

## 4. Troubleshooting (Xử lý lỗi nếu có)

- Trạng thái báo Failed do kẹt Port: Đảm bảo không có dịch vụ nào đang chiếm dụng cổng `6379`.
- Máy báo không tìm thấy systemctl do đang dùng WSL cũ: Sử dụng lệnh `sudo service redis-server start` thay cho lệnh systemctl ở bên trên.

Chúc bạn thành công! Nếu Database Redis hoạt động tốt, API `/api/auth/logout` sẽ tự động hết gây lỗi 500 kể từ luồng nâng cấp này.
