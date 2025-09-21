import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles//UserLogin.css';

// SVG Icons
const EmailIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="2" y="4" width="20" height="16" rx="2"></rect>
    <path d="M22 7l-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"></path>
  </svg>
);

const LockIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
  </svg>
);

const UserLogin = ({ onLoginSuccess }) => {
  const [formData, setFormData] = useState({
    email: '',
    password: ''
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
    
    setIsLoading(true);
    
    try {
      // Make the API call to your backend
      const response = await fetch('http://localhost:8082/users/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        const userData = await response.json();
        console.log('User logged in successfully:', userData);
        
        setMessage('Login successful! Redirecting to dashboard...');
        
        // Reset form
        setFormData({
          email: '',
          password: ''
        });
        
        // Call the success callback to update login status
        if (onLoginSuccess) {
          onLoginSuccess(userData);
        }
        
        // Navigate to route management after a brief delay
        setTimeout(() => {
          navigate('/routemanagement');
        }, 2000);
        
      } else if (response.status === 401) {
        // Unauthorized - invalid credentials
        setMessage('Login failed: Invalid email or password.');
      } else if (response.status === 500) {
        // Internal server error
        setMessage('Login failed: Server error. Please try again later.');
      } else {
        // Other errors
        setMessage('Login failed: An unexpected error occurred. Please try again.');
      }
      
    } catch (error) {
      console.error('Login error:', error);
      setMessage('Login failed: Unable to connect to the server. Please check your connection and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="login-container">
      <header className="app-header">
        <h1>User Login</h1>
        <p>Sign in to access shelter services</p>
      </header>

      <div className="login-dashboard">
        {message && (
          <div className={`message-banner ${message.includes('successful') ? 'success' : 'error'}`}>
            <span>{message}</span>
            <button onClick={() => setMessage('')} className="close-btn">×</button>
          </div>
        )}

        <div className="login-content">
          <div className="login-form-container">
            <div className="form-header">
              <h2>Sign In</h2>
              <p>Enter your credentials to access your account</p>
            </div>

            <form onSubmit={handleSubmit} className="login-form">
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
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Password *</label>
                  <div className="password-input-container">
                    <div className="input-with-icon">
                      <LockIcon />
                      <input
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleInputChange}
                        placeholder="Enter your password"
                        required
                        className="password-input"
                      />
                    </div>
                    <a href="#forgot-password" className="forgot-password-inside">Forgot Password?</a>
                  </div>
                </div>
              </div>

              <button 
                type="submit" 
                disabled={isLoading}
                className="submit-btn"
              >
                {isLoading ? 'Signing In...' : 'Sign In'}
              </button>
            </form>

            <div className="login-redirect">
              <p>Don't have an account? <a href="/register">Register here</a></p>
            </div>
          </div>

          <div className="login-info">
            <div className="info-card">
              <h3>Benefits of Your Account</h3>
              <ul>
                <li>Quick access to shelter services</li>
                <li>Faster check-in process</li>
                <li>Receive important notifications</li>
                <li>Track your shelter history</li>
              </ul>
            </div>

            <div className="info-card">
              <h3>Need Help?</h3>
              <p>Contact shelter staff at the front desk for assistance with login issues.</p>
              <p className="contact-info">📞 (555) 123-HELP</p>
              <p className="contact-info">✉️ help@sheltersystem.org</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserLogin;