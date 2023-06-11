package uj.jwzp.smarttrader.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import uj.jwzp.smarttrader.model.OrderSide;
import uj.jwzp.smarttrader.model.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDto {
    @Null(
            message = "Username of logged-in user is added automatically.",
            groups = {Market.class, Limit.class, TimeLimit.class}
    )
    private String username;
    @NotNull(
            message = "Stock ticker is required.",
            groups = {Market.class, Limit.class, TimeLimit.class}
    )
    private String ticker;
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
    private OrderSide orderSide;
    @Null(
            message = "Cancellation time is used only in time-limit orders.",
            groups = {Market.class, Limit.class}
    )
    @NotNull(
            message = "Cancellation time is required.",
            groups = {TimeLimit.class}
    )
    private LocalDateTime cancellationTime;
    private OrderType orderType;

    // Different order type groups for input validation in OrderController.
    public interface Market {
    }

    public interface Limit {
    }

    public interface TimeLimit {
    }

    public OrderDto(String username, @NotNull(
            message = "Stock ticker is required.",
            groups = {Market.class, Limit.class, TimeLimit.class}
    ) String ticker, @NotNull(
            message = "Price is required.",
            groups = {Limit.class, TimeLimit.class}
    ) BigDecimal price, @NotNull(
            message = "Quantity is required.",
            groups = {Market.class, Limit.class, TimeLimit.class}
    ) Integer quantity, @NotNull(
            message = "Order type is required.",
            groups = {Market.class, Limit.class, TimeLimit.class}
    ) OrderSide orderSide, OrderType orderType, @NotNull(
            message = "Cancellation time is required.",
            groups = {TimeLimit.class}
    ) LocalDateTime cancellationTime) {
        this.username = username;
        this.ticker = ticker;
        this.price = price;
        this.quantity = quantity;
        this.orderSide = orderSide;
        this.cancellationTime = cancellationTime;
        this.orderType = orderType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
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


    public LocalDateTime getCancellationTime() {
        return cancellationTime;
    }

    public void setCancellationTime(LocalDateTime cancellationTime) {
        this.cancellationTime = cancellationTime;
    }

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public void setOrderSide(OrderSide orderSide) {
        this.orderSide = orderSide;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }
}
