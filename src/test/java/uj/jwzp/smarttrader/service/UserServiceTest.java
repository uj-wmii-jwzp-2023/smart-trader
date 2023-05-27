package uj.jwzp.smarttrader.service;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uj.jwzp.smarttrader.model.Role;
import uj.jwzp.smarttrader.model.User;
import uj.jwzp.smarttrader.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void GetUserById_Should_ReturnUser_When_ValidId() {
        List<Role> roles = List.of(Role.USER);
        String username = "user";
        String password = "password";
        String id = "0";

        User user = new User(username, password, roles);
        user.setId(id);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        Optional<User> savedUser= userService.getUserById(id);

        Assertions.assertThat(savedUser).isNotEmpty().hasValue(user);
    }

}
