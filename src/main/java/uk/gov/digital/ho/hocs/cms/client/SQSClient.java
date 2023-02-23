package uk.gov.digital.ho.hocs.cms.client;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SQSClient {

    @Autowired
    public AmazonSQSAsync sqs;

    private final String queueUrl;

    private static final String MESSAGE_TYPE = "MIGRATION";

    public SQSClient(AmazonSQSAsync sqs, @Value("${aws.sqs.queue-name}")  String queueName) {
        this.sqs = sqs;
        this.queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
    }
    
    public void sendMessage(String message, String externalReference) {
        var sendMessageRequest = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(message)
                .addMessageAttributesEntry("MessageType", new com.amazonaws.services.sqs.model.MessageAttributeValue()
                        .withDataType("String")
                        .withStringValue(MESSAGE_TYPE));

        if (StringUtils.isNullOrEmpty(externalReference)) {
            sendMessageRequest.addMessageAttributesEntry("ExternalReference", new com.amazonaws.services.sqs.model.MessageAttributeValue()
                    .withDataType("String")
                    .withStringValue(externalReference));
        }


        SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);
        log.debug("Successfully sent MessageId: {} ,queueURL: {}", sendMessageResult.getMessageId(), queueUrl);
    }


}
