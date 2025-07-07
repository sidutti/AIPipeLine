import logging
import os
from datetime import datetime
from typing import List, Optional

import joblib
import numpy as np
import uvicorn
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from sklearn.cluster import MiniBatchKMeans
from sklearn.metrics import silhouette_score
from sklearn.preprocessing import StandardScaler

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Document Clustering Service", version="1.0.0")


class EmbeddingData(BaseModel):
    document_id: str
    embedding: List[float]
    text_content: str
    file_name: str


class ClusteringRequest(BaseModel):
    embeddings: List[EmbeddingData]
    num_clusters: Optional[int] = None
    algorithm: Optional[str] = "minibatch_kmeans"
    batch_size: Optional[int] = 1000
    input_path: str


class ClusteringResponse(BaseModel):
    clusters: List[dict]
    silhouette_score: float
    num_clusters: int
    algorithm: str
    processing_time: float
    cluster_centers: Optional[List[List[float]]] = None


class ClusteringService:
    def __init__(self):

        self.scaler = StandardScaler()
        self.clusterer = None  # Will hold the MiniBatchKMeans instance
        self.is_fitted = False


    def determine_optimal_clusters(self, embeddings: np.ndarray, max_clusters: int = 20) -> int:
        """Determine optimal number of clusters using elbow method and silhouette score http://192.168.1.84:30003/"""

        n_samples = len(embeddings)
        max_clusters = min(max_clusters, n_samples // 2)

        if max_clusters < 2:
            return 2

        silhouette_scores = []
        inertias = []

        for k in range(2, max_clusters + 1):
            minibatch_kmeans = MiniBatchKMeans(n_clusters=k, random_state=42, batch_size=1000)
            cluster_labels = minibatch_kmeans.fit_predict(embeddings)
            silhouette_avg = silhouette_score(embeddings, cluster_labels)
            silhouette_scores.append(silhouette_avg)
            inertias.append(minibatch_kmeans.inertia_)

        # Find optimal k using silhouette score
        optimal_k = np.argmax(silhouette_scores) + 2
        logger.info(f"Optimal number of clusters determined: {optimal_k}")

        return optimal_k

    def perform_clustering(self, input_file, embeddings: np.ndarray, num_clusters: int,
                           batch_size: int = 1000) -> tuple:
        """Perform clustering on embeddings using MiniBatchKMeans"""

        # Always use MiniBatchKMeans for scalability
        if os.path.exists(input_file):
            # Continue training existing model
            clusterer =  joblib.load(input_file)
            clusterer.partial_fit(embeddings)
        else:
            # Create new clusterer
            clusterer = MiniBatchKMeans(
                n_clusters=num_clusters,
                random_state=42,
                batch_size=min(batch_size, len(embeddings)),
                init_size=max(3 * num_clusters, 100),
                max_iter=100,
                tol=1e-4
            )
            clusterer.fit(embeddings)

        # Store clusterer for future partial fits
        self.clusterer = clusterer
        self.is_fitted = True

        # Get cluster labels
        cluster_labels = clusterer.predict(embeddings)

        # Calculate silhouette score (sample for large datasets)
        if len(embeddings) > 10000:
            # Sample for silhouette score calculation to avoid memory issues
            sample_size = min(5000, len(embeddings))
            sample_indices = np.random.choice(len(embeddings), sample_size, replace=False)
            sample_embeddings = embeddings[sample_indices]
            sample_labels = cluster_labels[sample_indices]

            if len(set(sample_labels)) > 1:
                silhouette_avg = silhouette_score(sample_embeddings, sample_labels)
            else:
                silhouette_avg = 0.0
        else:
            if len(set(cluster_labels)) > 1:
                silhouette_avg = silhouette_score(embeddings, cluster_labels)
            else:
                silhouette_avg = 0.0
        joblib.dump(clusterer, input_file)
        return cluster_labels, silhouette_avg, clusterer

clustering_service = ClusteringService()


@app.post("/cluster", response_model=ClusteringResponse)
async def cluster_documents(request: ClusteringRequest):
    """Cluster documents based on their embeddings"""
    try:
        start_time = datetime.now()

        if not request.embeddings:
            raise HTTPException(status_code=400, detail="No embeddings provided")

        # Convert embeddings to numpy array
        embedding_matrix = np.array([emb.embedding for emb in request.embeddings])

        # Normalize embeddings
        embedding_matrix = clustering_service.scaler.fit_transform(embedding_matrix)

        # Determine optimal number of clusters if not specified
        if request.num_clusters is None:
            # For large datasets, use a sample to determine optimal clusters
            if len(embedding_matrix) > 10000:
                sample_size = min(5000, len(embedding_matrix))
                sample_indices = np.random.choice(len(embedding_matrix), sample_size, replace=False)
                sample_embeddings = embedding_matrix[sample_indices]
                num_clusters = clustering_service.determine_optimal_clusters(sample_embeddings)
            else:
                num_clusters = clustering_service.determine_optimal_clusters(embedding_matrix)
        else:
            num_clusters = request.num_clusters

        # Convert cluster centers if provided
        cluster_centers = None
        if request.cluster_centers:
            cluster_centers = np.array(request.cluster_centers)

        # Perform clustering
        cluster_labels, silhouette_avg, clusterer = clustering_service.perform_clustering(
            request.input_path, embedding_matrix,  num_clusters
        )

        # Create cluster summary
        clusters = []
        for cluster_id in range(num_clusters):
            cluster_docs = [
                {
                    "document_id": request.embeddings[i].document_id,
                    "file_name": request.embeddings[i].file_name,
                    "text_preview": request.embeddings[i].text_content[:200] + "..." if len(
                        request.embeddings[i].text_content) > 200 else request.embeddings[i].text_content
                }
                for i, label in enumerate(cluster_labels) if label == cluster_id
            ]

            clusters.append({
                "cluster_id": f"cluster_{cluster_id}",
                "document_count": len(cluster_docs),
                "documents": cluster_docs
            })


        processing_time = (datetime.now() - start_time).total_seconds()

        response = ClusteringResponse(
            clusters=clusters,
            silhouette_score=silhouette_avg,
            num_clusters=num_clusters,
            algorithm=request.algorithm,
            processing_time=processing_time
        )

        # Add cluster centers to response for future partial fits
        if hasattr(clusterer, 'cluster_centers_'):
            response.cluster_centers = clusterer.cluster_centers_.tolist()

        return response

    except Exception as e:
        logger.error(f"Error in clustering: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "service": "clustering-service"}


@app.get("/clusters/{cluster_id}")
async def get_cluster_details(cluster_id: str):
    """Get details of a specific cluster"""
    try:
        # Query Elasticsearch for documents in this cluster
        search_body = {
            "query": {
                "term": {
                    "clusterId": cluster_id
                }
            }
        }

        response = clustering_service.es.search(
            index="document_embeddings",
            body=search_body
        )

        documents = []
        for hit in response['hits']['hits']:
            documents.append({
                "document_id": hit['_source']['documentId'],
                "file_name": hit['_source']['fileName'],
                "text_preview": hit['_source']['textContent'][:200] + "..." if len(
                    hit['_source']['textContent']) > 200 else hit['_source']['textContent']
            })

        return {
            "cluster_id": cluster_id,
            "document_count": len(documents),
            "documents": documents
        }

    except Exception as e:
        logger.error(f"Error retrieving cluster details: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8001)
