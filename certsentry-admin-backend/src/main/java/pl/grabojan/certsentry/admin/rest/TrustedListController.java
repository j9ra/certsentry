package pl.grabojan.certsentry.admin.rest;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.grabojan.certsentry.data.model.TrustedList;
import pl.grabojan.certsentry.data.model.TrustedListBriefDTO;
import pl.grabojan.certsentry.data.model.TrustedListExpiresDTO;
import pl.grabojan.certsentry.data.model.TrustedListUpdatesDTO;
import pl.grabojan.certsentry.data.repository.TrustedListRepository;

@RestController
@RequestMapping(path="/api/trustlists", produces = "application/json")
public class TrustedListController {

	private TrustedListRepository repository;
	
	public TrustedListController(TrustedListRepository trustedListRepository) {
		this.repository = trustedListRepository;
	}
	
	@GetMapping(path = "/brief")
	public Iterable<TrustedListBriefDTO> getListBrief() {
		return repository.findBy(TrustedListBriefDTO.class);
	}
	
	@GetMapping(path = "/updates")
	public Iterable<TrustedListUpdatesDTO> getListUpdates() {
		return repository.findByUpdates(PageRequest.of(0,5, Sort.by("lastUpdate").descending().and(Sort.by("listIssue").descending())));
	}
	
	@GetMapping(path = "/expires")
	public Iterable<TrustedListExpiresDTO> getListExpires() {
				
		return repository.findByExpires(PageRequest.of(0,5, Sort.by("nextUpdate")));
	}
	
	
	@GetMapping
	public Iterable<TrustedList> getTrustedLists() {
		return repository.findAll();
	}
	
	@GetMapping("/{territory}")
	public ResponseEntity<TrustedList> trustedListByTerritory(@PathVariable("territory") String territory) { 
		
		List<TrustedList> lists = repository.findByTerritory(territory);
		if(lists.size() == 0) {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); 
		} else {
			return new ResponseEntity<>(lists.get(0), HttpStatus.OK);
		}	
	}
	
	
	
}
