module com.hotel {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens com.hotel to javafx.fxml;
    opens com.hotel.model to javafx.base, com.google.gson;
    opens com.hotel.service to com.google.gson;
    opens com.hotel.ui to javafx.fxml;

    exports com.hotel;
    exports com.hotel.model;
    exports com.hotel.service;
    exports com.hotel.ui;
}
