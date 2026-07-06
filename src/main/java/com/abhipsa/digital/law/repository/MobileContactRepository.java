package com.abhipsa.digital.law.repository;

import com.abhipsa.digital.law.entity.MobileContact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MobileContactRepository extends JpaRepository<MobileContact, String> {

    Optional<MobileContact> findByMobile(String mobile);

    List<MobileContact> findByName(String name);

    List<MobileContact> findByNameContainingIgnoreCase(String name);

    List<MobileContact> findByRole(String role);

    List<MobileContact> findByRoleContainingIgnoreCase(String role);

    List<MobileContact> findByCaseDetailsId(String caseId);

    List<MobileContact> findByNameContainingIgnoreCaseOrMobileContaining(
            String name,
            String mobile);

    boolean existsByMobile(String mobile);

    long countByRole(String role);

    long countByCaseDetailsId(String caseId);

    @Modifying
    @Query(value = """
    INSERT INTO mobile_contact (id, mobile, name, email, role, case_details_id)
    VALUES (UUID(), :mobile, :name, :email, :role, :caseId)
    ON DUPLICATE KEY UPDATE
        name = VALUES(name),
        email = VALUES(email),
        case_details_id = VALUES(case_details_id)
    """, nativeQuery = true)
    void upsertContact(
            @Param("mobile") String mobile,
            @Param("name") String name,
            @Param("email") String email,
            @Param("role") String role,
            @Param("caseId") String caseId
    );

    // ==================================================================
    // ---- Pagination support (added; existing methods above unchanged) ----
    // JpaRepository already provides Page<MobileContact> findAll(Pageable)
    // for the unfiltered case, so only filtered paged queries are added here.
    // ==================================================================

    Page<MobileContact> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<MobileContact> findByRoleContainingIgnoreCase(String role, Pageable pageable);

    Page<MobileContact> findByCaseDetailsId(String caseId, Pageable pageable);
}