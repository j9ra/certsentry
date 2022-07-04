package pl.grabojan.certsentry.data.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import pl.grabojan.certsentry.data.model.TrustedList;
import pl.grabojan.certsentry.data.model.TrustedListExpiresDTO;
import pl.grabojan.certsentry.data.model.TrustedListType;
import pl.grabojan.certsentry.data.model.TrustedListUpdatesDTO;

public interface TrustedListRepository extends CrudRepository<TrustedList, Long> {

	List<TrustedList> findByType(TrustedListType type);
	List<TrustedList> findByTerritory(String territory);
	List<TrustedList> findByDistributionPoint(String distributionPoint);
	List<TrustedList> findByIsValid(Boolean isValid);
	List<TrustedList> findByListIssueBetween(Date listIssueBegin, Date listIssueEnd);
	List<TrustedList> findByNextUpdateBetween(Date nextUpdateBegin, Date nextUpdateEnd);
	
	// projections
	<T> List<T> findBy(Class<T> type);
	
	@Query(value="SELECT new pl.grabojan.certsentry.data.model.TrustedListUpdatesDTO(tlu.id, tl.territory, tl.listIssue, tlu.timestamp as lastUpdate, tlu.status, tlu.errorCode)  FROM TrustedList tl JOIN TrustedListUpdate tlu on tl.id = tlu.trustedList.id")
	List<TrustedListUpdatesDTO> findByUpdates(Pageable page);
	
	@Query(value="SELECT new pl.grabojan.certsentry.data.model.TrustedListExpiresDTO(tl.territory, tl.nextUpdate, tl.lastCheck, tl.isValid) FROM TrustedList tl")
	List<TrustedListExpiresDTO> findByExpires(Pageable page);
}
