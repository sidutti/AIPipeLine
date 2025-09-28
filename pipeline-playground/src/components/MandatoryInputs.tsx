import React from 'react';
import { PipelineConfig } from '../App';

interface MandatoryInputsProps {
  config: PipelineConfig;
  updateConfig: (field: keyof PipelineConfig, value: string) => void;
}

const MandatoryInputs: React.FC<MandatoryInputsProps> = ({ config, updateConfig }) => {
  const handleInputChange = (field: keyof PipelineConfig, value: string): void => {
    updateConfig(field, value);
  };

  return (
    <div className="mandatory-inputs">
      <div className="form-section">
        <p className="section-description">
          Please provide the required information to configure your pipeline.
        </p>

        <div className="form-group">
          <label htmlFor="usecase" className="required">
            Use Case *
          </label>
          <input
            type="text"
            id="usecase"
            value={config.usecase}
            onChange={(e) => handleInputChange('usecase', e.target.value)}
            placeholder="Enter your use case (e.g., Document Processing, Data Extraction, Content Analysis)"
            className="form-input"
            required
          />
          <span className="help-text">
            Describe what you want to accomplish with this pipeline
          </span>
        </div>

        <div className="form-group">
          <label htmlFor="apikey" className="required">
            API Key *
          </label>
          <input
            type="password"
            id="apikey"
            value={config.apikey}
            onChange={(e) => handleInputChange('apikey', e.target.value)}
            placeholder="Enter your API key"
            className="form-input"
            required
          />
          <span className="help-text">
            Your authentication key for accessing the pipeline services
          </span>
        </div>

        <div className="validation-status">
          {config.usecase && config.apikey ? (
            <div className="validation-success">
              ✓ All mandatory fields completed
            </div>
          ) : (
            <div className="validation-warning">
              ⚠ Please complete all mandatory fields to continue
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MandatoryInputs;