package id.josjismart.model;
import java.math.BigDecimal;
public record Product(long id, String sku, String name, String category, String unit, BigDecimal price, int stock) {
    @Override public String toString() { return name + "  •  " + sku + "  (stok " + stock + ")"; }
}
