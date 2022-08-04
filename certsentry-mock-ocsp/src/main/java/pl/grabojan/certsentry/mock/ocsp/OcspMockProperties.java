package pl.grabojan.certsentry.mock.ocsp;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mock.ocsp")
public class OcspMockProperties {

	private Duration delay = Duration.ofMillis(1L);

	public Duration getDelay() {
		return delay;
	}

	public void setDelay(Duration delay) {
		this.delay = delay;
	}
	
	
	
}
