package com.care.controller.user;

import com.care.dao.ChatSessionDAO;
import com.care.dao.MessageDAO;
import com.care.dao.ProductDAO;
import com.care.model.ChatSession;
import com.care.model.Product;
import com.care.util.SessionManager;
import com.care.util.ViewFactory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for User History View
 */
public class UserHistoryController {

    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Label totalSessionsLabel;
    @FXML private TableView<ChatSessionDisplay> historyTable;
    @FXML private TableColumn<ChatSessionDisplay, Integer> sessionIdCol;
    @FXML private TableColumn<ChatSessionDisplay, String> dateCol;
    @FXML private TableColumn<ChatSessionDisplay, String> productCol;
    @FXML private TableColumn<ChatSessionDisplay, String> statusCol;
    @FXML private TableColumn<ChatSessionDisplay, Integer> messagesCountCol;
    @FXML private TableColumn<ChatSessionDisplay, Void> actionsCol;

    private final SessionManager sessionManager;
    private final ChatSessionDAO chatSessionDAO;
    private final MessageDAO messageDAO;
    private final ProductDAO productDAO;
    private List<ChatSession> allSessions;

    public UserHistoryController() {
        this.sessionManager = SessionManager.getInstance();
        this.chatSessionDAO = new ChatSessionDAO();
        this.messageDAO = new MessageDAO();
        this.productDAO = new ProductDAO();
    }

    @FXML
    private void initialize() {
        // Allow horizontal scroll instead of squishing columns
        historyTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Status filter
        statusFilterCombo.getItems().addAll("All", "ACTIVE", "CLOSED", "ESCALATED");
        statusFilterCombo.setValue("All");

        // Columns
        sessionIdCol.setCellValueFactory(c ->
            new SimpleIntegerProperty(c.getValue().getSessionId()).asObject());
        dateCol.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getFormattedDate()));
        productCol.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getProductName()));
        statusCol.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getStatus()));
        messagesCountCol.setCellValueFactory(c ->
            new SimpleIntegerProperty(c.getValue().getMessageCount()).asObject());

        setupActionsColumn();
        loadHistory();
        autoResizeColumns();
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing history...");
        loadHistory();
    }

    @FXML
    private void handleFilterChange() {
        String filter = statusFilterCombo.getValue();
        System.out.println("Filtering by: " + filter);
        applyFilter();
    }

    private void setupActionsColumn() {
        // Only allow users to continue/view chats – no delete option
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button continueBtn = new Button("Continue");
            private final HBox box = new HBox(8, continueBtn);

            {
                continueBtn.getStyleClass().add("primary-button");
                continueBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5 10; -fx-min-width: 90px; -fx-max-width: 120px;");
                box.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                ChatSessionDisplay display = getTableView().getItems().get(getIndex());
                ChatSession session = display.getSession();

                if ("CLOSED".equals(session.getStatus())) {
                    continueBtn.setDisable(true);
                    continueBtn.setText("Ended");
                } else {
                    continueBtn.setDisable(false);
                    continueBtn.setText("ESCALATED".equals(session.getStatus()) ? "View Chat" : "Continue");
                }

                continueBtn.setOnAction(e -> handleContinueChat(session));
                setGraphic(box);
            }
        });
    }

    private void loadHistory() {
        try {
            if (!sessionManager.isLoggedIn()) {
                System.err.println("No user logged in");
                return;
            }

            int userId = sessionManager.getCurrentUser().getUserId();
            allSessions = chatSessionDAO.getByUserId(userId);
            System.out.println("✓ Loaded " + allSessions.size() + " chat sessions for user: " + userId);

            applyFilter();
            autoResizeColumns();
        } catch (Exception e) {
            System.err.println("Error loading chat history");
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Load History");
            alert.setContentText("Error: " + e.getMessage());
            alert.show();
        }
    }

    private void applyFilter() {
        String filter = statusFilterCombo.getValue();
        List<ChatSession> filtered = allSessions;

        if (!"All".equals(filter)) {
            filtered = allSessions.stream()
                .filter(s -> filter.equals(s.getStatus()))
                .collect(Collectors.toList());
        }

        historyTable.getItems().clear();
        for (ChatSession s : filtered) {
            historyTable.getItems().add(new ChatSessionDisplay(s));
        }

        totalSessionsLabel.setText("Total Sessions: " + filtered.size());
    }

    private void handleContinueChat(ChatSession session) {
        System.out.println("Continue chat for session: " + session.getSessionId());

        if ("CLOSED".equals(session.getStatus())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cannot Continue");
            alert.setHeaderText("Chat Has Ended");
            alert.setContentText("This chat session has been closed. Please start a new chat.");
            alert.show();
            return;
        }

        try {
            sessionManager.setCurrentChatSession(session);
            ViewFactory.getInstance().setUserSelectedMenuItem("ChatArea");
            System.out.println("✓ Navigating to ChatArea with session " + session.getSessionId());
        } catch (Exception e) {
            System.err.println("Error continuing chat: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Continue Chat");
            alert.setContentText("Error: " + e.getMessage());
            alert.show();
        }
    }

    private void handleDeleteSession(ChatSession session) {
        // Deleting chat history from the user UI has been disabled by design.
        System.out.println("Delete session requested from UI (disabled): " + session.getSessionId());
    }

    private void autoResizeColumns() {
        // Fit columns nicely within the table box
        actionsCol.setPrefWidth(140);
        sessionIdCol.setPrefWidth(70);
        messagesCountCol.setPrefWidth(90);

        TableColumn<?, ?>[] textColumns = {dateCol, productCol, statusCol};
        Text t = new Text();

        for (TableColumn<?, ?> column : textColumns) {
            double max = column.getText().length() * 10.0;
            int rows = Math.min(historyTable.getItems().size(), 50);

            for (int i = 0; i < rows; i++) {
                Object data = column.getCellData(i);
                if (data != null) {
                    t.setText(data.toString());
                    double w = t.getLayoutBounds().getWidth();
                    if (w > max) max = w;
                }
            }
            column.setPrefWidth(max + 30.0);
        }
    }

    public class ChatSessionDisplay {
        private final ChatSession session;
        private final String productName;
        private final int messageCount;
        private final String formattedDate;

        public ChatSessionDisplay(ChatSession session) {
            this.session = session;

            Product product = productDAO.getById(session.getProductId());
            this.productName = (product != null) ? product.getName() : "Product Deleted";

            this.messageCount = messageDAO.getMessageCount(session.getSessionId());

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            this.formattedDate = session.getCreatedAt().format(fmt);
        }

        public int getSessionId() { return session.getSessionId(); }
        public String getFormattedDate() { return formattedDate; }
        public String getProductName() { return productName; }
        public String getStatus() { return session.getStatus(); }
        public int getMessageCount() { return messageCount; }
        public ChatSession getSession() { return session; }
    }
}

