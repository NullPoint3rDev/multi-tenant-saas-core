package repository;

import domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    Optional<Report> findByIdAndTenantId(Long id, Long tenantId);
}
