package uj.jwzp.smarttrader.model;

public class Asset {
    public String stockId;

    public Integer quantity;

    public Asset(String stockId, Integer quantity) {
        this.stockId = stockId;
        this.quantity = quantity;
    }
}
