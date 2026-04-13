package com.sidutti.charlie.aipipeline.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sidutti.charlie.aipipeline.dto.DocumentEmbedding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class PythonServiceClient {

    private final WebClient webClient;

    @Value("${python.service.clustering-endpoint}")
    private String clusteringEndpoint;

    @Value("${python.service.classification-endpoint}")
    private String classificationEndpoint;

    public PythonServiceClient(WebClient.Builder webClientBuilder,
                               @Value("${python.service.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<ClusteringResponse> performClustering(Flux<DocumentEmbedding> embeddings, Integer numClusters) {

        return embeddings
                .map(this::mapToEmbeddingData)
                .collectList()
                .map(e -> new ClusteringRequest(
                        e,
                        numClusters,
                        "kmeans",
                        1000, // Default batch size
                        "/mnt/nas/CodeDataset/model/cluster_model"
                ))
                .flatMap(xr -> webClient.post()
                        .uri(clusteringEndpoint)
                        .bodyValue(xr)
                        .retrieve()
                        .bodyToMono(ClusteringResponse.class));
    }

    public Mono<ClassificationResponse> performClassification(String clusterId, List<String> documentContents) {
        ClassificationRequest request = new ClassificationRequest(clusterId, documentContents);

        return webClient.post()
                .uri(classificationEndpoint)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClassificationResponse.class);
    }

    public Mono<String> checkHealth() {
        return webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(String.class);
    }

    private EmbeddingData mapToEmbeddingData(DocumentEmbedding embedding) {
        return new EmbeddingData(
                embedding.getDocumentId(),
                embedding.getEmbedding(),
                embedding.getTextContent(),
                embedding.getFileName()
        );
    }

    // --- DTOs refactored as Records ---

    public record EmbeddingData(
            @JsonProperty("document_id") String documentId,
            @JsonProperty("embedding") float[] embedding,
            @JsonProperty("text_content") String textContent,
            @JsonProperty("file_name") String fileName
    ) {
    }

    public record ClusteringRequest(
            @JsonProperty("embeddings") List<EmbeddingData> embeddings,
            @JsonProperty("num_clusters") Integer numClusters,
            @JsonProperty("algorithm") String algorithm,
            @JsonProperty("batch_size") Integer batchSize,
            @JsonProperty("input_path") String inputPath
    ) {
    }

    public record ClusteringResponse(
            @JsonProperty("clusters") List<Map<String, Object>> clusters,
            @JsonProperty("silhouette_score") Double silhouetteScore,
            @JsonProperty("num_clusters") Integer numClusters,
            @JsonProperty("algorithm") String algorithm,
            @JsonProperty("processing_time") Double processingTime
    ) {
    }

    public record ClassificationRequest(
            @JsonProperty("cluster_id") String clusterId,
            @JsonProperty("document_contents") List<String> documentContents
    ) {
    }

    public record ClassificationResponse(
            @JsonProperty("cluster_id") String clusterId,
            @JsonProperty("classification") String classification,
            @JsonProperty("confidence") Double confidence,
            @JsonProperty("keywords") List<String> keywords
    ) {
    }
}