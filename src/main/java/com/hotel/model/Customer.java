package com.hotel.model;

import javafx.beans.property.*;

/**
 * Represents a hotel customer with their personal and booking details.
 * Uses JavaFX properties for direct TableView binding.
 */
public class Customer {

    private final IntegerProperty customerId;
    private final StringProperty name;
    private final StringProperty contactNumber;
    private final IntegerProperty roomNumber;

    private static int idCounter = 1000;

    public Customer(String name, String contactNumber, int roomNumber) {
        this.customerId = new SimpleIntegerProperty(++idCounter);
        this.name = new SimpleStringProperty(name);
        this.contactNumber = new SimpleStringProperty(contactNumber);
        this.roomNumber = new SimpleIntegerProperty(roomNumber);
    }

    public Customer(int customerId, String name, String contactNumber, int roomNumber) {
        this.customerId = new SimpleIntegerProperty(customerId);
        this.name = new SimpleStringProperty(name);
        this.contactNumber = new SimpleStringProperty(contactNumber);
        this.roomNumber = new SimpleIntegerProperty(roomNumber);
        if (customerId > idCounter) {
            idCounter = customerId;
        }
    }

    // --- Customer ID ---
    public int getCustomerId() { return customerId.get(); }
    public IntegerProperty customerIdProperty() { return customerId; }

    // --- Name ---
    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    // --- Contact Number ---
    public String getContactNumber() { return contactNumber.get(); }
    public void setContactNumber(String value) { contactNumber.set(value); }
    public StringProperty contactNumberProperty() { return contactNumber; }

    // --- Room Number ---
    public int getRoomNumber() { return roomNumber.get(); }
    public void setRoomNumber(int value) { roomNumber.set(value); }
    public IntegerProperty roomNumberProperty() { return roomNumber; }

    @Override
    public String toString() {
        return getName() + " (ID: " + getCustomerId() + ")";
    }
}
