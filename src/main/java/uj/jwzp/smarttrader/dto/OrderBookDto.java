package uj.jwzp.smarttrader.dto;

import uj.jwzp.smarttrader.model.OrderSide;

import java.math.BigDecimal;

public class OrderBookDto {
    private BigDecimal price;
    private Integer quantity;
    private OrderSide orderSide;

    public OrderBookDto(BigDecimal price, Integer quantity, OrderSide orderSide) {
        this.price = price;
        this.quantity = quantity;
        this.orderSide = orderSide;
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
}
