package uj.jwzp.smarttrader.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uj.jwzp.smarttrader.dtomapper.OrderMapper;
import uj.jwzp.smarttrader.model.Order;
import uj.jwzp.smarttrader.dto.OrderDto;
import uj.jwzp.smarttrader.model.OrderType;
import uj.jwzp.smarttrader.service.OrderService;

import java.util.List;
import java.util.Optional;

@RequestMapping(path = "api/v1/users/{username}/orders", produces = "application/json")
@RestController
public class OrderController {
    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @GetMapping
    @PreAuthorize("#username == authentication.principal.username")
    public List<Order> getAllOrders(@PathVariable("username") String username) {
        return orderService.getOrdersByUserName(username);
    }

    @GetMapping("/{id}")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<Order> getOrder(@PathVariable("id") String id, @PathVariable("username") String username) {
        Optional<Order> optionalOrder = orderService.getOrderById(id);
        return optionalOrder
                .map(person -> new ResponseEntity<>(person, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @PostMapping(value = "/market", consumes = "application/json")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<String> addMarketOrder(@Validated(OrderDto.Market.class) @RequestBody OrderDto orderDto,
                                                 @PathVariable("username") String username) {
        orderDto.setUsername(username);
        orderDto.setOrderType(OrderType.MARKET);
        orderService.addOrder(orderMapper.toEntity(orderDto));

        return new ResponseEntity<>("Order created", HttpStatus.CREATED);
    }

    @PostMapping(value = "/limit", consumes = "application/json")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<String> addLimitOrder(@Validated(OrderDto.Limit.class) @RequestBody OrderDto orderDto,
                                                @PathVariable("username") String username) {
        orderDto.setUsername(username);
        orderDto.setOrderType(OrderType.LIMIT);
        orderService.addOrder(orderMapper.toEntity(orderDto));

        return new ResponseEntity<>("Order created", HttpStatus.CREATED);
    }

    @PostMapping(value = "/time-limit", consumes = "application/json")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<String> addTimeLimitOrder(@Validated(OrderDto.TimeLimit.class) @RequestBody OrderDto orderDto,
                                                    @PathVariable("username") String username) {
        orderDto.setUsername(username);
        orderDto.setOrderType(OrderType.TIME_LIMIT);
        orderService.addOrder(orderMapper.toEntity(orderDto));

        return new ResponseEntity<>("Order created", HttpStatus.CREATED);
    }
}