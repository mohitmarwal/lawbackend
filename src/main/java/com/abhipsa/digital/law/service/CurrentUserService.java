package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

// Reads the authenticated User that JwtAuthFilter placed in the security
// context, so services can scope data to "my own work" for non-admin roles.
@Service
public class CurrentUserService {

    public User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            return null;
        }
        return (User) auth.getPrincipal();
    }

    // Only "admin" sees every record; associate and senior_associate are
    // scoped to their own assigned cases/tasks/notices/notifications.
    public boolean isAdmin() {
        User user = getUser();
        return user != null && user.getRole() != null && user.getRole().equalsIgnoreCase("admin");
    }

    // Senior associates get elevated personnel-creation and task-assignment
    // privileges over plain associates (see UserService.create()/getAll() and
    // TaskService.create()/reassign()), but remain below admin.
    public boolean isSeniorAssociate() {
        User user = getUser();
        return user != null && user.getRole() != null && user.getRole().equalsIgnoreCase("senior_associate");
    }

    public String getUserId() {
        User user = getUser();
        return user != null ? user.getId() : null;
    }
}
