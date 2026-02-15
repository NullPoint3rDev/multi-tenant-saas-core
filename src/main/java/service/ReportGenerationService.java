package service;

import domain.Report;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import repository.ReportRepository;

import java.util.Optional;

@Service
public class ReportGenerationService {

    private static final String METRIC_REPORT_GENERATION = "tenant.report.generation";
    private static final String TAG_TENANT_ID = "tenant_id";

    private final ReportRepository reportRepository;
    private final MeterRegistry meterRegistry;

    public ReportGenerationService(ReportRepository reportRepository, MeterRegistry meterRegistry) {
        this.reportRepository = reportRepository;
        this.meterRegistry = meterRegistry;
    }

    public void generateReport(Long tenantId, Long reportId) {
        Optional<Report> opt = reportRepository.findByIdAndTenantId(reportId, tenantId);
        if (opt.isEmpty()) {
            return;
        }
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Thread.sleep(2000);
            meterRegistry.counter(METRIC_REPORT_GENERATION + ".completed", TAG_TENANT_ID, String.valueOf(tenantId)).increment();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            sample.stop(Timer.builder(METRIC_REPORT_GENERATION + ".duration")
                    .tag(TAG_TENANT_ID, String.valueOf(tenantId))
                    .register(meterRegistry));
        }
    }
}
