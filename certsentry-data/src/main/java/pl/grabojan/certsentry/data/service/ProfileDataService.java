package pl.grabojan.certsentry.data.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import pl.grabojan.certsentry.data.model.Profile;
import pl.grabojan.certsentry.data.repository.ProfileRepository;

@Transactional
@RequiredArgsConstructor
public class ProfileDataService {
	
	private final ProfileRepository profileRepository;
	
	
	public List<Profile> getAllProfiles() {
		
		List<Profile> profiles = new ArrayList<Profile>();
		
		profileRepository.findAll().forEach(p -> profiles.add(p));
		
		return profiles;
	}
	
	public Optional<Profile> getProfile(String name) {
		
		return profileRepository.findByName(name);
	}

}
