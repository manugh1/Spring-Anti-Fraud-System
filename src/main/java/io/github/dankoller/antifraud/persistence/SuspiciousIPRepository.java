package io.github.dankoller.antifraud.persistence;

import io.github.dankoller.antifraud.entity.IPAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repository for suspicious IP entities
@Repository
public interface SuspiciousIPRepository extends JpaRepository<IPAddress, Long> {
    Optional<IPAddress> findByIp(String ip);
}
