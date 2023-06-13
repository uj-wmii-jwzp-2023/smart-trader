package uj.jwzp.smarttrader.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import uj.jwzp.smarttrader.model.Order;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findAllByUserId(String userId);

    List<Order> findAllByStockId(String stockId);

    boolean existsById(@NotNull String stockId);
}