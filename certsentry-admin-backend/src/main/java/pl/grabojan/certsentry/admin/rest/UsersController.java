package pl.grabojan.certsentry.admin.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import pl.grabojan.certsentry.admin.rest.dto.User;
import pl.grabojan.certsentry.data.model.Application;
import pl.grabojan.certsentry.data.model.SecUser;
import pl.grabojan.certsentry.data.repository.SecUserRepository;

@RestController
@RequestMapping(path="/api/users", produces = "application/json")
public class UsersController {

	private SecUserRepository repository;
	
	private PasswordEncoder passwordEncoder;
	
	public UsersController(SecUserRepository secUserRepository, PasswordEncoder passwordEncoder) {
		this.repository = secUserRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@GetMapping
	public List<User> getUsers() {
		
		List<User> usersToRet = new ArrayList<>();
		repository.findAll().forEach(u -> usersToRet.add(toUser(u)));
		
		return usersToRet;
	}
	
	@GetMapping("/{name:.+}")
	public ResponseEntity<User> userByName(@PathVariable("name") String name) {
		
		Optional<SecUser> secUser = repository.findByUsername(name);
		if(secUser.isPresent()) {		
			return new ResponseEntity<>(secUser.map(this::toUser).get(), HttpStatus.OK);
			
		} else {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}
	
	@PostMapping(consumes = "application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public User postUser(@RequestBody User user) {
		SecUser newSecUser = repository.save(toSecUser(user));
		return toUser(newSecUser);
	}
	
	@PutMapping("/{name:.+}")
	public User putUser(@RequestBody User user) {
		SecUser newSecUser = repository.save(toSecUser(user));
		return toUser(newSecUser);
	}
	
	@PatchMapping(path="/{name:.+}", consumes = "application/json")
	public User patchUser(@PathVariable("name") String name, @RequestBody User user) {
		
		SecUser secUserPatch = toSecUser(user);
		SecUser secUser = repository.findByUsername(name).get();
		if(secUserPatch.getPassword() != null) {
			secUser.setPassword(secUserPatch.getPassword());
		}
		if(secUserPatch.getEnabled() != null) {
			secUser.setEnabled(secUserPatch.getEnabled());
		}
		if(secUserPatch.getAuthorities() != null) {
			secUser.setAuthorities(secUserPatch.getAuthorities());
		}
		if(secUserPatch.getApplication() != null) {
			Application app = secUser.getApplication();
			if(app == null) {
				app = new Application();
				secUser.setApplication(app);
			}
			if(secUserPatch.getApplication().getName() != null) {
				app.setName(secUserPatch.getApplication().getName());
			}
			if(secUserPatch.getApplication().getDescription() != null) {
				app.setDescription(secUserPatch.getApplication().getDescription());
			}
			if(secUserPatch.getApplication().getApiKey() != null) {
				app.setApiKey(secUserPatch.getApplication().getApiKey());
			}
		}
		
		SecUser secUserNew = repository.save(secUser);
		return toUser(secUserNew);
		
	}
	
	@DeleteMapping(path = "/{name:.+}")
	public ResponseEntity<User> deleteUser(@PathVariable("name") String name) {
		Optional<SecUser> secUser = repository.findByUsername(name);
		if(secUser.isPresent()) {		
			repository.delete(secUser.get());
			return new ResponseEntity<>(secUser.map(this::toUser).get(), HttpStatus.ACCEPTED);
		} else {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}
	
	private User toUser(SecUser secUser) {
		User userRet = new User();
		
		userRet.setUsername(secUser.getUsername());
		userRet.setPassword(""); // security
		userRet.setEnabled(secUser.getEnabled());
		
		userRet.setRoles(String.join(",",
				secUser.getAuthorities().stream().
				map( v -> v.replace("ROLE_", "")).
				collect(Collectors.toList())));
		
		if(secUser.getApplication() != null) {
			Application app = secUser.getApplication();
			userRet.setAppname(app.getName());
			userRet.setDescription(app.getDescription());
			userRet.setApikey(app.getApiKey());
		}
		
		return userRet;
	}
	
	private SecUser toSecUser(User user) {
		SecUser secUserRet = new SecUser();
		
		secUserRet.setUsername(user.getUsername());
		secUserRet.setEnabled(user.getEnabled());

		if(user.getPassword() != null && user.getPassword().length() > 0) {
				secUserRet.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		
		secUserRet.setAuthorities(
				Arrays.asList(user.getRoles().split(",")).
				stream().
				map(v -> "ROLE_" + v.toUpperCase()).
				collect(Collectors.toSet()));
		
		if((user.getApikey() != null && user.getApikey().length() > 0) || 
				(user.getAppname() != null && user.getAppname().length() > 0) ||
				(user.getDescription() != null && user.getDescription().length() > 0)) {
			
			Application app = new Application();
			app.setApiKey(user.getApikey());
			app.setName(user.getAppname());
			app.setDescription(user.getDescription());
			secUserRet.setApplication(app);
		}
		
		return secUserRet;
	}
	

}
