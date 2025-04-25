package org.example;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqsService {
    private final SqsClient sqsClient;

    public SqsService() {
        this.sqsClient = SqsClient.builder().region(Region.AP_NORTHEAST_1).build();
    }

    public String sendMessage(String queueUrl, String message,
                              Map<String, MessageAttributeValue> messageAttributes) throws Exception {
        if (messageAttributes == null) {
            messageAttributes = new HashMap<>();
        }

        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .messageAttributes(messageAttributes)
                .build();

        SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
        System.out.println("Message sent with ID: " + response.messageId());
        return response.messageId();
    }

    public Message receiveMessage(String queueUrl,
                                 Map<String, MessageAttributeValue> expectedAttributes,
                                 int maxMessageNumber) throws Exception {

        List<String> attributeNames = expectedAttributes == null ? new ArrayList<>()
                : new ArrayList<>(expectedAttributes.keySet());

        while (true) {
            ReceiveMessageRequest receiveRequest = expectedAttributes == null ?
                    ReceiveMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageAttributeNames("All")
                            .maxNumberOfMessages(maxMessageNumber)
                            .build()
                    :
                    ReceiveMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageAttributeNames(attributeNames)
                            .maxNumberOfMessages(maxMessageNumber)
                            .build();

            List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

            for (Message message : messages) {
                Map<String, MessageAttributeValue> receivedAttributes = message.messageAttributes();

                boolean allMatch = expectedAttributes == null || expectedAttributes.entrySet().stream()
                        .allMatch(entry -> {
                            MessageAttributeValue receivedValue = receivedAttributes.get(entry.getKey());
                            return receivedValue != null &&
                                    receivedValue.stringValue().equals(entry.getValue().stringValue());
                        });

                if (allMatch) {
                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .receiptHandle(message.receiptHandle())
                            .build());
                    return message;
                }
            }

            Thread.sleep(2000);
        }
    }

    public Message receiveMessage(String queueUrl,
                                 Map<String, MessageAttributeValue> expectedAttributes) throws Exception {
        return receiveMessage(queueUrl, expectedAttributes, 10);
    }

    public Message receiveOneMessage(String queueUrl) throws Exception {
        return receiveMessage(queueUrl, null, 1);
    }

    public void close() {
        sqsClient.close();
    }
}
