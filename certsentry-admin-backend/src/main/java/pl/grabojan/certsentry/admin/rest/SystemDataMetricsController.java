package pl.grabojan.certsentry.admin.rest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.grabojan.certsentry.admin.rest.dto.GraphData;
import pl.grabojan.certsentry.admin.rest.dto.Metrics;
import pl.grabojan.certsentry.data.repository.EventLogRepository;
import pl.grabojan.certsentry.data.repository.ProfileRepository;
import pl.grabojan.certsentry.data.repository.SecUserRepository;
import pl.grabojan.certsentry.data.repository.TrustedListRepository;

@RestController
@RequestMapping(path="/api/system", produces = "application/json")
public class SystemDataMetricsController {

	private SecUserRepository secUserRepository;
	private ProfileRepository profileRepository;
	private TrustedListRepository trustedListRepository;
	private EventLogRepository eventLogRepository;
	
	public SystemDataMetricsController(SecUserRepository secUserRepository,
			ProfileRepository profileRepository,
			TrustedListRepository trustedListRepository,
			EventLogRepository eventLogRepository) {
		
		this.secUserRepository = secUserRepository;
		this.profileRepository = profileRepository;
		this.trustedListRepository = trustedListRepository;
		this.eventLogRepository = eventLogRepository;
	}
	
	@GetMapping("/metrics")
	public Metrics metrics() {
		
		Metrics m = Metrics.createDefault();
		m.setUsers(secUserRepository.count());
		m.setProfiles(profileRepository.count());
		m.setTrustLists(trustedListRepository.count());
		m.setEvents(eventLogRepository.count());
		return m;
	}

	@GetMapping("/validationTrends")
	public GraphData<String,Long> validationTrendsGraph() {		
		
		List<Integer> hours = new ArrayList<>(24);
		Calendar now = Calendar.getInstance();
		for(int i=0;i<24;++i) {
			hours.add(now.get(Calendar.HOUR_OF_DAY));
			now.add(Calendar.HOUR_OF_DAY, -1);
		}
		Collections.reverse(hours);
		
		Calendar dayBefore = Calendar.getInstance();
		dayBefore.add(Calendar.DAY_OF_MONTH, -1);
		
		List<Object[]> ret = eventLogRepository.findByTimestampAfterAsTrend(dayBefore.getTime());
		Map<Integer, Long> values = new HashMap<>();
		for (Object[] objects : ret) {
			values.put(toDateHours(objects[0]), (Long)objects[1]);
		}
		
		GraphData<String, Long> retGraph = new GraphData<>();
		hours.forEach( h -> {
			retGraph.addLabelValue(String.format("%02d:00", h), 
					(values.containsKey(h) ? values.get(h) : 0l));	
		});
		
		return retGraph;
	}
	
	@GetMapping("/eventStats")
	public GraphData<String,Long> eventStatsGraph() {
	
		List<Object[]> ret = eventLogRepository.findByTypesAsDistrib();
		GraphData<String, Long> retGraph = new GraphData<>();
		ret.forEach(t -> {
			retGraph.addLabelValue(t[0].toString(),(Long)t[1]);
		});
		return retGraph;
	}
	
	@GetMapping("/appsTop")
	public GraphData<String,Long> topAppsList() {
		
		List<Object[]> ret = eventLogRepository.findByUsersTop(PageRequest.of(0,5));
		GraphData<String, Long> retGraph = new GraphData<>();
		ret.forEach(t -> {
			retGraph.addLabelValue(t[0].toString(),(Long)t[1]);
		});
		return retGraph;
	}
	
	private Integer toDateHours(Object o) {
		if(o instanceof Date) {
			Date d = (Date)o;
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			return cal.get(Calendar.HOUR_OF_DAY);
		}
		return -1;
	}
}
