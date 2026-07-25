package com.abhipsa.digital.law.repository;

import com.abhipsa.digital.law.entity.TermsAndConditions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermsAndConditionsRepository extends JpaRepository<TermsAndConditions, String> {

    List<TermsAndConditions> findAllByOrderByUpdatedAtDesc();
}
