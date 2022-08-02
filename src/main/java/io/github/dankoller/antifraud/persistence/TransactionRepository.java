package io.github.dankoller.antifraud.persistence;

import io.github.dankoller.antifraud.entity.transaction.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// Repository for transaction entities
@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    List<Transaction> findAllByDateBetweenAndNumber(LocalDateTime start, LocalDateTime end, String number);

    List<Transaction> findAllByNumber(String number);
}
