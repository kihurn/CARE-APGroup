-- ==========================================
-- 1. DROP TABLES (DISABLED - Keep your data!)
-- ==========================================
-- DROP TABLE IF EXISTS tickets;
-- DROP TABLE IF EXISTS messages;
-- DROP TABLE IF EXISTS chat_sessions;
-- DROP TABLE IF EXISTS knowledge_base;
-- DROP TABLE IF EXISTS products;
-- DROP TABLE IF EXISTS users;

-- ==========================================
-- 2. CREATE TABLES
-- ==========================================

-- USERS: Handles Customers, Admins, and Agents
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    name TEXT NOT NULL,
    role TEXT CHECK(role IN ('USER', 'ADMIN', 'AGENT')) NOT NULL,
    license_key TEXT,
    is_2fa_enabled INTEGER DEFAULT 0, -- 0 = False, 1 = True
    preferred_language TEXT DEFAULT 'English',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- PRODUCTS: The items the chatbot supports
CREATE TABLE IF NOT EXISTS products (
    product_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    model_version TEXT NOT NULL,
    category TEXT NOT NULL -- e.g., 'Router', 'Laptop', 'Printer'
);

-- KNOWLEDGE BASE: Manuals and FAQ data
CREATE TABLE IF NOT EXISTS knowledge_base (
    kb_id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER,
    title TEXT NOT NULL,
    content TEXT NOT NULL, -- The text the AI will read
    file_path TEXT, -- Path to the PDF file if uploaded
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- CHAT SESSIONS: Grouping messages together
CREATE TABLE IF NOT EXISTS chat_sessions (
    session_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    product_id INTEGER,
    assigned_agent_id INTEGER,
    status TEXT CHECK(status IN ('ACTIVE', 'CLOSED', 'ESCALATED')) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    closed_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (assigned_agent_id) REFERENCES users(user_id)
);

-- MESSAGES: Individual chat bubbles
CREATE TABLE IF NOT EXISTS messages (
    message_id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER NOT NULL,
    sender_type TEXT CHECK(sender_type IN ('USER', 'BOT', 'AGENT', 'SYSTEM')) NOT NULL,
    content TEXT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id) ON DELETE CASCADE
);

-- TICKETS: For Human Agent Support
CREATE TABLE IF NOT EXISTS tickets (
    ticket_id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER UNIQUE NOT NULL, -- One ticket per session
    assigned_agent_id INTEGER,
    priority TEXT CHECK(priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')) DEFAULT 'MEDIUM',
    status TEXT CHECK(status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')) DEFAULT 'OPEN',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    resolved_at DATETIME,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id),
    FOREIGN KEY (assigned_agent_id) REFERENCES users(user_id)
);

-- ==========================================
-- 3. INSERT MOCK DATA (Only if tables are empty)
-- ==========================================

-- A. Users (Password is 'password' for all - in real app, hash this!)
INSERT OR IGNORE INTO users (email, password_hash, name, role, is_2fa_enabled) VALUES 
('admin@care.com', 'password', 'System Admin', 'ADMIN', 1),
-- Removed agent@gmail.com - use agent@care.com instead
('user@gmail.com', 'password', 'John Doe', 'USER', 0),
('jane@gmail.com', 'password', 'Jane Smith', 'USER', 1);

-- B. Products
INSERT OR IGNORE INTO products (product_id, name, model_version, category) VALUES 
(1, 'UltraFast Router X1', 'v1.0', 'Router'),
(2, 'Gaming Laptop Pro', 'GL-2024', 'Laptop'),
(3, 'Smart Home Hub', 'SH-500', 'Smart Device');

-- C. Knowledge Base (So the AI has something to "know")
INSERT OR IGNORE INTO knowledge_base (kb_id, product_id, title, content) VALUES 
(1, 1, 'Reset Instructions', 'To reset the UltraFast Router, hold the small button on the back for 10 seconds until lights flash.'),
(2, 1, 'Default Password', 'The default admin password is "admin123".'),
(3, 2, 'Overheating Fix', 'Ensure air vents are not blocked. Use the Fan Boost mode in settings.');

-- D. Chat History (To test the User History Page)
-- Session 1: John Doe asks about Router (Closed)
INSERT OR IGNORE INTO chat_sessions (session_id, user_id, product_id, status, created_at) VALUES (1, 3, 1, 'CLOSED', '2023-10-01 10:00:00');
INSERT OR IGNORE INTO messages (message_id, session_id, sender_type, content, timestamp) VALUES 
(1, 1, 'USER', 'My internet is slow.', '2023-10-01 10:00:05'),
(2, 1, 'BOT', 'Have you tried restarting the router?', '2023-10-01 10:00:06'),
(3, 1, 'USER', 'Yes, it worked. Thanks.', '2023-10-01 10:05:00');

-- Session 2: John Doe asks about Laptop (Escalated to Agent)
INSERT OR IGNORE INTO chat_sessions (session_id, user_id, product_id, status, created_at) VALUES (2, 3, 2, 'ESCALATED', '2023-10-02 14:00:00');
INSERT OR IGNORE INTO messages (message_id, session_id, sender_type, content, timestamp) VALUES 
(4, 2, 'USER', 'My laptop screen is blue.', '2023-10-02 14:00:05'),
(5, 2, 'BOT', 'I cannot diagnose hardware failure. Escalating...', '2023-10-02 14:00:10');

-- E. Tickets (Connect Session 2 to Agent Steve)
INSERT OR IGNORE INTO tickets (ticket_id, session_id, assigned_agent_id, priority, status) VALUES 
(1, 2, 2, 'HIGH', 'OPEN');
