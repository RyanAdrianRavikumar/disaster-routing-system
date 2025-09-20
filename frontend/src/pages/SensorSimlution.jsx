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

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-6">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-lg shadow-xl p-8">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-gray-800 mb-2">Sensor Controller Dashboard</h1>
            <p className="text-gray-600">Manage sensor data recording and obstacle clearing</p>
          </div>

          <div className="grid md:grid-cols-2 gap-8 mb-8">
            {/* Record Sensor Data */}
            <div className="bg-gray-50 rounded-lg p-6">
              <div className="flex items-center mb-4">
                <Database className="w-6 h-6 text-blue-600 mr-2" />
                <h2 className="text-xl font-semibold text-gray-800">Record Sensor Data</h2>
              </div>
              
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Sensor ID
                  </label>
                  <input
                    type="text"
                    value={recordData.sensorId}
                    onChange={(e) => setRecordData({...recordData, sensorId: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="e.g., S1, S2, S3"
                  />
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Edge/Location
                  </label>
                  <select
                    value={recordData.edge}
                    onChange={(e) => setRecordData({...recordData, edge: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">Select edge location (e.g., edge3)</option>
                    <option value="edge1">Edge 1</option>
                    <option value="edge2">Edge 2</option>
                    <option value="edge3">Edge 3</option>
                    <option value="edge4">Edge 4</option>
                    <option value="edge5">Edge 5</option>
                  </select>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Obstacle Type
                  </label>
                  <select
                    value={recordData.obstacleType}
                    onChange={(e) => setRecordData({...recordData, obstacleType: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
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
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Description
                  </label>
                  <textarea
                    value={recordData.description}
                    onChange={(e) => setRecordData({...recordData, description: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 h-20"
                    placeholder="Fallen tree blocking road"
                  />
                </div>
                
                <div className="bg-blue-50 p-3 rounded-md">
                  <p className="text-sm text-blue-800 font-medium mb-1">Data Preview:</p>
                  <p className="text-sm text-blue-700 font-mono">
                    {recordData.edge && recordData.obstacleType && recordData.description 
                      ? `${recordData.edge}:${recordData.obstacleType}:${recordData.description}`
                      : 'edge3:Tree:Fallen tree blocking road (example)'}
                  </p>
                </div>
                
                <button
                  onClick={handleRecord}
                  disabled={loading}
                  className="w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 flex items-center justify-center"
                >
                  {loading ? (
                    <Loader2 className="w-4 h-4 animate-spin mr-2" />
                  ) : (
                    <Database className="w-4 h-4 mr-2" />
                  )}
                  Record Data
                </button>
              </div>
            </div>

            {/* Clear Obstacle */}
            <div className="bg-gray-50 rounded-lg p-6">
              <div className="flex items-center mb-4">
                <Trash2 className="w-6 h-6 text-red-600 mr-2" />
                <h2 className="text-xl font-semibold text-gray-800">Clear Obstacle</h2>
              </div>
              
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Sensor ID
                  </label>
                  <input
                    type="text"
                    value={clearData.sensorId}
                    onChange={(e) => setClearData({...clearData, sensorId: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-red-500"
                    placeholder="S1"
                  />
                </div>
                
                <button
                  onClick={handleClearObstacle}
                  disabled={loading}
                  className="w-full bg-red-600 text-white py-2 px-4 rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 disabled:opacity-50 flex items-center justify-center"
                >
                  {loading ? (
                    <Loader2 className="w-4 h-4 animate-spin mr-2" />
                  ) : (
                    <Trash2 className="w-4 h-4 mr-2" />
                  )}
                  Clear Obstacle
                </button>
              </div>
            </div>
          </div>

          {/* Test Connection */}
          <div className="bg-gray-50 rounded-lg p-6 mb-8">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <TestTube className="w-6 h-6 text-green-600 mr-2" />
                <div>
                  <h2 className="text-xl font-semibold text-gray-800">Test Connection</h2>
                  <p className="text-gray-600 text-sm">Verify that the sensor API is working</p>
                </div>
              </div>
              
              <button
                onClick={handleTest}
                disabled={loading}
                className="bg-green-600 text-white py-2 px-6 rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 disabled:opacity-50 flex items-center"
              >
                {loading ? (
                  <Loader2 className="w-4 h-4 animate-spin mr-2" />
                ) : (
                  <TestTube className="w-4 h-4 mr-2" />
                )}
                Test API
              </button>
            </div>
          </div>

          {/* Response Display */}
          {response && (
            <div className="bg-gray-50 rounded-lg p-6">
              <div className="flex items-center mb-3">
                {responseType === 'success' ? (
                  <CheckCircle className="w-5 h-5 text-green-600 mr-2" />
                ) : (
                  <AlertCircle className="w-5 h-5 text-red-600 mr-2" />
                )}
                <h3 className="text-lg font-semibold text-gray-800">
                  {responseType === 'success' ? 'Success' : 'Error'}
                </h3>
              </div>
              
              <div className="bg-white rounded border p-4">
                <pre className="text-sm text-gray-800 whitespace-pre-wrap break-words">
                  {response}
                </pre>
              </div>
            </div>
          )}

          {/* API Information */}
          <div className="mt-8 p-4 bg-blue-50 rounded-lg">
            <h3 className="text-lg font-semibold text-gray-800 mb-2">API Endpoints</h3>
            <div className="space-y-2 text-sm text-gray-700">
              <div><strong>Base URL:</strong> {API_BASE_URL}</div>
              <div><strong>Record:</strong> POST /record?sensorId=S1&data=edge3:Tree:Fallen tree blocking road</div>
              <div><strong>Clear Obstacle:</strong> POST /clear-obstacle?sensorId=S1</div>
              <div><strong>Test:</strong> GET /test</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}