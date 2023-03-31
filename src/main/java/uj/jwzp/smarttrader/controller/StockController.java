package uj.jwzp.smarttrader.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uj.jwzp.smarttrader.service.StockService;

import java.io.IOException;
import java.math.BigDecimal;

@RequestMapping(path = "api/v1/stocks", produces = "application/json")
@RestController
public class StockController {
    private final StockService service;

    @Autowired
    public StockController(StockService service) {
        this.service = service;
    }

    @GetMapping("/{ticker}")
    public BigDecimal getPrice(@PathVariable("ticker") String ticker) throws IOException {
        return service.getStockPrice(ticker);
    }
}
