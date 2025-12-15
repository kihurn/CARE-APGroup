# 📊 Database Migration Guide

## 🔄 **Updating Your Existing Database**

Since we've updated the database schema, you have **two options**:

---

## **Option 1: Fresh Start (Recommended for Development)** ✅

### **Steps:**
1. **Backup existing data** (if you need it)
   ```bash
   # Copy your current database
   copy care.db care_backup.db
   ```

2. **Delete the old database**
   ```bash
   # Windows
   del care.db
   
   # Mac/Linux
   rm care.db
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```

4. **Done!** ✅
   - New database will be created with updated schema
   - Mock data will be inserted
   - All features will work immediately

### **Result:**
- ✅ Clean database with new schema
- ✅ Default users: admin@care.com, agent@gmail.com, user@gmail.com
- ✅ Sample products and chat history
- ✅ All passwords will be BCrypt hashed

---

## **Option 2: Migrate Existing Database (Keep Your Data)** 🔧

### **Steps:**

#### **1. Add Missing Fields to Tables**

Open a database tool (DB Browser for SQLite, DBeaver, etc.) or use SQLite command line:

```sql
-- Add fields to chat_sessions
ALTER TABLE chat_sessions ADD COLUMN assigned_agent_id INTEGER;
ALTER TABLE chat_sessions ADD COLUMN updated_at DATETIME;
ALTER TABLE chat_sessions ADD COLUMN closed_at DATETIME;

-- Add fields to tickets
ALTER TABLE tickets ADD COLUMN updated_at DATETIME;
ALTER TABLE tickets ADD COLUMN resolved_at DATETIME;
```

#### **2. Update Constraints (Requires Recreation)**

SQLite doesn't support modifying CHECK constraints, so we need to recreate tables:

**Create backup tables:**
```sql
-- Backup messages
CREATE TABLE messages_backup AS SELECT * FROM messages;

-- Backup tickets
CREATE TABLE tickets_backup AS SELECT * FROM tickets;
```

**Drop and recreate with new constraints:**
```sql
-- Drop old tables
DROP TABLE messages;
DROP TABLE tickets;

-- Recreate messages with SYSTEM support
CREATE TABLE messages (
    message_id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER NOT NULL,
    sender_type TEXT CHECK(sender_type IN ('USER', 'BOT', 'AGENT', 'SYSTEM')) NOT NULL,
    content TEXT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id) ON DELETE CASCADE
);

-- Recreate tickets with all statuses and priorities
CREATE TABLE tickets (
    ticket_id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER UNIQUE NOT NULL,
    assigned_agent_id INTEGER,
    priority TEXT CHECK(priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')) DEFAULT 'MEDIUM',
    status TEXT CHECK(status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')) DEFAULT 'OPEN',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    resolved_at DATETIME,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id),
    FOREIGN KEY (assigned_agent_id) REFERENCES users(user_id)
);
```

**Restore data:**
```sql
-- Restore messages
INSERT INTO messages SELECT * FROM messages_backup;

-- Restore tickets
INSERT INTO tickets (ticket_id, session_id, assigned_agent_id, priority, status, created_at)
SELECT ticket_id, session_id, assigned_agent_id, priority, status, created_at
FROM tickets_backup;

-- Clean up backup tables
DROP TABLE messages_backup;
DROP TABLE tickets_backup;
```

#### **3. Hash Existing Passwords (Optional but Recommended)**

If you want to keep existing users and convert their passwords:

```sql
-- For now, passwords will work as-is (backward compatibility)
-- On first login, they'll be automatically upgraded to BCrypt
-- No manual intervention needed!
```

The application will automatically upgrade plain text passwords to BCrypt hashes when users log in.

---

## 🎯 **Recommended Approach**

### **For Development/Testing:**
→ **Option 1: Fresh Start**
- Quickest and cleanest
- No risk of migration errors
- Gets you up and running in 30 seconds

### **For Production (with real user data):**
→ **Option 2: Migrate**
- Preserves all existing data
- Keeps user accounts
- Maintains chat history

---

## 🧪 **Verify Migration Success**

After migration, test these features:

### **1. Test SYSTEM Messages:**
```
1. Login as user
2. Escalate a chat
3. Check if "Requesting Live Support Agent..." message appears
✅ Should work without errors
```

### **2. Test Ticket Statuses:**
```
1. Login as agent
2. Mark ticket as "In Progress"
3. Mark ticket as "Resolved"
✅ Should work without constraint violations
```

### **3. Test CRITICAL Priority:**
```
1. Use keywords like "urgent", "critical", "emergency" in chat
2. Escalate
3. Check if ticket has CRITICAL priority
✅ Should work without errors
```

### **4. Test Password Security:**
```
1. Register new user
2. Check database: password should be $2a$10$... (BCrypt hash)
3. Login with password
✅ Should authenticate successfully
```

### **5. Test Agent Assignment:**
```
1. Escalate chat
2. Check database: tickets.assigned_agent_id should be 2 (Steve)
3. Login as Steve: should see ticket in dashboard
✅ Should show assigned ticket
```

---

## 📝 **SQL Script for Complete Migration**

Here's the complete migration script:

```sql
-- ==========================================
-- CARE Database Migration Script
-- Adds new fields and updates constraints
-- ==========================================

BEGIN TRANSACTION;

-- 1. Add missing fields to chat_sessions
ALTER TABLE chat_sessions ADD COLUMN assigned_agent_id INTEGER;
ALTER TABLE chat_sessions ADD COLUMN updated_at DATETIME;
ALTER TABLE chat_sessions ADD COLUMN closed_at DATETIME;

-- 2. Backup existing data
CREATE TABLE messages_backup AS SELECT * FROM messages;
CREATE TABLE tickets_backup AS SELECT * FROM tickets;

-- 3. Drop old tables
DROP TABLE messages;
DROP TABLE tickets;

-- 4. Recreate tables with new constraints
CREATE TABLE messages (
    message_id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER NOT NULL,
    sender_type TEXT CHECK(sender_type IN ('USER', 'BOT', 'AGENT', 'SYSTEM')) NOT NULL,
    content TEXT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id) ON DELETE CASCADE
);

CREATE TABLE tickets (
    ticket_id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER UNIQUE NOT NULL,
    assigned_agent_id INTEGER,
    priority TEXT CHECK(priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')) DEFAULT 'MEDIUM',
    status TEXT CHECK(status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')) DEFAULT 'OPEN',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    resolved_at DATETIME,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id),
    FOREIGN KEY (assigned_agent_id) REFERENCES users(user_id)
);

-- 5. Restore data
INSERT INTO messages SELECT * FROM messages_backup;

INSERT INTO tickets (ticket_id, session_id, assigned_agent_id, priority, status, created_at)
SELECT ticket_id, session_id, assigned_agent_id, priority, status, created_at
FROM tickets_backup;

-- 6. Clean up
DROP TABLE messages_backup;
DROP TABLE tickets_backup;

COMMIT;

-- Done! 🎉
```

**To run this script:**
1. Save as `migrate.sql`
2. Open DB Browser for SQLite
3. File → Open Database → care.db
4. Execute SQL → Paste script → Run
5. Done!

---

## ⚠️ **Troubleshooting**

### **Error: "constraint failed"**
→ Your old data has invalid values  
**Solution**: Use Option 1 (Fresh Start) or manually clean data

### **Error: "table already exists"**
→ Table already has new structure  
**Solution**: Skip that ALTER TABLE command

### **Passwords don't work after migration**
→ BCrypt verification enabled but passwords are plain text  
**Solution**: Users will be auto-upgraded on next login (backward compatible)

---

## ✅ **Migration Complete Checklist**

After migration, verify:
- [ ] Application starts without errors
- [ ] Can login with existing users
- [ ] Can register new users
- [ ] Escalation works without errors
- [ ] Agent can mark tickets as IN_PROGRESS
- [ ] CRITICAL priority tickets can be created
- [ ] System messages appear in chat

---

## 🎉 **You're Done!**

Your database is now up to date with all the latest fixes!

**Next**: Run `mvn javafx:run` and enjoy the fully functional CARE application! 🚀

