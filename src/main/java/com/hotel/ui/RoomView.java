package com.hotel.ui;

import com.hotel.model.Room;
import com.hotel.service.HotelDataService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Room management view — displays room table with search/filter,
 * and a form to add new rooms.
 */
public class RoomView {

    private final HotelDataService dataService;
    private final VBox root;
    private TableView<Room> table;
    private TextField searchField;
    private ComboBox<String> filterCombo;
    private TextField roomNumberInput;
    private ComboBox<Room.RoomType> roomTypeInput;
    private TextField priceInput;
    private Label statusLabel;

    public RoomView(HotelDataService dataService) {
        this.dataService = dataService;
        this.root = buildView();
    }

    public VBox getView() { return root; }

    @SuppressWarnings("unchecked")
    private VBox buildView() {
        VBox container = new VBox(20);
        container.getStyleClass().add("content-area");
        container.setPadding(new Insets(30));

        // ---- Header ----
        Label header = new Label("🏨  Room Management");
        header.getStyleClass().add("section-header");

        Label subtitle = new Label("Add, view, and manage hotel rooms");
        subtitle.getStyleClass().add("section-subtitle");

        VBox headerBox = new VBox(4, header, subtitle);

        // ---- Search & Filter Bar ----
        searchField = new TextField();
        searchField.setPromptText("🔍  Search by room number or type...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);

        filterCombo = new ComboBox<>(FXCollections.observableArrayList(
                "All Rooms", "Available Only", "Occupied Only", "Single", "Double", "Deluxe"
        ));
        filterCombo.setValue("All Rooms");
        filterCombo.getStyleClass().add("filter-combo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox searchBar = new HBox(12, searchField, filterCombo, spacer);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.getStyleClass().add("search-bar");

        // ---- Table ----
        table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Room, Number> colNum = new TableColumn<>("Room #");
        colNum.setCellValueFactory(cd -> cd.getValue().roomNumberProperty());
        colNum.setPrefWidth(100);
        colNum.getStyleClass().add("center-column");

        TableColumn<Room, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getRoomType().toString()));
        colType.setPrefWidth(120);

        TableColumn<Room, String> colPrice = new TableColumn<>("Price / Day");
        colPrice.setCellValueFactory(cd -> new SimpleStringProperty(
                String.format("₹ %.0f", cd.getValue().getPricePerDay())));
        colPrice.setPrefWidth(130);

        TableColumn<Room, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().isAvailable() ? "Available" : "Occupied"));
        colStatus.setPrefWidth(140);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Circle dot = new Circle(5);
                    if ("Available".equals(item)) {
                        dot.setFill(Color.web("#4ade80"));
                        setText("  Available");
                        getStyleClass().removeAll("status-occupied");
                        getStyleClass().add("status-available");
                    } else {
                        dot.setFill(Color.web("#f87171"));
                        setText("  Occupied");
                        getStyleClass().removeAll("status-available");
                        getStyleClass().add("status-occupied");
                    }
                    setGraphic(dot);
                }
            }
        });

        table.getColumns().addAll(colNum, colType, colPrice, colStatus);

        // ---- Filtered & Sorted data ----
        FilteredList<Room> filteredData = new FilteredList<>(dataService.getRooms(), p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                updateFilter(filteredData));
        filterCombo.valueProperty().addListener((obs, oldVal, newVal) ->
                updateFilter(filteredData));

        SortedList<Room> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // ---- Add Room Form ----
        Label formHeader = new Label("➕  Add New Room");
        formHeader.getStyleClass().add("form-header");

        roomNumberInput = new TextField();
        roomNumberInput.setPromptText("Room Number (e.g. 105)");
        roomNumberInput.getStyleClass().add("form-input");

        roomTypeInput = new ComboBox<>(FXCollections.observableArrayList(Room.RoomType.values()));
        roomTypeInput.setPromptText("Select Room Type");
        roomTypeInput.getStyleClass().add("form-input");
        roomTypeInput.setMaxWidth(Double.MAX_VALUE);

        priceInput = new TextField();
        priceInput.setPromptText("Price per Day (₹)");
        priceInput.getStyleClass().add("form-input");

        // Auto-fill price based on type
        roomTypeInput.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switch (newVal) {
                    case SINGLE -> priceInput.setText("2500");
                    case DOUBLE -> priceInput.setText("4000");
                    case DELUXE -> priceInput.setText("6500");
                }
            }
        });

        Button addBtn = new Button("Add Room");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> handleAddRoom());

        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-message");

        VBox formBox = new VBox(12, formHeader, roomNumberInput, roomTypeInput, priceInput, addBtn, statusLabel);
        formBox.getStyleClass().add("form-card");
        formBox.setPadding(new Insets(20));
        formBox.setPrefWidth(280);
        formBox.setMinWidth(260);

        // ---- Layout ----
        VBox tableSection = new VBox(12, searchBar, table);
        HBox.setHgrow(tableSection, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);

        HBox mainContent = new HBox(20, tableSection, formBox);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        container.getChildren().addAll(headerBox, mainContent);
        return container;
    }

    private void updateFilter(FilteredList<Room> filteredData) {
        filteredData.setPredicate(room -> {
            String search = searchField.getText();
            String filter = filterCombo.getValue();

            // Check filter dropdown
            boolean passesFilter = switch (filter) {
                case "Available Only" -> room.isAvailable();
                case "Occupied Only" -> !room.isAvailable();
                case "Single" -> room.getRoomType() == Room.RoomType.SINGLE;
                case "Double" -> room.getRoomType() == Room.RoomType.DOUBLE;
                case "Deluxe" -> room.getRoomType() == Room.RoomType.DELUXE;
                default -> true;
            };
            if (!passesFilter) return false;

            // Check search text
            if (search == null || search.isBlank()) return true;
            String lowerSearch = search.toLowerCase();
            return String.valueOf(room.getRoomNumber()).contains(lowerSearch) ||
                    room.getRoomType().toString().toLowerCase().contains(lowerSearch);
        });
    }

    private void handleAddRoom() {
        statusLabel.getStyleClass().removeAll("error-message", "success-message");

        String numText = roomNumberInput.getText().trim();
        Room.RoomType type = roomTypeInput.getValue();
        String priceText = priceInput.getText().trim();

        if (numText.isEmpty() || type == null || priceText.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        int roomNum;
        try {
            roomNum = Integer.parseInt(numText);
            if (roomNum <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Room number must be a positive integer");
            return;
        }

        if (dataService.isRoomNumberExists(roomNum)) {
            showError("Room " + roomNum + " already exists");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Price must be a positive number");
            return;
        }

        dataService.addRoom(new Room(roomNum, type, price));
        showSuccess("Room " + roomNum + " added successfully!");
        clearForm();
    }

    private void showError(String msg) {
        statusLabel.getStyleClass().removeAll("success-message");
        statusLabel.getStyleClass().add("error-message");
        statusLabel.setText("⚠  " + msg);
    }

    private void showSuccess(String msg) {
        statusLabel.getStyleClass().removeAll("error-message");
        statusLabel.getStyleClass().add("success-message");
        statusLabel.setText("✓  " + msg);
    }

    private void clearForm() {
        roomNumberInput.clear();
        roomTypeInput.setValue(null);
        priceInput.clear();
    }
}
