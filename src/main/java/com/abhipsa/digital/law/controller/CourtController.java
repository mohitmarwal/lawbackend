package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.Court;
import com.abhipsa.digital.law.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService service;

    @PostMapping
    public Court create(@RequestBody Court court) {
        return service.create(court);
    }

    @GetMapping
    public List<Court> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Court getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Court update(
            @PathVariable String id,
            @RequestBody Court court) {

        return service.update(id, court);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/name/{name}")
    public Court findByName(
            @PathVariable String name) {

        return service.findByName(name);
    }

    @GetMapping("/search/{name}")
    public List<Court> searchByName(
            @PathVariable String name) {

        return service.searchByName(name);
    }

    @GetMapping("/location/{location}")
    public List<Court> findByLocation(
            @PathVariable String location) {

        return service.findByLocation(location);
    }

    @GetMapping("/type/{type}")
    public List<Court> findByType(
            @PathVariable String type) {

        return service.findByType(type);
    }

    @GetMapping("/count/type/{type}")
    public long countByType(
            @PathVariable String type) {

        return service.countByType(type);
    }

    // ==================================================================
    // ---- Pagination support (added; existing endpoints above unchanged) ----
    // Example usage: GET /api/courts/paged?page=0&size=20&sort=name,asc
    // ==================================================================

    @GetMapping("/paged")
    public Page<Court> getAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        return service.getAllPaged(pageable);
    }

    @GetMapping("/search/{name}/paged")
    public Page<Court> searchByNamePaged(
            @PathVariable String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.searchByNamePaged(name, pageable);
    }

    @GetMapping("/location/{location}/paged")
    public Page<Court> findByLocationPaged(
            @PathVariable String location,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByLocationPaged(location, pageable);
    }

    @GetMapping("/type/{type}/paged")
    public Page<Court> findByTypePaged(
            @PathVariable String type,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByTypePaged(type, pageable);
    }
}