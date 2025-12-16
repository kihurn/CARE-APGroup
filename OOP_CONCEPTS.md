# OOP Concepts in CARE Project

This document outlines the Object-Oriented Programming (OOP) concepts and design patterns used throughout the CARE application.

## 📚 OOP Concepts Used

### 1. **Inheritance**

Inheritance is used to extend existing classes and create specialized implementations.

#### Examples:

**JavaFX Application Inheritance:**
- **Location**: `src/main/java/com/care/App.java`
```java
public class App extends Application {
    @Override
    public void start(Stage stage) { ... }
    @Override
    public void stop() { ... }
}
```

**TableCell Inheritance:**
Multiple controllers use anonymous inner classes that extend `TableCell` for custom table cell rendering:

- **AgentOverviewController** - `src/main/java/com/care/controller/agent/AgentOverviewController.java`
  - Lines 101-131: Priority column cell factory
  - Lines 133-158: Status column cell factory
  - Lines 155-198: Actions column cell factory

- **AdminUsersController** - `src/main/java/com/care/controller/admin/AdminUsersController.java`
  - Lines 90-134: Actions column with Edit/Delete buttons

- **UserHistoryController** - `src/main/java/com/care/controller/user/UserHistoryController.java`
  - Lines 90-122: Actions column with Continue button

- **AdminKBController** - `src/main/java/com/care/controller/admin/AdminKBController.java`
  - Lines 71-88: Manual status column
  - Lines 91-138: Actions column

- **AdminOverviewController** - `src/main/java/com/care/controller/admin/AdminOverviewController.java`
  - Lines 94-125: Recent escalations actions column

- **AdminTicketsController** - `src/main/java/com/care/controller/admin/AdminTicketsController.java`
  - Lines 104-165: Actions column

- **AdminProductsController** - `src/main/java/com/care/controller/admin/AdminProductsController.java`
  - Lines 70-120: Actions column

- **AdminReportsController** - `src/main/java/com/care/controller/admin/AdminReportsController.java`
  - Lines 100-142: Escalation actions column

Example:
```java
// src/main/java/com/care/controller/agent/AgentOverviewController.java (Line 101)
priorityCol.setCellFactory(column -> new TableCell<TicketDisplay, String>() {
    @Override
    protected void updateItem(String priority, boolean empty) {
        super.updateItem(priority, empty);
        // Custom rendering logic
    }
});
```

### 2. **Polymorphism**

Polymorphism allows objects of different types to be treated through the same interface.

#### Method Overriding:
- **20+ `@Override` annotations** found throughout the codebase

**Model Classes - toString() Override:**
- `User.toString()` - `src/main/java/com/care/model/User.java` (Line 116)
- `Product.toString()` - `src/main/java/com/care/model/Product.java` (Line 68)
- `Ticket.toString()` - `src/main/java/com/care/model/Ticket.java` (Line 95)
- `Message.toString()` - `src/main/java/com/care/model/Message.java` (Line 78)
- `ChatSession.toString()` - `src/main/java/com/care/model/ChatSession.java` (Line 110)
- `KnowledgeBase.toString()` - `src/main/java/com/care/model/KnowledgeBase.java` (Line 86)

**TableCell - updateItem() Override:**
- All TableCell implementations override `updateItem()` method (see Inheritance section for locations)

**Application - start() and stop() Override:**
- `App.start()` - `src/main/java/com/care/App.java` (Line 14)
- `App.stop()` - `src/main/java/com/care/App.java` (Line 46)

#### Callback Interfaces:
Using JavaFX `Callback` interface for polymorphic behavior:

**Location**: `src/main/java/com/care/controller/admin/AdminUsersController.java` (Lines 90-134)
```java
actionsCol.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
    @Override
    public TableCell<User, Void> call(TableColumn<User, Void> param) {
        return new TableCell<User, Void>() { ... };
    }
});
```

#### Lambda Expressions:
Functional programming approach for callbacks:
```java
actionsCol.setCellFactory(param -> new TableCell<>() {
    // Implementation
});
```

### 3. **Encapsulation**

Encapsulation is extensively used to hide internal implementation details and provide controlled access.

#### Private Fields with Public Accessors:
All model classes follow this pattern:

**Model Classes:**
- `src/main/java/com/care/model/User.java` - Lines 10-19 (fields), 35-114 (getters/setters)
- `src/main/java/com/care/model/Product.java` - Lines 10-14 (fields), 28-66 (getters/setters)
- `src/main/java/com/care/model/Ticket.java` - Lines 10-18 (fields), 30-94 (getters/setters)
- `src/main/java/com/care/model/Message.java` - Lines 10-15 (fields), 25-77 (getters/setters)
- `src/main/java/com/care/model/ChatSession.java` - Lines 10-17 (fields), 31-108 (getters/setters)
- `src/main/java/com/care/model/KnowledgeBase.java` - Lines 10-16 (fields), 25-85 (getters/setters)

Example:
```java
// src/main/java/com/care/model/User.java
private int userId;
private String email;
private String passwordHash;

public int getUserId() { return userId; }
public void setUserId(int userId) { this.userId = userId; }
public String getEmail() { return email; }
public void setEmail(String email) { this.email = email; }
```

#### Private Constructors (Singleton Pattern):

**Location**: `src/main/java/com/care/util/DatabaseDriver.java` (Line 25)
```java
private DatabaseDriver() {
    // Private constructor prevents instantiation
}
```

**Location**: `src/main/java/com/care/util/SessionManager.java` (Line 20)
```java
private SessionManager() {
    this.currentUser = null;
}
```

**Location**: `src/main/java/com/care/util/ViewFactory.java` (Line 39)
```java
private ViewFactory() {
    // Private constructor
}
```

#### Private Methods:
Internal helper methods are kept private:

- `DatabaseDriver.initializeDatabase()` - `src/main/java/com/care/util/DatabaseDriver.java` (Line 86)
- `ChatSessionDAO.mapResultSetToSession()` - `src/main/java/com/care/dao/ChatSessionDAO.java` (Line 219)
- `MessageDAO.mapResultSetToMessage()` - `src/main/java/com/care/dao/MessageDAO.java` (Line 148)
- `UserDAO.mapResultSetToUser()` - `src/main/java/com/care/dao/UserDAO.java` (if exists)
- `ProductDAO.mapResultSetToProduct()` - `src/main/java/com/care/dao/ProductDAO.java` (if exists)
- Various `setup*()` methods in controllers (private helper methods)

### 4. **Abstraction**

Abstraction is achieved through interfaces, service layers, and design patterns.

#### JavaFX Interfaces:
- Uses `Callback` interface for table cell factories
- Uses `Property` interfaces for observable values
- Uses `ChangeListener` for reactive programming

#### Service Layer Abstraction:
Business logic is abstracted from controllers:

- `AIService` - `src/main/java/com/care/service/AIService.java` - Abstracts OpenAI API calls
- `UserService` - `src/main/java/com/care/service/UserService.java` - Abstracts user business logic
- `TicketService` - `src/main/java/com/care/service/TicketService.java` - Abstracts ticket operations
- `ProductService` - `src/main/java/com/care/service/ProductService.java` - Abstracts product operations
- `AnalyticsService` - `src/main/java/com/care/service/AnalyticsService.java` - Abstracts analytics calculations
- `ReportGeneratorService` - `src/main/java/com/care/service/ReportGeneratorService.java` - Abstracts report generation

#### DAO Pattern (Data Access Abstraction):
All database operations are abstracted through DAO classes:

- `UserDAO` - `src/main/java/com/care/dao/UserDAO.java` - User data access
- `ProductDAO` - `src/main/java/com/care/dao/ProductDAO.java` - Product data access
- `TicketDAO` - `src/main/java/com/care/dao/TicketDAO.java` - Ticket data access
- `MessageDAO` - `src/main/java/com/care/dao/MessageDAO.java` - Message data access
- `ChatSessionDAO` - `src/main/java/com/care/dao/ChatSessionDAO.java` - Chat session data access
- `KnowledgeBaseDAO` - `src/main/java/com/care/dao/KnowledgeBaseDAO.java` - Knowledge base data access

### 5. **Design Patterns**

#### Singleton Pattern:
Three key classes use Singleton pattern for global state management:

**DatabaseDriver:**
- **Location**: `src/main/java/com/care/util/DatabaseDriver.java` (Lines 18, 55-60)
```java
private static DatabaseDriver instance;
public static synchronized DatabaseDriver getInstance() {
    if (instance == null) {
        instance = new DatabaseDriver();
    }
    return instance;
}
```

**SessionManager:**
- **Location**: `src/main/java/com/care/util/SessionManager.java` (Lines 12, 29-34)
```java
private static SessionManager instance;
public static synchronized SessionManager getInstance() {
    if (instance == null) {
        instance = new SessionManager();
    }
    return instance;
}
```

**ViewFactory:**
- **Location**: `src/main/java/com/care/util/ViewFactory.java` (Lines 18, 47-52)
```java
private static ViewFactory instance;
public static synchronized ViewFactory getInstance() {
    if (instance == null) {
        instance = new ViewFactory();
    }
    return instance;
}
```

#### Factory Pattern:
`ViewFactory` acts as a factory for creating and loading FXML views:

- **Location**: `src/main/java/com/care/util/ViewFactory.java`
- `loadUserChildView(String viewName)` - Line 200
- `loadAdminChildView(String viewName)` - Line 168
- `loadAgentChildView(String viewName)` - Line 236

#### DAO Pattern:
Separates data access logic from business logic:
- Each entity has a corresponding DAO class
- DAOs handle all database operations
- Services use DAOs for data access

#### MVC Pattern:
- **Model**: Data classes (User, Product, Ticket, etc.)
- **View**: FXML files defining UI
- **Controller**: JavaFX controllers handling user interactions

### 6. **Inner Classes**

#### Static Inner Classes:
Used for data transfer objects and display models:

**AgentOverviewController:**
- **Location**: `src/main/java/com/care/controller/agent/AgentOverviewController.java` (Lines 515-551)
```java
public static class TicketDisplay {
    private Ticket ticket;
    private int ticketId;
    private String userName;
    // ... getters and setters
}
```

**AdminOverviewController:**
- **Location**: `src/main/java/com/care/controller/admin/AdminOverviewController.java` (Lines 585-620)
```java
public static class EscalationRow {
    private int ticketId;
    private String customerName;
    // ... getters and setters
}
```

**AdminKBController:**
- **Location**: `src/main/java/com/care/controller/admin/AdminKBController.java` (Lines 271-310)
```java
public static class ProductKB {
    private Product product;
    private boolean hasManual;
    // ... getters and setters
}
```

**AnalyticsService:**
- **Location**: `src/main/java/com/care/service/AnalyticsService.java`
  - `IssueData` - Lines 403-424
  - `AgentPerformance` - Lines 426-449
  - `EscalationDetail` - Lines 451-472

#### Anonymous Inner Classes:
Extensively used for TableCell implementations:

**Locations:**
- `src/main/java/com/care/controller/agent/AgentOverviewController.java` (Lines 101, 133, 155)
- `src/main/java/com/care/controller/admin/AdminUsersController.java` (Line 93)
- `src/main/java/com/care/controller/user/UserHistoryController.java` (Line 90)
- `src/main/java/com/care/controller/admin/AdminKBController.java` (Lines 71, 91)
- `src/main/java/com/care/controller/admin/AdminOverviewController.java` (Line 94)
- `src/main/java/com/care/controller/admin/AdminTicketsController.java` (Line 104)
- `src/main/java/com/care/controller/admin/AdminProductsController.java` (Line 70)
- `src/main/java/com/care/controller/admin/AdminReportsController.java` (Line 100)

Example:
```java
// src/main/java/com/care/controller/agent/AgentOverviewController.java (Line 155)
actionsCol.setCellFactory(param -> new TableCell<>() {
    private final Button viewBtn = new Button("View");
    // ... implementation
});
```

### 7. **Method Overloading**

Multiple constructors in model classes provide flexibility:

**User.java:**
- **Location**: `src/main/java/com/care/model/User.java` (Lines 22-33)
```java
public User() { }  // Default constructor
public User(int userId, String email, String passwordHash, String role, String name) {
    // Parameterized constructor
}
```

**Product.java:**
- **Location**: `src/main/java/com/care/model/Product.java` (Lines 17-25)
```java
public Product() { }  // Default constructor
public Product(int productId, String name, String modelVersion, String category) {
    // Parameterized constructor
}
```

**ChatSession.java:**
- **Location**: `src/main/java/com/care/model/ChatSession.java` (Lines 20-28)
```java
public ChatSession() { }  // Default constructor
public ChatSession(int sessionId, int userId, Integer productId, String status) {
    // Parameterized constructor
}
```

### 8. **Reactive Programming**

Using JavaFX properties and listeners for reactive behavior:

**ViewFactory:**
- **Location**: `src/main/java/com/care/util/ViewFactory.java` (Lines 22-24)
```java
private final ObjectProperty<String> userSelectedMenuItem = new SimpleObjectProperty<>();
private final ObjectProperty<String> adminSelectedMenuItem = new SimpleObjectProperty<>();
private final ObjectProperty<String> agentSelectedMenuItem = new SimpleObjectProperty<>();
```

**Controllers listen to property changes:**

- **UserDashboardController** - `src/main/java/com/care/controller/user/UserDashboardController.java` (Line 30)
- **AdminDashboardController** - `src/main/java/com/care/controller/admin/AdminDashboardController.java` (Line 30)
- **AgentDashboardController** - `src/main/java/com/care/controller/agent/AgentDashboardController.java` (Line 30)

Example:
```java
// src/main/java/com/care/controller/user/UserDashboardController.java (Line 30)
viewFactory.userSelectedMenuItemProperty().addListener((observable, oldValue, newValue) -> {
    if (newValue != null) {
        loadChildView(newValue);
    }
});
```

## 📊 Summary

| OOP Concept | Usage Count | Examples |
|-------------|-------------|----------|
| **Inheritance** | 1 explicit + 20+ anonymous | App extends Application, TableCell implementations |
| **Polymorphism** | 20+ overrides | toString(), updateItem(), start(), stop() |
| **Encapsulation** | All classes | Private fields, getters/setters, private constructors |
| **Abstraction** | Service layer + DAO | AIService, UserService, UserDAO, etc. |
| **Singleton Pattern** | 3 classes | DatabaseDriver, SessionManager, ViewFactory |
| **Inner Classes** | 6+ static classes | TicketDisplay, EscalationRow, ProductKB, etc. |
| **Method Overloading** | All models | Multiple constructors per model class |

## 🎯 Benefits

1. **Maintainability**: Clear separation of concerns through MVC and service layers
2. **Reusability**: Singleton pattern ensures single instances, DAO pattern enables code reuse
3. **Flexibility**: Polymorphism allows different implementations through same interface
4. **Security**: Encapsulation protects internal state
5. **Scalability**: Abstraction allows easy extension without modifying existing code

## 📝 Notes

- The project follows Java best practices for OOP
- Design patterns are consistently applied throughout
- Code structure promotes maintainability and testability
- All model classes follow JavaBean conventions (private fields, public getters/setters)

