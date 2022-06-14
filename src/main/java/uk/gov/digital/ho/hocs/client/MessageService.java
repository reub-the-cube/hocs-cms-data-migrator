package uk.gov.digital.ho.hocs.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.client.SQSClient;
import uk.gov.digital.ho.hocs.payload.PayloadFile;
import uk.gov.digital.ho.hocs.payload.Replacer;

import java.util.Random;

import static org.apache.commons.lang3.StringUtils.replaceEach;
import static uk.gov.digital.ho.hocs.payload.FileReader.getResourceFileAsString;

@Service
@Slf4j
public class MessageService {

    private final SQSClient sqsClient;
    private final int numMessages;
    private final String complaintType;
    private final Replacer replacer = new Replacer();

    public MessageService(SQSClient sqsClient,
                          @Value("${run.config.num-messages}") int numMessages,
                          @Value("${run.config.complaint-type}")  String complaintType) {
        this.sqsClient = sqsClient;
        this.numMessages = numMessages;
        this.complaintType = complaintType;
    }

    public void startSending() {

        if (!StringUtils.isEmpty(complaintType)) {
            for (int i = 0; i < numMessages; i++) {
                String fileName = PayloadFile.valueOf(complaintType).getFileName();
                log.info("Sending {} : {}", complaintType, fileName);
//                sqsClient.sendMessage("Test Message");
                String msg= replaceEach(getResourceFileAsString(fileName), replacer.getSearchList(), replacer.getReplaceList());
                sqsClient.sendMessage(msg);
            }
            log.info("Successfully sent {}, {} messages.", numMessages, complaintType);
        } else {
            new Random().ints(numMessages, 1, PayloadFile.values().length).forEach((typeIndex) -> {
                String fileName = PayloadFile.values()[typeIndex].getFileName();
                log.info("Sending Random : {}", fileName);
                sqsClient.sendMessage(replaceEach(getResourceFileAsString(fileName), replacer.getSearchList(), replacer.getReplaceList()));
            });
            log.info("Successfully sent {}, random messages.", numMessages);
        }

        sqsClient.read();
    }
}
