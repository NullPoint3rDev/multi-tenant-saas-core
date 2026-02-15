package controller;

import domain.Tenant;
import dto.CreateTenantRequest;
import dto.TenantResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repository.TenantRepository;

@RestController
@RequestMapping("/api")
public class TenantController {

    private final TenantRepository tenantRepository;

    public TenantController(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @PostMapping("/tenants")
    public ResponseEntity<TenantResponse> registerTenant (@Valid @RequestBody CreateTenantRequest request) {
        if(tenantRepository.findBySlug(request.getSlug()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setSlug(request.getSlug());
        Tenant saved = tenantRepository.save(tenant);

        TenantResponse response = new TenantResponse();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setSlug(saved.getSlug());
        response.setStatus(saved.getStatus());
        response.setCreatedAt(saved.getCreated_at());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
