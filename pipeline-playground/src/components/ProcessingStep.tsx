import React, {JSX, useState} from 'react';
import { ProcessingConfig } from '../App';

interface ProcessingStepProps {
  config: ProcessingConfig;
  updateConfig: (data: Partial<ProcessingConfig>) => void;
}

interface ProcessingOption {
  id: string;
  name: string;
  description: string;
  parameters: Record<string, ParameterConfig>;
}

interface ParameterConfig {
  type: 'boolean' | 'number' | 'select' | 'text';
  label: string;
  default: any;
  min?: number;
  max?: number;
  step?: number;
  options?: string[];
  placeholder?: string;
}

const ProcessingStep: React.FC<ProcessingStepProps> = ({ config, updateConfig }) => {
  const processingOptions: ProcessingOption[] = [
    {
      id: 'text-extraction',
      name: 'Text Extraction',
      description: 'Extract text content from documents',
      parameters: {
        preserveFormatting: { type: 'boolean', label: 'Preserve formatting', default: false },
        extractTables: { type: 'boolean', label: 'Extract tables', default: true },
        ocrEnabled: { type: 'boolean', label: 'Enable OCR for images', default: false },
        language: { type: 'select', label: 'Language', options: ['auto', 'en', 'es', 'fr', 'de', 'zh'], default: 'auto' }
      }
    },
    {
      id: 'metadata-extraction',
      name: 'Metadata Extraction',
      description: 'Extract document metadata and properties',
      parameters: {
        extractAuthor: { type: 'boolean', label: 'Extract author', default: true },
        extractCreationDate: { type: 'boolean', label: 'Extract creation date', default: true },
        extractKeywords: { type: 'boolean', label: 'Extract keywords', default: false },
        extractCustomProperties: { type: 'boolean', label: 'Extract custom properties', default: false }
      }
    },
    {
      id: 'entity-extraction',
      name: 'Entity Extraction',
      description: 'Extract named entities from content',
      parameters: {
        extractPersons: { type: 'boolean', label: 'Extract persons', default: true },
        extractOrganizations: { type: 'boolean', label: 'Extract organizations', default: true },
        extractLocations: { type: 'boolean', label: 'Extract locations', default: true },
        extractDates: { type: 'boolean', label: 'Extract dates', default: false },
        customEntityTypes: { type: 'text', label: 'Custom entity types (comma-separated)', default: '' }
      }
    },
  ];

  const [selectedProcessors, setSelectedProcessors] = useState<string[]>(
    (config.processors || []).filter(id => processingOptions.some(p => p.id === id))
  );

  const handleProcessorToggle = (processorId: string): void => {
    const isCurrentlySelected = selectedProcessors.includes(processorId);
    let newProcessors: string[];

    if (isCurrentlySelected) {
      newProcessors = selectedProcessors.filter(id => id !== processorId);
    } else {
      newProcessors = [...selectedProcessors, processorId];
    }

    setSelectedProcessors(newProcessors);
    updateConfig({
      processors: newProcessors,
      parameters: config.parameters
    });
  };

  const handleParameterChange = (processorId: string, paramName: string, value: any): void => {
    updateConfig({
      processors: selectedProcessors,
      parameters: {
        ...config.parameters,
        [processorId]: {
          ...config.parameters[processorId],
          [paramName]: value
        }
      }
    });
  };

  const renderParameterField = (processorId: string, paramName: string, paramConfig: ParameterConfig): JSX.Element | null => {
    const currentValue = config.parameters[processorId]?.[paramName] ?? paramConfig.default;
    const fieldId = `${processorId}-${paramName}`;

    switch (paramConfig.type) {
      case 'boolean':
        return (
          <div key={paramName} className="form-group">
            <label className="checkbox-label">
              <input
                type="checkbox"
                id={fieldId}
                checked={currentValue}
                onChange={(e) => handleParameterChange(processorId, paramName, e.target.checked)}
              />
              {paramConfig.label}
            </label>
          </div>
        );

      case 'number':
        return (
          <div key={paramName} className="form-group">
            <label htmlFor={fieldId}>{paramConfig.label}</label>
            <input
              type="number"
              id={fieldId}
              value={currentValue}
              onChange={(e) => handleParameterChange(processorId, paramName, parseFloat(e.target.value))}
              min={paramConfig.min}
              max={paramConfig.max}
              step={paramConfig.step}
              className="form-input"
            />
          </div>
        );

      case 'select':
        return (
          <div key={paramName} className="form-group">
            <label htmlFor={fieldId}>{paramConfig.label}</label>
            <select
              id={fieldId}
              value={currentValue}
              onChange={(e) => handleParameterChange(processorId, paramName, e.target.value)}
              className="form-select"
            >
              {paramConfig.options?.map(option => (
                <option key={option} value={option}>
                  {option.charAt(0).toUpperCase() + option.slice(1)}
                </option>
              ))}
            </select>
          </div>
        );

      case 'text':
        return (
          <div key={paramName} className="form-group">
            <label htmlFor={fieldId}>{paramConfig.label}</label>
            <input
              type="text"
              id={fieldId}
              value={currentValue}
              onChange={(e) => handleParameterChange(processorId, paramName, e.target.value)}
              placeholder={paramConfig.placeholder}
              className="form-input"
            />
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="processing-step">
      <div className="form-section">
        <p className="section-description">
          Select the processing operations to apply to your content. These operations will be executed in the order shown.
        </p>

        <div className="processors-selection">
          <h3>Available Processors</h3>
          <div className="processors-grid">
            {processingOptions.map((processor) => (
              <div
                key={processor.id}
                className={`processor-card ${selectedProcessors.includes(processor.id) ? 'selected' : ''}`}
              >
                <div className="processor-header">
                  <label className="processor-toggle">
                    <input
                      type="checkbox"
                      checked={selectedProcessors.includes(processor.id)}
                      onChange={() => handleProcessorToggle(processor.id)}
                    />
                    <h4>{processor.name}</h4>
                  </label>
                </div>
                <p className="processor-description">{processor.description}</p>

                {selectedProcessors.includes(processor.id) && (
                  <div className="processor-parameters">
                    <h5>Parameters</h5>
                    {Object.entries(processor.parameters).map(([paramName, paramConfig]) =>
                      renderParameterField(processor.id, paramName, paramConfig)
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>

        {selectedProcessors.length === 0 && (
          <div className="no-processors-message">
            <p>No processors selected. You can skip this step or select processors to enhance your content processing.</p>
          </div>
        )}

        <div className="processing-order">
          {selectedProcessors.length > 1 && (
            <div>
              <h3>Processing Order</h3>
              <div className="order-list">
                {selectedProcessors.map((processorId, index) => {
                  const processor = processingOptions.find(p => p.id === processorId);
                  return (
                    <div key={processorId} className="order-item">
                      <span className="order-number">{index + 1}</span>
                      <span className="processor-name">{processor?.name}</span>
                    </div>
                  );
                })}
              </div>
              <p className="help-text">
                Processors will be executed in this order. To change the order, unselect and reselect processors in your preferred sequence.
              </p>
            </div>
          )}
        </div>

        <div className="validation-status">
          {selectedProcessors.length > 0 ? (
            <div className="validation-success">
              ✓ {selectedProcessors.length} processor{selectedProcessors.length > 1 ? 's' : ''} selected
            </div>
          ) : (
            <div className="validation-info">
              ℹ Processing step is optional - you can continue without selecting processors
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProcessingStep;