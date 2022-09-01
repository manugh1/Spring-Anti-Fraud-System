package io.github.dankoller.antifraud.persistence;

import io.github.dankoller.antifraud.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repository for user entities
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    Optional<User> findUserByUsername(String username);

    List<User> findAll();
}
