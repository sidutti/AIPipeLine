import argparse
import json
import os
from typing import Dict, List

import joblib  # More efficient for scikit-learn models than pickle
import numpy as np
from sklearn.cluster import MiniBatchKMeans

# Added for validation
from prompts import SYSTEM_PROMPT, USER_PROMPT_TEMPLATE, ValidatedEntity
# You will need a library to call your LLM, e.g., openai, anthropic, etc.
# import openai


def run_kmeans_batch(input_file, output_file, input_model_path, output_model_path, n_clusters=100, random_state=42):
    """
    Performs MiniBatchKMeans on a batch of embeddings and saves the updated model.
    """
    print(f"Starting MiniBatchKMeans processing for batch: {input_file}")

    # ... (rest of the function is unchanged)
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
    
    kmeans_model = None
    if os.path.exists(input_model_path):
        try:
            kmeans_model = joblib.load(input_model_path)
            if kmeans_model.n_clusters != n_clusters:
                print(f"WARNING: Loaded model has {kmeans_model.n_clusters} clusters, but {n_clusters} was requested.")
                n_clusters = kmeans_model.n_clusters
        except Exception as e:
            print(f"Error loading K-Means model: {e}. Initializing a new model.")
            kmeans_model = MiniBatchKMeans(n_clusters=n_clusters, random_state=random_state, n_init='auto', batch_size=256)
    else:
        print(f"No existing K-Means model found at {input_model_path}. Initializing a new model.")
        kmeans_model = MiniBatchKMeans(n_clusters=n_clusters, random_state=random_state, n_init='auto', batch_size=256)

    kmeans_model.partial_fit(embeddings)
    cluster_assignments = kmeans_model.predict(embeddings)
    joblib.dump(kmeans_model, output_model_path)

    with open(output_file, 'w') as f:
        for i, doc_id in enumerate(document_ids):
            assignment = {"id": doc_id, "cluster_id": int(cluster_assignments[i])}
            f.write(json.dumps(assignment) + '\n')

    print("MiniBatchKMeans batch processing finished successfully.")


def format_extractions_for_prompt(extractions: Dict[str, str]) -> str:
    """Formats a dictionary of extractions into a string for the prompt."""
    return "\n".join([f"- **Key:** {k}\n  - **Value:** {v}" for k, v in extractions.items()])


def validate_extractions(document_text: str, extractions: Dict[str, str]) -> List[ValidatedEntity]:
    """
    Validates a dictionary of extracted key-value pairs using an LLM.

    Args:
        document_text: The source text.
        extractions: A dictionary of extracted key-value pairs.

    Returns:
        A list of ValidatedEntity objects.
    """
    extractions_str = format_extractions_for_prompt(extractions)
    user_prompt = USER_PROMPT_TEMPLATE.format(
        document_text=document_text,
        extractions_text=extractions_str
    )

    # --- Placeholder for LLM call ---
    print("--- LLM VALIDATION CALL (PLACEHOLDER) ---")
    print(f"System Prompt:\n{SYSTEM_PROMPT}")
    print(f"User Prompt:\n{user_prompt}")
    print("------------------------------------")

    # This is a placeholder response for a dictionary input.
    validated_data_list = [
        {
            "key": k,
            "value": v,
            "accuracy": 0.9,
            "citation": f"A dummy citation for key '{k}'."
        } for k, v in extractions.items()
    ]
    # --- End of Placeholder ---

    validated_entities = []
    for item in validated_data_list:
        if not all(k in item for k in ["key", "value", "accuracy", "citation"]):
            raise ValueError("LLM response item did not contain all the required keys.")
        
        validated_entities.append(
            ValidatedEntity(
                key=item["key"],
                value=item["value"],
                accuracy=float(item["accuracy"]),
                citation=item["citation"]
            )
        )
    
    return validated_entities


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Perform actions like KMeans clustering or extraction validation.')
    subparsers = parser.add_subparsers(dest='command', required=True)

    # K-Means subcommand
    parser_kmeans = subparsers.add_parser('kmeans', help='Run MiniBatchKMeans clustering.')
    parser_kmeans.add_argument('--input', type=str, required=True, help='Path to input JSONL file with embeddings.')
    parser_kmeans.add_argument('--output', type=str, required=True, help='Path to output JSONL file for cluster assignments.')
    parser_kmeans.add_argument('--input-model', type=str, default='./kmeans_model.pkl', help='Path to existing KMeans model to load.')
    parser_kmeans.add_argument('--output-model', type=str, default='./kmeans_model_updated.pkl', help='Path to save the updated KMeans model.')
    parser_kmeans.add_argument('--n-clusters', type=int, default=100, help='Number of clusters for KMeans.')
    parser_kmeans.add_argument('--random-state', type=int, default=42, help='Random state for reproducibility.')

    # Validation subcommand
    parser_validate = subparsers.add_parser('validate', help='Validate extracted key-value pairs.')
    parser_validate.add_argument('--text', type=str, required=True, help='The source document text.')
    parser_validate.add_argument('--extractions', type=str, required=True, 
                                help='A JSON string of the dictionary to validate (e.g., \'{\"key1\": \"value1\", \"key2\": \"value2\"}\').')

    args = parser.parse_args()

    if args.command == 'kmeans':
        run_kmeans_batch(args.input, args.output, args.input_model, args.output_model, args.n_clusters, args.random_state)
    elif args.command == 'validate':
        try:
            extractions_dict = json.loads(args.extractions)
            if not isinstance(extractions_dict, dict):
                raise ValueError("The --extractions argument must be a JSON dictionary.")
            
            validation_results = validate_extractions(args.text, extractions_dict)
            print("\n--- Validation Results ---")
            for result in validation_results:
                print(result)
            print("--------------------------")

        except json.JSONDecodeError:
            print("Error: Invalid JSON format for --extractions argument.")
        except ValueError as e:
            print(f"Error: {e}")
