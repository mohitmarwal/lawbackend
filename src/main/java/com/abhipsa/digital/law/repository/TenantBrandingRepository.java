package com.abhipsa.digital.law.repository;

import com.abhipsa.digital.law.entity.TenantBranding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantBrandingRepository extends JpaRepository<TenantBranding, String> {

    List<TenantBranding> findAllByOrderByUpdatedAtDesc();
}
