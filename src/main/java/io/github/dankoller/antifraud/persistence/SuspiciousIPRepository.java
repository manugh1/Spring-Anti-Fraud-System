package io.github.dankoller.antifraud.persistence;

import io.github.dankoller.antifraud.entity.IPAddress;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repository for suspicious IP entities
@Repository
public interface SuspiciousIPRepository extends CrudRepository<IPAddress, Long> {
    Optional<IPAddress> findByIp(String ip);
}
