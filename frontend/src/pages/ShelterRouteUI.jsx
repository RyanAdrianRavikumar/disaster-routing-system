import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet'; // Leaflet map components
import L from 'leaflet'; // Leaflet library
import '../styles/ShelterRouteUI.css'; // Custom CSS

// Fix Leaflet icons
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-icon-2x.png',
    iconUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-icon.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.3/dist/images/marker-shadow.png',
});

// Custom icons
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

const ShelterRouteUI = () => {
    const [userLocation, setUserLocation] = useState({ lat: null, lng: null }); // User location from user microservice or geolocation
    const [shelters, setShelters] = useState([]); // Shelters from merged service
    const [nodes, setNodes] = useState([]); // Nodes from merged service
    const [shortestPath, setShortestPath] = useState([]); // Path coordinates
    const [routeResult, setRouteResult] = useState(null); // Path result
    const [selectedShelter, setSelectedShelter] = useState(null); // Nearest shelter name
    const [message, setMessage] = useState(''); // Status messages
    const [isLoading, setIsLoading] = useState(false); // Loading state

    const MERGED_API_BASE = 'http://localhost:8081/api'; // Single URL for merged ShelterRouteService
    const USER_API_BASE = 'http://localhost:8080/users'; // User microservice for logged-in user location

    useEffect(() => {
        const fetchUserLocation = async () => { // Fetch user location from user microservice
            try {
                const res = await fetch(`${USER_API_BASE}/current`, { // Fetch logged-in user (add auth header if needed)
                    headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` } // Assume token from login
                });
                if (res.ok) {
                    const userData = await res.json();
                    setUserLocation({ lat: userData.latitude, lng: userData.longitude }); // Set from user data
                } else {
                    // Fallback to geolocation if user endpoint fails
                    navigator.geolocation.getCurrentPosition(
                        pos => setUserLocation({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
                        err => setMessage('Failed to get location')
                    );
                }
            } catch (error) {
                setMessage('Error fetching user location');
                console.error(error);
            }
        };

        fetchUserLocation(); // Fetch on mount
        fetchShelters(); // Fetch shelters from merged service
        fetchNodes(); // Fetch nodes from merged service
    }, []);

    const fetchShelters = async () => { // Fetch shelters
        try {
            const res = await fetch(`${MERGED_API_BASE}/shelters`);
            if (res.ok) {
                const data = await res.json();
                setShelters(data);
            } else {
                setMessage('Failed to fetch shelters');
            }
        } catch (error) {
            setMessage('Error fetching shelters');
            console.error(error);
        }
    };

    const fetchNodes = async () => { // Fetch nodes
        try {
            const res = await fetch(`${MERGED_API_BASE}/nodes`);
            if (res.ok) {
                const data = await res.json();
                setNodes(data);
            } else {
                setMessage('Failed to fetch nodes');
            }
        } catch (error) {
            setMessage('Error fetching nodes');
            console.error(error);
        }
    };

    const findNearestPath = async () => { // Compute and display path
        if (!userLocation.lat) {
            setMessage('User location not available');
            return;
        }
        setIsLoading(true);
        try {
            const res = await fetch(`${MERGED_API_BASE}/nearest-shelter-path?userLat=${userLocation.lat}&userLng=${userLocation.lng}`);
            const data = await res.json();
            if (data.error) {
                setMessage(data.error);
                setShortestPath([]);
                setRouteResult(null);
                setSelectedShelter(null);
            } else {
                setRouteResult({ path: data.path, distance: data.distance });
                setSelectedShelter(data.shelterName);
                const pathCoords = data.path.map(nodeId => { // Convert to lat/lng
                    const node = nodes.find(n => n.id === nodeId);
                    return node && node.latitude && node.longitude ? [node.latitude, node.longitude] : null;
                }).filter(coord => coord);
                setShortestPath(pathCoords);
                setMessage('Path found to nearest shelter');
            }
        } catch (error) {
            setMessage('Error finding path');
            console.error(error);
        }
        setIsLoading(false);
    };

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
            </div>
            {userLocation.lat && (
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
                                icon={shelter.remainingCapacity > 0 ? shelterIconAvailable : shelterIconFull} // Dynamic icon based on capacity
                            >
                                <Popup>
                                    {shelter.name} <br />
                                    Capacity: {shelter.capacity} <br />
                                    Remaining: {shelter.remainingCapacity || shelter.capacity - (shelter.queue?.size || 0)} {/* Handle queue size */}
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

export default ShelterRouteUI;