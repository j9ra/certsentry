package pl.grabojan.certsentry.restapi.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface ApiKeyUserDetailsService extends UserDetailsService {

	UserDetails loadUserByApiKey(String apiKey) throws UsernameNotFoundException;
}
