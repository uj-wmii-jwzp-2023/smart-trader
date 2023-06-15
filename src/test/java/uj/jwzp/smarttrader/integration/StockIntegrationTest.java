package uj.jwzp.smarttrader.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uj.jwzp.smarttrader.dto.PatchStockDto;
import uj.jwzp.smarttrader.model.Stock;
import uj.jwzp.smarttrader.repository.StockRepository;

import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class StockIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StockRepository stockRepository;

    @WithMockUser(authorities = "ADMIN")
    @Test
    public void Stock_Works_Through_All_Layers() throws Exception {
        String ticker = "test-ticker123123";
        String name = "Allegro";
        Stock stock = new Stock(ticker, name);
        String requestBody = objectMapper.writeValueAsString(stock);

        // add
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/v1/stocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isCreated());

        Optional<Stock> optionalStock = stockRepository.findStockByTicker(ticker);
        Assertions.assertThat(optionalStock.isPresent()).isTrue();
        Assertions.assertThat(optionalStock.get().getName()).isEqualTo(name);
        String stockId = optionalStock.get().getId();

        // update
        String updatedName = "CD Projekt Red";
        PatchStockDto patchStockDto = new PatchStockDto(null, updatedName);
        String patchRequest = objectMapper.writeValueAsString(patchStockDto);

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/api/v1/stocks/"+ stockId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchRequest)
                )
                .andExpect(status().isOk());

        Optional<Stock> updatedStock = stockRepository.findStockById(stockId);
        Assertions.assertThat(updatedStock.isPresent()).isTrue();
        Assertions.assertThat(updatedStock.get().getName()).isEqualTo(updatedName);
        Assertions.assertThat(updatedStock.get().getTicker()).isEqualTo(ticker);


        // delete
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/v1/stocks/" + stockId)
                )
                .andExpect(status().isOk());

        boolean existsByTicker = stockRepository.existsByTicker(ticker);

        Assertions.assertThat(existsByTicker).isFalse();
    }

}
