package com.echoowl.backend.repository;

import com.echoowl.backend.model.Quota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuotaRepository extends JpaRepository<Quota, String> {
    Optional<Quota> findByUserIdAndYearAndMonth(String userId, int year, int month);

    Optional<Quota> findByUserId(String userId);
}
