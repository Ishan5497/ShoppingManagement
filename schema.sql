CREATE DATABASE IF NOT EXISTS shopping;
USE shopping;

CREATE TABLE IF NOT EXISTS products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    stock INT NOT NULL CHECK (stock >= 0)
);

CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_products
        FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- Optional sample products; run only if you want sample data.
INSERT INTO products (product_name, price, stock) VALUES
('Notebook', 55.00, 100),
('Pen', 10.00, 250),
('Backpack', 799.00, 25),
('Water Bottle', 199.00, 40);
