import React, { useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate, useLocation } from 'react-router-dom';
import './App.css';
import RescueQueueUI from './pages/RescueQueueUI';
import ShelterManagementUI from './pages/ShelterManagementUI';
import RouteManagementUI from './pages/RouteManagementUI';
import ShelterRouteUI from './pages/ShelterRouteUI';
import UserRegistration from './pages/UserRegistration';
import UserLogin from './pages/UserLogin';
import SensorSimulation from './pages/SensorSimulation';
import Navigation from './components/Navigation';
import ShelterUI from './pages/ShelterUI';

// Create a wrapper component to conditionally show navigation
const LayoutWrapper = ({ children, showNavigation, userData, onLogout }) => {
  return (
    <>
      {showNavigation && <Navigation userData={userData} onLogout={onLogout} />}
      {children}
    </>
  );
};

function App() {
  const [isRegistered, setIsRegistered] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userData, setUserData] = useState(null);

  // Function to handle successful registration
  const handleRegistrationSuccess = () => {
    setIsRegistered(true);
  };

  // Function to handle successful login
  const handleLoginSuccess = (userData) => {
    setIsLoggedIn(true);
    setUserData(userData);
  };

  // Function to handle logout
  const handleLogout = () => {
    setIsLoggedIn(false);
    setUserData(null);
  };

  return (
    <Router>
      <div className="App">
        <Routes>
          {/* Root path redirects to login */}
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

          {/* Login page (no navigation) */}
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

          {/* Registration page (no navigation) */}
          <Route
            path="/userregistration"
            element={
              isRegistered || isLoggedIn ? (
                <Navigate to="/routemanagement" replace />
              ) : (
                <UserRegistration onRegistrationSuccess={handleRegistrationSuccess} />
              )
            }
          />

          {/* Public routes with navigation */}
          <Route
            path="/sensorsimulation"
            element={
              <LayoutWrapper showNavigation={true} userData={userData} onLogout={handleLogout}>
                <SensorSimulation />
              </LayoutWrapper>
            }
          />
          
          <Route
            path="/rescuequeue"
            element={
              <LayoutWrapper showNavigation={true} userData={userData} onLogout={handleLogout}>
                <RescueQueueUI />
              </LayoutWrapper>
            }
          />
          
          <Route
            path="/sheltermanagement"
            element={
              <LayoutWrapper showNavigation={true} userData={userData} onLogout={handleLogout}>
                <ShelterManagementUI />
              </LayoutWrapper>
            }
          />
          
          <Route
            path="/shelterroute"
            element={
              <LayoutWrapper showNavigation={true} userData={userData} onLogout={handleLogout}>
                <ShelterRouteUI />
              </LayoutWrapper>
            }
          />
          
          <Route
            path="/shelter"
            element={
              <LayoutWrapper showNavigation={true} userData={userData} onLogout={handleLogout}>
                <ShelterUI />
              </LayoutWrapper>
            }
          />
          
          {/* Protected route with navigation */}
          <Route
            path="/routemanagement"
            element={
              isLoggedIn ? (
                <LayoutWrapper showNavigation={true} userData={userData} onLogout={handleLogout}>
                  <RouteManagementUI />
                </LayoutWrapper>
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
                <Navigate to="/" replace />
              )
            } 
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;