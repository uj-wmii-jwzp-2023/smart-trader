package uj.jwzp.smarttrader.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uj.jwzp.smarttrader.model.Stock;
import uj.jwzp.smarttrader.repository.StockRepository;
import uj.jwzp.smarttrader.utils.StockApiWrapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MarketRefreshServiceTest {

    @Mock
    StockRepository stockRepository;
    @Mock
    OrderService orderService;

    @Mock
    StockApiWrapper apiWrapper;

    @InjectMocks
    MarketRefreshService marketRefreshService;

    @Test
    void UpdateAllStockPrices_Should_Update_Stock_Prices() throws Exception {
        String name = "Company Name";
        String ticker = "TICKER";
        String id = "0";
        Stock stock = new Stock(ticker, name);
        stock.setId(id);
        stock.setPrice(BigDecimal.valueOf(100));
        List<Stock> stocks = List.of(stock);

        given(apiWrapper.getStockPrice(anyString())).willReturn(BigDecimal.ONE);
        given(stockRepository.findAll()).willReturn(stocks);

        marketRefreshService.updateAllStockPrices();

        verify(stockRepository, times(stocks.size())).save(any(Stock.class));
        Assertions.assertThat(stock.getPrice()).isEqualTo(BigDecimal.ONE);
    }

}
