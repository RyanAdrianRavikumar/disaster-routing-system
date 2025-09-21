import React, { useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import './App.css';
import RescueQueueUI from './pages/RescueQueueUI';
import ShelterManagementUI from './pages/ShelterManagementUI';
import RouteManagementUI from './pages/RouteManagementUI';
import ShelterRouteUI from './pages/ShelterRouteUI'; // FIXED: import missing
import UserRegistration from './pages/UserRegistration';
import SensorSimulation from './pages/SensorSimulation';
import Navigation from './components/Navigation';
import ShelterUI from './pages/ShelterUI'

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
          {/* Root path redirects to registration or route management */}
          <Route
            path="/"
            element={
              isRegistered ? (
                <Navigate to="/routemanagement" replace />
              ) : (
                <UserRegistration onRegistrationSuccess={handleRegistrationSuccess} />
              )
            }
          />

          <Route path="/sensorsimulation" element={<SensorSimulation />} />
          <Route path="/rescuequeue" element={<RescueQueueUI />} />
          <Route path="/sheltermanagement" element={<ShelterManagementUI />} />
          <Route path="/routemanagement" element={<RouteManagementUI />} />
          <Route path="/shelterroute" element={<ShelterRouteUI />} />
          <Route path="/shelter" element={<ShelterUI />} />
          <Route
            path="/userregistration"
            element={
              isRegistered ? (
                <Navigate to="/routemanagement" replace />
              ) : (
                <UserRegistration onRegistrationSuccess={handleRegistrationSuccess} />
              )
            }
          />


          {/* Catch-all: redirect unknown routes to root */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;