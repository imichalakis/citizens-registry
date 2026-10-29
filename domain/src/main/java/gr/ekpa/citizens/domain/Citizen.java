package gr.ekpa.citizens.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "citizens")
public class Citizen {

	/** National ID must contain exactly 8 characters. */
	@Id
	@NotBlank
	@Size(min = 8, max = 8)
	@Column(length = 8)
	private String at;

	@NotBlank
	@Column(nullable = false)
	private String firstName;

	@NotBlank
	@Column(nullable = false)
	private String lastName;

	@NotBlank
	@Column(nullable = false)
	private String gender;

	@NotNull
	@JsonFormat(pattern = "dd-MM-yyyy")
	@Column(nullable = false)
	private LocalDate birthDate;

	/** AFM may be absent; when present, it must contain exactly 9 digits. */
	@Pattern(regexp = "\\d{9}")
	@Column(length = 9)
	private String afm;

	private String address;

	public Citizen() {
	}

	public Citizen(String at, String firstName, String lastName, String gender, LocalDate birthDate, String afm,
			String address) {
		this.at = at;
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
		this.birthDate = birthDate;
		this.afm = afm;
		this.address = address;
	}

	public String getAt() {
		return at;
	}

	public void setAt(String at) {
		this.at = at;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public String getAfm() {
		return afm;
	}

	public void setAfm(String afm) {
		this.afm = afm;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Citizen citizen)) {
			return false;
		}
		return Objects.equals(at, citizen.at);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(at);
	}
}
