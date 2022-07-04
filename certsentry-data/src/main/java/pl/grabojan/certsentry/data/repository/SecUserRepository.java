package pl.grabojan.certsentry.data.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import pl.grabojan.certsentry.data.model.SecUser;

public interface SecUserRepository extends CrudRepository<SecUser, Long> {

	Optional<SecUser> findByUsername(String username);
	
}
