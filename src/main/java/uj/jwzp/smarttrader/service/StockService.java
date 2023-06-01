package uj.jwzp.smarttrader.service;

import org.springframework.stereotype.Service;
import uj.jwzp.smarttrader.model.Stock;
import uj.jwzp.smarttrader.repository.StockRepository;

import java.util.List;
import java.util.Optional;

@Service
public class StockService {
    private final StockRepository stockRepository;
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public void addStock(Stock stock) {
        stockRepository.save(stock);
    }

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public Optional<Stock> getStockById(String id) {
        return stockRepository.findById(id);
    }

    public Boolean existsByTicker(String ticker) {
        return stockRepository.existsByTicker(ticker);
    }
}