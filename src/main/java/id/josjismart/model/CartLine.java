package id.josjismart.model;
import java.math.BigDecimal;
public final class CartLine {
    private final Product product;
    private int quantity;
    public CartLine(Product product, int quantity) { this.product=product; this.quantity=quantity; }
    public Product product() { return product; }
    public int quantity() { return quantity; }
    public void setQuantity(int value) { quantity=value; }
    public BigDecimal total() { return product.price().multiply(BigDecimal.valueOf(quantity)); }
}
