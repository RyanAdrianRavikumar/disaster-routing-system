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

const UserRegistration = ({ onRegistrationSuccess }) => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    rfid: '',
    password: '',
    confirmPassword: ''
  });
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
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
    
    setIsLoading(true);
    
    try {
      // In a real application, you would send this data to your backend API
      console.log('Registration data:', formData);
      
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1500));
      
      setMessage('Registration successful! Welcome to the shelter system.');
      setFormData({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        rfid: '',
        password: '',
        confirmPassword: ''
      });
      
      // Call the success callback to update registration status
      if (onRegistrationSuccess) {
        onRegistrationSuccess();
      }
      
      // Navigate to route management after a brief delay
      setTimeout(() => {
        navigate('/routemanagement');
      }, 2000);
      
    } catch (error) {
      setMessage('Registration failed. Please try again.');
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
                  <label>First Name</label>
                  <div className="input-with-icon">
                    <UserIcon />
                    <input
                      type="text"
                      name="firstName"
                      value={formData.firstName}
                      onChange={handleInputChange}
                      placeholder="Enter your first name"
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Last Name</label>
                  <div className="input-with-icon">
                    <UserIcon />
                    <input
                      type="text"
                      name="lastName"
                      value={formData.lastName}
                      onChange={handleInputChange}
                      placeholder="Enter your last name"
                      required
                    />
                  </div>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Email Address</label>
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
                  <label>Phone Number</label>
                  <div className="input-with-icon">
                    <PhoneIcon />
                    <input
                      type="tel"
                      name="phone"
                      value={formData.phone}
                      onChange={handleInputChange}
                      placeholder="Enter your phone number"
                      required
                    />
                  </div>
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>RFID Tag Number</label>
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
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Password</label>
                  <div className="input-with-icon">
                    <LockIcon />
                    <input
                      type="password"
                      name="password"
                      value={formData.password}
                      onChange={handleInputChange}
                      placeholder="Create a password"
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Confirm Password</label>
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