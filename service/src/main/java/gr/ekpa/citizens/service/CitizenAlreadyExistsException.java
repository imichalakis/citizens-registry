package gr.ekpa.citizens.service;

public class CitizenAlreadyExistsException extends RuntimeException {

	public CitizenAlreadyExistsException(String message) {
		super(message);
	}
}
