package uk.gov.digital.ho.hocs.cms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.springframework.test.util.AssertionErrors.assertTrue;

@ExtendWith(MockitoExtension.class)
class HocsCmsDataMigratorTests {

	@Test
	void contextLoads() {
		assertTrue("This will succeed.", true);
	}

}
