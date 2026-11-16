# CoralCove — Luxury Resort Management Desktop Platform

[![Java 21](https://img.shields.io/badge/Java-21%2B-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX 21](https://img.shields.io/badge/JavaFX-21-2E7D32?logo=java&logoColor=white)](https://openjfx.io/)
[![SQLite 3](https://img.shields.io/badge/SQLite-3.46-003B57?logo=sqlite&logoColor=white)](https://sqlite.org/)
[![Maven Build](https://img.shields.io/badge/Build-Passing-brightgreen?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey.svg)]()

**CoralCove** is a high-performance, offline-first desktop Property Management System (PMS) and Resort Operations Platform built with Java 21, JavaFX, and embedded SQLite 3. Tailored for multi-property luxury resort destinations across beach, lagoon, cliff, and garden zones, CoralCove unifies front-desk reservations, guest KYC compliance with Indian Aadhaar verification, real-time housekeeping coordination, POS incidental billing, employee attendance, inventory tracking, and executive analytics into a cohesive, responsive desktop experience.

---

## Key Highlights

- **Offline-First Resilience**: Full transactional operations stored locally in SQLite with WAL (Write-Ahead Logging) mode. Operates seamlessly without internet dependency.
- **Strict Clean Architecture**: Four-layer separation of concerns (UI -> Service -> DAO -> Database/Model) with zero circular dependencies.
- **Indian Regulatory KYC Compliance**: Built-in 12-digit Aadhaar validation with Verhoeff-style checksum verification, document image staging, and instant modal preview.
- **Real-Time Folio Accounting**: Transactional master folios with automatic room tariff calculation, POS dining and spa charge posting, GST breakdown (CGST + SGST), and payment recording (Cash, Card, UPI, Bank Transfer).
- **Housekeeping & Turnover Sync**: Automated room state transitions (`READY`, `OCCUPIED`, `DIRTY`, `HELD`, `OUT_OF_ORDER`) triggered by reservations and front desk check-in/out events.
- **Role-Based Access Control (RBAC)**: Enforced across 4 distinct roles (`ADMIN` / Owner, `MANAGER`, `FRONT_DESK`, `ATTENDANT`) with tailored navigation menus and API authorization checks.
- **NIST-Grade Cryptographic Security**: Passwords hashed using PBKDF2 with HMAC-SHA256 across 210,000 iterations and cryptographically random 16-byte salts.
- **Bespoke Luxury Design Language**: Custom JavaFX CSS styling featuring a warm sand backdrop, deep navy navigation rail, brushed coastal gold accents, responsive high-DPI scaling, and multi-resolution application icons.

---

## Architectural Overview

```
+-------------------------------------------------------------------------+
|                  JavaFX 21 Desktop Presentation Tier                   |
|  ShellView | Sidebar | TopBar | ScreenHeader | Dialogs | Theme CSS     |
+-------------------------------------------------------------------------+
                                    |
                                    v (Method invocations / DTOs)
+-------------------------------------------------------------------------+
|                      Business Service & Logic Tier                      |
|  AuthenticationService | StayService | ArrivalService | BillingService  |
|  LodgeService | GuestService | LodgeCareService | DashboardService     |
+-------------------------------------------------------------------------+
                                    |
                                    v (java.sql.Connection passed)
+-------------------------------------------------------------------------+
|                     Data Access Object (DAO) Tier                       |
|  UserDAO | GuestDAO | LodgeDAO | StayBookingDAO | InvoiceDAO           |
|  LodgeCareDAO | InventoryDAO | EmployeeDAO | ReportDAO | AuditDAO       |
+-------------------------------------------------------------------------+
                                    |
                                    v (PreparedStatements / Parameterized SQL)
+-------------------------------------------------------------------------+
|                     Embedded Relational Storage Tier                    |
|  SQLite 3 Engine (data/coralcove.db) | WAL Mode | Foreign Keys Enabled |
+-------------------------------------------------------------------------+
```

---

## Technology Stack

| Layer | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java (OpenJDK) | 21 (LTS) | Virtual threads, record classes, modern syntax, strict typing |
| **GUI Framework** | JavaFX | 21.0.4 | High-DPI hardware-accelerated desktop UI, Scene Graph, CSS skinning |
| **Database** | SQLite JDBC | 3.46.0.0 | Zero-configuration, zero-latency local transactional relational storage |
| **Cryptography** | `javax.crypto` (PBKDF2) | Java 21 Standard | NIST SP 800-63B compliant password hashing with HMAC-SHA256 |
| **Build & Tooling** | Apache Maven & Maven Wrapper | 3.9+ | Cross-platform build automation, dependency resolution, packaging |
| **Testing** | JUnit Jupiter (JUnit 5) | 5.10.2 | Unit testing and transactional DAO integration verification |
| **Export Engines** | Native PDF & CSV Engines | Built-in | Custom zero-dependency PDF 1.4 compiler and RFC 4180 CSV export |

---

## Core Operational Modules

1. **Executive Dashboard**: Real-time KPI summary cards (occupancy rate, month-to-date revenue, active guests, pending turndown tasks), 14-day occupancy trend bar charts, and quick-action arrival feeds.
2. **Stay & Reservation Management**: Interactive reservation book supporting date range validation, overlapping night collision detection, guest linking, meal plans (EP, CP, MAP, AP), and airport transfer logistics.
3. **Arrivals & Front Desk**: Daily check-in / check-out processing, zone photo cards, guest details lookup, and immediate Aadhaar KYC verification.
4. **Lodge & Room Inventory**: Complete room status directory filtered by resort zone (Beach, Lagoon, Cliff, Garden) with live status toggles (`READY`, `OCCUPIED`, `DIRTY`, `HELD`, `OUT_OF_ORDER`).
5. **Guest Directory & KYC**: Searchable guest profiles, contact numbers, email formatting checks, lifetime stay expenditure metrics, and Aadhaar document capture.
6. **Folio Billing & Payments**: Multi-item master folios calculating room charges, meal tariffs, and incidental POS add-ons with automated CGST/SGST tax distribution and payment receipt generation.
7. **Housekeeping & Facility Care**: Cleaning task dispatcher, attendant assignment, inspection sign-off, and defect/repair maintenance logs.
8. **Resort Experiences & Dining POS**: Curated wellness spa, private yacht charters, diving excursions, and fine dining room service ordering pipeline.
9. **Staff Directory & Attendance**: Department-wise staff roster with daily attendance register (Present, Absent, On Leave, Half Day).
10. **Inventory & Warehouse Stock**: SKU inventory management with minimum reorder alerts, automated stock adjustment auditing, and vendor records.
11. **Reports & Business Intelligence**: Audited revenue summaries, occupancy statistics, and debtor folios exportable to CSV and printable PDF.
12. **Settings & Customization**: Resort property metadata configuration, GST rate modification, and personal user profile editor with instant avatar photo upload.

---

## Quick Start & Installation

### Prerequisites

- **Java Development Kit (JDK)**: Version 21 or higher installed and accessible on your `PATH`.
  ```powershell
  java -version
  ```
- **Operating System**: Windows 10/11, macOS 12+, or modern Linux distribution.

### Running the Application

Clone the repository and launch via the included Maven wrapper:

```powershell
# Clone the repository
git clone https://github.com/applejuice093/Coralcove.git
cd Coralcove

# Launch application on Windows
.\mvnw.cmd javafx:run

# Launch application on macOS / Linux
./mvnw javafx:run
```

The desktop window will initialize at `1280x800` resolution with the title **`CoralCove | Resort Management`**.

### Running the Test Suite

Execute the automated test suite covering database schema migrations, password hashing, date collision logic, money rounding, and DAO transactions:

```powershell
.\mvnw.cmd test
```

*(Tests execute against an isolated temporary in-memory database and run headless without opening UI windows).*

---

## Default Seeded Accounts

The initial database (`data/coralcove.db`) is automatically provisioned on first launch with role-differentiated demo accounts:

| Username | Default Password | Full Name | Assigned Role | Access Scope |
| :--- | :--- | :--- | :--- | :--- |
| `admin` | `Admin@123` | Aniket Sharma | **ADMIN (Owner)** | Unrestricted global access to all 12 modules, financial reports, and settings. |
| `mgr` | `Manager@123` | Priya Patel | **MANAGER** | Full operations access including staff attendance, reservations, inventory, and billing. |
| `rec` | `FrontDesk@123` | Rahul Verma | **FRONT_DESK** | Front-desk operations, guest check-in/out, stay bookings, and payment receipting. |
| `hk` | `Clean@123` | Sunita Rao | **ATTENDANT** | Housekeeping queue, room cleaning status updates, and facilities repair logging. |

> **Security Note:** On first authentication, accounts flagged with `must_change_password` will automatically prompt the user to establish a personalized password complying with security complexity standards before opening the main desk.

---

## Repository Structure

```
corlcovejavafx/
├── .mvn/                          # Maven wrapper configuration
├── data/                          # Runtime database, logs, and media storage
│   ├── aadhar/                    # Staged guest KYC document scans
│   ├── avatars/                   # User profile avatar uploads
│   ├── coralcove.db               # Embedded SQLite database file
│   └── coralcove.log              # Rolling application log file
├── src/
│   ├── main/
│   │   ├── java/coralcove/
│   │   │   ├── dao/               # Data Access Objects (SQL queries & result mapping)
│   │   │   ├── model/             # Domain entities and immutable records
│   │   │   ├── service/           # Business logic services & transaction managers
│   │   │   ├── ui/                # JavaFX views, dialogs, custom components & shell
│   │   │   ├── util/              # Native PDF compiler, CSV exporter, Money utilities
│   │   │   ├── Database.java      # SQLite connection manager & schema migrations
│   │   │   └── Main.java          # JavaFX Application entrypoint & stage configuration
│   │   └── resources/coralcove/
│   │       ├── media/             # Curated resort photography and brand assets
│   │       ├── app-icon.png       # High-resolution application brand icon
│   │       ├── icon.png           # Multi-resolution stage icon asset
│   │       └── theme.css          # Master JavaFX styling and luxury design token system
│   └── test/java/coralcove/       # JUnit 5 automated test suite
├── doc.md                         # Exhaustive technical specification (900+ lines)
├── report.md                      # Academic internship project report
├── presentation_script.md         # Comprehensive slide deck script and defense guide
├── pom.xml                        # Maven project dependencies and JavaFX compiler plugin
├── mvnw.cmd                       # Maven wrapper script for Windows
└── mvnw                           # Maven wrapper script for Unix/Linux
```

---

## Comprehensive Documentation Links

- [Complete Technical Documentation (`doc.md`)](file:///c:/A/PROJECT/corlcovejavafx/doc.md): Full 900+ line technical reference covering every database table, foreign key constraint, service API, UI controller, and cryptographic algorithm.
- [Academic Internship Report (`report.md`)](file:///c:/A/PROJECT/corlcovejavafx/report.md): 20-page comprehensive project report structured according to university project curriculum standards.
- [Project Presentation Deck (`presentation_script.md`)](file:///c:/A/PROJECT/corlcovejavafx/presentation_script.md): 11-slide project defense presentation guide with talking points and viva answers.

---

## License

This project is licensed under the [MIT License](LICENSE). Built for academic submission and enterprise desktop management evaluation.
