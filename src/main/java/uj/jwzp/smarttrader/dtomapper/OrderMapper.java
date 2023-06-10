package uj.jwzp.smarttrader.dtomapper;

import org.springframework.stereotype.Component;
import uj.jwzp.smarttrader.dto.OrderDto;
import uj.jwzp.smarttrader.model.Order;
import uj.jwzp.smarttrader.repository.StockRepository;
import uj.jwzp.smarttrader.repository.UserRepository;

@Component
public class OrderMapper {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    public OrderMapper(UserRepository userRepository, StockRepository stockRepository) {
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
    }

    public OrderDto toDto(Order order) {
        String username = userRepository.findUserById(order.getUserId()).orElseThrow().getName();
        String ticker = stockRepository.findStockById(order.getStockId()).orElseThrow().getTicker();

        return new OrderDto(
                username,
                ticker,
                order.getPrice(),
                order.getQuantity(),
                order.getOrderType(),
                order.getCancellationTime()
        );
    }
    public Order toEntity(OrderDto dto) {
        String userId = userRepository.findUserByName(dto.getUsername()).orElseThrow().getId();
        String stockId = stockRepository.findStockByTicker(dto.getTicker()).orElseThrow().getId();

        return new Order(
                userId,
                stockId,
                dto.getPrice(),
                dto.getQuantity(),
                dto.getOrderType(),
                dto.getCancellationTime()
        );
    }
}
