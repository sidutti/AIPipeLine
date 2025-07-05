import argparse
import json
import os

import joblib  # More efficient for scikit-learn models than pickle
import numpy as np
from sklearn.cluster import MiniBatchKMeans


def run_kmeans_batch(input_file, output_file, input_model_path, output_model_path, n_clusters=100, random_state=42):
    """
    Performs MiniBatchKMeans on a batch of embeddings and saves the updated model.

    Args:
        input_file (str): Path to a JSONL file containing documents with "id" and "embedding".
        output_file (str): Path to output JSONL file for cluster assignments.
        input_model_path (str): Path to load initial/previous KMeans model, or None for new model.
        output_model_path (str): Path to save the updated KMeans model.
        n_clusters (int): Number of clusters.
        random_state (int): Random state for reproducibility.
    """
    print(f"Starting MiniBatchKMeans processing for batch: {input_file}")

    # 1. Load embeddings from input file
    document_ids = []
    embeddings_list = []
    with open(input_file, 'r') as f:
        for line in f:
            data = json.loads(line.strip())
            document_ids.append(data['id'])
            embeddings_list.append(data['embedding'])

    if not embeddings_list:
        print("No embeddings found in input file. Exiting.")
        return

    embeddings = np.array(embeddings_list)
    print(f"Loaded {embeddings.shape[0]} embeddings of dimension {embeddings.shape[1]}")

    # 2. Load or initialize MiniBatchKMeans model
    kmeans_model = None
    if os.path.exists(input_model_path):
        print(f"Loading existing K-Means model from {input_model_path}")
        try:
            kmeans_model = joblib.load(input_model_path)
            # Ensure n_clusters matches if loading an existing model
            if kmeans_model.n_clusters != n_clusters:
                print(
                    f"WARNING: Loaded model has {kmeans_model.n_clusters} clusters, but {n_clusters} was requested. Using loaded model's cluster count.")
                n_clusters = kmeans_model.n_clusters
        except Exception as e:
            print(f"Error loading K-Means model: {e}. Initializing a new model.")
            kmeans_model = MiniBatchKMeans(n_clusters=n_clusters, random_state=random_state, n_init='auto',
                                           batch_size=256)
    else:
        print(f"No existing K-Means model found at {input_model_path}. Initializing a new model.")
        # Ensure 'n_init' is explicitly set to 'auto' for scikit-learn >= 1.2
        kmeans_model = MiniBatchKMeans(n_clusters=n_clusters, random_state=random_state, n_init='auto',
                                       batch_size=256)  # batch_size for MiniBatchKMeans itself

    # 3. Fit (first batch) or partial_fit (subsequent batches)
    print("Fitting/Partial-fitting model...")
    kmeans_model.partial_fit(embeddings)
    print("Model fitting complete.")

    # 4. Predict cluster assignments for the current batch
    cluster_assignments = kmeans_model.predict(embeddings)
    print("Predicted cluster assignments.")

    # 5. Save updated K-Means model for persistence
    print(f"Saving updated K-Means model to {output_model_path}")
    joblib.dump(kmeans_model, output_model_path)

    # 6. Write output assignments
    print(f"Writing cluster assignments to {output_file}")
    with open(output_file, 'w') as f:
        for i, doc_id in enumerate(document_ids):
            assignment = {"id": doc_id, "cluster_id": int(cluster_assignments[i])}
            f.write(json.dumps(assignment) + '\n')

    print("MiniBatchKMeans batch processing finished successfully.")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Perform MiniBatchKMeans clustering on document embeddings.')
    parser.add_argument('--input', type=str, required=True, help='Path to input JSONL file with embeddings.')
    parser.add_argument('--output', type=str, required=True, help='Path to output JSONL file for cluster assignments.')
    parser.add_argument('--input-model', type=str, default='./kmeans_model.pkl',
                        help='Path to existing KMeans model to load. If not exists, a new one is initialized.')
    parser.add_argument('--output-model', type=str, default='./kmeans_model_updated.pkl',
                        help='Path to save the updated KMeans model.')
    parser.add_argument('--n-clusters', type=int, default=100, help='Number of clusters for KMeans.')
    parser.add_argument('--random-state', type=int, default=42, help='Random state for reproducibility.')

    args = parser.parse_args()

    run_kmeans_batch(args.input, args.output, args.input_model, args.output_model, args.n_clusters, args.random_state)