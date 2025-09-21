import React, { useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import './App.css';
import RescueQueueUI from './pages/RescueQueueUI';
import ShelterManagementUI from './pages/ShelterManagementUI';
import RouteManagementUI from './pages/RouteManagementUI';
import ShelterRouteUI from './pages/ShelterRouteUI';
import UserRegistration from './pages/UserRegistration';
import UserLogin from './pages/UserLogin'; // NEW: Import the login component
import SensorSimulation from './pages/SensorSimulation';
import Navigation from './components/Navigation';

function App() {
  const [isRegistered, setIsRegistered] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false); // NEW: Login state
  const [userData, setUserData] = useState(null); // NEW: Store user data

  // Function to handle successful registration
  const handleRegistrationSuccess = () => {
    setIsRegistered(true);
  };

  // NEW: Function to handle successful login
  const handleLoginSuccess = (userData) => {
    setIsLoggedIn(true);
    setUserData(userData);
    // Store login status in localStorage for persistence
    localStorage.setItem('isLoggedIn', 'true');
    localStorage.setItem('userData', JSON.stringify(userData));
  };

  // NEW: Function to handle logout
  const handleLogout = () => {
    setIsLoggedIn(false);
    setUserData(null);
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('userData');
  };

  // NEW: Check if user is logged in on app load
  React.useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn');
    const userData = localStorage.getItem('userData');
    
    if (loggedIn === 'true' && userData) {
      setIsLoggedIn(true);
      setUserData(JSON.parse(userData));
    }
  }, []);

  return (
    <Router>
      <div className="App">
        {/* Show navigation only when user is logged in */}
        {isLoggedIn && <Navigation onLogout={handleLogout} userData={userData} />}

        <Routes>
          {/* Root path redirects to login or route management */}
          <Route 
            path="/" 
            element={
              isLoggedIn ? (
                <Navigate to="/routemanagement" replace />
              ) : (
                <UserLogin onLoginSuccess={handleLoginSuccess} />
              )
            } 
          />

          {/* Login route */}
          <Route 
            path="/login" 
            element={
              isLoggedIn ? (
                <Navigate to="/routemanagement" replace />
              ) : (
                <UserLogin onLoginSuccess={handleLoginSuccess} />
              )
            } 
          />

          {/* Registration route */}
          <Route 
            path="/register" 
            element={
              isLoggedIn ? (
                <Navigate to="/routemanagement" replace />
              ) : (
                <UserRegistration onRegistrationSuccess={handleRegistrationSuccess} />
              )
            } 
          />

          {/* Protected routes - only accessible when logged in */}
          <Route 
            path="/sensorsimulation" 
            element={
              isLoggedIn ? (
                <SensorSimulation />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          <Route 
            path="/rescuequeue" 
            element={
              isLoggedIn ? (
                <RescueQueueUI />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          <Route 
            path="/sheltermanagement" 
            element={
              isLoggedIn ? (
                <ShelterManagementUI />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          <Route 
            path="/routemanagement" 
            element={
              isLoggedIn ? (
                <RouteManagementUI />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          <Route 
            path="/shelterroute" 
            element={
              isLoggedIn ? (
                <ShelterRouteUI />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
          
          {/* Catch-all: redirect unknown routes to appropriate page */}
          <Route 
            path="*" 
            element={
              isLoggedIn ? (
                <Navigate to="/routemanagement" replace />
              ) : (
                <Navigate to="/login" replace />
              )
            } 
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;