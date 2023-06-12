package uj.jwzp.smarttrader.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uj.jwzp.smarttrader.dto.OrderDto;
import uj.jwzp.smarttrader.dtomapper.OrderMapper;
import uj.jwzp.smarttrader.model.Order;
import uj.jwzp.smarttrader.model.OrderSide;
import uj.jwzp.smarttrader.model.OrderType;
import uj.jwzp.smarttrader.model.ValidationResponse;
import uj.jwzp.smarttrader.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderMapper orderMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Order dummyOrder;
    private OrderDto dummyOrderDto;


    @BeforeEach
    public void setup() {
        String userId = "1";
        String stockId = "2";
        BigDecimal price = new BigDecimal(5);
        Integer quantity = 10;
        OrderSide orderSide = OrderSide.BUY;
        OrderType orderType = null;
        LocalDateTime cancellationTime = LocalDateTime.MAX;

        dummyOrder = new Order(userId, stockId, price, quantity, orderSide, orderType, cancellationTime);

        String ticker = "TICKER";
        String username = null;

        dummyOrderDto = new OrderDto(username, ticker, price, quantity, orderSide, orderType, cancellationTime);
    }

    @Test
    public void GetOrder_Should_ReturnOk_When_Exists() throws Exception {
        String dummyId = "0";
        String dummyName = "Username";
        dummyOrder.setId(dummyId);

        given(orderService.getOrderById(dummyId)).willReturn(Optional.of(dummyOrder));

        String url = String.format("/api/v1/users/%s/orders/%s", dummyName, dummyId);
        String body = mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Order returnedOrder = objectMapper.readValue(body, Order.class);

        Assertions.assertThat(returnedOrder).usingRecursiveComparison().isEqualTo(dummyOrder);
    }

    @Test
    public void GetOrder_Should_ReturnNotFound_When_DontExists() throws Exception {
        String existingId = "0";
        String notExistingId = "1";
        String dummyName = "Username";
        dummyOrder.setId(existingId);

        given(orderService.getOrderById(existingId)).willReturn(Optional.of(dummyOrder));

        String url = String.format("/api/v1/users/%s/orders/%s", dummyName, notExistingId);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isNotFound());

    }

    @Test
    public void AddMarketOrder_Should_ReturnCreated_When_ValidRequest() throws Exception {
        dummyOrderDto.setPrice(null);
        dummyOrderDto.setCancellationTime(null);

        expectThatPassedOrderDtoArgsAreValid(dummyOrderDto, dummyOrder, "market");
    }

    @Test
    public void AddLimitOrder_Should_ReturnCreated_When_ValidRequest() throws Exception {
        dummyOrderDto.setCancellationTime(null);

        expectThatPassedOrderDtoArgsAreValid(dummyOrderDto, dummyOrder, "limit");
    }

    @Test
    public void AddTimeLimitOrder_Should_ReturnCreated_When_ValidRequest() throws Exception {
        expectThatPassedOrderDtoArgsAreValid(dummyOrderDto, dummyOrder, "time-limit");
    }

    @Test
    public void AddMarketOrder_Should_ReturnBadRequest_When_MissingTicker() throws Exception {
        dummyOrderDto.setPrice(null);
        dummyOrderDto.setCancellationTime(null);

        dummyOrderDto.setTicker(null);
        expectThatPassedOrderDtoArgsAreNotValid(dummyOrderDto, dummyOrder, "market");
    }

    @Test
    public void AddMarketOrder_Should_ReturnBadRequest_When_MissingQuantity() throws Exception {
        dummyOrderDto.setPrice(null);
        dummyOrderDto.setCancellationTime(null);

        dummyOrderDto.setQuantity(null);
        expectThatPassedOrderDtoArgsAreNotValid(dummyOrderDto, dummyOrder, "market");
    }

    @Test
    public void AddMarketOrder_Should_ReturnBadRequest_When_MissingOrderSide() throws Exception {
        dummyOrderDto.setPrice(null);
        dummyOrderDto.setCancellationTime(null);

        dummyOrderDto.setOrderSide(null);
        expectThatPassedOrderDtoArgsAreNotValid(dummyOrderDto, dummyOrder, "market");
    }

    @Test
    public void AddMarketOrder_Should_ReturnBadRequest_When_ProvidedPrice() throws Exception {
        dummyOrderDto.setCancellationTime(null);

        expectThatPassedOrderDtoArgsAreNotValid(dummyOrderDto, dummyOrder, "market");
    }

    @Test
    public void AddMarketOrder_Should_ReturnBadRequest_When_ProvidedCancellationTime() throws Exception {
        dummyOrderDto.setPrice(null);

        expectThatPassedOrderDtoArgsAreNotValid(dummyOrderDto, dummyOrder, "market");
    }

    @Test
    public void AddLimitOrder_Should_ReturnBadRequest_When_MissingPrice() throws Exception {
        dummyOrderDto.setCancellationTime(null);

        dummyOrderDto.setPrice(null);
        expectThatPassedOrderDtoArgsAreNotValid(dummyOrderDto, dummyOrder, "limit");
    }

    @Test
    public void AddLimitOrder_Should_ReturnBadRequest_When_ProvidedCancellationTime() throws Exception {
        expectThatPassedOrderDtoArgsAreNotValid(dummyOrderDto, dummyOrder, "limit");
    }

    @Test
    public void AddTimeLimitOrder_Should_ReturnBadRequest_When_MissingCancellationTime() throws Exception {
        dummyOrderDto.setCancellationTime(null);
        expectThatPassedOrderDtoArgsAreNotValid(dummyOrderDto, dummyOrder, "time-limit");
    }

    @Test
    public void AddTimeLimitOrder_Should_ReturnBadRequest_When_MissingPrice() throws Exception {
        dummyOrderDto.setPrice(null);
        expectThatPassedOrderDtoArgsAreNotValid(dummyOrderDto, dummyOrder, "time-limit");
    }

    private void expectThatPassedOrderDtoArgsAreValid(OrderDto testedOrderDto, Order testedOrder, String orderType) throws Exception {
        String dummyName = "Username";
        testedOrderDto.setUsername(null);
        String requestBody = objectMapper.writeValueAsString(testedOrderDto);

        testedOrderDto.setUsername(dummyName);
        given(orderMapper.toEntity(refEq(testedOrderDto, "orderType"))).willReturn(testedOrder);
        given(orderService.addOrder(any())).willReturn(new ValidationResponse(new ArrayList<>()));

        String url = String.format("/api/v1/users/%s/orders/%s", dummyName, orderType);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated());

        verify(orderService).addOrder(any(Order.class));
    }

    private void expectThatPassedOrderDtoArgsAreNotValid(OrderDto testedOrderDto, Order testedOrder, String orderType) throws Exception {
        String requestBody = objectMapper.writeValueAsString(testedOrderDto);

        String dummyName = "Username";
        testedOrderDto.setUsername(dummyName);
        given(orderMapper.toEntity(refEq(testedOrderDto, "orderType"))).willReturn(testedOrder);

        String url = String.format("/api/v1/users/%s/orders/%s", dummyName, orderType);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verify(orderService, never()).addOrder(any(Order.class));
    }
}
