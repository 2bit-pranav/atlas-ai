"use client";

import { useMemo, useRef, useState } from "react";
import { useAtlasWebSocket } from "@/hooks/useAtlasWebSocket";
import styles from "./page.module.css";

type SidebarView =
  | "new-chat"
  | "chat-history"
  | "browser-sessions"
  | "saved-memory"
  | "integrations";

type ChatHistoryItem = {
  id: string;
  name: string;
  updatedAt: string;
};

type BrowserProfileItem = {
  id: string;
  name: string;
  storedData: string;
};

const INITIAL_CHAT_HISTORY: ChatHistoryItem[] = [
  { id: "chat-1", name: "Plan navigation stack", updatedAt: "Apr 20" },
  { id: "chat-2", name: "Fix captcha fallback policy", updatedAt: "Apr 19" },
  { id: "chat-3", name: "Review memory retrieval quality", updatedAt: "Apr 18" },
  { id: "chat-4", name: "Scrape finance dashboard tasks", updatedAt: "Apr 17" },
  { id: "chat-5", name: "Design tab orchestration flow", updatedAt: "Apr 16" },
];

const INITIAL_BROWSER_PROFILES: BrowserProfileItem[] = [
  {
    id: "profile-1",
    name: "Investor research profile",
    storedData: "Saved logins, cookies, and preferences",
  },
  {
    id: "profile-2",
    name: "Documentation profile",
    storedData: "Stored auth tokens and browsing state",
  },
  {
    id: "profile-3",
    name: "Lead sourcing profile",
    storedData: "Saved cookies and workspace context",
  },
  {
    id: "profile-4",
    name: "Pricing monitor profile",
    storedData: "Stored credentials and session cache",
  },
];

const PROMPT_PHRASES = [
  "What's on your mind today?",
  "What should Atlas tackle first?",
  "Where do you want to start?",
  "What are we working on today?",
  "Tell me the task you'd like to begin.",
  "What should I help you explore?",
  "Drop in a goal and Atlas will get moving.",
  "What's the mission for this session?",
  "What would you like to automate next?",
  "Start with a task, a link, or a question.",
];

function pickRandomPrompt() {
  return PROMPT_PHRASES[Math.floor(Math.random() * PROMPT_PHRASES.length)];
}

function TrashIcon() {
  return (
    <svg
      width="14"
      height="14"
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <path
        d="M3 6H21M9 6V4C9 3.45 9.45 3 10 3H14C14.55 3 15 3.45 15 4V6M18 6V20C18 20.55 17.55 21 17 21H7C6.45 21 6 20.55 6 20V6M10 11V17M14 11V17"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

export default function Home() {
  const { status, messages, terminalLogs, sendMessage: sendWebSocketMessage } = useAtlasWebSocket();
  const [activeView, setActiveView] = useState<SidebarView>("new-chat");
  const [promptPhrase, setPromptPhrase] = useState(PROMPT_PHRASES[0]);

  const [chatHistory, setChatHistory] =
    useState<ChatHistoryItem[]>(INITIAL_CHAT_HISTORY);
  const [chatSearch, setChatSearch] = useState("");
  const [selectedChatIds, setSelectedChatIds] = useState<string[]>([]);

  const [browserProfiles, setBrowserProfiles] = useState<BrowserProfileItem[]>(
    INITIAL_BROWSER_PROFILES,
  );
  const [profileSearch, setProfileSearch] = useState("");
  const [selectedProfileIds, setSelectedProfileIds] = useState<string[]>([]);

  const [messageText, setMessageText] = useState("");
  const [composerProfileId, setComposerProfileId] = useState("");
  const [composerLocked, setComposerLocked] = useState(false);
  const [attachedFiles, setAttachedFiles] = useState<string[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const filteredChatHistory = useMemo(() => {
    const query = chatSearch.trim().toLowerCase();
    if (!query) {
      return chatHistory;
    }

    return chatHistory.filter((item) => item.name.toLowerCase().includes(query));
  }, [chatHistory, chatSearch]);

  const filteredBrowserProfiles = useMemo(() => {
    const query = profileSearch.trim().toLowerCase();
    if (!query) {
      return browserProfiles;
    }

    return browserProfiles.filter((item) => item.name.toLowerCase().includes(query));
  }, [browserProfiles, profileSearch]);

  const toggleSelectedChat = (id: string) => {
    setSelectedChatIds((current) =>
      current.includes(id)
        ? current.filter((itemId) => itemId !== id)
        : [...current, id],
    );
  };

  const toggleSelectedProfile = (id: string) => {
    setSelectedProfileIds((current) =>
      current.includes(id)
        ? current.filter((itemId) => itemId !== id)
        : [...current, id],
    );
  };

  const removeChatItem = (id: string) => {
    setChatHistory((current) => current.filter((item) => item.id !== id));
    setSelectedChatIds((current) => current.filter((itemId) => itemId !== id));
  };

  const removeProfileItem = (id: string) => {
    setBrowserProfiles((current) => current.filter((item) => item.id !== id));
    setSelectedProfileIds((current) => current.filter((itemId) => itemId !== id));
  };

  const deleteSelectedChat = () => {
    if (!selectedChatIds.length) {
      return;
    }

    const selectedSet = new Set(selectedChatIds);
    setChatHistory((current) => current.filter((item) => !selectedSet.has(item.id)));
    setSelectedChatIds([]);
  };

  const deleteSelectedProfiles = () => {
    if (!selectedProfileIds.length) {
      return;
    }

    const selectedSet = new Set(selectedProfileIds);
    setBrowserProfiles((current) =>
      current.filter((item) => !selectedSet.has(item.id)),
    );
    setSelectedProfileIds([]);
  };

  const createProfile = () => {
    const nextIndex = browserProfiles.length + 1;
    const newProfile: BrowserProfileItem = {
      id: `profile-${Date.now()}`,
      name: `New browser profile ${nextIndex}`,
      storedData: "Stored browser data",
    };

    setBrowserProfiles((current) => [newProfile, ...current]);
  };

  const allVisibleChatsSelected =
    filteredChatHistory.length > 0 &&
    filteredChatHistory.every((item) => selectedChatIds.includes(item.id));

  const allVisibleProfilesSelected =
    filteredBrowserProfiles.length > 0 &&
    filteredBrowserProfiles.every((item) => selectedProfileIds.includes(item.id));

  const toggleAllVisibleChats = () => {
    if (allVisibleChatsSelected) {
      const visibleIds = new Set(filteredChatHistory.map((item) => item.id));
      setSelectedChatIds((current) =>
        current.filter((itemId) => !visibleIds.has(itemId)),
      );
      return;
    }

    const merged = new Set(selectedChatIds);
    filteredChatHistory.forEach((item) => merged.add(item.id));
    setSelectedChatIds(Array.from(merged));
  };

  const toggleAllVisibleProfiles = () => {
    if (allVisibleProfilesSelected) {
      const visibleIds = new Set(filteredBrowserProfiles.map((item) => item.id));
      setSelectedProfileIds((current) =>
        current.filter((itemId) => !visibleIds.has(itemId)),
      );
      return;
    }

    const merged = new Set(selectedProfileIds);
    filteredBrowserProfiles.forEach((item) => merged.add(item.id));
    setSelectedProfileIds(Array.from(merged));
  };

  const startNewChat = () => {
    setActiveView("new-chat");
    setPromptPhrase(pickRandomPrompt());
    setMessageText("");
    setComposerProfileId("");
    setComposerLocked(false);
    setAttachedFiles([]);
  };

  const handleUploadClick = () => {
    fileInputRef.current?.click();
  };

  const handleFilesSelected = (event: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(event.target.files ?? []);
    if (!files.length) {
      return;
    }

    setAttachedFiles((current) => [
      ...current,
      ...files.map((file) => file.name),
    ]);
    event.target.value = "";
  };

  const sendMessage = () => {
    if (!messageText.trim() && !attachedFiles.length) {
      return;
    }
    setComposerLocked(true);
    const userContent = messageText.trim() || `Attached ${attachedFiles.length} file(s)`;
    
    sendWebSocketMessage(userContent);
    
    setMessageText("");
    setAttachedFiles([]);
  };

  return (
    <div className={styles.shell}>
      <aside className={styles.sidebar}>
        <div className={styles.sidebarTop}>
          <p className={styles.brand}>Atlas</p>
          <p className={styles.brandSub}>Autonomous Browser Agent</p>
        </div>

        <button
          type="button"
          className={`${styles.newChatButton} ${
            activeView === "new-chat" ? styles.newChatButtonActive : ""
          }`}
          onClick={startNewChat}
        >
          New Chat
        </button>

        <nav className={styles.navList}>
          <button
            type="button"
            className={`${styles.navItem} ${
              activeView === "chat-history" ? styles.navItemActive : ""
            }`}
            onClick={() => setActiveView("chat-history")}
          >
            Chat History
          </button>
          <button
            type="button"
            className={`${styles.navItem} ${
              activeView === "browser-sessions" ? styles.navItemActive : ""
            }`}
            onClick={() => setActiveView("browser-sessions")}
          >
            Browser Profiles
          </button>
          <button
            type="button"
            className={`${styles.navItem} ${
              activeView === "saved-memory" ? styles.navItemActive : ""
            }`}
            onClick={() => setActiveView("saved-memory")}
          >
            Saved Documents
          </button>
          <button
            type="button"
            className={`${styles.navItem} ${
              activeView === "integrations" ? styles.navItemActive : ""
            }`}
            onClick={() => setActiveView("integrations")}
          >
            Integrations
          </button>
        </nav>
      </aside>

      <section className={styles.centerPanel}>
        <div className={styles.statusBar}>
          <span
            className={`${styles.connectionBadge} ${
              status === "CONNECTED" ? styles.connected : styles.notConnected
            }`}
          >
            {status === "CONNECTED" ? "Connected" : "Not Connected"}
          </span>
        </div>

        {activeView === "new-chat" && (
          <div className={styles.newChatStage}>
            <div
              className={`${styles.newChatContent} ${
                messages.length === 0 ? styles.newChatContentCentered : ""
              }`}
            >
              {messages.length === 0 ? (
                <p className={styles.promptPhrase}>{promptPhrase}</p>
              ) : (
                <div className={styles.chatStream}>
                  {messages.map((message, index) => (
                    <div
                      key={index}
                      className={`${styles.messageBubble} ${
                        message.role === "user" ? styles.userBubble : styles.agentBubble
                      }`}
                    >
                      {message.content}
                    </div>
                  ))}
                </div>
              )}
            </div>

            <form 
              className={styles.chatComposer}
              onSubmit={(e) => {
                e.preventDefault();
                sendMessage();
              }}
            >
              <input
                ref={fileInputRef}
                type="file"
                className={styles.hiddenFileInput}
                accept="image/png,image/jpeg,image/jpg,image/webp,image/gif,application/pdf,.png,.jpg,.jpeg,.webp,.gif,.pdf"
                multiple
                onChange={handleFilesSelected}
              />
              <input
                type="text"
                className={styles.composerInput}
                placeholder="Type a message"
                value={messageText}
                onChange={(event) => setMessageText(event.target.value)}
                disabled={status !== "CONNECTED"}
              />
              <div className={styles.composerActions}>
                <button
                  type="button"
                  className={styles.attachButton}
                  onClick={handleUploadClick}
                  aria-label="Upload images or PDF files"
                >
                  +
                </button>
                <select
                  className={styles.profileSelectInline}
                  value={composerProfileId}
                  onChange={(event) => setComposerProfileId(event.target.value)}
                  disabled={composerLocked}
                  aria-label="Select browser profile"
                >
                  <option value="">Profile</option>
                  {browserProfiles.map((profile) => (
                    <option key={profile.id} value={profile.id}>
                      {profile.name}
                    </option>
                  ))}
                </select>
              </div>
              <button
                type="submit"
                className={styles.sendButton}
                disabled={status !== "CONNECTED"}
              >
                Send
              </button>
            </form>
            {terminalLogs.length > 0 && (
              <div className="bg-black text-green-400 font-mono text-xs overflow-y-auto h-32 p-2 mt-4 rounded">
                {terminalLogs.map((log, i) => (
                  <div key={i}>{log}</div>
                ))}
              </div>
            )}
          </div>
        )}

        {activeView === "chat-history" && (
          <div className={styles.workspacePanel}>
            <div className={styles.panelHeader}>
              <h1 className={styles.panelTitle}>Chat History</h1>
              <p className={styles.panelSubtitle}>
                Search, multi-select, and delete previous conversations.
              </p>
            </div>

            <div className={styles.tableCard}>
              <div className={styles.tableControls}>
                <input
                  className={styles.searchInput}
                  type="search"
                  placeholder="Search chat history"
                  value={chatSearch}
                  onChange={(event) => setChatSearch(event.target.value)}
                />
                <button
                  type="button"
                  className={styles.ghostButton}
                  onClick={deleteSelectedChat}
                  disabled={!selectedChatIds.length}
                >
                  Delete Selected ({selectedChatIds.length})
                </button>
              </div>

              <div className={styles.tableWrap}>
                <div className={`${styles.tableRow} ${styles.tableHead}`}>
                  <label className={styles.checkboxCell}>
                    <input
                      type="checkbox"
                      checked={allVisibleChatsSelected}
                      onChange={toggleAllVisibleChats}
                      aria-label="Select all chat history rows"
                    />
                  </label>
                  <p>Name</p>
                  <p>Updated</p>
                  <p className={styles.tableRight}>Actions</p>
                </div>

                {filteredChatHistory.map((item) => (
                  <div className={styles.tableRow} key={item.id}>
                    <label className={styles.checkboxCell}>
                      <input
                        type="checkbox"
                        checked={selectedChatIds.includes(item.id)}
                        onChange={() => toggleSelectedChat(item.id)}
                        aria-label={`Select ${item.name}`}
                      />
                    </label>
                    <p className={styles.cellPrimary}>{item.name}</p>
                    <p className={styles.cellMuted}>{item.updatedAt}</p>
                    <div className={styles.rowActions}>
                      <button
                        type="button"
                        className={styles.iconButton}
                        aria-label={`Delete ${item.name}`}
                        onClick={() => removeChatItem(item.id)}
                      >
                        <TrashIcon />
                      </button>
                    </div>
                  </div>
                ))}

                {!filteredChatHistory.length && (
                  <div className={styles.emptyState}>No chat history results found.</div>
                )}
              </div>
            </div>
          </div>
        )}

        {activeView === "browser-sessions" && (
          <div className={styles.workspacePanel}>
            <div className={styles.panelHeader}>
              <h1 className={styles.panelTitle}>Browser Profiles</h1>
              <p className={styles.panelSubtitle}>
                Create, inspect, and prune stored browser profiles.
              </p>
            </div>

            <div className={styles.tableCard}>
              <div className={styles.tableControls}>
                <input
                  className={styles.searchInput}
                  type="search"
                  placeholder="Search browser profiles"
                  value={profileSearch}
                  onChange={(event) => setProfileSearch(event.target.value)}
                />
                <button
                  type="button"
                  className={styles.createButton}
                  onClick={createProfile}
                >
                  Create Profile
                </button>
                <button
                  type="button"
                  className={styles.ghostButton}
                  onClick={deleteSelectedProfiles}
                  disabled={!selectedProfileIds.length}
                >
                  Delete Selected ({selectedProfileIds.length})
                </button>
              </div>

              <div className={styles.tableWrap}>
                <div className={`${styles.tableRow} ${styles.tableHead}`}>
                  <label className={styles.checkboxCell}>
                    <input
                      type="checkbox"
                      checked={allVisibleProfilesSelected}
                      onChange={toggleAllVisibleProfiles}
                      aria-label="Select all browser profile rows"
                    />
                  </label>
                  <p>Name</p>
                  <p>Stored Data</p>
                  <p className={styles.tableRight}>Actions</p>
                </div>

                {filteredBrowserProfiles.map((item) => (
                  <div className={styles.tableRowProfiles} key={item.id}>
                    <label className={styles.checkboxCell}>
                      <input
                        type="checkbox"
                        checked={selectedProfileIds.includes(item.id)}
                        onChange={() => toggleSelectedProfile(item.id)}
                        aria-label={`Select ${item.name}`}
                      />
                    </label>
                    <p className={styles.cellPrimary}>{item.name}</p>
                    <p className={styles.cellMuted}>{item.storedData}</p>
                    <div className={styles.rowActions}>
                      <button
                        type="button"
                        className={styles.iconButton}
                        aria-label={`Delete ${item.name}`}
                        onClick={() => removeProfileItem(item.id)}
                      >
                        <TrashIcon />
                      </button>
                    </div>
                  </div>
                ))}

                {!filteredBrowserProfiles.length && (
                  <div className={styles.emptyState}>No browser profiles found.</div>
                )}
              </div>
            </div>
          </div>
        )}

        {(activeView === "saved-memory" || activeView === "integrations") && (
          <div className={styles.workspacePanel}>
            <div className={styles.blankCard}>
              <p className={styles.blankTitle}>
                {activeView === "saved-memory" ? "Saved Documents" : "Integrations"}
              </p>
              <p className={styles.blankHint} />
            </div>
          </div>
        )}
      </section>

      <aside className={styles.rightPanel}>
        <p className={styles.terminalTitle}>Terminal</p>
      </aside>
    </div>
  );
}
