package uj.jwzp.smarttrader.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uj.jwzp.smarttrader.model.Stock;

import java.util.Optional;

public interface StockRepository extends MongoRepository<Stock, String> {
    Boolean existsByTicker(String ticker);
    Optional<Stock> findStockByTicker(String ticker);
    Optional<Stock> findStockById(String id);

}