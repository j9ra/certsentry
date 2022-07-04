package pl.grabojan.certsentry.data.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import pl.grabojan.certsentry.data.model.EventLog;

public interface EventLogRepository extends PagingAndSortingRepository<EventLog, Long> {
		
	@Query("select date_trunc('hour',timestamp) as stamp, count(*) as cnt from EventLog e where e.timestamp >= ?1 group by date_trunc('hour',timestamp)")
	List<Object[]> findByTimestampAfterAsTrend(Date lastday);
	
	@Query("select type,count(*) as cnt from EventLog e group by type order by type")
	List<Object[]> findByTypesAsDistrib();

	@Query("select e.secUser.username,count(*) as cnt from EventLog e group by e.secUser.username order by cnt desc")
	List<Object[]> findByUsersTop(Pageable pageable);
}
