package uj.jwzp.smarttrader.utils;

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
public class StockApiWrapperTest {

    @InjectMocks
    StockApiWrapper apiWrapper;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(apiWrapper, "apiUrl", "https://stooq.com/q/l/?s=%s&f=c");
    }

    @Test
    public void ParseLatestPrice_Should_Return_ValidPrice() {
        String response = "180.9500";
        Assertions.assertThatCode(() -> {
            BigDecimal price = apiWrapper.parseLatestPrice(response);
            BigDecimal expectedPrice = new BigDecimal("180.9500");
            Assertions.assertThat(price).isEqualTo(expectedPrice);
        }).doesNotThrowAnyException();
    }

    @Test
    public void GetStockPrice_Should_Throw_When_Ticker_Not_Found() {
        Assertions.assertThatThrownBy(() -> apiWrapper.getStockPrice(",,,,")).isInstanceOf(Exception.class);
    }
}
