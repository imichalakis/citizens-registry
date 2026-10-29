package gr.ekpa.citizens.service;

import gr.ekpa.citizens.domain.Citizen;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CitizenRepositoryOrmTest {

	@Autowired
	private CitizenRepository citizenRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void saveThenFindByIdReturnsAllCitizenData() {
		Citizen citizen = citizen("12345678", "Alex", "Smith", "F",
				LocalDate.of(2008, 11, 12), "123456789", "Main Street");

		citizenRepository.save(citizen);

		assertThat(citizenRepository.findById(citizen.getAt()))
				.hasValueSatisfying(found -> assertCitizenFields(found, citizen));
	}

	@Test
	void saveAndFindByIdPreserveNullOptionalFields() {
		Citizen citizen = citizen("12345678", "Alex", "Smith", "F",
				LocalDate.of(2008, 11, 12), null, null);

		citizenRepository.save(citizen);
		entityManager.flush();
		entityManager.clear();

		assertThat(citizenRepository.findById(citizen.getAt()))
				.hasValueSatisfying(found -> {
					assertThat(found.getAfm()).isNull();
					assertThat(found.getAddress()).isNull();
				});
	}

	@Test
	void existsByIdReflectsWhetherCitizenExists() {
		Citizen citizen = citizen("12345678", "Alex", "Smith", "F",
				LocalDate.of(2008, 11, 12), null, null);
		citizenRepository.save(citizen);

		assertThat(citizenRepository.existsById(citizen.getAt())).isTrue();
		assertThat(citizenRepository.existsById("87654321")).isFalse();
	}

	@Test
	void savingSameAtTwiceUpdatesExistingCitizen() {
		Citizen original = citizen("12345678", "Alex", "Smith", "F",
				LocalDate.of(2008, 11, 12), null, null);
		Citizen updated = citizen("12345678", "Jordan", "Jones", "M",
				LocalDate.of(2000, 1, 2), "987654321", "New Street");

		citizenRepository.save(original);
		citizenRepository.save(updated);
		entityManager.flush();

		assertThat(citizenRepository.count()).isEqualTo(1);
		assertThat(citizenRepository.findById(updated.getAt()))
				.hasValueSatisfying(found -> assertCitizenFields(found, updated));
	}

	@Test
	void deleteByIdRemovesCitizen() {
		Citizen citizen = citizen("12345678", "Alex", "Smith", "F",
				LocalDate.of(2008, 11, 12), null, null);
		citizenRepository.save(citizen);

		citizenRepository.deleteById(citizen.getAt());

		assertThat(citizenRepository.findById(citizen.getAt())).isEmpty();
		assertThat(citizenRepository.count()).isZero();
	}

	@Test
	void persistFlushAndClearPreserveColumnMapping() {
		Citizen original = citizen("12345678", "Alex", "Smith", "F",
				LocalDate.of(2008, 11, 12), "123456789", "Main Street");

		entityManager.persist(original);
		entityManager.flush();
		entityManager.clear();

		Citizen found = entityManager.find(Citizen.class, original.getAt());
		assertThat(found).isEqualTo(original);
		assertCitizenFields(found, original);
	}

	@Test
	void flushWithNullFirstNameThrowsConstraintViolation() {
		Citizen citizen = citizen("12345678", null, "Smith", "F",
				LocalDate.of(2008, 11, 12), null, null);

		assertThatThrownBy(() -> {
			entityManager.persist(citizen);
			entityManager.flush();
		}).isInstanceOf(ConstraintViolationException.class)
  .hasMessageContaining("firstName");
	}

	@Test
	void specificationSearchIsCaseInsensitiveAndCombinable() {
		citizenRepository.saveAll(List.of(
				citizen("00000001", "Alex", "Smith", "F", LocalDate.of(2000, 1, 1), null, null),
				citizen("00000002", "Alex", "Smith", "M", LocalDate.of(2000, 1, 1), null, null),
				citizen("00000003", "Sam", "Smith", "F", LocalDate.of(2000, 1, 1), null, null),
				citizen("00000004", "Alex", "Jones", "F", LocalDate.of(2000, 1, 1), null, null)));

		Specification<Citizen> smithByLastName = (root, query, criteriaBuilder) ->
				criteriaBuilder.equal(criteriaBuilder.lower(root.get("lastName")), "smith");
		Specification<Citizen> alexSmithByFirstNameAndGender = (root, query, criteriaBuilder) ->
				criteriaBuilder.and(
						criteriaBuilder.equal(criteriaBuilder.lower(root.get("firstName")), "alex"),
						criteriaBuilder.equal(criteriaBuilder.lower(root.get("gender")), "f"));

		assertThat(citizenRepository.findAll(smithByLastName))
				.extracting(Citizen::getAt)
				.containsExactlyInAnyOrder("00000001", "00000002", "00000003");
		assertThat(citizenRepository.findAll(smithByLastName.and(alexSmithByFirstNameAndGender)))
				.extracting(Citizen::getAt)
				.containsExactly("00000001");
	}

	private static Citizen citizen(String at, String firstName, String lastName, String gender,
			LocalDate birthDate, String afm, String address) {
		return new Citizen(at, firstName, lastName, gender, birthDate, afm, address);
	}

	private static void assertCitizenFields(Citizen actual, Citizen expected) {
		assertThat(actual.getAt()).isEqualTo(expected.getAt());
		assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName());
		assertThat(actual.getLastName()).isEqualTo(expected.getLastName());
		assertThat(actual.getGender()).isEqualTo(expected.getGender());
		assertThat(actual.getBirthDate()).isEqualTo(expected.getBirthDate());
		assertThat(actual.getAfm()).isEqualTo(expected.getAfm());
		assertThat(actual.getAddress()).isEqualTo(expected.getAddress());
	}
}
