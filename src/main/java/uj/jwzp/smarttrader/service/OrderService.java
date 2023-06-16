package uj.jwzp.smarttrader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uj.jwzp.smarttrader.dto.OrderBookDto;
import uj.jwzp.smarttrader.dto.PatchOrderDto;
import uj.jwzp.smarttrader.model.*;
import uj.jwzp.smarttrader.repository.OrderRepository;
import uj.jwzp.smarttrader.repository.StockRepository;
import uj.jwzp.smarttrader.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final Clock clock;
    private static Logger logger = LoggerFactory.getLogger(OrderService.class);

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
            logger.trace("Negative order validation : non-positive value");
        }
        if (order.getQuantity() <= 0) {
            validationResponse.addMessage("Quantity should be a positive value.");
            logger.trace("Negative order validation : non-positive quantity");
        }
        if (order.getOrderType() == OrderType.TIME_LIMIT && order.getCancellationTime().isBefore(LocalDateTime.now(clock))) {
            validationResponse.addMessage("Cancellation time should be a future date.");
            logger.trace("Negative order validation : past cancellation date");
        }

        logger.trace("Order parameters validated successfully.");

        return validationResponse;
    }

    public ValidationResponse validateOrderBeforeExecuting(Order order, Optional<User> optionalUser, Optional<Stock> optionalStock) {
        ValidationResponse validationResponse = new ValidationResponse();

        if (optionalUser.isEmpty()) {
            validationResponse.addMessage("User does not exist.");
            logger.trace("Negative order validation : user not found");
        }
        if (optionalStock.isEmpty()) {
            validationResponse.addMessage("Invalid stock ticker.");
            logger.trace("Negative order validation : stock not found");
        }

        if (!validationResponse.isValid()) {
            return validationResponse;
        }

        User user = optionalUser.get();
        Stock stock = optionalStock.get();

        if (stock.getPrice() == null) {
            validationResponse.addMessage("Stock not available.");
            logger.error("Price for existing stock {} is not present.", stock.getId());

            return validationResponse;
        }

        if (order.getOrderSide() == OrderSide.SELL) {
            return validateSellOrder(order, user, stock);
        }
        return validateBuyOrder(order, user, stock);
    }

    public ValidationResponse validateSellOrder(Order order, User user, Stock stock) {
        ValidationResponse validationResponse = new ValidationResponse();

        boolean userDoesNotHaveRequiredStock = user.getAssets().stream().noneMatch(asset -> asset.stockId.equals(stock.getId()));
        boolean userDoesNotHaveEnoughStockQuantity = userDoesNotHaveRequiredStock ||
                order.getQuantity() > user
                        .getAssets()
                        .stream()
                        .filter(asset -> asset.stockId.equals(stock.getId())).findFirst().get().quantity;
        if (userDoesNotHaveEnoughStockQuantity) {
            validationResponse.addMessage("Order quantity should be smaller or equal to the amount of stock that user owns");
            logger.trace("Negative order validation : too large quantity");
        }
        logger.trace("Successful quantity validation for SELL order.");

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
            logger.trace("Negative order validation : not enough funds");
        }
        logger.trace("Successful price validation for BUY order.");

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
            logger.debug("Negative order validation, order won't be added.");
            return validationResponse;
        }

        String userId = order.getUserId();
        String stockId = order.getStockId();
        Optional<User> optionalUser = userRepository.findUserById(userId);
        Optional<Stock> optionalStock = stockRepository.findStockById(stockId);

        validationResponse = validateOrderBeforeExecuting(order, optionalUser, optionalStock);
        if (!validationResponse.isValid()) {
            logger.debug("Negative order validation, order won't be added.");
            return validationResponse;
        }

        logger.debug("Order positively validated.");

        boolean realised;
        if (isMarketOpen()) {
            logger.debug("Trying to execute order.");
            realised = executeOrder(order, optionalUser, optionalStock);
        } else {
            logger.debug("Order added outside market hours.");
            realised = false;
        }

        if (!realised && order.getOrderType() != OrderType.MARKET) {
            orderRepository.save(order);
            logger.debug("Order {} added to active orders.", order.getId());
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

        userRepository.save(user);

        logger.info("User {} sold {} x {} for total {}.", user.getName(), order.getQuantity(), stock.getTicker(), totalPrice);

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

        userRepository.save(user);

        logger.info("User {} bought {} x {} for total {}.", user.getName(), order.getQuantity(), stock.getTicker(), totalPrice);

        return true;
    }

    public void matchOrders() {
        logger.info("Start of active orders matching.");

        var orders = orderRepository.findAll();

        List<Order> toRemove = new ArrayList<>();
        for (var order : orders) {
            if (order.getOrderType() == OrderType.TIME_LIMIT
                    && order.getCancellationTime().isBefore(LocalDateTime.now(clock))) {
                toRemove.add(order);
                logger.debug("Order {} set to be removed after passing cancellation time.", order.getId());
                continue;
            }

            String userId = order.getUserId();
            String stockId = order.getStockId();
            Optional<User> optionalUser = userRepository.findUserById(userId);
            Optional<Stock> optionalStock = stockRepository.findStockById(stockId);

            var validationResponse = validateOrderBeforeExecuting(order, optionalUser, optionalStock);
            if (!validationResponse.isValid()) {
                toRemove.add(order);
                logger.debug("Order {} set to be removed after failed validation.", order.getId());
                continue;
            }

            boolean isExecuted = executeOrder(order, optionalUser, optionalStock);
            if (isExecuted) {
                toRemove.add(order);
                logger.trace("Order {} got executed.", order.getId());
            }
        }

        orderRepository.deleteAll(toRemove);
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

    public ValidationResponse updateOrder(String orderId, PatchOrderDto patchOrderDto) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        Order order = optionalOrder.get();

        if (patchOrderDto.getPrice() != null) {
            order.setPrice(patchOrderDto.getPrice());
            logger.debug("Order {} changed price to {}.", order.getId(), patchOrderDto.getPrice());
        }
        if (patchOrderDto.getQuantity() != null) {
            order.setQuantity(patchOrderDto.getQuantity());
            logger.debug("Order {} changed quantity to {}.", order.getId(), patchOrderDto.getQuantity());
        }
        if (patchOrderDto.getOrderSide() != null) {
            order.setOrderSide(patchOrderDto.getOrderSide());
            logger.debug("Order {} swapped side.", order.getId());
        }
        if (order.getOrderType() == OrderType.TIME_LIMIT && patchOrderDto.getCancellationTime() != null) {
            order.setCancellationTime(patchOrderDto.getCancellationTime());
            logger.debug("Order {} changed cancellation time to {}.", order.getId(), patchOrderDto.getCancellationTime());
        }

        logger.info("Order {} is updated.", orderId);

        return addOrder(order);
    }

    public boolean existsById(String stockId) {
        return orderRepository.existsById(stockId);
    }

    public void deleteOrder(String stockId) {
        orderRepository.deleteById(stockId);
    }

    public List<OrderBookDto> getOrderBook(String ticker) {
        logger.debug("Preparing order book for {}.", ticker);

        Optional<Stock> optionalStock = stockRepository.findStockByTicker(ticker);
        Stock stock = optionalStock.get();

        List<Order> orders = orderRepository.findAllByStockId(stock.getId());
        orders.sort(Comparator.comparing(Order::getPrice));

        List<OrderBookDto> orderBookDtos = new ArrayList<>();

        OrderBookDto prevElem = null;

        for (Order order : orders) {
            if (prevElem != null
                    && Objects.equals(order.getPrice(), prevElem.getPrice())
                    && order.getOrderSide() == prevElem.getOrderSide()) {
                prevElem.setQuantity(order.getQuantity() + prevElem.getQuantity());
            } else {
                OrderBookDto orderBookDto = new OrderBookDto(order.getPrice(), order.getQuantity(), order.getOrderSide());
                orderBookDtos.add(orderBookDto);
                prevElem = orderBookDto;
            }
        }

        return orderBookDtos;
    }
}
