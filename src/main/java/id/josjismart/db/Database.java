package id.josjismart.db;

import id.josjismart.config.AppConfig;
import java.math.BigDecimal;
import java.sql.*;

public final class Database {
    private Database() {}
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(AppConfig.baseUrl() + AppConfig.databaseName() + AppConfig.options(),
                AppConfig.get("db.user"), AppConfig.get("db.password"));
    }
    public static void initialize() throws SQLException {
        String dbName = AppConfig.databaseName();
        try (Connection server = DriverManager.getConnection(AppConfig.baseUrl() + AppConfig.options(),
                AppConfig.get("db.user"), AppConfig.get("db.password"));
             Statement s = server.createStatement()) {
            s.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.executeUpdate("""
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
                  INDEX idx_products_name (name)
                ) ENGINE=InnoDB
                """);
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS sales (
                  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                  invoice_no VARCHAR(65) NOT NULL UNIQUE,
                  customer_name VARCHAR(160) NOT NULL,
                  cashier_name_snapshot VARCHAR(120) NOT NULL,
                  total DECIMAL(16,2) NOT NULL,
                  paid DECIMAL(16,2) NOT NULL,
                  change_amount DECIMAL(16,2) NOT NULL,
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  INDEX idx_sales_time (created_at),
                  INDEX idx_sales_customer (customer_name)
                ) ENGINE=InnoDB
                """);
            s.executeUpdate("""
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
                  INDEX idx_saleitems_sale (sale_id)
                ) ENGINE=InnoDB
                """);
            // Data demo disisipkan sekali saja; tidak akan muncul kembali setelah produk dihapus.
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS app_meta (
                  meta_key VARCHAR(70) PRIMARY KEY,
                  meta_value VARCHAR(255) NOT NULL
                ) ENGINE=InnoDB
                """);
            // Snapshot item transaksi TIDAK mempunyai FK/join ke products.
            try (ResultSet r = s.executeQuery("SELECT COUNT(*) FROM app_meta WHERE meta_key='demo_seeded'")) {
                r.next();
                if (r.getInt(1) == 0) {
                    boolean empty;
                    try (Statement check = c.createStatement();
                         ResultSet productCount = check.executeQuery("SELECT COUNT(*) FROM products")) {
                        productCount.next();
                        empty = productCount.getInt(1) == 0;
                    }
                    if (empty) {
                    String[][] demo = {
                        {"JS-001","Air Mineral 600ml","Minuman","botol","4000","65"},
                        {"JS-002","Roti Cokelat","Makanan","pcs","7500","40"},
                        {"JS-003","Susu UHT 250ml","Minuman","kotak","8500","28"},
                        {"JS-004","Mie Instan Goreng","Sembako","bungkus","3500","80"},
                        {"JS-005","Sabun Mandi","Perawatan","pcs","6500","34"},
                        {"JS-006","Beras Premium 1kg","Sembako","pak","17000","22"},
                        {"JS-007","Kopi Sachet","Minuman","sachet","2500","96"},
                        {"JS-008","Biskuit Butter","Makanan","pak","12000","19"}
                    };
                    try (PreparedStatement p = c.prepareStatement("INSERT INTO products(sku,name,category,unit,price,stock) VALUES(?,?,?,?,?,?)")) {
                        for (String[] a : demo) {
                            p.setString(1,a[0]); p.setString(2,a[1]); p.setString(3,a[2]); p.setString(4,a[3]);
                            p.setBigDecimal(5,new BigDecimal(a[4])); p.setInt(6,Integer.parseInt(a[5])); p.addBatch();
                        }
                        p.executeBatch();
                    }
                    }
                    s.executeUpdate("INSERT INTO app_meta(meta_key,meta_value) VALUES('demo_seeded','1')");
                }
            }
        }
    }
}
