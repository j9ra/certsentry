package pl.grabojan.certsentry.restapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import pl.grabojan.certsentry.data.service.SecUserAppDataService;
import pl.grabojan.certsentry.restapi.security.ApiKeyAuthenticationFilter;
import pl.grabojan.certsentry.restapi.security.ApiKeyUserDetailsService;
import pl.grabojan.certsentry.restapi.security.CombinedAuthenticationProvider;
import pl.grabojan.certsentry.restapi.security.SecUserAppRepositoryApiKeyUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private SecUserAppDataService secUserAppDataService;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
			.antMatchers("/", "/**")
			.hasRole("USER")
		.and()
			.x509().subjectPrincipalRegex("CN=(.*?),")
		.and()
			.httpBasic()
		.and()
			.anonymous().disable()
			.formLogin().disable()
			.csrf().disable()
			.logout().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
			.exceptionHandling().defaultAuthenticationEntryPointFor(forbiddenEntryPoint(), new AntPathRequestMatcher("/**"))
		;
		
		http.apply(MyCustomDsl.customDsl());
		
	    return http.build();
	}
	
	@Bean
	public PasswordEncoder encoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public AuthenticationEntryPoint forbiddenEntryPoint() {
		return new HttpStatusEntryPoint(HttpStatus.FORBIDDEN);
	}

	@Bean
	public CombinedAuthenticationProvider authenticationProvider() {
		return new CombinedAuthenticationProvider(userDetailsService());
	}

	@Bean
	public ApiKeyUserDetailsService userDetailsService() {
		return new SecUserAppRepositoryApiKeyUserDetailsService(secUserAppDataService);
	}
	
	public static class MyCustomDsl extends AbstractHttpConfigurer<MyCustomDsl, HttpSecurity> {
	    @Override
	    public void configure(HttpSecurity http) throws Exception {
	        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
	        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(new AntPathRequestMatcher("/**"));
	        filter.setAuthenticationManager(authenticationManager);
	        http.addFilterAfter(filter, BasicAuthenticationFilter.class);
	    }

	    public static MyCustomDsl customDsl() {
	        return new MyCustomDsl();
	    }
	}
}
