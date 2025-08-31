import React, { useState, useEffect } from 'react';

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

  const styles = {
    container: {
      padding: '20px',
      fontFamily: 'Arial, sans-serif',
      maxWidth: '1200px',
      margin: '0 auto'
    },
    header: {
      backgroundColor: '#007bff',
      color: 'white',
      padding: '20px',
      textAlign: 'center',
      borderRadius: '8px',
      marginBottom: '20px'
    },
    message: {
      padding: '10px',
      backgroundColor: '#e7f3ff',
      border: '1px solid #b3d9ff',
      borderRadius: '4px',
      marginBottom: '20px',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center'
    },
    dashboard: {
      display: 'grid',
      gridTemplateColumns: '2fr 1fr',
      gap: '20px'
    },
    shelterGrid: {
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
      gap: '15px',
      marginTop: '10px'
    },
    shelterCard: {
      border: '2px solid #ddd',
      borderRadius: '8px',
      padding: '15px',
      cursor: 'pointer',
      transition: 'all 0.2s ease'
    },
    shelterCardSelected: {
      border: '2px solid #007bff',
      backgroundColor: '#e7f3ff'
    },
    operationsPanel: {
      border: '1px solid #ddd',
      borderRadius: '8px',
      padding: '20px'
    },
    operationSection: {
      marginBottom: '20px',
      padding: '15px',
      border: '1px solid #eee',
      borderRadius: '6px'
    },
    input: {
      width: '100%',
      padding: '8px 12px',
      border: '1px solid #ddd',
      borderRadius: '4px',
      marginBottom: '10px'
    },
    button: {
      padding: '8px 16px',
      backgroundColor: '#007bff',
      color: 'white',
      border: 'none',
      borderRadius: '4px',
      cursor: 'pointer',
      width: '100%'
    },
    buttonDisabled: {
      backgroundColor: '#ccc',
      cursor: 'not-allowed'
    },
    checkinButton: {
      backgroundColor: '#28a745'
    },
    checkoutButton: {
      backgroundColor: '#fd7e14'
    },
    refreshButton: {
      backgroundColor: '#6c757d',
      marginBottom: '10px'
    }
  };

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <h1>Shelter Management Dashboard</h1>
      </header>

      {message && (
        <div style={styles.message}>
          <span>{message}</span>
          <button onClick={() => setMessage('')} style={{border: 'none', background: 'none', fontSize: '20px'}}>×</button>
        </div>
      )}

      <div style={styles.dashboard}>
        <div>
          <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
            <h2>Available Shelters</h2>
            <button 
              onClick={fetchShelters}
              disabled={isLoading}
              style={{...styles.button, ...styles.refreshButton, width: 'auto', padding: '6px 12px'}}
            >
              {isLoading ? 'Loading...' : 'Refresh'}
            </button>
          </div>
          
          <div style={styles.shelterGrid}>
            {shelters.map(shelter => (
              <div
                key={shelter.shelterId}
                style={{
                  ...styles.shelterCard,
                  ...(selectedShelter?.shelterId === shelter.shelterId ? styles.shelterCardSelected : {})
                }}
                onClick={() => setSelectedShelter(shelter)}
              >
                <h3 style={{margin: '0 0 10px 0', color: '#333'}}>{shelter.name}</h3>
                <p style={{margin: '5px 0', fontSize: '14px', color: '#666'}}>
                  ID: {shelter.shelterId}
                </p>
                <p style={{margin: '5px 0', fontSize: '14px'}}>
                  Capacity: {shelter.capacity}
                </p>
                <p style={{margin: '5px 0', fontSize: '14px'}}>
                  Population: {shelter.currentPopulation || shelter.queue?.size || 0}
                </p>
                <p style={{margin: '5px 0', fontSize: '14px'}}>
                  Available: {shelter.remainingCapacity || (shelter.capacity - (shelter.queue?.size || 0))}
                </p>
                {shelter.queue?.queueList && shelter.queue.queueList.length > 0 && (
                  <p style={{margin: '8px 0 0 0', fontSize: '12px', color: '#666', borderTop: '1px solid #eee', paddingTop: '5px'}}>
                    Queue: {shelter.queue.queueList.slice(0, 3).join(', ')}
                    {shelter.queue.queueList.length > 3 && '...'}
                  </p>
                )}
              </div>
            ))}
          </div>
        </div>

        <div>
          {selectedShelter && (
            <div style={styles.operationsPanel}>
              <h3 style={{marginTop: '0'}}>Operations - {selectedShelter.name}</h3>
              
              <div style={styles.operationSection}>
                <h4>Check-in User</h4>
                <input
                  type="text"
                  value={rfidTag}
                  onChange={e => setRfidTag(e.target.value)}
                  placeholder="Enter RFID Tag"
                  style={styles.input}
                  onKeyPress={e => e.key === 'Enter' && handleCheckIn()}
                />
                <button 
                  onClick={handleCheckIn} 
                  disabled={isLoading || !rfidTag.trim()}
                  style={{...styles.button, ...styles.checkinButton, ...(isLoading || !rfidTag.trim() ? styles.buttonDisabled : {})}}
                >
                  {isLoading ? 'Processing...' : 'Check-in'}
                </button>
              </div>

              <div style={styles.operationSection}>
                <h4>Check-out User</h4>
                <button 
                  onClick={handleCheckOut} 
                  disabled={isLoading}
                  style={{...styles.button, ...styles.checkoutButton, ...(isLoading ? styles.buttonDisabled : {})}}
                >
                  {isLoading ? 'Processing...' : 'Check-out Next'}
                </button>
              </div>

              <div style={{...styles.operationSection, backgroundColor: '#f8f9fa'}}>
                <h4>Shelter Details</h4>
                <p><strong>ID:</strong> {selectedShelter.shelterId}</p>
                <p><strong>Name:</strong> {selectedShelter.name}</p>
                <p><strong>Capacity:</strong> {selectedShelter.capacity}</p>
                <p><strong>Current Population:</strong> {selectedShelter.currentPopulation || selectedShelter.queue?.size || 0}</p>
                <p><strong>Remaining Space:</strong> {selectedShelter.remainingCapacity || (selectedShelter.capacity - (selectedShelter.queue?.size || 0))}</p>
                
                {selectedShelter.queue?.queueList && selectedShelter.queue.queueList.length > 0 && (
                  <div style={{marginTop: '10px', paddingTop: '10px', borderTop: '1px solid #ddd'}}>
                    <strong>Current Queue:</strong>
                    <div style={{maxHeight: '100px', overflowY: 'auto', marginTop: '5px'}}>
                      {selectedShelter.queue.queueList.map((rfid, index) => (
                        <div key={index} style={{fontSize: '12px', padding: '2px 0'}}>
                          {index + 1}. {rfid}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}

          <div style={{...styles.operationsPanel, marginTop: '20px'}}>
            <h3 style={{marginTop: '0'}}>Create New Shelter</h3>
            <input 
              name="id" 
              placeholder="Shelter ID" 
              value={newShelter.id} 
              onChange={handleInputChange}
              style={styles.input}
            />
            <input 
              name="name" 
              placeholder="Shelter Name" 
              value={newShelter.name} 
              onChange={handleInputChange}
              style={styles.input}
            />
            <input
              name="capacity"
              type="number"
              min="1"
              placeholder="Capacity"
              value={newShelter.capacity}
              onChange={handleInputChange}
              style={styles.input}
            />
            <button 
              onClick={handleCreateShelter} 
              disabled={isLoading}
              style={{...styles.button, ...(isLoading ? styles.buttonDisabled : {})}}
            >
              {isLoading ? 'Creating...' : 'Create Shelter'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ShelterManagementUI;