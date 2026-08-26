package com.recoverai.backend.repository;

import com.recoverai.backend.entity.RecoveryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecoveryEventRepository
        extends JpaRepository<RecoveryEvent, Long> {

    Optional<RecoveryEvent> findByEventId(String eventId);

}