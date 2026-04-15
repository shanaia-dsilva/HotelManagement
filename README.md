# 🏨 Grand Hotel Manager

A modern, premium **Hotel Management System** built with **JavaFX** — featuring a beautiful dark/light themed UI with sidebar navigation, real-time analytics dashboard, and complete room/booking/customer management.

---

## ✨ Features

### 📊 Dashboard
- Real-time metrics: Total rooms, available, occupied, active bookings
- Revenue tracking & occupancy rate
- Welcome panel with quick tips

### 🏠 Room Management
- Add rooms dynamically with auto-fill pricing
- Search rooms by number or type
- Filter: All / Available / Occupied / Single / Double / Deluxe
- Color-coded status indicators (🟢 Available / 🔴 Occupied)
- Sortable table columns

### 📅 Booking & Checkout
- Book rooms with customer details and validation
- Auto-fill room type and price on selection
- 10-digit phone number validation
- Booking confirmation popup with summary
- Inline checkout buttons with cost calculation
- Duration tracking (check-in to check-out)
- Filter bookings by status (Active / Checked Out)

### 👤 Customer Records
- Auto-managed customer list (added on booking, removed on checkout)
- Structured table view with all guest details

### 🎨 Premium UI
- **Dark / Light mode** toggle
- Modern sidebar navigation
- Gradient buttons with hover effects
- Glass-morphism styled cards
- Smooth color transitions
- Custom-styled tables and forms

### 💾 Data Persistence
- Auto-saves to JSON files (`hotel_data/` directory)
- Persists between application restarts

---

## 🚀 How to Run

### Prerequisites
- **Java 17+** installed
- No Maven installation needed (Maven Wrapper included)

### Run the Application
```bash
cd hotel-management-system
./mvnw javafx:run
```

### Build Only
```bash
./mvnw clean compile
```

---

## 📁 Project Structure

```
hotel-management-system/
├── pom.xml                          # Maven configuration
├── mvnw                             # Maven Wrapper script
├── src/main/java/
│   ├── module-info.java             # Java module descriptor
│   └── com/hotel/
│       ├── HotelManagementApp.java  # Main application entry point
│       ├── model/
│       │   ├── Room.java            # Room entity
│       │   ├── Customer.java        # Customer entity
│       │   └── Booking.java         # Booking entity
│       ├── service/
│       │   └── HotelDataService.java # Business logic & persistence
│       └── ui/
│           ├── DashboardView.java   # Analytics dashboard
│           ├── RoomView.java        # Room management view
│           ├── BookingView.java     # Booking & checkout view
│           └── CustomerView.java    # Customer records view
├── src/main/resources/
│   └── com/hotel/
│       └── styles.css               # Premium CSS theme
└── hotel_data/                      # Auto-generated data files
    ├── rooms.json
    ├── customers.json
    └── bookings.json
```

---

## 🛠 Technology Stack
- **Java 17** — Core language
- **JavaFX 21** — GUI framework
- **Gson 2.10** — JSON serialization for file persistence
- **Maven** — Build tool (via wrapper)

---

## 📋 Usage Guide

1. **Dashboard** — View hotel statistics at a glance
2. **Rooms** — Add new rooms, search/filter existing ones
3. **Bookings** — Create new bookings (selects from available rooms only), checkout guests
4. **Customers** — View all currently checked-in guests

> 💡 All data automatically saves to `hotel_data/` folder and loads on next startup.
