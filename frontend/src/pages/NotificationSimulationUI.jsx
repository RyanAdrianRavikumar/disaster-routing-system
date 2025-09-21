import React, { useState } from "react";

const DisasterSimulator = () => {
  const [disasterType, setDisasterType] = useState("");
  const [description, setDescription] = useState("");
  const [customMessage, setCustomMessage] = useState("");
  const [response, setResponse] = useState("");

  const simulateDisaster = async () => {
    try {
      const res = await fetch(
        `http://localhost:8083/disaster/simulate?disasterType=${encodeURIComponent(
          disasterType
        )}&description=${encodeURIComponent(description)}`,
        {
          method: "POST",
        }
      );
      const text = await res.text();
      setResponse(text);
    } catch (err) {
      console.error(err);
      setResponse("Error sending disaster notification");
    }
  };

  const sendCustomMessage = async () => {
    try {
      const res = await fetch(
        `http://localhost:8083/disaster/message?message=${encodeURIComponent(
          customMessage
        )}`,
        {
          method: "POST",
        }
      );
      const text = await res.text();
      setResponse(text);
    } catch (err) {
      console.error(err);
      setResponse("Error sending custom message");
    }
  };

  return (
    <div style={{ padding: "2rem", fontFamily: "Arial, sans-serif" }}>
      <h2>Disaster Simulator</h2>

      <div style={{ marginBottom: "1rem" }}>
        <h4>Simulate Disaster</h4>
        <input
          type="text"
          placeholder="Disaster Type"
          value={disasterType}
          onChange={(e) => setDisasterType(e.target.value)}
          style={{ marginRight: "1rem", padding: "0.5rem" }}
        />
        <input
          type="text"
          placeholder="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          style={{ marginRight: "1rem", padding: "0.5rem", width: "300px" }}
        />
        <button onClick={simulateDisaster} style={{ padding: "0.5rem 1rem" }}>
          Send Disaster Notification
        </button>
      </div>

      <div style={{ marginBottom: "1rem" }}>
        <h4>Send Custom Message</h4>
        <input
          type="text"
          placeholder="Custom Message"
          value={customMessage}
          onChange={(e) => setCustomMessage(e.target.value)}
          style={{ marginRight: "1rem", padding: "0.5rem", width: "400px" }}
        />
        <button onClick={sendCustomMessage} style={{ padding: "0.5rem 1rem" }}>
          Send Custom Message
        </button>
      </div>

      {response && (
        <div
          style={{
            marginTop: "2rem",
            padding: "1rem",
            backgroundColor: "#f0f0f0",
            borderRadius: "5px",
            whiteSpace: "pre-wrap",
          }}
        >
          <strong>Response:</strong>
          <p>{response}</p>
        </div>
      )}
    </div>
  );
};

export default DisasterSimulator;
