package pl.grabojan.certsentry.admin.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import lombok.RequiredArgsConstructor;
import pl.grabojan.certsentry.data.model.SecUser;
import pl.grabojan.certsentry.data.service.SecUserAppDataService;

@RequiredArgsConstructor
public class SecUserDetailsService implements UserDetailsService {

	private final SecUserAppDataService secUserAppDataService;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		Optional<SecUser> secUser = secUserAppDataService.getUserByName(username);
		if(secUser.isPresent()) {
			return new SimpleUserDetails(secUser.get());
		}
		
		throw new UsernameNotFoundException("User [" + username + "] not found");
	}
	
	private static class SimpleUserDetails implements UserDetails {
		
		private static final long serialVersionUID = 1L;
		
		private final SecUser secUser;

		SimpleUserDetails(SecUser secUser) {
			this.secUser = secUser;
		}
		
		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			
			List<SimpleGrantedAuthority> authoritiesRet = new ArrayList<SimpleGrantedAuthority>();
			Set<String> roles = secUser.getAuthorities();
			roles.forEach(r ->authoritiesRet.add(new SimpleGrantedAuthority(r)));
				
			return authoritiesRet;
		}

		@Override
		public String getPassword() {
			return secUser.getPassword();
		}

		@Override
		public String getUsername() {
			return secUser.getUsername();
		}

		@Override
		public boolean isAccountNonExpired() {
			return true;
		}

		@Override
		public boolean isAccountNonLocked() {
			return true;
		}

		@Override
		public boolean isCredentialsNonExpired() {
			return true;
		}

		@Override
		public boolean isEnabled() {
			return secUser.getEnabled();
		}
		
	}
}
