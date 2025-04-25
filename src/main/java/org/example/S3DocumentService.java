package org.example;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class S3DocumentService {
    private final S3Client s3Client;

    public S3DocumentService() {
        this.s3Client = S3Client.builder().region(Region.AP_NORTHEAST_1).build();
    }

    public void uploadFile(String bucketName, String key, String filePath) throws Exception {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(Paths.get(filePath)));

        System.out.println("File uploaded successfully to S3: " + bucketName + "/" + key);
    }

    public void downloadFile(String bucketName, String key, String destinationPath) throws Exception {
        Path localPath = Paths.get(destinationPath);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        Files.createDirectories(localPath.getParent());

//        s3Client.getObject(getObjectRequest, localPath);
        var inputStream = s3Client.getObject(getObjectRequest);
        var outputStream = Files.newOutputStream(localPath);
        inputStream.transferTo(outputStream);

        System.out.println("File downloaded successfully from S3: " + bucketName + "/" + key +
                " to " + destinationPath);
    }
}
