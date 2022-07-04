package pl.grabojan.certsentry.data.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import pl.grabojan.certsentry.data.model.Provider;

public interface ProviderRepository extends CrudRepository<Provider, Long> {

	List<Provider> findByName(String name);
	List<Provider> findByTradeName(String tradeName);
	
}
