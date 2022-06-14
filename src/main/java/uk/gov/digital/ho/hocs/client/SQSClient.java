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

    private final AmazonSQSAsync sqs;
    private final String queueUrl;

    @Autowired
    public SQSClient(AmazonSQSAsync sqs,
                     @Value("${run.config.queue-name}")  String queueName) {
        this.sqs = sqs;
        this.queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
    }
    
    public void sendMessage(String message) {
        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(message);

        SendMessageResult sendMessageResult = sqs.sendMessage(send_msg_request);

        log.info("Successfully sent MessageId: {} ,queueURL: {}", sendMessageResult.getMessageId(), queueUrl);
    }
    public void read() {
        ReceiveMessageRequest receive_msg_request = new ReceiveMessageRequest()
                .withQueueUrl(queueUrl);

        ReceiveMessageResult receiveMessageResult = sqs.receiveMessage(receive_msg_request);

        log.info("Successfully received Message: {} ,queueURL: {}", receiveMessageResult.getMessages().get(0), queueUrl);
    }

}
