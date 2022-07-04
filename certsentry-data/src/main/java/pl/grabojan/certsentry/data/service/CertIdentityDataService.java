package pl.grabojan.certsentry.data.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.Cacheable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.grabojan.certsentry.data.model.CertIdentity;
import pl.grabojan.certsentry.data.model.SupplyPoint;
import pl.grabojan.certsentry.data.repository.CertIdentityRepository;

@Transactional
@Slf4j
@RequiredArgsConstructor
public class CertIdentityDataService {

	private final CertIdentityRepository certIdentityRepository;
	
	
	@Cacheable(cacheNames = "certIdentities")
	public List<CertIdentity> getIssuers(String issuerName) {
		log.debug("Searching issuers for name: [{}]", issuerName);
		// cert issuerName is CA subjectName
		return certIdentityRepository.findBySubject(issuerName);
	}
	
	public List<SupplyPoint> getCertIdentitySupplyPoints(String serialNumber, String issuerName) {
		
		List<SupplyPoint> supplyPoints = new ArrayList<>();
		
		Optional<CertIdentity> certIdentity = certIdentityRepository.findBySerialNumberAndIssuer(serialNumber,issuerName);
		if(certIdentity.isPresent()) {
			certIdentity.get().
				getService().getSupplyPoints().
					forEach(sp -> supplyPoints.add(sp));
		}
		return supplyPoints;
	}
	
	
}
