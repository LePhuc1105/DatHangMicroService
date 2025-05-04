USE product_db;

CREATE TABLE IF NOT EXISTS product (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255),
  description TEXT,
  price DOUBLE,
  quantity INT
);

INSERT INTO product (name, description, price, quantity) VALUES 
('iPhone 13', 'Apple iPhone 13 128GB, Blue', 799.99, 50),
('Samsung Galaxy S21', 'Samsung Galaxy S21 5G 128GB, Phantom Black', 699.99, 40),
('MacBook Pro', 'Apple MacBook Pro 13-inch, M1 chip, 8GB RAM, 256GB SSD', 1299.99, 25),
('Sony PlayStation 5', 'Sony PlayStation 5 Console', 499.99, 10),
('Nintendo Switch', 'Nintendo Switch Console with Neon Red/Blue Joy-Con', 299.99, 30); 