package uj.jwzp.smarttrader.service;

import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import uj.jwzp.smarttrader.model.*;
import uj.jwzp.smarttrader.repository.OrderRepository;
import uj.jwzp.smarttrader.repository.StockRepository;
import uj.jwzp.smarttrader.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public ValidationResponse validateNewOrder(Order order) {
        ValidationResponse validationResponse = new ValidationResponse();

        if (order.getOrderType() != OrderType.MARKET && order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            validationResponse.addMessage("Price should be a positive value.");
        }
        if (order.getQuantity() <= 0) {
            validationResponse.addMessage("Quantity should be a positive value.");
        }
        if (order.getCancellationTime() != null && order.getCancellationTime().isBefore(LocalDateTime.now(clock))) {
            validationResponse.addMessage("Cancellation time should be a future date.");
        }

        return validationResponse;
    }

    public ValidationResponse validateOrderBeforeExecuting(Order order, Optional<User> optionalUser, Optional<Stock> optionalStock) {
        ValidationResponse validationResponse = new ValidationResponse();

        if (optionalUser.isEmpty()) {
            validationResponse.addMessage("User does not exist.");
        }
        if (optionalStock.isEmpty()) {
            validationResponse.addMessage("Invalid stock ticker.");
        }

        if (!validationResponse.isValid()) {
            return validationResponse;
        }

        User user = optionalUser.get();
        Stock stock = optionalStock.get();

        if (order.getOrderSide() == OrderSide.SELL) {
            return validateSellOrder(order, user, stock);
        }

        return validateBuyOrder(order, user, stock);
    }

    public ValidationResponse validateSellOrder(Order order, User user, Stock stock) {
        ValidationResponse validationResponse = new ValidationResponse();

        boolean userDoesNotHaveRequiredStock = user.getAssets().stream().noneMatch(asset -> asset.stockId.equals(stock.getId()));
        boolean userDoesNotHaveEnoughStockQuantity = order.getQuantity() > user.getAssets().stream().filter(asset -> asset.stockId.equals(stock.getId())).findFirst().get().quantity;
        if (userDoesNotHaveRequiredStock || userDoesNotHaveEnoughStockQuantity) {
            validationResponse.addMessage("Order quantity should be smaller or equal to the amount of stock that user owns");
        }

        return validationResponse;
    }

    public ValidationResponse validateBuyOrder(Order order, User user, Stock stock) {
        ValidationResponse validationResponse = new ValidationResponse();

        BigDecimal totalPrice;
        if (order.getOrderType() == OrderType.MARKET) {
            totalPrice = stock.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        } else {
            totalPrice = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        }

        if (totalPrice.compareTo(user.getCashBalance()) > 0) {
            validationResponse.addMessage("Total price should be smaller or equal to user cash balance");
        }

        return validationResponse;
    }

    public boolean isMarketOpen() {
        LocalDateTime currentTime = LocalDateTime.now(clock);
        DayOfWeek dayOfWeek = currentTime.getDayOfWeek();
        LocalTime time = currentTime.toLocalTime();

        return dayOfWeek.getValue() >= 1 // Monday
                && dayOfWeek.getValue() <= 5 // Friday
                && time.isAfter(LocalTime.of(9, 0))
                && time.isBefore(LocalTime.of(17, 0));
    }

    public ValidationResponse addOrder(Order order) {
        var validationResponse = validateNewOrder(order);
        if (!validationResponse.isValid()) {
            return validationResponse;
        }

        String userId = order.getUserId();
        String stockId = order.getStockId();
        Optional<User> optionalUser = userRepository.findUserById(userId);
        Optional<Stock> optionalStock = stockRepository.findStockById(stockId);

        validationResponse = validateOrderBeforeExecuting(order, optionalUser, optionalStock);
        if (!validationResponse.isValid()) {
            return validationResponse;
        }

        boolean realised;
        if (isMarketOpen()) {
            realised = executeOrder(order, optionalUser, optionalStock);
        } else {
            realised = false;
        }

        if (!realised && order.getOrderType() != OrderType.MARKET) {
            orderRepository.save(order);
        }

        return new ValidationResponse(new ArrayList<>());
    }

    public boolean executeOrder(Order order, Optional<User> optionalUser, Optional<Stock> optionalStock) {
        User user = optionalUser.get();
        Stock stock = optionalStock.get();

        if (order.getOrderSide() == OrderSide.SELL) {
            return executeSellOrder(order, user, stock);
        }
        return executeBuyOrder(order, user, stock);
    }

    public boolean executeSellOrder(Order order, User user, Stock stock) {
        BigDecimal totalPrice;
        if (order.getOrderType() == OrderType.MARKET) {
            totalPrice = stock.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        } else {
            if (stock.getPrice().compareTo(order.getPrice()) < 0) {
                return false;
            }
            totalPrice = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        }

        BigDecimal newBalance = user.getCashBalance().add(totalPrice);
        user.setCashBalance(newBalance);

        var userAssets = user.getAssets();
        Optional<Asset> optionalAsset = userAssets.stream()
                .filter(asset -> asset.stockId.equals(stock.getId()))
                .findFirst();

        if (optionalAsset.isEmpty()) {
            throw new RuntimeException("User does not have required asset");
        }

        Asset asset = optionalAsset.get();
        asset.quantity -= order.getQuantity();

        if (asset.quantity < 0) {
            throw new RuntimeException("Negative asset quantity");
        }

        userAssets.removeIf(a -> a.quantity == 0);

        return true;
    }

    public boolean executeBuyOrder(Order order, User user, Stock stock) {
        BigDecimal totalPrice;
        if (order.getOrderType() == OrderType.MARKET) {
            totalPrice = stock.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        } else {
            if (stock.getPrice().compareTo(order.getPrice()) > 0) {
                return false;
            }
            totalPrice = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
        }

        BigDecimal newBalance = user.getCashBalance().subtract(totalPrice);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Negative user cash balance");
        }
        user.setCashBalance(newBalance);

        var userAssets = user.getAssets();
        Optional<Asset> optionalAsset = userAssets.stream()
                .filter(asset -> asset.stockId.equals(stock.getId()))
                .findFirst();

        if (optionalAsset.isPresent()) {
            Asset asset = optionalAsset.get();
            asset.quantity += order.getQuantity();
        } else {
            userAssets.add(new Asset(stock.getId(), order.getQuantity()));
        }

        return true;
    }

    public void matchOrders() {
        var orders = orderRepository.findAll();

        List<Order> toRemove = new ArrayList<>();
        for (var order : orders) {
            if (order.getOrderType() == OrderType.TIME_LIMIT
                    && order.getCancellationTime().isAfter(LocalDateTime.now(clock))) {
                toRemove.add(order);
                continue;
            }

            String userId = order.getUserId();
            String stockId = order.getStockId();
            Optional<User> optionalUser = userRepository.findUserById(userId);
            Optional<Stock> optionalStock = stockRepository.findStockById(stockId);

            var validationResponse = validateOrderBeforeExecuting(order, optionalUser, optionalStock);
            if (!validationResponse.isValid()) {
                continue;
            }

            boolean isExecuted = executeOrder(order, optionalUser, optionalStock);
            if (isExecuted) {
                toRemove.add(order);
            }
        }

        orders.removeAll(toRemove);
        orderRepository.saveAll(orders);
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
