package pl.grabojan.certsentry.restapi.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiKeyAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	@Setter
	private String headerName = "X-API-KEY";
	
	public ApiKeyAuthenticationFilter(RequestMatcher requiresAuthMatcher) {
		super(requiresAuthMatcher);
	}
		
	@Override
	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
		return (super.requiresAuthentication(request, response) 
				&& request.getHeader(headerName) != null);
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		
		String xApiKey = request.getHeader(headerName);
				
		log.debug("Got header {} value: [{}]", headerName, xApiKey);
		if(xApiKey != null) {
			Authentication requestAuthentication = new ApiKeyAuthenticationToken(xApiKey, xApiKey);
			return getAuthenticationManager().authenticate(requestAuthentication);
		}
		
		// fallback
		return null;
	}
	
	@Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, 
    		final FilterChain chain, final Authentication authResult) throws IOException, ServletException {
		log.debug("Successful Auth: {}", authResult);
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }

}
