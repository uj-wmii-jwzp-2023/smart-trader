package uj.jwzp.smarttrader.service;


import org.springframework.stereotype.Service;
import uj.jwzp.smarttrader.model.User;
import uj.jwzp.smarttrader.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addUser(User user) {
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByName(String name) {
        return userRepository.findUserByName(name);
    }

    public Boolean existsByName(String name) { return userRepository.existsByName(name); }

    public void depositFunds(String username, BigDecimal value) {
        User user = getUserByName(username).orElseThrow();
        user.setCashBalance(user.getCashBalance().add(value));
        userRepository.save(user);
    }

    public void withdrawFunds(String username, BigDecimal value) {
        User user = getUserByName(username).orElseThrow();
        user.setCashBalance(user.getCashBalance().subtract(value));
        userRepository.save(user);
    }
}