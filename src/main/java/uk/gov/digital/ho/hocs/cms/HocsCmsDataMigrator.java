package uk.gov.digital.ho.hocs.cms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.digital.ho.hocs.client.MessageService;



@SpringBootApplication
public class HocsCmsDataMigrator {

	public static void main(String[] args) {
		SpringApplication.run(HocsCmsDataMigrator.class, args);
	}

}
