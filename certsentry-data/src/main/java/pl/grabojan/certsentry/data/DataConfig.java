package pl.grabojan.certsentry.data;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import pl.grabojan.certsentry.data.repository.ApplicationRepository;
import pl.grabojan.certsentry.data.repository.CertIdentityRepository;
import pl.grabojan.certsentry.data.repository.EventLogRepository;
import pl.grabojan.certsentry.data.repository.ProfileRepository;
import pl.grabojan.certsentry.data.repository.SecUserRepository;
import pl.grabojan.certsentry.data.repository.TrustedListRepository;
import pl.grabojan.certsentry.data.repository.TrustedListUpdateRepository;
import pl.grabojan.certsentry.data.service.CertIdentityDataService;
import pl.grabojan.certsentry.data.service.EventLogDataService;
import pl.grabojan.certsentry.data.service.ProfileDataService;
import pl.grabojan.certsentry.data.service.SecUserAppDataService;
import pl.grabojan.certsentry.data.service.TrustedListDataService;

@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration
@EnableCaching
public class DataConfig {
	
	@Bean
	public TrustedListDataService trustedListDataService(TrustedListRepository trustedListRepository, 
			TrustedListUpdateRepository trustedListUpdateRepository) {
		return new TrustedListDataService(trustedListRepository, trustedListUpdateRepository);
	}
	
	@Bean
	public SecUserAppDataService secUserAppDataService(ApplicationRepository applicationRepository,
			SecUserRepository secUserRepository) {
		return new SecUserAppDataService(applicationRepository, secUserRepository);
	}
	
	@Bean
	public ProfileDataService profileDataService(ProfileRepository profileRepository) {
		return new ProfileDataService(profileRepository);
	}
	
	@Bean
	public CertIdentityDataService certIdentityDataService(CertIdentityRepository certIdentityRepository) {
		return new CertIdentityDataService(certIdentityRepository);
	}

	@Bean
	public EventLogDataService eventLogDataService(EventLogRepository eventLogRepository, 
			SecUserRepository secUserRepository) {
		
		return new EventLogDataService(eventLogRepository, secUserRepository);
	}
}
