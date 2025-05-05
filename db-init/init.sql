-- Tạo database
CREATE DATABASE IF NOT EXISTS order_db;

-- Sử dụng order_db
USE order_db;

-- Tạo bảng users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL,
    email VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tạo bảng Products
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    quantity_in_stock INT NOT NULL,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tạo bảng Orders
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_username VARCHAR(255) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    delivery_date DATETIME NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_username) REFERENCES users(username) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_customer_username (customer_username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tạo bảng OrderItems
CREATE TABLE orderitems (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tạo bảng CartItems (dành cho Cart Service, nếu lưu trữ giỏ hàng trong database)
CREATE TABLE cartitems (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_username (username),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Thêm dữ liệu mẫu
INSERT INTO users (username, password, role, is_active, email)
VALUES 
    ('testuser', 'password', 'CUSTOMER', TRUE, 'test@example.com'),
    ('admin', 'adminpass', 'ADMIN', TRUE, 'admin@example.com');

INSERT INTO products (name, description, price, quantity_in_stock)
VALUES 
    ('Product A', 'Description for Product A', 10.00, 100),
    ('Product B', 'Description for Product B', 20.00, 50),
    ('Product C', 'Description for Product C', 15.00, 75);

-- Dữ liệu mẫu cho CartItems (giỏ hàng của testuser)
INSERT INTO cartitems (username, product_id, quantity)
VALUES 
    ('testuser', 1, 2),
    ('testuser', 2, 1); 