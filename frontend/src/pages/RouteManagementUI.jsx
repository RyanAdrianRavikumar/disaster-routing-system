import React, { useState, useEffect } from 'react';
import '../styles/RouteManagementUI.css';

const RouteManagementUI = () => {
  const [nodes, setNodes] = useState([]);
  const [edges, setEdges] = useState([]);
  const [startNode, setStartNode] = useState('');
  const [endNode, setEndNode] = useState('');
  const [shortestPath, setShortestPath] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [selectedNode, setSelectedNode] = useState(null);

  // Fetch nodes and edges on component mount
  useEffect(() => {
    fetchNodes();
    fetchEdges();
  }, []);

  const fetchNodes = async () => {
    try {
      const response = await fetch('/route/nodes');
      if (response.ok) {
        const nodesData = await response.json();
        setNodes(nodesData);
      }
    } catch (error) {
      console.error('Error fetching nodes:', error);
      setMessage('Error fetching nodes');
    }
  };

  const fetchEdges = async () => {
    try {
      const response = await fetch('/route/edges');
      if (response.ok) {
        const edgesData = await response.json();
        setEdges(edgesData);
      }
    } catch (error) {
      console.error('Error fetching edges:', error);
      setMessage('Error fetching edges');
    }
  };

  const handleFindShortestPath = async () => {
    if (!startNode || !endNode) {
      setMessage('Please select both start and end nodes');
      return;
    }

    if (startNode === endNode) {
      setMessage('Start and end nodes cannot be the same');
      return;
    }

    setIsLoading(true);
    try {
      const response = await fetch(`/route/shortest-path?start=${startNode}&end=${endNode}`);
      if (response.ok) {
        const pathData = await response.json();
        setShortestPath(pathData);
        setMessage('Shortest path found successfully');
      } else {
        setMessage('Error finding shortest path');
      }
    } catch (error) {
      console.error('Error finding shortest path:', error);
      setMessage('Error finding shortest path');
    }
    setIsLoading(false);
  };

  const handleInitData = async () => {
    setIsLoading(true);
    try {
      const response = await fetch('/admin/init-data', { method: 'POST' });
      if (response.ok) {
        const result = await response.text();
        setMessage(result);
        // Refresh data
        fetchNodes();
        fetchEdges();
      } else {
        setMessage('Error initializing data');
      }
    } catch (error) {
      console.error('Error initializing data:', error);
      setMessage('Error initializing data');
    }
    setIsLoading(false);
  };

  const handleClearData = async () => {
    setIsLoading(true);
    try {
      const response = await fetch('/admin/clear-data', { method: 'POST' });
      if (response.ok) {
        const result = await response.text();
        setMessage(result);
        // Clear local state
        setNodes([]);
        setEdges([]);
        setStartNode('');
        setEndNode('');
        setShortestPath(null);
      } else {
        setMessage('Error clearing data');
      }
    } catch (error) {
      console.error('Error clearing data:', error);
      setMessage('Error clearing data');
    }
    setIsLoading(false);
  };

  const getNodeName = (nodeId) => {
    const node = nodes.find(n => n.id === nodeId);
    return node ? node.name : nodeId;
  };

  const getConnectedEdges = (nodeId) => {
    return edges.filter(edge => edge.source === nodeId || edge.target === nodeId);
  };

  return (
    <div className="route-container">
      <header className="app-header">
        <h1>Route Management System</h1>
        <p>Find shortest paths between locations</p>
      </header>

      <div className="route-dashboard">
        {message && (
          <div className="message-banner">
            {message}
            <button onClick={() => setMessage('')} className="close-btn">×</button>
          </div>
        )}

        <div className="admin-panel">
          <h2>Data Management</h2>
          <div className="admin-actions">
            <button 
              onClick={handleInitData} 
              disabled={isLoading}
              className="admin-btn init-btn"
            >
              {isLoading ? 'Initializing...' : 'Initialize Sample Data'}
            </button>
            <button 
              onClick={handleClearData} 
              disabled={isLoading}
              className="admin-btn clear-btn"
            >
              {isLoading ? 'Clearing...' : 'Clear All Data'}
            </button>
          </div>
        </div>

        <div className="route-content">
          <div className="path-finder-section">
            <h2>Find Shortest Path</h2>
            <div className="path-inputs">
              <div className="input-group">
                <label>Start Node:</label>
                <select 
                  value={startNode} 
                  onChange={(e) => setStartNode(e.target.value)}
                  disabled={nodes.length === 0}
                >
                  <option value="">Select start node</option>
                  {nodes.map(node => (
                    <option key={node.id} value={node.id}>{node.name}</option>
                  ))}
                </select>
              </div>
              <div className="input-group">
                <label>End Node:</label>
                <select 
                  value={endNode} 
                  onChange={(e) => setEndNode(e.target.value)}
                  disabled={nodes.length === 0}
                >
                  <option value="">Select end node</option>
                  {nodes.map(node => (
                    <option key={node.id} value={node.id}>{node.name}</option>
                  ))}
                </select>
              </div>
              <button 
                onClick={handleFindShortestPath} 
                disabled={isLoading || !startNode || !endNode}
                className="find-path-btn"
              >
                {isLoading ? 'Finding Path...' : 'Find Shortest Path'}
              </button>
            </div>

            {shortestPath && (
              <div className="path-result">
                <h3>Shortest Path Result</h3>
                <div className="path-details">
                  <div className="path-info">
                    <span className="info-label">Total Distance:</span>
                    <span className="info-value">{shortestPath.distance} units</span>
                  </div>
                  <div className="path-info">
                    <span className="info-label">Path:</span>
                    <div className="path-nodes">
                      {shortestPath.path.map((nodeId, index) => (
                        <React.Fragment key={nodeId}>
                          <span className="path-node">{getNodeName(nodeId)}</span>
                          {index < shortestPath.path.length - 1 && (
                            <span className="path-arrow">→</span>
                          )}
                        </React.Fragment>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>

          <div className="data-sections">
            <div className="nodes-section">
              <h2>Nodes ({nodes.length})</h2>
              {nodes.length > 0 ? (
                <div className="nodes-list">
                  {nodes.map(node => (
                    <div 
                      key={node.id} 
                      className={`node-card ${selectedNode?.id === node.id ? 'selected' : ''}`}
                      onClick={() => setSelectedNode(node)}
                    >
                      <h4>{node.name}</h4>
                      <p className="node-id">ID: {node.id}</p>
                      {selectedNode?.id === node.id && (
                        <div className="node-connections">
                          <h5>Connected to:</h5>
                          {getConnectedEdges(node.id).map(edge => {
                            const connectedNodeId = edge.source === node.id ? edge.target : edge.source;
                            const connectedNode = nodes.find(n => n.id === connectedNodeId);
                            return (
                              <div key={edge.id} className="connection">
                                <span>{connectedNode ? connectedNode.name : connectedNodeId}</span>
                                <span className="connection-distance">{edge.distance} units</span>
                              </div>
                            );
                          })}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <p className="empty-data">No nodes available. Initialize sample data first.</p>
              )}
            </div>

            <div className="edges-section">
              <h2>Edges ({edges.length})</h2>
              {edges.length > 0 ? (
                <div className="edges-list">
                  {edges.map(edge => {
                    const sourceNode = nodes.find(n => n.id === edge.source);
                    const targetNode = nodes.find(n => n.id === edge.target);
                    return (
                      <div key={edge.id} className="edge-card">
                        <div className="edge-connection">
                          <span className="node-name">{sourceNode ? sourceNode.name : edge.source}</span>
                          <span className="connection-arrow">↔</span>
                          <span className="node-name">{targetNode ? targetNode.name : edge.target}</span>
                        </div>
                        <div className="edge-distance">{edge.distance} units</div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <p className="empty-data">No edges available. Initialize sample data first.</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RouteManagementUI;