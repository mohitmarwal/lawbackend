package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.TermsAndConditions;
import com.abhipsa.digital.law.entity.User;
import com.abhipsa.digital.law.service.TermsAndConditionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
public class TermsAndConditionsController {

    private final TermsAndConditionsService service;

    @GetMapping
    public TermsAndConditions getLatest() {
        return service.getLatest();
    }

    @PutMapping
    public TermsAndConditions update(
            @RequestBody TermsAndConditionsUpdateRequest request) {

        User admin = null;
        if (request.updatedById() != null) {
            admin = new User();
            admin.setId(request.updatedById());
        }
        return service.update(request.content(), admin);
    }

    public record TermsAndConditionsUpdateRequest(String content, String updatedById) {
    }
}
