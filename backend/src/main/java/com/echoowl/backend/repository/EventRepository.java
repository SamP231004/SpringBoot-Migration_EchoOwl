package com.echoowl.backend.repository;

import com.echoowl.backend.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findByEventCategoryIdAndCreatedAtGreaterThanEqual(String categoryId, OffsetDateTime startDate);

    long countByEventCategoryNameAndEventCategoryUserIdAndCreatedAtGreaterThanEqual(
            String name,
            String userId,
            OffsetDateTime startDate
    );

    Page<Event> findByEventCategoryNameAndEventCategoryUserIdAndCreatedAtGreaterThanEqual(
            String name,
            String userId,
            OffsetDateTime startDate,
            Pageable pageable
    );

    @Query("select count(e) from Event e where e.eventCategory.id = :categoryId")
    long countForCategory(@Param("categoryId") String categoryId);
}
