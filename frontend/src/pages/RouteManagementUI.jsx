import React, { useState, useEffect } from 'react';
import { MapPin, Route, Settings, RefreshCw, Zap, ZapOff, Navigation, Database } from 'lucide-react';

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

  const styles = {
    container: {
      padding: '20px',
      fontFamily: 'Arial, sans-serif',
      maxWidth: '1400px',
      margin: '0 auto',
      backgroundColor: '#f8f9fa'
    },
    header: {
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      color: 'white',
      padding: '30px',
      textAlign: 'center',
      borderRadius: '12px',
      marginBottom: '20px',
      boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
    },
    message: {
      padding: '12px 16px',
      backgroundColor: '#e3f2fd',
      border: '1px solid #1976d2',
      borderRadius: '8px',
      marginBottom: '20px',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
    },
    tabs: {
      display: 'flex',
      marginBottom: '20px',
      backgroundColor: 'white',
      borderRadius: '8px',
      overflow: 'hidden',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
    },
    tab: {
      flex: 1,
      padding: '12px 20px',
      cursor: 'pointer',
      border: 'none',
      backgroundColor: 'transparent',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: '8px',
      transition: 'all 0.2s ease'
    },
    activeTab: {
      backgroundColor: '#667eea',
      color: 'white'
    },
    content: {
      display: 'grid',
      gridTemplateColumns: '1fr 1fr',
      gap: '20px'
    },
    panel: {
      backgroundColor: 'white',
      borderRadius: '12px',
      padding: '20px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
    },
    fullWidthPanel: {
      backgroundColor: 'white',
      borderRadius: '12px',
      padding: '20px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
      gridColumn: '1 / -1'
    },
    input: {
      width: '100%',
      padding: '12px',
      border: '1px solid #ddd',
      borderRadius: '6px',
      marginBottom: '10px',
      fontSize: '14px'
    },
    button: {
      padding: '12px 20px',
      backgroundColor: '#667eea',
      color: 'white',
      border: 'none',
      borderRadius: '6px',
      cursor: 'pointer',
      width: '100%',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: '8px',
      fontSize: '14px',
      fontWeight: '500',
      transition: 'all 0.2s ease'
    },
    buttonSuccess: {
      backgroundColor: '#28a745'
    },
    buttonWarning: {
      backgroundColor: '#fd7e14'
    },
    buttonDanger: {
      backgroundColor: '#dc3545'
    },
    buttonDisabled: {
      backgroundColor: '#ccc',
      cursor: 'not-allowed'
    },
    grid: {
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
      gap: '12px',
      marginTop: '15px'
    },
    card: {
      border: '1px solid #e0e0e0',
      borderRadius: '8px',
      padding: '12px',
      backgroundColor: '#f9f9f9'
    },
    edgeCard: {
      border: '1px solid #e0e0e0',
      borderRadius: '8px',
      padding: '12px',
      backgroundColor: '#f9f9f9',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center'
    },
    pathResult: {
      backgroundColor: '#e8f5e8',
      border: '2px solid #28a745',
      borderRadius: '8px',
      padding: '15px',
      marginTop: '15px'
    },
    routeStep: {
      display: 'inline-block',
      padding: '4px 8px',
      backgroundColor: '#667eea',
      color: 'white',
      borderRadius: '4px',
      margin: '2px',
      fontSize: '12px'
    }
  };

  const renderPathfindingTab = () => (
    <div style={styles.content}>
      <div style={styles.panel}>
        <h3 style={{ margin: '0 0 20px 0', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Navigation size={20} />
          Find Shortest Path
        </h3>
        
        <input
          type="text"
          value={routeQuery.start}
          onChange={e => setRouteQuery({ ...routeQuery, start: e.target.value })}
          placeholder="Start Node (e.g., A)"
          style={styles.input}
        />
        
        <input
          type="text"
          value={routeQuery.end}
          onChange={e => setRouteQuery({ ...routeQuery, end: e.target.value })}
          placeholder="End Node (e.g., E)"
          style={styles.input}
          onKeyPress={e => e.key === 'Enter' && findShortestPath()}
        />
        
        <button
          onClick={findShortestPath}
          disabled={isLoading || !routeQuery.start.trim() || !routeQuery.end.trim()}
          style={{
            ...styles.button,
            ...styles.buttonSuccess,
            ...(isLoading || !routeQuery.start.trim() || !routeQuery.end.trim() ? styles.buttonDisabled : {})
          }}
        >
          <Route size={16} />
          {isLoading ? 'Finding Path...' : 'Find Path'}
        </button>

        {routeResult && routeResult.path && routeResult.path.length > 0 && (
          <div style={styles.pathResult}>
            <h4 style={{ margin: '0 0 10px 0' }}>Path Found:</h4>
            <div style={{ marginBottom: '10px' }}>
              {routeResult.path.map((node, index) => (
                <span key={index}>
                  <span style={styles.routeStep}>{node}</span>
                  {index < routeResult.path.length - 1 && ' → '}
                </span>
              ))}
            </div>
            <p style={{ margin: '5px 0', fontWeight: 'bold' }}>
              Total Distance: {routeResult.distance.toFixed(2)}
            </p>
          </div>
        )}
      </div>

      <div style={styles.panel}>
        <h3 style={{ margin: '0 0 20px 0', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <RefreshCw size={20} />
          Quick Actions
        </h3>
        
        <button
          onClick={fetchAllData}
          disabled={isLoading}
          style={{
            ...styles.button,
            marginBottom: '10px',
            ...(isLoading ? styles.buttonDisabled : {})
          }}
        >
          <RefreshCw size={16} />
          Refresh Data
        </button>

        <button
          onClick={initSampleData}
          disabled={isLoading}
          style={{
            ...styles.button,
            ...styles.buttonSuccess,
            marginBottom: '10px',
            ...(isLoading ? styles.buttonDisabled : {})
          }}
        >
          <Database size={16} />
          Initialize Sample Data
        </button>

        <button
          onClick={clearAllData}
          disabled={isLoading}
          style={{
            ...styles.button,
            ...styles.buttonDanger,
            ...(isLoading ? styles.buttonDisabled : {})
          }}
        >
          <Database size={16} />
          Clear All Data
        </button>
      </div>
    </div>
  );

  const renderDataTab = () => (
    <div style={styles.content}>
      <div style={styles.panel}>
        <h3 style={{ margin: '0 0 15px 0', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <MapPin size={20} />
          Nodes ({nodes.length})
        </h3>
        
        {nodes.length === 0 ? (
          <p style={{ color: '#666', fontStyle: 'italic' }}>No nodes found. Initialize sample data to get started.</p>
        ) : (
          <div style={styles.grid}>
            {nodes.map(node => (
              <div key={node.id} style={styles.card}>
                <h4 style={{ margin: '0 0 5px 0', color: '#333' }}>{node.id}</h4>
                <p style={{ margin: '0', fontSize: '14px', color: '#666' }}>{node.name}</p>
              </div>
            ))}
          </div>
        )}
      </div>

      <div style={styles.panel}>
        <h3 style={{ margin: '0 0 15px 0', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Route size={20} />
          Edges ({edges.length})
        </h3>
        
        {edges.length === 0 ? (
          <p style={{ color: '#666', fontStyle: 'italic' }}>No edges found. Initialize sample data to get started.</p>
        ) : (
          <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
            {edges.map((edge, index) => (
              <div key={index} style={styles.edgeCard}>
                <div>
                  <strong>{edge.from} → {edge.to}</strong>
                  <div style={{ fontSize: '12px', color: '#666', marginTop: '2px' }}>
                    Weight: {edge.weight}
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                  {edge.blocked ? (
                    <ZapOff size={16} style={{ color: '#dc3545' }} />
                  ) : (
                    <Zap size={16} style={{ color: '#28a745' }} />
                  )}
                  <span style={{ 
                    fontSize: '12px', 
                    color: edge.blocked ? '#dc3545' : '#28a745',
                    fontWeight: 'bold'
                  }}>
                    {edge.blocked ? 'Blocked' : 'Open'}
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
    <div style={styles.container}>
      <header style={styles.header}>
        <h1 style={{ margin: '0 0 10px 0' }}>🗺️ Route Management Dashboard</h1>
        <p style={{ margin: 0, opacity: 0.9 }}>Manage nodes, edges, and find optimal paths</p>
      </header>

      {message && (
        <div style={styles.message}>
          <span>{message}</span>
          <button 
            onClick={() => setMessage('')}
            style={{ border: 'none', background: 'none', fontSize: '20px', cursor: 'pointer' }}
          >
            ×
          </button>
        </div>
      )}

      <div style={styles.tabs}>
        <button
          style={{
            ...styles.tab,
            ...(activeTab === 'pathfinding' ? styles.activeTab : {})
          }}
          onClick={() => setActiveTab('pathfinding')}
        >
          <Navigation size={18} />
          Pathfinding
        </button>
        <button
          style={{
            ...styles.tab,
            ...(activeTab === 'data' ? styles.activeTab : {})
          }}
          onClick={() => setActiveTab('data')}
        >
          <Settings size={18} />
          Data View
        </button>
      </div>

      {activeTab === 'pathfinding' && renderPathfindingTab()}
      {activeTab === 'data' && renderDataTab()}
    </div>
  );
};

export default RouteManagementUI;