import React, { useState, useEffect } from 'react';
import '../styles/ShelterManagementUI.css';

const ShelterManagementUI = () => {
  const [shelters, setShelters] = useState([]);
  const [selectedShelter, setSelectedShelter] = useState(null);
  const [rfidTag, setRfidTag] = useState('');
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [newShelter, setNewShelter] = useState({
    id: '',
    name: '',
    capacity: 10
  });

  // Simulated shelter data for demonstration
  useEffect(() => {
    // In a real application, you would fetch this from an API
    const simulatedShelters = [
      { id: 'shelter1', name: 'Northside Emergency Shelter', capacity: 50, population: 32 },
      { id: 'shelter2', name: 'Southside Community Shelter', capacity: 30, population: 28 },
      { id: 'shelter3', name: 'Central City Refuge', capacity: 100, population: 75 }
    ];
    setShelters(simulatedShelters);
    setSelectedShelter(simulatedShelters[0]);
  }, []);

  const handleCheckIn = async () => {
    if (!selectedShelter || !rfidTag) {
      setMessage('Please select a shelter and enter an RFID tag');
      return;
    }

    setIsLoading(true);
    try {
      const response = await fetch(`/shelters/${selectedShelter.id}/checkin/${rfidTag}`, {
        method: 'POST'
      });
      
      if (response.ok) {
        const result = await response.text();
        setMessage(result);
        // Refresh shelter data
        refreshShelterData(selectedShelter.id);
      } else {
        const error = await response.text();
        setMessage(`Error: ${error}`);
      }
    } catch (error) {
      console.error('Error checking in:', error);
      setMessage('Error occurred during check-in');
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
      const response = await fetch(`/shelters/${selectedShelter.id}/checkout`, {
        method: 'POST'
      });
      
      if (response.ok) {
        const result = await response.text();
        setMessage(result);
        // Refresh shelter data
        refreshShelterData(selectedShelter.id);
      } else {
        const error = await response.text();
        setMessage(`Error: ${error}`);
      }
    } catch (error) {
      console.error('Error checking out:', error);
      setMessage('Error occurred during check-out');
    }
    setIsLoading(false);
  };

  const refreshShelterData = async (shelterId) => {
    try {
      // In a real application, you would fetch updated data from the API
      // For demonstration, we'll just simulate a small change
      setShelters(prev => prev.map(shelter => {
        if (shelter.id === shelterId) {
          // Randomly adjust population for demo purposes
          const change = Math.floor(Math.random() * 3) - 1; // -1, 0, or 1
          const newPopulation = Math.max(0, Math.min(shelter.capacity, shelter.population + change));
          return { ...shelter, population: newPopulation };
        }
        return shelter;
      }));
    } catch (error) {
      console.error('Error refreshing shelter data:', error);
    }
  };

  const handleCreateShelter = async (e) => {
    e.preventDefault();
    if (!newShelter.id || !newShelter.name || newShelter.capacity <= 0) {
      setMessage('Please fill all fields with valid values');
      return;
    }

    setIsLoading(true);
    try {
      const response = await fetch(`/shelters/create/${newShelter.id}?name=${encodeURIComponent(newShelter.name)}&capacity=${newShelter.capacity}`, {
        method: 'POST'
      });
      
      if (response.ok) {
        const result = await response.text();
        setMessage(result);
        
        // Add the new shelter to our list
        const shelterToAdd = {
          id: newShelter.id,
          name: newShelter.name,
          capacity: newShelter.capacity,
          population: 0
        };
        
        setShelters(prev => [...prev, shelterToAdd]);
        setSelectedShelter(shelterToAdd);
        setNewShelter({ id: '', name: '', capacity: 10 });
      } else {
        const error = await response.text();
        setMessage(`Error: ${error}`);
      }
    } catch (error) {
      console.error('Error creating shelter:', error);
      setMessage('Error occurred while creating shelter');
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
        <h1>Shelter Management System</h1>
        <p>Manage shelter check-ins, check-outs, and capacity</p>
      </header>

      <div className="shelter-dashboard">
        {message && (
          <div className="message-banner">
            {message}
            <button onClick={() => setMessage('')} className="close-btn">×</button>
          </div>
        )}

        <div className="shelter-content">
          <div className="shelter-selection">
            <h2>Select Shelter</h2>
            <div className="shelter-cards">
              {shelters.map(shelter => (
                <div 
                  key={shelter.id} 
                  className={`shelter-card ${selectedShelter?.id === shelter.id ? 'selected' : ''}`}
                  onClick={() => setSelectedShelter(shelter)}
                >
                  <h3>{shelter.name}</h3>
                  <div className="shelter-stats">
                    <div className="shelter-stat">
                      <span className="stat-label">Capacity</span>
                      <span className="stat-value">{shelter.capacity}</span>
                    </div>
                    <div className="shelter-stat">
                      <span className="stat-label">Current</span>
                      <span className="stat-value">{shelter.population}</span>
                    </div>
                    <div className="shelter-stat">
                      <span className="stat-label">Available</span>
                      <span className="stat-value">{shelter.capacity - shelter.population}</span>
                    </div>
                  </div>
                  <div className="capacity-bar">
                    <div 
                      className="capacity-fill" 
                      style={{ width: `${(shelter.population / shelter.capacity) * 100}%` }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {selectedShelter && (
            <div className="shelter-operations">
              <div className="shelter-info">
                <h2>{selectedShelter.name}</h2>
                <div className="info-grid">
                  <div className="info-item">
                    <span className="info-label">Shelter ID:</span>
                    <span className="info-value">{selectedShelter.id}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Total Capacity:</span>
                    <span className="info-value">{selectedShelter.capacity}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Current Population:</span>
                    <span className="info-value">{selectedShelter.population}</span>
                  </div>
                  <div className="info-item">
                    <span className="info-label">Remaining Capacity:</span>
                    <span className="info-value highlight">{selectedShelter.capacity - selectedShelter.population}</span>
                  </div>
                </div>
              </div>

              <div className="operation-section">
                <h3>Check-In/Check-Out</h3>
                <div className="operation-cards">
                  <div className="operation-card">
                    <h4>Check-In</h4>
                    <div className="input-group">
                      <label>RFID Tag:</label>
                      <input
                        type="text"
                        value={rfidTag}
                        onChange={(e) => setRfidTag(e.target.value)}
                        placeholder="Enter RFID tag"
                      />
                    </div>
                    <button 
                      onClick={handleCheckIn} 
                      disabled={isLoading || !rfidTag}
                      className="operation-btn checkin-btn"
                    >
                      {isLoading ? 'Processing...' : 'Check-In'}
                    </button>
                  </div>

                  <div className="operation-card">
                    <h4>Check-Out</h4>
                    <p>Check out the most recent arrival</p>
                    <button 
                      onClick={handleCheckOut} 
                      disabled={isLoading}
                      className="operation-btn checkout-btn"
                    >
                      {isLoading ? 'Processing...' : 'Check-Out'}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}

          <div className="create-shelter-section">
            <h2>Create New Shelter</h2>
            <form onSubmit={handleCreateShelter} className="shelter-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Shelter ID:</label>
                  <input
                    type="text"
                    name="id"
                    value={newShelter.id}
                    onChange={handleInputChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Shelter Name:</label>
                  <input
                    type="text"
                    name="name"
                    value={newShelter.name}
                    onChange={handleInputChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Capacity:</label>
                  <input
                    type="number"
                    name="capacity"
                    value={newShelter.capacity}
                    onChange={handleInputChange}
                    min="1"
                    required
                  />
                </div>
              </div>
              <button type="submit" disabled={isLoading} className="create-btn">
                {isLoading ? 'Creating...' : 'Create Shelter'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ShelterManagementUI;