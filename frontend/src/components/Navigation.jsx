import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Navigation.css';

const Navigation = ({ onLogout, userData }) => {
  const location = useLocation();

  const handleLogout = () => {
    if (onLogout) {
      onLogout();
    }
  };

  return (
    <nav className="main-navigation">
      <div className="nav-container">
        {/* Left column with app name */}
        <div className="nav-brand">
          <h2>RescQ</h2>
        </div>
        
        {/* Right column with navigation links */}
        <div className="nav-content">
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
                to="/shelterroute" 
                className={`nav-link ${location.pathname === '/shelterroute' ? 'active' : ''}`}
              >
                Shelter Route
              </Link>
            </li>
            <li className="nav-item">
              <Link 
                to="/shelter" 
                className={`nav-link ${location.pathname === '/shelter' ? 'active' : ''}`}
              >
                Shelter
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
          
          <div className="nav-user">
            {userData && (
              <span className="user-welcome">Welcome, {userData.name}</span>
            )}
            <button onClick={handleLogout} className="logout-btn">
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navigation;