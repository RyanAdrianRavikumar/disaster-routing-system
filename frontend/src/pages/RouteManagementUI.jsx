import React, { useState, useEffect } from 'react';
import '../styles/RouteManagementUI.css';

// SVG Icons
const NavigationIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <polygon points="12 2 19 21 12 17 5 21 12 2"></polygon>
  </svg>
);

const RefreshIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <polyline points="23 4 23 10 17 10"></polyline>
    <polyline points="1 20 1 14 7 14"></polyline>
    <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
  </svg>
);

const DatabaseIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <ellipse cx="12" cy="5" rx="9" ry="3"></ellipse>
    <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"></path>
    <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"></path>
  </svg>
);

const MapPinIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
    <circle cx="12" cy="10" r="3"></circle>
  </svg>
);

const RouteIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="6" cy="19" r="3"></circle>
    <circle cx="18" cy="5" r="3"></circle>
    <path d="M9 19h8.5a3.5 3.5 0 0 0 0-7h-11a3.5 3.5 0 0 1 0-7H15"></path>
  </svg>
);

const SettingsIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="12" cy="12" r="3"></circle>
    <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
  </svg>
);

const CheckIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
    <polyline points="20 6 9 17 4 12"></polyline>
  </svg>
);

const XIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
    <line x1="18" y1="6" x2="6" y2="18"></line>
    <line x1="6" y1="6" x2="18" y2="18"></line>
  </svg>
);

const RouteManagementUI = () => {
  const [nodes, setNodes] = useState([]);
  const [edges, setEdges] = useState([]);
  const [routeQuery, setRouteQuery] = useState({ start: '', end: '' });
  const [routeResult, setRouteResult] = useState(null);
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('pathfinding');

  const API_BASE = 'http://localhost:8080';

  // Fetch nodes from backend
  const fetchNodes = async () => {
    try {
      const res = await fetch(`${API_BASE}/route/nodes`);
      if (res.ok) {
        const data = await res.json();
        setNodes(data);
        return true;
      } else {
        setMessage('Failed to fetch nodes');
        return false;
      }
    } catch (error) {
      console.error('Error fetching nodes:', error);
      setMessage('Error connecting to server');
      return false;
    }
  };

  // Fetch edges from backend
  const fetchEdges = async () => {
    try {
      const res = await fetch(`${API_BASE}/route/edges`);
      if (res.ok) {
        const data = await res.json();
        setEdges(data);
        return true;
      } else {
        setMessage('Failed to fetch edges');
        return false;
      }
    } catch (error) {
      console.error('Error fetching edges:', error);
      setMessage('Error connecting to server');
      return false;
    }
  };

  // Fetch all data
  const fetchAllData = async () => {
    setIsLoading(true);
    const [nodesSuccess, edgesSuccess] = await Promise.all([
      fetchNodes(),
      fetchEdges()
    ]);
    
    if (nodesSuccess && edgesSuccess) {
      setMessage('Data loaded successfully');
    }
    setIsLoading(false);
  };

  useEffect(() => {
    fetchAllData();
  }, []);

  // Find shortest path
  const findShortestPath = async () => {
    if (!routeQuery.start.trim() || !routeQuery.end.trim()) {
      setMessage('Please enter both start and end nodes');
      return;
    }

    setIsLoading(true);
    try {
      const res = await fetch(
        `${API_BASE}/route/shortest-path?start=${encodeURIComponent(
          routeQuery.start.trim()
        )}&end=${encodeURIComponent(routeQuery.end.trim())}`
      );

      if (res.ok) {
        const result = await res.json();
        setRouteResult(result);
        
        if (result.path && result.path.length > 0) {
          setMessage(
            `Path found: ${result.path.join(' → ')} (Distance: ${result.distance.toFixed(2)})`
          );
        } else {
          setMessage('No path found between the specified nodes');
        }
      } else {
        const errorText = await res.text();
        setMessage(`Error: ${errorText}`);
      }
    } catch (error) {
      console.error('Error finding path:', error);
      setMessage('Error during pathfinding');
    }
    setIsLoading(false);
  };

  // Initialize sample data
  const initSampleData = async () => {
    setIsLoading(true);
    try {
      const res = await fetch(`${API_BASE}/admin/init-data`, {
        method: 'POST'
      });

      if (res.ok) {
        const result = await res.text();
        setMessage(result);
        await fetchAllData();
      } else {
        setMessage('Failed to initialize data');
      }
    } catch (error) {
      console.error('Error initializing data:', error);
      setMessage('Error initializing data');
    }
    setIsLoading(false);
  };

  // Clear all data
  const clearAllData = async () => {
    if (!window.confirm('Are you sure you want to clear all data? This cannot be undone.')) {
      return;
    }

    setIsLoading(true);
    try {
      const res = await fetch(`${API_BASE}/admin/clear-data`, {
        method: 'POST'
      });

      if (res.ok) {
        const result = await res.text();
        setMessage(result);
        setNodes([]);
        setEdges([]);
        setRouteResult(null);
      } else {
        setMessage('Failed to clear data');
      }
    } catch (error) {
      console.error('Error clearing data:', error);
      setMessage('Error clearing data');
    }
    setIsLoading(false);
  };

  const renderPathfindingTab = () => (
    <div className="route-content">
      <div className="path-finder-section">
        <h2>
          <NavigationIcon />
          Find Shortest Path
        </h2>
        
        <div className="path-inputs">
          <div className="input-group">
            <label>Start Node</label>
            <input
              type="text"
              value={routeQuery.start}
              onChange={e => setRouteQuery({ ...routeQuery, start: e.target.value })}
              placeholder="Start Node (e.g., A)"
            />
          </div>
          
          <div className="input-group">
            <label>End Node</label>
            <input
              type="text"
              value={routeQuery.end}
              onChange={e => setRouteQuery({ ...routeQuery, end: e.target.value })}
              placeholder="End Node (e.g., E)"
              onKeyPress={e => e.key === 'Enter' && findShortestPath()}
            />
          </div>
          
          <button
            onClick={findShortestPath}
            disabled={isLoading || !routeQuery.start.trim() || !routeQuery.end.trim()}
            className="find-path-btn"
          >
            <RouteIcon />
            {isLoading ? 'Finding Path...' : 'Find Path'}
          </button>
        </div>

        {routeResult && routeResult.path && routeResult.path.length > 0 && (
          <div className="path-result">
            <h3>Path Found:</h3>
            <div className="path-details">
              <div className="path-info">
                <span className="info-label">Route:</span>
                <div className="path-nodes">
                  {routeResult.path.map((node, index) => (
                    <span key={index}>
                      <span className="path-node">{node}</span>
                      {index < routeResult.path.length - 1 && <span className="path-arrow">→</span>}
                    </span>
                  ))}
                </div>
              </div>
              <div className="path-info">
                <span className="info-label">Total Distance:</span>
                <span className="info-value">{routeResult.distance.toFixed(2)}</span>
              </div>
            </div>
          </div>
        )}
      </div>

      <div className="admin-panel">
        <h2>
          <RefreshIcon />
          Quick Actions
        </h2>
        
        <div className="admin-actions">
          <button
            onClick={fetchAllData}
            disabled={isLoading}
            className="admin-btn init-btn"
          >
            <RefreshIcon />
            Refresh Data
          </button>

          <button
            onClick={initSampleData}
            disabled={isLoading}
            className="admin-btn init-btn"
          >
            <DatabaseIcon />
            Initialize Sample Data
          </button>

          <button
            onClick={clearAllData}
            disabled={isLoading}
            className="admin-btn clear-btn"
          >
            <DatabaseIcon />
            Clear All Data
          </button>
        </div>
      </div>
    </div>
  );

  const renderDataTab = () => (
    <div className="data-sections">
      <div className="nodes-section">
        <h2>
          <MapPinIcon />
          Nodes ({nodes.length})
        </h2>
        
        {nodes.length === 0 ? (
          <div className="empty-data">No nodes found. Initialize sample data to get started.</div>
        ) : (
          <div className="nodes-list">
            {nodes.map(node => (
              <div key={node.id} className="node-card">
                <h4>{node.id}</h4>
                <p className="node-id">{node.name}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="edges-section">
        <h2>
          <RouteIcon />
          Edges ({edges.length})
        </h2>
        
        {edges.length === 0 ? (
          <div className="empty-data">No edges found. Initialize sample data to get started.</div>
        ) : (
          <div className="edges-list">
            {edges.map((edge, index) => (
              <div key={index} className="edge-card">
                <div className="edge-connection">
                  <span className="node-name">{edge.from}</span>
                  <span className="connection-arrow">→</span>
                  <span className="node-name">{edge.to}</span>
                </div>
                <div className="edge-info">
                  <span className="edge-distance">Weight: {edge.weight}</span>
                  <span className={`edge-status ${edge.blocked ? 'blocked' : 'open'}`}>
                    {edge.blocked ? (
                      <>
                        <XIcon /> Blocked
                      </>
                    ) : (
                      <>
                        <CheckIcon /> Open
                      </>
                    )}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );

  return (
    <div className="route-container">
      <header className="app-header">
        <h1>Route Management Dashboard</h1>
        <p>Manage nodes, edges, and find optimal paths</p>
      </header>

      <div className="route-dashboard">
        {message && (
          <div className="message-banner">
            <span>{message}</span>
            <button 
              onClick={() => setMessage('')}
              className="close-btn"
            >
              ×
            </button>
          </div>
        )}

        <div className="tabs">
          <button
            className={`tab ${activeTab === 'pathfinding' ? 'active-tab' : ''}`}
            onClick={() => setActiveTab('pathfinding')}
          >
            <NavigationIcon />
            Pathfinding
          </button>
          <button
            className={`tab ${activeTab === 'data' ? 'active-tab' : ''}`}
            onClick={() => setActiveTab('data')}
          >
            <SettingsIcon />
            Data View
          </button>
        </div>

        {activeTab === 'pathfinding' && renderPathfindingTab()}
        {activeTab === 'data' && renderDataTab()}
      </div>
    </div>
  );
};

export default RouteManagementUI;