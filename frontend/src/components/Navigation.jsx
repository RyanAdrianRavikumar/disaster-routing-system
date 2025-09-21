// components/Navigation.js
import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Navigation.css';

const Navigation = () => {
  const location = useLocation();

  return (
    <nav className="main-navigation">
      <div className="nav-container">
        <div className="nav-brand">
          <h2>Rescue Management System</h2>
        </div>
        <ul className="nav-menu">
          <li className="nav-item">
            <Link 
              to="/routemanagement" 
              className={`nav-link ${location.pathname === '/routemanagement' ? 'active' : ''}`}
            >
              Route Management
            </Link>
          </li>
          <li className="nav-item">
            <Link 
              to="/sheltermanagement" 
              className={`nav-link ${location.pathname === '/sheltermanagement' ? 'active' : ''}`}
            >
              Shelter Management
            </Link>
          </li>
          <li className="nav-item">
            <Link 
              to="/rescuequeue" 
              className={`nav-link ${location.pathname === '/rescuequeue' ? 'active' : ''}`}
            >
              Rescue Queue
            </Link>
          </li>
          <li className="nav-item">
            <Link 
              to="/sensorsimulation" 
              className={`nav-link ${location.pathname === '/sensorsimulation' ? 'active' : ''}`}
            >
              Sensor Simulation
            </Link>
          </li>
        </ul>
      </div>
    </nav>
  );
};

export default Navigation;