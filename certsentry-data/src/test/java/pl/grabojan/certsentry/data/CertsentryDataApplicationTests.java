package pl.grabojan.certsentry.data;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;

import pl.grabojan.certsentry.data.model.Profile;
import pl.grabojan.certsentry.data.model.Provider;
import pl.grabojan.certsentry.data.repository.ProfileRepository;
import pl.grabojan.certsentry.data.repository.ProviderRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace=Replace.NONE)
@ContextConfiguration(classes = DataConfig.class)
class CertsentryDataApplicationTests {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private ProviderRepository providerRepository;
	
	@Autowired
	private ProfileRepository profileRepository;
	
	
	@Test
	void contextLoads() {
	}
	
	@Test
	@Commit
	void testProviderFindByName() {
		
		Provider provider = new Provider();
		provider.setName("Test Provider");
		provider.setTradeName("NIP:111-11-11-111");
		provider.setInformationUri("http://somesite/somepage/info.html");
				
		Provider provider2 = entityManager.persist(provider);
		
		List<Provider> provs = providerRepository.findByName("Test Provider");
		
		Assertions.assertEquals(1, provs.size());
		Assertions.assertNotNull(provider2.getId());
	}
	
	@Test
	@Commit
	void testProfileFindByName() {
		Profile p1 = new Profile();
		p1.setName("test1");
		p1.setTerritory("PL");
		entityManager.persist(p1);
		
		Optional<Profile> p2 = profileRepository.findByName("test1");
		Assertions.assertTrue(p2.isPresent());
		
	}

}
