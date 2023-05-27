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

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") String id) {
        Optional<User> optionalUser = userService.getUserById(id);
        return optionalUser
                .map(person -> new ResponseEntity<>(person, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void addUser(@RequestBody User user) {
        userService.addUser(user);
    }
}