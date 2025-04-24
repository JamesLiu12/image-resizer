package org.example;

import java.util.UUID;

public class ImageResizerClient {
    private final S3DocumentService s3DocumentService;
    private final SqsService sqsService;
    private final String bucketName = "comp3358-bucket-liu-sizhe";
    private final String inboxQueueUrl = "https://sqs.ap-northeast-1.amazonaws.com/193862962632/comp3358-a4-inbox";
    private final String outboxQueueUrl = "https://sqs.ap-northeast-1.amazonaws.com/193862962632/comp3358-a4-outbox";

    public ImageResizerClient() {
        s3DocumentService = new S3DocumentService();
        sqsService = new SqsService();
    }

    public void ResizeImage(String filePath, String destinationPath) {
        String imageId = UUID.randomUUID().toString();
        String originalKey = "original/" + imageId;

        try {
            s3DocumentService.uploadFile(bucketName, originalKey, filePath);
            sqsService.sendMessageWithId(inboxQueueUrl, imageId, originalKey);
            String resizedKey = sqsService.receiveMessageWithId(outboxQueueUrl, imageId);
            s3DocumentService.downloadFile(bucketName, resizedKey, destinationPath);
        } catch (Exception e) {
            System.err.println("Error resizing image: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage : java ImageResizerClient <filePath>");
            return;
        }
        String filePath = args[0];
        ImageResizerClient imageResizerClient = new ImageResizerClient();
        String destinationPath = "resized/" + filePath.substring(filePath.lastIndexOf("/") + 1);
        imageResizerClient.ResizeImage(filePath, destinationPath);
    }

}
