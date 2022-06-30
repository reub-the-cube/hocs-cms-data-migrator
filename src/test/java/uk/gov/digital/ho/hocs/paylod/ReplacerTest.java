package uk.gov.digital.ho.hocs.paylod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.digital.ho.hocs.client.MessageService;
import uk.gov.digital.ho.hocs.payload.PayloadFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.replaceEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.digital.ho.hocs.payload.FileReader.getResourceFileAsString;

@ExtendWith(MockitoExtension.class)
class ReplacerTest {
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);

        private final MessageService.Replacer replacer = new MessageService.Replacer();

        @Test
        public void shouldCreateValidJson() throws IOException {
            InputStream schemaStream = IOUtils.toInputStream(getResourceFileAsString("cmsSchema.json"), StandardCharsets.UTF_8);
            for (PayloadFile payloadFile : PayloadFile.values()) {
                String newJson = replaceEach(getResourceFileAsString(payloadFile.getFileName()), replacer.getSearchList(), replacer.getReplaceList());
                InputStream jsonStream = IOUtils.toInputStream(newJson, StandardCharsets.UTF_8);
                assertTrue(isValidJson(schemaStream, jsonStream));
            }
        }

        @Test
        public void shouldReplaceAllTokens() {
            for (PayloadFile payloadFile : PayloadFile.values()) {
                assertFalse(containsToken(replaceEach(getResourceFileAsString(payloadFile.getFileName()), replacer.getSearchList(), replacer.getReplaceList())));
            }
        }

        @Test
        public void shouldFailSchemaValidation() throws IOException {
            String newJson = replaceEach(getResourceFileAsString("incorrectDateFormat.json"), replacer.getSearchList(), replacer.getReplaceList());
            InputStream schemaStream = IOUtils.toInputStream(getResourceFileAsString("cmsSchema.json"), StandardCharsets.UTF_8);
            InputStream jsonStream = IOUtils.toInputStream(newJson, StandardCharsets.UTF_8);
            assertFalse(isValidJson(schemaStream, jsonStream));
        }

        @Test
        public void shouldFailIfTokenPresent() {
            assertTrue(containsToken(replaceEach(getResourceFileAsString("missedToken.json"), replacer.getSearchList(), replacer.getReplaceList())));
        }

        private boolean containsToken(String convertedJson) {
            return Pattern.compile("@@.*@@").matcher(convertedJson).find();
        }

        private boolean isValidJson(InputStream schemaStream, InputStream jsonStream) throws IOException {
            byte[] jsonBytes = jsonStream.readAllBytes();
            JsonNode json = objectMapper.readTree(jsonBytes);
            JsonSchema schema = schemaFactory.getSchema(schemaStream);
            Set<ValidationMessage> validationResult = schema.validate(json);
            if (validationResult.isEmpty()) {
                return jsonBytes.length < 256000;
            } else {
                return false;
            }
        }
    }

