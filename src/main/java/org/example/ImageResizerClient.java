package org.example;

import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
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

    public void processImage(String filePath, String destinationPath, int scalePercentage) {
        String imageId = UUID.randomUUID().toString();
        String fileName = (new File(filePath).getName());
        String originalKey = "original/" + imageId + "_" + fileName;

        try {
            s3DocumentService.uploadFile(bucketName, originalKey, filePath);

            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("id",
                    MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue(imageId)
                            .build());
            messageAttributes.put("scale",
                    MessageAttributeValue.builder()
                            .dataType("Number")
                            .stringValue(String.valueOf(scalePercentage))
                            .build());
            messageAttributes.put("fileName",
                    MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue(fileName)
                            .build());

            sqsService.sendMessage(inboxQueueUrl, originalKey, messageAttributes);

            Map <String, MessageAttributeValue> expectedAttributes = new HashMap<>();
            expectedAttributes.put("id",
                    MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue(imageId)
                            .build());

            String resizedKey = sqsService.receiveMessage(outboxQueueUrl, expectedAttributes).body();

            s3DocumentService.downloadFile(bucketName, resizedKey, destinationPath);
        } catch (Exception e) {
            System.err.println("Error resizing image: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String inputFile = null;
        int scalePercentage = -1;
        String outputFile = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-i":
                case "--input-file":
                    if (i + 1 < args.length) inputFile = args[++i];
                    break;
                case "-s":
                case "--scale":
                    if (i + 1 < args.length) {
                        try {
                            scalePercentage = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Error: Scale percentage must be a number");
                            printUsageAndExit();
                        }
                    }
                    break;
                case "-o":
                case "--output-file":
                    if (i + 1 < args.length) outputFile = args[++i];
                    break;
                default:
                    System.err.println("Error: Unknown option '" + args[i] + "'");
                    printUsageAndExit();
            }
        }

        if (inputFile == null || scalePercentage == -1) {
            printUsageAndExit();
        }

        if (outputFile == null) {
            String filename = inputFile.substring(inputFile.lastIndexOf('/') + 1);
            outputFile = "resized/" + filename;
        }

        ImageResizerClient imageResizerClient = new ImageResizerClient();
        imageResizerClient.processImage(inputFile, outputFile, scalePercentage);
    }

    private static void printUsageAndExit() {
        System.err.println("Usage: ImageResizerClient [options]");
        System.err.println("Options:");
        System.err.println("  -i, --input-file FILE       Input image file to resize");
        System.err.println("  -s, --scale PERCENTAGE      Scale percentage (0-100)");
        System.err.println("  [-o, --output-file FILE]    Output file (default: based on input filename)");
        System.exit(1);
    }

}
