# Image Resizer

A Java application for resizing images, with client-server architecture using AWS S3 for storage and SQS for messaging.

## Features

- Client application to request image resizing
- Server application to process resize requests
- Uses AWS S3 for image storage
- Uses AWS SQS for message queueing
- Configurable scaling percentage

## Prerequisites

- Java 21
- Maven
- AWS account with configured credentials
- AWS S3 bucket
- AWS SQS queue

## Building

```bash
mvn clean package
```

## Running the Server
```bash
mvn exec:java -Dexec.mainClass="org.example.ImageResizerServer"
```

## Running the Client
```bash
mvn exec:java -Dexec.mainClass="org.example.ImageResizerClient" -Dexec.args="[options]"
```

### Client Options
```
Usage: ImageResizerClient [options]
Options:
  -i, --input-file FILE       Input image file to resize
  -s, --scale PERCENTAGE      Scale percentage (0-100)
  [-o, --output-file FILE]    Output file (default: resized/input_file_name)
```

### Example
```bash
mvn exec:java -Dexec.mainClass="org.example.ImageResizerClient" \
  -Dexec.args="-i \"/home/u3036098041/Documents/T5/capoo.jpeg\" -s 50"
```

## Project Structure
```
image-resizer
├── src
│   └── main
│       └── java
│           └── org.example
│               ├── ImageResizerClient.java
│               ├── ImageResizerServer.java
│               ├── S3DocumentService.java
│               └── SqsService.java
├── pom.xml
└── README.md
```