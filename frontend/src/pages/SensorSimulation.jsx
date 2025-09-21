import React, { useState } from 'react';
import '../styles/SensorSimulation.css';

// SVG Icons
const DatabaseIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
    <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
    <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
  </svg>
);

const TrashIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M3 6h18"></path>
    <path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
    <path d="M18 6v14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V6"></path>
    <path d="M10 11v6"></path>
    <path d="M14 11v6"></path>
  </svg>
);

const TestTubeIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M14.5 2v17.5c0 1.38-2.5 2.5-5.5 2.5s-5.5-1.12-5.5-2.5V2"></path>
    <path d="M14.5 2v17.5c0 1.38 2.5 2.5 5.5 2.5s5.5-1.12 5.5-2.5V2"></path>
    <line x1="14.5" y1="12" x2="19.5" y2="12"></line>
    <line x1="14.5" y1="7" x2="19.5" y2="7"></line>
    <line x1="14.5" y1="17" x2="19.5" y2="17"></line>
  </svg>
);

const CheckCircleIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
    <polyline points="22 4 12 14.01 9 11.01"></polyline>
  </svg>
);

const AlertCircleIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="12" cy="12" r="10"></circle>
    <line x1="12" y1="8" x2="12" y2="12"></line>
    <line x1="12" y1="16" x2="12.01" y2="16"></line>
  </svg>
);

const LoaderIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <line x1="12" y1="2" x2="12" y2="6"></line>
    <line x1="12" y1="18" x2="12" y2="22"></line>
    <line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line>
    <line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line>
    <line x1="2" y1="12" x2="6" y2="12"></line>
    <line x1="18" y1="12" x2="22" y2="12"></line>
    <line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line>
    <line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line>
  </svg>
);

const DisasterIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z"></path>
    <path d="M22 10a3 3 0 0 0-3-3h-2.207a5.502 5.502 0 0 0-10.702.5"></path>
  </svg>
);

const CloseIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <line x1="18" y1="6" x2="6" y2="18"></line>
    <line x1="6" y1="6" x2="18" y2="18"></line>
  </svg>
);

const SensorSimulation = () => {
  const [loading, setLoading] = useState(false);
  const [response, setResponse] = useState('');
  const [responseType, setResponseType] = useState('');
  const [showDisasterForm, setShowDisasterForm] = useState(false);
  
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

  // Disaster simulation states
  const [disasterType, setDisasterType] = useState("");
  const [disasterDescription, setDisasterDescription] = useState("");
  const [customMessage, setCustomMessage] = useState("");

  const API_BASE_URL = 'http://localhost:8085/api/sensors';
  const DISASTER_API_URL = 'http://localhost:8083/disaster';

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

  const simulateDisaster = async () => {
    setLoading(true);
    setResponse('');
    
    try {
      const res = await fetch(
        `${DISASTER_API_URL}/simulate?disasterType=${encodeURIComponent(
          disasterType
        )}&description=${encodeURIComponent(disasterDescription)}`,
        {
          method: "POST",
        }
      );
      const text = await res.text();
      setResponse(text);
      setResponseType(res.ok ? 'success' : 'error');
    } catch (err) {
      console.error(err);
      setResponse("Error sending disaster notification");
      setResponseType('error');
    } finally {
      setLoading(false);
    }
  };

  const sendCustomMessage = async () => {
    setLoading(true);
    setResponse('');
    
    try {
      const res = await fetch(
        `${DISASTER_API_URL}/message?message=${encodeURIComponent(
          customMessage
        )}`,
        {
          method: "POST",
        }
      );
      const text = await res.text();
      setResponse(text);
      setResponseType(res.ok ? 'success' : 'error');
    } catch (err) {
      console.error(err);
      setResponse("Error sending custom message");
      setResponseType('error');
    } finally {
      setLoading(false);
    }
  };

  const toggleDisasterForm = () => {
    setShowDisasterForm(!showDisasterForm);
    setResponse('');
  };

  return (
    <div className="sensor-container">
      <header className="app-header">
        <h1>Sensor Simulation Dashboard</h1>
        <p>Manage sensor data recording and obstacle clearing</p>
      </header>

      <div className="sensor-dashboard">
        {response && (
          <div className={`message-banner ${responseType === 'success' ? 'success' : 'error'}`}>
            <span>{response}</span>
            <button onClick={() => setResponse('')} className="close-btn">×</button>
          </div>
        )}

        <div className="sensor-content">
          {/* Simulate Disaster Button */}
          <div className="disaster-button-container">
            <button
              onClick={toggleDisasterForm}
              className="btn btn-warning"
            >
              <DisasterIcon />
              {showDisasterForm ? 'Hide Disaster Form' : 'Simulate Disaster'}
            </button>
          </div>

          {/* Disaster Simulation Form */}
          {showDisasterForm && (
            <div className="sensor-card disaster-form">
              <div className="sensor-section-header">
                <DisasterIcon />
                <h2>Disaster Simulation</h2>
                <button 
                  onClick={toggleDisasterForm} 
                  className="close-disaster-btn"
                  title="Close disaster form"
                >
                  <CloseIcon />
                </button>
              </div>
              
              <div className="disaster-form-content">
                <div className="disaster-form-section">
                  <h3>Simulate Disaster</h3>
                  <div className="input-group left-align">
                    <label>Disaster Type</label>
                    <input
                      type="text"
                      value={disasterType}
                      onChange={(e) => setDisasterType(e.target.value)}
                      placeholder="e.g., Earthquake, Flood, Hurricane"
                    />
                  </div>
                  
                  <div className="input-group left-align">
                    <label>Description</label>
                    <textarea
                      value={disasterDescription}
                      onChange={(e) => setDisasterDescription(e.target.value)}
                      placeholder="Detailed description of the disaster"
                      rows="3"
                    />
                  </div>
                  
                  <button
                    onClick={simulateDisaster}
                    disabled={loading}
                    className="btn btn-danger"
                  >
                    {loading ? <LoaderIcon className="btn-spinner" /> : <DisasterIcon />}
                    Send Disaster Notification
                  </button>
                </div>

                <div className="disaster-form-section">
                  <h3>Send Custom Message</h3>
                  <div className="input-group left-align">
                    <label>Custom Message</label>
                    <textarea
                      value={customMessage}
                      onChange={(e) => setCustomMessage(e.target.value)}
                      placeholder="Enter your custom emergency message"
                      rows="3"
                    />
                  </div>
                  
                  <button
                    onClick={sendCustomMessage}
                    disabled={loading}
                    className="btn btn-primary"
                  >
                    {loading ? <LoaderIcon className="btn-spinner" /> : <DisasterIcon />}
                    Send Custom Message
                  </button>
                </div>
              </div>
            </div>
          )}

          <div className="sensor-grid">
            {/* Record Sensor Data */}
            <div className="sensor-card">
              <div className="sensor-section-header">
                <DatabaseIcon />
                <h2>Record Sensor Data</h2>
              </div>
              
              <div className="sensor-form">
                <div className="input-group left-align">
                  <label>Sensor ID</label>
                  <input
                    type="text"
                    value={recordData.sensorId}
                    onChange={(e) => setRecordData({...recordData, sensorId: e.target.value})}
                    placeholder="e.g., S1, S2, S3"
                  />
                </div>
                
                <div className="input-group left-align">
                  <label>Edge/Location</label>
                  <select
                    value={recordData.edge}
                    onChange={(e) => setRecordData({...recordData, edge: e.target.value})}
                  >
                    <option value="">Select edge location</option>
                    <option value="edge1">Edge 1</option>
                    <option value="edge2">Edge 2</option>
                    <option value="edge3">Edge 3</option>
                    <option value="edge4">Edge 4</option>
                    <option value="edge5">Edge 5</option>
                  </select>
                </div>
                
                <div className="input-group left-align">
                  <label>Obstacle Type</label>
                  <select
                    value={recordData.obstacleType}
                    onChange={(e) => setRecordData({...recordData, obstacleType: e.target.value})}
                  >
                    <option value="">Select obstacle type</option>
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
                
                <div className="input-group left-align">
                  <label>Description</label>
                  <textarea
                    value={recordData.description}
                    onChange={(e) => setRecordData({...recordData, description: e.target.value})}
                    placeholder="Fallen tree blocking road"
                  />
                </div>
                                
                <button
                  onClick={handleRecord}
                  disabled={loading}
                  className="btn btn-primary"
                >
                  {loading ? <LoaderIcon className="btn-spinner" /> : <DatabaseIcon />}
                  Record Data
                </button>
              </div>
            </div>

            {/* Clear Obstacle */}
            <div className="sensor-card">
              <div className="sensor-section-header">
                <TrashIcon />
                <h2>Clear Obstacle</h2>
              </div>
              
              <div className="sensor-form">
                <div className="input-group">
                  <label>Sensor ID</label>
                  <input
                    type="text"
                    value={clearData.sensorId}
                    onChange={(e) => setClearData({...clearData, sensorId: e.target.value})}
                    placeholder="S1"
                  />
                </div>
                
                <button
                  onClick={handleClearObstacle}
                  disabled={loading}
                  className="btn btn-danger"
                >
                  {loading ? <LoaderIcon className="btn-spinner" /> : <TrashIcon />}
                  Clear Obstacle
                </button>
              </div>
            </div>
          </div>

          {/* Test Connection */}
          <div className="sensor-card">
            <div className="sensor-test-section">
              <div className="test-info">
                <TestTubeIcon />
                <div>
                  <h2>Test Connection</h2>
                  <p>Verify that the sensor API is working</p>
                </div>
              </div>
              
              <button
                onClick={handleTest}
                disabled={loading}
                className="btn btn-success"
              >
                {loading ? <LoaderIcon className="btn-spinner" /> : <TestTubeIcon />}
                Test API
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SensorSimulation;