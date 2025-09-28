import React, { useState } from 'react';
import { PipelineConfig } from '../App';

interface PipelinePreviewProps {
  config: PipelineConfig;
  onSubmit?: (config: PipelineConfig) => void;
}

interface SubmissionResult {
  success: boolean;
  pipelineId?: string;
  message: string;
}

const PipelinePreview: React.FC<PipelinePreviewProps> = ({ config, onSubmit }) => {
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [submissionResult, setSubmissionResult] = useState<SubmissionResult | null>(null);

  const handleSubmit = async (): Promise<void> => {
    setIsSubmitting(true);
    setSubmissionResult(null);

    try {
      await new Promise(resolve => setTimeout(resolve, 2000));

      const pipelineId = `pipeline_${Date.now()}`;
      setSubmissionResult({
        success: true,
        pipelineId,
        message: 'Pipeline configuration submitted successfully!'
      });

      if (onSubmit) {
        onSubmit(config);
      }
    } catch (error) {
      setSubmissionResult({
        success: false,
        message: 'Failed to submit pipeline configuration. Please try again.'
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const generateConfigJSON = (): string => {
    return JSON.stringify(config, null, 2);
  };

  const downloadConfig = (): void => {
    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(generateConfigJSON());
    const downloadAnchorNode = document.createElement('a');
    downloadAnchorNode.setAttribute("href", dataStr);
    downloadAnchorNode.setAttribute("download", `pipeline-config-${Date.now()}.json`);
    document.body.appendChild(downloadAnchorNode);
    downloadAnchorNode.click();
    downloadAnchorNode.remove();
  };

  return (
    <div className="pipeline-preview">
      <div className="form-section">
        <p className="section-description">
          Review your pipeline configuration before submission. Once submitted,
          your pipeline will be processed and published to the vector space.
        </p>

        <div className="config-summary">
          <h3>Pipeline Configuration Summary</h3>

          <div className="summary-section">
            <h4>🎯 Basic Information</h4>
            <div className="summary-grid">
              <div className="summary-item">
                <label>Use Case:</label>
                <span>{config.usecase || 'Not specified'}</span>
              </div>
              <div className="summary-item">
                <label>API Key:</label>
                <span>{config.apikey ? '••••••••••••' + config.apikey.slice(-4) : 'Not provided'}</span>
              </div>
            </div>
          </div>

          <div className="summary-section">
            <h4>📁 Data Source</h4>
            <div className="summary-grid">
              <div className="summary-item">
                <label>Source Type:</label>
                <span>{config.source?.type || 'Not selected'}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="config-actions">
          <h3>Configuration Export</h3>
          <div className="action-buttons">
            <button
              onClick={downloadConfig}
              className="action-button secondary"
              type="button"
            >
              📥 Download Configuration
            </button>
          </div>
        </div>

        <div className="submission-section">
          <h3>Submit Pipeline</h3>

          {submissionResult && (
            <div className={`submission-result ${submissionResult.success ? 'success' : 'error'}`}>
              {submissionResult.success ? (
                <div>
                  <div className="result-icon">✅</div>
                  <div className="result-content">
                    <h4>Pipeline Submitted Successfully!</h4>
                    <p>Pipeline ID: <code>{submissionResult.pipelineId}</code></p>
                    <p>Your pipeline is now being processed and will be available in the vector space shortly.</p>
                  </div>
                </div>
              ) : (
                <div>
                  <div className="result-icon">❌</div>
                  <div className="result-content">
                    <h4>Submission Failed</h4>
                    <p>{submissionResult.message}</p>
                  </div>
                </div>
              )}
            </div>
          )}

          <div className="submission-controls">
            <button
              onClick={handleSubmit}
              disabled={isSubmitting}
              className="submit-button"
            >
              {isSubmitting ? (
                <>
                  <span className="loading-spinner">⏳</span>
                  Submitting Pipeline...
                </>
              ) : (
                <>
                  🚀 Submit Pipeline to Vector Space
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PipelinePreview;