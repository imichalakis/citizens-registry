package gr.ekpa.citizens.client;

import gr.ekpa.citizens.domain.Citizen;
import gr.ekpa.citizens.domain.CitizenUpdate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

@Component
public class MenuRunner {

	private static final DateTimeFormatter BIRTH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	private final CitizenApiClient citizenApiClient;
	private final Scanner scanner;

	public MenuRunner(CitizenApiClient citizenApiClient) {
		this.citizenApiClient = citizenApiClient;
		this.scanner = new Scanner(System.in);
	}

	public void run() {
		while (true) {
			printMenu();
			if (!scanner.hasNextLine()) {
				printGoodbye();
				return;
			}

			String choice = scanner.nextLine();
			try {
				switch (choice) {
					case "1" -> insertCitizen();
					case "2" -> deleteCitizen();
					case "3" -> updateCitizen();
					case "4" -> searchCitizens();
					case "5" -> showCitizen();
					default -> {
						printGoodbye();
						return;
					}
				}
			} catch (NoSuchElementException exception) {
				printGoodbye();
				return;
			}
		}
	}

	private void insertCitizen() {
		String at = prompt("AT: ");
		String firstName = prompt("First name: ");
		String lastName = prompt("Last name: ");
		String gender = prompt("Gender: ");
		String birthDateInput = prompt("Birth date (dd-MM-yyyy): ");
		String afm = optionalValue(prompt("AFM (optional): "));
		String address = optionalValue(prompt("Address (optional): "));

		try {
			LocalDate birthDate = LocalDate.parse(birthDateInput, BIRTH_DATE_FORMAT);
			printResult(citizenApiClient.create(
					new Citizen(at, firstName, lastName, gender, birthDate, afm, address)));
		} catch (DateTimeParseException exception) {
			System.out.println("Invalid birth date. Use dd-MM-yyyy.");
		}
	}

	private void deleteCitizen() {
		String at = prompt("AT: ");
		printResult(citizenApiClient.delete(at));
	}

	private void updateCitizen() {
		String at = prompt("AT: ");
		String afm = optionalValue(prompt("AFM (optional): "));
		String address = optionalValue(prompt("Address (optional): "));
		printResult(citizenApiClient.update(at, new CitizenUpdate(afm, address)));
	}

	private void searchCitizens() {
		Map<String, String> params = new HashMap<>();
		addSearchParameter(params, "at", "AT (optional): ");
		addSearchParameter(params, "firstName", "First name (optional): ");
		addSearchParameter(params, "lastName", "Last name (optional): ");
		addSearchParameter(params, "gender", "Gender (optional): ");
		addSearchParameter(params, "birthDate", "Birth date (dd-MM-yyyy, optional): ");
		addSearchParameter(params, "afm", "AFM (optional): ");
		addSearchParameter(params, "address", "Address (optional): ");
		printResult(citizenApiClient.search(params));
	}

	private void showCitizen() {
		String at = prompt("AT: ");
		printResult(citizenApiClient.get(at));
	}

	private void addSearchParameter(Map<String, String> params, String name, String prompt) {
		String value = optionalValue(prompt(prompt));
		if (value != null) {
			params.put(name, value);
		}
	}

	private String prompt(String message) {
		System.out.print(message);
		return scanner.nextLine();
	}

	private static String optionalValue(String value) {
		return value.isBlank() ? null : value;
	}

	private static void printResult(CitizenApiClient.ApiResult<?> result) {
		if (!result.isSuccessful()) {
			System.out.println("Error: " + result.error());
		} else if (result.body() == null) {
			System.out.println("Request completed successfully.");
		} else if (result.body() instanceof Citizen citizen) {
			System.out.println(formatCitizen(citizen));
		} else if (result.body() instanceof List<?> results) {
			if (results.isEmpty()) {
				System.out.println("No citizens found.");
			} else {
				results.forEach(item -> System.out.println(
						item instanceof Citizen citizen ? formatCitizen(citizen) : item));
			}
		} else {
			System.out.println(result.body());
		}
	}

	private static String formatCitizen(Citizen citizen) {
		return "AT: " + citizen.getAt()
				+ ", First name: " + citizen.getFirstName()
				+ ", Last name: " + citizen.getLastName()
				+ ", Gender: " + citizen.getGender()
				+ ", Birth date: " + BIRTH_DATE_FORMAT.format(citizen.getBirthDate())
				+ ", AFM: " + citizen.getAfm()
				+ ", Address: " + citizen.getAddress();
	}

	private static void printMenu() {
		System.out.println();
		System.out.println("1 Insert citizen");
		System.out.println("2 Delete citizen");
		System.out.println("3 Update citizen (AFM and address)");
		System.out.println("4 Search");
		System.out.println("5 Show citizen by AT");
		System.out.print("Choice: ");
	}

	private static void printGoodbye() {
		System.out.println("Goodbye.");
	}
}
