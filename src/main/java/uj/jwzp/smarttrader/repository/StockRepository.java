package uj.jwzp.smarttrader.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uj.jwzp.smarttrader.model.Stock;

public interface StockRepository extends MongoRepository<Stock, String> {
    Boolean existsByTicker(String ticker);
}