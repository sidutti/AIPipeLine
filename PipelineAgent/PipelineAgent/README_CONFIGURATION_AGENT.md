# Pipeline Configuration Agent

An interactive Spring WebFlux agent that guides users through configuring complete vectoring pipelines with both data sources and targets. The agent uses a two-phase approach:

1. **Source Configuration Phase**: Configures the data source (where data comes from)
2. **Target Configuration Phase**: Configures the vectoring target (API key and use case name)

## Supported Data Sources

1. **Confluence** - Atlassian Confluence wiki pages and attachments
2. **ECM Repositories** - Git, SVN, Mercurial code repositories
3. **SharePoint** - Microsoft SharePoint documents and libraries
4. **Object Storage** - AWS S3, Azure Blob, Google Cloud Storage, HPOS
5. **URL Lists** - Web crawling with configurable depth and filters
6. **CSV Files** - Structured data from uploaded CSV files
7. **Kafka** - Real-time streaming data from Kafka topics
8. **Webhooks** - HTTP endpoints for receiving data

## API Endpoints

### Get Available Data Sources
```
GET /api/configuration/datasources
```

### Start Configuration
```
POST /api/configuration/start
{
  "dataSourceType": "confluence"
}
```

### Answer Questions
```
POST /api/configuration/answer
{
  "sessionId": "uuid",
  "answer": "your answer"
}
```

### Check Status
```
GET /api/configuration/status/{sessionId}
```

### Get Help for Data Source
```
GET /api/configuration/help/{dataSourceType}
```

## Usage Example

1. **Start Configuration**:
   ```bash
   curl -X POST http://localhost:8080/api/configuration/start \
   -H "Content-Type: application/json" \
   -d '{"dataSourceType": "confluence"}'
   ```

2. **Answer Source Questions** (Phase 1):
   ```bash
   curl -X POST http://localhost:8080/api/configuration/answer \
   -H "Content-Type: application/json" \
   -d '{"sessionId": "session-id", "answer": "https://mycompany.atlassian.net"}'
   ```
   Continue answering all source-specific questions...

3. **Answer Target Questions** (Phase 2 - automatically triggered):
   ```bash
   curl -X POST http://localhost:8080/api/configuration/answer \
   -H "Content-Type: application/json" \
   -d '{"sessionId": "session-id", "answer": "sk-your-api-key"}'

   curl -X POST http://localhost:8080/api/configuration/answer \
   -H "Content-Type: application/json" \
   -d '{"sessionId": "session-id", "answer": "My Use Case Name"}'
   ```

The response will include a `phase` field indicating whether you're in the "source", "target", or "completed" phase.

## Configuration Questions by Data Source

### Confluence
- Base URL
- Username/Email
- API Token
- Space Keys
- Include Attachments
- Maximum Pages
- Last Modified Filter

### ECM Repository
- Repository Type (git/svn/mercurial)
- Repository URL
- Branch/Tag
- Authentication (username/password/SSH key)
- Include/Exclude Paths
- File Extensions
- Version History
- Maximum Commits

### SharePoint
- Site URL
- Tenant ID
- Client ID/Secret
- Document Libraries
- Folder Paths
- File Types
- Metadata Inclusion
- Version History
- Date Filters

### Object Storage
- Storage Type (S3/Azure/GCS/HPOS)
- Endpoint URL
- Access/Secret Keys
- Bucket Name
- Region
- Prefixes
- File Extensions
- Metadata
- Size Limits

### URL List
- URLs to crawl
- Crawl depth
- External link following
- Domain restrictions
- Exclude patterns
- Custom headers
- Rate limiting
- Robots.txt compliance

### CSV
- File path
- Delimiter
- Quote/Escape characters
- Header row presence
- Column mappings
- Encoding
- Skip lines
- Record limits
- Date formats
- Required columns

### Kafka
- Bootstrap servers
- Topic name
- Consumer group
- Offset reset strategy
- Serialization
- Authentication (SASL)
- Connection properties

### Webhook
- Endpoint URL
- HTTP method
- Custom headers
- Authentication
- SSL validation
- Timeout/retry settings
- **JSON Path Configuration**:
  - Corpus text extraction path
  - Document ID path
  - Timestamp path and format
  - Metadata field mappings
  - Array flattening options

## Running the Application

1. Build the project:
   ```bash
   ./gradlew build
   ```

2. Run the application:
   ```bash
   ./gradlew bootRun
   ```

3. The API will be available at `http://localhost:8080`

## Testing

Run the tests:
```bash
./gradlew test
```

## Target Configuration Questions

After completing the source configuration, the agent automatically moves to the target phase with these questions:

1. **API Key**: Your API key for the custom vectoring service
2. **Use Case Name**: A descriptive name for this vectoring pipeline use case

## Configuration Output

The agent generates a complete `PipelineConfig` object containing:
- **source**: Fully configured data source with all connection and processing parameters
- **target**: Target configuration with API key and use case name
- **configurationId**: Unique identifier for the pipeline configuration
- **createdTimestamp**: When the configuration was created

Example final output:
```json
{
  "source": {
    "type": "confluence",
    "name": "Company Wiki",
    "baseUrl": "https://company.atlassian.net",
    "username": "user@company.com",
    "spaceKeys": ["SPACE1", "SPACE2"],
    "includeAttachments": true,
    "maxPages": 1000
  },
  "target": {
    "apiKey": "[REDACTED]",
    "useCaseName": "Knowledge Base Analytics"
  },
  "configurationId": "session-uuid",
  "createdTimestamp": 1694123456789
}
```