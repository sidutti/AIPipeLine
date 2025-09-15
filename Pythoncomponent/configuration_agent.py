# configuration_agent.py

def ask_question(question, options=None, is_sensitive=False):
    """
    Prints a question to the user and lists the available options.
    In a real app, this would also handle input.
    """
    print(f"Question: {question}")
    if options:
        print("Options:")
        for i, option in enumerate(options, 1):
            print(f"  {i}. {option}")
    # In a real interactive scenario, you would wait for and capture user input here.
    # If is_sensitive, the input should be masked.
    print("[User response placeholder]\n")


class ConfigurationAgent:
    """
    An agent that asks a series of questions to configure a data pipeline.
    """
    def __init__(self):
        self.config = {}
        self.source_options = [
            "Confluence",
            "ECM Repo",
            "Sharepoint",
            "Object Storage (S3, HPOS)",
            "List of URLs",
            "CSV File Upload",
            "Kafka",
            "Webhook (Posted URL)"
        ]

    def start_configuration(self, source_type):
        """
        Starts the configuration process for a given source type.
        """
        print(f"--- Starting Configuration for '{source_type}' ---")
        self.handle_source_selection(source_type)
        print("------------------------------------\n")

    def handle_source_selection(self, source_type):
        """
        Handles the user's selection of a data source and asks relevant questions.
        """
        self.config = {'source_type': source_type} # Reset config for the new source

        ask_question(f"First, what is a friendly name for this '{source_type}' configuration?")
        # self.config['friendly_name'] = user_input

        # Branch to source-specific questions
        if source_type == "Confluence":
            self._ask_confluence_questions()
        elif source_type == "ECM Repo":
            self._ask_ecm_repo_questions()
        elif source_type == "Sharepoint":
            self._ask_sharepoint_questions()
        elif source_type == "Object Storage (S3, HPOS)":
            self._ask_object_storage_questions()
        elif source_type == "List of URLs":
            self._ask_url_list_questions()
        elif source_type == "CSV File Upload":
            self._ask_csv_questions()
        elif source_type == "Kafka":
            self._ask_kafka_questions()
        elif source_type == "Webhook (Posted URL)":
            self._ask_webhook_questions()
        else:
            print(f"Configuration for '{source_type}' is not yet implemented.")

        print(f"--- '{source_type}' Configuration Complete (Example) ---")
        # In a real app, you would save or use self.config
        print("Configuration data would be saved at this point.")


    def _ask_confluence_questions(self):
        # ... (previous implementation)
        print("Great. Now I need some details about your Confluence instance.\n")
        ask_question("What is your Confluence site URL? (e.g., https://your-company.atlassian.net)")
        ask_question("What is your Confluence username or email?")
        ask_question("What is your Confluence API Token? (This will be handled securely)", is_sensitive=True)
        ask_question("Which Confluence Space key do you want to index?")

    def _ask_ecm_repo_questions(self):
        # ... (previous implementation)
        print("Great. Now I need some details about your ECM repository.\n")
        ask_question("What is the URL of the ECM repository?")
        ask_question("What authentication type does it use?", ["Basic Auth", "OAuth 2.0", "API Key"])
        ask_question("Please provide your credentials.", is_sensitive=True)
        ask_question("What is the specific folder path or repository ID to start from?")

    def _ask_sharepoint_questions(self):
        # ... (previous implementation)
        print("Great. Now I need some details about your SharePoint site.\n")
        ask_question("What is your SharePoint site URL?")
        ask_question("How do you want to authenticate?", ["Username/Password", "App ID/Secret"])
        ask_question("Please provide your credentials.", is_sensitive=True)
        ask_question("What is the name of the Document Library to index?")

    def _ask_object_storage_questions(self):
        """Asks questions specific to Object Storage."""
        print("Okay. Let's set up the connection to your object storage.\n")
        ask_question("What is the endpoint URL for the storage service? (e.g., s3.us-west-2.amazonaws.com)")
        ask_question("What is the name of the bucket?")
        ask_question("What is your Access Key ID?", is_sensitive=True)
        ask_question("What is your Secret Access Key?", is_sensitive=True)
        ask_question("Is there a specific prefix (folder) you want to sync from? (Leave blank for the whole bucket)")

    def _ask_url_list_questions(self):
        """Asks questions for processing a list of URLs."""
        print("Okay. I can process a list of URLs from a file.\n")
        ask_question("Please provide the local file path to the .txt file containing the list of URLs (one URL per line).")
        ask_question("How many links deep should the crawler follow from the initial URLs? (Enter 0 to only process the URLs in the file)")

    def _ask_csv_questions(self):
        """Asks questions for a CSV file upload."""
        print("Okay. Let's configure the CSV upload.\n")
        ask_question("What is the local file path for the CSV file you want to upload?")
        ask_question("Which column name in the CSV contains the main text content to be vectorized?")
        ask_question("Are there any other columns you'd like to include as metadata? (Provide a comma-separated list of column names)")

    def _ask_kafka_questions(self):
        """Asks questions for connecting to a Kafka topic."""
        print("Great. Let's configure the Kafka consumer.\n")
        ask_question("What are your Kafka bootstrap servers? (Provide a comma-separated list of host:port)")
        ask_question("What is the name of the Kafka topic you want to consume from?")
        ask_question("What is the consumer group ID to use?")
        ask_question("What security protocol does your cluster use?", ["PLAINTEXT", "SASL_SSL", "SSL"])
        ask_question("Please provide any necessary credentials (e.g., username/password in a connection string).", is_sensitive=True)

    def _ask_webhook_questions(self):
        """Provides information for a webhook configuration."""
        print("Okay. For this option, I will generate a unique URL for you.\n")
        print("You will be able to POST data directly to this endpoint to have it vectorized.")
        ask_question("Would you like to require a secret bearer token for authentication?", ["Yes", "No"])
        print("I will also provide documentation on the expected JSON format (e.g., {'text': 'your content here'}).")


# --- Main execution ---
if __name__ == '__main__':
    agent = ConfigurationAgent()
    
    # Simulate running the agent for the new data sources
    new_sources_to_demo = [
        "Object Storage (S3, HPOS)",
        "List of URLs",
        "CSV File Upload",
        "Kafka",
        "Webhook (Posted URL)"
    ]

    for source in new_sources_to_demo:
        agent.start_configuration(source)
