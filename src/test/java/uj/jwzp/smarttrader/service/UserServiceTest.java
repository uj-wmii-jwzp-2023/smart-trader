package uj.jwzp.smarttrader.service;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import uj.jwzp.smarttrader.dto.UserCredentialsDto;
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

    private User user;

    @BeforeEach
    public void setup() {
        List<Role> roles = List.of(Role.USER);
        String username = "user";
        String password = "password";
        String id = "0";

        user = new User(username, password, roles);
        user.setId(id);
    }

    @Test
    public void GetUserById_Should_Return_User_When_Valid_Id() {
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        Optional<User> savedUser = userService.getUserById(user.getId());

        Assertions.assertThat(savedUser).isNotEmpty().hasValue(user);
    }

    @Test
    public void UpdateUser_Should_Not_Update_User_When_Null_Fields() {
        given(userRepository.findUserByName(user.getName())).willReturn(Optional.of(user));
        String newName = null;
        String newPassword = null;
        UserCredentialsDto dto = new UserCredentialsDto(newName, newPassword);

        userService.updateUser(user.getName(), dto);

        Assertions.assertThat(user.getName()).isNotNull();
        Assertions.assertThat(user.getPassword()).isNotNull();
    }

}
