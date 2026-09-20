package reception.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class FlywayConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String flywayLocations;

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        String[] locations = Arrays.stream(flywayLocations.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        return Flyway.configure()
                .locations(locations)
                .dataSource(datasourceUrl, datasourceUsername, datasourcePassword)
                .load();
    }
}
