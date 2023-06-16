package uj.jwzp.smarttrader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uj.jwzp.smarttrader.model.Stock;
import uj.jwzp.smarttrader.repository.StockRepository;
import uj.jwzp.smarttrader.utils.StockApiWrapper;

import java.math.BigDecimal;
import java.util.List;


@Service
public class MarketRefreshService {
    StockRepository stockRepository;
    private final OrderService orderService;
    private static Logger logger = LoggerFactory.getLogger(MarketRefreshService.class);
    StockApiWrapper apiWrapper;
    public final static String MARKET_TIME_WINDOW = "*/30 * 9-16 * * 1-5"; // every 30 seconds, 9am-5pm, Monday-Friday

    @Autowired
    public MarketRefreshService(StockRepository stockRepository, OrderService orderService, StockApiWrapper apiWrapper) {
        this.stockRepository = stockRepository;
        this.orderService = orderService;
        this.apiWrapper = apiWrapper;
    }

    @Scheduled(cron = MARKET_TIME_WINDOW)
    public void refreshMarket() {
        updateAllStockPrices();
        orderService.matchOrders();
    }

    public void updateAllStockPrices() {
        logger.info("Updating stock prices.");

        List<Stock> stocks = stockRepository.findAll();
        for (var stock : stocks) {
            try {
                BigDecimal newPrice = apiWrapper.getStockPrice(stock.getTicker());
                stock.setPrice(newPrice);
                stockRepository.save(stock);
                logger.info("{} price updated to {}.", stock.getTicker(), newPrice);
            } catch (Exception e) {
                logger.error("Unable to fetch price for {}.", stock.getTicker());
            }
        }
    }

}
