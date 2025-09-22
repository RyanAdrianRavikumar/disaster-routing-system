import React, { useState, useEffect, useCallback } from 'react';
import '../styles/RescueQueueUI.css';

const RescueQueueUI = () => {
  const BASE_URL = 'http://localhost:8087/rescue';

  const [queue, setQueue] = useState([]);
  const [nextUser, setNextUser] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [isEmpty, setIsEmpty] = useState(true);
  const [isFull, setIsFull] = useState(false);

  // Fetch entire queue from backend
  const fetchQueue = useCallback(async () => {
    try {
      // Fetch all users currently in queue
      const usersResp = await fetch(`${BASE_URL}/users`);
      if (usersResp.ok) {
        const users = await usersResp.json();
        setQueue(users);
      } else {
        setQueue([]);
      }

      // Fetch next user
      const nextResp = await fetch(`${BASE_URL}/peek`);
      if (nextResp.ok) {
        const user = await nextResp.json();
        setNextUser(user);
      } else {
        setNextUser(null);
      }

      // Fetch status
      const emptyResp = await fetch(`${BASE_URL}/isEmpty`);
      const fullResp = await fetch(`${BASE_URL}/isFull`);
      setIsEmpty(await emptyResp.json());
      setIsFull(await fullResp.json());

    } catch (error) {
      console.error('Error fetching queue:', error);
    }
  }, []);

  useEffect(() => {
    fetchQueue();
    const interval = setInterval(fetchQueue, 5000);
    return () => clearInterval(interval);
  }, [fetchQueue]);

  const handleEnqueueUsers = async () => {
    setIsLoading(true);
    try {
      const resp = await fetch(`${BASE_URL}/enqueue`, { method: 'POST' });
      if (resp.ok) {
        setMessage(await resp.text());
        await fetchQueue();
      } else {
        setMessage('Failed to enqueue users');
      }
    } catch (err) {
      console.error(err);
      setMessage('Error while enqueuing users');
    }
    setIsLoading(false);
  };

  const handleRescueNext = async () => {
    setIsLoading(true);
    try {
      const resp = await fetch(`${BASE_URL}`, { method: 'POST' });
      if (resp.ok) {
        const rescued = await resp.json();
        setMessage(`Rescued: ${rescued.name || 'Family'}`);
        await fetchQueue();
      } else {
        setMessage('Queue is empty');
      }
    } catch (err) {
      console.error(err);
      setMessage('Error during rescue');
    }
    setIsLoading(false);
  };

  return (
    <div className="rescue-container">
      <header className="app-header">
        <h1>Emergency Rescue Queue Management</h1>
        <p>Prioritizing families based on vulnerable members</p>
      </header>

      <div className="rescue-dashboard">
        {message && (
          <div className="message-banner">
            {message}
            <button onClick={() => setMessage('')} className="close-btn">×</button>
          </div>
        )}

        <div className="stats-panel">
          <div className="stat-card">
            <h3>Queue Size</h3>
            <p className="stat-number">{queue.length}</p>
          </div>
          <div className="stat-card">
            <h3>Next in Line</h3>
            <p className="stat-number">{nextUser ? nextUser.name : 'None'}</p>
          </div>
          <div className="stat-card">
            <h3>Queue Empty?</h3>
            <p className="stat-number">{isEmpty ? 'Yes' : 'No'}</p>
          </div>
          <div className="stat-card">
            <h3>Queue Full?</h3>
            <p className="stat-number">{isFull ? 'Yes' : 'No'}</p>
          </div>
        </div>

        <div className="rescue-actions-card">
          <div className="shelter-section-header">
            <h2>Queue Actions</h2>
          </div>
          <div className="actions-panel">
            <button className="btn btn-primary" onClick={handleEnqueueUsers} disabled={isLoading}>
              {isLoading ? 'Enqueuing...' : 'Enqueue All Users'}
            </button>
            <button className="btn btn-rescue" onClick={handleRescueNext} disabled={isLoading || isEmpty}>
              {isLoading ? 'Rescuing...' : 'Rescue Next User'}
            </button>
          </div>
        </div>

        <div className="rescue-queue-card">
          <div className="shelter-section-header">
            <h2>Rescue Queue ({queue.length})</h2>
          </div>
          {queue.length > 0 ? (
            <div className="queue-list">
              {queue.map((user, index) => (
                <div key={index} className="queue-item">
                  <div className="user-info">
                    <h4>{user.name}</h4>
                    <p>Children: {user.childrenCount || 0} | Elderly: {user.elderlyCount || 0}</p>
                    <p>Priority: {user.priority || ((user.childrenCount || 0) * 2 + (user.elderlyCount || 0) * 3)}</p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="no-queue-text">No families currently in the queue.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default RescueQueueUI;