package pl.grabojan.certsentry.admin.rest;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.grabojan.certsentry.data.model.EventLog;
import pl.grabojan.certsentry.data.repository.EventLogRepository;

@RestController
@RequestMapping(path="/api/events", produces = "application/json")
public class EventsController {

	private EventLogRepository repository;
	
	public EventsController(EventLogRepository eventLogRepository) {
		this.repository = eventLogRepository;
	}

	@GetMapping
	public Iterable<EventLog> getProfiles() {
		PageRequest page = PageRequest.of(
				0, 200, Sort.by("timestamp").descending());
		return repository.findAll(page).getContent();
	}
}
