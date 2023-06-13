package uj.jwzp.smarttrader.dto;

import uj.jwzp.smarttrader.model.OrderSide;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PatchOrderDto {
    private BigDecimal price;
    private Integer quantity;
    private OrderSide orderSide;
    private LocalDateTime cancellationTime;

    public PatchOrderDto(BigDecimal price, Integer quantity, OrderSide orderSide, LocalDateTime cancellationTime) {
        this.price = price;
        this.quantity = quantity;
        this.orderSide = orderSide;
        this.cancellationTime = cancellationTime;
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

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public void setOrderSide(OrderSide orderSide) {
        this.orderSide = orderSide;
    }

    public LocalDateTime getCancellationTime() {
        return cancellationTime;
    }

    public void setCancellationTime(LocalDateTime cancellationTime) {
        this.cancellationTime = cancellationTime;
    }
}
