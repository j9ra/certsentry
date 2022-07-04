package pl.grabojan.certsentry.data.repository;

import org.springframework.data.repository.CrudRepository;

import pl.grabojan.certsentry.data.model.ServiceExtension;

public interface ExtensionRepository extends CrudRepository<ServiceExtension, Long> {

}
