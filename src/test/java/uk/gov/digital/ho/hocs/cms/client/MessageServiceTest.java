package uk.gov.digital.ho.hocs.cms.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.digital.ho.hocs.cms.client.MessageService;
import uk.gov.digital.ho.hocs.cms.client.SQSClient;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

	@Mock
	private SQSClient sqsClient;

	@Test
	public void shouldSendCountMessagesWithComplaintType() {
		int numMessages = 2;
		MessageService messageService = new MessageService(sqsClient, numMessages, "BIOMETRIC");
		messageService.startSending();
		verify(sqsClient, times(numMessages)).sendMessage(anyString());
	}

	@Test
	public void shouldSendCountMessagesWithoutComplaintType() {
		int numMessages = 2;
		MessageService messageService = new MessageService(sqsClient, numMessages, "");
		messageService.startSending();
		verify(sqsClient, times(numMessages)).sendMessage(anyString());
	}
}
