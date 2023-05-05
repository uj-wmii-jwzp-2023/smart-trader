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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void GetUserById_Should_ReturnUser_When_ValidId() {
        Role role=new Role();
        role.setId(0L);
        role.setName("USER");

        User user=new User();
        user.setId(0L);
        user.setName("user");
        user.setPassword("password");
        user.setRoles(List.of(role));


        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Optional<User> savedUser= userService.getUserById(0L);

        Assertions.assertThat(savedUser).isNotEmpty().hasValue(user);
    }

}
