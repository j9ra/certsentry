package pl.grabojan.certsentry.data.service;

import java.util.Calendar;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.grabojan.certsentry.data.model.EventLog;
import pl.grabojan.certsentry.data.model.EventType;
import pl.grabojan.certsentry.data.model.SecUser;
import pl.grabojan.certsentry.data.repository.EventLogRepository;
import pl.grabojan.certsentry.data.repository.SecUserRepository;


@Transactional
@RequiredArgsConstructor
@Slf4j
public class EventLogDataService {

	private final EventLogRepository eventLogRepository;
	private final SecUserRepository secUserRepository;
	
	
	public void logEvent(EventType eventType, String eventSource,
			String messageDescription, String usernameContext) {
		
		EventLog el = new EventLog();
		el.setType(eventType);
		el.setSource(eventSource);
		el.setDescription(messageDescription);
		el.setTimestamp(Calendar.getInstance().getTime());
		
		if(usernameContext != null) {
			Optional<SecUser> secUser = secUserRepository.findByUsername(usernameContext);
			if(secUser.isPresent()) {
				el.setSecUser(secUser.get());
			}
		}
	
		log.debug("Saving EventLog {}", el);
		eventLogRepository.save(el);
	}
	
	public void logInfoEvent(String eventSource, String messageDescription, String usernameContext) {
		logEvent(EventType.INFO, eventSource, messageDescription, usernameContext);
	}
	
	public void logNoticeEvent(String eventSource, String messageDescription, String usernameContext) {
		logEvent(EventType.NOTICE, eventSource, messageDescription, usernameContext);
	}
	
	public void logWarnEvent(String eventSource, String messageDescription, String usernameContext) {
		logEvent(EventType.WARN, eventSource, messageDescription, usernameContext);
	}
	
	public void logErrorEvent(String eventSource, String messageDescription, String usernameContext) {
		logEvent(EventType.ERROR, eventSource, messageDescription, usernameContext);
	}
	
}
