
import React, { useState } from "react";
import "../styles/aiAssistant.css";
import { getToken } from "../services/authService";
export default function AIAssistant() {
  const [message, setMessage] = useState("");
  const [messages, setMessages] = useState([
    {
      role: "assistant",
      text: "Hello! How can I help you manage your farm today?",
    },
  ]);

  const suggestions = [
    "Show my registered crops",
    "Check irrigation status",
    "Check inventory availability",
    "Show equipment status",
  ];

  async function sendMessage(text = message) {
  const value = text.trim();

  if (!value) return;

  setMessages((previous) => [
    ...previous,
    { role: "user", text: value },
  ]);

  setMessage("");

  try {
    const token = getToken();

    if (!token) {
      throw new Error("Authentication token not found. Please log in again.");
    }

    const response = await fetch("http://localhost:8001/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        message: value,
      }),
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.detail || "Chatbot request failed");
    }
    if (typeof data.reply !== "string") {
      throw new Error("Invalid chatbot response format");
    }

    setMessages((previous) => [
      ...previous,
      {
        role: "assistant",
        text: data.reply,
      },
    ]);

  } catch (error) {
    setMessages((previous) => [
      ...previous,
      {
        role: "assistant",
        text: `Error: ${error.message}`,
      },
    ]);
  }
}

  return (
    <div className="ai-assistant-page">
      <div className="ai-assistant-header">
        <div className="ai-assistant-icon">🤖</div>
        <div>
          <h1>AI Assistant</h1>
          <p>Your intelligent assistant for farm operations</p>
        </div>
      </div>

      <div className="ai-chat-card">
        <div className="ai-chat-messages">
          {messages.map((item, index) => (
            <div
              key={index}
              className={`ai-message ${item.role}`}
            >
              <div className="ai-message-label">
                {item.role === "assistant" ? "FarmVerse AI" : "You"}
              </div>
              <div className="ai-message-text">{item.text}</div>
            </div>
          ))}
        </div>

        <div className="ai-suggestions">
          <h3>Suggested questions</h3>

          <div className="ai-suggestion-list">
            {suggestions.map((item) => (
              <button
                key={item}
                type="button"
                onClick={() => sendMessage(item)}
              >
                {item}
              </button>
            ))}
          </div>
        </div>

        <form
          className="ai-chat-input"
          onSubmit={(event) => {
            event.preventDefault();
            sendMessage();
          }}
        >
          <input
            value={message}
            onChange={(event) => setMessage(event.target.value)}
            placeholder="Ask about your farm..."
          />

          <button type="submit" disabled={!message.trim()}>
            Send
          </button>
        </form>
      </div>
    </div>
  );
}
