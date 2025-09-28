import React, {JSX, useState} from 'react';
import './App.css';
import MandatoryInputs from './components/MandatoryInputs';
import SourceSelector from './components/SourceSelector';
import ProcessingStep from './components/ProcessingStep';
import ChunkingStep from './components/ChunkingStep';
import ChunkHydrationStep from './components/ChunkHydrationStep';
import PipelinePreview from './components/PipelinePreview';

// Type definitions
export interface SourceConfig {
  type: string;
  parameters: Record<string, any>;
}

export interface ProcessingConfig {
  processors?: string[];
  parameters: Record<string, Record<string, any>>;
}

export interface ChunkingConfig {
  strategy?: string;
  parameters: Record<string, any>;
}

export interface ChunkHydrationConfig {
  hydrators?: string[];
  parameters: Record<string, Record<string, any>>;
}

export interface PipelineConfig {
  usecase: string;
  apikey: string;
  source: SourceConfig;
  processing: ProcessingConfig;
  chunking: ChunkingConfig;
  chunkHydration: ChunkHydrationConfig;
}

function App(): JSX.Element {
  const [pipelineConfig, setPipelineConfig] = useState<PipelineConfig>({
    usecase: '',
    apikey: '',
    source: {
      type: '',
      parameters: {}
    },
    processing: {
      parameters: {}
    },
    chunking: {
      parameters: {}
    },
    chunkHydration: {
      parameters: {}
    }
  });

  const [currentStep, setCurrentStep] = useState<number>(0);

  const updatePipelineConfig = (section: keyof PipelineConfig, data: any): void => {
    setPipelineConfig(prev => ({
      ...prev,
      [section]: typeof data === 'object' && !Array.isArray(data) && data !== null
        ? data
        : data
    }));
  };

  const steps: string[] = [
    'Mandatory Inputs',
    'Source Selection',
    'Processing Configuration',
    'Chunking Configuration',
    'Chunk Hydration',
    'Preview & Submit'
  ];

  const isStepValid = (stepIndex: number): boolean => {
    switch (stepIndex) {
      case 0:
        return !!(pipelineConfig.usecase && pipelineConfig.apikey);
      case 1:
        return !!pipelineConfig.source.type;
      case 2:
        return true; // Processing is optional
      case 3:
        return true; // Chunking parameters depend on configuration
      case 4:
        return true; // Chunk hydration parameters depend on configuration
      case 5:
        return true; // Preview step
      default:
        return false;
    }
  };

  const nextStep = (): void => {
    if (currentStep < steps.length - 1 && isStepValid(currentStep)) {
      setCurrentStep(currentStep + 1);
    }
  };

  const prevStep = (): void => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1);
    }
  };

  const renderCurrentStep = (): JSX.Element | null => {
    switch (currentStep) {
      case 0:
        return (
          <MandatoryInputs
            config={pipelineConfig}
            updateConfig={updatePipelineConfig}
          />
        );
      case 1:
        return (
          <SourceSelector
            config={pipelineConfig.source}
            updateConfig={(data: Partial<SourceConfig>) => updatePipelineConfig('source', data)}
          />
        );
      case 2:
        return (
          <ProcessingStep
            config={pipelineConfig.processing}
            updateConfig={(data: Partial<ProcessingConfig>) => updatePipelineConfig('processing', data)}
          />
        );
      case 3:
        return (
          <ChunkingStep
            config={pipelineConfig.chunking}
            updateConfig={(data: Partial<ChunkingConfig>) => updatePipelineConfig('chunking', data)}
            sourceType={pipelineConfig.source.type}
          />
        );
      case 4:
        return (
          <ChunkHydrationStep
            config={pipelineConfig.chunkHydration}
            updateConfig={(data: Partial<ChunkHydrationConfig>) => updatePipelineConfig('chunkHydration', data)}
          />
        );
      case 5:
        return (
          <PipelinePreview
            config={pipelineConfig}
            onSubmit={(config: PipelineConfig) => console.log('Pipeline submitted:', config)}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>Pipeline Playground</h1>
        <p>Configure your data processing pipeline</p>
      </header>

      <div className="pipeline-wizard">
        <div className="step-indicator">
          {steps.map((step, index) => (
            <div
              key={index}
              className={`step ${index === currentStep ? 'active' : ''} ${
                index < currentStep ? 'completed' : ''
              }`}
            >
              <div className="step-number">{index + 1}</div>
              <div className="step-title">{step}</div>
            </div>
          ))}
        </div>

        <div className="step-content">
          <h2>{steps[currentStep]}</h2>
          {renderCurrentStep()}
        </div>

        <div className="navigation-buttons">
          <button
            onClick={prevStep}
            disabled={currentStep === 0}
            className="nav-button prev"
          >
            Previous
          </button>
          <button
            onClick={nextStep}
            disabled={currentStep === steps.length - 1 || !isStepValid(currentStep)}
            className="nav-button next"
          >
            {currentStep === steps.length - 1 ? 'Submit Pipeline' : 'Next'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default App;