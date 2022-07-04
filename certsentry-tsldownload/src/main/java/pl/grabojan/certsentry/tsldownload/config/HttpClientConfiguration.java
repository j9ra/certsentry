package pl.grabojan.certsentry.tsldownload.config;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import pl.grabojan.certsentry.util.HttpResource;

@Configuration
@ConfigurationProperties(prefix = "httpclient.config")
public class HttpClientConfiguration {
	
	private Duration connectTimeout = Duration.ofSeconds(30);
	
	private Duration requestTimeout = Duration.ofSeconds(30);
	
	private Duration socketTimeout = Duration.ofSeconds(30);
	
	private Resource trustStoreResource;
	
	private String trustStorePassword;
	
	private boolean useProxy = false;
	
	private String proxyURL = "http://localhost:8080";

		
	public void setConnectTimeout(Duration connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setRequestTimeout(Duration requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public void setSocketTimeout(Duration socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	
	public void setTrustStoreResource(Resource resource) {
		this.trustStoreResource = resource;
	}
	
	public void setTrustStorePassword(String password) {
		this.trustStorePassword = password;
	}
	
	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}

	public void setProxyURL(String proxyURL) {
		this.proxyURL = proxyURL;
	}
	
	@Bean
	public RestTemplateCustomizer restTemplateCustomizer() {
		return (rt) -> rt.setRequestFactory(clientHttpRequestFactory());
	}
	
	@Bean
	public ClientHttpRequestFactory clientHttpRequestFactory() { 
	    return new HttpComponentsClientHttpRequestFactory(httpClient());
	}
	
	@Bean
	public HttpClient httpClient() {
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout((int)connectTimeout.toMillis())
				.setConnectionRequestTimeout((int)requestTimeout.toMillis())
				.setSocketTimeout((int)socketTimeout.toMillis())
				.setProxy(useProxy ? HttpHost.create(proxyURL) : null)
				.build();
		
		SocketConfig socketConfig = SocketConfig.custom()
				.setSoTimeout((int)socketTimeout.toMillis())
				.build();
		
		return HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.setDefaultSocketConfig(socketConfig)
				.setConnectionManager(connectionManager())
				.build();
	}
	
	@Bean
	public HttpClientConnectionManager connectionManager() {
		return new PoolingHttpClientConnectionManager(socketFactoryRegistry());
	}
	
	@Bean
	public Registry<ConnectionSocketFactory> socketFactoryRegistry() {
		  Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
	                .<ConnectionSocketFactory>create()
	                .register("https", sslConnectionSocketFactory())
	                .register("http", new PlainConnectionSocketFactory())
	                .build();
		  
		  return socketFactoryRegistry;
	}
	
	@Bean
	public SSLConnectionSocketFactory sslConnectionSocketFactory() {
		
		SSLContextBuilder builder = new SSLContextBuilder();
		
		// configure trusted cas store
		if(trustStoreResource != null) {
			try {
				KeyStore ks = KeyStore.getInstance("JKS");
				ks.load(trustStoreResource.getInputStream(), trustStorePassword.toCharArray());
				builder.loadTrustMaterial(ks, null);
			} catch(IOException|CertificateException|
					KeyStoreException|NoSuchAlgorithmException e) {
				throw new IllegalStateException("Failed to initialize trustStore" 
					+ trustStoreResource, e);
			}
		}
		
		SSLConnectionSocketFactory sslsf = null;
        try {
            sslsf = new SSLConnectionSocketFactory(builder.build());
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
		
		return  sslsf;
	}
	
	@Bean
	public HttpResource httpResource(RestTemplateBuilder restTemplateBuilder) {
		return new HttpResource(restTemplateBuilder);
	}
	
}
