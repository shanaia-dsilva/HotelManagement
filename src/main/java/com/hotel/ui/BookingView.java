package com.hotel.ui;

import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.service.HotelDataService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Booking management view — handles creating bookings and checking out guests.
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

        // ---- Booking Form ----
        Label formHeader = new Label("📝  New Booking");
        formHeader.getStyleClass().add("form-header");

        customerNameInput = new TextField();
        customerNameInput.setPromptText("Customer Name");
        customerNameInput.getStyleClass().add("form-input");

        contactInput = new TextField();
        contactInput.setPromptText("Contact Number (10 digits)");
        contactInput.getStyleClass().add("form-input");

        // Real-time validation
        contactInput.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        customerNameInput.textProperty().addListener((obs, oldVal, newVal) -> validateForm());

        roomCombo = new ComboBox<>();
        roomCombo.setPromptText("Select Available Room");
        roomCombo.getStyleClass().add("form-input");
        roomCombo.setMaxWidth(Double.MAX_VALUE);
        refreshAvailableRooms();

        // Auto-fill room details
        roomTypeLabel = new Label("Room Type: —");
        roomTypeLabel.getStyleClass().add("detail-label");
        roomPriceLabel = new Label("Price/Day: —");
        roomPriceLabel.getStyleClass().add("detail-label");

        roomCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                roomTypeLabel.setText("Room Type: " + newVal.getRoomType());
                roomPriceLabel.setText(String.format("Price/Day: ₹ %.0f", newVal.getPricePerDay()));
            } else {
                roomTypeLabel.setText("Room Type: —");
                roomPriceLabel.setText("Price/Day: —");
            }
            validateForm();
        });

        HBox roomDetails = new HBox(20, roomTypeLabel, roomPriceLabel);
        roomDetails.getStyleClass().add("room-detail-bar");

        validationLabel = new Label();
        validationLabel.getStyleClass().add("status-message");

        bookBtn = new Button("📋  Book Room");
        bookBtn.getStyleClass().add("btn-primary");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setDisable(true);
        bookBtn.setOnAction(e -> handleBooking());

        VBox formBox = new VBox(12, formHeader, customerNameInput, contactInput,
                roomCombo, roomDetails, bookBtn, validationLabel);
        formBox.getStyleClass().add("form-card");
        formBox.setPadding(new Insets(20));
        formBox.setPrefWidth(320);
        formBox.setMinWidth(300);

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

        TableColumn<Booking, Number> colId = new TableColumn<>("Booking #");
        colId.setCellValueFactory(cd -> cd.getValue().bookingIdProperty());
        colId.setPrefWidth(90);

        TableColumn<Booking, String> colName = new TableColumn<>("Customer");
        colName.setCellValueFactory(cd -> cd.getValue().customerNameProperty());
        colName.setPrefWidth(140);

        TableColumn<Booking, Number> colRoom = new TableColumn<>("Room #");
        colRoom.setCellValueFactory(cd -> cd.getValue().roomNumberProperty());
        colRoom.setPrefWidth(80);

        TableColumn<Booking, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(cd -> cd.getValue().roomTypeProperty());
        colType.setPrefWidth(80);

        TableColumn<Booking, String> colCheckIn = new TableColumn<>("Check-In");
        colCheckIn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCheckInFormatted()));
        colCheckIn.setPrefWidth(130);

        TableColumn<Booking, String> colCheckOut = new TableColumn<>("Check-Out");
        colCheckOut.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCheckOutFormatted()));
        colCheckOut.setPrefWidth(130);

        TableColumn<Booking, String> colDuration = new TableColumn<>("Duration");
        colDuration.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDuration()));
        colDuration.setPrefWidth(90);

        TableColumn<Booking, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getStatus().toString()));
        colStatus.setPrefWidth(100);
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
                        setStyle("-fx-text-fill: #94a3b8;");
                    }
                }
            }
        });

        TableColumn<Booking, Void> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(120);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button checkoutBtn = new Button("Checkout");
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
            msg.append("⚠  Contact must be 10 digits  ");
            validationLabel.getStyleClass().add("error-message");
            valid = false;
        }

        if (room == null) {
            valid = false;
        }

        validationLabel.setText(msg.toString());
        bookBtn.setDisable(!valid || contact.isEmpty());
    }

    private void refreshAvailableRooms() {
        roomCombo.setItems(FXCollections.observableArrayList(dataService.getAvailableRooms()));
    }

    private void handleBooking() {
        String name = customerNameInput.getText().trim();
        String contact = contactInput.getText().trim();
        Room selectedRoom = roomCombo.getValue();

        if (selectedRoom == null) return;

        // Confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Booking");
        confirm.setHeaderText("Booking Summary");
        confirm.setContentText(
                "Guest: " + name + "\n" +
                "Contact: " + contact + "\n" +
                "Room: " + selectedRoom.getRoomNumber() + " (" + selectedRoom.getRoomType() + ")\n" +
                "Rate: ₹ " + String.format("%.0f", selectedRoom.getPricePerDay()) + " / day\n\n" +
                "Proceed with booking?"
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Booking booking = dataService.bookRoom(name, contact, selectedRoom.getRoomNumber());
                if (booking != null) {
                    showSuccess("Booking #" + booking.getBookingId() + " created!");
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
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Checkout");
        confirm.setHeaderText("Checkout Confirmation");
        confirm.setContentText(
                "Booking #" + booking.getBookingId() + "\n" +
                "Guest: " + booking.getCustomerName() + "\n" +
                "Room: " + booking.getRoomNumber() + "\n" +
                "Duration: " + booking.getDuration() + "\n" +
                "Estimated Total: ₹ " + String.format("%.0f", booking.getTotalCost()) + "\n\n" +
                "Confirm checkout?"
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (dataService.checkout(booking)) {
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
        validationLabel.setText("✓  " + msg);
    }

    private void clearForm() {
        customerNameInput.clear();
        contactInput.clear();
        roomCombo.setValue(null);
        roomTypeLabel.setText("Room Type: —");
        roomPriceLabel.setText("Price/Day: —");
        bookBtn.setDisable(true);
    }
}
