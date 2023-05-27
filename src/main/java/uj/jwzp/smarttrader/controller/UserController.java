package uj.jwzp.smarttrader.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
    private final UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return service.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        Optional<User> optionalPerson = service.getUserById(id);
        return optionalPerson
                .map(person -> new ResponseEntity<>(person, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void addUser(@RequestBody User user) {
        service.addUser(user);
    }
}
