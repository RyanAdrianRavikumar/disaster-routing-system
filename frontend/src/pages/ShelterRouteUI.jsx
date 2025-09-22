import React, { useState, useEffect, useRef, useCallback } from 'react';
import axios from 'axios';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import '../styles/ShelterRouteUI.css'; // Make sure to import the CSS

// Fix Leaflet marker icon issue
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
    iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// Haversine distance function
const haversineDistance = (lat1, lng1, lat2, lng2) => {
    const earthRadius = 6371; // km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLng = (lng2 - lng1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLng / 2) * Math.sin(dLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return earthRadius * c;
};

const ShelterRouteUI = () => {
    const [shelters, setShelters] = useState([]);
    const [userLocation, setUserLocation] = useState(null);
    const [nearestShelter, setNearestShelter] = useState(null);
    const [path, setPath] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const mapRef = useRef(null);

    // Fetch shelters
    const fetchShelters = useCallback(async () => {
        try {
            const response = await axios.get('http://localhost:8081/api/shelters');
            console.log('Shelters fetched:', JSON.stringify(response.data, null, 2));
            setShelters(response.data || []);
            setError(null);
        } catch (err) {
            console.error('Error fetching shelters:', err.message);
            setError('Failed to fetch shelters: ' + err.message);
        }
    }, []);

    // Initialize sample data
    const initSampleData = useCallback(async () => {
        try {
            await axios.post('http://localhost:8081/api/init-data');
            console.log('Sample data initialized');
            await fetchShelters();
        } catch (err) {
            console.error('Error initializing sample data:', err.message);
            setError('Failed to initialize sample data: ' + err.message);
        }
    }, [fetchShelters]);

    // Get user location
    const getUserLocation = useCallback(() => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const { latitude, longitude } = position.coords;
                    console.log('Geolocation success:', { latitude, longitude });
                    setUserLocation({ latitude, longitude });
                },
                (geoErr) => {
                    console.error('Geolocation error:', geoErr.message);
                    setError('Failed to get user location: ' + geoErr.message);
                }
            );
        } else {
            console.error('Geolocation not supported');
            setError('Geolocation not supported by browser');
        }
    }, []);

    // On mount: initialize data and get location
    useEffect(() => {
        fetchShelters();
        getUserLocation();
    }, [fetchShelters, getUserLocation]);

    // Invalidate map size on updates
    useEffect(() => {
        const map = mapRef.current;
        if (map && typeof map.invalidateSize === 'function') {
            try {
                map.invalidateSize();
            } catch (err) {
                console.warn('invalidateSize failed (ignored):', err);
            }
        }
    }, [shelters, path, userLocation]);

    const handleFindNearestShelter = useCallback(async () => {
        if (!userLocation) {
            setError('User location not available');
            return;
        }

        if (!shelters || shelters.length === 0) {
            console.error('No shelters available, attempting to reinitialize');
            await initSampleData();
            if (!shelters || shelters.length === 0) {
                setError('No shelters available after reinitialization');
                return;
            }
        }

        setLoading(true);
        setError(null);
        try {
            // Find nearest shelter with capacity > 0
            let nearestShelter = null;
            let minDistance = Number.MAX_VALUE;
            for (const shelter of shelters) {
                if (shelter.capacity > 0) {
                    const distance = haversineDistance(
                        userLocation.latitude,
                        userLocation.longitude,
                        shelter.latitude,
                        shelter.longitude
                    );
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestShelter = shelter;
                    }
                }
            }

            if (!nearestShelter) {
                console.error('No shelter with available capacity found');
                setPath({ error: 'No shelter with available capacity found' });
                setLoading(false);
                return;
            }

            console.log('Nearest shelter:', JSON.stringify(nearestShelter, null, 2));
            setNearestShelter(nearestShelter);

            // Create a direct path (USER → nearest shelter)
            const pathData = {
                path: ['USER', nearestShelter.shelterId],
                distance: minDistance
            };
            console.log('Path response:', JSON.stringify(pathData, null, 2));
            setPath(pathData);
        } catch (err) {
            console.error('Error finding nearest shelter or path:', err.message);
            setPath({ error: 'Failed to find nearest shelter or path: ' + err.message });
        } finally {
            setLoading(false);
        }
    }, [userLocation, shelters, initSampleData]);

    // Convert path to coordinates for Polyline
    const pathCoordinates = React.useMemo(() => {
        if (!path || path.error || !Array.isArray(path.path) || path.path.length === 0) {
            return [];
        }

        return path.path
            .map((nodeId, index) => {
                if (nodeId === 'USER') {
                    return userLocation ? { key: `USER-${index}`, coords: [userLocation.latitude, userLocation.longitude] } : null;
                }
                const shelter = shelters.find((s) => s.shelterId === nodeId);
                return shelter ? { key: `${nodeId}-${index}`, coords: [shelter.latitude, shelter.longitude] } : null;
            })
            .filter((item) => item !== null);
    }, [path, shelters, userLocation]);

    return (
        <div className="shelter-route-container">
            <header className="app-header">
                <h1>Find Nearest Shelter</h1>
                <p>Locate the closest available shelter with evacuation routes</p>
            </header>

            <div className="shelter-route-dashboard">
                {error && (
                    <div className="message-banner error">
                        <span>{error}</span>
                        <button onClick={() => setError('')} className="close-btn">×</button>
                    </div>
                )}

                <div className="shelter-route-card">
                    <div className="shelter-section-header">
                        <h2>Find Nearest Shelter</h2>
                    </div>
                    <div className="controls-container">
                        <div className="controls">
                            <button 
                                className="btn btn-primary find-path-btn" 
                                onClick={handleFindNearestShelter} 
                                disabled={loading}
                            >
                                {loading ? 'Finding Shelter...' : 'Find Nearest Shelter'}
                            </button>
                            <button 
                                className="btn init-data-btn" 
                                onClick={initSampleData}
                            >
                                Reinitialize Sample Data
                            </button>
                        </div>
                    </div>

                    {userLocation && (
                        <div className="location-info">
                            <h3>Your Location</h3>
                            <p>Latitude: {userLocation.latitude.toFixed(6)}</p>
                            <p>Longitude: {userLocation.longitude.toFixed(6)}</p>
                        </div>
                    )}
                </div>

                <div className="shelter-route-card">
                    <div className="shelter-section-header">
                        <h2>Shelters Map</h2>
                    </div>
                    <div className="map-container">
                        <MapContainer
                            center={userLocation ? [userLocation.latitude, userLocation.longitude] : [6.9271, 79.8612]}
                            zoom={13}
                            style={{ height: '100%', width: '100%' }}
                            whenCreated={(map) => (mapRef.current = map)}
                        >
                            <TileLayer
                                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                            />

                            {/* User location marker */}
                            {userLocation && (
                                <Marker position={[userLocation.latitude, userLocation.longitude]}>
                                    <Popup>Your Location</Popup>
                                </Marker>
                            )}

                            {/* Shelter markers */}
                            {shelters.map((shelter) => (
                                <Marker key={shelter.shelterId} position={[shelter.latitude, shelter.longitude]}>
                                    <Popup>
                                        {shelter.name} (ID: {shelter.shelterId})<br />
                                        Capacity: {shelter.capacity}
                                    </Popup>
                                </Marker>
                            ))}

                            {/* Path polyline */}
                            {pathCoordinates.length > 0 && (
                                <Polyline positions={pathCoordinates.map((item) => item.coords)} color="blue" />
                            )}
                        </MapContainer>
                    </div>
                </div>

                {(nearestShelter || path) && (
                    <div className="shelter-route-card">
                        <div className="shelter-section-header">
                            <h2>Route Information</h2>
                        </div>
                        <div className="results-container">
                            {nearestShelter && (
                                <div className="shelter-info">
                                    <h3>Nearest Shelter</h3>
                                    <p><strong>Name:</strong> {nearestShelter.name}</p>
                                    <p><strong>ID:</strong> {nearestShelter.shelterId}</p>
                                    <p><strong>Capacity:</strong> {nearestShelter.capacity}</p>
                                    <p><strong>Latitude:</strong> {nearestShelter.latitude.toFixed(6)}</p>
                                    <p><strong>Longitude:</strong> {nearestShelter.longitude.toFixed(6)}</p>
                                </div>
                            )}

                            {path && !path.error && path.path && (
                                <div className="path-info">
                                    <h3>Evacuation Path</h3>
                                    <p><strong>Distance:</strong> {path.distance.toFixed(2)} km</p>
                                    
                                    <div className="path-nodes">
                                        {path.path.map((nodeId, index) => (
                                            <React.Fragment key={`${nodeId}-${index}`}>
                                                {index > 0 && <span className="path-arrow">→</span>}
                                                <span className={`path-node ${nodeId === 'USER' ? 'user' : ''}`}>
                                                    {nodeId === 'USER'
                                                        ? 'Your Location'
                                                        : shelters.find((s) => s.shelterId === nodeId)?.name || nodeId}
                                                </span>
                                            </React.Fragment>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {path && path.error && (
                    <div className="message-banner error">
                        <span>{path.error}</span>
                        <button onClick={() => setPath(null)} className="close-btn">×</button>
                    </div>
                )}

                <div className="shelter-route-card">
                    <div className="shelter-section-header">
                        <h2>All Shelters</h2>
                    </div>
                    <div className="shelters-list">
                        {shelters.length > 0 ? (
                            <div>
                                {shelters.map((shelter) => (
                                    <div key={shelter.shelterId} className="shelter-item">
                                        <div>
                                            <div className="shelter-name">{shelter.name}</div>
                                            <div className="shelter-details">ID: {shelter.shelterId} | Lat: {shelter.latitude.toFixed(6)}, Lng: {shelter.longitude.toFixed(6)}</div>
                                        </div>
                                        <span className="capacity-badge">Capacity: {shelter.capacity}</span>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <p>No shelters found</p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ShelterRouteUI;