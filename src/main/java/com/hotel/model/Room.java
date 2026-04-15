package com.hotel.model;

import javafx.beans.property.*;

/**
 * Represents a hotel room with its details and availability status.
 * Uses JavaFX properties for direct TableView binding.
 */
public class Room {

    public enum RoomType {
        SINGLE("Single"), DOUBLE("Double"), DELUXE("Deluxe");

        private final String displayName;

        RoomType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private final IntegerProperty roomNumber;
    private final ObjectProperty<RoomType> roomType;
    private final DoubleProperty pricePerDay;
    private final BooleanProperty available;

    public Room(int roomNumber, RoomType roomType, double pricePerDay) {
        this.roomNumber = new SimpleIntegerProperty(roomNumber);
        this.roomType = new SimpleObjectProperty<>(roomType);
        this.pricePerDay = new SimpleDoubleProperty(pricePerDay);
        this.available = new SimpleBooleanProperty(true);
    }

    public Room(int roomNumber, RoomType roomType, double pricePerDay, boolean available) {
        this.roomNumber = new SimpleIntegerProperty(roomNumber);
        this.roomType = new SimpleObjectProperty<>(roomType);
        this.pricePerDay = new SimpleDoubleProperty(pricePerDay);
        this.available = new SimpleBooleanProperty(available);
    }

    // --- Room Number ---
    public int getRoomNumber() { return roomNumber.get(); }
    public void setRoomNumber(int value) { roomNumber.set(value); }
    public IntegerProperty roomNumberProperty() { return roomNumber; }

    // --- Room Type ---
    public RoomType getRoomType() { return roomType.get(); }
    public void setRoomType(RoomType value) { roomType.set(value); }
    public ObjectProperty<RoomType> roomTypeProperty() { return roomType; }

    // --- Price Per Day ---
    public double getPricePerDay() { return pricePerDay.get(); }
    public void setPricePerDay(double value) { pricePerDay.set(value); }
    public DoubleProperty pricePerDayProperty() { return pricePerDay; }

    // --- Available ---
    public boolean isAvailable() { return available.get(); }
    public void setAvailable(boolean value) { available.set(value); }
    public BooleanProperty availableProperty() { return available; }

    @Override
    public String toString() {
        return "Room " + getRoomNumber() + " (" + getRoomType() + ")";
    }
}
