import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import '../styles/ShelterUI.css'; // Import the CSS file

// Fix Leaflet marker icon issue
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
    iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// Custom green marker icon
const greenIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
    iconRetinaUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
});

const ShelterUI = () => {
    const [shelters, setShelters] = useState([]);
    const [name, setName] = useState('');
    const [capacity, setCapacity] = useState('');
    const [latitude, setLatitude] = useState('');
    const [longitude, setLongitude] = useState('');
    const [error, setError] = useState(null);
    const mapRef = useRef(null);

    // Fetch shelters
    const fetchShelters = async () => {
        try {
            const response = await axios.get('http://localhost:8081/api/shelters');
            console.log('Shelters fetched:', JSON.stringify(response.data, null, 2));
            setShelters(response.data);
            setError(null);
        } catch (error) {
            console.error('Error fetching shelters:', error.message);
            setError('Failed to fetch shelters: ' + error.message);
        }
    };

    useEffect(() => {
        fetchShelters();
    }, []);

    // Invalidate map size on shelters update to fix rendering
    useEffect(() => {
        if (mapRef.current) {
            mapRef.current.invalidateSize();
        }
    }, [shelters]);

    const handleAddShelter = async (e) => {
        e.preventDefault();
        try {
            const shelterId = `S${Date.now()}`;
            await axios.post('http://localhost:8081/api/shelters', null, {
                params: { shelterId, name, capacity, latitude, longitude }
            });
            setName('');
            setCapacity('');
            setLatitude('');
            setLongitude('');
            await fetchShelters();
        } catch (error) {
            console.error('Error adding shelter:', error.message);
            setError('Failed to add shelter: ' + error.message);
        }
    };

    return (
        <div className="shelter-container">
            <header className="app-header">
                <h1>Shelter Management</h1>
                <p>Add, view, and manage emergency shelters</p>
            </header>

            {error && (
                <div className="message-banner error">
                    <span>{error}</span>
                    <button onClick={() => setError('')} className="close-btn">×</button>
                </div>
            )}

            <div className="shelter-dashboard">
                <div className="shelter-form-container">
                    <div className="shelter-card">
                        <div className="shelter-section-header">
                            <h2>Add New Shelter</h2>
                        </div>
                        <form onSubmit={handleAddShelter} className="shelter-form">
                            <div className="input-group">
                                <label>Shelter Name</label>
                                <input
                                    type="text"
                                    placeholder="Enter shelter name"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="input-group">
                                <label>Capacity</label>
                                <input
                                    type="number"
                                    placeholder="Enter capacity"
                                    value={capacity}
                                    onChange={(e) => setCapacity(e.target.value)}
                                    required
                                />
                            </div>
                            <div className="input-group">
                                <label>Latitude</label>
                                <input
                                    type="number"
                                    placeholder="Enter latitude"
                                    value={latitude}
                                    onChange={(e) => setLatitude(e.target.value)}
                                    step="any"
                                    required
                                />
                            </div>
                            <div className="input-group">
                                <label>Longitude</label>
                                <input
                                    type="number"
                                    placeholder="Enter longitude"
                                    value={longitude}
                                    onChange={(e) => setLongitude(e.target.value)}
                                    step="any"
                                    required
                                />
                            </div>
                            <button type="submit" className="btn btn-primary">Add Shelter</button>
                        </form>
                    </div>
                </div>

                <div className="shelter-card">
                    <div className="shelter-section-header">
                        <h2>Shelters Map</h2>
                    </div>
                    <div className="map-container">
                        <MapContainer
                            center={[6.9271, 79.8612]}
                            zoom={13}
                            style={{ height: '100%', width: '100%' }}
                            whenCreated={(map) => (mapRef.current = map)}
                        >
                            <TileLayer
                                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                            />
                            {shelters.length > 0 ? (
                                shelters.filter(shelter => shelter.capacity > 0).map((shelter) => (
                                    <Marker
                                        key={shelter.shelterId}
                                        position={[shelter.latitude, shelter.longitude]}
                                        icon={greenIcon}
                                    >
                                        <Popup>
                                            {shelter.name} (ID: {shelter.shelterId})<br />
                                            Capacity: {shelter.capacity}
                                        </Popup>
                                    </Marker>
                                ))
                            ) : (
                                <div className="no-shelters-message">
                                    No shelters found
                                </div>
                            )}
                        </MapContainer>
                    </div>
                </div>

                <div className="shelter-card">
                    <div className="shelter-section-header">
                        <h2>Shelters List</h2>
                    </div>
                    {shelters.length > 0 ? (
                        <div className="shelters-table-container">
                            <table className="shelters-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Name</th>
                                        <th>Capacity</th>
                                        <th>Latitude</th>
                                        <th>Longitude</th>
                                        <th>Queue</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {shelters.filter(shelter => shelter.capacity > 0).map((shelter) => (
                                        <tr key={shelter.shelterId}>
                                            <td>{shelter.shelterId}</td>
                                            <td>{shelter.name}</td>
                                            <td>{shelter.capacity}</td>
                                            <td>{shelter.latitude}</td>
                                            <td>{shelter.longitude}</td>
                                            <td>
                                                {shelter.queue ? shelter.queue.join(', ') : 'Empty'}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <p className="no-shelters-text">No shelters found</p>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ShelterUI;