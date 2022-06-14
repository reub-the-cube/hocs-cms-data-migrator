package uk.gov.digital.ho.hocs.cms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import uk.gov.digital.ho.hocs.client.MessageService;

@SpringBootApplication
@ComponentScan(basePackages = {"com.amazonaws.services.sqs", "uk.gov.digital.ho.hocs"})
@Slf4j
public class HocsCmsDataMigrator implements CommandLineRunner {

	private final MessageService messageService;

	public HocsCmsDataMigrator(MessageService messageService) {
		this.messageService = messageService;
	}

	@Override
	public void run(String... args) {
		messageService.startSending();
	}

	public static void main(String[] args) {
		SpringApplication.run(uk.gov.digital.ho.hocs.cms.HocsCmsDataMigrator.class, args);
	}
}