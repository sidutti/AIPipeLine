package com.siduuti.aipipeline.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

@Service
public class S3Service {

    private final S3AsyncClient s3AsyncClient;
    private final String bucketName;

    public S3Service(S3AsyncClient s3AsyncClient, 
                     @Value("${aws.s3.bucket-name}") String bucketName) {
        this.s3AsyncClient = s3AsyncClient;
        this.bucketName = bucketName;
    }

    public Mono<String> uploadFile(String key, byte[] content) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return Mono.fromFuture(s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes(content)))
                .map(response -> "File uploaded successfully: " + key)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> uploadFile(String key, InputStream inputStream) {
        return Mono.fromCallable(() -> {
            try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] data = new byte[1024];
                int nRead;
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                return buffer.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Failed to read input stream", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(content -> uploadFile(key, content));
    }


    public Mono<byte[]> downloadFileAsBytes(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return Mono.fromFuture(s3AsyncClient.getObject(getObjectRequest, AsyncResponseTransformer.toBytes()))
                .map(response -> response.asByteArray())
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> deleteFile(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return Mono.fromFuture(s3AsyncClient.deleteObject(deleteObjectRequest))
                .map(response -> "File deleted successfully: " + key)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<String> listFiles(String prefix) {
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        return Mono.fromFuture(s3AsyncClient.listObjectsV2(listObjectsRequest))
                .flatMapMany(response -> Flux.fromIterable(response.contents()))
                .map(S3Object::key)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> fileExists(String key) {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return Mono.fromFuture(s3AsyncClient.headObject(headObjectRequest))
                .map(response -> true)
                .onErrorReturn(false)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> copyFile(String sourceKey, String destinationKey) {
        CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(sourceKey)
                .destinationBucket(bucketName)
                .destinationKey(destinationKey)
                .build();

        return Mono.fromFuture(s3AsyncClient.copyObject(copyObjectRequest))
                .map(response -> "File copied successfully from " + sourceKey + " to " + destinationKey)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Long> getFileSize(String key) {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return Mono.fromFuture(s3AsyncClient.headObject(headObjectRequest))
                .map(HeadObjectResponse::contentLength)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<S3FileInfo> getAllFilesInBucket() {
        return getAllFilesInBucket(null);
    }

    public Flux<S3FileInfo> getAllFilesInBucket(String prefix) {
        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                .bucket(bucketName);
        
        if (prefix != null && !prefix.trim().isEmpty()) {
            requestBuilder.prefix(prefix);
        }

        return listObjectsPaginated(requestBuilder.build())
                .map(s3Object -> new S3FileInfo(
                        s3Object.key(),
                        s3Object.size(),
                        s3Object.lastModified(),
                        s3Object.eTag(),
                        s3Object.storageClass() != null ? s3Object.storageClass().toString() : "STANDARD"
                ));
    }

    private Flux<S3Object> listObjectsPaginated(ListObjectsV2Request initialRequest) {
        return Mono.fromFuture(s3AsyncClient.listObjectsV2(initialRequest))
                .expand(response -> {
                    if (response.isTruncated()) {
                        ListObjectsV2Request nextRequest = initialRequest.toBuilder()
                                .continuationToken(response.nextContinuationToken())
                                .build();
                        return Mono.fromFuture(s3AsyncClient.listObjectsV2(nextRequest));
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMapIterable(ListObjectsV2Response::contents)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<DocumentContent> downloadDocument(String key) {
        return downloadDocumentAsBytes(key);
    }

    public Mono<DocumentContent> downloadDocumentAsBytes(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return Mono.fromFuture(s3AsyncClient.getObject(getObjectRequest, AsyncResponseTransformer.toBytes()))
                .map(response -> new DocumentContent(
                        key,
                        response.asByteArray(),
                        null,
                        response.response().contentType(),
                        response.response().contentLength(),
                        response.response().lastModified(),
                        response.response().eTag()
                ))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<StreamingDocumentContent> downloadDocumentAsStream(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return Mono.fromFuture(s3AsyncClient.getObject(getObjectRequest, AsyncResponseTransformer.toInputStream()))
                .map(inputStream -> new StreamingDocumentContent(
                        key,
                        inputStream,
                        inputStream.response().contentType(),
                        inputStream.response().contentLength(),
                        inputStream.response().lastModified(),
                        inputStream.response().eTag()
                ))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<DocumentContent> downloadAllDocuments() {
        return getAllFilesInBucket()
                .flatMap(fileInfo -> downloadDocument(fileInfo.key())
                        .onErrorResume(error -> {
                            System.err.println("Failed to download file: " + fileInfo.key() + " - " + error.getMessage());
                            return Mono.empty();
                        }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<DocumentContent> downloadDocumentsByPrefix(String prefix) {
        return getAllFilesInBucket(prefix)
                .flatMap(fileInfo -> downloadDocument(fileInfo.key())
                        .onErrorResume(error -> {
                            System.err.println("Failed to download file: " + fileInfo.key() + " - " + error.getMessage());
                            return Mono.empty();
                        }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<S3FileInfo> getFilesModifiedInLast24Hours() {
        return getFilesModifiedInLast24Hours(null);
    }

    public Flux<S3FileInfo> getFilesModifiedInLast24Hours(String prefix) {
        Instant twentyFourHoursAgo = Instant.now().minus(Duration.ofHours(24));
        
        return getAllFilesInBucket(prefix)
                .filter(fileInfo -> fileInfo.getLastModified().isAfter(twentyFourHoursAgo))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public static class S3FileInfo {
        private final String key;
        private final Long size;
        private final Instant lastModified;
        private final String eTag;
        private final String storageClass;

        public S3FileInfo(String key, Long size, Instant lastModified, String eTag, String storageClass) {
            this.key = key;
            this.size = size;
            this.lastModified = lastModified;
            this.eTag = eTag;
            this.storageClass = storageClass;
        }

        public String getKey() { return key; }
        public Long getSize() { return size; }
        public Instant getLastModified() { return lastModified; }
        public String getETag() { return eTag; }
        public String getStorageClass() { return storageClass; }
    }

    public static class DocumentContent {
        private final String key;
        private final byte[] content;
        private final InputStream inputStream;
        private final String contentType;
        private final Long contentLength;
        private final Instant lastModified;
        private final String eTag;

        public DocumentContent(String key, byte[] content, InputStream inputStream, String contentType, Long contentLength, Instant lastModified, String eTag) {
            this.key = key;
            this.content = content;
            this.inputStream = inputStream;
            this.contentType = contentType;
            this.contentLength = contentLength;
            this.lastModified = lastModified;
            this.eTag = eTag;
        }

        public String getKey() { return key; }
        public byte[] getContent() { return content; }
        public InputStream getInputStream() { return inputStream; }
        public String getContentType() { return contentType; }
        public Long getContentLength() { return contentLength; }
        public Instant getLastModified() { return lastModified; }
        public String getETag() { return eTag; }
    }

    public static class StreamingDocumentContent {
        private final String key;
        private final InputStream inputStream;
        private final String contentType;
        private final Long contentLength;
        private final Instant lastModified;
        private final String eTag;

        public StreamingDocumentContent(String key, InputStream inputStream, String contentType, Long contentLength, Instant lastModified, String eTag) {
            this.key = key;
            this.inputStream = inputStream;
            this.contentType = contentType;
            this.contentLength = contentLength;
            this.lastModified = lastModified;
            this.eTag = eTag;
        }

        public String getKey() { return key; }
        public InputStream getInputStream() { return inputStream; }
        public String getContentType() { return contentType; }
        public Long getContentLength() { return contentLength; }
        public Instant getLastModified() { return lastModified; }
        public String getETag() { return eTag; }
    }
}