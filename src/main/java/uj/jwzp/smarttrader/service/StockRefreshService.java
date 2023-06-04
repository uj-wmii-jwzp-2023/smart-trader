package uj.jwzp.smarttrader.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uj.jwzp.smarttrader.model.Stock;
import uj.jwzp.smarttrader.repository.StockRepository;
import uj.jwzp.smarttrader.utils.StockApiWrapper;

import java.math.BigDecimal;
import java.util.List;


@Service
public class StockRefreshService {
    StockRepository stockRepository;

    StockApiWrapper apiWrapper;

    @Autowired
    public StockRefreshService(StockRepository stockRepository, StockApiWrapper apiWrapper) {
        this.stockRepository = stockRepository;
        this.apiWrapper = apiWrapper;
    }

    @Scheduled(fixedRate = 70000)
    public void updateAllStockPrices() {
        List<Stock> stocks = stockRepository.findAll();
        for (var stock : stocks) {
            try {
                BigDecimal newPrice = apiWrapper.getStockPrice(stock.getTicker());
                stock.setPrice(newPrice);
                stockRepository.save(stock);
            } catch (Exception e) {
                System.out.println("Stock ticker not found."); // log
            }
        }
    }

}
