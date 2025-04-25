package org.example;

import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ImageResizerServer {
    private final S3DocumentService s3DocumentService;
    private final SqsService sqsService;
    private final String bucketName = "comp3358-bucket-liu-sizhe";
    private final String inboxQueueUrl = "https://sqs.ap-northeast-1.amazonaws.com/193862962632/comp3358-a4-inbox";
    private final String outboxQueueUrl = "https://sqs.ap-northeast-1.amazonaws.com/193862962632/comp3358-a4-outbox";

    public ImageResizerServer() {
        s3DocumentService = new S3DocumentService();
        sqsService = new SqsService();
    }

    public void processImage() {
        try {
            Message message = sqsService.receiveOneMessage(inboxQueueUrl);
            String originalKey = message.body();
            String imageId = message.messageAttributes().get("id").stringValue();
            int scalePercentage = Integer.parseInt(message.messageAttributes().get("scale").stringValue());
            String fileName = message.messageAttributes().get("fileName").stringValue();
            String resizedKey = "resized/" + imageId + "_" + fileName;

            Path inputPath = Files.createTempFile("input-", fileName.substring(fileName.lastIndexOf('.')));
            Path outputPath = Files.createTempFile("output-", fileName.substring(fileName.lastIndexOf('.')));

            s3DocumentService.downloadFile(bucketName, originalKey, inputPath.toString());
            s3DocumentService.deleteFile(bucketName, originalKey);

            resizeImage(inputPath.toString(), outputPath.toString(), scalePercentage);

            s3DocumentService.uploadFile(bucketName, resizedKey, outputPath.toString());

            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("id",
                    MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue(imageId)
                            .build());

            sqsService.sendMessage(outboxQueueUrl, resizedKey, messageAttributes);

            Files.deleteIfExists(inputPath);
            Files.deleteIfExists(outputPath);
        } catch (Exception e) {
            System.err.println("Error processing image: " + e.getMessage());
        }
    }

    public void resizeImage(String inputPath, String outputPath, int scalePercentage)
            throws RuntimeException, IOException, InterruptedException {
        System.out.println("Resizing image...");
        Process process = new ProcessBuilder("convert",
                inputPath,
                "-resize", scalePercentage + "%",
                outputPath)
                .inheritIO()
                .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Image processing failed with exit code " + exitCode);
        }
    }

    public static void main(String[] args) {
        ImageResizerServer server = new ImageResizerServer();
        while (true) {
            server.processImage();
        }
    }
}
