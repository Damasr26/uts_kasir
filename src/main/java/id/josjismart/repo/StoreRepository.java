package id.josjismart.repo;

import id.josjismart.db.Database;
import id.josjismart.model.Product;
import java.sql.*;
import java.util.*;

public final class StoreRepository {
    private Product map(ResultSet rs) throws SQLException {
        return new Product(rs.getLong("id"), rs.getString("sku"), rs.getString("name"), rs.getString("category"),
                rs.getString("unit"), rs.getBigDecimal("price"), rs.getInt("stock"));
    }
    public List<Product> findProducts(String query) throws SQLException {
        String sql = "SELECT id,sku,name,category,unit,price,stock FROM products " +
                "WHERE sku LIKE ? OR name LIKE ? OR category LIKE ? ORDER BY name ASC LIMIT 500";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            String q = "%" + query.trim() + "%";
            p.setString(1,q); p.setString(2,q); p.setString(3,q);
            try (ResultSet r = p.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (r.next()) products.add(map(r));
                return products;
            }
        }
    }
    public void saveProduct(Product product) throws SQLException {
        if (product.sku().isBlank() || product.name().isBlank() || product.category().isBlank() || product.unit().isBlank())
            throw new IllegalArgumentException("SKU, nama, kategori, dan satuan wajib diisi.");
        if (product.price().signum() < 0 || product.stock() < 0)
            throw new IllegalArgumentException("Harga dan stok tidak boleh negatif.");
        String sql = product.id() == 0
            ? "INSERT INTO products(sku,name,category,unit,price,stock) VALUES(?,?,?,?,?,?)"
            : "UPDATE products SET sku=?,name=?,category=?,unit=?,price=?,stock=? WHERE id=?";
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1,product.sku().trim()); p.setString(2,product.name().trim());
            p.setString(3,product.category().trim()); p.setString(4,product.unit().trim());
            p.setBigDecimal(5,product.price()); p.setInt(6,product.stock());
            if (product.id()!=0) p.setLong(7,product.id());
            if (p.executeUpdate()!=1) throw new SQLException("Data produk tidak ditemukan. Muat ulang tabel.");
        }
    }
    public void deleteProduct(long id) throws SQLException {
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement("DELETE FROM products WHERE id=?")) {
            p.setLong(1,id);
            if (p.executeUpdate()!=1) throw new SQLException("Produk tidak ditemukan / telah dihapus.");
        }
    }
    /** Pengaturan tanpa login, disimpan di database lokal. */
    public String getSetting(String key, String fallback) throws SQLException {
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(
                "SELECT meta_value FROM app_meta WHERE meta_key=?")) {
            p.setString(1,key);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getString(1) : fallback;
            }
        }
    }
    public void saveSetting(String key, String value) throws SQLException {
        if (key == null || key.isBlank() || key.length()>70 || value == null || value.isBlank() || value.length()>120)
            throw new IllegalArgumentException("Nama kasir wajib diisi, maksimal 120 karakter.");
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO app_meta(meta_key,meta_value) VALUES(?,?) ON DUPLICATE KEY UPDATE meta_value=VALUES(meta_value)")) {
            p.setString(1,key);p.setString(2,value.trim());p.executeUpdate();
        }
    }
}
