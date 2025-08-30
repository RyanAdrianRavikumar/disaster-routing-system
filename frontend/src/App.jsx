import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import RescueQueueUI from './pages/RescueQueueUI'; // Adjust path as needed
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<RescueQueueUI />} />
          {/* Add other routes as needed */}
        </Routes>
      </div>
    </Router>
  );
}

export default App;