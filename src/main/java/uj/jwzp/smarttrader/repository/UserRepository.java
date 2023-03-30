package uj.jwzp.smarttrader.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uj.jwzp.smarttrader.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
