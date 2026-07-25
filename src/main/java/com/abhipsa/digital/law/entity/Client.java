package com.abhipsa.digital.law.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true)
    private String name;

    private String address;

    @ElementCollection
    @CollectionTable(name = "client_mobile_numbers", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "mobile_number")
    private List<String> mobileNumbers;

    @ElementCollection
    @CollectionTable(name = "client_email_ids", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "email_id")
    private List<String> emailIds;

    // plaintiff | defendant | others
    private String clientRole;

    private LocalDateTime createdAt = LocalDateTime.now();
}
