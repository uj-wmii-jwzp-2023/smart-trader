package uj.jwzp.smarttrader.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uj.jwzp.smarttrader.dto.OrderBookDto;
import uj.jwzp.smarttrader.dto.PatchStockDto;
import uj.jwzp.smarttrader.model.Stock;
import uj.jwzp.smarttrader.service.OrderService;
import uj.jwzp.smarttrader.service.StockService;

import java.util.List;
import java.util.Optional;

@RequestMapping(path = "api/v1/stocks", produces = "application/json")
@RestController
public class StockController {
    private final StockService stockService;

    private final OrderService orderService;
    private static Logger logger = LoggerFactory.getLogger(StockController.class);

    public StockController(StockService stockService, OrderService orderService) {
        this.stockService = stockService;
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Stock>> getAllStocks() {
        return new ResponseEntity<>(stockService.getAllStocks(), HttpStatus.OK);
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
        logger.info("Trying to add new stock {}.", stock.getId());

        if (stockService.existsByTicker(stock.getTicker())) {
            return new ResponseEntity<>("Stock with the same ticker is already added.", HttpStatus.BAD_REQUEST);
        }
        stockService.addStock(stock);

        return new ResponseEntity<>("Stock created", HttpStatus.CREATED);
    }

    @PatchMapping("/{stockId}")
    public ResponseEntity<String> updateStock(@PathVariable("stockId") String stockId,
                                              @RequestBody PatchStockDto patchStockDto) {
        logger.info("Trying to update stock {}.", stockId);

        if (!stockService.existsById(stockId)) {
            return new ResponseEntity<>("Stock does not exist", HttpStatus.NOT_FOUND);
        }
        stockService.updateStock(stockId, patchStockDto);
        return new ResponseEntity<>("Stock updated", HttpStatus.OK);
    }

    @DeleteMapping("/{stockId}")
    public ResponseEntity<String> deleteStock(@PathVariable("stockId") String stockId) {
        logger.info("Trying to delete stock {}.", stockId);

        if (stockService.existsById(stockId)) {
            stockService.deleteById(stockId);
            return new ResponseEntity<>("Stock deleted successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Stock does not exist", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{ticker}/order-book")
    public ResponseEntity<List<OrderBookDto>> getOrderBook(@PathVariable("ticker") String ticker) {
        logger.info("Request for order book for {}.", ticker);

        if (!stockService.existsByTicker(ticker)) {
            return new ResponseEntity<>(List.of(), HttpStatus.NOT_FOUND);
        }
        List<OrderBookDto> orderBook = orderService.getOrderBook(ticker);
        return new ResponseEntity<>(orderBook, HttpStatus.OK);
    }

}