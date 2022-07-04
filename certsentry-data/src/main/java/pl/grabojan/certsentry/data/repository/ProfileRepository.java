package pl.grabojan.certsentry.data.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import pl.grabojan.certsentry.data.model.Profile;

public interface ProfileRepository extends CrudRepository<Profile, Long> {

	Optional<Profile> findByName(String name);
	
	
}
