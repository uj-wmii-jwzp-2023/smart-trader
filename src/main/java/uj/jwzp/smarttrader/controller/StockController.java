package uj.jwzp.smarttrader.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uj.jwzp.smarttrader.model.Stock;
import uj.jwzp.smarttrader.service.StockService;

import java.util.List;
import java.util.Optional;

@RequestMapping(path = "api/v1/stocks", produces = "application/json")
@RestController
public class StockController {
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }
    private final StockService stockService;

    @GetMapping
    public List<Stock> getAllStocks() {
        return stockService.getAllStocks();
    }

    @GetMapping("/{ticker}")
    public ResponseEntity<Stock> getStock(@PathVariable("ticker") String ticker) {
        Optional<Stock> optionalStock = stockService.getStockByTicker(ticker);
        return optionalStock
                .map(person -> new ResponseEntity<>(person, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<String> addStock(@Valid @RequestBody Stock stock) {
        if (stockService.existsByTicker(stock.getTicker())) {
            return new ResponseEntity<>("Stock with the same ticker is already added.", HttpStatus.BAD_REQUEST);
        }
        stockService.addStock(stock);

        return new ResponseEntity<>("Stock created", HttpStatus.CREATED);
    }
}