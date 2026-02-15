package controller;

import async.TenantContext;
import domain.Report;
import dto.CreateReportRequest;
import dto.ReportResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repository.ReportRepository;
import repository.TenantRepository;
import exception.NoTenantContextException;
import service.ReportGenerationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final TenantRepository tenantRepository;
    private final ReportRepository reportRepository;
    private final ExecutorService virtualThreadExecutor;
    private final ReportGenerationService reportGenerationService;

    public ReportController(TenantRepository tenantRepository, ReportRepository reportRepository,
                            @Qualifier("virtualThreadExecutor") ExecutorService virtualThreadExecutor,
                            ReportGenerationService reportGenerationService) {
        this.tenantRepository = tenantRepository;
        this.reportRepository = reportRepository;
        this.virtualThreadExecutor = virtualThreadExecutor;
        this.reportGenerationService = reportGenerationService;
    }

    private void requireTenantId() {
        if(TenantContext.getTenantId() == null) {
            throw new NoTenantContextException();
        }
    }

    @PostMapping("/reports")
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody CreateReportRequest reportRequest) {
        requireTenantId();

        Report report = new Report();
        report.setTenant(tenantRepository.getReferenceById(TenantContext.getTenantId()));
        report.setName(reportRequest.getName());
        report.setType(reportRequest.getType());
        Report saved = reportRepository.save(report);


        ReportResponse reportResponse = new ReportResponse();
        reportResponse.setId(saved.getId());
        reportResponse.setName(saved.getName());
        reportResponse.setType(saved.getType());
        reportResponse.setCreatedAt(saved.getCreatedAt());

        return status(HttpStatus.CREATED).body(reportResponse);
    }

    @PostMapping("/reports/{id}/generate")
    public ResponseEntity<?> triggerReportGeneration(@PathVariable Long id) {
        requireTenantId();

        Long tenantId = TenantContext.getTenantId();
        Optional<Report> opt = reportRepository.findById(id);
        if(opt.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        if(!opt.get().getTenant().getId().equals(tenantId)) {
            return ResponseEntity.notFound().build();
        }
        virtualThreadExecutor.submit(() -> reportGenerationService.generateReport(tenantId, id));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("message", "Report generation started"));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<ReportResponse>> listReports() {
        requireTenantId();

        Long tenantId = TenantContext.getTenantId();
        List<Report> reports = reportRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);

        List<ReportResponse> list = new ArrayList<>();
        for(Report report1 : reports) {
            list.add(toResponse(report1));
        }

        return ResponseEntity.ok(list);
    }

    @GetMapping("/reports/{id}")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable Long id) {
        requireTenantId();

        Long tenantId = TenantContext.getTenantId();
        Optional<Report> opt = reportRepository.findById(id);
        if(opt.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Report report = opt.get();
        if(!report.getTenant().getId().equals(tenantId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(report));
    }

    private ReportResponse toResponse(Report report) {
        ReportResponse r = new ReportResponse();
        r.setId(report.getId());
        r.setName(report.getName());
        r.setType(report.getType());
        r.setCreatedAt(report.getCreatedAt());
        return r;
    }
}
