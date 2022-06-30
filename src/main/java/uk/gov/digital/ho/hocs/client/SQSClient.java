package uk.gov.digital.ho.hocs.client;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
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

    public SQSClient(AmazonSQSAsync sqs,
                     @Value("${aws.sqs.queue-name}")  String queueName) {
        this.sqs = sqs;
        this.queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
    }
    
    public void sendMessage(String message) {
        SendMessageResult sendMessageResult = sqs.sendMessage(queueUrl,message);
        log.debug("Successfully sent MessageId: {} ,queueURL: {}", sendMessageResult.getMessageId(), queueUrl);
    }


}
