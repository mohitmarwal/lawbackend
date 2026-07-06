package com.abhipsa.digital.law.repository;

import com.abhipsa.digital.law.entity.Court;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourtRepository extends JpaRepository<Court, String> {

    Optional<Court> findByName(String name);

    List<Court> findByNameContainingIgnoreCase(String name);

    List<Court> findByLocation(String location);

    List<Court> findByLocationContainingIgnoreCase(String location);

    List<Court> findByType(String type);

    List<Court> findByNameContainingIgnoreCaseAndLocationContainingIgnoreCase(
            String name,
            String location);

    List<Court> findByTypeAndLocation(
            String type,
            String location);

    boolean existsByName(String name);

    long countByType(String type);

    long countByLocation(String location);

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // JpaRepository already provides Page<Court> findAll(Pageable)
    // for the unfiltered case, so only filtered paged queries are added here.
    // ==================================================================

    Page<Court> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Court> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    Page<Court> findByType(String type, Pageable pageable);
}