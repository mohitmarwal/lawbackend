package com.abhipsa.digital.law.repository;

import com.abhipsa.digital.law.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    List<User> findByName(String name);

    List<User> findBySurname(String surname);

    List<User> findByRole(String role);

    List<User> findByEnabled(boolean enabled);

    List<User> findByNameContainingIgnoreCase(String name);

    List<User> findBySurnameContainingIgnoreCase(String surname);

    List<User> findByEmailContainingIgnoreCase(String email);

    List<User> findByRoleContainingIgnoreCase(String role);

    List<User> findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(
            String name,
            String surname);

    List<User> findByRoleAndEnabled(
            String role,
            boolean enabled);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    long countByRole(String role);

    long countByEnabled(boolean enabled);

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // JpaRepository already provides Page<User> findAll(Pageable)
    // for the unfiltered case, so only filtered paged queries are added here.
    // ==================================================================

    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<User> findBySurnameContainingIgnoreCase(String surname, Pageable pageable);

    Page<User> findByRole(String role, Pageable pageable);

    Page<User> findByEnabled(boolean enabled, Pageable pageable);
}