# CARE - Customer Assistance and Resource Engine

A comprehensive JavaFX-based customer support system with AI-powered chat, ticket management, and knowledge base.

## 🚀 Project Overview

CARE is a production-ready customer support application that provides AI-powered assistance, ticket management, and comprehensive analytics. It supports multiple user roles (User, Agent, Admin) with role-based dashboards and features.

### Key Highlights
- **AI-Powered Support**: OpenAI GPT integration with product context
- **Multi-Language Support**: Automatic language detection and response
- **Image Analysis**: Vision API for analyzing product issues from images
- **Role-Based Access**: Separate portals for Users, Agents, and Admins
- **Real-Time Analytics**: Comprehensive dashboards and reports
- **PDF Knowledge Base**: Automatic text extraction from product manuals

## 🛠️ Technology Stack

- **Language**: Java 17+
- **UI Framework**: JavaFX 21
- **Build Tool**: Maven
- **Database**: SQLite (JDBC)
- **Architecture**: MVC with Service-Repository pattern
- **AI Integration**: OpenAI API (GPT-3.5-turbo, GPT-4o for vision)
- **Libraries**: Apache PDFBox, BCrypt, org.json, JUnit 5

## 👥 User Roles

### **USER** (Customer)
- Chat with AI assistant
- View chat history
- Manage profile and settings
- Select products for support

### **AGENT** (Support Agent)
- View assigned tickets
- Reply to customers
- Mark tickets as resolved
- Filter and manage tickets

### **ADMIN** (Administrator)
- Full system access
- User management (CRUD)
- Product management with PDF upload
- Knowledge base management
- Ticket management and assignment
- Analytics and reports

## 📋 Features

### Authentication & Security
- User login and registration
- Password hashing (BCrypt)
- Session management
- Role-based routing
- Secure password change

### User Portal
- Product selection by category
- AI chat with product context
- Image attachment for issue analysis
- Chat history viewing
- Continue previous chats
- Profile management
- Password change
- Preferences settings

### Admin Portal
- Dashboard with real-time statistics
- User management (add, edit, delete, filter)
- Product management (add, delete, PDF upload)
- Knowledge base management
- Ticket management and assignment
- Analytics dashboard with charts
- PDF report generation

### Agent Portal
- Assigned tickets dashboard
- View conversation history
- Reply to customers
- Mark tickets as resolved
- Filter tickets by status
- Real-time statistics

### AI & Intelligence
- OpenAI GPT integration
- Product context injection
- PDF manual text extraction
- Multi-language support (auto-detect)
- Vision API for image analysis
- Conversation history maintenance

## 📁 Project Structure

```
src/main/
├── java/com/care/
│   ├── App.java                    # Main entry point
│   ├── model/                      # Data models (7 classes)
│   ├── dao/                        # Data Access Objects (6 classes)
│   ├── service/                    # Business logic (6 services)
│   ├── controller/                 # JavaFX Controllers
│   │   ├── shared/                 # Login, Register
│   │   ├── user/                   # User portal (5 controllers)
│   │   ├── admin/                  # Admin portal (11 controllers)
│   │   └── agent/                  # Agent portal (1 controller)
│   └── util/                       # Utilities
├── resources/com/care/
│   ├── view/                       # FXML files (16 views)
│   ├── styles/                     # CSS stylesheets
│   └── sql/                        # Database schema
└── config.properties               # Configuration
```

## 🏃 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- OpenAI API key (for AI features)

### Installation

1. **Navigate to project directory**
   ```bash
   cd CAREv2
   ```

2. **Configure OpenAI API**
   - Open `src/main/resources/config.properties`
   - Add your OpenAI API key:
     ```properties
     openai.api.key=your-api-key-here
     openai.model=gpt-3.5-turbo
     ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn javafx:run
   ```

### Default Credentials

**Admin:** (has a portal)
- Email: `admin@care.com`
- Password: `admin123`

**Agent:** (does not have a portal)
- Email: `agent@gmail.com`
- Password: `password`

**User:** (has portal)
- Email: `user@gmail.com`
- Password: `password`

## ⚙️ Configuration

### config.properties (find the file in src\main\resources\config.properties)

```properties
# OpenAI API Settings
openai.api.key=your-api-key-here
openai.model=gpt-3.5-turbo
openai.max.tokens=500
openai.temperature=0.7
```

**Model Options:**
- `gpt-3.5-turbo` - Fast, cost-effective (recommended)
- `gpt-4` - More capable, slower, more expensive
- `gpt-4o` - Best for vision/image analysis

## 📊 Database Schema

The application uses SQLite with the following tables:
- **users**: User accounts with roles
- **products**: Product catalog
- **chat_sessions**: Chat conversations
- **messages**: Individual messages
- **tickets**: Escalated support tickets
- **knowledge_base**: Documentation and support articles

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
4. **AIService**: Handles OpenAI API integration
5. **Controllers**: Handle UI events and user interactions

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

### Issue: OpenAI API not working
**Solution**: Check API key in `config.properties`, maybe the key is expired or blocked by OpennAI, and verify internet connection

### Issue: FXML not loading
**Solution**: Verify paths in ViewFactory match actual file locations


**Version 2.0 - December 2024**
