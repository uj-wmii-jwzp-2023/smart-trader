package uj.jwzp.smarttrader.service;

import org.springframework.stereotype.Service;
import uj.jwzp.smarttrader.model.*;
import uj.jwzp.smarttrader.repository.OrderRepository;
import uj.jwzp.smarttrader.repository.StockRepository;
import uj.jwzp.smarttrader.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final Clock clock;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, StockRepository stockRepository, Clock clock) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
        this.clock = clock;
    }

    private List<String> validateNewOrder(Order order) {
        List<String> errors = new ArrayList<>();

        if (order.getOrderType() != OrderType.MARKET && order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Price should be a positive value.");
        }
        if (order.getQuantity() <= 0) {
            errors.add("Quantity should be a positive value.");
        }
        if (order.getCancellationTime() != null && order.getCancellationTime().isBefore(LocalDateTime.now(clock))) {
            errors.add("Cancellation time should be a future date.");
        }

        return errors;
    }

    private List<String> validateOrderBeforeExecuting(Order order, Optional<User> optionalUser, Optional<Stock> optionalStock) {
        List<String> errors = new ArrayList<>();

        if (optionalUser.isEmpty()) {
            errors.add("User does not exist.");
        }
        if (optionalStock.isEmpty()) {
            errors.add("Invalid stock ticker.");
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        User user = optionalUser.get();
        Stock stock = optionalStock.get();

        if (order.getOrderSide() == OrderSide.SELL) {
            errors.addAll(validateSellOrder(order, user, stock));
        }

        if (order.getOrderSide() == OrderSide.BUY) {
            errors.addAll(validateBuyOrder(order, user, stock));
        }

        return errors;
    }

    private List<String> validateSellOrder(Order order, User user, Stock stock) {
        List<String> errors = new ArrayList<>();

        if (order.getQuantity() > user.getAssets().stream().filter(asset -> asset.stockId.equals(stock.getId())).count()) {
            errors.add("Quantity should be smaller or equal to the amount of stock that user owns");
        }

        return errors;
    }

    private List<String> validateBuyOrder(Order order, User user, Stock stock) {
        List<String> errors = new ArrayList<>();

        BigDecimal totalPrice = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        if (totalPrice.compareTo(user.getCashBalance()) > 0) {
            errors.add("Total price should be smaller or equal to user cash balance");
        }

        return errors;
    }

    public OrderValidationResponse addOrder(Order order) {
        var errors = validateNewOrder(order);
        if (!errors.isEmpty()) {
            return new OrderValidationResponse(false, errors);
        }

        String userId = order.getUserId();
        String stockId = order.getStockId();
        Optional<User> optionalUser = userRepository.findUserById(userId);
        Optional<Stock> optionalStock = stockRepository.findStockById(stockId);

        errors = validateOrderBeforeExecuting(order, optionalUser, optionalStock);
        if (!errors.isEmpty()) {
            return new OrderValidationResponse(false, errors);
        }

        boolean realised = executeOrder(order, optionalUser, optionalStock);

        if (!realised && order.getOrderType() != OrderType.MARKET) {
            orderRepository.save(order);
        }

        return new OrderValidationResponse(true, new ArrayList<>());
    }

    public boolean executeOrder(Order order, Optional<User> optionalUser, Optional<Stock> optionalStock) {
        User user = optionalUser.get();
        Stock stock = optionalStock.get();

        if (order.getOrderSide() == OrderSide.SELL) {
            return executeSellOrder(order, user, stock);
        }
        return executeBuyOrder(order, user, stock);
    }

    private boolean executeSellOrder(Order order, User user, Stock stock) {
        return true;
    }

    private boolean executeBuyOrder(Order order, User user, Stock stock) {
        return true;
    }

    public void matchOrders() {
        var orders = orderRepository.findAll();

        for (var order : orders) {

        }

    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findAllByUserId(userId);
    }

    public List<Order> getOrdersByUserName(String username) {
        Optional<User> user = userRepository.findUserByName(username);
        return user.map(User::getId).map(this::getOrdersByUserId).orElse(new ArrayList<>());
    }

    public Optional<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }
}
