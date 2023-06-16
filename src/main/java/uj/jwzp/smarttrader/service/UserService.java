package uj.jwzp.smarttrader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uj.jwzp.smarttrader.dto.UserCredentialsDto;
import uj.jwzp.smarttrader.model.User;
import uj.jwzp.smarttrader.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void addUser(User user) {
        userRepository.save(user);
    }

    public List<User> getAllUsers() { return userRepository.findAll(); }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByName(String name) {
        return userRepository.findUserByName(name);
    }

    public Boolean existsByName(String name) {
        return userRepository.existsByName(name);
    }

    public void depositFunds(String username, BigDecimal value) {
        User user = getUserByName(username).orElseThrow();
        user.setCashBalance(user.getCashBalance().add(value));
        userRepository.save(user);

        logger.info("User {} deposited {} to account.", username, value);
    }

    public void withdrawFunds(String username, BigDecimal value) {
        User user = getUserByName(username).orElseThrow();
        user.setCashBalance(user.getCashBalance().subtract(value));
        userRepository.save(user);

        logger.info("User {} withdrawn {} from account.", username, value);
    }

    public void deleteUser(String username) {
        userRepository.deleteByName(username);

        logger.info("Account of user {} got closed.", username);
    }

    public void updateUser(String username, UserCredentialsDto patchUser) {
        Optional<User> optionalUser = userRepository.findUserByName(username);
        User user = optionalUser.get();

        boolean changed = false;
        if (patchUser.getUsername() != null) {
            changed = true;
            String newName = patchUser.getUsername();
            user.setName(newName);

            logger.info("User {} changed name to {}.", username, newName);
        }
        if (patchUser.getPassword() != null) {
            changed = true;
            String encodedPassword = passwordEncoder.encode(patchUser.getPassword());
            user.setPassword(encodedPassword);

            logger.info("User {} changed password.", username);
        }

        if (changed) {
            userRepository.save(user);
            var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
            var authenticationToken = new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword(), authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

    }
}