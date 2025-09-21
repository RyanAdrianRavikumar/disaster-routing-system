import React, { useState, useEffect, useCallback } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import '../styles/ShelterUI.css';

// Fix Leaflet icon paths
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-icon-2x.png',
    iconUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-icon.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-shadow.png',
});

// Custom icons
const shelterIconAvailable = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-green.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-shadow.png',
    shadowSize: [41, 41]
});

const shelterIconFull = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-shadow.png',
    shadowSize: [41, 41]
});

// Mock data for offline testing
const MOCK_SHELTERS = [
    { shelterId: 'A', name: 'Shelter A', capacity: 10, queue: [], latitude: 40.7128, longitude: -74.0060, remainingCapacity: 10 },
    { shelterId: 'D', name: 'Shelter D', capacity: 15, queue: ['user1'], latitude: 40.7428, longitude: -74.0360, remainingCapacity: 14 }
];

const ShelterUI = () => {
    const [shelters, setShelters] = useState(MOCK_SHELTERS);
    const [formData, setFormData] = useState({ shelterId: '', name: '', latitude: '', longitude: '', capacity: '' });
    const [editingId, setEditingId] = useState(null);
    const [rfidTag, setRfidTag] = useState('');
    const [message, setMessage] = useState('Initializing...');
    const [isLoading, setIsLoading] = useState(false);
    const [useMockData, setUseMockData] = useState(false);
    const SHELTERROUTE_API_BASE = 'http://localhost:8081/api';

    const fetchShelters = useCallback(async () => {
        try {
            const token = localStorage.getItem('token');
            const res = await fetch(`${SHELTERROUTE_API_BASE}/shelters`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (res.ok) {
                const data = await res.json();
                setShelters(data.map(s => ({
                    ...s,
                    remainingCapacity: s.remainingCapacity || s.capacity - (s.queue?.length || 0)
                })));
                console.log('Shelters fetched:', data);
                setMessage('Shelters loaded');
            } else {
                console.error('Shelters fetch failed:', res.status, res.statusText);
                setMessage(`Shelters fetch failed (${res.status}). Using mock data.`);
                setUseMockData(true);
            }
        } catch (error) {
            console.error('Shelters fetch error:', error.message);
            setMessage('Error fetching shelters: ' + error.message + '. Using mock data.');
            setUseMockData(true);
        }
    }, []);

    useEffect(() => {
        if (useMockData) {
            setShelters(MOCK_SHELTERS);
            setMessage('Using mock data');
        } else {
            fetchShelters();
        }
    }, [useMockData, fetchShelters]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleRfidChange = (e) => {
        setRfidTag(e.target.value);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!formData.shelterId || !formData.name || !formData.latitude || !formData.longitude || !formData.capacity) {
            setMessage('All fields are required');
            return;
        }
        setIsLoading(true);
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                setMessage('Please log in to perform this action');
                setIsLoading(false);
                return;
            }
            const url = editingId
                ? `${SHELTERROUTE_API_BASE}/shelters/${editingId}?name=${encodeURIComponent(formData.name)}&capacity=${formData.capacity}&latitude=${formData.latitude}&longitude=${formData.longitude}`
                : `${SHELTERROUTE_API_BASE}/shelters/${formData.shelterId}?name=${encodeURIComponent(formData.name)}&capacity=${formData.capacity}&latitude=${formData.latitude}&longitude=${formData.longitude}`;
            const method = editingId ? 'PUT' : 'POST';
            const res = await fetch(url, {
                method,
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (res.ok) {
                const message = await res.text();
                setMessage(message);
                fetchShelters(); // Refresh shelters
                resetForm();
            } else {
                console.error('Shelter operation failed:', res.status, res.statusText);
                setMessage(`Operation failed (${res.status}): ${res.statusText}`);
            }
        } catch (error) {
            console.error('Shelter operation error:', error.message);
            setMessage('Error: ' + error.message);
        }
        setIsLoading(false);
    };

    const handleEdit = (shelter) => {
        setFormData({
            shelterId: shelter.shelterId,
            name: shelter.name,
            latitude: shelter.latitude,
            longitude: shelter.longitude,
            capacity: shelter.capacity
        });
        setEditingId(shelter.shelterId);
    };

    const handleDelete = async (shelterId) => {
        if (!window.confirm('Are you sure you want to delete this shelter?')) return;
        setIsLoading(true);
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                setMessage('Please log in to perform this action');
                setIsLoading(false);
                return;
            }
            const res = await fetch(`${SHELTERROUTE_API_BASE}/shelters/${shelterId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (res.ok) {
                const message = await res.text();
                setMessage(message);
                fetchShelters(); // Refresh shelters
            } else {
                console.error('Delete failed:', res.status, res.statusText);
                setMessage(`Delete failed (${res.status}): ${res.statusText}`);
            }
        } catch (error) {
            console.error('Delete error:', error.message);
            setMessage('Error deleting shelter: ' + error.message);
        }
        setIsLoading(false);
    };

    const handleCheckIn = async (shelterId) => {
        if (!rfidTag) {
            setMessage('Please enter an RFID tag');
            return;
        }
        setIsLoading(true);
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                setMessage('Please log in to perform this action');
                setIsLoading(false);
                return;
            }
            const res = await fetch(`${SHELTERROUTE_API_BASE}/shelters/${shelterId}/checkin/${rfidTag}`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (res.ok) {
                const message = await res.text();
                setMessage(message);
                fetchShelters(); // Refresh shelters
                setRfidTag('');
            } else {
                console.error('Check-in failed:', res.status, res.statusText);
                setMessage(`Check-in failed (${res.status}): ${res.statusText}`);
            }
        } catch (error) {
            console.error('Check-in error:', error.message);
            setMessage('Error checking in: ' + error.message);
        }
        setIsLoading(false);
    };

    const handleCheckOut = async (shelterId) => {
        setIsLoading(true);
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                setMessage('Please log in to perform this action');
                setIsLoading(false);
                return;
            }
            const res = await fetch(`${SHELTERROUTE_API_BASE}/shelters/${shelterId}/checkout`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (res.ok) {
                const message = await res.text();
                setMessage(message);
                fetchShelters(); // Refresh shelters
            } else {
                console.error('Check-out failed:', res.status, res.statusText);
                setMessage(`Check-out failed (${res.status}): ${res.statusText}`);
            }
        } catch (error) {
            console.error('Check-out error:', error.message);
            setMessage('Error checking out: ' + error.message);
        }
        setIsLoading(false);
    };

    const resetForm = () => {
        setFormData({ shelterId: '', name: '', latitude: '', longitude: '', capacity: '' });
        setEditingId(null);
    };

    return (
        <div className="shelter-route-container">
            <header className="app-header">
                <h1>Shelter Management</h1>
                <p>Manage shelters and their capacities</p>
            </header>
            {message && (
                <div className="message-banner">
                    {message}
                    <button onClick={() => setMessage('')} className="close-btn">×</button>
                </div>
            )}
            <div className="controls">
                <button
                    onClick={() => setUseMockData(!useMockData)}
                    className="toggle-btn"
                >
                    {useMockData ? 'Use Real Data' : 'Use Mock Data'}
                </button>
            </div>
            <div className="form-container">
                <h2>{editingId ? 'Edit Shelter' : 'Add New Shelter'}</h2>
                <form onSubmit={handleSubmit}>
                    <input
                        type="text"
                        name="shelterId"
                        value={formData.shelterId}
                        onChange={handleInputChange}
                        placeholder="Shelter ID (e.g., S1)"
                        disabled={isLoading || editingId}
                    />
                    <input
                        type="text"
                        name="name"
                        value={formData.name}
                        onChange={handleInputChange}
                        placeholder="Shelter Name"
                        disabled={isLoading}
                    />
                    <input
                        type="number"
                        name="latitude"
                        value={formData.latitude}
                        onChange={handleInputChange}
                        placeholder="Latitude (e.g., 40.7128)"
                        step="any"
                        disabled={isLoading}
                    />
                    <input
                        type="number"
                        name="longitude"
                        value={formData.longitude}
                        onChange={handleInputChange}
                        placeholder="Longitude (e.g., -74.0060)"
                        step="any"
                        disabled={isLoading}
                    />
                    <input
                        type="number"
                        name="capacity"
                        value={formData.capacity}
                        onChange={handleInputChange}
                        placeholder="Capacity (e.g., 10)"
                        min="1"
                        disabled={isLoading}
                    />
                    <button type="submit" disabled={isLoading}>
                        {isLoading ? 'Processing...' : editingId ? 'Update Shelter' : 'Add Shelter'}
                    </button>
                    {editingId && (
                        <button type="button" onClick={resetForm} disabled={isLoading}>
                            Cancel Edit
                        </button>
                    )}
                </form>
            </div>
            <div className="checkin-container">
                <h2>Check In User</h2>
                <div className="checkin-form">
                    <input
                        type="text"
                        value={rfidTag}
                        onChange={handleRfidChange}
                        placeholder="RFID Tag (e.g., user123)"
                        disabled={isLoading}
                    />
                </div>
            </div>
            <div className="map-container">
                <MapContainer
                    center={[40.7128, -74.0060]}
                    zoom={13}
                    style={{ height: '400px', width: '100%' }}
                >
                    <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                    {shelters.map(shelter => (
                        shelter.latitude && shelter.longitude && (
                            <Marker
                                key={shelter.shelterId}
                                position={[shelter.latitude, shelter.longitude]}
                                icon={shelter.remainingCapacity > 0 ? shelterIconAvailable : shelterIconFull}
                            >
                                <Popup>
                                    {shelter.name} <br />
                                    Capacity: {shelter.capacity} <br />
                                    Remaining: {shelter.remainingCapacity}
                                </Popup>
                            </Marker>
                        )
                    ))}
                </MapContainer>
            </div>
            <div className="shelter-list">
                <h2>Shelter List</h2>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Latitude</th>
                            <th>Longitude</th>
                            <th>Capacity</th>
                            <th>Remaining</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {shelters.map(shelter => (
                            <tr key={shelter.shelterId}>
                                <td>{shelter.shelterId}</td>
                                <td>{shelter.name}</td>
                                <td>{shelter.latitude}</td>
                                <td>{shelter.longitude}</td>
                                <td>{shelter.capacity}</td>
                                <td>{shelter.remainingCapacity}</td>
                                <td>
                                    <button onClick={() => handleEdit(shelter)} disabled={isLoading}>
                                        Edit
                                    </button>
                                    <button onClick={() => handleDelete(shelter.shelterId)} disabled={isLoading}>
                                        Delete
                                    </button>
                                    <button onClick={() => handleCheckIn(shelter.shelterId)} disabled={isLoading || !rfidTag}>
                                        Check In
                                    </button>
                                    <button onClick={() => handleCheckOut(shelter.shelterId)} disabled={isLoading}>
                                        Check Out
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default ShelterUI;