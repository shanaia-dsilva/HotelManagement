package com.hotel.ui;

import com.hotel.model.Room;
import com.hotel.service.HotelDataService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Room management view — displays room table with search/filter,
 * and a GridPane-based form to add new rooms.
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
        table.setPlaceholder(new Label("No rooms found. Add a room or adjust your filters."));

        TableColumn<Room, Number> colNum = new TableColumn<>("Room #");
        colNum.setCellValueFactory(cd -> cd.getValue().roomNumberProperty());
        colNum.setPrefWidth(100);
        colNum.setSortable(true);

        TableColumn<Room, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getRoomType().toString()));
        colType.setPrefWidth(120);
        colType.setSortable(true);

        TableColumn<Room, Number> colPrice = new TableColumn<>("Price / Day");
        colPrice.setCellValueFactory(cd -> cd.getValue().pricePerDayProperty());
        colPrice.setPrefWidth(130);
        colPrice.setSortable(true);
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("₹ %.0f", item.doubleValue()));
                }
            }
        });

        TableColumn<Room, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().isAvailable() ? "Available" : "Occupied"));
        colStatus.setPrefWidth(140);
        colStatus.setSortable(true);
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
                        setStyle("-fx-text-fill: #4ade80; -fx-font-weight: 600;");
                    } else {
                        dot.setFill(Color.web("#f87171"));
                        setText("  Occupied");
                        setStyle("-fx-text-fill: #f87171; -fx-font-weight: 600;");
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

        // ---- Add Room Form (GridPane layout as required) ----
        Label formHeader = new Label("➕  Add New Room");
        formHeader.getStyleClass().add("form-header");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(14);

        Label lblRoomNum = new Label("Room Number:");
        lblRoomNum.getStyleClass().add("form-label");
        roomNumberInput = new TextField();
        roomNumberInput.setPromptText("e.g. 105");
        roomNumberInput.getStyleClass().add("form-input");

        Label lblRoomType = new Label("Room Type:");
        lblRoomType.getStyleClass().add("form-label");
        roomTypeInput = new ComboBox<>(FXCollections.observableArrayList(Room.RoomType.values()));
        roomTypeInput.setPromptText("Select Type");
        roomTypeInput.getStyleClass().add("form-input");
        roomTypeInput.setMaxWidth(Double.MAX_VALUE);

        Label lblPrice = new Label("Price / Day:");
        lblPrice.getStyleClass().add("form-label");
        priceInput = new TextField();
        priceInput.setPromptText("₹");
        priceInput.getStyleClass().add("form-input");

        // Add to grid: col 0 = labels, col 1 = inputs
        formGrid.add(lblRoomNum, 0, 0);
        formGrid.add(roomNumberInput, 1, 0);
        formGrid.add(lblRoomType, 0, 1);
        formGrid.add(roomTypeInput, 1, 1);
        formGrid.add(lblPrice, 0, 2);
        formGrid.add(priceInput, 1, 2);

        // Make inputs fill available width
        ColumnConstraints col0 = new ColumnConstraints();
        col0.setMinWidth(90);
        col0.setHalignment(HPos.RIGHT);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        col1.setFillWidth(true);
        formGrid.getColumnConstraints().addAll(col0, col1);

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

        Button addBtn = new Button("➕  Add Room");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> handleAddRoom());

        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-message");
        statusLabel.setWrapText(true);

        VBox formBox = new VBox(14, formHeader, formGrid, addBtn, statusLabel);
        formBox.getStyleClass().add("form-card");
        formBox.setPadding(new Insets(20));
        formBox.setPrefWidth(300);
        formBox.setMinWidth(280);

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

        // Validation
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

        // Add room
        dataService.addRoom(new Room(roomNum, type, price));

        // Show success alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Room Added");
        alert.setHeaderText(null);
        alert.setContentText("Room " + roomNum + " (" + type + ") added successfully at ₹" + String.format("%.0f", price) + "/day!");
        alert.show();

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
