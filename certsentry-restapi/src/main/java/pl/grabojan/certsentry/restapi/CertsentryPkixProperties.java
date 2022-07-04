package pl.grabojan.certsentry.restapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "certsentry.pkix")
public class CertsentryPkixProperties {

	private boolean validateOneLevel = false;
	private boolean revokePreferOCSP = true;
	private boolean revokePreferCDP = false;
	private boolean revokeOcspUseNonce = false;
	private boolean revokeUseProxy = false;
	private String revokeProxyURL = "http://localhost:8080";
	
	public boolean isValidateOneLevel() {
		return validateOneLevel;
	}
	public void setValidateOneLevel(boolean validateOneLevel) {
		this.validateOneLevel = validateOneLevel;
	}
	
	public boolean isRevokePreferOCSP() {
		return revokePreferOCSP;
	}
	public void setRevokePreferOCSP(boolean revokePreferOCSP) {
		this.revokePreferOCSP = revokePreferOCSP;
	}
	
	public boolean isRevokePreferCDP() {
		return revokePreferCDP;
	}
	public void setRevokePreferCDP(boolean revokePreferCDP) {
		this.revokePreferCDP = revokePreferCDP;
	}
	
	public boolean isRevokeOcspUseNonce() {
		return revokeOcspUseNonce;
	}
	public void setRevokeOcspUseNonce(boolean revokeOcspUseNonce) {
		this.revokeOcspUseNonce = revokeOcspUseNonce;
	}
	
	public boolean isRevokeUseProxy() {
		return revokeUseProxy;
	}
	public void setRevokeUseProxy(boolean revokeUseProxy) {
		this.revokeUseProxy = revokeUseProxy;
	}
	
	public String getRevokeProxyURL() {
		return revokeProxyURL;
	}
	public void setRevokeProxyURL(String revokeProxyURL) {
		this.revokeProxyURL = revokeProxyURL;
	}
	
}
