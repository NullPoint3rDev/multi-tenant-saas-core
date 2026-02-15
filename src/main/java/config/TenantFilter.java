package config;

import async.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import repository.TenantRepository;

import java.io.IOException;

@Component
@Order(1)
public class TenantFilter implements Filter{

    private final TenantRepository tenantRepository;

    public TenantFilter(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        try {
            String slug = request.getHeader("X-Tenant-Slug");
            if(slug != null && !slug.isBlank()) {
                tenantRepository.findBySlug(slug.strip())
                        .ifPresent(tenant -> TenantContext.setTenantId(tenant.getId()));
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            TenantContext.clear();
        }
    }
}
