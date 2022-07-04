package pl.grabojan.certsentry.admin.rest;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pl.grabojan.certsentry.data.model.Profile;
import pl.grabojan.certsentry.data.repository.ProfileRepository;

@RestController
@RequestMapping(path="/api/profiles", produces = "application/json")
public class ProfilesController {

	private ProfileRepository repository;
	
	public ProfilesController(ProfileRepository profileRepository) {
		this.repository = profileRepository;
	}
	
	@GetMapping
	public Iterable<Profile> getProfiles() {
		return repository.findAll();
	}
	
	@GetMapping("/{name}")
	public ResponseEntity<Profile> profileByName(@PathVariable("name") String name) {
		
		Optional<Profile> profile = repository.findByName(name);
		if(profile.isPresent()) {		
			return new ResponseEntity<>(profile.get(), HttpStatus.OK);
			
		} else {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}
	
	@PostMapping(consumes = "application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public Profile postProfile(@RequestBody Profile profile) {
		return repository.save(profile);
	}

	@PutMapping("/{name}")
	public Profile putProfile(@RequestBody Profile profile) {
		return repository.save(profile);
	}
	
	@PatchMapping(path="/{name}", consumes = "application/json")
	public Profile patchProfile(@PathVariable("name") String name, @RequestBody Profile profilePatch) {
		
		Profile profile = repository.findByName(name).get();
		if(profilePatch.getProvider() != null) {
			profile.setProvider(profilePatch.getProvider());
		}
		if(profilePatch.getTerritory() != null) {
			profile.setTerritory(profilePatch.getTerritory());
		}
		if(profilePatch.getServiceInfo() != null) {
			profile.setServiceInfo(profilePatch.getServiceInfo());
		}
		return repository.save(profile);
	}
	
	@DeleteMapping(path = "/{name}")
	public ResponseEntity<Profile> deleteProfile(@PathVariable("name") String name) {
		Optional<Profile> profile = repository.findByName(name);
		if(profile.isPresent()) {		
			repository.delete(profile.get());
			return new ResponseEntity<>(profile.get(), HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}
}
