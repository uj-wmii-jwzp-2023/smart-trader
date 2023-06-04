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
    @Null(message = "Id is generated automatically.")
    private String id;

    @NotNull(
            message = "User ID is required.",
            groups = {Market.class, Limit.class, TimeLimit.class}
    )
    private String userId;
    @NotNull(
            message = "Stock ID is required.",
            groups = {Market.class, Limit.class, TimeLimit.class}
    )
    private String stockId;
    @Null(
            message = "Price is not used for market orders.",
            groups = {Market.class}
    )
    @NotNull(
            message = "Price is required.",
            groups = {Limit.class, TimeLimit.class}
    )
    private BigDecimal price;
    @NotNull(
            message = "Quantity is required.",
            groups = {Market.class, Limit.class, TimeLimit.class}
    )
    private Integer quantity;
    @NotNull(
            message = "Order type is required.",
            groups = {Market.class, Limit.class, TimeLimit.class}
    )
    private OrderType orderType;
    @Null(
            message = "Cancellation time is used only in time-limit orders.",
            groups = {Market.class, Limit.class}
    )
    @NotNull(
            message = "Cancellation time is required.",
            groups = {TimeLimit.class}
    )
    private LocalDateTime cancellationTime;

    // Different order type groups for input validation in OrderController.
    public interface Market {}
    public interface Limit{}
    public interface TimeLimit {}

    public Order(@NotNull(
            message = "User ID is required.",
            groups = {Market.class, Limit.class, TimeLimit.class}
    ) String userId, @NotNull(
            message = "Stock ID is required.",
            groups = {Market.class, Limit.class, TimeLimit.class}
    ) String stockId, @NotNull(
            message = "Price is required.",
            groups = {Limit.class, TimeLimit.class}
    ) BigDecimal price, @NotNull(
            message = "Quantity is required.",
            groups = {Market.class, Limit.class, TimeLimit.class}
    ) Integer quantity, @NotNull(
            message = "Order type is required.",
            groups = {Market.class, Limit.class, TimeLimit.class}
    ) OrderType orderType, @NotNull(
            message = "Cancellation time is required.",
            groups = {TimeLimit.class}
    ) LocalDateTime cancellationTime) {
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
