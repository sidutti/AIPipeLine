import React, { useState } from 'react';
import { ChunkHydrationConfig } from '../App';

interface ChunkHydrationStepProps {
  config: ChunkHydrationConfig;
  updateConfig: (data: Partial<ChunkHydrationConfig>) => void;
}

const ChunkHydrationStep: React.FC<ChunkHydrationStepProps> = ({ config, updateConfig }) => {
  const [selectedHydrators, setSelectedHydrators] = useState<string[]>(config.hydrators || []);

  const hydrationOptions = [
    {
      id: 'vector-embeddings',
      name: 'Vector Embeddings',
      description: 'Generate vector embeddings for semantic search'
    },
    {
      id: 'metadata-enrichment',
      name: 'Metadata Enrichment',
      description: 'Add contextual metadata to chunks'
    }
  ];

  const handleHydratorToggle = (hydratorId: string): void => {
    const isCurrentlySelected = selectedHydrators.includes(hydratorId);
    let newHydrators: string[];

    if (isCurrentlySelected) {
      newHydrators = selectedHydrators.filter(id => id !== hydratorId);
    } else {
      newHydrators = [...selectedHydrators, hydratorId];
    }

    setSelectedHydrators(newHydrators);
    updateConfig({
      hydrators: newHydrators,
      parameters: config.parameters || {}
    });
  };

  return (
    <div className="chunk-hydration-step">
      <div className="form-section">
        <p className="section-description">
          Configure chunk hydration to enrich your content chunks with additional metadata and embeddings.
        </p>

        <div className="hydrators-grid">
          {hydrationOptions.map((hydrator) => (
            <div
              key={hydrator.id}
              className={`hydrator-card ${selectedHydrators.includes(hydrator.id) ? 'selected' : ''}`}
            >
              <div className="hydrator-header">
                <label className="hydrator-toggle">
                  <input
                    type="checkbox"
                    checked={selectedHydrators.includes(hydrator.id)}
                    onChange={() => handleHydratorToggle(hydrator.id)}
                  />
                  <h4>{hydrator.name}</h4>
                </label>
              </div>
              <p className="hydrator-description">{hydrator.description}</p>
            </div>
          ))}
        </div>

        <div className="validation-status">
          {selectedHydrators.length > 0 ? (
            <div className="validation-success">
              ✓ {selectedHydrators.length} hydrator{selectedHydrators.length > 1 ? 's' : ''} configured
            </div>
          ) : (
            <div className="validation-info">
              ℹ Chunk hydration is optional but recommended for better search performance
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ChunkHydrationStep;