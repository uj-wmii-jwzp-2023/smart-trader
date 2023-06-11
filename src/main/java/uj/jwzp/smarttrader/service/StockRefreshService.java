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
    private final OrderService orderService;

    StockApiWrapper apiWrapper;

    @Autowired
    public StockRefreshService(StockRepository stockRepository, OrderService orderService, StockApiWrapper apiWrapper) {
        this.stockRepository = stockRepository;
        this.orderService = orderService;
        this.apiWrapper = apiWrapper;
    }

    @Scheduled(cron = "10 */1 9-16 * * 1-5") // every 70 second, 9am-5pm, Monday-Friday
    public void updateAllStockPrices() {
        List<Stock> stocks = stockRepository.findAll();
        for (var stock : stocks) {
            try {
                BigDecimal newPrice = apiWrapper.getStockPrice(stock.getTicker());
                stock.setPrice(newPrice);
                stockRepository.save(stock);
                System.out.println(stock.getTicker() + " price updated."); // log
            } catch (Exception e) {
                System.out.println(stock.getTicker() + " price not found."); // log
            }
        }

        orderService.matchOrders();
    }

}
