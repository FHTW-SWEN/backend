package tourplanner.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tourplanner.backend.persistence.repository.TourLogRepository;
import tourplanner.backend.persistence.repository.TourPhotoRepository;
import tourplanner.backend.persistence.repository.TourRepository;
import tourplanner.backend.persistence.repository.UserRepository;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class BackendApplicationTests {

	@MockitoBean
	private TourRepository tourRepository;

	@MockitoBean
	private TourLogRepository tourLogRepository;

	@MockitoBean
	private TourPhotoRepository tourPhotoRepository;

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

}
