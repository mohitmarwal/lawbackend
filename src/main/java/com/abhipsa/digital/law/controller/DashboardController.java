package com.abhipsa.digital.law.controller;

import com.abhipsa.digital.law.entity.Approved;
import com.abhipsa.digital.law.entity.CaseDetails;
import com.abhipsa.digital.law.entity.Notice;
import com.abhipsa.digital.law.service.ApprovedService;
import com.abhipsa.digital.law.service.CaseDetailsService;
import com.abhipsa.digital.law.service.NoticeService;
import com.abhipsa.digital.law.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final CaseDetailsService caseService;
    private final NoticeService noticeService;
    private final ApprovedService approvedService;
    private final TaskService taskService;
    private String caseType;
    private LocalDate limitationDate;

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {

        List<CaseDetails> cases = caseService.getAll();
        List<Notice> notices = noticeService.getAll();
        List<Approved> approvals = approvedService.getAll();

        long disposedCases = cases.stream()
                .filter(c -> c.getStatus() != null)
                .filter(c -> c.getStatus().equalsIgnoreCase("Disposed"))
                .count();

        long pendingCases = cases.stream()
                .filter(c -> c.getStatus() != null)
                .filter(c -> c.getStatus().equalsIgnoreCase("Pending"))
                .count();

        long pendingDates = cases.stream()
                .filter(c -> c.getNextDate() != null)
                .count();

        long pendingApprovals = approvals.stream()
                .filter(a -> !a.isApproved())
                .count();

        long noticePendingApprovals = approvals.stream()
                .filter(a -> !a.isApproved())
                .count();

        long pendingTasks = taskService.getAll()
                .stream()
                .filter(t -> t.getStatus() != null)
                .filter(t ->
                        t.getStatus().equalsIgnoreCase("Pending") ||
                                t.getStatus().equalsIgnoreCase("Open"))
                .count();


        long limitationBegins = cases.stream()
                .filter(c -> c.getLimitationDate() != null)
                .filter(c -> !c.getLimitationDate().isBefore(LocalDate.now()))
                .filter(c -> !c.getLimitationDate().isAfter(LocalDate.now().plusDays(30)))
                .count();



        long filedSuitComplaint = cases.stream()
                .filter(c -> c.getCaseType() != null)
                .filter(c ->
                        c.getCaseType().equalsIgnoreCase("SUIT") ||
                                c.getCaseType().equalsIgnoreCase("COMPLAINT"))
                .count();

        Map<String, Object> response = new HashMap<>();

        response.put("disposedCases", disposedCases);
        response.put("pendingCases", pendingCases);
        response.put("pendingDates", pendingDates);
        response.put("pendingApprovals", pendingApprovals);
        response.put("notices", notices.size());
        response.put("noticePendingApprovals", noticePendingApprovals);
        response.put("pendingTasks", pendingTasks);

        // Need additional fields in database
        response.put("limitationBegins", limitationBegins);
        response.put("filedSuitComplaint", filedSuitComplaint);

        return response;
    }
}