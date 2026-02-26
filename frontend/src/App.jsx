import { useEffect, useMemo, useRef, useState } from "react";
import "./App.css";

const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

function ChatBubble({ sender, text, timestamp }) {
  const isUser = sender === "user";
  const time = new Date(timestamp).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });

  return (
    <div className={`bubble ${isUser ? "bubble-user" : "bubble-bot"}`}>
      <div className="bubble-meta">
        <span>{isUser ? "You" : "Alon's Bot"}</span>
        <small>{time}</small>
      </div>
      <p>{text}</p>
    </div>
  );
}

function SuggestionChips({ suggestions, onSelect, disabled }) {
  const memoSuggestions = useMemo(() => suggestions ?? [], [suggestions]);
  if (!memoSuggestions.length) return null;
  return (
    <div className="chip-row">
      {memoSuggestions.map((item, index) => (
        <button
          key={`${item}-${index}`}
          onClick={() => onSelect(item)}
          disabled={disabled}
          className="chip"
        >
          {item}
        </button>
      ))}
    </div>
  );
}

function App() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [suggestions, setSuggestions] = useState([]);
  const [error, setError] = useState(null);
  const endOfChatRef = useRef(null);

  const scrollToBottom = () => {
    endOfChatRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    fetch(`${API_BASE}/api/messages`)
      .then((res) => res.json())
      .then((history) => {
        setMessages(history);
        scrollToBottom();
      })
      .catch(() => setMessages([]));
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const sendMessage = async (text) => {
    if (!text.trim() || loading) return;
    setLoading(true);
    setError(null);
    setInput("");

    try {
      const response = await fetch(`${API_BASE}/api/messages`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: text }),
      });

      if (!response.ok) {
        throw new Error("The server refused to answer. Try again.");
      }

      const data = await response.json();
      setSuggestions(data.suggestions ?? []);

      const updatedHistory = await fetch(`${API_BASE}/api/messages`).then((res) =>
        res.json()
      );
      setMessages(updatedHistory);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    sendMessage(input);
  };

  return (
    <div className="page">
      <div className="hero-glow" />
      <div className="chat-shell">
        <header>
          <div>
            <p className="badge">Experimental</p>
            <h1>Alon's Chat Bot</h1>
            <p className="subtitle">
              Day-to-day assistant powered by OpenAI — grounded, personal, and fast.
            </p>
          </div>
        </header>

        <main>
          {messages.length === 0 && (
            <div className="empty-state">
              <p>Say hi and I'll keep the conversation rolling.</p>
            </div>
          )}
          {messages.map((msg) => (
            <ChatBubble key={`${msg.timestamp}-${msg.sender}`} {...msg} />
          ))}
          <span ref={endOfChatRef} />
        </main>

        {error && <div className="error-banner">{error}</div>}

        <SuggestionChips
          suggestions={suggestions}
          onSelect={sendMessage}
          disabled={loading}
        />

        <form onSubmit={handleSubmit} className="composer">
          <input
            type="text"
            placeholder="Ask me anything…"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            disabled={loading}
          />
          <button type="submit" disabled={loading}>
            {loading ? "Thinking…" : "Send"}
          </button>
        </form>
      </div>
    </div>
  );
}

export default App;
