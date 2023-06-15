package uj.jwzp.smarttrader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uj.jwzp.smarttrader.dto.PatchStockDto;
import uj.jwzp.smarttrader.model.Stock;
import uj.jwzp.smarttrader.repository.StockRepository;

import java.util.List;
import java.util.Optional;

@Service
public class StockService {
    private final StockRepository stockRepository;

    private static Logger logger = LoggerFactory.getLogger(StockService.class);

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public void addStock(Stock stock) {
        stockRepository.save(stock);

        logger.info("{} stock with ticker {} added to list of available stocks.", stock.getName(), stock.getTicker());
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

    public Optional<Stock> getStockByTicker(String ticker) {
        return stockRepository.findStockByTicker(ticker);
    }

    public boolean existsById(String stockId) {
        return stockRepository.existsById(stockId);
    }

    public void deleteById(String stockId) {
        stockRepository.deleteById(stockId);
    }

    public void updateStock(String stockId, PatchStockDto patchStockDto) {
        Optional<Stock> optionalStock = stockRepository.findStockById(stockId);
        Stock stock = optionalStock.get();

        if (patchStockDto.getTicker() != null && stockRepository.existsByTicker(patchStockDto.getTicker())) {
            stock.setTicker(patchStockDto.getTicker());
            logger.debug("{} stock changed ticker to {}.", stock.getId(), patchStockDto.getTicker());
        }
        if (patchStockDto.getName() != null) {
            stock.setName(patchStockDto.getName());
            logger.debug("{} stock changed name to {}.", stock.getId(), patchStockDto.getName());
        }

        logger.info("{} stock updated.", stock.getId());

        stockRepository.save(stock);
    }
}