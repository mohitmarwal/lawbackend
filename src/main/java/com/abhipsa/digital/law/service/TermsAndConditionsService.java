package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.TermsAndConditions;
import com.abhipsa.digital.law.entity.User;
import com.abhipsa.digital.law.repository.TermsAndConditionsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TermsAndConditionsService {

    private final TermsAndConditionsRepository repository;

    // One global template applied to new bills; each bill can still override
    // it locally via Bill.termsAndConditions.
    public TermsAndConditions getLatest() {
        List<TermsAndConditions> all = repository.findAllByOrderByUpdatedAtDesc();
        if (!all.isEmpty()) {
            return all.get(0);
        }
        TermsAndConditions blank = new TermsAndConditions();
        blank.setContent("");
        return repository.save(blank);
    }

    public TermsAndConditions update(String content, User admin) {
        TermsAndConditions latest = getLatest();
        latest.setContent(content);
        latest.setUpdatedBy(admin);
        latest.setUpdatedAt(LocalDateTime.now());
        return repository.save(latest);
    }
}
