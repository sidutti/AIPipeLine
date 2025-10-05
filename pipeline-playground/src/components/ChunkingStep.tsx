import React, { useState, useMemo } from 'react';
import { ChunkingConfig } from '../App';

interface ChunkingStepProps {
  config: ChunkingConfig;
  updateConfig: (data: Partial<ChunkingConfig>) => void;
  sourceType?: string;
}

const ChunkingStep: React.FC<ChunkingStepProps> = ({ config, updateConfig, sourceType }) => {
  const [chunkingStrategy, setChunkingStrategy] = useState<string>(config.strategy || 'by-page');

  const chunkingStrategies = useMemo(() => {
    const baseStrategies = [
      {
        id: 'by-page',
        name: 'By Page',
        description: 'Split content by page boundaries'
      },
      {
        id: 'by-section',
        name: 'By Section',
        description: 'Split content by section boundaries'
      },
      {
        id: 'contextual-chunking',
        name: 'Contextual Chunking',
        description: 'Create chunks that include surrounding context for better retrieval'
      }
    ];

    // Add source-specific chunking options
    if (sourceType === 'pgp') {
      baseStrategies.push({
        id: 'pgp-chunking',
        name: 'PGP Chunking',
        description: 'Specialized chunking for PGP encrypted content'
      });
    } else if (sourceType === 'opentext') {
      baseStrategies.push({
        id: 'opentext-chunking',
        name: 'OpenText Chunking',
        description: 'Optimized chunking for OpenText documents'
      });
    } else if (sourceType === 'virtual-library') {
      baseStrategies.push({
        id: 'virtual-library-chunking',
        name: 'Virtual Library Chunking',
        description: 'Specialized chunking for Virtual Library content'
      });
    }

    return baseStrategies;
  }, [sourceType]);

  const handleStrategyChange = (strategy: string): void => {
    setChunkingStrategy(strategy);
    const defaultParams = strategy === 'contextual-chunking'
      ? { chunkSize: 800, overlap: 200, contextWindow: 1, respectHeadings: true }
      : { chunkSize: 1000, overlap: 100 };
    updateConfig({
      strategy: strategy,
      parameters: defaultParams
    });
  };

  const getDescription = (): string => {
    if (sourceType === 'pgp') {
      return 'Configure how your PGP encrypted content will be split into chunks for vector processing.';
    } else if (sourceType === 'opentext') {
      return 'Configure how your OpenText documents will be split into chunks for vector processing.';
    } else if (sourceType === 'virtual-library') {
      return 'Configure how your Virtual Library content will be split into chunks for vector processing.';
    }
    return 'Configure how your content will be split into chunks for vector processing.';
  };

  return (
    <div className="chunking-step">
      <div className="form-section">
        <p className="section-description">
          {getDescription()}
        </p>

        <div className="strategies-grid">
          {chunkingStrategies.map((strategy) => (
            <div
              key={strategy.id}
              className={`strategy-card ${chunkingStrategy === strategy.id ? 'selected' : ''}`}
              onClick={() => handleStrategyChange(strategy.id)}
            >
              <h4>{strategy.name}</h4>
              <p>{strategy.description}</p>
            </div>
          ))}
        </div>

        {sourceType && ['pgp', 'opentext', 'virtual-library'].includes(sourceType) && (
          <div className="source-specific-info">
            <h4>💡 Source-Specific Chunking</h4>
            <p>
              {sourceType === 'pgp' && 'PGP Chunking provides specialized processing for encrypted content, maintaining security while optimizing chunk boundaries.'}
              {sourceType === 'opentext' && 'OpenText Chunking leverages document structure and metadata from your OpenText system for optimal chunking.'}
              {sourceType === 'virtual-library' && 'Virtual Library Chunking uses collection metadata and document relationships for intelligent chunking.'}
            </p>
          </div>
        )}

        <div className="validation-status">
          <div className="validation-success">
            ✓ Chunking strategy configured: {chunkingStrategies.find(s => s.id === chunkingStrategy)?.name}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ChunkingStep;