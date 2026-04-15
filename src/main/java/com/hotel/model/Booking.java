package com.hotel.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

/**
 * Represents a booking record linking a customer to a room.
 * Tracks check-in and check-out timestamps for duration calculations.
 */
public class Booking {

    public enum BookingStatus {
        ACTIVE("Active"), CHECKED_OUT("Checked Out");

        private final String displayName;

        BookingStatus(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final IntegerProperty bookingId;
    private final IntegerProperty customerId;
    private final StringProperty customerName;
    private final IntegerProperty roomNumber;
    private final StringProperty roomType;
    private final DoubleProperty pricePerDay;
    private final ObjectProperty<LocalDateTime> checkInTime;
    private final ObjectProperty<LocalDateTime> checkOutTime;
    private final ObjectProperty<BookingStatus> status;

    private static int bookingCounter = 5000;

    public Booking(int customerId, String customerName, int roomNumber,
                   String roomType, double pricePerDay) {
        this.bookingId = new SimpleIntegerProperty(++bookingCounter);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.customerName = new SimpleStringProperty(customerName);
        this.roomNumber = new SimpleIntegerProperty(roomNumber);
        this.roomType = new SimpleStringProperty(roomType);
        this.pricePerDay = new SimpleDoubleProperty(pricePerDay);
        this.checkInTime = new SimpleObjectProperty<>(LocalDateTime.now());
        this.checkOutTime = new SimpleObjectProperty<>(null);
        this.status = new SimpleObjectProperty<>(BookingStatus.ACTIVE);
    }

    public Booking(int bookingId, int customerId, String customerName, int roomNumber,
                   String roomType, double pricePerDay, LocalDateTime checkInTime,
                   LocalDateTime checkOutTime, BookingStatus status) {
        this.bookingId = new SimpleIntegerProperty(bookingId);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.customerName = new SimpleStringProperty(customerName);
        this.roomNumber = new SimpleIntegerProperty(roomNumber);
        this.roomType = new SimpleStringProperty(roomType);
        this.pricePerDay = new SimpleDoubleProperty(pricePerDay);
        this.checkInTime = new SimpleObjectProperty<>(checkInTime);
        this.checkOutTime = new SimpleObjectProperty<>(checkOutTime);
        this.status = new SimpleObjectProperty<>(status);
        if (bookingId > bookingCounter) {
            bookingCounter = bookingId;
        }
    }

    // --- Booking ID ---
    public int getBookingId() { return bookingId.get(); }
    public IntegerProperty bookingIdProperty() { return bookingId; }

    // --- Customer ID ---
    public int getCustomerId() { return customerId.get(); }
    public IntegerProperty customerIdProperty() { return customerId; }

    // --- Customer Name ---
    public String getCustomerName() { return customerName.get(); }
    public StringProperty customerNameProperty() { return customerName; }

    // --- Room Number ---
    public int getRoomNumber() { return roomNumber.get(); }
    public IntegerProperty roomNumberProperty() { return roomNumber; }

    // --- Room Type ---
    public String getRoomType() { return roomType.get(); }
    public StringProperty roomTypeProperty() { return roomType; }

    // --- Price Per Day ---
    public double getPricePerDay() { return pricePerDay.get(); }
    public DoubleProperty pricePerDayProperty() { return pricePerDay; }

    // --- Check-in Time ---
    public LocalDateTime getCheckInTime() { return checkInTime.get(); }
    public ObjectProperty<LocalDateTime> checkInTimeProperty() { return checkInTime; }
    public String getCheckInFormatted() {
        return checkInTime.get() != null ? checkInTime.get().format(FORMATTER) : "";
    }

    // --- Check-out Time ---
    public LocalDateTime getCheckOutTime() { return checkOutTime.get(); }
    public void setCheckOutTime(LocalDateTime value) { checkOutTime.set(value); }
    public ObjectProperty<LocalDateTime> checkOutTimeProperty() { return checkOutTime; }
    public String getCheckOutFormatted() {
        return checkOutTime.get() != null ? checkOutTime.get().format(FORMATTER) : "—";
    }

    // --- Status ---
    public BookingStatus getStatus() { return status.get(); }
    public void setStatus(BookingStatus value) { status.set(value); }
    public ObjectProperty<BookingStatus> statusProperty() { return status; }

    // --- Duration ---
    public String getDuration() {
        if (checkInTime.get() == null) return "—";
        LocalDateTime end = checkOutTime.get() != null ? checkOutTime.get() : LocalDateTime.now();
        Duration duration = Duration.between(checkInTime.get(), end);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }

    // --- Total Cost ---
    public double getTotalCost() {
        if (checkInTime.get() == null) return 0;
        LocalDateTime end = checkOutTime.get() != null ? checkOutTime.get() : LocalDateTime.now();
        long days = Duration.between(checkInTime.get(), end).toDays();
        return Math.max(1, days) * getPricePerDay();
    }
}
