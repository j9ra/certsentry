package pl.grabojan.certsentry.data.service;

import java.util.Optional;

import javax.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import pl.grabojan.certsentry.data.model.Application;
import pl.grabojan.certsentry.data.model.SecUser;
import pl.grabojan.certsentry.data.repository.ApplicationRepository;
import pl.grabojan.certsentry.data.repository.SecUserRepository;

@Transactional
@RequiredArgsConstructor
public class SecUserAppDataService {

	private final ApplicationRepository applicationRepository;
	private final SecUserRepository secUserRepository;
	
	
	public Optional<SecUser> getUserByName(String username) {
		return secUserRepository.findByUsername(username);
	}
	
	public Optional<SecUser> getUserByApiKey(String apiKey) {
		Optional<Application> app = applicationRepository.findByApiKey(apiKey);
		if(app.isPresent()) {
			return Optional.of(app.get().getSecUser());
		}
		return Optional.empty();
	}
	
}
