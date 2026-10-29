package gr.ekpa.citizens.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
		properties = "spring.datasource.url=jdbc:h2:mem:configtest",
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ConfigurationTest {

	@Autowired
	private CitizenRepository citizenRepository;

	@Autowired
	private CitizenService citizenService;

	@Value("${spring.datasource.url}")
	private String datasourceUrl;

	@Value("${spring.jpa.hibernate.ddl-auto}")
	private String ddlAuto;

	@Test
	void contextLoads() {
	}

	@Test
	void citizenRepositoryBeanExists() {
		assertNotNull(citizenRepository);
	}

	@Test
	void citizenServiceBeanExists() {
		assertNotNull(citizenService);
	}

	@Test
	void datasourceUrlUsesH2() {
		assertTrue(datasourceUrl.startsWith("jdbc:h2:"));
	}

	@Test
	void hibernateDdlAutoIsUpdate() {
		assertEquals("update", ddlAuto);
	}
}
