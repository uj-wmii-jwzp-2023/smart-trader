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
}
