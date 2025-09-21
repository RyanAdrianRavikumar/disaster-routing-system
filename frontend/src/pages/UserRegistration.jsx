import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/UserRegistration.css';

// SVG Icons
const UserIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
    <circle cx="12" cy="7" r="4"></circle>
  </svg>
);

const EmailIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="2" y="4" width="20" height="16" rx="2"></rect>
    <path d="M22 7l-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"></path>
  </svg>
);

const PhoneIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
  </svg>
);

const LockIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
  </svg>
);

const IDIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="2" y="4" width="20" height="16" rx="2"></rect>
    <line x1="8" y1="12" x2="16" y2="12"></line>
    <line x1="8" y1="8" x2="8" y2="8"></line>
    <line x1="16" y1="8" x2="16" y2="8"></line>
    <line x1="8" y1="16" x2="16" y2="16"></line>
  </svg>
);

const FamilyIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
    <circle cx="9" cy="7" r="4"></circle>
    <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
    <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
  </svg>
);

const ChildIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
    <circle cx="9" cy="7" r="4"></circle>
    <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
    <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
  </svg>
);

const ElderlyIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
    <circle cx="9" cy="7" r="4"></circle>
    <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
    <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
  </svg>
);

const LocationIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
    <circle cx="12" cy="10" r="3"></circle>
  </svg>
);

const UserRegistration = ({ onRegistrationSuccess }) => {
  const [formData, setFormData] = useState({
    rfid: '',
    name: '',
    phoneNumber: '',
    email: '',
    password: '',
    confirmPassword: '',
    currentLatitude: '',
    currentLongitude: '',
    familyCount: 1,
    childrenCount: 0,
    elderlyCount: 0
  });
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name.includes('Count') ? parseInt(value) || 0 : value
    }));
  };

  const getCurrentLocation = () => {
    if (navigator.geolocation) {
      setIsLoading(true);
      setMessage('Getting your location...');
      
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setFormData(prev => ({
            ...prev,
            currentLatitude: position.coords.latitude,
            currentLongitude: position.coords.longitude
          }));
          setMessage('Location obtained successfully!');
          setIsLoading(false);
        },
        (error) => {
          setMessage('Unable to get your location. Please enter manually.');
          setIsLoading(false);
        }
      );
    } else {
      setMessage('Geolocation is not supported by this browser. Please enter manually.');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Basic validation
    if (formData.password !== formData.confirmPassword) {
      setMessage('Passwords do not match');
      return;
    }
    
    if (formData.password.length < 6) {
      setMessage('Password must be at least 6 characters long');
      return;
    }
    
    if (!formData.currentLatitude || !formData.currentLongitude) {
      setMessage('Please provide your location');
      return;
    }
    
    setIsLoading(true);
    
    try {
      // Prepare the data for API submission
      const submissionData = {
        rfid: formData.rfid,
        name: formData.name,
        phoneNumber: formData.phoneNumber,
        email: formData.email,
        password: formData.password,
        currentLatitude: parseFloat(formData.currentLatitude),
        currentLongitude: parseFloat(formData.currentLongitude),
        familyCount: formData.familyCount,
        childrenCount: formData.childrenCount,
        elderlyCount: formData.elderlyCount
      };

      console.log('Registration data:', submissionData);
      
      // Make the actual API call to your backend
      const response = await fetch('http://localhost:8082/users/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(submissionData),
      });

      if (response.ok) {
        const createdUser = await response.json();
        console.log('User registered successfully:', createdUser);
        
        setMessage('Registration successful! Welcome to the shelter system.');
        
        // Reset form
        setFormData({
          rfid: '',
          name: '',
          phoneNumber: '',
          email: '',
          password: '',
          confirmPassword: '',
          currentLatitude: '',
          currentLongitude: '',
          familyCount: 1,
          childrenCount: 0,
          elderlyCount: 0
        });
        
        // Call the success callback to update registration status
        if (onRegistrationSuccess) {
          onRegistrationSuccess();
        }
        
        // Navigate to route management after a brief delay
        setTimeout(() => {
          navigate('/routemanagement');
        }, 2000);
        
      } else if (response.status === 400) {
        // Bad request - validation error
        setMessage('Registration failed: Please check your information and try again.');
      } else if (response.status === 500) {
        // Internal server error
        setMessage('Registration failed: Server error. Please try again later.');
      } else {
        // Other errors
        setMessage('Registration failed: An unexpected error occurred. Please try again.');
      }
      
    } catch (error) {
      console.error('Registration error:', error);
      setMessage('Registration failed: Unable to connect to the server. Please check your connection and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="registration-container">
      <header className="app-header">
        <h1>User Registration</h1>
        <p>Create your account to access shelter services</p>
      </header>

      <div className="registration-dashboard">
        {message && (
          <div className={`message-banner ${message.includes('successful') ? 'success' : 'error'}`}>
            <span>{message}</span>
            <button onClick={() => setMessage('')} className="close-btn">×</button>
          </div>
        )}

        <div className="registration-content">
          <div className="registration-form-container">
            <div className="form-header">
              <h2>Create Account</h2>
              <p>Please fill in all the required information</p>
            </div>

            <form onSubmit={handleSubmit} className="registration-form">
              <div className="form-row">
                <div className="form-group">
                  <label>RFID Tag Number *</label>
                  <div className="input-with-icon">
                    <IDIcon />
                    <input
                      type="text"
                      name="rfid"
                      value={formData.rfid}
                      onChange={handleInputChange}
                      placeholder="Enter your RFID tag number"
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Full Name *</label>
                  <div className="input-with-icon">
                    <UserIcon />
                    <input
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      placeholder="Enter your full name"
                      required
                    />
                  </div>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Email Address *</label>
                  <div className="input-with-icon">
                    <EmailIcon />
                    <input
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      placeholder="Enter your email"
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Phone Number *</label>
                  <div className="input-with-icon">
                    <PhoneIcon />
                    <input
                      type="tel"
                      name="phoneNumber"
                      value={formData.phoneNumber}
                      onChange={handleInputChange}
                      placeholder="Enter your phone number"
                      required
                    />
                  </div>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Password *</label>
                  <div className="input-with-icon">
                    <LockIcon />
                    <input
                      type="password"
                      name="password"
                      value={formData.password}
                      onChange={handleInputChange}
                      placeholder="Create a password (min 6 characters)"
                      required
                      minLength="6"
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Confirm Password *</label>
                  <div className="input-with-icon">
                    <LockIcon />
                    <input
                      type="password"
                      name="confirmPassword"
                      value={formData.confirmPassword}
                      onChange={handleInputChange}
                      placeholder="Confirm your password"
                      required
                    />
                  </div>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Family Members Count *</label>
                  <div className="input-with-icon">
                    <FamilyIcon />
                    <input
                      type="number"
                      name="familyCount"
                      value={formData.familyCount}
                      onChange={handleInputChange}
                      min="1"
                      max="20"
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Children Count</label>
                  <div className="input-with-icon">
                    <ChildIcon />
                    <input
                      type="number"
                      name="childrenCount"
                      value={formData.childrenCount}
                      onChange={handleInputChange}
                      min="0"
                      max="20"
                    />
                  </div>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Elderly Count</label>
                  <div className="input-with-icon">
                    <ElderlyIcon />
                    <input
                      type="number"
                      name="elderlyCount"
                      value={formData.elderlyCount}
                      onChange={handleInputChange}
                      min="0"
                      max="20"
                    />
                  </div>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Location *</label>
                  <div className="location-input-group">
                    <div className="location-coordinates">
                      <div className="input-with-icon">
                        <LocationIcon />
                        <input
                          type="number"
                          step="any"
                          name="currentLatitude"
                          value={formData.currentLatitude}
                          onChange={handleInputChange}
                          placeholder="Latitude"
                          required
                        />
                      </div>
                      <div className="input-with-icon">
                        <LocationIcon />
                        <input
                          type="number"
                          step="any"
                          name="currentLongitude"
                          value={formData.currentLongitude}
                          onChange={handleInputChange}
                          placeholder="Longitude"
                          required
                        />
                      </div>
                    </div>
                    <button 
                      type="button" 
                      onClick={getCurrentLocation}
                      className="location-btn"
                      disabled={isLoading}
                    >
                      {isLoading ? 'Getting Location...' : 'Get My Location'}
                    </button>
                  </div>
                </div>
              </div>

              <button 
                type="submit" 
                disabled={isLoading}
                className="submit-btn"
              >
                {isLoading ? 'Creating Account...' : 'Create Account'}
              </button>
            </form>

            <div className="login-redirect">
              <p>Already have an account? <a href="#login">Sign in here</a></p>
            </div>
          </div>

          <div className="registration-info">
            <div className="info-card">
              <h3>Why Register?</h3>
              <ul>
                <li>Quick access to shelter services</li>
                <li>Faster check-in process with RFID</li>
                <li>Receive important notifications</li>
                <li>Track your shelter history</li>
              </ul>
            </div>

            <div className="info-card">
              <h3>RFID Information</h3>
              <p>Your RFID tag will be provided by shelter staff. This tag helps us provide you with faster and more efficient service.</p>
            </div>

            <div className="info-card">
              <h3>Need Help?</h3>
              <p>Contact shelter staff at the front desk for assistance with registration or RFID tags.</p>
              <p className="contact-info">📞 (555) 123-HELP</p>
              <p className="contact-info">✉️ help@sheltersystem.org</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserRegistration;