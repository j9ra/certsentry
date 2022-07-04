package pl.grabojan.certsentry.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import pl.grabojan.certsentry.data.model.CertIdentity;

public interface CertIdentityRepository extends CrudRepository<CertIdentity, Long> {

	List<CertIdentity> findBySerialNumber(String serialNumber);
	List<CertIdentity> findByIssuer(String issuer);
	List<CertIdentity> findBySubject(String subject);
	Optional<CertIdentity> findBySerialNumberAndIssuer(String serialNumber, String issuerName);
}
