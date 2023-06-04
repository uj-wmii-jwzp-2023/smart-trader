package uj.jwzp.smarttrader.model;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document("Stock")
public class Stock {
    @Id
    private String id;
    @Indexed(unique=true)
    @NotNull(message = "Ticker field is required.")
    private String ticker;
    @NotNull(message = "Name field is required.")
    private String name;
    private BigDecimal price;

    public Stock(String ticker, String name) {
        this.ticker = ticker;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}