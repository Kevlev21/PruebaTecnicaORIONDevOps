package reception.test;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableRabbit
@SpringBootApplication(scanBasePackages = { "reception" })
@EnableJpaRepositories(basePackages = "reception.jpa") 
@EntityScan(basePackages = "reception.jpa")
public class TestApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}

}
