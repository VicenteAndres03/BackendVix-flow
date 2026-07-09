package apicalendario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApicalendarioApplication {

	public static void main(String[] args) {
		io.github.cdimascio.dotenv.Dotenv.configure().systemProperties().load();
		SpringApplication.run(ApicalendarioApplication.class, args);
	}

}
