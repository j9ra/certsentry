package pl.grabojan.certsentry.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import pl.grabojan.certsentry.data.DataConfig;

@SpringBootApplication
@Import(DataConfig.class)
public class CertsentryAdminBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CertsentryAdminBackendApplication.class, args);
	}

}
