package gr.ekpa.citizens.service;

import gr.ekpa.citizens.domain.Citizen;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitizenEntityTest {

	private static ValidatorFactory validatorFactory;
	private static Validator validator;

	@BeforeAll
	static void createValidator() {
		validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@AfterAll
	static void closeValidatorFactory() {
		validatorFactory.close();
	}

	@Test
	void constructorAndGettersAndSettersStoreValues() {
		LocalDate birthDate = LocalDate.of(2008, 11, 12);
		Citizen citizen = new Citizen("12345678", "First", "Last", "F", birthDate, "123456789", "Address");

		assertEquals("12345678", citizen.getAt());
		assertEquals("First", citizen.getFirstName());
		assertEquals("Last", citizen.getLastName());
		assertEquals("F", citizen.getGender());
		assertEquals(birthDate, citizen.getBirthDate());
		assertEquals("123456789", citizen.getAfm());
		assertEquals("Address", citizen.getAddress());

		citizen.setAt("87654321");
		citizen.setFirstName("UpdatedFirst");
		citizen.setLastName("UpdatedLast");
		citizen.setGender("M");
		citizen.setBirthDate(LocalDate.of(2000, 1, 2));
		citizen.setAfm("987654321");
		citizen.setAddress("UpdatedAddress");

		assertEquals("87654321", citizen.getAt());
		assertEquals("UpdatedFirst", citizen.getFirstName());
		assertEquals("UpdatedLast", citizen.getLastName());
		assertEquals("M", citizen.getGender());
		assertEquals(LocalDate.of(2000, 1, 2), citizen.getBirthDate());
		assertEquals("987654321", citizen.getAfm());
		assertEquals("UpdatedAddress", citizen.getAddress());
	}

	@Test
	void validCitizenHasNoViolations() {
		assertTrue(validator.validate(validCitizen()).isEmpty());
	}

	@ParameterizedTest
	@ValueSource(strings = {"1234567", "123456789", " "})
	void invalidAtHasViolations(String at) {
		Citizen citizen = validCitizen();
		citizen.setAt(at);

		assertFalse(validator.validate(citizen).isEmpty());
	}

	@Test
	void missingFirstNameHasViolation() {
		Citizen citizen = validCitizen();
		citizen.setFirstName(null);

		assertFalse(validator.validate(citizen).isEmpty());
	}

	@Test
	void missingLastNameHasViolation() {
		Citizen citizen = validCitizen();
		citizen.setLastName(null);

		assertFalse(validator.validate(citizen).isEmpty());
	}

	@Test
	void missingGenderHasViolation() {
		Citizen citizen = validCitizen();
		citizen.setGender(null);

		assertFalse(validator.validate(citizen).isEmpty());
	}

	@Test
	void missingBirthDateHasViolation() {
		Citizen citizen = validCitizen();
		citizen.setBirthDate(null);

		assertFalse(validator.validate(citizen).isEmpty());
	}

	@Test
	void nullAfmIsValid() {
		Citizen citizen = validCitizen();
		citizen.setAfm(null);

		assertTrue(validator.validate(citizen).isEmpty());
	}

	@ParameterizedTest
	@ValueSource(strings = {"12345678", "1234567890", "12345678a"})
	void invalidAfmHasViolations(String afm) {
		Citizen citizen = validCitizen();
		citizen.setAfm(afm);

		assertFalse(validator.validate(citizen).isEmpty());
	}

	@Test
	void nullAddressIsValid() {
		Citizen citizen = validCitizen();
		citizen.setAddress(null);

		assertTrue(validator.validate(citizen).isEmpty());
	}

	@Test
	void equalityAndHashCodeAreBasedOnAt() {
		Citizen first = new Citizen("12345678", "First", "Person", "F",
				LocalDate.of(2008, 11, 12), null, null);
		Citizen sameAt = new Citizen("12345678", "Different", "Name", "M",
				LocalDate.of(2000, 1, 1), null, null);
		Citizen differentAt = new Citizen("87654321", "First", "Person", "F",
				LocalDate.of(2008, 11, 12), null, null);

		assertEquals(first, sameAt);
		assertEquals(first.hashCode(), sameAt.hashCode());
		assertNotEquals(first, differentAt);
	}

	@Test
	void jsonUsesFormattedBirthDateAndRestoresLocalDate() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Citizen citizen = validCitizen();

		String json = objectMapper.writeValueAsString(citizen);
		JsonNode birthDate = objectMapper.readTree(json).get("birthDate");

		assertEquals("\"12-11-2008\"", birthDate.toString());
		assertEquals(LocalDate.of(2008, 11, 12), objectMapper.readValue(json, Citizen.class).getBirthDate());
	}

	private static Citizen validCitizen() {
		return new Citizen("12345678", "First", "Last", "F",
				LocalDate.of(2008, 11, 12), "123456789", "Address");
	}
}
