package uj.jwzp.smarttrader.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uj.jwzp.smarttrader.model.Role;
import uj.jwzp.smarttrader.model.User;
import uj.jwzp.smarttrader.service.UserService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers=UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User dummyUser;

    @BeforeEach
    public void setup() {
        List<Role> roles = List.of(Role.USER);
        String username = "user";
        String password = "password";

        dummyUser = new User(username, password, roles);
    }

    @Test
    public void GetUser_Should_ReturnOk_When_Exists() throws Exception {
        String dummyName = "name";
        dummyUser.setName(dummyName);

        given(userService.getUserByName(dummyName)).willReturn(Optional.of(dummyUser));

        String url = String.format("/api/v1/users/%s", dummyName);
        String body = mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        User returnedUser = objectMapper.readValue(body, User.class);

        Assertions.assertThat(returnedUser).usingRecursiveComparison().isEqualTo(dummyUser);
    }

    @Test
    public void GetUser_Should_ReturnNotFound_When_DontExists() throws Exception {
        String existingName = "existing-name";
        String notExistingName = "not-existing-name";
        dummyUser.setName(existingName);

        given(userService.getUserByName(existingName)).willReturn(Optional.of(dummyUser));

        String url = String.format("/api/v1/users/%s", notExistingName);
        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpect(status().isNotFound());
    }
    @Test
    public void DepositFunds_Should_ReturnAccepted_When_CorrectDepositValue() throws Exception {
        BigDecimal depositValue = new BigDecimal(10);

        given(userService.existsByName(dummyUser.getName())).willReturn(Boolean.TRUE);

        String url = String.format("/api/v1/users/%s/deposit?value=%s", dummyUser.getName(), depositValue);
        mockMvc.perform(MockMvcRequestBuilders.post(url))
                .andExpect(status().isAccepted());
    }
    @Test
    public void DepositFunds_Should_ReturnBadRequest_When_NegativeDepositValue() throws Exception {
        BigDecimal depositValue = new BigDecimal(-10);

        given(userService.existsByName(dummyUser.getName())).willReturn(Boolean.TRUE);

        String url = String.format("/api/v1/users/%s/deposit?value=%s", dummyUser.getName(), depositValue);
        mockMvc.perform(MockMvcRequestBuilders.post(url))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void DepositFunds_Should_ReturnNotFound_When_UserDontExist() throws Exception {
        BigDecimal depositValue = new BigDecimal(10);
        String notExistingName = "not-existing-name";

        given(userService.existsByName(notExistingName)).willReturn(Boolean.FALSE);

        String url = String.format("/api/v1/users/%s/deposit?value=%s", notExistingName, depositValue);
        mockMvc.perform(MockMvcRequestBuilders.post(url))
                .andExpect(status().isNotFound());
    }
    @Test
    public void WithdrawFunds_Should_ReturnAccepted_When_CorrectWithdrawValue() throws Exception {
        BigDecimal withdrawValue = new BigDecimal(10);
        dummyUser.setCashBalance(new BigDecimal(20));

        given(userService.existsByName(dummyUser.getName())).willReturn(Boolean.TRUE);
        given(userService.getUserByName(dummyUser.getName())).willReturn(Optional.of(dummyUser));

        String url = String.format("/api/v1/users/%s/withdraw?value=%s", dummyUser.getName(), withdrawValue);
        mockMvc.perform(MockMvcRequestBuilders.post(url))
                .andExpect(status().isAccepted());
    }
    @Test
    public void WithdrawFunds_Should_ReturnBadRequest_When_NotEnoughFunds() throws Exception {
        BigDecimal withdrawValue = new BigDecimal(10);
        dummyUser.setCashBalance(new BigDecimal(5));

        given(userService.existsByName(dummyUser.getName())).willReturn(Boolean.TRUE);
        given(userService.getUserByName(dummyUser.getName())).willReturn(Optional.of(dummyUser));

        String url = String.format("/api/v1/users/%s/withdraw?value=%s", dummyUser.getName(), withdrawValue);
        mockMvc.perform(MockMvcRequestBuilders.post(url))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void WithdrawFunds_Should_ReturnBadRequest_When_NegativeWithdrawValue() throws Exception {
        BigDecimal withdrawValue = new BigDecimal(-10);
        dummyUser.setCashBalance(new BigDecimal(100));

        given(userService.existsByName(dummyUser.getName())).willReturn(Boolean.TRUE);
        given(userService.getUserByName(dummyUser.getName())).willReturn(Optional.of(dummyUser));

        given(userService.existsByName(dummyUser.getName())).willReturn(Boolean.TRUE);

        String url = String.format("/api/v1/users/%s/withdraw?value=%s", dummyUser.getName(), withdrawValue);
        mockMvc.perform(MockMvcRequestBuilders.post(url))
                .andExpect(status().isBadRequest());
    }
    @Test
    public void WithdrawFunds_Should_ReturnNotFound_When_UserDontExist() throws Exception {
        BigDecimal withdrawValue = new BigDecimal(10);
        dummyUser.setCashBalance(new BigDecimal(20));
        String notExistingName = "not-existing-name";

        given(userService.getUserByName(dummyUser.getName())).willReturn(Optional.of(dummyUser));

        given(userService.existsByName(notExistingName)).willReturn(Boolean.FALSE);

        String url = String.format("/api/v1/users/%s/withdraw?value=%s", notExistingName, withdrawValue);
        mockMvc.perform(MockMvcRequestBuilders.post(url))
                .andExpect(status().isNotFound());
    }

}
