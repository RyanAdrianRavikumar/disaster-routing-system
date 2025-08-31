import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import './App.css';
import RescueQueueUI from './pages/RescueQueueUI';
import ShelterManagementUI from './pages/ShelterManagementUI';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/r" element={<RescueQueueUI />} />
          <Route path="/s" element={<ShelterManagementUI />} />
          {/* Add other routes as needed */}
        </Routes>
      </div>
    </Router>
  );
}

export default App;