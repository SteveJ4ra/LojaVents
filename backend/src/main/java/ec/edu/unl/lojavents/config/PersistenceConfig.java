package ec.edu.unl.lojavents.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EntityScan(basePackages = {
        "ec.edu.unl.lojavents.user.domain",
        "ec.edu.unl.lojavents.venue.domain",
        "ec.edu.unl.lojavents.reservation.domain",
        "ec.edu.unl.lojavents.engagement.domain"
})
@EnableJpaRepositories(basePackages = {
        "ec.edu.unl.lojavents.user.repository",
        "ec.edu.unl.lojavents.venue.repository",
        "ec.edu.unl.lojavents.reservation.repository",
        "ec.edu.unl.lojavents.engagement.repository"
})
@EnableMongoRepositories(basePackages = "ec.edu.unl.lojavents.audit.repository")
public class PersistenceConfig {
}
