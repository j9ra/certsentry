package pl.grabojan.certsentry.tsldownload.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import pl.grabojan.certsentry.data.service.TrustedListDataService;
import pl.grabojan.certsentry.tsldownload.CertsentryTSLProperties;
import pl.grabojan.certsentry.tsldownload.data.TSLXmlToObjectConverter;
import pl.grabojan.certsentry.tsldownload.data.TrustedListDataMapper;
import pl.grabojan.certsentry.tsldownload.data.TrustedListDataMapperImpl;
import pl.grabojan.certsentry.tsldownload.TSLDownloadManager;
import pl.grabojan.certsentry.tsldownload.util.FileSystemLocalStore;
import pl.grabojan.certsentry.tsldownload.util.FileSystemLocalStoreImpl;
import pl.grabojan.certsentry.tsldownload.util.KeyStoreServicesHelper;
import pl.grabojan.certsentry.tsldownload.validation.TSLValidator;
import pl.grabojan.certsentry.tsldownload.validation.TSLValidatorImpl;
import pl.grabojan.certsentry.tsldownload.validation.XmlFormatValidator;
import pl.grabojan.certsentry.tsldownload.validation.XmlSchemaValidator;
import pl.grabojan.certsentry.tsldownload.validation.XmlTSLFormatValidator;
import pl.grabojan.certsentry.tsldownload.validation.XmlTSLSignatureValidator;
import pl.grabojan.certsentry.util.CertificateServiceHelper;
import pl.grabojan.certsentry.util.HttpResource;

@Configuration
public class ApplicationConfiguration {

	@Bean
	public TSLDownloadManager tslDownloadManager(CertsentryTSLProperties certsentryTSLProperties,
			HttpResource httpResource, TSLValidator tslValidator, KeyStoreServicesHelper tslAuthKeyStoreServicesHelper,
			TSLXmlToObjectConverter tslXmlToObjectConverter, FileSystemLocalStore fileSystemLocalStore, 
			TrustedListDataService trustedListDataService, TrustedListDataMapper trustedListDataMapper, 
			CertificateServiceHelper certificateServiceHelper) {
		return new TSLDownloadManager(certsentryTSLProperties, httpResource, tslValidator, tslAuthKeyStoreServicesHelper,
				tslXmlToObjectConverter, fileSystemLocalStore, trustedListDataService, trustedListDataMapper, certificateServiceHelper);
	}
	
	@Bean
	public TSLValidator tslValidator(XmlFormatValidator xmlFormatValidator, XmlSchemaValidator xmlSchemaValidator,
			XmlTSLFormatValidator xmlTSLFormatValidator, XmlTSLSignatureValidator xmlTSLSignatureValidator) {
		return new TSLValidatorImpl(xmlFormatValidator, xmlSchemaValidator, xmlTSLFormatValidator, xmlTSLSignatureValidator);
	}
	
	@Bean
	public TSLXmlToObjectConverter tslXmlToObjectConverter(Jaxb2Marshaller marshaller) {
		return new TSLXmlToObjectConverter(marshaller);
	}
	
	@Bean(initMethod = "init")
	public FileSystemLocalStore fileSystemLocalStore(CertsentryTSLProperties certsentryTSLProperties) {
		return new FileSystemLocalStoreImpl(certsentryTSLProperties.getLocalStorePath(), certsentryTSLProperties.isLocalStoreBackup());
	}
	
	@Bean
	public TrustedListDataMapper trustedListDataMapper(CertificateServiceHelper certificateServiceHelper) {
		return new TrustedListDataMapperImpl(certificateServiceHelper);
	}
	
	
}
