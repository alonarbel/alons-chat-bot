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
  const [chats, setChats] = useState([]);
  const [activeChatId, setActiveChatId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [suggestions, setSuggestions] = useState([]);
  const [error, setError] = useState(null);
  const endOfChatRef = useRef(null);

  useEffect(() => {
    const boot = async () => {
      try {
        const response = await fetch(`${API_BASE}/api/chats`);
        const data = await response.json();
        setChats(data);
        if (data.length && !activeChatId) {
          setActiveChatId(data[0].id);
        }
      } catch (err) {
        console.error(err);
      }
    };
    boot();
  }, []);

  useEffect(() => {
    if (!activeChatId) return;
    const load = async () => {
      setLoadingHistory(true);
      try {
        const res = await fetch(`${API_BASE}/api/chats/${activeChatId}/messages`);
        if (res.ok) {
          const history = await res.json();
          setMessages(history);
          setTimeout(() => endOfChatRef.current?.scrollIntoView({ behavior: "smooth" }), 50);
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoadingHistory(false);
      }
    };
    load();
  }, [activeChatId]);

  const refreshChats = async () => {
    const res = await fetch(`${API_BASE}/api/chats`);
    const list = await res.json();
    setChats(list);
    return list;
  };

  const handleNewChat = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/chats`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title: "Quick chat" }),
      });
      const created = await res.json();
      const list = await refreshChats();
      setActiveChatId(created.id ?? list[0]?.id ?? null);
      setMessages([]);
      setSuggestions([]);
      setInput("");
    } catch (err) {
      console.error(err);
    }
  };

  const sendMessage = async (text) => {
    if (!text.trim() || loading || !activeChatId) return;
    setLoading(true);
    setError(null);
    setInput("");

    try {
      const response = await fetch(`${API_BASE}/api/chats/${activeChatId}/messages`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message: text }),
      });

      if (!response.ok) {
        throw new Error("Server refused to answer. Try again.");
      }

      const data = await response.json();
      setSuggestions(data.suggestions ?? []);
      const history = await fetch(`${API_BASE}/api/chats/${activeChatId}/messages`).then((res) =>
        res.json()
      );
      setMessages(history);
      await refreshChats();
      setTimeout(() => endOfChatRef.current?.scrollIntoView({ behavior: "smooth" }), 50);
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

  const activeChatTitle = chats.find((chat) => chat.id === activeChatId)?.title ?? "";

  return (
    <div className="page">
      <div className="hero-glow" />
      <div className="chat-shell">
        <header className="app-header">
          <div>
            <p className="badge">Pinned</p>
            <h1>Alon's Chat Bot</h1>
            <p className="subtitle">
              Ask anything – logistics, travel, reminders, or spur-of-the-moment ideas.
            </p>
          </div>
          <div className="header-actions">
            <button className="sidebar__action" onClick={handleNewChat}>
              + New chat
            </button>
          </div>
        </header>

        <div className="main-area">
          <aside className="sidebar">
            <div className="sidebar__section">
              <p className="sidebar__label">History</p>
              <div className="sidebar__list">
                {chats.map((chat) => (
                  <button
                    key={chat.id}
                    className={`sidebar__chat ${chat.id === activeChatId ? "is-active" : ""}`}
                    onClick={() => setActiveChatId(chat.id)}
                  >
                    <span>{chat.title}</span>
                    <small>
                      {new Date(chat.updatedAt).toLocaleTimeString([], {
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </small>
                  </button>
                ))}
                {!chats.length && <p className="sidebar__empty">No chats yet</p>}
              </div>
            </div>
            <div className="sidebar__section quick-links">
              <p className="sidebar__label">Shortcuts</p>
              <button className="sidebar__ghost">Reminders</button>
              <button className="sidebar__ghost">Trips</button>
              <button className="sidebar__ghost">Household</button>
            </div>
          </aside>

          <section className="conversation">
            <div className="conversation__title">
              <h2>{activeChatTitle || "Pick a chat"}</h2>
              <p>Single focused conversation. Scroll inside the chat area.</p>
            </div>

            <div className="conversation__body">
              {loadingHistory && <p className="loading">Loading chat…</p>}
              {!loadingHistory && messages.length === 0 && (
                <div className="empty-state">
                  <p>Say hi and I'll keep the conversation rolling.</p>
                </div>
              )}
              {messages.map((msg) => (
                <ChatBubble key={`${msg.timestamp}-${msg.sender}-${msg.text}`} {...msg} />
              ))}
              <span ref={endOfChatRef} />
            </div>

            {error && <div className="error-banner">{error}</div>}

            <SuggestionChips
              suggestions={suggestions}
              onSelect={sendMessage}
              disabled={loading || !activeChatId}
            />

            <form onSubmit={handleSubmit} className="composer">
              <input
                type="text"
                placeholder="Ask me anything…"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                disabled={loading || !activeChatId}
              />
              <button type="submit" disabled={loading || !activeChatId}>
                {loading ? "Thinking…" : "Send"}
              </button>
            </form>
          </section>
        </div>
      </div>
    </div>
  );
}

export default App;
