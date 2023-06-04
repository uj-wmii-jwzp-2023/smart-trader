package uj.jwzp.smarttrader.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uj.jwzp.smarttrader.model.User;
import uj.jwzp.smarttrader.service.UserService;

import java.util.List;
import java.util.Optional;

@RequestMapping(path = "api/v1/users", produces = "application/json")
@RestController
public class UserController {
    public UserController(UserService userService) {
        this.userService = userService;
    }
    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable("username") String username) {
        Optional<User> optionalUser = userService.getUserByName(username);
        return optionalUser
                .map(person -> new ResponseEntity<>(person, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }
}