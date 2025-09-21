import React, { useState, useEffect, useCallback } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import L from 'leaflet';
import '../styles/ShelterUI.css';

// Fix Leaflet icon paths for React
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-icon-2x.png',
    iconUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-icon.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-shadow.png',
});

// Custom icons for map markers
const userIcon = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-shadow.png',
    shadowSize: [41, 41]
});

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

const MOCK_NODES = [
    { id: 'A', name: 'Point A', latitude: 40.7128, longitude: -74.0060 },
    { id: 'B', name: 'Point B', latitude: 40.7228, longitude: -74.0160 },
    { id: 'C', name: 'Point C', latitude: 40.7328, longitude: -74.0260 },
    { id: 'D', name: 'Point D', latitude: 40.7428, longitude: -74.0360 },
    { id: 'E', name: 'Point E', latitude: 40.7528, longitude: -74.0460 }
];

const MOCK_PATH = {
    path: ['A', 'B', 'D'],
    distance: 13.0,
    shelterName: 'Shelter D',
    shelterId: 'D'
};

const ShelterUI = () => {
    // Initialize states with sensible defaults
    const [userLocation, setUserLocation] = useState({ lat: 40.7128, lng: -74.0060 }); // Default to NYC
    const [shelters, setShelters] = useState(MOCK_SHELTERS);
    const [nodes, setNodes] = useState(MOCK_NODES);
    const [shortestPath, setShortestPath] = useState([]);
    const [routeResult, setRouteResult] = useState(null);
    const [selectedShelter, setSelectedShelter] = useState(null);
    const [message, setMessage] = useState('Initializing...');
    const [isLoading, setIsLoading] = useState(false);
    const [useMockData, setUseMockData] = useState(false);

    const SHELTERROUTE_API_BASE = 'http://localhost:8081/api';
    const USER_API_BASE = 'http://localhost:8080/api/users';

    // Memoize fetch functions to prevent redefinition
    const fetchUserLocation = useCallback(async () => {
        try {
            const token = localStorage.getItem('token');
            if (!token) {
                setMessage('Please log in to fetch user location');
                fallbackToGeolocation();
                return;
            }
            const res = await fetch(`${USER_API_BASE}/current`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (res.ok) {
                const userData = await res.json();
                if (userData.latitude && userData.longitude) {
                    setUserLocation({ lat: userData.latitude, lng: userData.longitude });
                    console.log('User location fetched:', userData);
                    setMessage('User location loaded');
                } else {
                    setMessage('Invalid user location data');
                    fallbackToGeolocation();
                }
            } else {
                console.error('User location fetch failed:', res.status, res.statusText);
                setMessage(`User location fetch failed (${res.status})`);
                fallbackToGeolocation();
            }
        } catch (error) {
            console.error('User location fetch error:', error.message);
            setMessage('Error fetching user location: ' + error.message);
            fallbackToGeolocation();
        }
    }, []);

    const fallbackToGeolocation = useCallback(() => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                pos => {
                    setUserLocation({ lat: pos.coords.latitude, lng: pos.coords.longitude });
                    console.log('Geolocation success:', pos.coords);
                    setMessage('Using geolocation');
                },
                err => {
                    console.warn('Geolocation failed:', err.message);
                    setMessage('Geolocation failed. Using default location (NYC).');
                }
            );
        } else {
            setMessage('Geolocation not supported. Using default location (NYC).');
        }
    }, []);

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

    const fetchNodes = useCallback(async () => {
        try {
            const token = localStorage.getItem('token');
            const res = await fetch(`${SHELTERROUTE_API_BASE}/nodes`, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (res.ok) {
                const data = await res.json();
                setNodes(data);
                console.log('Nodes fetched:', data);
                setMessage('Nodes loaded');
            } else {
                console.error('Nodes fetch failed:', res.status, res.statusText);
                setMessage(`Nodes fetch failed (${res.status}). Using mock data.`);
                setUseMockData(true);
            }
        } catch (error) {
            console.error('Nodes fetch error:', error.message);
            setMessage('Error fetching nodes: ' + error.message + '. Using mock data.');
            setUseMockData(true);
        }
    }, []);

    const findNearestPath = useCallback(async () => {
        if (!userLocation.lat || !userLocation.lng) {
            setMessage('User location not available');
            return;
        }
        setIsLoading(true);
        if (useMockData) {
            setRouteResult(MOCK_PATH);
            setSelectedShelter(MOCK_PATH.shelterName);
            const pathCoords = MOCK_PATH.path.map(nodeId => {
                const node = MOCK_NODES.find(n => n.id === nodeId);
                return node ? [node.latitude, node.longitude] : null;
            }).filter(coord => coord !== null);
            setShortestPath(pathCoords);
            setMessage('Path displayed using mock data');
            setIsLoading(false);
        } else {
            try {
                const token = localStorage.getItem('token');
                if (!token) {
                    setMessage('Please log in to find path');
                    setIsLoading(false);
                    return;
                }
                const res = await fetch(`${SHELTERROUTE_API_BASE}/nearest-shelter-path?userLat=${userLocation.lat}&userLng=${userLocation.lng}`, {
                    method: 'GET',
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                if (res.ok) {
                    const data = await res.json();
                    if (data.error) {
                        setMessage(data.error);
                        setShortestPath([]);
                        setRouteResult(null);
                        setSelectedShelter(null);
                    } else {
                        setRouteResult({ path: data.path, distance: data.distance });
                        setSelectedShelter(data.shelterName);
                        const pathCoords = data.path.map(nodeId => {
                            const node = nodes.find(n => n.id === nodeId);
                            return node ? [node.latitude, node.longitude] : null;
                        }).filter(coord => coord !== null);
                        setShortestPath(pathCoords);
                        setMessage('Path found to nearest shelter');
                    }
                } else {
                    console.error('Path fetch failed:', res.status, res.statusText);
                    setMessage(`Path fetch failed (${res.status}). Using mock data.`);
                    setUseMockData(true);
                }
            } catch (error) {
                console.error('Path fetch error:', error.message);
                setMessage('Error finding path: ' + error.message + '. Using mock data.');
                setUseMockData(true);
            }
            setIsLoading(false);
        }
    }, [userLocation, nodes, useMockData]);

    useEffect(() => {
        console.log('ShelterUI mounted', { useMockData });
        if (useMockData) {
            setShelters(MOCK_SHELTERS);
            setNodes(MOCK_NODES);
            setMessage('Using mock data');
            fallbackToGeolocation();
        } else {
            fetchUserLocation();
            fetchShelters();
            fetchNodes();
        }
    }, [useMockData, fetchUserLocation, fetchShelters, fetchNodes]);

    return (
        <div className="shelter-route-container">
            <header className="app-header">
                <h1>Shelter Route Finder</h1>
                <p>Find the shortest path to the nearest available shelter</p>
            </header>
            {message && (
                <div className="message-banner">
                    {message}
                    <button onClick={() => setMessage('')} className="close-btn">×</button>
                </div>
            )}
            <div className="controls">
                <button
                    onClick={findNearestPath}
                    disabled={isLoading || !userLocation.lat}
                    className="find-path-btn"
                >
                    {isLoading ? 'Loading...' : 'Find Nearest Shelter'}
                </button>
                <button
                    onClick={() => setUseMockData(!useMockData)}
                    className="toggle-btn"
                >
                    {useMockData ? 'Use Real Data' : 'Use Mock Data'}
                </button>
            </div>
            {userLocation.lat && userLocation.lng ? (
                <MapContainer
                    center={[userLocation.lat, userLocation.lng]}
                    zoom={13}
                    style={{ height: '500px', width: '100%' }}
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
                    <Marker position={[userLocation.lat, userLocation.lng]} icon={userIcon}>
                        <Popup>Your Location</Popup>
                    </Marker>
                    {shortestPath.length > 0 && (
                        <Polyline positions={shortestPath} color="blue" />
                    )}
                </MapContainer>
            ) : (
                <div className="message-banner">Waiting for user location...</div>
            )}
            {routeResult && (
                <div className="path-result">
                    <h2>Shortest Path to {selectedShelter}</h2>
                    <div className="path-nodes">
                        {routeResult.path.map((node, index) => (
                            <span key={node} className="path-node">
                                {node}
                                {index < routeResult.path.length - 1 && <span className="path-arrow">→</span>}
                            </span>
                        ))}
                    </div>
                    <p>Distance: {routeResult.distance.toFixed(2)} km</p>
                </div>
            )}
        </div>
    );
};

export default ShelterUI;