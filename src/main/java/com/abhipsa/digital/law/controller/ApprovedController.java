package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.Approved;
import com.abhipsa.digital.law.service.ApprovedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approved")
@RequiredArgsConstructor
public class ApprovedController {

    private final ApprovedService service;

    @PostMapping
    public Approved create(@RequestBody Approved approved) {
        return service.create(approved);
    }

    @GetMapping
    public List<Approved> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Approved getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Approved update(
            @PathVariable String id,
            @RequestBody Approved approved) {

        return service.update(id, approved);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/status/{approved}")
    public List<Approved> findByApproved(
            @PathVariable boolean approved) {

        return service.findByApproved(approved);
    }

    @GetMapping("/case/{caseId}")
    public List<Approved> findByCaseId(
            @PathVariable String caseId) {

        return service.findByCaseId(caseId);
    }

    @GetMapping("/court/{courtId}")
    public List<Approved> findByCourtId(
            @PathVariable String courtId) {

        return service.findByCourtId(courtId);
    }

    @GetMapping("/entered-by/{enteredBy}")
    public List<Approved> findByEnteredBy(
            @PathVariable String enteredBy) {

        return service.findByEnteredBy(enteredBy);
    }
}