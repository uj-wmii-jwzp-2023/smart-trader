package uj.jwzp.smarttrader.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uj.jwzp.smarttrader.model.Role;
import uj.jwzp.smarttrader.model.User;
import uj.jwzp.smarttrader.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByName(username).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getName(),
                user.getPassword(),
                rolesToAuthorities(user.getRoles()));
    }

    private Collection<GrantedAuthority> rolesToAuthorities(List<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }
}
