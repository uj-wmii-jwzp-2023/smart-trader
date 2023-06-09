package uj.jwzp.smarttrader.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document("Order")
public class Order {
    @Id
    private String id;
    @NotNull(message = "User Id field is required.")
    private String userId;
    @NotNull(message = "Stock Id field is required.")
    private String stockId;
    @NotNull(message = "Price field is required.")
    private BigDecimal price;
    @NotNull(message = "Quantity field is required.")
    private Integer quantity;
    @NotNull(message = "Order Type field is required.")
    private OrderType orderType;
    private LocalDateTime cancellationTime;

    public Order(String userId, String stockId, BigDecimal price, Integer quantity, OrderType orderType, LocalDateTime cancellationTime) {
        this.userId = userId;
        this.stockId = stockId;
        this.price = price;
        this.quantity = quantity;
        this.orderType = orderType;
        this.cancellationTime = cancellationTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public LocalDateTime getCancellationTime() {
        return cancellationTime;
    }

    public void setCancellationTime(LocalDateTime cancellationTime) {
        this.cancellationTime = cancellationTime;
    }
}
