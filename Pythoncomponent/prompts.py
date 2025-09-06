# prompts.py

# --- System Prompt for Validation LLM ---
SYSTEM_PROMPT = """
You are a validation AI. Your task is to verify the accuracy of extracted key-value pairs against a provided source document.
You must carefully analyze the source document and the extracted information.
Your response must be a JSON array, where each object in the array has the following four properties: 'key', 'value', 'accuracy', and 'citation'.

- 'key': This should be the same as the key from the input.
- 'value': This should be the corrected value if the original was inaccurate, otherwise the original value.
- 'accuracy': A score from 0.0 to 1.0 indicating your confidence in the accuracy of the 'value'. 1.0 means you are certain, 0.0 means it's completely wrong.
- 'citation': A direct quote from the source document that supports the 'value'. If no citation is possible, this should be an empty string.

For each key-value pair in the input, you must provide a corresponding validation object in the output array.
Do not provide any explanation or additional text outside of the JSON array.
"""

# --- User Prompt for Validation LLM ---
# This is a template. You will need to format it with the actual data.
USER_PROMPT_TEMPLATE = """
Please validate the following extracted information based on the source document provided.

**Source Document:**
```
{document_text}
```

**Extracted Information (Key-Value Pairs):**
{extractions_text}
"""

# --- Output Schema Definition ---
# The expected output from the validation LLM will be a JSON array of objects
# that can be parsed into a list of Python dictionaries.

OUTPUT_SCHEMA = [
    {
        "key": "string",
        "value": "string",
        "accuracy": "float",
        "citation": "string"
    }
]

# Example of a Python class representing one item in the schema
class ValidatedEntity:
    def __init__(self, key: str, value: str, accuracy: float, citation: str):
        self.key = key
        self.value = value
        self.accuracy = accuracy
        self.citation = citation

    def __repr__(self):
        return f"ValidatedEntity(key='{self.key}', value='{self.value}', accuracy={self.accuracy}, citation='{self.citation}')"
