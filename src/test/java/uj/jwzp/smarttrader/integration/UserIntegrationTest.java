package uj.jwzp.smarttrader.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uj.jwzp.smarttrader.model.Role;
import uj.jwzp.smarttrader.model.User;
import uj.jwzp.smarttrader.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @WithMockUser(username = "integration-test-user")
    @Test
    public void User_Works_Through_All_Layers() throws Exception {
        String name = "integration-test-user";
        String password = passwordEncoder.encode("password");
        User user = new User(name, password, List.of(Role.USER));
        userRepository.save(user);


        // get
        String getUrl = String.format("/api/v1/users/%s", name);
        String body = mockMvc.perform(MockMvcRequestBuilders.get(getUrl))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        User returnedUser = objectMapper.readValue(body, User.class);
        Assertions.assertThat(returnedUser.getName()).isEqualTo(name);

        // deposit
        BigDecimal value = BigDecimal.valueOf(123);
        String depositUrl = String.format("/api/v1/users/%s/deposit?value=%s", name, value);
        mockMvc.perform(MockMvcRequestBuilders.post(depositUrl))
                .andExpect(status().isAccepted());
        Optional<User> optionalUser = userRepository.findUserByName(name);
        Assertions.assertThat(optionalUser.isPresent()).isTrue();
        Assertions.assertThat(optionalUser.get().getCashBalance()).isEqualTo(value);


        // delete
        String deleteUrl = String.format("/api/v1/users/%s", name);
        mockMvc.perform(MockMvcRequestBuilders.delete(deleteUrl))
                .andExpect(status().isOk());
        boolean existsByName = userRepository.existsByName(name);
        Assertions.assertThat(existsByName).isFalse();
    }

}
