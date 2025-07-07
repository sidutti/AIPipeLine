package com.siduuti.aipipeline.service;

import com.siduuti.aipipeline.dto.DocumentEmbedding;
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
                .map(e -> {
                    ClusteringRequest request = new ClusteringRequest();
                    request.setNumClusters(numClusters);
                    request.setAlgorithm("kmeans");
                    request.setInputPath("/mnt/nas/CodeDataset/model/cluster_model");
                    request.setEmbeddings(e);
                    return request;
                })
                .flatMap(xr -> webClient.post()
                        .uri(clusteringEndpoint)
                        .bodyValue(xr)
                        .retrieve()
                        .bodyToMono(ClusteringResponse.class));
    }

    public Mono<ClassificationResponse> performClassification(String clusterId, List<String> documentContents) {
        ClassificationRequest request = new ClassificationRequest();
        request.setClusterId(clusterId);
        request.setDocumentContents(documentContents);

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
        EmbeddingData data = new EmbeddingData();
        data.setDocumentId(embedding.getDocumentId());
        data.setEmbedding(embedding.getEmbedding());
        data.setTextContent(embedding.getTextContent());
        data.setFileName(embedding.getFileName());
        return data;
    }

    public static class EmbeddingData {
        private String documentId;
        private float[] embedding;
        private String textContent;
        private String fileName;

        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public float[] getEmbedding() {
            return embedding;
        }

        public void setEmbedding(float[] embedding) {
            this.embedding = embedding;
        }

        public String getTextContent() {
            return textContent;
        }

        public void setTextContent(String textContent) {
            this.textContent = textContent;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }

    public static class ClusteringRequest {
        private List<EmbeddingData> embeddings;
        private Integer numClusters;
        private String algorithm;
        private Integer batchSize=1000;
        private String inputPath;

        public List<EmbeddingData> getEmbeddings() {
            return embeddings;
        }

        public void setEmbeddings(List<EmbeddingData> embeddings) {
            this.embeddings = embeddings;
        }

        public Integer getNumClusters() {
            return numClusters;
        }

        public void setNumClusters(Integer numClusters) {
            this.numClusters = numClusters;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public String getInputPath() {
            return inputPath;
        }

        public void setInputPath(String inputPath) {
            this.inputPath = inputPath;
        }

        public Integer getBatchSize() {
            return batchSize;
        }
    }

    public static class ClusteringResponse {
        private List<Map<String, Object>> clusters;
        private Double silhouetteScore;
        private Integer numClusters;
        private String algorithm;
        private Double processingTime;

        public List<Map<String, Object>> getClusters() {
            return clusters;
        }

        public void setClusters(List<Map<String, Object>> clusters) {
            this.clusters = clusters;
        }

        public Double getSilhouetteScore() {
            return silhouetteScore;
        }

        public void setSilhouetteScore(Double silhouetteScore) {
            this.silhouetteScore = silhouetteScore;
        }

        public Integer getNumClusters() {
            return numClusters;
        }

        public void setNumClusters(Integer numClusters) {
            this.numClusters = numClusters;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public Double getProcessingTime() {
            return processingTime;
        }

        public void setProcessingTime(Double processingTime) {
            this.processingTime = processingTime;
        }
    }

    public static class ClassificationRequest {
        private String clusterId;
        private List<String> documentContents;

        public String getClusterId() {
            return clusterId;
        }

        public void setClusterId(String clusterId) {
            this.clusterId = clusterId;
        }

        public List<String> getDocumentContents() {
            return documentContents;
        }

        public void setDocumentContents(List<String> documentContents) {
            this.documentContents = documentContents;
        }
    }

    public static class ClassificationResponse {
        private String clusterId;
        private String classification;
        private Double confidence;
        private List<String> keywords;

        public String getClusterId() {
            return clusterId;
        }

        public void setClusterId(String clusterId) {
            this.clusterId = clusterId;
        }

        public String getClassification() {
            return classification;
        }

        public void setClassification(String classification) {
            this.classification = classification;
        }

        public Double getConfidence() {
            return confidence;
        }

        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }

        public List<String> getKeywords() {
            return keywords;
        }

        public void setKeywords(List<String> keywords) {
            this.keywords = keywords;
        }
    }
}