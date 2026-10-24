package id.josjismart.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record Sale(long id, String invoice, String customer, String cashier, BigDecimal total,
                   BigDecimal paid, BigDecimal change, LocalDateTime dateTime) {}
