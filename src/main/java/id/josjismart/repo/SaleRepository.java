package id.josjismart.repo;

import id.josjismart.db.Database;
import id.josjismart.model.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class SaleRepository {
    public record Summary(BigDecimal revenue, long transactions, long units, long lowStock) {
        public BigDecimal average() { return transactions == 0 ? BigDecimal.ZERO : revenue.divide(BigDecimal.valueOf(transactions), 2, java.math.RoundingMode.HALF_UP); }
    }
    public record SeriesPoint(String label, BigDecimal amount) {}
    public record BestSeller(String name, String sku, long sold, BigDecimal revenue) {}

    public Sale checkout(List<CartLine> chosenLines, String customer, String cashier, BigDecimal tendered) throws SQLException {
        if (chosenLines == null || chosenLines.isEmpty()) throw new IllegalArgumentException("Keranjang masih kosong.");
        String customerName = customer == null || customer.isBlank() ? "Pelanggan Umum" : customer.trim();
        if (customerName.length()>160) throw new IllegalArgumentException("Nama pembeli maksimal 160 karakter.");
        if (tendered == null || tendered.signum() < 0) throw new IllegalArgumentException("Uang bayar tidak valid.");
        if (cashier == null || cashier.isBlank()) throw new IllegalArgumentException("Nama kasir belum diisi di Pengaturan.");

        // Urutkan ID agar lock konsisten jika ada beberapa mesin kasir.
        List<CartLine> items = new ArrayList<>(chosenLines);
        items.sort(Comparator.comparingLong(x->x.product().id()));
        try (Connection c = Database.connect()) {
            c.setAutoCommit(false);
            try {
                BigDecimal total = BigDecimal.ZERO;
                List<SaleItem> snapshots = new ArrayList<>();
                for (CartLine line : items) {
                    if (line.quantity() < 1) throw new IllegalArgumentException("Jumlah beli minimal 1.");
                    try (PreparedStatement lock = c.prepareStatement(
                            "SELECT sku,name,category,unit,price,stock FROM products WHERE id=? FOR UPDATE")) {
                        lock.setLong(1,line.product().id());
                        try (ResultSet r = lock.executeQuery()) {
                            if (!r.next()) throw new IllegalArgumentException("Produk '" + line.product().name() + "' sudah dihapus. Perbarui keranjang.");
                            if (!r.getString("sku").equals(line.product().sku())
                                    || !r.getString("name").equals(line.product().name())
                                    || r.getBigDecimal("price").compareTo(line.product().price()) != 0)
                                throw new IllegalArgumentException("Data/harga '" + line.product().name() + "' berubah. Hapus lalu masukkan ulang dari katalog.");
                            if (r.getInt("stock") < line.quantity())
                                throw new IllegalArgumentException("Stok " + line.product().name() + " tersisa " + r.getInt("stock") + ".");
                            BigDecimal lineTotal = r.getBigDecimal("price").multiply(BigDecimal.valueOf(line.quantity()));
                            snapshots.add(new SaleItem(r.getString("sku"),r.getString("name"),r.getString("category"),
                                    r.getString("unit"),line.quantity(),r.getBigDecimal("price"),lineTotal));
                            total=total.add(lineTotal);
                        }
                    }
                }
                if (tendered.compareTo(total)<0) throw new IllegalArgumentException("Uang bayar kurang dari total belanja.");
                BigDecimal change = tendered.subtract(total);
                String invoice = "JSM-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) +
                        "-" + UUID.randomUUID().toString().substring(0,6).toUpperCase(Locale.ROOT);
                long saleId;
                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO sales(invoice_no,customer_name,cashier_name_snapshot,total,paid,change_amount) VALUES(?,?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1,invoice); p.setString(2,customerName); p.setString(3,cashier.trim());
                    p.setBigDecimal(4,total); p.setBigDecimal(5,tendered); p.setBigDecimal(6,change);
                    p.executeUpdate();
                    try (ResultSet k = p.getGeneratedKeys()) {
                        if (!k.next()) throw new SQLException("Gagal memperoleh ID transaksi.");
                        saleId=k.getLong(1);
                    }
                }
                try (PreparedStatement ins = c.prepareStatement("""
                        INSERT INTO sale_items(sale_id,sku_snapshot,name_snapshot,category_snapshot,unit_snapshot,qty,price_snapshot,line_total)
                        VALUES(?,?,?,?,?,?,?,?)
                        """);
                     PreparedStatement dec = c.prepareStatement("UPDATE products SET stock=stock-? WHERE id=? AND stock>=?")) {
                    for (int i=0; i<items.size(); i++) {
                        CartLine line=items.get(i);
                        SaleItem snap=snapshots.get(i);
                        ins.setLong(1,saleId); ins.setString(2,snap.sku()); ins.setString(3,snap.name());
                        ins.setString(4,snap.category()); ins.setString(5,snap.unit());
                        ins.setInt(6,snap.qty()); ins.setBigDecimal(7,snap.price()); ins.setBigDecimal(8,snap.total()); ins.addBatch();
                        dec.setInt(1,line.quantity()); dec.setLong(2,line.product().id()); dec.setInt(3,line.quantity());
                        if (dec.executeUpdate()!=1) throw new SQLException("Stok tidak bisa diperbarui, transaksi dibatalkan.");
                    }
                    ins.executeBatch();
                }
                c.commit();
                return findSale(saleId);
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            } finally { c.setAutoCommit(true); }
        }
    }
    public Sale findSale(long id) throws SQLException {
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement("SELECT * FROM sales WHERE id=?")) {
            p.setLong(1,id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Transaksi tidak ditemukan.");
                return mapSale(r);
            }
        }
    }
    private Sale mapSale(ResultSet r) throws SQLException {
        return new Sale(r.getLong("id"),r.getString("invoice_no"),r.getString("customer_name"),
            r.getString("cashier_name_snapshot"),r.getBigDecimal("total"),r.getBigDecimal("paid"),
            r.getBigDecimal("change_amount"),r.getTimestamp("created_at").toLocalDateTime());
    }
    public List<SaleItem> findSaleItems(long saleId) throws SQLException {
        try (Connection c = Database.connect(); PreparedStatement p = c.prepareStatement(
                "SELECT sku_snapshot,name_snapshot,category_snapshot,unit_snapshot,qty,price_snapshot,line_total FROM sale_items WHERE sale_id=? ORDER BY id")) {
            p.setLong(1,saleId);
            try (ResultSet r=p.executeQuery()) {
                List<SaleItem> items=new ArrayList<>();
                while(r.next()) items.add(new SaleItem(r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getInt(5),r.getBigDecimal(6),r.getBigDecimal(7)));
                return items;
            }
        }
    }
    public List<Sale> listSales(LocalDate from, LocalDate to, String search) throws SQLException {
        if (to.isBefore(from)) throw new IllegalArgumentException("Tanggal akhir tidak boleh sebelum tanggal awal.");
        try (Connection c=Database.connect(); PreparedStatement p=c.prepareStatement("""
                SELECT * FROM sales WHERE created_at>=? AND created_at<?
                  AND (invoice_no LIKE ? OR customer_name LIKE ? OR cashier_name_snapshot LIKE ?)
                ORDER BY created_at DESC, id DESC LIMIT 2000
                """)) {
            p.setTimestamp(1,Timestamp.valueOf(from.atStartOfDay()));
            p.setTimestamp(2,Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            String q="%"+(search==null?"":search.trim())+"%";
            p.setString(3,q);p.setString(4,q);p.setString(5,q);
            try(ResultSet r=p.executeQuery()) {
                List<Sale> out=new ArrayList<>();
                while(r.next())out.add(mapSale(r));
                return out;
            }
        }
    }
    public Summary summary(LocalDate from, LocalDate to) throws SQLException {
        BigDecimal revenue=BigDecimal.ZERO;long tx=0,qty=0,low=0;
        try(Connection c=Database.connect()) {
            try(PreparedStatement p=c.prepareStatement("SELECT COALESCE(SUM(total),0),COUNT(*) FROM sales WHERE created_at>=? AND created_at<?")) {
                p.setTimestamp(1,Timestamp.valueOf(from.atStartOfDay())); p.setTimestamp(2,Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
                try(ResultSet r=p.executeQuery()) {r.next();revenue=r.getBigDecimal(1);tx=r.getLong(2);}
            }
            try(PreparedStatement p=c.prepareStatement("""
                    SELECT COALESCE(SUM(si.qty),0) FROM sale_items si JOIN sales s ON s.id=si.sale_id
                    WHERE s.created_at>=? AND s.created_at<?
                    """)) {
                p.setTimestamp(1,Timestamp.valueOf(from.atStartOfDay()));p.setTimestamp(2,Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
                try(ResultSet r=p.executeQuery()){r.next();qty=r.getLong(1);}
            }
            try(Statement st=c.createStatement();ResultSet r=st.executeQuery("SELECT COUNT(*) FROM products WHERE stock<=5")) {
                r.next();low=r.getLong(1);
            }
        }
        return new Summary(revenue,tx,qty,low);
    }
    public List<BestSeller> bestSellers(LocalDate from, LocalDate to) throws SQLException {
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("""
                SELECT si.name_snapshot,si.sku_snapshot,SUM(si.qty) sold,SUM(si.line_total) revenue
                FROM sale_items si JOIN sales s ON s.id=si.sale_id
                WHERE s.created_at>=? AND s.created_at<?
                GROUP BY si.name_snapshot,si.sku_snapshot ORDER BY sold DESC,revenue DESC LIMIT 8
                """)) {
            p.setTimestamp(1,Timestamp.valueOf(from.atStartOfDay()));p.setTimestamp(2,Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            try(ResultSet r=p.executeQuery()) {
                List<BestSeller> out=new ArrayList<>();
                while(r.next())out.add(new BestSeller(r.getString(1),r.getString(2),r.getLong(3),r.getBigDecimal(4)));
                return out;
            }
        }
    }
    public List<SeriesPoint> dailyRevenue(LocalDate from, LocalDate to) throws SQLException {
        // Rentang hari tidak terlalu panjang agar chart tetap mudah dibaca.
        LocalDate chartStart=from.isBefore(to.minusDays(13))?to.minusDays(13):from;
        Map<LocalDate,BigDecimal> values=new LinkedHashMap<>();
        for(LocalDate d=chartStart;!d.isAfter(to);d=d.plusDays(1))values.put(d,BigDecimal.ZERO);
        try(Connection c=Database.connect();PreparedStatement p=c.prepareStatement("""
                SELECT DATE(created_at) d,COALESCE(SUM(total),0) r FROM sales
                WHERE created_at>=? AND created_at<? GROUP BY DATE(created_at)
                """)) {
            p.setTimestamp(1,Timestamp.valueOf(chartStart.atStartOfDay()));p.setTimestamp(2,Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
            try(ResultSet r=p.executeQuery()) {while(r.next())values.put(r.getDate(1).toLocalDate(),r.getBigDecimal(2));}
        }
        List<SeriesPoint> out=new ArrayList<>();
        for(var entry:values.entrySet())out.add(new SeriesPoint(entry.getKey().format(DateTimeFormatter.ofPattern("dd/MM")),entry.getValue()));
        return out;
    }
}
