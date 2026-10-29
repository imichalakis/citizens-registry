package gr.ekpa.citizens.service;

import gr.ekpa.citizens.domain.Citizen;
import gr.ekpa.citizens.domain.CitizenUpdate;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/citizens")
public class CitizenController {

	private final CitizenService citizenService;

	public CitizenController(CitizenService citizenService) {
		this.citizenService = citizenService;
	}

	@PostMapping
	public ResponseEntity<Citizen> create(@Valid @RequestBody Citizen citizen) {
		Citizen saved = citizenService.create(citizen);
		return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{at}").buildAndExpand(saved.getAt()).toUri()).body(saved);
	}

	@GetMapping("/{at}")
	public Citizen get(@PathVariable String at) {
		return citizenService.get(at);
	}

	@GetMapping
	public List<Citizen> search(
			@RequestParam(required = false) String at,
			@RequestParam(required = false) String firstName,
			@RequestParam(required = false) String lastName,
			@RequestParam(required = false) String gender,
			@RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate birthDate,
			@RequestParam(required = false) String afm,
			@RequestParam(required = false) String address) {
		return citizenService.search(at, firstName, lastName, gender, birthDate, afm, address);
	}

	@PutMapping("/{at}")
	public Citizen update(@PathVariable String at, @Valid @RequestBody CitizenUpdate update) {
		return citizenService.update(at, update);
	}

	@DeleteMapping("/{at}")
	public ResponseEntity<Void> delete(@PathVariable String at) {
		citizenService.delete(at);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
