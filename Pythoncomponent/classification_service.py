import logging
import os
import re
from collections import Counter
from datetime import datetime
from typing import List, Optional

import openai
import uvicorn
from elasticsearch import Elasticsearch
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Document Classification Service", version="1.0.0")

# Set OpenAI API key
openai.api_key = os.getenv("OPENAI_API_KEY")

class ClassificationRequest(BaseModel):
    cluster_id: str
    document_contents: List[str]
    classification_type: Optional[str] = "financial_document"

class ClassificationResponse(BaseModel):
    cluster_id: str
    classification: str
    confidence: float
    keywords: List[str]
    processing_time: float
    document_count: int

class DocumentClassificationService:
    def __init__(self):
        self.es = Elasticsearch([{'host': 'localhost', 'port': 9200}])
        self.financial_categories = [
            "bank_statements",
            "loan_documents",
            "insurance_policies",
            "tax_documents",
            "investment_reports",
            "credit_reports",
            "mortgage_documents",
            "financial_statements",
            "invoices",
            "receipts",
            "contracts",
            "regulatory_filings",
            "compliance_documents",
            "audit_reports",
            "other_financial"
        ]
    
    def extract_keywords(self, text_contents: List[str], top_k: int = 10) -> List[str]:
        """Extract key terms from document contents"""
        # Combine all text
        combined_text = " ".join(text_contents).lower()
        
        # Remove common stop words and extract meaningful terms
        financial_terms = [
            "account", "balance", "payment", "loan", "credit", "debit", "transaction",
            "statement", "invoice", "receipt", "interest", "principal", "mortgage",
            "insurance", "policy", "premium", "claim", "tax", "income", "expense",
            "revenue", "profit", "loss", "asset", "liability", "equity", "investment",
            "portfolio", "security", "bond", "stock", "dividend", "capital", "fund",
            "bank", "banking", "financial", "finance", "money", "dollar", "currency",
            "deposit", "withdrawal", "transfer", "check", "cheque", "wire", "ach"
        ]
        
        # Count occurrences of financial terms
        term_counts = Counter()
        for term in financial_terms:
            count = len(re.findall(r'\b' + term + r'\b', combined_text))
            if count > 0:
                term_counts[term] = count
        
        # Return top keywords
        return [term for term, count in term_counts.most_common(top_k)]
    
    async def classify_with_llm(self, text_contents: List[str], keywords: List[str]) -> tuple:
        """Use OpenAI to classify the document cluster"""
        try:
            # Prepare sample text for classification
            sample_text = "\n".join(text_contents[:5])  # Use first 5 documents as sample
            if len(sample_text) > 3000:
                sample_text = sample_text[:3000]
            
            # Create classification prompt
            prompt = f"""
            Analyze the following financial documents and classify them into one of these categories:
            
            Categories:
            - bank_statements: Monthly or periodic bank account statements
            - loan_documents: Loan applications, agreements, or related paperwork
            - insurance_policies: Insurance policies, claims, or coverage documents
            - tax_documents: Tax returns, forms, or tax-related paperwork
            - investment_reports: Investment statements, portfolio reports, or market analysis
            - credit_reports: Credit scores, credit history, or credit-related documents
            - mortgage_documents: Mortgage applications, agreements, or related paperwork
            - financial_statements: Company financial statements, balance sheets, income statements
            - invoices: Bills, invoices, or payment requests
            - receipts: Payment receipts, transaction confirmations
            - contracts: Financial contracts, agreements, or legal documents
            - regulatory_filings: SEC filings, regulatory compliance documents
            - compliance_documents: Compliance reports, audit trails, regulatory documents
            - audit_reports: Internal or external audit reports
            - other_financial: Any other financial documents not fitting above categories
            
            Key terms found in documents: {', '.join(keywords)}
            
            Sample document content:
            {sample_text}
            
            Based on the content and key terms, classify these documents and provide:
            1. The most appropriate category from the list above
            2. A confidence score from 0.0 to 1.0
            3. A brief explanation (one sentence)
            
            Respond in this exact format:
            Category: [category_name]
            Confidence: [0.0-1.0]
            Explanation: [brief explanation]
            """
            
            response = openai.ChatCompletion.create(
                model="gpt-3.5-turbo",
                messages=[
                    {"role": "system", "content": "You are a financial document classification expert."},
                    {"role": "user", "content": prompt}
                ],
                max_tokens=200,
                temperature=0.1
            )
            
            result = response.choices[0].message.content.strip()
            
            # Parse the response
            category = None
            confidence = 0.5
            
            for line in result.split('\n'):
                if line.startswith('Category:'):
                    category = line.split(':', 1)[1].strip()
                elif line.startswith('Confidence:'):
                    try:
                        confidence = float(line.split(':', 1)[1].strip())
                    except:
                        confidence = 0.5
            
            # Validate category
            if category not in self.financial_categories:
                category = "other_financial"
                confidence = 0.3
            
            return category, confidence
            
        except Exception as e:
            logger.error(f"Error in LLM classification: {str(e)}")
            # Fallback classification based on keywords
            return self.fallback_classification(keywords)
    
    def fallback_classification(self, keywords: List[str]) -> tuple:
        """Fallback classification based on keywords when LLM fails"""
        keyword_mapping = {
            "bank_statements": ["statement", "balance", "account", "bank", "deposit", "withdrawal"],
            "loan_documents": ["loan", "credit", "principal", "interest", "mortgage"],
            "insurance_policies": ["insurance", "policy", "premium", "claim", "coverage"],
            "tax_documents": ["tax", "income", "irs", "1099", "w2", "return"],
            "investment_reports": ["investment", "portfolio", "stock", "bond", "dividend"],
            "invoices": ["invoice", "bill", "payment", "due", "amount"],
            "receipts": ["receipt", "transaction", "purchase", "paid"]
        }
        
        scores = {}
        for category, category_keywords in keyword_mapping.items():
            score = sum(1 for keyword in keywords if keyword in category_keywords)
            if score > 0:
                scores[category] = score
        
        if scores:
            best_category = max(scores, key=scores.get)
            confidence = min(scores[best_category] / len(keywords), 1.0) if keywords else 0.3
            return best_category, confidence
        
        return "other_financial", 0.3
    
    async def update_document_classifications(self, cluster_id: str, classification: str, confidence: float):
        """Update MongoDB documents with classification results"""
        try:
            # This would typically update the MongoDB collection
            # For now, we'll just log the update
            logger.info(f"Would update documents in cluster {cluster_id} with classification: {classification} (confidence: {confidence})")
        except Exception as e:
            logger.error(f"Error updating document classifications: {str(e)}")

classification_service = DocumentClassificationService()

@app.post("/classify", response_model=ClassificationResponse)
async def classify_cluster(request: ClassificationRequest):
    """Classify a cluster of documents"""
    try:
        start_time = datetime.now()
        
        if not request.document_contents:
            raise HTTPException(status_code=400, detail="No document contents provided")
        
        # Extract keywords from document contents
        keywords = classification_service.extract_keywords(request.document_contents)
        
        # Classify using LLM
        classification, confidence = await classification_service.classify_with_llm(
            request.document_contents, keywords
        )
        
        # Update document classifications
        await classification_service.update_document_classifications(
            request.cluster_id, classification, confidence
        )
        
        processing_time = (datetime.now() - start_time).total_seconds()
        
        return ClassificationResponse(
            cluster_id=request.cluster_id,
            classification=classification,
            confidence=confidence,
            keywords=keywords,
            processing_time=processing_time,
            document_count=len(request.document_contents)
        )
        
    except Exception as e:
        logger.error(f"Error in classification: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "service": "classification-service"}

@app.get("/categories")
async def get_categories():
    """Get available classification categories"""
    return {
        "categories": classification_service.financial_categories,
        "description": "Available financial document categories for classification"
    }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8002)