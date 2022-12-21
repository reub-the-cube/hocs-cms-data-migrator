package uk.gov.digital.ho.hocs.cms.domain.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import uk.gov.digital.ho.hocs.cms.client.SQSClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CaseMigrationMessageTest {

    @Mock
    private SQSClient sqsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);

    @Test
    public void testMigrationMessage() throws IOException {
        CaseDetails caseMigration = buildCaseDetails();
        InputStream schemaStream = new ClassPathResource("hocs-migration-schema.json").getInputStream();
        JsonSchema schema = schemaFactory.getSchema(schemaStream);
        String migrationMessage = objectMapper.writeValueAsString(caseMigration);
        System.out.println(migrationMessage);
        JsonNode migrationJsonNode = objectMapper.readTree(migrationMessage);
        Set<ValidationMessage> validationResult = schema.validate(migrationJsonNode);
        if (validationResult.isEmpty()) {
            sqsClient.sendMessage(migrationMessage);
            verify(sqsClient, times(1)).sendMessage(migrationMessage);
        } else {
            for (ValidationMessage validationMessage : validationResult) {
                System.out.println(validationMessage.getMessage());
            }
            fail();
        }
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
        //List<CaseAttachment> attachments = Arrays.asList(caseAttachment);
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

    private static InputStream inputStreamFromClasspath(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }
}

