package pl.grabojan.certsentry.restapi.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CombinedAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	private final ApiKeyUserDetailsService apiKeyUserDetailsService;
	
	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		// nothing to do
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
		
		throw new UsernameNotFoundException("User [" + username + "] not found");
	}

}
