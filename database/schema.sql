-- ====================================================================
-- Cinema Express - SQLite Database Schema Definition
-- Database File: cinema.db
-- ====================================================================

-- 1. USERS & STAFF ACCOUNTS TABLE
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'STAFF',            -- 'ADMIN' or 'STAFF'
    full_name TEXT NOT NULL,
    counter TEXT DEFAULT 'Counter #01 (Main Concourse)',
    shift TEXT DEFAULT 'Morning Shift (09:00 AM - 04:00 PM)',
    phone TEXT DEFAULT '',
    status TEXT DEFAULT 'ACTIVE',                  -- 'ACTIVE' or 'INACTIVE'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Default Super Admin
INSERT OR IGNORE INTO users (id, username, password, role, full_name, counter, shift, phone, status)
VALUES (1, 'admin', 'admin123', 'ADMIN', 'System Administrator', 'HQ Management Station', 'General Shift (10:00 AM - 07:00 PM)', '+91 98765 00001', 'ACTIVE');

-- Default Staff Cashier
INSERT OR IGNORE INTO users (id, username, password, role, full_name, counter, shift, phone, status)
VALUES (2, 'staff', 'staff123', 'STAFF', 'Rahul Sharma', 'Counter #01 (Main Concourse)', 'Morning Shift (09:00 AM - 04:00 PM)', '+91 98765 43210', 'ACTIVE');


-- 2. MOVIE CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed Initial Default Movie Categories
INSERT OR IGNORE INTO categories (id, name, description) VALUES
(1, 'Action', 'High-energy sequences, stunts, pursuits, and physical conflicts'),
(2, 'Adventure', 'Exciting journeys, expeditions, heroic quests, and exploration'),
(3, 'Animation', 'CGI, 3D, and hand-drawn animated films for all audiences'),
(4, 'Comedy', 'Lighthearted humor, satirical plots, and comedic entertainment'),
(5, 'Crime', 'Detective investigations, criminal syndicates, and forensic drama'),
(6, 'Drama', 'Character-driven realistic stories, conflicts, and deep emotion'),
(7, 'Fantasy', 'Mythological realms, magical powers, folklore, and mythical creatures'),
(8, 'Horror', 'Supernatural mysteries, psychological fear, and eerie atmosphere'),
(9, 'Romance', 'Love stories, passionate relationships, and intimate journeys'),
(10, 'Sci-Fi', 'Futuristic technology, space exploration, time travel, and AI'),
(11, 'Thriller', 'High-stakes tension, psychological suspense, and unexpected twists');


-- 3. MOVIES TABLE
CREATE TABLE IF NOT EXISTS movies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    genre TEXT NOT NULL,
    duration_mins INTEGER DEFAULT 150,
    price REAL NOT NULL DEFAULT 200.0,
    rating TEXT DEFAULT 'UA',
    poster_label TEXT DEFAULT 'MOVIE POSTER',
    status TEXT DEFAULT 'NOW_SHOWING',             -- 'NOW_SHOWING', 'UPCOMING', 'ARCHIVED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed Initial Default Movies
INSERT OR IGNORE INTO movies (id, title, genre, duration_mins, price, rating, poster_label, status) VALUES
(1, 'Interstellar', 'Sci-Fi / Adventure', 169, 200.0, 'UA', 'INTERSTELLAR', 'NOW_SHOWING'),
(2, 'Dune: Part Two', 'Action / Adventure', 166, 220.0, 'UA', 'DUNE: PART TWO', 'NOW_SHOWING'),
(3, 'Oppenheimer', 'Biography / Drama', 180, 200.0, 'A', 'OPPENHEIMER', 'NOW_SHOWING'),
(4, 'Spider-Man: Across The Spider-Verse', 'Animation / Action', 140, 180.0, 'U', 'SPIDER-MAN', 'NOW_SHOWING'),
(5, 'Inception', 'Sci-Fi / Thriller', 148, 180.0, 'UA', 'INCEPTION', 'NOW_SHOWING'),
(6, 'The Dark Knight', 'Action / Crime', 152, 190.0, 'UA', 'THE DARK KNIGHT', 'NOW_SHOWING');


-- 3. SCHEDULES & SHOWTIMES TABLE
CREATE TABLE IF NOT EXISTS shows (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    movie_id INTEGER NOT NULL,
    screen TEXT NOT NULL DEFAULT 'Screen 1 (IMAX Laser)',
    show_time TEXT NOT NULL,                       -- e.g. '10:30 AM', '02:00 PM'
    show_date TEXT NOT NULL,                       -- e.g. '2026-09-26'
    price REAL NOT NULL DEFAULT 200.0,
    available_seats INTEGER DEFAULT 120,
    total_seats INTEGER DEFAULT 120,
    status TEXT DEFAULT 'OPEN',                    -- 'OPEN', 'HOUSEFULL', 'CANCELLED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
);


-- 4. BOOKINGS & TICKETS TABLE
CREATE TABLE IF NOT EXISTS bookings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    booking_code TEXT UNIQUE NOT NULL,             -- e.g. 'TKT-20260926-1001'
    show_id INTEGER NOT NULL,
    customer_name TEXT NOT NULL,
    customer_phone TEXT NOT NULL,
    seat_numbers TEXT NOT NULL,                    -- comma-separated: 'A1, A2, A3'
    seat_count INTEGER NOT NULL DEFAULT 1,
    total_amount REAL NOT NULL,
    payment_mode TEXT DEFAULT 'CASH',              -- 'CASH', 'UPI', 'CARD'
    booked_by_staff TEXT DEFAULT 'staff',
    booked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE
);

-- 4. SCREENS & AUDITORIUMS TABLE
CREATE TABLE IF NOT EXISTS screens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    screen_number INTEGER UNIQUE NOT NULL,
    screen_type TEXT NOT NULL DEFAULT 'Standard',  -- 'Standard', 'Premium', 'IMAX', 'Dolby', '4DX', 'Other'
    status TEXT NOT NULL DEFAULT 'ACTIVE',          -- 'ACTIVE', 'INACTIVE', 'MAINTENANCE'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed Initial Screens
INSERT OR IGNORE INTO screens (id, name, screen_number, screen_type, status) VALUES
(1, 'Screen 1', 1, 'IMAX', 'ACTIVE'),
(2, 'Screen 2', 2, 'Premium', 'ACTIVE'),
(3, 'Screen 3', 3, 'Standard', 'ACTIVE'),
(4, 'Screen 4', 4, 'Dolby', 'MAINTENANCE');


-- 5. PHYSICAL SCREEN SEATS TABLE
CREATE TABLE IF NOT EXISTS screen_seats (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    screen_id INTEGER NOT NULL,
    row_name TEXT NOT NULL,                        -- e.g. 'A', 'B', 'C'
    seat_number INTEGER NOT NULL,                  -- e.g. 1, 2, 3
    seat_label TEXT NOT NULL,                      -- e.g. 'A1', 'A2'
    seat_type TEXT NOT NULL DEFAULT 'REGULAR',      -- 'REGULAR', 'PREMIUM', 'RECLINER'
    status TEXT NOT NULL DEFAULT 'ACTIVE',          -- 'ACTIVE', 'BLOCKED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (screen_id) REFERENCES screens(id) ON DELETE CASCADE,
    UNIQUE(screen_id, seat_label)
);


-- 6. SHOWTIME SEATS INVENTORY TABLE
CREATE TABLE IF NOT EXISTS show_seats (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    show_id INTEGER NOT NULL,
    screen_seat_id INTEGER NOT NULL,
    price REAL NOT NULL DEFAULT 200.0,
    status TEXT NOT NULL DEFAULT 'AVAILABLE',      -- 'AVAILABLE', 'BOOKED', 'BLOCKED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE,
    FOREIGN KEY (screen_seat_id) REFERENCES screen_seats(id) ON DELETE CASCADE
);
