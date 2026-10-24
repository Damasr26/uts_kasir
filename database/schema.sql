-- JOSJISMART | MySQL/XAMPP | opsi instalasi manual.
-- Impor ini OPSIONAL: saat aplikasi pertama kali dijalankan, tabel dan database dibuat otomatis.
-- Aplikasi langsung masuk dashboard; tidak menggunakan akun/login.
CREATE DATABASE IF NOT EXISTS josjismart_db
 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE josjismart_db;

CREATE TABLE IF NOT EXISTS products (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 sku VARCHAR(70) NOT NULL UNIQUE,
 name VARCHAR(180) NOT NULL,
 category VARCHAR(100) NOT NULL,
 unit VARCHAR(32) NOT NULL DEFAULT 'pcs',
 price DECIMAL(15,2) NOT NULL,
 stock INT NOT NULL DEFAULT 0,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 INDEX idx_products_name(name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sales (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 invoice_no VARCHAR(65) NOT NULL UNIQUE,
 customer_name VARCHAR(160) NOT NULL,
 cashier_name_snapshot VARCHAR(120) NOT NULL,
 total DECIMAL(16,2) NOT NULL,
 paid DECIMAL(16,2) NOT NULL,
 change_amount DECIMAL(16,2) NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_sales_time(created_at),
 INDEX idx_sales_customer(customer_name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sale_items (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 sale_id BIGINT NOT NULL,
 sku_snapshot VARCHAR(70) NOT NULL,
 name_snapshot VARCHAR(180) NOT NULL,
 category_snapshot VARCHAR(100) NOT NULL,
 unit_snapshot VARCHAR(32) NOT NULL,
 qty INT NOT NULL,
 price_snapshot DECIMAL(15,2) NOT NULL,
 line_total DECIMAL(16,2) NOT NULL,
 CONSTRAINT fk_saleitems_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE RESTRICT,
 INDEX idx_saleitems_sale(sale_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS app_meta (
 meta_key VARCHAR(70) PRIMARY KEY,
 meta_value VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

-- PENTING:
-- Tidak ada `product_id`/foreign key products pada sale_items.
-- Nama, SKU, kategori, satuan, harga, dan subtotal disimpan sebagai snapshot.
-- DELETE / UPDATE products tidak akan merusak riwayat transaksi.
