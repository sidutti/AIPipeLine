# Webhook JSON Path Configuration Examples

The webhook configuration now supports flexible JSON path extraction to handle any JSON structure for vectoring pipelines.

## Configuration Fields

### Core JSON Path Fields

1. **Corpus Text Path** - The main content to be vectorized
2. **Document ID Path** - Unique identifier for the document
3. **Timestamp Path** - When the document was created/modified
4. **Timestamp Format** - How to parse the timestamp
5. **Metadata Paths** - Additional fields to extract as metadata
6. **Flatten Arrays** - Whether to create separate records for array elements

## JSON Path Syntax

We use JSONPath syntax (`$.path.to.field`) to extract values from incoming JSON:

- `$` - Root of JSON document
- `.` - Child operator
- `[]` - Array index operator
- `*` - Wildcard (all elements)

## Example Configurations

### Example 1: Simple Blog Post
**Input JSON:**
```json
{
  "id": "post-123",
  "title": "My Blog Post",
  "content": "This is the main content to be vectorized",
  "author": "John Doe",
  "created_at": "2024-01-15T10:30:00Z",
  "tags": ["tech", "ai", "ml"]
}
```

**Configuration:**
- Corpus Text Path: `$.content`
- Document ID Path: `$.id`
- Timestamp Path: `$.created_at`
- Timestamp Format: `ISO8601`
- Metadata Paths: `title:$.title,author:$.author,tags:$.tags`
- Flatten Arrays: `no`

### Example 2: Nested Document Structure
**Input JSON:**
```json
{
  "document": {
    "uuid": "doc-456",
    "metadata": {
      "title": "Research Paper",
      "authors": ["Alice Smith", "Bob Johnson"],
      "department": "Engineering"
    },
    "body": {
      "abstract": "This paper discusses...",
      "full_text": "The complete research content here..."
    }
  },
  "timestamp": 1705320600
}
```

**Configuration:**
- Corpus Text Path: `$.document.body.full_text`
- Document ID Path: `$.document.uuid`
- Timestamp Path: `$.timestamp`
- Timestamp Format: `epoch`
- Metadata Paths: `title:$.document.metadata.title,authors:$.document.metadata.authors,department:$.document.metadata.department,abstract:$.document.body.abstract`
- Flatten Arrays: `no`

### Example 3: Array of Documents (Flattened)
**Input JSON:**
```json
{
  "batch_id": "batch-789",
  "documents": [
    {
      "id": "doc1",
      "text": "First document content",
      "category": "news"
    },
    {
      "id": "doc2",
      "text": "Second document content",
      "category": "blog"
    }
  ],
  "processed_at": "2024-01-15 10:30:00"
}
```

**Configuration:**
- Corpus Text Path: `$.documents[*].text`
- Document ID Path: `$.documents[*].id`
- Timestamp Path: `$.processed_at`
- Timestamp Format: `yyyy-MM-dd HH:mm:ss`
- Metadata Paths: `category:$.documents[*].category,batch_id:$.batch_id`
- Flatten Arrays: `yes`

### Example 4: Chat/Message System
**Input JSON:**
```json
{
  "conversation_id": "conv-101",
  "messages": [
    {
      "message_id": "msg-1",
      "sender": "user@example.com",
      "content": "Hello, I need help with...",
      "timestamp": "2024-01-15T10:30:00Z",
      "type": "user"
    },
    {
      "message_id": "msg-2",
      "sender": "support@company.com",
      "content": "I'd be happy to help you with that...",
      "timestamp": "2024-01-15T10:32:00Z",
      "type": "support"
    }
  ]
}
```

**Configuration:**
- Corpus Text Path: `$.messages[*].content`
- Document ID Path: `$.messages[*].message_id`
- Timestamp Path: `$.messages[*].timestamp`
- Timestamp Format: `ISO8601`
- Metadata Paths: `sender:$.messages[*].sender,type:$.messages[*].type,conversation_id:$.conversation_id`
- Flatten Arrays: `yes`

### Example 5: E-commerce Product Data
**Input JSON:**
```json
{
  "product": {
    "sku": "PROD-001",
    "name": "Wireless Headphones",
    "description": "High-quality wireless headphones with noise cancellation...",
    "details": {
      "brand": "TechCorp",
      "model": "WH-1000XM5",
      "price": 299.99,
      "specifications": "Bluetooth 5.0, 30-hour battery life..."
    },
    "reviews": [
      {
        "id": "rev-1",
        "rating": 5,
        "comment": "Excellent sound quality and comfort"
      }
    ]
  },
  "updated": "2024-01-15T10:30:00Z"
}
```

**Configuration:**
- Corpus Text Path: `$.product.description`
- Document ID Path: `$.product.sku`
- Timestamp Path: `$.updated`
- Timestamp Format: `ISO8601`
- Metadata Paths: `name:$.product.name,brand:$.product.details.brand,model:$.product.details.model,price:$.product.details.price,specifications:$.product.details.specifications`
- Flatten Arrays: `no`

## Timestamp Formats

Supported timestamp formats:
- `ISO8601` - Standard ISO format (2024-01-15T10:30:00Z)
- `epoch` - Unix timestamp (1705320600)
- `yyyy-MM-dd HH:mm:ss` - Custom format
- `yyyy-MM-dd` - Date only
- `MM/dd/yyyy HH:mm:ss` - US format
- `dd-MM-yyyy HH:mm:ss` - European format

## Best Practices

1. **Test Your Paths**: Use a JSONPath tester online to verify your expressions work
2. **Handle Missing Fields**: Use optional paths when fields might not exist
3. **Metadata Strategy**: Choose the most relevant metadata for your use case
4. **Array Handling**: Consider whether you want separate vectors for array items
5. **Content Length**: Ensure corpus text paths point to substantial content
6. **ID Uniqueness**: Make sure ID paths produce unique identifiers

## Common Patterns

- **Simple flat structure**: `$.field_name`
- **Nested object**: `$.parent.child.field`
- **Array element**: `$.array[0].field` (specific index)
- **All array elements**: `$.array[*].field` (with flatten enabled)
- **Deep nesting**: `$.level1.level2.level3.field`

## Troubleshooting

- If a path returns no data, check the JSON structure
- For arrays, decide whether to flatten or take the first element
- Test timestamp parsing with your actual timestamp format
- Verify metadata field names don't conflict with system fields