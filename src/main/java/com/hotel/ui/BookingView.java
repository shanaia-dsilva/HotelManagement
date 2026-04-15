package com.hotel.ui;

import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.service.HotelDataService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Booking management view — handles creating bookings and checking out guests.
 * Uses GridPane for the booking form as per GUI requirements.
 */
public class BookingView {

    private final HotelDataService dataService;
    private final VBox root;
    private final Runnable refreshDashboard;

    // Form fields
    private TextField customerNameInput;
    private TextField contactInput;
    private ComboBox<Room> roomCombo;
    private Label roomTypeLabel;
    private Label roomPriceLabel;
    private Label validationLabel;
    private Button bookBtn;

    // Table
    private TableView<Booking> table;
    private ComboBox<String> statusFilter;

    public BookingView(HotelDataService dataService, Runnable refreshDashboard) {
        this.dataService = dataService;
        this.refreshDashboard = refreshDashboard;
        this.root = buildView();
    }

    public VBox getView() { return root; }

    @SuppressWarnings("unchecked")
    private VBox buildView() {
        VBox container = new VBox(20);
        container.getStyleClass().add("content-area");
        container.setPadding(new Insets(30));

        // ---- Header ----
        Label header = new Label("📅  Bookings & Checkout");
        header.getStyleClass().add("section-header");

        Label subtitle = new Label("Create bookings and manage guest checkouts");
        subtitle.getStyleClass().add("section-subtitle");

        VBox headerBox = new VBox(4, header, subtitle);

        // ---- Booking Form (GridPane as required) ----
        Label formHeader = new Label("📝  New Booking");
        formHeader.getStyleClass().add("form-header");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(14);

        // Row 0: Customer Name
        Label lblName = new Label("Guest Name:");
        lblName.getStyleClass().add("form-label");
        customerNameInput = new TextField();
        customerNameInput.setPromptText("Enter full name");
        customerNameInput.getStyleClass().add("form-input");

        // Row 1: Contact Number
        Label lblContact = new Label("Contact #:");
        lblContact.getStyleClass().add("form-label");
        contactInput = new TextField();
        contactInput.setPromptText("10-digit number");
        contactInput.getStyleClass().add("form-input");

        // Row 2: Room Selection
        Label lblRoom = new Label("Room:");
        lblRoom.getStyleClass().add("form-label");
        roomCombo = new ComboBox<>();
        roomCombo.setPromptText("Select Available Room");
        roomCombo.getStyleClass().add("form-input");
        roomCombo.setMaxWidth(Double.MAX_VALUE);
        refreshAvailableRooms();

        formGrid.add(lblName, 0, 0);
        formGrid.add(customerNameInput, 1, 0);
        formGrid.add(lblContact, 0, 1);
        formGrid.add(contactInput, 1, 1);
        formGrid.add(lblRoom, 0, 2);
        formGrid.add(roomCombo, 1, 2);

        ColumnConstraints col0 = new ColumnConstraints();
        col0.setMinWidth(85);
        col0.setHalignment(HPos.RIGHT);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        col1.setFillWidth(true);
        formGrid.getColumnConstraints().addAll(col0, col1);

        // Real-time validation
        contactInput.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        customerNameInput.textProperty().addListener((obs, oldVal, newVal) -> validateForm());

        // Auto-fill room details
        roomTypeLabel = new Label("Type: —");
        roomTypeLabel.getStyleClass().add("detail-label");
        roomPriceLabel = new Label("Rate: —");
        roomPriceLabel.getStyleClass().add("detail-label");

        roomCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                roomTypeLabel.setText("Type: " + newVal.getRoomType());
                roomPriceLabel.setText(String.format("Rate: ₹ %.0f/day", newVal.getPricePerDay()));
            } else {
                roomTypeLabel.setText("Type: —");
                roomPriceLabel.setText("Rate: —");
            }
            validateForm();
        });

        HBox roomDetails = new HBox(16, roomTypeLabel, roomPriceLabel);
        roomDetails.getStyleClass().add("room-detail-bar");
        roomDetails.setPadding(new Insets(2, 0, 2, 0));

        validationLabel = new Label();
        validationLabel.getStyleClass().add("status-message");
        validationLabel.setWrapText(true);

        bookBtn = new Button("📋  Book Room");
        bookBtn.getStyleClass().add("btn-primary");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setDisable(true);
        bookBtn.setOnAction(e -> handleBooking());

        VBox formBox = new VBox(14, formHeader, formGrid, roomDetails, bookBtn, validationLabel);
        formBox.getStyleClass().add("form-card");
        formBox.setPadding(new Insets(20));
        formBox.setPrefWidth(340);
        formBox.setMinWidth(310);

        // ---- Bookings Table ----
        statusFilter = new ComboBox<>(FXCollections.observableArrayList(
                "All Bookings", "Active", "Checked Out"
        ));
        statusFilter.setValue("All Bookings");
        statusFilter.getStyleClass().add("filter-combo");

        Button refreshBtn = new Button("🔄  Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> refreshAvailableRooms());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(12, statusFilter, spacer, refreshBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setPlaceholder(new Label("No bookings yet. Create your first booking!"));

        TableColumn<Booking, Number> colId = new TableColumn<>("Booking #");
        colId.setCellValueFactory(cd -> cd.getValue().bookingIdProperty());
        colId.setPrefWidth(90);
        colId.setSortable(true);

        TableColumn<Booking, String> colName = new TableColumn<>("Customer");
        colName.setCellValueFactory(cd -> cd.getValue().customerNameProperty());
        colName.setPrefWidth(130);
        colName.setSortable(true);

        TableColumn<Booking, Number> colRoom = new TableColumn<>("Room #");
        colRoom.setCellValueFactory(cd -> cd.getValue().roomNumberProperty());
        colRoom.setPrefWidth(75);

        TableColumn<Booking, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(cd -> cd.getValue().roomTypeProperty());
        colType.setPrefWidth(75);

        TableColumn<Booking, String> colCheckIn = new TableColumn<>("Check-In");
        colCheckIn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCheckInFormatted()));
        colCheckIn.setPrefWidth(125);

        TableColumn<Booking, String> colCheckOut = new TableColumn<>("Check-Out");
        colCheckOut.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCheckOutFormatted()));
        colCheckOut.setPrefWidth(125);

        TableColumn<Booking, String> colDuration = new TableColumn<>("Duration");
        colDuration.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDuration()));
        colDuration.setPrefWidth(85);

        TableColumn<Booking, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus().toString()));
        colStatus.setPrefWidth(90);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Active".equals(item)) {
                        setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                    }
                }
            }
        });

        TableColumn<Booking, Void> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(110);
        colAction.setSortable(false);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button checkoutBtn = new Button("🚪 Checkout");
            {
                checkoutBtn.getStyleClass().add("btn-danger");
                checkoutBtn.setOnAction(e -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    handleCheckout(booking);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Booking booking = getTableView().getItems().get(getIndex());
                    if (booking.getStatus() == Booking.BookingStatus.ACTIVE) {
                        setGraphic(checkoutBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        table.getColumns().addAll(colId, colName, colRoom, colType, colCheckIn, colCheckOut, colDuration, colStatus, colAction);

        // Filtered data
        FilteredList<Booking> filteredBookings = new FilteredList<>(dataService.getBookings(), p -> true);
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredBookings.setPredicate(booking -> switch (newVal) {
                case "Active" -> booking.getStatus() == Booking.BookingStatus.ACTIVE;
                case "Checked Out" -> booking.getStatus() == Booking.BookingStatus.CHECKED_OUT;
                default -> true;
            });
        });
        table.setItems(filteredBookings);

        VBox tableSection = new VBox(12, toolbar, table);
        HBox.setHgrow(tableSection, Priority.ALWAYS);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ---- Main Layout ----
        HBox mainContent = new HBox(20, tableSection, formBox);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        container.getChildren().addAll(headerBox, mainContent);
        return container;
    }

    private void validateForm() {
        validationLabel.getStyleClass().removeAll("error-message", "success-message");

        String name = customerNameInput.getText().trim();
        String contact = contactInput.getText().trim();
        Room room = roomCombo.getValue();

        boolean valid = true;
        StringBuilder msg = new StringBuilder();

        if (name.isEmpty()) {
            valid = false;
        }

        if (!contact.isEmpty() && !contact.matches("\\d{10}")) {
            msg.append("⚠  Contact must be exactly 10 digits");
            validationLabel.getStyleClass().add("error-message");
            valid = false;
        } else if (contact.isEmpty()) {
            valid = false;
        }

        if (room == null) {
            valid = false;
        }

        validationLabel.setText(msg.toString());
        bookBtn.setDisable(!valid);
    }

    private void refreshAvailableRooms() {
        roomCombo.setItems(FXCollections.observableArrayList(dataService.getAvailableRooms()));
    }

    private void handleBooking() {
        String name = customerNameInput.getText().trim();
        String contact = contactInput.getText().trim();
        Room selectedRoom = roomCombo.getValue();

        if (selectedRoom == null) return;

        // Confirmation dialog with booking summary
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Booking");
        confirm.setHeaderText("📋  Booking Summary");
        confirm.setContentText(
                "Guest Name:  " + name + "\n" +
                "Contact:  " + contact + "\n" +
                "Room:  " + selectedRoom.getRoomNumber() + " (" + selectedRoom.getRoomType() + ")\n" +
                "Rate:  ₹ " + String.format("%.0f", selectedRoom.getPricePerDay()) + " / day\n\n" +
                "Proceed with booking?"
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Booking booking = dataService.bookRoom(name, contact, selectedRoom.getRoomNumber());
                if (booking != null) {
                    showSuccess("✓  Booking #" + booking.getBookingId() + " created for " + name + "!");
                    clearForm();
                    refreshAvailableRooms();
                    refreshDashboard.run();
                } else {
                    showError("Booking failed. Room may no longer be available.");
                }
            }
        });
    }

    private void handleCheckout(Booking booking) {
        // Confirmation popup before checkout
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Checkout");
        confirm.setHeaderText("🚪  Checkout Confirmation");
        confirm.setContentText(
                "Booking #" + booking.getBookingId() + "\n" +
                "Guest:  " + booking.getCustomerName() + "\n" +
                "Room:  " + booking.getRoomNumber() + " (" + booking.getRoomType() + ")\n" +
                "Stay Duration:  " + booking.getDuration() + "\n" +
                "Estimated Total:  ₹ " + String.format("%.0f", booking.getTotalCost()) + "\n\n" +
                "Confirm checkout?"
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (dataService.checkout(booking)) {
                    // Show success info
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Checkout Complete");
                    info.setHeaderText(null);
                    info.setContentText("Booking #" + booking.getBookingId() + " checked out.\n" +
                            "Total: ₹ " + String.format("%.0f", booking.getTotalCost()));
                    info.show();

                    showSuccess("Checkout successful for Booking #" + booking.getBookingId());
                    table.refresh();
                    refreshAvailableRooms();
                    refreshDashboard.run();
                }
            }
        });
    }

    private void showError(String msg) {
        validationLabel.getStyleClass().removeAll("success-message");
        validationLabel.getStyleClass().add("error-message");
        validationLabel.setText("⚠  " + msg);
    }

    private void showSuccess(String msg) {
        validationLabel.getStyleClass().removeAll("error-message");
        validationLabel.getStyleClass().add("success-message");
        validationLabel.setText(msg);
    }

    private void clearForm() {
        customerNameInput.clear();
        contactInput.clear();
        roomCombo.setValue(null);
        roomTypeLabel.setText("Type: —");
        roomPriceLabel.setText("Rate: —");
        bookBtn.setDisable(true);
    }
}
