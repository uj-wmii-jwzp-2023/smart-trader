package uj.jwzp.smarttrader.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
public class StockRefreshServiceTest {

    @InjectMocks
    StockRefreshService stockRefreshService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(stockRefreshService, "apiKey", "5COR5RTOJLBUVDG2");
        ReflectionTestUtils.setField(stockRefreshService, "apiUrl", "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s");
    }

    @Disabled
    @Test
    public void test() {
        try {
            BigDecimal price = stockRefreshService.getStockPrice("AAPL");
            System.out.println(price);
        } catch (Exception e) {
            System.out.println("Stock not found.");
        }
    }

    @Test
    public void ParseLatestPrice_Should_Return_ValidPrice() {
        String response = "{\"Global Quote\":{\"01. symbol\":\"AAPL\",\"02. open\":\"181.0300\",\"03. high\":\"181.7800\",\"04. low\":\"179.2600\",\"05. price\":\"180.9500\",\"06. volume\":\"61996913\",\"07. latest trading day\":\"2023-06-02\",\"08. previous close\":\"180.0900\",\"09. change\":\"0.8600\",\"10. change percent\":\"0.4775%\"}}";
        Assertions.assertThatCode(() -> {
            BigDecimal price = stockRefreshService.parseLatestPrice(response);
            BigDecimal expectedPrice = new BigDecimal("180.9500");
            Assertions.assertThat(price).isEqualTo(expectedPrice);
        }).doesNotThrowAnyException();
    }

    @Test
    public void ParseLatestPrice_Should_Throw_When_Invalid_Input() {
        String response = "{\"Global Quote\":{}}";
        Assertions.assertThatThrownBy(()->stockRefreshService.parseLatestPrice(response)).isInstanceOf(Exception.class);
    }

    @Test
    public void GetStockPrice_Should_Throw_When_Ticker_Not_Found() {
        Assertions.assertThatThrownBy(()->stockRefreshService.getStockPrice(",,,,")).isInstanceOf(Exception.class);
    }
}
