package uj.jwzp.smarttrader.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uj.jwzp.smarttrader.dto.OrderBookDto;
import uj.jwzp.smarttrader.dto.PatchOrderDto;
import uj.jwzp.smarttrader.model.*;
import uj.jwzp.smarttrader.repository.OrderRepository;
import uj.jwzp.smarttrader.repository.StockRepository;
import uj.jwzp.smarttrader.repository.UserRepository;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private Clock clock;
    private final LocalDateTime dateTime = LocalDateTime.of(2023, 6, 12, 12, 0); // during open market
    private final Instant instant = dateTime.atZone(ZoneId.of("CET")).toInstant();

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Order order;
    private Stock stock;

    @BeforeEach
    public void setup() {
        List<Role> roles = List.of(Role.USER);
        String username = "user";
        String password = "password";
        String userId = "userId";
        user = new User(username, password, roles);
        user.setId(userId);
        user.setCashBalance(new BigDecimal(30000));

        String stockId = "stockId";
        BigDecimal price = new BigDecimal(20);
        Integer quantity = 2;
        OrderSide orderSide = OrderSide.BUY;
        OrderType orderType = OrderType.LIMIT;
        LocalDateTime cancellationTime = LocalDateTime.MAX;
        String orderId = "orderId";
        order = new Order(userId, stockId, price, quantity, orderSide, orderType, cancellationTime);
        order.setId(orderId);


        String name = "Dummy Name";
        String ticker = "DUMMY";
        stock = new Stock(name, ticker);
        stock.setId(stockId);
        stock.setPrice(BigDecimal.valueOf(15));
    }

    @Test
    public void GetOrderById_Should_ReturnOrder_When_ValidId() {
        String userId = "1";
        String stockId = "2";
        BigDecimal price = new BigDecimal(10);
        Integer quantity = 10;
        OrderSide orderSide = OrderSide.BUY;
        OrderType orderType = OrderType.LIMIT;
        LocalDateTime cancellationTime = null;

        Order order = new Order(userId, stockId, price, quantity, orderSide, orderType, cancellationTime);
        String orderId = "0";
        order.setId(orderId);

        given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));

        Optional<Order> savedOrder = orderService.getOrderById(orderId);

        Assertions.assertThat(savedOrder).isNotEmpty().hasValue(order);
    }

    @Test
    public void ExecuteBuyOrder_Returns_False_When_Order_Not_Satisfied_And_Limit() {
        order.setPrice(BigDecimal.valueOf(15));
        stock.setPrice(BigDecimal.valueOf(20));

        boolean realised = orderService.executeBuyOrder(order, user, stock);

        Assertions.assertThat(realised).isFalse();
    }


    @Test
    public void ExecuteBuyOrder_Returns_True_And_Properly_Updates_User_If_Order_Satisfied() {
        BigDecimal expectedBalance = user.getCashBalance()
                .subtract(order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));

        boolean realised = orderService.executeBuyOrder(order, user, stock);

        Assertions.assertThat(realised).isTrue();
        Assertions.assertThat(user.getAssets())
                .usingRecursiveComparison()
                .isEqualTo(List.of(new Asset(stock.getId(), order.getQuantity())));
        Assertions.assertThat(user.getCashBalance()).isEqualTo(expectedBalance);
        verify(userRepository).save(user);
    }

    @Test
    public void ExecuteBuyOrder_Returns_True_And_Properly_Updates_User_If_Market_Order() {
        order.setOrderType(OrderType.MARKET);

        BigDecimal expectedBalance = user.getCashBalance()
                .subtract(stock.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));

        boolean realised = orderService.executeBuyOrder(order, user, stock);

        Assertions.assertThat(realised).isTrue();
        Assertions.assertThat(user.getAssets())
                .usingRecursiveComparison()
                .isEqualTo(List.of(new Asset(stock.getId(), order.getQuantity())));
        Assertions.assertThat(user.getCashBalance()).isEqualTo(expectedBalance);
        verify(userRepository).save(user);
    }

    @Test
    public void ExecuteSellOrder_Returns_False_When_Order_Not_Satisfied_And_Limit() {
        boolean realised = orderService.executeSellOrder(order, user, stock);
        Assertions.assertThat(realised).isFalse();
    }

    @Test
    public void ExecuteSellOrder_Returns_True_And_Properly_Updates_User_If_Order_Satisfied() {
        order.setPrice(BigDecimal.valueOf(15));
        stock.setPrice(BigDecimal.valueOf(20));

        int quantity = 20;
        user.setAssets(new ArrayList<>(Arrays.asList(new Asset(stock.getId(), quantity))));

        BigDecimal expectedBalance = user.getCashBalance()
                .add(order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));

        boolean realised = orderService.executeSellOrder(order, user, stock);

        Assertions.assertThat(realised).isTrue();
        Assertions.assertThat(user.getAssets())
                .usingRecursiveComparison()
                .isEqualTo(List.of(new Asset(stock.getId(), quantity - order.getQuantity())));
        Assertions.assertThat(user.getCashBalance()).isEqualTo(expectedBalance);
        verify(userRepository).save(user);
    }

    @Test
    public void ExecuteSellOrder_Returns_True_And_Properly_Updates_User_Assets_If_Order_Satisfied() {
        order.setPrice(BigDecimal.valueOf(15));
        stock.setPrice(BigDecimal.valueOf(20));
        order.setQuantity(2);

        user.setAssets(new ArrayList<>(Arrays.asList(new Asset(stock.getId(), 2))));

        BigDecimal expectedBalance = user.getCashBalance()
                .add(order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));

        boolean realised = orderService.executeSellOrder(order, user, stock);

        Assertions.assertThat(realised).isTrue();
        Assertions.assertThat(user.getAssets()).isEmpty();
        Assertions.assertThat(user.getCashBalance()).isEqualTo(expectedBalance);
        verify(userRepository).save(user);
    }

    @Test
    public void ExecuteSellOrder_Returns_True_And_Properly_Updates_User_If_Market() {
        order.setOrderType(OrderType.MARKET);
        user.setAssets(new ArrayList<>(Arrays.asList(new Asset(stock.getId(), 2))));

        BigDecimal expectedBalance = user.getCashBalance()
                .add(stock.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));

        boolean realised = orderService.executeSellOrder(order, user, stock);

        Assertions.assertThat(realised).isTrue();
        Assertions.assertThat(user.getAssets()).isEmpty();
        Assertions.assertThat(user.getCashBalance()).isEqualTo(expectedBalance);
        verify(userRepository).save(user);
    }

    @Test
    public void ValidateNewOrder_Should_Return_Errors_When_Invalid_Parameters() {
        order.setOrderType(OrderType.TIME_LIMIT);
        given(clock.instant()).willReturn(instant);
        given(clock.getZone()).willReturn(ZoneId.of("CET"));

        order.setPrice(BigDecimal.valueOf(-10));
        order.setQuantity(-4);
        LocalDateTime oldDate = LocalDateTime.of(2020, 6, 12, 3, 1);
        order.setCancellationTime(oldDate);

        var validationResponse = orderService.validateNewOrder(order);

        Assertions.assertThat(validationResponse.getMessages().size()).isEqualTo(3);
    }

    @Test
    public void ValidateNewOrder_Should_Return_Empty_List_When_Valid_Parameters() {
        order.setPrice(BigDecimal.valueOf(100));
        order.setQuantity(10);

        var validationResponse = orderService.validateNewOrder(order);

        Assertions.assertThat(validationResponse.isValid()).isTrue();
    }


    @Test
    public void ValidateSellOrder_Returns_Empty_List_When_Valid_Assets_Quantity() {
        order.setOrderSide(OrderSide.SELL);
        order.setQuantity(2);
        user.setAssets(new ArrayList<>(Arrays.asList(new Asset(stock.getId(), 10))));

        var validationResponse = orderService.validateSellOrder(order, user, stock);

        Assertions.assertThat(validationResponse.isValid()).isTrue();
    }

    @Test
    public void ValidateSellOrder_Returns_Errors_When_Invalid_Assets_Quantity() {
        order.setQuantity(20);
        user.setAssets(new ArrayList<>(Arrays.asList(new Asset(stock.getId(), 10))));

        var validationResponse = orderService.validateSellOrder(order, user, stock);

        Assertions.assertThat(validationResponse.getMessages().size()).isEqualTo(1);
    }

    @Test
    public void ValidateBuyOrder_Returns_Empty_List_When_Valid_Balance() {
        var validationResponse = orderService.validateBuyOrder(order, user, stock);
        Assertions.assertThat(validationResponse.isValid()).isTrue();
    }

    @Test
    public void ValidateBuyOrder_Returns_Errors_When_Invalid_Balance() {
        user.setCashBalance(BigDecimal.valueOf(10));
        order.setPrice(BigDecimal.valueOf(10));
        order.setQuantity(2);

        var validationResponse = orderService.validateBuyOrder(order, user, stock);
        Assertions.assertThat(validationResponse.isValid()).isFalse();
    }

    @Test
    public void ValidateBuyOrder_Returns_Empty_List_When_Valid_Balance_Market() {
        order.setOrderType(OrderType.MARKET);
        user.setCashBalance(BigDecimal.valueOf(10));
        stock.setPrice(BigDecimal.valueOf(10));
        order.setQuantity(1);
        var validationResponse = orderService.validateBuyOrder(order, user, stock);
        Assertions.assertThat(validationResponse.isValid()).isTrue();
    }


    @Test
    public void ValidateBuyOrder_Returns_Errors_When_Invalid_Balance_Market() {
        order.setOrderType(OrderType.MARKET);
        user.setCashBalance(BigDecimal.valueOf(10));
        stock.setPrice(BigDecimal.valueOf(10));
        order.setQuantity(2);

        var validationResponse = orderService.validateBuyOrder(order, user, stock);
        Assertions.assertThat(validationResponse.isValid()).isFalse();
    }


    @Test
    public void AddOrder_Does_Not_Save_Order_When_Realised() {
        given(clock.instant()).willReturn(instant);
        given(clock.getZone()).willReturn(ZoneId.of("CET"));
        given(userRepository.findUserById(user.getId())).willReturn(Optional.of(user));
        given(stockRepository.findStockById(stock.getId())).willReturn(Optional.of(stock));

        var validationResponse = orderService.addOrder(order);

        verify(orderRepository, never()).save(any(Order.class));
        verify(userRepository).save(any());
        Assertions.assertThat(validationResponse.isValid()).isTrue();
    }

    @Test
    public void AddOrder_Saves_Order_When_Added_Outside_Of_Trading_Widow() {
        LocalDateTime outsideDateTime = LocalDateTime.of(2023, 6, 12, 6, 0);
        Instant outsideInstant = outsideDateTime.atZone(ZoneId.of("CET")).toInstant();

        given(clock.instant()).willReturn(outsideInstant);
        given(clock.getZone()).willReturn(ZoneId.of("CET"));

        given(userRepository.findUserById(user.getId())).willReturn(Optional.of(user));
        given(stockRepository.findStockById(stock.getId())).willReturn(Optional.of(stock));

        var validationResponse = orderService.addOrder(order);

        verify(orderRepository).save(order);
        Assertions.assertThat(validationResponse.isValid()).isTrue();
    }

    @Test
    public void AddOrder_Saves_Order_When_Not_Realised() {
        given(clock.instant()).willReturn(instant);
        given(clock.getZone()).willReturn(ZoneId.of("CET"));

        order.setPrice(BigDecimal.valueOf(2));
        given(userRepository.findUserById(user.getId())).willReturn(Optional.of(user));
        given(stockRepository.findStockById(stock.getId())).willReturn(Optional.of(stock));

        var validationResponse = orderService.addOrder(order);

        verify(orderRepository).save(order);
        Assertions.assertThat(validationResponse.isValid()).isTrue();
    }

    @Test
    public void AddOrder_Never_Saves_Market_Order_When_Not_Realised() {
        given(clock.instant()).willReturn(instant);
        given(clock.getZone()).willReturn(ZoneId.of("CET"));

        order.setPrice(BigDecimal.valueOf(2));
        order.setOrderType(OrderType.MARKET);
        given(userRepository.findUserById(user.getId())).willReturn(Optional.of(user));
        given(stockRepository.findStockById(stock.getId())).willReturn(Optional.of(stock));

        var validationResponse = orderService.addOrder(order);

        verify(orderRepository, never()).save(any(Order.class));
        Assertions.assertThat(validationResponse.isValid()).isTrue();
    }

    @Test
    public void AddOrder_Does_Not_Save_Time_Limit_Order_With_Old_Date() {
        given(clock.instant()).willReturn(instant);
        given(clock.getZone()).willReturn(ZoneId.of("CET"));

        LocalDateTime oldDateTime = LocalDateTime.of(2022, 6, 12, 12, 0);
        order.setOrderType(OrderType.TIME_LIMIT);
        order.setCancellationTime(oldDateTime);
        order.setPrice(BigDecimal.valueOf(2));

        var validationResponse = orderService.addOrder(order);

        Assertions.assertThat(validationResponse.isValid()).isFalse();
    }

    @Test
    public void MatchOrders_Executes_Orders_When_Possible() {
        List<Order> orders = new ArrayList<>(Arrays.asList(order));

        given(userRepository.findUserById(user.getId())).willReturn(Optional.of(user));
        given(stockRepository.findStockById(stock.getId())).willReturn(Optional.of(stock));
        given(orderRepository.findAll()).willReturn(orders);

        BigDecimal expectedBalance = user.getCashBalance()
                .subtract(order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));

        orderService.matchOrders();
        verify(orderRepository).deleteAll(orders);


        Assertions.assertThat(user.getAssets())
                .usingRecursiveComparison()
                .isEqualTo(List.of(new Asset(stock.getId(), order.getQuantity())));
        Assertions.assertThat(user.getCashBalance()).isEqualTo(expectedBalance);
    }

    @Test
    public void UpdateOrder_Executes_Order_When_Valid_Parameters() {
        BigDecimal newPrice = BigDecimal.valueOf(100);
        Integer newQuantity = 3;
        OrderSide newOrderSide = null;
        LocalDateTime newCancellationTime = null;

        PatchOrderDto patchOrderDto = new PatchOrderDto(newPrice, newQuantity, newOrderSide, newCancellationTime);

        given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));
        given(userRepository.findUserById(user.getId())).willReturn(Optional.of(user));
        given(stockRepository.findStockById(stock.getId())).willReturn(Optional.of(stock));
        given(clock.instant()).willReturn(instant);
        given(clock.getZone()).willReturn(ZoneId.of("CET"));

        var validationResponse = orderService.updateOrder(order.getId(), patchOrderDto);

        verify(orderRepository, never()).save(any());
        verify(userRepository).save(user);
        Assertions.assertThat(validationResponse.isValid()).isTrue();
        Assertions.assertThat(order.getQuantity()).isEqualTo(3);
        Assertions.assertThat(order.getPrice()).isEqualTo(newPrice);
        Assertions.assertThat(order.getOrderSide()).isEqualTo(OrderSide.BUY);
    }

    @Test
    public void UpdateOrder_Does_Not_Save_Order_When_InValid_Parameters() {
        BigDecimal newPrice = BigDecimal.valueOf(-100);
        Integer newQuantity = 0;
        OrderSide newOrderSide = null;
        LocalDateTime newCancellationTime = null;

        PatchOrderDto patchOrderDto = new PatchOrderDto(newPrice, newQuantity, newOrderSide, newCancellationTime);

        given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));

        var validationResponse = orderService.updateOrder(order.getId(), patchOrderDto);

        verify(orderRepository, never()).save(order);
        verify(userRepository, never()).save(any());
        Assertions.assertThat(validationResponse.isValid()).isFalse();
    }

    @Test
    public void GetOrderBook_Returns_Sorted_Orders_And_Merges() {
        given(stockRepository.findStockByTicker(stock.getTicker())).willReturn(Optional.of(stock));
        List<Order> orders = new ArrayList<>(Arrays.asList(
                new Order(user.getId(), stock.getId(), BigDecimal.valueOf(100), 10, OrderSide.BUY, OrderType.TIME_LIMIT, order.getCancellationTime()),
                new Order(user.getId(), stock.getId(), BigDecimal.valueOf(200), 2, OrderSide.BUY, OrderType.LIMIT, order.getCancellationTime()),
                new Order(user.getId(), stock.getId(), BigDecimal.valueOf(130), 1, OrderSide.BUY, OrderType.LIMIT, order.getCancellationTime()),
                new Order(user.getId(), stock.getId(), BigDecimal.valueOf(100), 3, OrderSide.BUY, OrderType.TIME_LIMIT, order.getCancellationTime()),
                new Order(user.getId(), stock.getId(), BigDecimal.valueOf(100), 3, OrderSide.SELL, OrderType.TIME_LIMIT, order.getCancellationTime())
        ));

        given(orderRepository.findAllByStockId(stock.getId())).willReturn(orders);

        List<OrderBookDto> expected = new ArrayList<>(Arrays.asList(
                new OrderBookDto(BigDecimal.valueOf(100), 13, OrderSide.BUY),
                new OrderBookDto(BigDecimal.valueOf(100), 3, OrderSide.SELL),
                new OrderBookDto(BigDecimal.valueOf(130), 1, OrderSide.BUY),
                new OrderBookDto(BigDecimal.valueOf(200), 2, OrderSide.BUY)
        ));

        var res = orderService.getOrderBook(stock.getTicker());

        Assertions.assertThat(res.size()).isEqualTo(expected.size());

        for (int i = 0; i < expected.size(); i++) {
            Assertions.assertThat(res.get(i)).usingRecursiveComparison().isEqualTo(expected.get(i));
        }
    }

}
