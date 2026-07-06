package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.User;
import com.abhipsa.digital.law.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User create(User user) {
        return repository.save(user);
    }

    public List<User> getAll() {
        return repository.findAll();
    }

    public User getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User update(String id, User user) {

        User existing = getById(id);

        existing.setName(user.getName());
        existing.setSurname(user.getSurname());
        existing.setEmail(user.getEmail());
        existing.setPassword(user.getPassword());
        existing.setRole(user.getRole());
        existing.setPhone(user.getPhone());
        existing.setEnabled(user.isEnabled());

        return repository.save(existing);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public User findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findByPhone(String phone) {
        return repository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> findByRole(String role) {
        return repository.findByRole(role);
    }

    public List<User> findByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    public List<User> findBySurname(String surname) {
        return repository.findBySurnameContainingIgnoreCase(surname);
    }

    public List<User> findByEnabled(boolean enabled) {
        return repository.findByEnabled(enabled);
    }

    public long countByRole(String role) {
        return repository.countByRole(role);
    }

    public long countEnabledUsers() {
        return repository.countByEnabled(true);
    }

    public long countDisabledUsers() {
        return repository.countByEnabled(false);
    }

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // ==================================================================

    public Page<User> getAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<User> findByNamePaged(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Page<User> findBySurnamePaged(String surname, Pageable pageable) {
        return repository.findBySurnameContainingIgnoreCase(surname, pageable);
    }

    public Page<User> findByRolePaged(String role, Pageable pageable) {
        return repository.findByRole(role, pageable);
    }

    public Page<User> findByEnabledPaged(boolean enabled, Pageable pageable) {
        return repository.findByEnabled(enabled, pageable);
    }
}