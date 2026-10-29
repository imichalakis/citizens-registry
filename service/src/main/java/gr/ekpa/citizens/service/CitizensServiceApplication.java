package gr.ekpa.citizens.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "gr.ekpa.citizens.domain")
public class CitizensServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CitizensServiceApplication.class, args);
	}

}
