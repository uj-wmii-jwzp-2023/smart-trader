package uj.jwzp.smarttrader.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uj.jwzp.smarttrader.model.Stock;
import uj.jwzp.smarttrader.service.StockService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers=StockController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class StockControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockService stockService;

    @Autowired
    private ObjectMapper objectMapper;

    private Stock dummyStock;

    @BeforeEach
    public void setup() {
        String name = "Dummy Name";
        String ticker = "DUMMY";

        dummyStock = new Stock(name, ticker);
    }

    @Test
    public void GetStock_Should_ReturnOk_When_Exists() throws Exception {
        String dummyTicker = "TICKER";
        dummyStock.setTicker(dummyTicker);

        given(stockService.getStockByTicker(dummyTicker)).willReturn(Optional.of(dummyStock));

        String url = String.format("/api/v1/stocks/%s", dummyTicker);
        String body = mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Stock returnedStock = objectMapper.readValue(body, Stock.class);

        Assertions.assertThat(returnedStock).usingRecursiveComparison().isEqualTo(dummyStock);
    }

    @Test
    public void GetStock_Should_ReturnNotFound_When_DontExists() throws Exception {
        String existingTicker = "REAL";
        String notExistingTicker = "FAKE";
        dummyStock.setId(existingTicker);

        given(stockService.getStockById(existingTicker)).willReturn(Optional.of(dummyStock));

        String url = String.format("/api/v1/stocks/%s", notExistingTicker);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isNotFound());

    }

    @Test
    public void AddStocks_Should_ReturnCreated_When_ValidRequest() throws Exception {
        String requestBody = objectMapper.writeValueAsString(dummyStock);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/v1/stocks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
            )
            .andExpect(MockMvcResultMatchers.status().isCreated());

        verify(stockService).addStock(any(Stock.class));
    }

    @Test
    public void AddStocks_Should_ReturnBadRequest_When_MissingName() throws Exception {
        dummyStock.setName(null);
        String requestBody = objectMapper.writeValueAsString(dummyStock);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(stockService, never()).addStock(any(Stock.class));
    }

    @Test
    public void AddStocks_Should_ReturnBadRequest_When_MissingTicker() throws Exception {
        dummyStock.setTicker(null);
        String requestBody = objectMapper.writeValueAsString(dummyStock);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(stockService, never()).addStock(any(Stock.class));
    }

    @Test
    public void AddStocks_Should_ReturnBadRequest_When_AlreadyExists() throws Exception {
        String requestBody = objectMapper.writeValueAsString(dummyStock);

        given(stockService.existsByTicker(dummyStock.getTicker())).willReturn(Boolean.TRUE);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(stockService, never()).addStock(any(Stock.class));
    }
}
