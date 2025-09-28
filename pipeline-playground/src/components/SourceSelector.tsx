import React, {JSX, useState} from 'react';
import { SourceConfig } from '../App';

interface SourceType {
  id: string;
  name: string;
  description: string;
}

interface SourceSelectorProps {
  config: SourceConfig;
  updateConfig: (data: Partial<SourceConfig>) => void;
}

const SourceSelector: React.FC<SourceSelectorProps> = ({ config, updateConfig }) => {
  const [selectedType, setSelectedType] = useState<string>(config.type || '');

  const sourceTypes: SourceType[] = [
    {
      id: 'opentext',
      name: 'OpenText',
      description: 'Connect to OpenText content management system'
    },
    {
      id: 'virtual-library',
      name: 'Virtual Library',
      description: 'Access documents from virtual library collections'
    },
    {
      id: 'pgp',
      name: 'PGP',
      description: 'Process PGP encrypted content'
    },
    {
      id: 'object-storage',
      name: 'Object Storage',
      description: 'Connect to cloud object storage (S3, Azure Blob, etc.)'
    },
    {
      id: 'ecm-repo',
      name: 'ECM Repository',
      description: 'Enterprise Content Management repository'
    },
    {
      id: 'url',
      name: 'URL',
      description: 'Process content from web URLs'
    },
    {
      id: 'raw-html',
      name: 'Raw HTML',
      description: 'Direct HTML content input'
    },
    {
      id: 'raw-binary',
      name: 'Raw Binary',
      description: 'Binary file content processing'
    }
  ];

  const handleTypeChange = (type: string): void => {
    setSelectedType(type);
    updateConfig({
      type: type,
      parameters: {}
    });
  };

  const handleParameterChange = (paramName: string, value: any): void => {
    updateConfig({
      type: selectedType,
      parameters: {
        ...config.parameters,
        [paramName]: value
      }
    });
  };

  const renderParameterFields = (): JSX.Element => {
    switch (selectedType) {
      case 'opentext':
        return (
          <div className="parameter-fields">
            <div className="form-group">
              <label htmlFor="ot-id-lob">ID / LOB Code</label>
              <input
                type="text"
                id="ot-id-lob"
                value={config.parameters.idOrLobCode || ''}
                onChange={(e) => handleParameterChange('idOrLobCode', e.target.value)}
                placeholder="Enter ID or LOB Code"
                className="form-input"
              />
              <span className="help-text">
                Configuration details are automatically handled by the backend service
              </span>
            </div>
          </div>
        );

      case 'virtual-library':
        return (
          <div className="parameter-fields">
            <div className="form-group">
              <label htmlFor="vl-id-lob">ID / LOB Code</label>
              <input
                type="text"
                id="vl-id-lob"
                value={config.parameters.idOrLobCode || ''}
                onChange={(e) => handleParameterChange('idOrLobCode', e.target.value)}
                placeholder="Enter ID or LOB Code"
                className="form-input"
              />
              <span className="help-text">
                Configuration details are automatically handled by the backend service
              </span>
            </div>
          </div>
        );

      case 'pgp':
        return (
          <div className="parameter-fields">
            <div className="form-group">
              <label htmlFor="pgp-id-lob">ID / LOB Code</label>
              <input
                type="text"
                id="pgp-id-lob"
                value={config.parameters.idOrLobCode || ''}
                onChange={(e) => handleParameterChange('idOrLobCode', e.target.value)}
                placeholder="Enter ID or LOB Code"
                className="form-input"
              />
              <span className="help-text">
                Configuration details are automatically handled by the backend service
              </span>
            </div>
          </div>
        );

      case 'object-storage':
        return (
          <div className="parameter-fields">
            <div className="form-group">
              <label htmlFor="os-bucket">Bucket Name</label>
              <input
                type="text"
                id="os-bucket"
                value={config.parameters.bucket || ''}
                onChange={(e) => handleParameterChange('bucket', e.target.value)}
                placeholder="Enter bucket name"
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label htmlFor="os-object-id">Object ID</label>
              <input
                type="text"
                id="os-object-id"
                value={config.parameters.objectId || ''}
                onChange={(e) => handleParameterChange('objectId', e.target.value)}
                placeholder="Enter object identifier"
                className="form-input"
              />
            </div>
            <span className="help-text">
              Provider configuration and credentials are automatically handled by the backend service
            </span>
          </div>
        );

      case 'ecm-repo':
        return (
          <div className="parameter-fields">
            <div className="form-group">
              <label htmlFor="ecm-system">ECM System</label>
              <select
                id="ecm-system"
                value={config.parameters.system || ''}
                onChange={(e) => handleParameterChange('system', e.target.value)}
                className="form-select"
              >
                <option value="">Select ECM system</option>
                <option value="sharepoint">SharePoint</option>
                <option value="documentum">EMC Documentum</option>
                <option value="filenet">IBM FileNet</option>
                <option value="alfresco">Alfresco</option>
                <option value="m-files">M-Files</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="ecm-server">Server URL</label>
              <input
                type="text"
                id="ecm-server"
                value={config.parameters.serverUrl || ''}
                onChange={(e) => handleParameterChange('serverUrl', e.target.value)}
                placeholder="ECM server URL"
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label htmlFor="ecm-credentials">Credentials</label>
              <input
                type="password"
                id="ecm-credentials"
                value={config.parameters.credentials || ''}
                onChange={(e) => handleParameterChange('credentials', e.target.value)}
                placeholder="Authentication credentials"
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label htmlFor="ecm-path">Repository Path</label>
              <input
                type="text"
                id="ecm-path"
                value={config.parameters.repositoryPath || ''}
                onChange={(e) => handleParameterChange('repositoryPath', e.target.value)}
                placeholder="/path/to/documents"
                className="form-input"
              />
            </div>
          </div>
        );

      case 'url':
        return (
          <div className="parameter-fields">
            <div className="form-group">
              <label htmlFor="url-address">URL</label>
              <input
                type="url"
                id="url-address"
                value={config.parameters.url || ''}
                onChange={(e) => handleParameterChange('url', e.target.value)}
                placeholder="https://example.com/document.pdf"
                className="form-input"
              />
            </div>
            <div className="form-group">
              <label htmlFor="url-headers">Custom Headers (JSON)</label>
              <textarea
                id="url-headers"
                value={config.parameters.headers || ''}
                onChange={(e) => handleParameterChange('headers', e.target.value)}
                placeholder='{"Authorization": "Bearer token", "User-Agent": "Pipeline Bot"}'
                className="form-textarea"
                rows={3}
              />
            </div>
            <div className="form-group">
              <label htmlFor="url-timeout">Timeout (seconds)</label>
              <input
                type="number"
                id="url-timeout"
                value={config.parameters.timeout || ''}
                onChange={(e) => handleParameterChange('timeout', parseInt(e.target.value))}
                placeholder="30"
                className="form-input"
                min={1}
                max={300}
              />
            </div>
          </div>
        );

      case 'raw-html':
        return (
          <div className="parameter-fields">
            <div className="form-group">
              <label htmlFor="html-content">HTML Content</label>
              <textarea
                id="html-content"
                value={config.parameters.content || ''}
                onChange={(e) => handleParameterChange('content', e.target.value)}
                placeholder="<html><body><p>Your HTML content here...</p></body></html>"
                className="form-textarea"
                rows={10}
              />
            </div>
            <div className="form-group">
              <label htmlFor="html-encoding">Character Encoding</label>
              <select
                id="html-encoding"
                value={config.parameters.encoding || 'utf-8'}
                onChange={(e) => handleParameterChange('encoding', e.target.value)}
                className="form-select"
              >
                <option value="utf-8">UTF-8</option>
                <option value="utf-16">UTF-16</option>
                <option value="iso-8859-1">ISO-8859-1</option>
                <option value="windows-1252">Windows-1252</option>
              </select>
            </div>
          </div>
        );

      case 'raw-binary':
        return (
          <div className="parameter-fields">
            <div className="form-group">
              <label htmlFor="binary-file">Binary File Upload</label>
              <input
                type="file"
                id="binary-file"
                onChange={(e) => handleParameterChange('file', e.target.files?.[0])}
                className="form-input-file"
                accept=".pdf,.doc,.docx,.xlsx,.pptx,.zip,.tar,.gz"
              />
            </div>
            <div className="form-group">
              <label htmlFor="binary-type">File Type</label>
              <select
                id="binary-type"
                value={config.parameters.fileType || ''}
                onChange={(e) => handleParameterChange('fileType', e.target.value)}
                className="form-select"
              >
                <option value="">Auto-detect</option>
                <option value="pdf">PDF</option>
                <option value="docx">Word Document</option>
                <option value="xlsx">Excel Spreadsheet</option>
                <option value="pptx">PowerPoint Presentation</option>
                <option value="zip">ZIP Archive</option>
                <option value="tar">TAR Archive</option>
                <option value="image">Image File</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="binary-processing">Processing Options</label>
              <div className="checkbox-group">
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={config.parameters.extractText || false}
                    onChange={(e) => handleParameterChange('extractText', e.target.checked)}
                  />
                  Extract text content
                </label>
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={config.parameters.extractMetadata || false}
                    onChange={(e) => handleParameterChange('extractMetadata', e.target.checked)}
                  />
                  Extract metadata
                </label>
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={config.parameters.preserveStructure || false}
                    onChange={(e) => handleParameterChange('preserveStructure', e.target.checked)}
                  />
                  Preserve document structure
                </label>
              </div>
            </div>
          </div>
        );

      default:
        return (
          <div className="parameter-fields">
            <p className="help-text">Select a source type above to configure its parameters.</p>
          </div>
        );
    }
  };

  return (
    <div className="source-selector">
      <div className="form-section">
        <p className="section-description">
          Choose your data source and configure its connection parameters.
        </p>

        <div className="source-types">
          <h3>Select Source Type</h3>
          <div className="source-grid">
            {sourceTypes.map((source) => (
              <div
                key={source.id}
                className={`source-card ${selectedType === source.id ? 'selected' : ''}`}
                onClick={() => handleTypeChange(source.id)}
              >
                <h4>{source.name}</h4>
                <p>{source.description}</p>
              </div>
            ))}
          </div>
        </div>

        {selectedType && (
          <div className="source-parameters">
            <h3>Configure {sourceTypes.find(s => s.id === selectedType)?.name} Parameters</h3>
            {renderParameterFields()}
          </div>
        )}

        <div className="validation-status">
          {selectedType ? (
            <div className="validation-success">
              ✓ Source type selected: {sourceTypes.find(s => s.id === selectedType)?.name}
            </div>
          ) : (
            <div className="validation-warning">
              ⚠ Please select a source type to continue
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SourceSelector;