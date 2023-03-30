package uj.jwzp.smarttrader.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import uj.jwzp.smarttrader.model.User;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final JpaRepository<User, Long> repository;

    @Autowired
    public UserService(JpaRepository<User, Long> repository) {
        this.repository = repository;
    }

    public void addUser(User user) {
        repository.save(user);
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return repository.findById(id);
    }
}
