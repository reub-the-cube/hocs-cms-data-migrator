package uk.gov.digital.ho.hocs.cms.client;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.cms.payload.PayloadFile;
import uk.gov.digital.ho.hocs.cms.payload.TokenReplacer;

import java.util.Random;

import static org.apache.commons.lang3.StringUtils.replaceEach;
import static uk.gov.digital.ho.hocs.cms.payload.FileReader.getResourceFileAsString;

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

        if (StringUtils.hasText(complaintType)) {
            for (int i = 0; i < numMessages; i++) {
                String fileName = PayloadFile.valueOf(complaintType).getFileName();
                log.debug("Sending {} : {}", complaintType, fileName);
                String msg= replaceEach(getResourceFileAsString(fileName), replacer.getSearchList(), replacer.getReplaceList());
                sqsClient.sendMessage(msg);
            }
            log.debug("Successfully sent {}, {} messages.", numMessages, complaintType);
        } else {
            new Random().ints(numMessages, 1, PayloadFile.values().length).forEach((typeIndex) -> {
                String fileName = PayloadFile.values()[typeIndex].getFileName();
                log.debug("Sending Random : {}", fileName);
                sqsClient.sendMessage(replaceEach(getResourceFileAsString(fileName), replacer.getSearchList(), replacer.getReplaceList()));
            });
            log.debug("Successfully sent {}, random messages.", numMessages);
        }

    }

    @Getter
    static public class Replacer {

        private final String[] searchList = {
                "@@TODAY@@",
                "@@COMPLAINT_TEXT@@",
                "@@APPLICANT_NAME@@",
                "@@AGENT_NAME@@",
                "@@NATIONALITY@@",
                "@@COUNTRY@@",
                "@@CITY@@",
                "@@DOB@@",
                "@@APPLICANT_EMAIL@@",
                "@@AGENT_EMAIL@@",
                "@@PHONE@@",
                "@@REFERENCE@@"
        };

        public String[] getReplaceList() {
            String[] replaceList = new String[searchList.length];
            for (int i = 0; i < searchList.length; i++) {
                String token = searchList[i];
                replaceList[i] = TokenReplacer.replaceToken(token);
            }
            return replaceList;
        }
    }

}
