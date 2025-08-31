import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import './App.css';
import RescueQueueUI from './pages/RescueQueueUI';
import ShelterManagementUI from './pages/ShelterManagementUI';
import RouteManagementUI from './pages/RouteManagementUI';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/rescuequeue" element={<RescueQueueUI />} />
          <Route path="/sheltermanagement" element={<ShelterManagementUI />} />
          <Route path="/routemanagement" element={<RouteManagementUI />} />
          {/* Add other routes as needed */}
        </Routes>
      </div>
    </Router>
  );
}

export default App;