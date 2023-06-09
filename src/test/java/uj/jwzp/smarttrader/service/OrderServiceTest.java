package uj.jwzp.smarttrader.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uj.jwzp.smarttrader.model.Order;
import uj.jwzp.smarttrader.model.OrderType;
import uj.jwzp.smarttrader.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void GetOrderById_Should_ReturnOrder_When_ValidId() {
        String userId = "1";
        String stockId = "2";
        BigDecimal price = new BigDecimal(10);
        Integer quantity = 10;
        OrderType orderType = OrderType.BUY;
        LocalDateTime cancellationTime = null;

        Order order = new Order(userId, stockId, price, quantity, orderType, cancellationTime);
        String orderId = "0";
        order.setId(orderId);

        given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));

        Optional<Order> savedOrder = orderService.getOrderById(orderId);

        Assertions.assertThat(savedOrder).isNotEmpty().hasValue(order);
    }

}
