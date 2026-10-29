package gr.ekpa.citizens.domain;

import jakarta.validation.constraints.Pattern;

public record CitizenUpdate(
		@Pattern(regexp = "\\d{9}") String afm,
		String address) {
}
