package uk.gov.digital.ho.hocs.cms.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseAttachment;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDataItem;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDetails;
import uk.gov.digital.ho.hocs.cms.domain.message.Correspondent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

	@Mock
	private SQSClient sqsClient;

	@Test
	public void shouldSendCountMessagesWithComplaintType() throws IOException {
		MessageService messageService = new MessageService(sqsClient);
		messageService.sendMigrationMessage(buildCaseDetails());
		verify(sqsClient, times(1)).sendMessage(anyString());
	}
	private CaseDetails buildCaseDetails() {
		CaseDataItem caseDataItem = new CaseDataItem();
		caseDataItem.setName("type");
		caseDataItem.setValue("cms");

		CaseDataItem caseDataItem1 = new CaseDataItem();
		caseDataItem1.setName("test");
		caseDataItem1.setValue("test");

		List<CaseDataItem> items = new ArrayList<>();
		items.add(caseDataItem);
		items.add(caseDataItem1);

		CaseAttachment caseAttachment = new CaseAttachment();
		caseAttachment.setDocumentType("pdf");
		caseAttachment.setDocumentPath("s3://");
		caseAttachment.setDisplayName("letter");

		Correspondent correspondent = getCorrespondent();
		List<Correspondent> additionalCorrespondents = new ArrayList<>();
		additionalCorrespondents.add(correspondent);
		List<CaseAttachment> attachments = new ArrayList<>();
		attachments.add(caseAttachment);

		CaseDetails caseDetails = CaseDetails.builder()
				.caseType("cms")
				.sourceCaseId("001")
				.primaryCorrespondent(correspondent)
				.additionalCorrespondents(additionalCorrespondents)
				.creationDate("2020-07-01")
				.caseStatus("Closed")
				.caseStatusDate("2020-07-01")
				.creationDate("2020-07-01")
				.caseData(items)
				.caseAttachments(attachments)
				.build();
		return caseDetails;
	}

	private Correspondent getCorrespondent() {
		Correspondent correspondent = new Correspondent();
		correspondent.setFullName("Full name");
		correspondent.setCorrespondentType("Correspondent Type");
		return correspondent;
	}
}
