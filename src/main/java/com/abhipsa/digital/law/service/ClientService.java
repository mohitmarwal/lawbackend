package com.abhipsa.digital.law.service;

import com.abhipsa.digital.law.entity.Bill;
import com.abhipsa.digital.law.entity.CaseDetails;
import com.abhipsa.digital.law.entity.Client;
import com.abhipsa.digital.law.entity.Notice;
import com.abhipsa.digital.law.repository.BillRepository;
import com.abhipsa.digital.law.repository.CaseDetailsRepository;
import com.abhipsa.digital.law.repository.ClientRepository;
import com.abhipsa.digital.law.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository repository;
    private final CaseDetailsRepository caseDetailsRepository;
    private final BillRepository billRepository;
    private final NoticeRepository noticeRepository;

    // Finds an existing Client by name (case-insensitive) or creates one.
    // Case/Notice parties are stored as a Client FK internally, but the
    // controllers keep accepting/returning plaintiff/defendant as plain names.
    public Client resolveByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String trimmed = name.trim();
        return repository.findByNameIgnoreCase(trimmed)
                .orElseGet(() -> {
                    Client client = new Client();
                    client.setName(trimmed);
                    return repository.save(client);
                });
    }

    public Client create(Client client) {
        return repository.save(client);
    }

    public List<Client> getAll() {
        return repository.findAll();
    }

    public Client getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }

    public Client update(String id, Client client) {
        Client existing = getById(id);

        existing.setName(client.getName());
        existing.setAddress(client.getAddress());
        existing.setMobileNumbers(client.getMobileNumbers());
        existing.setEmailIds(client.getEmailIds());
        existing.setClientRole(client.getClientRole());

        return repository.save(existing);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public List<Client> findByName(String name) {
        return repository.findAll().stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    public Page<Client> getAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<CaseDetails> listCases(String clientId) {
        return caseDetailsRepository.findByPlaintiffClientIdOrDefendantClientId(clientId, clientId);
    }

    public List<Bill> listBills(String clientId) {
        List<String> caseIds = listCases(clientId).stream().map(CaseDetails::getId).toList();
        return billRepository.findByCaseDetailsIdIn(caseIds);
    }

    public List<Notice> listNotices(String clientId) {
        List<String> caseIds = listCases(clientId).stream().map(CaseDetails::getId).toList();
        return noticeRepository.findByCaseDetailsIdIn(caseIds);
    }
}
