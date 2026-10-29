package gr.ekpa.citizens.service;

import gr.ekpa.citizens.domain.Citizen;
import gr.ekpa.citizens.domain.CitizenUpdate;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class CitizenService {

	private static final Pattern AFM_PATTERN = Pattern.compile("\\d{9}");

	private final CitizenRepository citizenRepository;

	public CitizenService(CitizenRepository citizenRepository) {
		this.citizenRepository = citizenRepository;
	}

	public Citizen create(Citizen citizen) {
		if (citizenRepository.existsById(citizen.getAt())) {
			throw new CitizenAlreadyExistsException("Citizen already exists: " + citizen.getAt());
		}
		return citizenRepository.save(citizen);
	}

	public Citizen get(String at) {
		validateAt(at);
		return citizenRepository.findById(at)
				.orElseThrow(() -> new CitizenNotFoundException("Citizen not found: " + at));
	}

	public Citizen update(String at, CitizenUpdate update) {
		validateAt(at);
		Citizen citizen = citizenRepository.findById(at)
				.orElseThrow(() -> new CitizenNotFoundException("Citizen not found: " + at));
		citizen.setAfm(update.afm());
		citizen.setAddress(update.address());
		return citizenRepository.save(citizen);
	}

	public void delete(String at) {
		validateAt(at);
		if (!citizenRepository.existsById(at)) {
			throw new CitizenNotFoundException("Citizen not found: " + at);
		}
		citizenRepository.deleteById(at);
	}

	public List<Citizen> search(String at, String firstName, String lastName, String gender, LocalDate birthDate,
			String afm, String address) {
		if (at != null) {
			validateAt(at);
		}
		if (afm != null && !AFM_PATTERN.matcher(afm).matches()) {
			throw new InvalidInputException("AFM must contain exactly 9 digits");
		}

		Specification<Citizen> specification = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			addStringPredicate(predicates, criteriaBuilder, root.<String>get("at"), at);
			addStringPredicate(predicates, criteriaBuilder, root.<String>get("firstName"), firstName);
			addStringPredicate(predicates, criteriaBuilder, root.<String>get("lastName"), lastName);
			addStringPredicate(predicates, criteriaBuilder, root.<String>get("gender"), gender);
			if (birthDate != null) {
				predicates.add(criteriaBuilder.equal(root.get("birthDate"), birthDate));
			}
			addStringPredicate(predicates, criteriaBuilder, root.<String>get("afm"), afm);
			addStringPredicate(predicates, criteriaBuilder, root.<String>get("address"), address);
			return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
		};

		return citizenRepository.findAll(specification);
	}

	private static void validateAt(String at) {
		if (at == null || at.isBlank() || at.length() != 8) {
			throw new InvalidInputException("AT must contain exactly 8 non-blank characters");
		}
	}

	private static void addStringPredicate(List<Predicate> predicates,
			CriteriaBuilder criteriaBuilder, Expression<String> field, String value) {
		if (value != null && !value.isBlank()) {
			predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(field), value.toLowerCase(Locale.ROOT)));
		}
	}
}
