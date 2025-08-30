import React, { useState, useEffect, useCallback } from 'react';
import '../styles/RescueQueueUI.css';

const RescueQueueUI = () => {
  const [queue, setQueue] = useState([]);
  const [nextUser, setNextUser] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [newFamily, setNewFamily] = useState({
    name: '',
    address: '',
    children: 0,
    elderly: 0,
    specialNeeds: ''
  });

  // Calculate priority function
  const calculatePriority = useCallback((children, elderly) => {
    // Simple priority calculation based on vulnerable members
    return children * 2 + elderly * 3;
  }, []);

  // Simulate queue data
  const simulateQueueData = useCallback((size) => {
    // Simulated data for demonstration
    const simulatedQueue = Array.from({ length: size }, (_, i) => ({
      id: i + 1,
      name: `Family ${i + 1}`,
      address: `Address ${i + 1}`,
      children: Math.floor(Math.random() * 4),
      elderly: Math.floor(Math.random() * 3),
      specialNeeds: i % 3 === 0 ? 'Medical assistance needed' : 'None',
      priority: calculatePriority(Math.floor(Math.random() * 4), Math.floor(Math.random() * 3))
    }));
    setQueue(simulatedQueue);
  }, [calculatePriority]);

  // Fetch queue size
  const fetchQueueSize = useCallback(async () => {
    try {
      const response = await fetch('/rescue/size');
      const size = await response.json();
      // For demo purposes, we'll simulate some queue data
      if (size > 0) {
        simulateQueueData(size);
      }
    } catch (error) {
      console.error('Error fetching queue size:', error);
    }
  }, [simulateQueueData]);

  // Fetch next user
  const fetchNextUser = useCallback(async () => {
    try {
      const response = await fetch('/rescue/peek');
      if (response.ok) {
        const user = await response.json();
        setNextUser(user);
      }
    } catch (error) {
      console.error('Error fetching next user:', error);
    }
  }, []);

  // Fetch queue data on component mount
  useEffect(() => {
    fetchQueueSize();
    fetchNextUser();
  }, [fetchQueueSize, fetchNextUser]);

  const handleRescueNext = async () => {
    setIsLoading(true);
    try {
      const response = await fetch('/rescue', { method: 'POST' });
      if (response.ok) {
        const rescuedUser = await response.json();
        setMessage(`Rescued: ${rescuedUser.name || 'Family'}`);
        // Refresh queue data
        fetchQueueSize();
        fetchNextUser();
      } else {
        setMessage('Queue is empty');
      }
    } catch (error) {
      console.error('Error rescuing user:', error);
      setMessage('Error occurred during rescue');
    }
    setIsLoading(false);
  };

  const handleAddFamily = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    
    const familyToAdd = {
      ...newFamily,
      priority: calculatePriority(newFamily.children, newFamily.elderly)
    };
    
    try {
      const response = await fetch('/rescue/enqueue', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify([familyToAdd]),
      });
      
      if (response.ok) {
        const msg = await response.text();
        setMessage(msg);
        setNewFamily({
          name: '',
          address: '',
          children: 0,
          elderly: 0,
          specialNeeds: ''
        });
        // Refresh queue data
        fetchQueueSize();
        fetchNextUser();
      }
    } catch (error) {
      console.error('Error adding family:', error);
      setMessage('Error adding family to queue');
    }
    setIsLoading(false);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewFamily(prev => ({
      ...prev,
      [name]: name === 'children' || name === 'elderly' ? parseInt(value) || 0 : value
    }));
  };

  return (
    <div className="rescue-container">
      <header className="app-header">
        <h1>Emergency Rescue Queue Management</h1>
        <p>Prioritizing families based on vulnerable members</p>
      </header>

      <div className="dashboard">
        <div className="stats-panel">
          <div className="stat-card">
            <h3>Queue Size</h3>
            <p className="stat-number">{queue.length}</p>
          </div>
          <div className="stat-card">
            <h3>Next in Line</h3>
            <p className="stat-number">{nextUser ? 'Family Ready' : 'None'}</p>
          </div>
          <div className="stat-card">
            <h3>Status</h3>
            <p className="stat-number">{queue.length > 0 ? 'Active' : 'Empty'}</p>
          </div>
        </div>

        {message && (
          <div className="message-banner">
            {message}
            <button onClick={() => setMessage('')} className="close-btn">×</button>
          </div>
        )}

        <div className="content-area">
          <div className="next-rescue-section">
            <h2>Next Rescue Priority</h2>
            {nextUser ? (
              <div className="next-family-card">
                <h3>{nextUser.name || 'Unnamed Family'}</h3>
                <p><strong>Address:</strong> {nextUser.address || 'Unknown'}</p>
                <div className="family-stats">
                  <span className="stat-tag children">Children: {nextUser.children || 0}</span>
                  <span className="stat-tag elderly">Elderly: {nextUser.elderly || 0}</span>
                  <span className="stat-tag priority">Priority: {nextUser.priority || calculatePriority(nextUser.children || 0, nextUser.elderly || 0)}</span>
                </div>
                <p><strong>Special Needs:</strong> {nextUser.specialNeeds || 'None reported'}</p>
                <button 
                  onClick={handleRescueNext} 
                  disabled={isLoading}
                  className="rescue-btn"
                >
                  {isLoading ? 'Processing...' : 'Rescue This Family'}
                </button>
              </div>
            ) : (
              <p>No families currently in the queue.</p>
            )}
          </div>

          <div className="add-family-section">
            <h2>Add Family to Rescue Queue</h2>
            <form onSubmit={handleAddFamily} className="family-form">
              <div className="form-group">
                <label>Family Name:</label>
                <input
                  type="text"
                  name="name"
                  value={newFamily.name}
                  onChange={handleInputChange}
                  required
                />
              </div>
              <div className="form-group">
                <label>Address:</label>
                <input
                  type="text"
                  name="address"
                  value={newFamily.address}
                  onChange={handleInputChange}
                  required
                />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Number of Children:</label>
                  <input
                    type="number"
                    name="children"
                    value={newFamily.children}
                    onChange={handleInputChange}
                    min="0"
                  />
                </div>
                <div className="form-group">
                  <label>Number of Elderly:</label>
                  <input
                    type="number"
                    name="elderly"
                    value={newFamily.elderly}
                    onChange={handleInputChange}
                    min="0"
                  />
                </div>
              </div>
              <div className="form-group">
                <label>Special Needs:</label>
                <textarea
                  name="specialNeeds"
                  value={newFamily.specialNeeds}
                  onChange={handleInputChange}
                  rows="3"
                />
              </div>
              <button type="submit" disabled={isLoading} className="add-btn">
                {isLoading ? 'Adding...' : 'Add to Queue'}
              </button>
            </form>
          </div>
        </div>

        <div className="queue-section">
          <h2>Rescue Queue ({queue.length} families)</h2>
          {queue.length > 0 ? (
            <div className="queue-list">
              {queue.map(family => (
                <div key={family.id} className="queue-item">
                  <div className="family-info">
                    <h4>{family.name}</h4>
                    <p>{family.address}</p>
                  </div>
                  <div className="priority-indicator">
                    <span className="priority-badge">Priority: {family.priority}</span>
                    <div className="member-counts">
                      <span className="count-tag children">👶 {family.children}</span>
                      <span className="count-tag elderly">👵 {family.elderly}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="empty-queue">No families in the rescue queue.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default RescueQueueUI;