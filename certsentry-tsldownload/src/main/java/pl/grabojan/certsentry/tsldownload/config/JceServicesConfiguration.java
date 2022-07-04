package pl.grabojan.certsentry.tsldownload.config;


import org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import pl.grabojan.certsentry.tsldownload.CertsentryTSLProperties;
import pl.grabojan.certsentry.tsldownload.util.KeyStoreServicesHelper;
import pl.grabojan.certsentry.util.CertificateServiceHelper;
import pl.grabojan.certsentry.util.SecurityProviderRegistrar;

@Configuration
public class JceServicesConfiguration {

	@Bean
	public KeyStoreServicesHelper keyStoreServicesHelper(CertsentryTSLProperties properties) {
		return new KeyStoreServicesHelper(properties.getAuthcerts(), properties.getAuthcertPassword());
	}
	
	@Bean(initMethod = "init")
	public SecurityProviderRegistrar securityProviderRegistrar() {
		return new SecurityProviderRegistrar(new Class<?>[] {
			BouncyCastleProvider.class, 
			XMLDSigRI.class
		});
	}
	
	@Bean
	@DependsOn("securityProviderRegistrar")
	public CertificateServiceHelper	certificateServiceHelper() {
		return new CertificateServiceHelper();
	}
	
}
