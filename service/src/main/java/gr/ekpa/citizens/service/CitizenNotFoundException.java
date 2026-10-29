package gr.ekpa.citizens.service;

public class CitizenNotFoundException extends RuntimeException {

	public CitizenNotFoundException(String message) {
		super(message);
	}
}
