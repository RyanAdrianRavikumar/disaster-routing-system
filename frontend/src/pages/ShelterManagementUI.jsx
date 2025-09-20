import React, { useState, useEffect } from 'react';
import '../styles/ShelterManagementUI.css';

// SVG Icons
const RefreshIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <polyline points="23 4 23 10 17 10"></polyline>
    <polyline points="1 20 1 14 7 14"></polyline>
    <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
  </svg>
);

const CheckInIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M5 17H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2h-1"></path>
    <polygon points="12 15 17 21 7 21 12 15"></polygon>
  </svg>
);

const CheckOutIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M19 7V4a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2v3"></path>
    <path d="M5 7h14v10a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V7z"></path>
    <line x1="12" y1="12" x2="12" y2="16"></line>
    <line x1="9" y1="13" x2="15" y2="13"></line>
  </svg>
);

const CreateIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M12 5v14M5 12h14"></path>
  </svg>
);

const ShelterManagementUI = () => {
  const [shelters, setShelters] = useState([]);
  const [selectedShelter, setSelectedShelter] = useState(null);
  const [rfidTag, setRfidTag] = useState('');
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [newShelter, setNewShelter] = useState({ id: '', name: '', capacity: 10 });

  const API_BASE = 'http://localhost:8081/shelters';

  // Fetch all shelters from backend
  const fetchShelters = async () => {
    setIsLoading(true);
    try {
      const res = await fetch(`${API_BASE}/all`);
      if (res.ok) {
        const data = await res.json();
        console.log('Fetched shelters:', data);
        setShelters(data);
        if (!selectedShelter && data.length > 0) {
          setSelectedShelter(data[0]);
        }
        setMessage('Shelters loaded successfully');
      } else {
        setMessage('Failed to fetch shelters');
      }
    } catch (error) {
      console.error('Error fetching shelters:', error);
      setMessage('Error connecting to server');
    }
    setIsLoading(false);
  };

  useEffect(() => {
    fetchShelters();
  }, []);

  const refreshShelterData = async (shelterId) => {
    try {
      const [popRes, remainingRes] = await Promise.all([
        fetch(`${API_BASE}/${shelterId}/population`),
        fetch(`${API_BASE}/${shelterId}/remainingCapacity`)
      ]);
      
      if (popRes.ok && remainingRes.ok) {
        const population = await popRes.json();
        const remaining = await remainingRes.json();
        
        setShelters(prev =>
          prev.map(s => s.shelterId === shelterId ? 
            { ...s, currentPopulation: population, remainingCapacity: remaining } : s
          )
        );
        
        if (selectedShelter?.shelterId === shelterId) {
          setSelectedShelter(prev => ({ 
            ...prev, 
            currentPopulation: population, 
            remainingCapacity: remaining 
          }));
        }
      }
    } catch (error) {
      console.error('Error refreshing shelter data:', error);
    }
  };

  const handleCheckIn = async () => {
    if (!selectedShelter || !rfidTag.trim()) {
      setMessage('Please select a shelter and enter an RFID tag');
      return;
    }
    setIsLoading(true);
    try {
      const res = await fetch(`${API_BASE}/${selectedShelter.shelterId}/checkin/${encodeURIComponent(rfidTag.trim())}`, {
        method: 'POST'
      });
      const result = await res.text();
      setMessage(result);
      setRfidTag('');
      await refreshShelterData(selectedShelter.shelterId);
    } catch (error) {
      console.error('Check-in error:', error);
      setMessage('Error during check-in');
    }
    setIsLoading(false);
  };

  const handleCheckOut = async () => {
    if (!selectedShelter) {
      setMessage('Please select a shelter');
      return;
    }
    setIsLoading(true);
    try {
      const res = await fetch(`${API_BASE}/${selectedShelter.shelterId}/checkout`, {
        method: 'POST'
      });
      const result = await res.text();
      setMessage(result);
      await refreshShelterData(selectedShelter.shelterId);
    } catch (error) {
      console.error('Check-out error:', error);
      setMessage('Error during check-out');
    }
    setIsLoading(false);
  };

  const handleCreateShelter = async () => {
    if (!newShelter.id.trim() || !newShelter.name.trim() || newShelter.capacity <= 0) {
      setMessage('Please fill all fields with valid values');
      return;
    }
    setIsLoading(true);
    try {
      const res = await fetch(
        `${API_BASE}/create/${encodeURIComponent(newShelter.id.trim())}?name=${encodeURIComponent(
          newShelter.name.trim()
        )}&capacity=${newShelter.capacity}`,
        { method: 'POST' }
      );
      const result = await res.text();
      setMessage(result);

      if (res.ok) {
        await fetchShelters();
        setNewShelter({ id: '', name: '', capacity: 10 });
      }
    } catch (error) {
      console.error('Error creating shelter:', error);
      setMessage('Error creating shelter');
    }
    setIsLoading(false);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewShelter(prev => ({
      ...prev,
      [name]: name === 'capacity' ? parseInt(value) || 0 : value
    }));
  };

  return (
    <div className="shelter-container">
      <header className="app-header">
        <h1>Shelter Management Dashboard</h1>
      </header>

      <div className="shelter-dashboard">
        {message && (
          <div className="message-banner">
            <span>{message}</span>
            <button onClick={() => setMessage('')} className="close-btn">×</button>
          </div>
        )}

        <div className="shelter-content">
          <div className="shelter-selection">
            <div className="section-header">
              <h2>Available Shelters</h2>
              <button 
                onClick={fetchShelters}
                disabled={isLoading}
                className="refresh-btn"
              >
                <RefreshIcon />
                {isLoading ? 'Loading...' : 'Refresh'}
              </button>
            </div>
            
            <div className="shelter-cards">
              {shelters.map(shelter => {
                const population = shelter.currentPopulation || shelter.queue?.size || 0;
                const capacity = shelter.capacity;
                const remaining = shelter.remainingCapacity || (capacity - population);
                const capacityPercentage = capacity > 0 ? (population / capacity) * 100 : 0;
                
                return (
                  <div
                    key={shelter.shelterId}
                    className={`shelter-card ${selectedShelter?.shelterId === shelter.shelterId ? 'selected' : ''}`}
                    onClick={() => setSelectedShelter(shelter)}
                  >
                    <h3>{shelter.name}</h3>
                    <p className="shelter-id">ID: {shelter.shelterId}</p>
                    
                    <div className="shelter-stats">
                      <div className="shelter-stat">
                        <span className="stat-label">Capacity</span>
                        <span className="stat-value">{capacity}</span>
                      </div>
                      <div className="shelter-stat">
                        <span className="stat-label">Population</span>
                        <span className="stat-value">{population}</span>
                      </div>
                      <div className="shelter-stat">
                        <span className="stat-label">Available</span>
                        <span className="stat-value highlight">{remaining}</span>
                      </div>
                    </div>
                    
                    <div className="capacity-bar">
                      <div 
                        className="capacity-fill" 
                        style={{ width: `${capacityPercentage}%` }}
                      ></div>
                    </div>
                    
                    {shelter.queue?.queueList && shelter.queue.queueList.length > 0 && (
                      <div className="queue-info">
                        <span className="queue-label">Queue: {shelter.queue.queueList.length}</span>
                        <div className="queue-preview">
                          {shelter.queue.queueList.slice(0, 3).join(', ')}
                          {shelter.queue.queueList.length > 3 && '...'}
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          <div className="shelter-operations">
            {selectedShelter && (
              <div className="shelter-info">
                <h2>Operations - {selectedShelter.name}</h2>
                
                <div className="info-grid">
                  <div className="info-item">
                    <span className="info-label">ID</span>
                    <span className="info-value">{selectedShelter.shelterId}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Name</span>
                    <span className="info-value">{selectedShelter.name}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Capacity</span>
                    <span className="info-value">{selectedShelter.capacity}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Population</span>
                    <span className="info-value">
                      {selectedShelter.currentPopulation || selectedShelter.queue?.size || 0}
                    </span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Available</span>
                    <span className="info-value highlight">
                      {selectedShelter.remainingCapacity || 
                       (selectedShelter.capacity - (selectedShelter.queue?.size || 0))}
                    </span>
                  </div>
                </div>
                
                {selectedShelter.queue?.queueList && selectedShelter.queue.queueList.length > 0 && (
                  <div className="queue-details">
                    <h4>Current Queue</h4>
                    <div className="queue-list">
                      {selectedShelter.queue.queueList.map((rfid, index) => (
                        <div key={index} className="queue-item">
                          <span className="queue-position">{index + 1}.</span>
                          <span className="queue-rfid">{rfid}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}

            <div className="operation-section">
              <h3>Shelter Operations</h3>
              
              <div className="operation-cards">
                <div className="operation-card">
                  <h4>Check-in User</h4>
                  <div className="input-group">
                    <label>RFID Tag</label>
                    <input
                      type="text"
                      value={rfidTag}
                      onChange={e => setRfidTag(e.target.value)}
                      placeholder="Enter RFID Tag"
                      onKeyPress={e => e.key === 'Enter' && handleCheckIn()}
                    />
                  </div>
                  <button 
                    onClick={handleCheckIn} 
                    disabled={isLoading || !rfidTag.trim()}
                    className="operation-btn checkin-btn"
                  >
                    <CheckInIcon />
                    {isLoading ? 'Processing...' : 'Check-in'}
                  </button>
                </div>

                <div className="operation-card">
                  <h4>Check-out User</h4>
                  <p>Check out the next person in queue</p>
                  <button 
                    onClick={handleCheckOut} 
                    disabled={isLoading}
                    className="operation-btn checkout-btn"
                  >
                    <CheckOutIcon />
                    {isLoading ? 'Processing...' : 'Check-out Next'}
                  </button>
                </div>
              </div>
            </div>

            <div className="create-shelter-section">
              <h2>Create New Shelter</h2>
              
              <div className="shelter-form">
                <div className="form-row">
                  <div className="form-group">
                    <label>Shelter ID</label>
                    <input 
                      name="id" 
                      placeholder="Unique identifier" 
                      value={newShelter.id} 
                      onChange={handleInputChange}
                    />
                  </div>
                  
                  <div className="form-group">
                    <label>Shelter Name</label>
                    <input 
                      name="name" 
                      placeholder="Display name" 
                      value={newShelter.name} 
                      onChange={handleInputChange}
                    />
                  </div>
                  
                  <div className="form-group">
                    <label>Capacity</label>
                    <input
                      name="capacity"
                      type="number"
                      min="1"
                      placeholder="Maximum capacity"
                      value={newShelter.capacity}
                      onChange={handleInputChange}
                    />
                  </div>
                </div>
                
                <button 
                  onClick={handleCreateShelter} 
                  disabled={isLoading}
                  className="create-btn"
                >
                  <CreateIcon />
                  {isLoading ? 'Creating...' : 'Create Shelter'}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ShelterManagementUI;