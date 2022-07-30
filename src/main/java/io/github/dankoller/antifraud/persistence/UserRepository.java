package io.github.dankoller.antifraud.persistence;

import io.github.dankoller.antifraud.entity.user.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);

    Optional<User> findUserByUsername(String username);

    List<User> findAll();

    boolean existsByUsername(String username);
}
