package uj.jwzp.smarttrader.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import uj.jwzp.smarttrader.dto.RegisterDto;
import uj.jwzp.smarttrader.model.Role;
import uj.jwzp.smarttrader.model.User;
import uj.jwzp.smarttrader.repository.RoleRepository;
import uj.jwzp.smarttrader.repository.UserRepository;

import java.util.List;

@RequestMapping("api/v1/auth")
@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        if(userRepository.existsByName(registerDto.getUsername())) {
            return new ResponseEntity<>("Username is taken", HttpStatus.BAD_REQUEST);
        }
        User user=new User();
        user.setName(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Role role=roleRepository.findByName("USER").get();
        user.setRoles(List.of(role));

        userRepository.save(user);
        return new ResponseEntity<>("User registered", HttpStatus.OK);
    }
}

