package pl.grabojan.certsentry.data.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import pl.grabojan.certsentry.data.model.Application;

public interface ApplicationRepository extends CrudRepository<Application, Long> {

	Optional<Application> findByApiKey(String apiKey);
	
}
