package org.example;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

public class AwsService {
    private final S3Client s3Client;
    private final SqsClient sqsClient;
    private final String bucketName;
    private final String inboxQueueUrl;
    private final String outboxQueueUrl;

    public AwsService(String bucketName, String inboxQueueUrl, String outboxQueueUrl) {
        this.s3Client = S3Client.builder().build();
        this.sqsClient = SqsClient.builder().build();
        this.bucketName = bucketName;
        this.inboxQueueUrl = inboxQueueUrl;
        this.outboxQueueUrl = outboxQueueUrl;
    }
}
