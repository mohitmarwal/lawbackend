package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.User;
import com.abhipsa.digital.law.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public User create(@RequestBody User user) {
        return service.create(user);
    }

    @GetMapping
    public List<User> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public User update(
            @PathVariable String id,
            @RequestBody User user) {

        return service.update(id, user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/email/{email}")
    public User findByEmail(
            @PathVariable String email) {

        return service.findByEmail(email);
    }

    @GetMapping("/phone/{phone}")
    public User findByPhone(
            @PathVariable String phone) {

        return service.findByPhone(phone);
    }

    @GetMapping("/role/{role}")
    public List<User> findByRole(
            @PathVariable String role) {

        return service.findByRole(role);
    }

    @GetMapping("/name/{name}")
    public List<User> findByName(
            @PathVariable String name) {

        return service.findByName(name);
    }

    @GetMapping("/surname/{surname}")
    public List<User> findBySurname(
            @PathVariable String surname) {

        return service.findBySurname(surname);
    }

    @GetMapping("/enabled/{enabled}")
    public List<User> findByEnabled(
            @PathVariable boolean enabled) {

        return service.findByEnabled(enabled);
    }

    @GetMapping("/count/role/{role}")
    public long countByRole(
            @PathVariable String role) {

        return service.countByRole(role);
    }

    // ==================================================================
    // ---- Pagination support (added; existing endpoints above unchanged) ----
    // Example usage: GET /api/users/paged?page=0&size=20&sort=surname,asc
    // ==================================================================

    @GetMapping("/paged")
    public Page<User> getAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        return service.getAllPaged(pageable);
    }

    @GetMapping("/name/{name}/paged")
    public Page<User> findByNamePaged(
            @PathVariable String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByNamePaged(name, pageable);
    }

    @GetMapping("/surname/{surname}/paged")
    public Page<User> findBySurnamePaged(
            @PathVariable String surname,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findBySurnamePaged(surname, pageable);
    }

    @GetMapping("/role/{role}/paged")
    public Page<User> findByRolePaged(
            @PathVariable String role,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByRolePaged(role, pageable);
    }

    @GetMapping("/enabled/{enabled}/paged")
    public Page<User> findByEnabledPaged(
            @PathVariable boolean enabled,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByEnabledPaged(enabled, pageable);
    }
}