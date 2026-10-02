# 🎬 Cinema Express - Cinema Management & Box Office POS System

A high-performance desktop Cinema Management and Point of Sale (POS) system built with **Java Swing**, **JDBC**, and **MySQL**. Designed for modern single-theater and multi-screen cinema operations, Cinema Express provides role-based access for theater administrators and box office counter staff.

---

## 🌟 Key Features

### 🏢 1. Admin Management Panel
- **Executive Dashboard:** Real-time metrics tracking today's box office revenue, ticket counts, screen occupancy rates, and active screenings.
- **Movie Catalog Manager:** Full lifecycle management (Now Showing, Upcoming, Archived) with genres, certificates (U, UA, A), durations, and movie posters.
- **Screen & Seat Layout Designer:**
  - Create and configure screens (Standard, IMAX, Dolby Atmos, 4DX, VIP Lounge).
  - Visual seat layout configuration with tier assignment (**Regular / Silver**, **Premium / Gold**, and **Platinum Recliners**).
  - Physical seats (`screen_seats`) separated cleanly from show-specific seat pricing and availability (`show_seats`).
- **Show Scheduling Engine:**
  - Smart scheduling with automatic show end-time calculation based on movie duration + cleaning buffer.
  - Conflict detection ensuring no overlapping shows on the same screen.
  - Dynamic tiered pricing per show (e.g. customized prices for morning, matinee, and prime-time shows).
- **Staff & Access Control:** Manage counter staff accounts with role-based permissions (`ADMIN`, `STAFF`).
- **Financial & Sales Analytics:** Daily, weekly, and monthly revenue breakdown, movie-wise sales, and audit reports.

---

### 🎟️ 2. Staff Counter Terminal (Box Office POS)
- **Fast 1-Page Counter Workflow:** Optimized for speedy box office operations during peak rush hours:
  $$\text{Movie Selection} \longrightarrow \text{Date \& Show Selection} \longrightarrow \text{Seat Matrix} \longrightarrow \text{Payment} \longrightarrow \text{Ticket Generation}$$
- **Interactive Visual Seating Arrangement:**
  - Real-time seat status colors: **Available** (White/Blue/Gold), **Selected** (Blue), **Booked** (Red cross), and **Blocked** (Grey).
  - Enforces booking window policies: automatically prevents bookings for past shows (grace period allowed up to 30 mins after show start).
- **Customer Lookup & Smart Validation:**
  - Phone-based search to quickly find and reuse existing customer profiles.
  - Strict Indian 10-digit mobile number validation with automatic input field correction.
  - Prevents duplicate customer record creation.
- **Automated Price Calculation Engine:**
  - Monetary calculations strictly use `BigDecimal` to prevent floating-point rounding errors.
  - Real-time itemized seat breakdown with subtotal, discounts, and payable total.
- **Multi-Method Payment Processing:**
  - 💵 **Cash:** Live change calculator (Amount Received vs. Total Payable).
  - 📱 **Dynamic UPI QR Code:**
    - Generates standard, ISO-compliant scannable QR codes using **ZXing**.
    - Pre-configures payee details, exact order amount, unique order transaction reference, and movie note.
    - 100% compatible with **Google Pay**, **PhonePe**, **Paytm**, **BHIM**, and banking apps.
    - Includes an **"Expand QR"** modal for customer-facing display screens.
  - 💳 **Card:** Reference tracking for external POS swipe / card slip authorization.
- **Official Printable Ticket Receipt:**
  - Clean receipt with theater branding, movie info, seat list, order total, and cashier identifier.
  - Generates a **Gate Verification QR Code** for barcode scanner entry validation at theater gates.
  - Direct 1-click receipt printing support (`java.awt.print.PrinterJob`).
- **Today's Bookings & Re-print:** Live counter transaction log for audit and quick ticket reprints.

---

## 🏗️ Architecture & Technical Design

Cinema Express strictly follows standard **Layered DAO + Service Architecture**:

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│     (Java Swing, Custom Components, Theme Manager)     │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                    Service Layer                       │
│    (Business Logic, Transaction Management, Validations)│
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                      DAO Layer                         │
│       (Data Access Objects, Prepared Statements, SQL)  │
└───────────────────────────┬────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│                   Database Engine                      │
│             (MySQL Database: cinema_db)                │
└────────────────────────────────────────────────────────┘
```

### 📂 Project Structure

```
ticket_booking/
├── database/                          # MySQL DDL Schema
│   └── schema_mysql.sql
├── db.properties                      # MySQL connection configuration
├── lib/                               # Bundled runtime libraries
│   ├── mysql-connector-j-8.3.0.jar   # MySQL JDBC Driver
│   ├── zxing-core-3.5.3.jar           # QR Code generator engine
│   ├── slf4j-api-2.0.12.jar           # Logging facade
│   └── slf4j-simple-2.0.12.jar        # Simple logger implementation
├── src/com/cinemats/
│   ├── config/                        # MySQL database connection manager
│   │   └── DBConnection.java
│   ├── dao/                           # Data Access Objects (CRUD queries)
│   │   ├── BookingDAO.java
│   │   ├── CustomerDAO.java
│   │   ├── MovieDAO.java
│   │   ├── ScreenDAO.java
│   │   ├── ScreenSeatDAO.java
│   │   ├── ShowDAO.java
│   │   └── UserDAO.java
│   ├── model/                         # Domain Entities
│   │   ├── Booking.java
│   │   ├── Customer.java
│   │   ├── Movie.java
│   │   ├── Payment.java
│   │   ├── Screen.java
│   │   ├── Show.java
│   │   └── ShowSeat.java
│   ├── service/                       # Business logic services
│   │   ├── BookingService.java
│   │   ├── CustomerService.java
│   │   └── ShowService.java
│   ├── ui/                            # Swing User Interfaces
│   │   ├── admin/                     # Admin Dashboard & Management Pages
│   │   ├── staff/                     # Staff POS Counter & Ticket Pages
│   │   │   ├── OrderBookingPage.java  # Main 3-column POS Terminal
│   │   │   └── booking/               # Ticket Receipt & Confirmation Dialog
│   │   └── login/                     # Secure Authentication Dialog
│   └── util/                          # Utility & Helper classes
│       ├── QRCodeRenderer.java        # ZXing-based standard QR generator
│       └── Theme.java                 # UI Color Palette & Typography
├── seed.sh                            # Database Seeder & Mock Data Reset
├── run.sh                             # Cross-platform launcher (Linux / Mac / Windows Git Bash)
├── run.bat                            # 1-Click launcher for Windows CMD / Explorer
├── run.ps1                            # PowerShell launcher for Windows
└── README.md                          # Project Documentation
```

---

## 🗄️ Database Schema Overview

The system uses a **MySQL** database (`cinema_db`), configured in `db.properties`:

| Table | Description |
| :--- | :--- |
| `users` | Staff & administrator login credentials and roles (`ADMIN`, `STAFF`). |
| `movies` | Movies catalog (title, genre, duration, age rating, status, poster). |
| `screens` | Physical auditorium records (name, screen number, screen type, status). |
| `screen_seats` | Permanent physical seat definitions per screen (`row_name`, `seat_number`, `seat_type`). |
| `shows` | Scheduled movie screenings (movie ID, screen ID, show date, start/end time). |
| `show_seats` | Live seats per show instance with dynamic `price` and real-time `status` (`AVAILABLE`, `BOOKED`, `BLOCKED`). |
| `customers` | Registered patrons identified uniquely by their 10-digit Indian phone number. |
| `bookings` | Master booking headers (`booking_number`, total amount, cashier, status, timestamps). |
| `booking_items` | Individual ticket line items per seat (`seat_label`, `unit_price`). |
| `payments` | Audit records for payments (`method`, `amount_paid`, `amount_received`, `change_amount`, `trx_ref`). |

---

## 🚀 Getting Started

### Prerequisites
- **Java JDK 17 or higher** installed (`javac` compiler is required).
  - Download free OpenJDK from [Eclipse Adoptium (Temurin)](https://adoptium.net/).
- **MySQL 8.0+ or MariaDB** (running locally or remotely).
- **Git** (to clone the project).

---

### ⚙️ Database Setup (For You & Team Members)

1. Create or copy your local configuration:
   ```bash
   cp db.properties.example db.properties
   ```
2. Open `db.properties` and enter your MySQL credentials:
   ```properties
   db.mysql.host=localhost
   db.mysql.port=3306
   db.mysql.database=cinema_db
   db.mysql.user=root
   db.mysql.password=YOUR_PASSWORD_HERE
   ```
   *(Note: `db.properties` is listed in `.gitignore`, so personal passwords will never be pushed to GitHub.)*

3. Seed initial mock data and create all tables:
   ```bash
   ./seed.sh
   # On Windows: seed.bat
   ```

---

### How to Run

#### 🪟 On Windows (Choose Any):
1. **Easiest (No Terminal Needed):**
   - Simply double-click **`run.bat`** in File Explorer.
2. **Via Command Prompt (CMD):**
   ```cmd
   run.bat
   ```
3. **Via PowerShell / VS Code Terminal:**
   ```powershell
   .\run.ps1
   ```
4. **Via Git Bash:**
   ```bash
   ./run.sh
   ```

#### 🐧 On Linux / 🍎 macOS:
```bash
./run.sh
```

*(The launcher automatically resolves dependencies, compiles `src` into `bin/`, and launches the application.)*

---

## 🔑 Default Login Credentials

| Role | Username | Password | Access Area |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `password` | Full system access (Movies, Screens, Schedules, Reports, Staff) |
| **Counter Staff** | `staff` | `password` | Box Office POS Counter (Ticket Booking, History, Reprints) |

---

## 🛠️ Built With
- **Java 17+ (Core)**
- **Java Swing & AWT** (Rich Desktop GUI)
- **MySQL Connector/J** (`mysql-connector-j-8.3.0.jar`)
- **Google ZXing** (`zxing-core-3.5.3.jar`) for ISO QR Matrix generation
- **SLF4J** for database & runtime logging