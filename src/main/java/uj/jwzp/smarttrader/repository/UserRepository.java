package uj.jwzp.smarttrader.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uj.jwzp.smarttrader.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findUserByName(String name);
    boolean existsByName(String name);
}