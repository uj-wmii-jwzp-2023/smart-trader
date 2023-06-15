package uj.jwzp.smarttrader.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class StockApiWrapper {

    @Value("${api.url}")
    private String apiUrl;

    public BigDecimal getStockPrice(String stockTicker) throws Exception {
        String url = String.format(apiUrl, stockTicker);

        RestTemplate restTemplate = new RestTemplate();

        String stooqResponse = restTemplate.getForObject(url, String.class).trim();
        return parseLatestPrice(stooqResponse);
    }

    BigDecimal parseLatestPrice(String response) throws Exception {
        if (response.contains("N/D")) {
            throw new Exception("Price not found.");
        }
        return new BigDecimal(response);
    }

}
