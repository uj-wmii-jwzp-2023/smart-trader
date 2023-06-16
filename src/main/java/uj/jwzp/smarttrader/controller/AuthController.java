package uj.jwzp.smarttrader.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import uj.jwzp.smarttrader.dto.UserCredentialsDto;
import uj.jwzp.smarttrader.model.Role;
import uj.jwzp.smarttrader.model.User;
import uj.jwzp.smarttrader.repository.UserRepository;
import uj.jwzp.smarttrader.service.MarketRefreshService;

import java.util.List;

@RequestMapping("api/v1/auth")
@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserCredentialsDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        logger.info("User {} logging in.", loginDto.getUsername());
        return new ResponseEntity<>("User logged", HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserCredentialsDto registerDto) {
        if (userRepository.existsByName(registerDto.getUsername())) {
            logger.debug("Registration failed, username {} taken.", registerDto.getUsername());
            return new ResponseEntity<>("Username is taken", HttpStatus.BAD_REQUEST);
        }
        String username = registerDto.getUsername();
        String password = passwordEncoder.encode(registerDto.getPassword());
        List<Role> roles = List.of(Role.USER);

        User user = new User(username, password, roles);
        userRepository.save(user);

        logger.info("User {} registered.", registerDto.getUsername());
        return new ResponseEntity<>("User registered", HttpStatus.OK);
    }
}

