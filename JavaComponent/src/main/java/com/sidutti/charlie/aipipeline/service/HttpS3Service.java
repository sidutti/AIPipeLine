package com.sidutti.charlie.aipipeline.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.stream.Collectors;

@Service
public class HttpS3Service {

    private final WebClient webClient;
    private final String bucketName;
    private final String accessKey;
    private final String secretKey;
    private final String region;
    private final String s3Endpoint;

    public HttpS3Service(@Value("${aws.s3.bucket-name}") String bucketName,
                         @Value("${aws.access-key}") String accessKey,
                         @Value("${aws.secret-key}") String secretKey,
                         @Value("${aws.region}") String region) {
        this.bucketName = bucketName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.s3Endpoint = String.format("https://%s.s3.%s.amazonaws.com", bucketName, region);
        this.webClient = WebClient.builder()
                .baseUrl(s3Endpoint)
                .build();
    }

    public Mono<InputStream> downloadFile(String key) {
        return webClient.get()
                .uri("/{key}", key)
                .headers(headers -> addAuthHeaders(headers, HttpMethod.GET, "/" + key, ""))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(byte[].class)
                                .map(ByteArrayInputStream::new)
                                .cast(InputStream.class);
                    } else {
                        return response.createException()
                                .flatMap(Mono::error);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<DataBuffer> downloadFileAsStream(String key) {
        return webClient.get()
                .uri("/{key}", key)
                .headers(headers -> addAuthHeaders(headers, HttpMethod.GET, "/" + key, ""))
                .exchangeToFlux(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToFlux(DataBuffer.class);
                    } else {
                        return response.createException()
                                .flux()
                                .flatMap(Flux::error);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> uploadFile(String key, byte[] content) {
        String contentHash = calculateSHA256(content);
        
        return webClient.put()
                .uri("/{key}", key)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(headers -> {
                    headers.set("x-amz-content-sha256", contentHash);
                    addAuthHeaders(headers, HttpMethod.PUT, "/" + key, contentHash);
                })
                .body(BodyInserters.fromValue(content))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.just("File uploaded successfully: " + key);
                    } else {
                        return response.createException()
                                .flatMap(Mono::error);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> deleteFile(String key) {
        return webClient.delete()
                .uri("/{key}", key)
                .headers(headers -> addAuthHeaders(headers, HttpMethod.DELETE, "/" + key, ""))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.just("File deleted successfully: " + key);
                    } else {
                        return response.createException()
                                .flatMap(Mono::error);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<String> listFiles(String prefix) {
        String queryParams = prefix != null ? "?prefix=" + urlEncode(prefix) : "";
        
        return webClient.get()
                .uri("/" + queryParams)
                .headers(headers -> addAuthHeaders(headers, HttpMethod.GET, "/", ""))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(String.class);
                    } else {
                        return response.createException()
                                .flatMap(Mono::error);
                    }
                })
                .flatMapMany(this::parseListObjectsResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> fileExists(String key) {
        return webClient.head()
                .uri("/{key}", key)
                .headers(headers -> addAuthHeaders(headers, HttpMethod.HEAD, "/" + key, ""))
                .exchangeToMono(response -> Mono.just(response.statusCode().is2xxSuccessful()))
                .onErrorReturn(false)
                .subscribeOn(Schedulers.boundedElastic());
    }

    private void addAuthHeaders(HttpHeaders headers, HttpMethod method, String path, String payloadHash) {
        Instant now = Instant.now();
        String dateTime = now.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
        String date = now.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        headers.set("Host", String.format("%s.s3.%s.amazonaws.com", bucketName, region));
        headers.set("x-amz-date", dateTime);
        headers.set("x-amz-content-sha256", payloadHash.isEmpty() ? "UNSIGNED-PAYLOAD" : payloadHash);
        
        String authHeader = createAuthorizationHeader(method, path, headers, dateTime, date, payloadHash);
        headers.set("Authorization", authHeader);
    }

    private String createAuthorizationHeader(HttpMethod method, String path, HttpHeaders headers, String dateTime, String date, String payloadHash) {
        // Simplified AWS Signature Version 4 implementation
        String algorithm = "AWS4-HMAC-SHA256";
        String credentialScope = String.format("%s/%s/s3/aws4_request", date, region);
        String credential = String.format("%s/%s", accessKey, credentialScope);
        
        // Create canonical request
        String canonicalHeaders = headers.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().startsWith("host") || 
                               entry.getKey().toLowerCase().startsWith("x-amz"))
                .sorted((a, b) -> a.getKey().toLowerCase().compareTo(b.getKey().toLowerCase()))
                .map(entry -> entry.getKey().toLowerCase() + ":" + String.join(",", entry.getValue()))
                .collect(Collectors.joining("\n"));
        
        String signedHeaders = headers.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().startsWith("host") || 
                               entry.getKey().toLowerCase().startsWith("x-amz"))
                .map(entry -> entry.getKey().toLowerCase())
                .sorted()
                .collect(Collectors.joining(";"));
        
        String canonicalRequest = String.format("%s\n%s\n\n%s\n\n%s\n%s",
                method.name(), path, canonicalHeaders, signedHeaders, 
                payloadHash.isEmpty() ? "UNSIGNED-PAYLOAD" : payloadHash);
        
        String canonicalRequestHash = calculateSHA256(canonicalRequest.getBytes(StandardCharsets.UTF_8));
        
        // Create string to sign
        String stringToSign = String.format("%s\n%s\n%s\n%s", 
                algorithm, dateTime, credentialScope, canonicalRequestHash);
        
        // Calculate signature
        String signature = calculateSignature(stringToSign, date);
        
        return String.format("%s Credential=%s, SignedHeaders=%s, Signature=%s",
                algorithm, credential, signedHeaders, signature);
    }

    private String calculateSignature(String stringToSign, String date) {
        try {
            byte[] kDate = hmacSHA256(("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8), date);
            byte[] kRegion = hmacSHA256(kDate, region);
            byte[] kService = hmacSHA256(kRegion, "s3");
            byte[] kSigning = hmacSHA256(kService, "aws4_request");
            byte[] signature = hmacSHA256(kSigning, stringToSign);
            return HexFormat.of().formatHex(signature);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate signature", e);
        }
    }

    private byte[] hmacSHA256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String calculateSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate SHA-256", e);
        }
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private Flux<String> parseListObjectsResponse(String xmlResponse) {
        // Simple XML parsing for S3 ListObjects response
        // In production, use a proper XML parser
        return Flux.fromArray(xmlResponse.split("<Key>"))
                .skip(1) // Skip first empty element
                .map(part -> part.substring(0, part.indexOf("</Key>")))
                .filter(key -> !key.trim().isEmpty());
    }
}