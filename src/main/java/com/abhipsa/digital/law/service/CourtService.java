package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.Court;
import com.abhipsa.digital.law.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtService {

    private final CourtRepository repository;

    public Court create(Court court) {
        return repository.save(court);
    }

    public List<Court> getAll() {
        return repository.findAll();
    }

    public Court getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Court not found"));
    }

    public Court update(String id, Court court) {

        Court existing = getById(id);

        existing.setName(court.getName());
        existing.setLocation(court.getLocation());
        existing.setType(court.getType());

        return repository.save(existing);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public Court findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Court not found"));
    }

    public List<Court> findByLocation(String location) {
        return repository.findByLocationContainingIgnoreCase(location);
    }

    public List<Court> findByType(String type) {
        return repository.findByType(type);
    }

    public List<Court> searchByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    public long countByType(String type) {
        return repository.countByType(type);
    }

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // ==================================================================

    public Page<Court> getAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Court> findByLocationPaged(String location, Pageable pageable) {
        return repository.findByLocationContainingIgnoreCase(location, pageable);
    }

    public Page<Court> findByTypePaged(String type, Pageable pageable) {
        return repository.findByType(type, pageable);
    }

    public Page<Court> searchByNamePaged(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }
}