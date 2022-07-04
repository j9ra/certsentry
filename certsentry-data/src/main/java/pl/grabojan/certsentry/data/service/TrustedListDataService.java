package pl.grabojan.certsentry.data.service;

import java.util.Calendar;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.grabojan.certsentry.data.model.TrustedList;
import pl.grabojan.certsentry.data.model.TrustedListUpdate;
import pl.grabojan.certsentry.data.repository.TrustedListRepository;
import pl.grabojan.certsentry.data.repository.TrustedListUpdateRepository;

@Transactional
@RequiredArgsConstructor
@Slf4j
public class TrustedListDataService {

	private final TrustedListRepository trustedListRepository;
	private final TrustedListUpdateRepository trustedListUpdateRepository;
		
	public TrustedList getListForTerritory(String territory) {
		
		List<TrustedList> lists = trustedListRepository.findByTerritory(territory);
		if(lists.size() == 0) { 
			return null;
		}
		return lists.get(0);
	}
	
	public TrustedList getListOfTheList() {
		return getListForTerritory("EU");
	}
	
	@CacheEvict(cacheNames = "certIdentities", allEntries = true)
	public TrustedList save(TrustedList trustedList) {
		log.info("TrustedList update");
		return trustedListRepository.save(trustedList);
	}

	public TrustedListUpdate save(TrustedListUpdate tlu) {
		return trustedListUpdateRepository.save(tlu);
	}

	public void markLastCheck(String territory) {
		TrustedList tl = getListForTerritory(territory);
		tl.setLastCheck(Calendar.getInstance().getTime());
		
	}
	
}
