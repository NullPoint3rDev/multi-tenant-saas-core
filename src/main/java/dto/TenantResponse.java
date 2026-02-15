package dto;

import domain.Tenant;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TenantResponse {

    private long id;
    private String name;
    private String slug;
    private Tenant.TenantStatus status;
    private Instant createdAt;
}
