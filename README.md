# CARE - Customer Assistance and Resource Engine

A JavaFX-based customer support system with chat functionality, ticket management, and knowledge base.

## 🚀 Project Overview

CARE is a comprehensive customer support application built with JavaFX, implementing a clean MVC architecture with Service-Repository pattern. It supports multiple user roles (User, Agent, Admin) with role-based dashboards.

## 📋 Features

- **User Authentication**: Login and registration system
- **Role-Based Access**: Different dashboards for Users, Agents, and Admins
- **Chat Sessions**: Real-time chat support (skeleton implemented)
- **Ticket Management**: Escalation and assignment system
- **Knowledge Base**: Product documentation and support articles
- **SQLite Database**: Local database with complete schema

## 🛠️ Technology Stack

- **Language**: Java 17+
- **UI Framework**: JavaFX 21
- **Build Tool**: Maven
- **Database**: SQLite (JDBC)
- **Architecture**: MVC with Service-Repository pattern
- **Testing**: JUnit 5

## 📁 Project Structure

```
src/main/
├── java/com/care/
│   ├── App.java                    # Main entry point
│   ├── model/                      # POJOs matching database schema
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── ChatSession.java
│   │   ├── Message.java
│   │   ├── Ticket.java
│   │   └── KnowledgeBase.java
│   ├── dao/                        # Data Access Objects
│   │   └── UserDAO.java
│   ├── service/                    # Business logic layer
│   │   └── UserService.java
│   ├── controller/                 # JavaFX Controllers
│   │   ├── shared/                 # Login, Register
│   │   ├── user/                   # User dashboard
│   │   ├── admin/                  # Admin dashboard
│   │   └── agent/                  # Agent dashboard
│   └── util/                       # Utility classes
│       ├── DatabaseDriver.java     # Singleton DB connection
│       ├── SessionManager.java     # User session management
│       └── ViewFactory.java        # Navigation handler
├── resources/com/care/
│   ├── view/                       # FXML files
│   │   ├── shared/                 # Login.fxml, Register.fxml
│   │   ├── user/                   # UserDashboard.fxml
│   │   ├── admin/                  # AdminDashboard.fxml
│   │   └── agent/                  # AgentDashboard.fxml
│   ├── styles/                     # CSS stylesheets
│   │   └── main.css
│   └── sql/                        # Database schema
│       └── schema.sql
└── module-info.java                # Java module configuration
```

## 🏃 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- IDE with JavaFX support (IntelliJ IDEA, Eclipse, VS Code)

### Installation

1. **Clone/Navigate to the project directory**
   ```bash
   cd CAREv2
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```

### Default Admin Credentials

- **Email**: `admin@care.com`
- **Password**: `admin123`

## 📊 Database Schema

The application uses SQLite with the following tables:

1. **Users**: User accounts with roles (USER, ADMIN, AGENT)
2. **Products**: Product catalog
3. **ChatSessions**: Chat conversations between users and bots/agents
4. **Messages**: Individual messages within chat sessions
5. **Tickets**: Escalated support tickets
6. **KnowledgeBase**: Documentation and support articles

Database file: `care.db` (auto-created on first run)

## 🎨 Architecture Highlights

### Design Patterns

- **Singleton**: DatabaseDriver, SessionManager, ViewFactory
- **DAO Pattern**: Separates data access logic
- **Service Layer**: Business logic and validation
- **MVC**: Clean separation of concerns

### Key Components

1. **DatabaseDriver**: Manages SQLite connection, initializes schema
2. **ViewFactory**: Centralized navigation and FXML loading
3. **SessionManager**: Tracks logged-in user state
4. **Controllers**: Handle UI events and user interactions

## 🔧 Next Steps / TODO

The foundation is complete. Here are suggested next steps:

1. **Implement Chat Functionality**
   - Create ChatController and ChatView
   - Implement message sending/receiving
   - Add bot response logic

2. **Build Knowledge Base**
   - Create KnowledgeBaseDAO and Service
   - Implement search functionality
   - Add file upload for PDFs

3. **Ticket System**
   - Create TicketDAO and Service
   - Implement ticket assignment
   - Add status tracking

4. **Security Enhancements**
   - Implement password hashing (BCrypt)
   - Add 2FA support
   - Session timeout mechanism

5. **UI/UX Improvements**
   - Add loading indicators
   - Implement notifications
   - Enhance error handling

## 📝 Development Guidelines

### Adding a New Feature

1. Create Model class in `model/` package
2. Create DAO class in `dao/` package
3. Create Service class in `service/` package
4. Create Controller in `controller/` package
5. Create FXML view in `resources/com/care/view/`
6. Update `module-info.java` if needed

### Running Tests

```bash
mvn test
```

## 🐛 Common Issues

### Issue: JavaFX modules not found
**Solution**: Ensure Java 17+ is installed and JAVA_HOME is set correctly

### Issue: Database connection fails
**Solution**: Check file permissions in the project directory

### Issue: FXML not loading
**Solution**: Verify paths in ViewFactory match actual file locations

## 📄 License

This project is for educational/internal use.

## 👥 Contributors

- Initial setup and architecture: AI Assistant
- Your team/name here

## 📞 Support

For issues or questions, create a ticket in the system (once implemented) or contact your administrator.

---

**Built with ❤️ using JavaFX and clean architecture principles**

