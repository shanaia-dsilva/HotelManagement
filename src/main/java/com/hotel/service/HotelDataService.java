package com.hotel.service;

import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import com.google.gson.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Central data service managing all hotel data (rooms, customers, bookings).
 * Provides in-memory storage with JSON file persistence.
 */
public class HotelDataService {

    private static final String DATA_DIR = "hotel_data";
    private static final String ROOMS_FILE = DATA_DIR + "/rooms.json";
    private static final String CUSTOMERS_FILE = DATA_DIR + "/customers.json";
    private static final String BOOKINGS_FILE = DATA_DIR + "/bookings.json";
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private final ObservableList<Customer> customers = FXCollections.observableArrayList();
    private final ObservableList<Booking> bookings = FXCollections.observableArrayList();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public HotelDataService() {
        loadData();
        if (rooms.isEmpty()) {
            addSampleData();
        }
    }

    // ========================= ROOM OPERATIONS =========================

    public ObservableList<Room> getRooms() { return rooms; }

    public void addRoom(Room room) {
        rooms.add(room);
        saveData();
    }

    public boolean isRoomNumberExists(int roomNumber) {
        return rooms.stream().anyMatch(r -> r.getRoomNumber() == roomNumber);
    }

    public Room findRoom(int roomNumber) {
        return rooms.stream()
                .filter(r -> r.getRoomNumber() == roomNumber)
                .findFirst().orElse(null);
    }

    public ObservableList<Room> getAvailableRooms() {
        return rooms.filtered(Room::isAvailable);
    }

    public long getOccupiedRoomCount() {
        return rooms.stream().filter(r -> !r.isAvailable()).count();
    }

    public long getAvailableRoomCount() {
        return rooms.stream().filter(Room::isAvailable).count();
    }

    public long getTotalRoomCount() {
        return rooms.size();
    }

    public double getTotalRevenue() {
        return bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CHECKED_OUT)
                .mapToDouble(Booking::getTotalCost)
                .sum();
    }

    public long getActiveBookingCount() {
        return bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.ACTIVE)
                .count();
    }

    // ========================= CUSTOMER OPERATIONS =========================

    public ObservableList<Customer> getCustomers() { return customers; }

    public void addCustomer(Customer customer) {
        customers.add(customer);
        saveData();
    }

    // ========================= BOOKING OPERATIONS =========================

    public ObservableList<Booking> getBookings() { return bookings; }

    /**
     * Books a room for a customer. Creates both a Customer and Booking record,
     * and marks the room as unavailable.
     */
    public Booking bookRoom(String customerName, String contactNumber, int roomNumber) {
        Room room = findRoom(roomNumber);
        if (room == null || !room.isAvailable()) {
            return null;
        }

        Customer customer = new Customer(customerName, contactNumber, roomNumber);
        customers.add(customer);

        Booking booking = new Booking(
                customer.getCustomerId(),
                customerName,
                roomNumber,
                room.getRoomType().toString(),
                room.getPricePerDay()
        );
        bookings.add(booking);

        room.setAvailable(false);
        saveData();
        return booking;
    }

    /**
     * Checks out a booking, releasing the room and recording checkout time.
     */
    public boolean checkout(Booking booking) {
        if (booking == null || booking.getStatus() == Booking.BookingStatus.CHECKED_OUT) {
            return false;
        }

        booking.setCheckOutTime(LocalDateTime.now());
        booking.setStatus(Booking.BookingStatus.CHECKED_OUT);

        Room room = findRoom(booking.getRoomNumber());
        if (room != null) {
            room.setAvailable(true);
        }

        // Remove customer from active list
        customers.removeIf(c -> c.getCustomerId() == booking.getCustomerId());

        saveData();
        return true;
    }

    // ========================= DATA PERSISTENCE =========================

    private void addSampleData() {
        rooms.add(new Room(101, Room.RoomType.SINGLE, 2500.0));
        rooms.add(new Room(102, Room.RoomType.SINGLE, 2500.0));
        rooms.add(new Room(103, Room.RoomType.DOUBLE, 4000.0));
        rooms.add(new Room(104, Room.RoomType.DOUBLE, 4000.0));
        rooms.add(new Room(201, Room.RoomType.DELUXE, 6500.0));
        rooms.add(new Room(202, Room.RoomType.DELUXE, 6500.0));
        rooms.add(new Room(203, Room.RoomType.SINGLE, 2800.0));
        rooms.add(new Room(204, Room.RoomType.DOUBLE, 4200.0));
        rooms.add(new Room(301, Room.RoomType.DELUXE, 7500.0));
        rooms.add(new Room(302, Room.RoomType.DOUBLE, 4500.0));
        saveData();
    }

    public void saveData() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));

            // Save rooms
            JsonArray roomsArr = new JsonArray();
            for (Room r : rooms) {
                JsonObject obj = new JsonObject();
                obj.addProperty("roomNumber", r.getRoomNumber());
                obj.addProperty("roomType", r.getRoomType().name());
                obj.addProperty("pricePerDay", r.getPricePerDay());
                obj.addProperty("available", r.isAvailable());
                roomsArr.add(obj);
            }
            Files.writeString(Paths.get(ROOMS_FILE), gson.toJson(roomsArr));

            // Save customers
            JsonArray custsArr = new JsonArray();
            for (Customer c : customers) {
                JsonObject obj = new JsonObject();
                obj.addProperty("customerId", c.getCustomerId());
                obj.addProperty("name", c.getName());
                obj.addProperty("contactNumber", c.getContactNumber());
                obj.addProperty("roomNumber", c.getRoomNumber());
                custsArr.add(obj);
            }
            Files.writeString(Paths.get(CUSTOMERS_FILE), gson.toJson(custsArr));

            // Save bookings
            JsonArray bookArr = new JsonArray();
            for (Booking b : bookings) {
                JsonObject obj = new JsonObject();
                obj.addProperty("bookingId", b.getBookingId());
                obj.addProperty("customerId", b.getCustomerId());
                obj.addProperty("customerName", b.getCustomerName());
                obj.addProperty("roomNumber", b.getRoomNumber());
                obj.addProperty("roomType", b.getRoomType());
                obj.addProperty("pricePerDay", b.getPricePerDay());
                obj.addProperty("checkInTime", b.getCheckInTime() != null ? b.getCheckInTime().format(DT_FMT) : null);
                obj.addProperty("checkOutTime", b.getCheckOutTime() != null ? b.getCheckOutTime().format(DT_FMT) : null);
                obj.addProperty("status", b.getStatus().name());
                bookArr.add(obj);
            }
            Files.writeString(Paths.get(BOOKINGS_FILE), gson.toJson(bookArr));

        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private void loadData() {
        try {
            // Load rooms
            Path roomsPath = Paths.get(ROOMS_FILE);
            if (Files.exists(roomsPath)) {
                String json = Files.readString(roomsPath);
                JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
                for (JsonElement el : arr) {
                    JsonObject obj = el.getAsJsonObject();
                    rooms.add(new Room(
                            obj.get("roomNumber").getAsInt(),
                            Room.RoomType.valueOf(obj.get("roomType").getAsString()),
                            obj.get("pricePerDay").getAsDouble(),
                            obj.get("available").getAsBoolean()
                    ));
                }
            }

            // Load customers
            Path custsPath = Paths.get(CUSTOMERS_FILE);
            if (Files.exists(custsPath)) {
                String json = Files.readString(custsPath);
                JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
                for (JsonElement el : arr) {
                    JsonObject obj = el.getAsJsonObject();
                    customers.add(new Customer(
                            obj.get("customerId").getAsInt(),
                            obj.get("name").getAsString(),
                            obj.get("contactNumber").getAsString(),
                            obj.get("roomNumber").getAsInt()
                    ));
                }
            }

            // Load bookings
            Path bookPath = Paths.get(BOOKINGS_FILE);
            if (Files.exists(bookPath)) {
                String json = Files.readString(bookPath);
                JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
                for (JsonElement el : arr) {
                    JsonObject obj = el.getAsJsonObject();
                    LocalDateTime checkIn = obj.has("checkInTime") && !obj.get("checkInTime").isJsonNull()
                            ? LocalDateTime.parse(obj.get("checkInTime").getAsString(), DT_FMT) : null;
                    LocalDateTime checkOut = obj.has("checkOutTime") && !obj.get("checkOutTime").isJsonNull()
                            ? LocalDateTime.parse(obj.get("checkOutTime").getAsString(), DT_FMT) : null;
                    bookings.add(new Booking(
                            obj.get("bookingId").getAsInt(),
                            obj.get("customerId").getAsInt(),
                            obj.get("customerName").getAsString(),
                            obj.get("roomNumber").getAsInt(),
                            obj.get("roomType").getAsString(),
                            obj.get("pricePerDay").getAsDouble(),
                            checkIn, checkOut,
                            Booking.BookingStatus.valueOf(obj.get("status").getAsString())
                    ));
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
}
