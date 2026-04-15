package com.hotel.ui;

import com.hotel.service.HotelDataService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

/**
 * Dashboard view — displays analytics cards and a summary
 * of hotel operations at a glance.
 */
public class DashboardView {

    private final HotelDataService dataService;
    private VBox root;

    // Direct references to value labels (no lookup needed)
    private Label totalRoomsValue;
    private Label availableRoomsValue;
    private Label occupiedRoomsValue;
    private Label activeBookingsValue;
    private Label revenueValue;
    private Label occupancyValue;

    public DashboardView(HotelDataService dataService) {
        this.dataService = dataService;
        this.root = buildView();
        refresh(); // Initial data load after UI is built
    }

    public VBox getView() { return root; }

    public void refresh() {
        totalRoomsValue.setText(String.valueOf(dataService.getTotalRoomCount()));
        availableRoomsValue.setText(String.valueOf(dataService.getAvailableRoomCount()));
        occupiedRoomsValue.setText(String.valueOf(dataService.getOccupiedRoomCount()));
        activeBookingsValue.setText(String.valueOf(dataService.getActiveBookingCount()));
        revenueValue.setText(String.format("₹ %.0f", dataService.getTotalRevenue()));

        long total = dataService.getTotalRoomCount();
        long occupied = dataService.getOccupiedRoomCount();
        double rate = total > 0 ? (occupied * 100.0 / total) : 0;
        occupancyValue.setText(String.format("%.1f%%", rate));
    }

    private VBox buildView() {
        VBox container = new VBox(24);
        container.getStyleClass().add("content-area");
        container.setPadding(new Insets(30));

        // ---- Header ----
        Label header = new Label("📊  Dashboard");
        header.getStyleClass().add("section-header");

        Label subtitle = new Label("Hotel operations overview at a glance");
        subtitle.getStyleClass().add("section-subtitle");

        VBox headerBox = new VBox(4, header, subtitle);

        // ---- Metric Cards Row 1 ----
        totalRoomsValue = new Label("0");
        VBox totalCard = createMetricCard("Total Rooms", "🏠", "#6366f1", "#818cf8", totalRoomsValue);

        availableRoomsValue = new Label("0");
        VBox availableCard = createMetricCard("Available", "✅", "#22c55e", "#4ade80", availableRoomsValue);

        occupiedRoomsValue = new Label("0");
        VBox occupiedCard = createMetricCard("Occupied", "🔒", "#ef4444", "#f87171", occupiedRoomsValue);

        HBox cardsRow1 = new HBox(20, totalCard, availableCard, occupiedCard);
        cardsRow1.setAlignment(Pos.CENTER_LEFT);

        // ---- Metric Cards Row 2 ----
        activeBookingsValue = new Label("0");
        VBox bookingsCard = createMetricCard("Active Bookings", "📅", "#f59e0b", "#fbbf24", activeBookingsValue);

        revenueValue = new Label("₹ 0");
        VBox revenueCard = createMetricCard("Total Revenue", "💰", "#10b981", "#34d399", revenueValue);

        occupancyValue = new Label("0.0%");
        VBox occupancyCard = createMetricCard("Occupancy Rate", "📈", "#8b5cf6", "#a78bfa", occupancyValue);

        HBox cardsRow2 = new HBox(20, bookingsCard, revenueCard, occupancyCard);
        cardsRow2.setAlignment(Pos.CENTER_LEFT);

        // ---- Welcome Message ----
        VBox welcomeBox = new VBox(10);
        welcomeBox.getStyleClass().add("welcome-card");
        welcomeBox.setPadding(new Insets(25));

        Label welcomeTitle = new Label("Welcome to Grand Hotel Manager");
        welcomeTitle.getStyleClass().add("welcome-title");

        Label welcomeText = new Label(
                "Manage your hotel operations efficiently. Use the sidebar to navigate between " +
                "Room Management, Booking & Checkout, and Customer Records. " +
                "All data is automatically saved and persisted between sessions."
        );
        welcomeText.getStyleClass().add("welcome-text");
        welcomeText.setWrapText(true);

        Label quickLabel = new Label("Quick Tips:");
        quickLabel.getStyleClass().add("quick-tip-title");

        Label tip1 = createTipBadge("💡  Use search to find rooms instantly");
        Label tip2 = createTipBadge("🎨  Toggle dark/light mode in the header");
        Label tip3 = createTipBadge("💾  Data auto-saves to local JSON files");

        VBox tipsBox = new VBox(8, quickLabel, tip1, tip2, tip3);
        tipsBox.setPadding(new Insets(10, 0, 0, 0));

        welcomeBox.getChildren().addAll(welcomeTitle, welcomeText, tipsBox);

        container.getChildren().addAll(headerBox, cardsRow1, cardsRow2, welcomeBox);
        return container;
    }

    private VBox createMetricCard(String title, String icon, String color1, String color2, Label valueLabel) {
        VBox card = new VBox(8);
        card.getStyleClass().add("metric-card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setMinWidth(180);
        HBox.setHgrow(card, Priority.ALWAYS);

        // Icon circle
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");
        Circle iconBg = new Circle(22);
        iconBg.setFill(Color.web(color1, 0.15));

        StackPane iconPane = new StackPane(iconBg, iconLabel);
        iconPane.setAlignment(Pos.CENTER_LEFT);
        iconPane.setMaxWidth(50);

        valueLabel.getStyleClass().add("metric-value");
        valueLabel.setStyle("-fx-text-fill: " + color2 + ";");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");

        card.getChildren().addAll(iconPane, valueLabel, titleLabel);
        return card;
    }

    private Label createTipBadge(String text) {
        Label badge = new Label(text);
        badge.getStyleClass().add("tip-badge");
        return badge;
    }
}
