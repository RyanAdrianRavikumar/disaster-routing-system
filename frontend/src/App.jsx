import React, { useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import './App.css';
import RescueQueueUI from './pages/RescueQueueUI';
import ShelterManagementUI from './pages/ShelterManagementUI';
import RouteManagementUI from './pages/RouteManagementUI';
import UserRegistration from './pages/UserRegistration';
import SensorSimulation from './pages/SensorSimulation';
import Navigation from './components/Navigation';

function App() {
  const [isRegistered, setIsRegistered] = useState(false);

  // Function to handle successful registration
  const handleRegistrationSuccess = () => {
    setIsRegistered(true);
  };

  return (
    <Router>
      <div className="App">
        {/* Show navigation only when user is registered */}
        {isRegistered && <Navigation />}
        
        <Routes>
          {/* Root path redirects to registration or route management based on registration status */}
          <Route 
            path="/" 
            element={
              isRegistered ? 
              <Navigate to="/routemanagement" replace /> : 
              <UserRegistration onRegistrationSuccess={handleRegistrationSuccess} />
            } 
          />
          
          <Route path="/rescuequeue" element={<RescueQueueUI />} />
          <Route path="/sheltermanagement" element={<ShelterManagementUI />} />
          <Route path="/routemanagement" element={<RouteManagementUI />} />
          <Route 
            path="/userregistration" 
            element={
              isRegistered ? 
              <Navigate to="/routemanagement" replace /> : 
              <UserRegistration onRegistrationSuccess={handleRegistrationSuccess} />
            } 
          />
          <Route path="/sensorsimulation" element={<SensorSimulation />} />
          
          {/* Redirect any unknown routes to the root */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;