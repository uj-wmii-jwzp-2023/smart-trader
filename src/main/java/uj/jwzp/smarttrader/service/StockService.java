package uj.jwzp.smarttrader.service;

import org.springframework.stereotype.Service;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;

@Service
public class StockService {
    public BigDecimal getStockPrice(String ticker) throws IOException {
        return YahooFinance.get(ticker).getQuote(true).getPrice();
    }
}
