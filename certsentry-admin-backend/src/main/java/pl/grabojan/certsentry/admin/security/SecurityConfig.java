package pl.grabojan.certsentry.admin.security;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import pl.grabojan.certsentry.data.service.SecUserAppDataService;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private SecUserAppDataService secUserAppDataService;
	
	@Autowired
	private Environment env;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception {
			
			boolean initMode = env.getProperty("certsentry.admin.init", Boolean.class, Boolean.FALSE);
		
			if(initMode) {
				String genpass = UUID.randomUUID().toString();
				System.out.println("------------------------------");
				System.out.println("ADMIN init mode ENABLED");
				System.out.println("Please login with credentials: user=admin, password=" + genpass);
				System.out.println("------------------------------");
				auth.inMemoryAuthentication().withUser("admin").password(encoder().encode(genpass)).roles("ADMIN");
			} else {			
				auth
				.userDetailsService(userDetailsService())
				.passwordEncoder(encoder());	
			}
		
	}
		
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
			.antMatchers("/api", "/api/**")
			.hasRole("ADMIN")
		.and()
			.httpBasic().authenticationEntryPoint(forbiddenEntryPoint())
		.and()
			.anonymous().disable()
			.formLogin().disable()
			.csrf().disable()
			.logout().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		.and()
			.exceptionHandling().defaultAuthenticationEntryPointFor(forbiddenEntryPoint(), new AntPathRequestMatcher("/api/**"))
			;
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
	public UserDetailsService userDetailsService() {
		return new SecUserDetailsService(secUserAppDataService);
	}
}
