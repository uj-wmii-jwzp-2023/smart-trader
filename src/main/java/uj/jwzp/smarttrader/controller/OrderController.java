package uj.jwzp.smarttrader.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uj.jwzp.smarttrader.model.Order;
import uj.jwzp.smarttrader.service.OrderService;

import java.util.List;
import java.util.Optional;

// TODO need to verify if queries are coming from logged username
@RequestMapping(path = "api/v1/users/{username}/orders", produces = "application/json")
@RestController
public class OrderController {
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    private final OrderService orderService;

    @GetMapping
    public List<Order> getAllOrders(@PathVariable("username") String username) {
        return orderService.getOrdersByUserId(username);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable("id") String id, @PathVariable("username") String username) {
        Optional<Order> optionalOrder = orderService.getOrderById(id);
        return optionalOrder
                .map(person -> new ResponseEntity<>(person, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }
    @PostMapping(value = "/market", consumes = "application/json")
    public ResponseEntity<String> addMarketOrder(@Validated(Order.Market.class) @RequestBody Order order) {
        orderService.addOrder(order);

        return new ResponseEntity<>("Order created", HttpStatus.CREATED);
    }
    @PostMapping(value = "/limit", consumes = "application/json")
    public ResponseEntity<String> addLimitOrder(@Validated(Order.Limit.class) @RequestBody Order order) {
        orderService.addOrder(order);

        return new ResponseEntity<>("Order created", HttpStatus.CREATED);
    }
    @PostMapping(value = "/time-limit", consumes = "application/json")
    public ResponseEntity<String> addTimeLimitOrder(@Validated(Order.TimeLimit.class) @RequestBody Order order) {
        orderService.addOrder(order);

        return new ResponseEntity<>("Order created", HttpStatus.CREATED);
    }

//    public String getLoggedInUserId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
//            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//            return userDetails.getId(); // Assuming your UserDetailsService returns a custom UserDetails implementation with an 'id' field
//        }
//
//        return null; // User not authenticated or principal is not a UserDetails instance
//    }

}