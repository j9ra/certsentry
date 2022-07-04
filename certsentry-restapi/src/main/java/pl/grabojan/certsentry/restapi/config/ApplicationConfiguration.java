package pl.grabojan.certsentry.restapi.config;

import java.net.ProxySelector;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

import pl.grabojan.certsentry.data.DataConfig;
import pl.grabojan.certsentry.data.service.CertIdentityDataService;
import pl.grabojan.certsentry.restapi.CertsentryPkixProperties;
import pl.grabojan.certsentry.restapi.pkix.CertPathServiceHelper;
import pl.grabojan.certsentry.restapi.pkix.PkixURIResolver;
import pl.grabojan.certsentry.restapi.pkix.SimpleProxySelector;
import pl.grabojan.certsentry.util.CertificateServiceHelper;
import pl.grabojan.certsentry.util.HttpResource;
import pl.grabojan.certsentry.util.SecurityProviderRegistrar;

@Configuration
@Import(DataConfig.class)
public class ApplicationConfiguration {

	@Bean(initMethod = "init")
	public SecurityProviderRegistrar securityProviderRegistrar() {
		return new SecurityProviderRegistrar(new Class<?>[] {
			BouncyCastleProvider.class
		});
	}
	
	@Bean
	@DependsOn("securityProviderRegistrar")
	public CertificateServiceHelper	certificateServiceHelper() {
		return new CertificateServiceHelper();
	}
	
	@Bean
	public CertPathServiceHelper certPathServiceHelper(CertIdentityDataService certIdentityDataService, 
			PkixURIResolver pkixURIResolver, CertsentryPkixProperties properties) {
		
		if(properties.isRevokeUseProxy()) {
			SimpleProxySelector ps = new SimpleProxySelector(properties.getRevokeProxyURL());
			ProxySelector.setDefault(ps);
		}
		
		CertPathServiceHelper helper = new CertPathServiceHelper(certificateServiceHelper(),
				certIdentityDataService, pkixURIResolver);
		helper.setOneLevel(properties.isValidateOneLevel());
		helper.setRevokePreferOCSP(properties.isRevokePreferOCSP());
		helper.setRevokePreferCDP(properties.isRevokePreferCDP());
		helper.setRevokeOcspUseNonce(properties.isRevokeOcspUseNonce());
		return helper;
	}

	@Bean
	public HttpResource httpResource(RestTemplateBuilder restTemplateBuilder) {
		return new HttpResource(restTemplateBuilder);
	}
	
	@Bean
	public PkixURIResolver pkixURIResolver(HttpResource httpResource) {
		return new PkixURIResolver(httpResource, certificateServiceHelper());
	}
	

}
