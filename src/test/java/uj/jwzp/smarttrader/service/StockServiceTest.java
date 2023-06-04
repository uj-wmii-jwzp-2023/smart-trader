package uj.jwzp.smarttrader.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uj.jwzp.smarttrader.model.Role;
import uj.jwzp.smarttrader.model.Stock;
import uj.jwzp.smarttrader.repository.StockRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {
    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    public void GetStockById_Should_ReturnStock_When_ValidId() {
        String name = "Company Name";
        String ticker = "TICKER";
        String id = "0";

        Stock stock = new Stock(ticker, name);
        stock.setId(id);

        given(stockRepository.findById(stock.getId())).willReturn(Optional.of(stock));

        Optional<Stock> savedStock = stockService.getStockById(id);

        Assertions.assertThat(savedStock).isNotEmpty().hasValue(stock);
    }

}
