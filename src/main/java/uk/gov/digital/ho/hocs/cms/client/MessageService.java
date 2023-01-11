package uk.gov.digital.ho.hocs.cms.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDetails;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.MIGRATION_MESSAGE_FAILED;

@Service
@Slf4j
public class MessageService {

    private final SQSClient sqsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);

    public MessageService(SQSClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void sendMigrationMessage(CaseDetails caseDetails) {
        String message = null;
        try {
            message = validateMigrationMessage(caseDetails);
        } catch(IOException e) {
            log.error("Failed sending migration message for case ID {}", caseDetails.getSourceCaseId());
            throw new ApplicationExceptions.SendMigrationMessageException(
                    String.format("Failed sending migration message for case ID %s", caseDetails.getSourceCaseId()),  MIGRATION_MESSAGE_FAILED, e);
        }
        log.debug("Sending {}", message);
        sqsClient.sendMessage(message);
        log.debug("Successfully sent message");
    }

    private String validateMigrationMessage(CaseDetails caseDetails) throws IOException {
        InputStream schemaStream = new ClassPathResource("hocs-migration-schema.json").getInputStream();
        JsonSchema schema = schemaFactory.getSchema(schemaStream);
        String migrationMessage = objectMapper.writeValueAsString(caseDetails);
        JsonNode migrationJsonNode = objectMapper.readTree(migrationMessage);
        Set<ValidationMessage> validationResult = schema.validate(migrationJsonNode);
        if (!validationResult.isEmpty()) {
            for (ValidationMessage validationMessage : validationResult) {
                log.error(validationMessage.getMessage());
            }
            throw new ApplicationExceptions.SendMigrationMessageException(
                    String.format("Migration message failed validation for case ID %s", caseDetails.getSourceCaseId()),  MIGRATION_MESSAGE_FAILED);
        }
        return migrationMessage;
    }
}
