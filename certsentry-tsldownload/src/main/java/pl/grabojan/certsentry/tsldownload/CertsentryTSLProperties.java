package pl.grabojan.certsentry.tsldownload;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "certsentry.tsl")
public class CertsentryTSLProperties {
	
	private String location;
	
	private String localStorePath;
	
	private boolean localStoreBackup;
	
	private Resource authcerts;
	
	private String authcertPassword;
	
	public String getLocation() {
		return this.location;
	}	
	public void setLocation(String l) {
		this.location = l;
	}
		
	public String getLocalStorePath() {
		return localStorePath;
	}
	public void setLocalStorePath(String localStorePath) {
		this.localStorePath = localStorePath;
	}
		
	public boolean isLocalStoreBackup() {
		return localStoreBackup;
	}
	public void setLocalStoreBackup(boolean localStoreBackup) {
		this.localStoreBackup = localStoreBackup;
	}
	
	public Resource getAuthcerts() {
		return authcerts;
	}
	public void setAuthcerts(Resource authcerts) {
		this.authcerts = authcerts;
	}
	
	public String getAuthcertPassword() {
		return authcertPassword;
	}
	public void setAuthcertPassword(String authcertPassword) {
		this.authcertPassword = authcertPassword;
	}
	
	
	
	

}
