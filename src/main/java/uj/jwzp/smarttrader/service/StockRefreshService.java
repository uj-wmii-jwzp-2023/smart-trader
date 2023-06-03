package uj.jwzp.smarttrader.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uj.jwzp.smarttrader.model.Stock;
import uj.jwzp.smarttrader.repository.StockRepository;

import java.math.BigDecimal;
import java.util.List;


@Service
public class StockRefreshService {
    StockRepository stockRepository;

    @Autowired
    public StockRefreshService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Value("${api.key}")
    private String apiKey;

    @Value("${api.url}")
    private String apiUrl;

    public BigDecimal getStockPrice(String stockTicker) throws Exception {
        String url = String.format(apiUrl, stockTicker, apiKey);
//        String url = String.format("https://cloud.iexapis.com/stable/stock/%s/quote?token=%s", stockTicker, iexapiKey);
//        String apiUrl = "https://stooq.com/q/l/?s=" + stockTicker + "&f=sd2t2c&h";

        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(url, String.class);
        return parseLatestPrice(jsonResponse);
    }

    BigDecimal parseLatestPrice(String response) throws Exception {
        ObjectMapper objectMapper=new ObjectMapper();
        JsonNode jsonNode= null;
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            throw new Exception("Cannot read JSON response.");
        }

        if(!jsonNode.has("Global Quote")) {
            throw new Exception("Global quote not found.");
        }

        JsonNode globalQuote =jsonNode.get("Global Quote");


        if(!globalQuote.has("05. price")) {
            throw new Exception("Price not found.");
        }
        String latestPrice=globalQuote.get("05. price").asText();

        return new BigDecimal(latestPrice);
    }



    @Scheduled(fixedRate = 70000)
    public void updateAllStockPrices() {

        List<Stock> stocks = stockRepository.findAll();
        for(var stock : stocks) {
            try {
                BigDecimal newPrice= getStockPrice(stock.getTicker());
                stock.setPrice(newPrice);
                stockRepository.save(stock);
            } catch (Exception e) {
                System.out.println("Stock ticker not found."); // log
            }
        }
    }

}
