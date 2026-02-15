package domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant created_at;

    @Column(name = "updated_at", nullable = false)
    private Instant updated_at;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if(created_at == null) created_at = now;
        updated_at = now;
    }

    @PreUpdate
    void preUpdate() {
        updated_at = Instant.now();
    }

    public Tenant() {}

    public Tenant(Long id, String name, String slug, TenantStatus status, Instant created_at, Instant updated_at) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public void setStatus(TenantStatus status) {
        this.status = status;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }

    public Instant getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Instant updated_at) {
        this.updated_at = updated_at;
    }

    public enum TenantStatus {
        ACTIVE,
        SUSPENDED
    }
}
