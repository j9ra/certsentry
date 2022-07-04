package pl.grabojan.certsentry.tsldownload;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import pl.grabojan.certsentry.data.DataConfig;

@SpringBootApplication
@EnableConfigurationProperties
@Import(DataConfig.class)
public class CertsentryTslDownloadApplication {
		
	public static void main(String[] args) {
		SpringApplication.run(CertsentryTslDownloadApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner run(TSLDownloadManager tslDownloadManager) throws Exception {
		return args -> tslDownloadManager.runDownload();
	}

}
