package com.hotel;

import com.hotel.service.HotelDataService;
import com.hotel.ui.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Main entry point for the Hotel Management System.
 * Builds the application shell with a sidebar navigation and content area.
 */
public class HotelManagementApp extends Application {

    private HotelDataService dataService;
    private StackPane contentPane;
    private DashboardView dashboardView;
    private RoomView roomView;
    private BookingView bookingView;
    private CustomerView customerView;
    private boolean isDarkMode = true;
    private Scene scene;

    // Sidebar buttons for active-state management
    private Button btnDashboard;
    private Button btnRooms;
    private Button btnBookings;
    private Button btnCustomers;
    private Button activeBtn;

    @Override
    public void start(Stage primaryStage) {
        dataService = new HotelDataService();

        // Initialize views
        dashboardView = new DashboardView(dataService);
        roomView = new RoomView(dataService);
        bookingView = new BookingView(dataService, () -> dashboardView.refresh());
        customerView = new CustomerView(dataService);

        // ---- Top Bar ----
        HBox topBar = createTopBar();

        // ---- Sidebar ----
        VBox sidebar = createSidebar();

        // ---- Content Area ----
        contentPane = new StackPane();
        contentPane.getStyleClass().add("content-pane");
        HBox.setHgrow(contentPane, Priority.ALWAYS);

        // Start with dashboard
        contentPane.getChildren().add(dashboardView.getView());

        // ---- Main Layout ----
        HBox mainBody = new HBox(sidebar, contentPane);
        VBox.setVgrow(mainBody, Priority.ALWAYS);

        VBox rootLayout = new VBox(topBar, mainBody);
        rootLayout.getStyleClass().add("root-container");

        scene = new Scene(rootLayout, 1280, 820);
        scene.getStylesheets().add(getClass().getResource("/com/hotel/styles.css").toExternalForm());

        primaryStage.setTitle("Shanaia's Grand Hotel");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");
        topBar.setPadding(new Insets(12, 24, 12, 24));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(16);

        Label logo = new Label("🏨");
        logo.setStyle("-fx-font-size: 28px;");

        Label title = new Label("Shanaia's Grand Hotel");
        title.getStyleClass().add("app-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Dark/Light mode toggle
        ToggleButton themeToggle = new ToggleButton("🌙  Dark Mode");
        themeToggle.getStyleClass().add("theme-toggle");
        themeToggle.setSelected(true);
        themeToggle.setOnAction(e -> {
            isDarkMode = themeToggle.isSelected();
            if (isDarkMode) {
                themeToggle.setText("🌙  Dark Mode");
                scene.getRoot().getStyleClass().remove("light-mode");
            } else {
                themeToggle.setText("☀  Light Mode");
                scene.getRoot().getStyleClass().add("light-mode");
            }
        });

        topBar.getChildren().addAll(logo, title, spacer, themeToggle);
        return topBar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20, 12, 20, 12));
        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(200);

        Label navLabel = new Label("NAVIGATION");
        navLabel.getStyleClass().add("nav-label");

        btnDashboard = createNavButton("📊  Dashboard");
        btnRooms = createNavButton("🏠  Rooms");
        btnBookings = createNavButton("📅  Bookings");
        btnCustomers = createNavButton("👤  Customers");

        btnDashboard.setOnAction(e -> switchView(dashboardView.getView(), btnDashboard));
        btnRooms.setOnAction(e -> switchView(roomView.getView(), btnRooms));
        btnBookings.setOnAction(e -> switchView(bookingView.getView(), btnBookings));
        btnCustomers.setOnAction(e -> switchView(customerView.getView(), btnCustomers));

        // Set initial active
        activeBtn = btnDashboard;
        btnDashboard.getStyleClass().add("nav-btn-active");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label versionLabel = new Label("v1.0.0");
        versionLabel.getStyleClass().add("version-label");

        Label creditLabel = new Label("Hotel Management System");
        creditLabel.getStyleClass().add("credit-label");

        sidebar.getChildren().addAll(
                navLabel,
                btnDashboard, btnRooms, btnBookings, btnCustomers,
                spacer,
                new Separator(),
                creditLabel, versionLabel
        );

        return sidebar;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private void switchView(VBox view, Button clickedBtn) {
        contentPane.getChildren().setAll(view);

        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("nav-btn-active");
        }
        clickedBtn.getStyleClass().add("nav-btn-active");
        activeBtn = clickedBtn;

        // Refresh dashboard when switching to it
        if (view == dashboardView.getView()) {
            dashboardView.refresh();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
