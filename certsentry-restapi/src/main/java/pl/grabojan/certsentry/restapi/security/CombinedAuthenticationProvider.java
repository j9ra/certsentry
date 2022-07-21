package pl.grabojan.certsentry.restapi.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CombinedAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	private final ApiKeyUserDetailsService apiKeyUserDetailsService;
	
	private final PasswordEncoder passwordEncoder;
	
	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		
		
		if (authentication.getCredentials() == null) {
			log.debug("Failed to authenticate since no credentials provided");
			throw new BadCredentialsException("Bad credentials");
		}
		String presentedPassword = authentication.getCredentials().toString();
		if (!this.passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
			log.debug("Failed to authenticate since password does not match stored value");
			throw new BadCredentialsException("Bad credentials");
		}
	}

	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {

		log.debug("retrieveUser: {}", authentication);
		if(authentication instanceof ApiKeyAuthenticationToken) {
			log.debug("Found ApiKey token!");
			
			ApiKeyAuthenticationToken authToken = (ApiKeyAuthenticationToken)authentication;			
			String token = authToken.getCredentials().toString();
			return apiKeyUserDetailsService.loadUserByApiKey(token);
		}
		
		if(authentication instanceof UsernamePasswordAuthenticationToken) {
			return apiKeyUserDetailsService.loadUserByUsername(authentication.getName());
		}
		
		log.debug("Class: {}", authentication.getClass());
		
		throw new UsernameNotFoundException("User [" + username + "] not found");
	}

}
