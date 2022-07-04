package pl.grabojan.certsentry.tsldownload.util;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeyStoreServicesHelper {

	private String keystoreType;
	private String keystoreProviderName;
	private KeyStore keystore;
	
	
	public KeyStoreServicesHelper(Resource keystoreResource, String keystorePassword) {
		this.keystoreType = "JKS";
		this.keystore = openKeystore(keystoreResource, keystorePassword);
	}
	
	public KeyStoreServicesHelper(Resource keystoreResource, String keystorePassword, String keystoreType) {
		this.keystoreType = keystoreType;
		this.keystore = openKeystore(keystoreResource, keystorePassword);
	}
	
	public KeyStoreServicesHelper(Resource keystoreResource, String keystorePassword, String keystoreType, String keystoreProviderName) {
		this.keystoreType = keystoreType;
		this.keystoreProviderName = keystoreProviderName;
		this.keystore = openKeystore(keystoreResource, keystorePassword);
	}
	
	public List<X509Certificate> getTrustedCerts() {
				
		List<X509Certificate> toRet = new ArrayList<>();
		try {
			Enumeration<String> aliases = keystore.aliases();
			
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				if(keystore.isCertificateEntry(alias)) {
					X509Certificate cert = (X509Certificate)keystore.getCertificate(alias);
					toRet.add(cert);
				}
			}
			
		} catch (KeyStoreException e) {
			String msg = "KeyStore content list failed";
			log.error(msg, e);
			throw new RuntimeException(msg,e);
		}
				
		return toRet;
	}
	
	private KeyStore openKeystore(Resource resource, String password) {
		try {
			KeyStore keystore = (keystoreProviderName != null) ? 
					KeyStore.getInstance(keystoreType, keystoreProviderName):
						KeyStore.getInstance(keystoreType);
			keystore.load(resource.getInputStream(),password.toCharArray());
			return keystore;
		} catch (KeyStoreException e) {
			log.error("KeyStore getInstance failed", e);
			throw new RuntimeException("Unable to instantiate KeyStore", e);
		} catch (NoSuchAlgorithmException|CertificateException|NoSuchProviderException|IOException e) {
			log.error("Keystore load failed", e);
			throw new RuntimeException("Unable to load KeyStore", e);
		} 
	}
	
}
