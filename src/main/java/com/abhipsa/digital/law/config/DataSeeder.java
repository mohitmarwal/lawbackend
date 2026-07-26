package com.abhipsa.digital.law.config;

import com.abhipsa.digital.law.entity.User;
import com.abhipsa.digital.law.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Ensures a fresh deployment always has one working admin login, since
// there's otherwise no way in — associates/senior associates can only be
// created by an admin, and there's no admin until one exists. Idempotent:
// only creates the row if it isn't already there, so it's a no-op on every
// restart after the first.
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@login.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(DEFAULT_ADMIN_EMAIL).isPresent()) {
            return;
        }

        User admin = new User();
        admin.setName("Admin");
        admin.setSurname("");
        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setPassword(DEFAULT_ADMIN_PASSWORD);
        admin.setRole("admin");
        admin.setEnabled(true);
        userRepository.save(admin);
    }
}
