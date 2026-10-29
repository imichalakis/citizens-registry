package gr.ekpa.citizens.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CitizensClientApplication implements CommandLineRunner {

	private final MenuRunner menuRunner;

	public CitizensClientApplication(MenuRunner menuRunner) {
		this.menuRunner = menuRunner;
	}

	public static void main(String[] args) {
		SpringApplication.run(CitizensClientApplication.class, args);
	}

	@Override
	public void run(String... args) {
		menuRunner.run();
	}
}
