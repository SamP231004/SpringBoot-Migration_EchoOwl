package com.echoowl.backend.repository;

import com.echoowl.backend.model.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventCategoryRepository extends JpaRepository<EventCategory, String> {
    List<EventCategory> findByUserIdOrderByUpdatedAtDesc(String userId);

    Optional<EventCategory> findByNameAndUserId(String name, String userId);

    long countByUserId(String userId);

    void deleteByNameAndUserId(String name, String userId);

    boolean existsByNameAndUserId(String name, String userId);
}
