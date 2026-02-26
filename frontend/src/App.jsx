import { useEffect, useMemo, useState } from "react";
import "./App.css";

const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

function ChatBubble({ sender, text, timestamp }) {
  const isUser = sender === "user";
  const formattedTime = new Date(timestamp).toLocaleTimeString("he-IL", {
    hour: "2-digit",
    minute: "2-digit",
  });
  return (
    <div className={`bubble ${isUser ? "bubble-user" : "bubble-bot"}`}>
      <div className="bubble-header">
        <span>{isUser ? "אתה" : "אלון'ס בוט"}</span>
        <small>{formattedTime}</small>
      </div>
      <p>{text}</p>
    </div>
  );
}

function SuggestionChips({ suggestions, onSelect }) {
  const memoSuggestions = useMemo(() => suggestions ?? [], [suggestions]);
  if (!memoSuggestions.length) return null;
  return (
    <div className="chips">
      {memoSuggestions.map((s, index) => (
        <button key={`${s}-${index}`} onClick={() => onSelect(s)}>
          {s}
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

  useEffect(() => {
    fetch(`${API_BASE}/api/messages`)
      .then((res) => res.json())
      .then((data) => setMessages(data))
      .catch(() => setMessages([]));
  }, []);

  const sendMessage = async (text) => {
    if (!text.trim()) return;
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
        throw new Error("שגיאה בשליחה לשרת");
      }
      const data = await response.json();
      const refreshed = await fetch(`${API_BASE}/api/messages`).then((res) =>
        res.json()
      );
      setMessages(refreshed);
      setSuggestions(data.suggestions ?? []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    sendMessage(input);
  };

  return (
    <div className="app-container">
      <header>
        <h1>Alon's Chat Bot</h1>
        <p>בוט יומיומי שנותן בראש 🙂</p>
      </header>

      <main>
        {messages.length === 0 && (
          <div className="empty-state">
            <p>עדיין אין שיחה. תגיד שלום ונתחיל.</p>
          </div>
        )}
        {messages.map((msg) => (
          <ChatBubble key={`${msg.timestamp}-${msg.sender}`} {...msg} />
        ))}
      </main>

      {error && <div className="error">{error}</div>}

      <SuggestionChips suggestions={suggestions} onSelect={sendMessage} />

      <form onSubmit={handleSubmit} className="composer">
        <input
          type="text"
          placeholder="מה תרצה לשאול היום?"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          disabled={loading}
        />
        <button type="submit" disabled={loading}>
          {loading ? "שולח..." : "שלח"}
        </button>
      </form>
    </div>
  );
}

export default App;
