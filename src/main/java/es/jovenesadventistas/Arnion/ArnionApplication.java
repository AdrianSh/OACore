package es.jovenesadventistas.Arnion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ArnionApplication {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger();

	
	public static void main(String[] args) {
		SpringApplication.run(ArnionApplication.class, args);
	}
	
}
