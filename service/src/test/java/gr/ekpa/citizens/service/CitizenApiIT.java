package gr.ekpa.citizens.service;

import gr.ekpa.citizens.domain.Citizen;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "spring.datasource.url=jdbc:h2:mem:it")
class CitizenApiIT {

	private static final String VALID_CITIZEN_JSON = """
			{"at":"12345678","firstName":"Alex","lastName":"Smith","gender":"F",
			"birthDate":"12-11-2008","afm":"123456789","address":"Main Street"}
			""";

	@LocalServerPort
	private int port;

	@Autowired
	private CitizenRepository citizenRepository;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
		citizenRepository.deleteAll();
	}

	@Test
	void createCitizenReturnsCreatedCitizen() {
		given()
				.contentType(ContentType.JSON)
				.body(VALID_CITIZEN_JSON)
				.when()
				.post("/citizens")
				.then()
				.statusCode(201)
				.header("Location", endsWith("/citizens/12345678"))
				.body("at", equalTo("12345678"))
				.body("firstName", equalTo("Alex"))
				.body("lastName", equalTo("Smith"))
				.body("gender", equalTo("F"))
				.body("birthDate", equalTo("12-11-2008"))
				.body("afm", equalTo("123456789"))
				.body("address", equalTo("Main Street"));
	}

	@Test
	void createDuplicateCitizenReturnsConflict() {
		given().contentType(ContentType.JSON).body(VALID_CITIZEN_JSON).post("/citizens").then().statusCode(201);

		given()
				.contentType(ContentType.JSON)
				.body(VALID_CITIZEN_JSON)
				.when()
				.post("/citizens")
				.then()
				.statusCode(409)
				.body("error", containsString("already exists"));
	}

	@Test
	void createInvalidCitizenRequestsReturnBadRequest() {
		given()
				.contentType(ContentType.JSON)
				.body(VALID_CITIZEN_JSON.replace("\"12345678\"", "\"123\""))
				.post("/citizens")
				.then()
				.statusCode(400)
				.body("error", containsString("8"));

		given()
				.contentType(ContentType.JSON)
				.body(VALID_CITIZEN_JSON.replace("\"firstName\":\"Alex\",", ""))
				.post("/citizens")
				.then()
				.statusCode(400)
				.body("error", containsString("blank"));

		given()
				.contentType(ContentType.JSON)
				.body(VALID_CITIZEN_JSON.replace("123456789", "12"))
				.post("/citizens")
				.then()
				.statusCode(400)
				.body("error", containsString("9"));
	}

	@Test
	void createCitizenWithMalformedBirthDateReturnsBadRequest() {
		given()
				.contentType(ContentType.JSON)
				.body(VALID_CITIZEN_JSON.replace("12-11-2008", "2008-11-12"))
				.post("/citizens")
				.then()
				.statusCode(400)
				.body("error", containsString("malformed"));
	}

	@Test
	void getCitizenReturnsCitizen() {
		citizenRepository.save(citizen("12345678", "Alex", "Smith", "F", "123456789", "Main Street"));

		given()
				.when()
				.get("/citizens/12345678")
				.then()
				.statusCode(200)
				.body("at", equalTo("12345678"))
				.body("firstName", equalTo("Alex"))
				.body("lastName", equalTo("Smith"))
				.body("gender", equalTo("F"))
				.body("birthDate", equalTo("12-11-2008"))
				.body("afm", equalTo("123456789"))
				.body("address", equalTo("Main Street"));
	}

	@Test
	void getUnknownCitizenReturnsNotFound() {
		given()
				.get("/citizens/12345678")
				.then()
				.statusCode(404)
				.body("error", containsString("not found"));
	}

	@Test
	void getCitizenWithInvalidAtReturnsBadRequest() {
		given()
				.get("/citizens/123")
				.then()
				.statusCode(400)
				.body("error", containsString("AT"));
	}

	@Test
	void searchCitizensByLastNameCaseInsensitively() {
		citizenRepository.saveAll(List.of(
				citizen("00000001", "Alex", "Smith", "F", null, null),
				citizen("00000002", "Sam", "Jones", "M", null, null)));

		given()
				.queryParam("lastName", "sMiTh")
				.get("/citizens")
				.then()
				.statusCode(200)
				.body("size()", equalTo(1))
				.body("[0].at", equalTo("00000001"))
				.body("[0].lastName", equalTo("Smith"));
	}

	@Test
	void searchCitizensByFirstNameAndGender() {
		citizenRepository.saveAll(List.of(
				citizen("00000001", "Alex", "Smith", "F", null, null),
				citizen("00000002", "Alex", "Jones", "M", null, null),
				citizen("00000003", "Sam", "Smith", "F", null, null)));

		given()
				.queryParam("firstName", "alex")
				.queryParam("gender", "f")
				.get("/citizens")
				.then()
				.statusCode(200)
				.body("size()", equalTo(1))
				.body("[0].at", equalTo("00000001"));
	}

	@Test
	void searchWithoutMatchesReturnsEmptyList() {
		citizenRepository.save(citizen("12345678", "Alex", "Smith", "F", null, null));

		given()
				.queryParam("lastName", "Unknown")
				.get("/citizens")
				.then()
				.statusCode(200)
				.body("size()", equalTo(0));
	}

	@Test
	void searchWithInvalidAfmReturnsBadRequest() {
		given()
				.queryParam("afm", "12")
				.get("/citizens")
				.then()
				.statusCode(400)
				.body("error", containsString("9"));
	}

	@Test
	void updateCitizenChangesOnlyAfmAndAddress() {
		citizenRepository.save(citizen("12345678", "Alex", "Smith", "F", "123456789", "Main Street"));

		given()
				.contentType(ContentType.JSON)
				.body("""
						{"afm":"987654321","address":"New Street"}
						""")
				.put("/citizens/12345678")
				.then()
				.statusCode(200)
				.body("at", equalTo("12345678"))
				.body("firstName", equalTo("Alex"))
				.body("lastName", equalTo("Smith"))
				.body("gender", equalTo("F"))
				.body("birthDate", equalTo("12-11-2008"))
				.body("afm", equalTo("987654321"))
				.body("address", equalTo("New Street"));
	}

	@Test
	void updateCitizenWithInvalidAfmReturnsBadRequest() {
		citizenRepository.save(citizen("12345678", "Alex", "Smith", "F", "123456789", "Main Street"));

		given()
				.contentType(ContentType.JSON)
				.body("""
						{"afm":"12","address":"New Street"}
						""")
				.put("/citizens/12345678")
				.then()
				.statusCode(400)
				.body("error", containsString("9"));
	}

	@Test
	void updateUnknownCitizenReturnsNotFound() {
		given()
				.contentType(ContentType.JSON)
				.body("""
						{"afm":"987654321","address":"New Street"}
						""")
				.put("/citizens/12345678")
				.then()
				.statusCode(404)
				.body("error", containsString("not found"));
	}

	@Test
	void deleteCitizenReturnsNoContentAndThenNotFound() {
		citizenRepository.save(citizen("12345678", "Alex", "Smith", "F", null, null));

		given().delete("/citizens/12345678").then().statusCode(204);

		given()
				.get("/citizens/12345678")
				.then()
				.statusCode(404)
				.body("error", containsString("not found"));
	}

	@Test
	void deleteUnknownCitizenReturnsNotFound() {
		given()
				.delete("/citizens/12345678")
				.then()
				.statusCode(404)
				.body("error", containsString("not found"));
	}

	private static Citizen citizen(String at, String firstName, String lastName, String gender, String afm,
			String address) {
		return new Citizen(at, firstName, lastName, gender, LocalDate.of(2008, 11, 12), afm, address);
	}
}
