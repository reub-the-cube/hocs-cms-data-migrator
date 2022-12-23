package uk.gov.digital.ho.hocs.cms.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.cms.payload.PayloadFile;

@Service
@Slf4j
public class MessageService {

    private final SQSClient sqsClient;
    private final int numMessages;
    private final String complaintType;

    public MessageService(SQSClient sqsClient,
                          @Value("${run.config.num-messages}") int numMessages,
                          @Value("${run.config.complaint-type}")  String complaintType) {
        this.sqsClient = sqsClient;
        this.numMessages = numMessages;
        this.complaintType = complaintType;
    }

    public void sendMessage() {

        if (StringUtils.hasText(complaintType)) {
            for (int i = 0; i < numMessages; i++) {
                String fileName = PayloadFile.valueOf(complaintType).getFileName();
                log.debug("Sending {} : {}", complaintType, fileName);
                String msg= "message";
                sqsClient.sendMessage(msg);
            }
            log.debug("Successfully sent {}, {} messages.", numMessages, complaintType);
        }

    }
}
