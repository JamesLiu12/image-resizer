package org.example;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqsService {
    private final SqsClient sqsClient;

    public SqsService() {
        this.sqsClient = SqsClient.builder().region(Region.AP_NORTHEAST_1).build();
    }

    public String sendMessageWithId(String queueUrl, String messageId, String message) throws Exception {
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("id",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(messageId)
                        .build());

        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .messageAttributes(messageAttributes)
                .build();

        SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
        System.out.println("Message sent with ID: " + response.messageId());
        return response.messageId();
    }

    public String receiveMessageWithId(String queueUrl, String messageId) throws Exception {
        while (true) {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageAttributeNames("id")
                    .maxNumberOfMessages(10)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

            for (Message message : messages) {
                String receivedId = message.messageAttributes().get("id").stringValue();
                if (receivedId.equals(messageId)) {
                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .receiptHandle(message.receiptHandle())
                            .build());
                    return message.body();
                }
            }

            Thread.sleep(2000);
        }
    }
}
