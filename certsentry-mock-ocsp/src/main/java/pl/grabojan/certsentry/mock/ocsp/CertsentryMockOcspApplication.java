package pl.grabojan.certsentry.mock.ocsp;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import pl.grabojan.certsentry.util.CertificateServiceHelper;
import pl.grabojan.certsentry.util.HttpResource;
import pl.grabojan.certsentry.util.OcspClientService;
import pl.grabojan.certsentry.util.SecurityProviderRegistrar;


@SpringBootApplication
public class CertsentryMockOcspApplication {

	public static void main(String[] args) {
		SpringApplication.run(CertsentryMockOcspApplication.class, args);
	}
	
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
	public HttpResource httpResource(RestTemplateBuilder restTemplateBuilder) {
		return new HttpResource(restTemplateBuilder);
	}
	
	@Bean
	public OcspClientService ocspClientService() {
		return new OcspClientService();
	}
	
}
