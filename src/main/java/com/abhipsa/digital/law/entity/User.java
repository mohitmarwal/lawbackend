package com.abhipsa.digital.law.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private String surname;

    private String email;

    private String password;

    private String role;

    private String phone;

    private boolean enabled = true;

    // Only set for role="client" logins - links this account to the party
    // record (plaintiff/defendant) it may view cases/documents for. Accepted
    // on create (write-only) but never serialized back out: nothing in the
    // frontend needs it, and Client's own lazy collections (emailIds/
    // mobileNumbers) can't be initialized once this User (the authenticated
    // principal) is detached from its original request-scoped session.
    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Client client;
}