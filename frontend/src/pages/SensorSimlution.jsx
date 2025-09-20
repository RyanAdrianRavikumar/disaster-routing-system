import React, { useState } from 'react';
import { AlertCircle, CheckCircle, Loader2, Trash2, TestTube, Database } from 'lucide-react';

export default function SensorControllerPage() {
  const [loading, setLoading] = useState(false);
  const [response, setResponse] = useState('');
  const [responseType, setResponseType] = useState('');
  
  // Form states
  const [recordData, setRecordData] = useState({
    sensorId: '',
    edge: '',
    obstacleType: '',
    description: ''
  });
  
  const [clearData, setClearData] = useState({
    sensorId: ''
  });

  const API_BASE_URL = 'http://localhost:8085/api/sensors';

  const makeRequest = async (url, method = 'GET', params = {}) => {
    setLoading(true);
    setResponse('');
    
    try {
      let fetchUrl = url;
      const options = {
        method,
        headers: {
          'Content-Type': 'application/json',
        },
      };

      if (method === 'POST' && Object.keys(params).length > 0) {
        const urlParams = new URLSearchParams(params);
        fetchUrl = `${url}?${urlParams.toString()}`;
      }

      const res = await fetch(fetchUrl, options);
      const data = await res.text();
      
      setResponse(data);
      setResponseType(res.ok ? 'success' : 'error');
    } catch (error) {
      setResponse(`Error: ${error.message}`);
      setResponseType('error');
    } finally {
      setLoading(false);
    }
  };

  const handleRecord = () => {
    // Combine the separate fields into the expected data format
    const combinedData = `${recordData.edge}:${recordData.obstacleType}:${recordData.description}`;
    const apiData = {
      sensorId: recordData.sensorId,
      data: combinedData
    };
    makeRequest(`${API_BASE_URL}/record`, 'POST', apiData);
  };

  const handleClearObstacle = () => {
    makeRequest(`${API_BASE_URL}/clear-obstacle`, 'POST', clearData);
  };

  const handleTest = () => {
    makeRequest(`${API_BASE_URL}/test`);
  };

  const sensorStyles = `
    .sensor-page-container {
      min-height: 100vh;
      background: linear-gradient(135deg, #f3f4f6 0%, #e0e7ff 100%);
      padding: 24px;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    }

    .sensor-main-content {
      max-width: 1200px;
      margin: 0 auto;
    }

    .sensor-card {
      background: white;
      border-radius: 12px;
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
      padding: 32px;
    }

    .sensor-header {
      text-align: center;
      margin-bottom: 32px;
    }

    .sensor-title {
      font-size: 30px;
      font-weight: bold;
      color: #1f2937;
      margin: 0 0 8px 0;
    }

    .sensor-subtitle {
      color: #6b7280;
      margin: 0;
    }

    .sensor-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 32px;
      margin-bottom: 32px;
    }

    .sensor-section-card {
      background: #f9fafb;
      border-radius: 12px;
      padding: 24px;
    }

    .sensor-section-header {
      display: flex;
      align-items: center;
      margin-bottom: 16px;
    }

    .sensor-section-title {
      font-size: 20px;
      font-weight: 600;
      color: #1f2937;
      margin: 0;
    }

    .sensor-icon {
      width: 24px;
      height: 24px;
      margin-right: 8px;
    }

    .sensor-icon-blue {
      color: #2563eb;
    }

    .sensor-icon-red {
      color: #dc2626;
    }

    .sensor-icon-green {
      color: #16a34a;
    }

    .sensor-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .sensor-field {
      display: flex;
      flex-direction: column;
    }

    .sensor-label {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: #374151;
      margin-bottom: 4px;
    }

    .sensor-input,
    .sensor-select,
    .sensor-textarea {
      width: 100%;
      padding: 8px 12px;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      font-size: 14px;
      transition: border-color 0.2s, box-shadow 0.2s;
      box-sizing: border-box;
      font-family: inherit;
    }

    .sensor-input:focus,
    .sensor-select:focus,
    .sensor-textarea:focus {
      outline: none;
      border-color: #2563eb;
      box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
    }

    .sensor-input-red:focus {
      border-color: #dc2626;
      box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
    }

    .sensor-textarea {
      height: 80px;
      resize: vertical;
    }

    .sensor-select {
      background-color: white;
      cursor: pointer;
    }

    .sensor-preview {
      background: #dbeafe;
      padding: 12px;
      border-radius: 6px;
    }

    .sensor-preview-title {
      font-size: 14px;
      font-weight: 500;
      color: #1e40af;
      margin: 0 0 4px 0;
    }

    .sensor-preview-data {
      font-size: 14px;
      color: #1d4ed8;
      font-family: 'Courier New', monospace;
      margin: 0;
    }

    .sensor-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 8px 16px;
      border: none;
      border-radius: 6px;
      font-size: 14px;
      font-weight: 500;
      cursor: pointer;
      transition: background-color 0.2s, transform 0.1s;
      text-decoration: none;
      font-family: inherit;
    }

    .sensor-btn:hover {
      transform: translateY(-1px);
    }

    .sensor-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
      transform: none;
    }

    .sensor-btn-primary {
      background-color: #2563eb;
      color: white;
    }

    .sensor-btn-primary:hover:not(:disabled) {
      background-color: #1d4ed8;
    }

    .sensor-btn-danger {
      background-color: #dc2626;
      color: white;
    }

    .sensor-btn-danger:hover:not(:disabled) {
      background-color: #b91c1c;
    }

    .sensor-btn-success {
      background-color: #16a34a;
      color: white;
    }

    .sensor-btn-success:hover:not(:disabled) {
      background-color: #15803d;
    }

    .sensor-btn-icon {
      width: 16px;
      height: 16px;
      margin-right: 8px;
    }

    .sensor-spinning {
      animation: sensor-spin 1s linear infinite;
    }

    @keyframes sensor-spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }

    .sensor-test-section {
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 16px;
    }

    .sensor-test-info {
      display: flex;
      align-items: center;
    }

    .sensor-test-description {
      color: #6b7280;
      font-size: 14px;
      margin: 0;
    }

    .sensor-response-header {
      display: flex;
      align-items: center;
      margin-bottom: 12px;
    }

    .sensor-response-title {
      font-size: 18px;
      font-weight: 600;
      color: #1f2937;
      margin: 0;
    }

    .sensor-response-content {
      background: white;
      border: 1px solid #e5e7eb;
      border-radius: 6px;
      padding: 16px;
    }

    .sensor-response-text {
      font-size: 14px;
      color: #1f2937;
      white-space: pre-wrap;
      word-break: break-words;
      margin: 0;
      font-family: 'Courier New', monospace;
    }

    .sensor-api-info {
      margin-top: 32px;
      padding: 16px;
      background: #dbeafe;
      border-radius: 12px;
    }

    .sensor-api-title {
      font-size: 18px;
      font-weight: 600;
      color: #1f2937;
      margin: 0 0 8px 0;
    }

    .sensor-api-details {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .sensor-api-details div {
      font-size: 14px;
      color: #374151;
    }

    @media (max-width: 768px) {
      .sensor-grid {
        grid-template-columns: 1fr;
      }
      
      .sensor-test-section {
        flex-direction: column;
        align-items: stretch;
      }
      
      .sensor-test-info {
        justify-content: center;
        text-align: center;
      }
      
      .sensor-page-container {
        padding: 16px;
      }
      
      .sensor-card {
        padding: 24px;
      }
    }
  `;

  return (
    <>
      <style>{sensorStyles}</style>
      <div className="sensor-page-container">
        <div className="sensor-main-content">
          <div className="sensor-card">
            <div className="sensor-header">
              <h1 className="sensor-title">Sensor Controller Dashboard</h1>
              <p className="sensor-subtitle">Manage sensor data recording and obstacle clearing</p>
            </div>

            <div className="sensor-grid">
              {/* Record Sensor Data */}
              <div className="sensor-section-card">
                <div className="sensor-section-header">
                  <Database className="sensor-icon sensor-icon-blue" />
                  <h2 className="sensor-section-title">Record Sensor Data</h2>
                </div>
                
                <div className="sensor-form">
                  <div className="sensor-field">
                    <label className="sensor-label">Sensor ID</label>
                    <input
                      type="text"
                      value={recordData.sensorId}
                      onChange={(e) => setRecordData({...recordData, sensorId: e.target.value})}
                      className="sensor-input"
                      placeholder="e.g., S1, S2, S3"
                    />
                  </div>
                  
                  <div className="sensor-field">
                    <label className="sensor-label">Edge/Location</label>
                    <select
                      value={recordData.edge}
                      onChange={(e) => setRecordData({...recordData, edge: e.target.value})}
                      className="sensor-select"
                    >
                      <option value="">Select edge location (e.g., edge3)</option>
                      <option value="edge1">Edge 1</option>
                      <option value="edge2">Edge 2</option>
                      <option value="edge3">Edge 3</option>
                      <option value="edge4">Edge 4</option>
                      <option value="edge5">Edge 5</option>
                    </select>
                  </div>
                  
                  <div className="sensor-field">
                    <label className="sensor-label">Obstacle Type</label>
                    <select
                      value={recordData.obstacleType}
                      onChange={(e) => setRecordData({...recordData, obstacleType: e.target.value})}
                      className="sensor-select"
                    >
                      <option value="">Select obstacle type (e.g., Tree)</option>
                      <option value="Tree">Tree</option>
                      <option value="Rock">Rock</option>
                      <option value="Vehicle">Vehicle</option>
                      <option value="Construction">Construction</option>
                      <option value="Debris">Debris</option>
                      <option value="Accident">Accident</option>
                      <option value="Weather">Weather</option>
                      <option value="Other">Other</option>
                    </select>
                  </div>
                  
                  <div className="sensor-field">
                    <label className="sensor-label">Description</label>
                    <textarea
                      value={recordData.description}
                      onChange={(e) => setRecordData({...recordData, description: e.target.value})}
                      className="sensor-textarea"
                      placeholder="Fallen tree blocking road"
                    />
                  </div>
                                  
                  <button
                    onClick={handleRecord}
                    disabled={loading}
                    className="sensor-btn sensor-btn-primary"
                  >
                    {loading ? (
                      <Loader2 className="sensor-btn-icon sensor-spinning" />
                    ) : (
                      <Database className="sensor-btn-icon" />
                    )}
                    Record Data
                  </button>
                </div>
              </div>

              {/* Clear Obstacle */}
              <div className="sensor-section-card">
                <div className="sensor-section-header">
                  <Trash2 className="sensor-icon sensor-icon-red" />
                  <h2 className="sensor-section-title">Clear Obstacle</h2>
                </div>
                
                <div className="sensor-form">
                  <div className="sensor-field">
                    <label className="sensor-label">Sensor ID</label>
                    <input
                      type="text"
                      value={clearData.sensorId}
                      onChange={(e) => setClearData({...clearData, sensorId: e.target.value})}
                      className="sensor-input sensor-input-red"
                      placeholder="S1"
                    />
                  </div>
                  
                  <button
                    onClick={handleClearObstacle}
                    disabled={loading}
                    className="sensor-btn sensor-btn-danger"
                  >
                    {loading ? (
                      <Loader2 className="sensor-btn-icon sensor-spinning" />
                    ) : (
                      <Trash2 className="sensor-btn-icon" />
                    )}
                    Clear Obstacle
                  </button>
                </div>
              </div>
            </div>

            {/* Test Connection */}
            <div className="sensor-section-card">
              <div className="sensor-test-section">
                <div className="sensor-test-info">
                  <TestTube className="sensor-icon sensor-icon-green" />
                  <div>
                    <h2 className="sensor-section-title">Test Connection</h2>
                    <p className="sensor-test-description">Verify that the sensor API is working</p>
                  </div>
                </div>
                
                <button
                  onClick={handleTest}
                  disabled={loading}
                  className="sensor-btn sensor-btn-success"
                >
                  {loading ? (
                    <Loader2 className="sensor-btn-icon sensor-spinning" />
                  ) : (
                    <TestTube className="sensor-btn-icon" />
                  )}
                  Test API
                </button>
              </div>
            </div>

            {/* Response Display */}
            {response && (
              <div className="sensor-section-card">
                <div className="sensor-response-header">
                  {responseType === 'success' ? (
                    <CheckCircle className="sensor-icon sensor-icon-green" />
                  ) : (
                    <AlertCircle className="sensor-icon sensor-icon-red" />
                  )}
                  <h3 className="sensor-response-title">
                    {responseType === 'success' ? 'Success' : 'Error'}
                  </h3>
                </div>
                
                <div className="sensor-response-content">
                  <pre className="sensor-response-text">{response}</pre>
                </div>
              </div>
            )}

            {/* API Information */}
            <div className="sensor-api-info">
              <h3 className="sensor-api-title">API Endpoints</h3>
              <div className="sensor-api-details">
                <div><strong>Base URL:</strong> {API_BASE_URL}</div>
                <div><strong>Record:</strong> POST /record?sensorId=S1&data=edge3:Tree:Fallen tree blocking road</div>
                <div><strong>Clear Obstacle:</strong> POST /clear-obstacle?sensorId=S1</div>
                <div><strong>Test:</strong> GET /test</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}