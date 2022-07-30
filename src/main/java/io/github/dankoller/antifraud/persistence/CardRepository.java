package io.github.dankoller.antifraud.persistence;

import io.github.dankoller.antifraud.entity.Card;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repository for stolen card entities
@Repository
public interface CardRepository extends CrudRepository<Card, Long> {
    Optional<Card> findByNumber(String ip);

    List<Card> findAllByIsLockedTrue();

    boolean existsByNumberAndIsLockedTrue(String number);
}
