package com.ymastorak.maestros.persistence.repository;

import com.ymastorak.maestros.persistence.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findEventsByMemberId(int memberId);
}
