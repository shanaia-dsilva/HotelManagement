package com.hotel.ui;

import com.hotel.model.Customer;
import com.hotel.service.HotelDataService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Customer management view — displays all active customers
 * in a structured table format.
 */
public class CustomerView {

    private final HotelDataService dataService;
    private final VBox root;

    public CustomerView(HotelDataService dataService) {
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
        Label header = new Label("👤  Customer Records");
        header.getStyleClass().add("section-header");

        Label subtitle = new Label("View all currently checked-in guests");
        subtitle.getStyleClass().add("section-subtitle");

        VBox headerBox = new VBox(4, header, subtitle);

        // ---- Info Banner ----
        Label infoBanner = new Label("ℹ  Customers are automatically added during booking and removed after checkout.");
        infoBanner.getStyleClass().add("info-banner");
        infoBanner.setMaxWidth(Double.MAX_VALUE);

        // ---- Table ----
        TableView<Customer> table = new TableView<>();
        table.getStyleClass().add("data-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Customer, Number> colId = new TableColumn<>("Customer ID");
        colId.setCellValueFactory(cd -> cd.getValue().customerIdProperty());
        colId.setPrefWidth(120);

        TableColumn<Customer, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(cd -> cd.getValue().nameProperty());
        colName.setPrefWidth(200);

        TableColumn<Customer, String> colContact = new TableColumn<>("Contact Number");
        colContact.setCellValueFactory(cd -> cd.getValue().contactNumberProperty());
        colContact.setPrefWidth(180);

        TableColumn<Customer, Number> colRoom = new TableColumn<>("Room Number");
        colRoom.setCellValueFactory(cd -> cd.getValue().roomNumberProperty());
        colRoom.setPrefWidth(140);

        table.getColumns().addAll(colId, colName, colContact, colRoom);
        table.setItems(dataService.getCustomers());

        // Empty state placeholder
        table.setPlaceholder(new Label("No active guests. Create a booking to add customers."));

        // ---- Stats ----
        long count = dataService.getCustomers().size();
        Label statsLabel = new Label("📊  Total Active Guests: " + count);
        statsLabel.getStyleClass().add("stats-label");

        container.getChildren().addAll(headerBox, infoBanner, table, statsLabel);
        return container;
    }
}
