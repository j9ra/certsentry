package pl.grabojan.certsentry.util;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecurityProviderRegistrar {
	
	private final Class<?>[] providers;
	
	public SecurityProviderRegistrar(Class<?>[] providers) {
		this.providers = providers;
	}
	
	public void init() {
		
		for (Class<?> clazzProv : providers) {
			try {
				log.info("Adding provider: {}", clazzProv.getName());
				Provider p = (Provider)clazzProv.newInstance();
				Security.addProvider(p);
			} catch (InstantiationException|IllegalAccessException e) {
				log.error("Unable to instatiate class: " + clazzProv, e);
				throw new RuntimeException("Provider registration failed", e);
			} 
		}
				
		if(log.isDebugEnabled()) {
			Arrays.stream(Security.getProviders()).forEach(prov ->
				log.debug(" Prov: {}, version: {} info: [{}] ",
						prov.getName(), prov.getVersion(), prov.getInfo()));
		}
		
	}

}
