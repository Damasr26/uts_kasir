package id.josjismart.model;
import java.math.BigDecimal;
public record SaleItem(String sku, String name, String category, String unit, int qty, BigDecimal price, BigDecimal total) {}
