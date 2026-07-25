package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.Bill;
import com.abhipsa.digital.law.entity.CaseDetails;
import com.abhipsa.digital.law.entity.Client;
import com.abhipsa.digital.law.entity.Notice;
import com.abhipsa.digital.law.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;

    @PostMapping
    public Client create(@RequestBody Client client) {
        return service.create(client);
    }

    @GetMapping
    public List<Client> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Client getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Client update(
            @PathVariable String id,
            @RequestBody Client client) {

        return service.update(id, client);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/name/{name}")
    public List<Client> findByName(@PathVariable String name) {
        return service.findByName(name);
    }

    @GetMapping("/{id}/cases")
    public List<CaseDetails> listCases(@PathVariable String id) {
        return service.listCases(id);
    }

    @GetMapping("/{id}/bills")
    public List<Bill> listBills(@PathVariable String id) {
        return service.listBills(id);
    }

    @GetMapping("/{id}/notices")
    public List<Notice> listNotices(@PathVariable String id) {
        return service.listNotices(id);
    }

    @GetMapping("/paged")
    public Page<Client> getAllPaged(@PageableDefault(size = 20) Pageable pageable) {
        return service.getAllPaged(pageable);
    }
}
