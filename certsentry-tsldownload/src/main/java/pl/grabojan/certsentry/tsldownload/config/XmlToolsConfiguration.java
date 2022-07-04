package pl.grabojan.certsentry.tsldownload.config;


import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.xpath.XPathExpression;
import org.springframework.xml.xpath.XPathExpressionFactoryBean;

import lombok.extern.slf4j.Slf4j;
import pl.grabojan.certsentry.tsldownload.util.PooledDocumentBuilder;
import pl.grabojan.certsentry.tsldownload.util.PooledDocumentBuilderImpl;
import pl.grabojan.certsentry.tsldownload.validation.XmlFormatValidator;
import pl.grabojan.certsentry.tsldownload.validation.XmlFormatValidatorImpl;
import pl.grabojan.certsentry.tsldownload.validation.XmlSchemaValidator;
import pl.grabojan.certsentry.tsldownload.validation.XmlSchemaValidatorImpl;
import pl.grabojan.certsentry.tsldownload.validation.XmlTSLFormatValidator;
import pl.grabojan.certsentry.tsldownload.validation.XmlTSLFormatValidatorImpl;
import pl.grabojan.certsentry.tsldownload.validation.XmlTSLSignatureValidator;
import pl.grabojan.certsentry.tsldownload.validation.XmlTSLSignatureValidatorImpl;

@Slf4j
@Configuration
public class XmlToolsConfiguration {

	@Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        
        String[] packages = new String[] {
                pl.grabojan.certsentry.schema.xmldsig.ObjectFactory.class.getPackage().getName(),
                pl.grabojan.certsentry.schema.xades.ObjectFactory.class.getPackage().getName(),
                pl.grabojan.certsentry.schema.tsl.ObjectFactory.class.getPackage().getName(),
                pl.grabojan.certsentry.schema.tsladdtyp.ObjectFactory.class.getPackage().getName(),
                pl.grabojan.certsentry.schema.tslsie.ObjectFactory.class.getPackage().getName()
        };
      
        marshaller.setContextPaths(packages);

        return marshaller;
    }
	
	@Bean
	public XmlSchemaValidator xmlSchemaValidator(ResourceLoader loader) {
		
		Map<String, String> schemaResources = new HashMap<>();
		schemaResources.put("classpath:xmldsig-core-schema.xsd","http://www.w3.org/TR/2002/REC-xmldsig-core-20020212/xmldsig-core-schema.xsd");
		schemaResources.put("classpath:tsl.xsd","http://uri.etsi.org/19612/v1.2.1/tsl.xsd");
		schemaResources.put("classpath:XAdES.xsd","http://uri.etsi.org/01903/v1.3.2/XAdES.xsd");
		schemaResources.put("classpath:xml.xsd", "http://www.w3.org/2001/xml.xsd");
		
		// xmldsig-core-schema.xsd dependency
		Map<String, String> dtdResources = new HashMap<>();
		dtdResources.put("classpath:datatypes.dtd", "http://www.w3.org/2001/datatypes.dtd");
		dtdResources.put("classpath:XMLSchema.dtd", "http://www.w3.org/2001/XMLSchema.dtd");
		
		XmlSchemaValidatorImpl xsv = new XmlSchemaValidatorImpl();
		
		Map<String, Resource> resourceMapping = new HashMap<>();

		for (String schema : schemaResources.keySet()) {
			Resource resource = loader.getResource(schema);
			try {
				log.debug("Adding schema {}, resource {}", schema, resource);
				xsv.addSchema(resource.getInputStream());
				resourceMapping.put(schemaResources.get(schema), resource);
			} catch(IOException e) {
				throw new RuntimeException("Resource load failed for [" + schema + "]", e);
			}	
		}
		
		// only mapping, dont add DTD to validator as schema / will not validate
		for (String dtd : dtdResources.keySet()) {
			Resource resource = loader.getResource(dtd);
			resourceMapping.put(dtdResources.get(dtd), resource);	
		}

		
		xsv.compileSchema(resourceMapping);
		
		return xsv;
		
	}
	
	@Bean
	public XmlFormatValidator xmlFormatChecker() {
		return new XmlFormatValidatorImpl();
	}
	
	@Bean
	public Map<String,String> tslNamespace() {
		return Collections.singletonMap("tsl", "http://uri.etsi.org/02231/v2#");
	}
	
	@Bean
	public Map<String,String> xadesNamespace() {
		Map<String,String> map = new HashMap<>();
		map.put("xades", "http://uri.etsi.org/01903/v1.3.2#");
		map.put("ds", "http://www.w3.org/2000/09/xmldsig#");
		return map;
	}
	
	@Bean(name = "version")
	public XPathExpressionFactoryBean tslVersionXpathExpressionFactoryBean() {
		XPathExpressionFactoryBean xpathExpr = new XPathExpressionFactoryBean();
		xpathExpr.setExpression("/tsl:TrustServiceStatusList/tsl:SchemeInformation/tsl:TSLVersionIdentifier/text()");
		xpathExpr.setNamespaces(tslNamespace());
		
		return xpathExpr;
	}
	
	@Bean(name = "type")
	public XPathExpressionFactoryBean tslTypeXpathExpressionFactoryBean() {
		
		XPathExpressionFactoryBean xpathExpr = new XPathExpressionFactoryBean();
		xpathExpr.setExpression("/tsl:TrustServiceStatusList/tsl:SchemeInformation/tsl:TSLType/text()");
		xpathExpr.setNamespaces(tslNamespace());
		
		return xpathExpr;
	}
	
	@Bean(name = "nextUpdate")
	public XPathExpressionFactoryBean tslNextUpdateXpathExpressionFactoryBean() {
		
		XPathExpressionFactoryBean xpathExpr = new XPathExpressionFactoryBean();
		xpathExpr.setExpression("/tsl:TrustServiceStatusList/tsl:SchemeInformation/tsl:NextUpdate/tsl:dateTime/text()");
		xpathExpr.setNamespaces(tslNamespace());
		
		return xpathExpr;
	}
		
	@Bean
	public XmlTSLFormatValidator trustedListFormatChecker(@Qualifier("version") XPathExpression version,
			@Qualifier("type") XPathExpression type, @Qualifier("nextUpdate") XPathExpression nextUpdate,
			 PooledDocumentBuilder pooledDocumentBuilderProxy) {
		
			return new XmlTSLFormatValidatorImpl(version, type, nextUpdate, pooledDocumentBuilderProxy);	
	}
	
	@Bean(name = "certDigestMethod")
	public XPathExpressionFactoryBean xadesCertDigestMethodXpathExpressionFactoryBean() {
		
		XPathExpressionFactoryBean xpathExpr = new XPathExpressionFactoryBean();
		xpathExpr.setExpression("./xades:SignedSignatureProperties/xades:SigningCertificate/xades:Cert/xades:CertDigest/ds:DigestMethod/@Algorithm");
		xpathExpr.setNamespaces(xadesNamespace());
		
		return xpathExpr;
	}
	
	@Bean(name = "certDigestValue")
	public XPathExpressionFactoryBean xadesCertDigestValueXpathExpressionFactoryBean() {
		
		XPathExpressionFactoryBean xpathExpr = new XPathExpressionFactoryBean();
		xpathExpr.setExpression("./xades:SignedSignatureProperties/xades:SigningCertificate/xades:Cert/xades:CertDigest/ds:DigestValue/text()");
		xpathExpr.setNamespaces(xadesNamespace());
		
		return xpathExpr;
	}
	
	@Bean(name = "certIssuerName")
	public XPathExpressionFactoryBean xadesCertIssuerNameXpathExpressionFactoryBean() {
		
		XPathExpressionFactoryBean xpathExpr = new XPathExpressionFactoryBean();
		xpathExpr.setExpression("./xades:SignedSignatureProperties/xades:SigningCertificate/xades:Cert/xades:IssuerSerial/ds:X509IssuerName/text()");
		xpathExpr.setNamespaces(xadesNamespace());
		
		return xpathExpr;
	}
	
	@Bean(name = "certSerialNumber")
	public XPathExpressionFactoryBean xadesCertSerialNumberXpathExpressionFactoryBean() {
		
		XPathExpressionFactoryBean xpathExpr = new XPathExpressionFactoryBean();
		xpathExpr.setExpression("./xades:SignedSignatureProperties/xades:SigningCertificate/xades:Cert/xades:IssuerSerial/ds:X509SerialNumber/text()");
		xpathExpr.setNamespaces(xadesNamespace());
		
		return xpathExpr;
	}
	
	
	@Bean
	public XmlTSLSignatureValidator trustedListAuthenticator(@Qualifier("certDigestMethod") XPathExpression certDigestMethod,
			@Qualifier("certDigestValue") XPathExpression certDigestValue, @Qualifier("certIssuerName") XPathExpression certIssuerName, 
			@Qualifier("certSerialNumber") XPathExpression certSerialNumber, PooledDocumentBuilder pooledDocumentBuilderProxy ) {
		return new XmlTSLSignatureValidatorImpl(certDigestMethod, certDigestValue, certIssuerName, certSerialNumber, pooledDocumentBuilderProxy);
	}
	
	
	@Bean
	@Scope(value = "prototype")
	public PooledDocumentBuilder pooledDocumentBuilder(XmlSchemaValidator xmlSchemaValidator) {
		return new PooledDocumentBuilderImpl(xmlSchemaValidator.getSchema());
	}
	
	@Bean
	public CommonsPool2TargetSource pooledDocumentBuilderTargetSource() {
		CommonsPool2TargetSource targetSource = new CommonsPool2TargetSource();
		targetSource.setTargetBeanName("pooledDocumentBuilder");
		targetSource.setTargetClass(PooledDocumentBuilder.class);
		targetSource.setMaxSize(10);
		return targetSource;
	}
	
	@Bean
	public ProxyFactoryBean pooledDocumentBuilderProxyFactoryBean(CommonsPool2TargetSource pooledDocumentBuilderTargetSource) {
		ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
		proxyFactoryBean.setTargetSource(pooledDocumentBuilderTargetSource);
		return proxyFactoryBean;
	}
	
	@Bean
	public PooledDocumentBuilder pooledDocumentBuilderProxy(ProxyFactoryBean pooledDocumentBuilderProxyFactoryBean) {
		return (PooledDocumentBuilder) pooledDocumentBuilderProxyFactoryBean.getObject();
	}
	
	
}
