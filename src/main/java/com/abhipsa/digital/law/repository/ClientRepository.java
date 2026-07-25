package com.abhipsa.digital.law.repository;

import com.abhipsa.digital.law.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    Optional<Client> findByNameIgnoreCase(String name);
}
