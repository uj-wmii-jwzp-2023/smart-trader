package uj.jwzp.smarttrader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;

@SpringBootApplication
public class SmartTraderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTraderApplication.class, args);
    }

    Stock stock;

    {
        try {
            stock = YahooFinance.get("TSLA");
            BigDecimal price = stock.getQuote(true).getPrice();
            stock.print();
            System.out.println("TSLA price: " + price);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
