package uj.jwzp.smarttrader.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uj.jwzp.smarttrader.dto.UserCredentialsDto;
import uj.jwzp.smarttrader.model.User;
import uj.jwzp.smarttrader.service.UserService;

import java.math.BigDecimal;
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
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @GetMapping("/{username}")
    @PreAuthorize("#username == authentication.principal.username or hasAuthority('ADMIN')")
    public ResponseEntity<User> getUser(@PathVariable("username") String username) {
        Optional<User> optionalUser = userService.getUserByName(username);
        return optionalUser
                .map(person -> new ResponseEntity<>(person, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("#username == authentication.principal.username or hasAuthority('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable("username") String username) {
        boolean userExists = userService.existsByName(username);
        if (userExists) {
            userService.deleteUser(username);
            return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("User does not exist", HttpStatus.NOT_FOUND);
    }

    @PatchMapping("/{username}")
    @PreAuthorize("#username == authentication.principal.username or hasAuthority('ADMIN')")
    public ResponseEntity<String> updateUser(@PathVariable("username") String username, @RequestBody UserCredentialsDto userCredentials) {
        boolean userExists = userService.existsByName(username);
        if (userExists) {
            if (userCredentials.getUsername() != null && userService.existsByName(userCredentials.getUsername())) {
                return new ResponseEntity<>("Username is taken", HttpStatus.BAD_REQUEST);
            }

            userService.updateUser(username, userCredentials);
            return new ResponseEntity<>("User updated successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("User does not exist", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{username}/deposit")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<String> depositFunds(@PathVariable("username") String username,
                                               @RequestParam("value") BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0)
            return new ResponseEntity<>("Deposit value must be positive", HttpStatus.BAD_REQUEST);
        if (!userService.existsByName(username))
            return new ResponseEntity<>("Username does not exist", HttpStatus.NOT_FOUND);

        userService.depositFunds(username, value);

        return new ResponseEntity<>("Successful deposit of funds", HttpStatus.ACCEPTED);
    }

    @PostMapping("/{username}/withdraw")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<String> withdrawFunds(@PathVariable("username") String username,
                                                @RequestParam("value") BigDecimal value) {
        if (!userService.existsByName(username))
            return new ResponseEntity<>("Username does not exist", HttpStatus.NOT_FOUND);
        if (value.compareTo(BigDecimal.ZERO) <= 0)
            return new ResponseEntity<>("Withdraw value must be positive", HttpStatus.BAD_REQUEST);
        if (userService.getUserByName(username).get().getCashBalance().compareTo(value) < 0)
            return new ResponseEntity<>("No sufficient funds", HttpStatus.BAD_REQUEST);

        userService.withdrawFunds(username, value);

        return new ResponseEntity<>("Successful withdraw of funds", HttpStatus.ACCEPTED);
    }
}