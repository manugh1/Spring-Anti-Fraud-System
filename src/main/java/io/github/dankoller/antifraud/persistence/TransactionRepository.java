package io.github.dankoller.antifraud.persistence;

import io.github.dankoller.antifraud.entity.transaction.Transaction;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    List<Transaction> findAllByDateBetweenAndNumber(LocalDateTime start, LocalDateTime end, String number);

    List<Transaction> findAllByNumber(String number);
}
