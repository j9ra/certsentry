package pl.grabojan.certsentry.data.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import pl.grabojan.certsentry.data.model.Provider;
import pl.grabojan.certsentry.data.model.Service;
import pl.grabojan.certsentry.data.model.ServiceStatus;
import pl.grabojan.certsentry.data.model.ServiceType;

public interface ServiceRepository extends CrudRepository<Service, Long> {

	List<Service> findByType(ServiceType type);
	List<Service> findByName(String name);
	List<Service> findByStatus(ServiceStatus status);
	List<Service> findByProvider(Provider provider);
	List<Service> findByStartDateBetween(Date startDate, Date endDate);
}
