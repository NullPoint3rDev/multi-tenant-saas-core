package saas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = {
    R2dbcAutoConfiguration.class,
    CassandraAutoConfiguration.class
})
@ComponentScan(basePackages = {"saas", "controller", "domain", "repository", "config", "async", "exception", "dto", "service"})
@EntityScan(basePackages = "domain")
@EnableJpaRepositories(basePackages = "repository")
public class MultiTenantSaasCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiTenantSaasCoreApplication.class, args);
    }
}
