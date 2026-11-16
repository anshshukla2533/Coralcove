# CoralCove — Presentation Deck Script & Viva Defense Guide
## Summer Internship Final Project Presentation (11 Slides)

This document provides the slide-by-slide text, visual layout structure, speaker delivery script, and anticipated viva/exam defense questions for **`CoralCove_Presentation.pptx`**.

---

### Slide 1: Title Slide

**Visual Layout:**
- Dark navy background (#1B2430) with gold accent line (#C4A35A).
- Top Header Badge: `SUMMER INTERNSHIP - FINAL PROJECT PRESENTATION`
- Main Title: **`CORALCOVE`**
- Subtitle: `Luxury Resort Management Desktop Application - JavaFX & SQLite`
- Presenter Information:
  - `Aniket Sharma - AP24110010141`
  - `Pratinik Infotech - Remote`
  - `SRM University, AP`

**Speaker Script:**
> "Respected mentors and evaluators, good morning. Today I am presenting my summer internship project, CoralCove. CoralCove is an offline-capable, standalone desktop Property Management System designed for multi-zone luxury resorts. The system is built with Java 21, JavaFX, and an embedded SQLite 3 database. Over the course of this presentation, I will cover the operational problem statement, system architecture, core engineering highlights, automated testing results, and practical project outcomes."

---

### Slide 2: Agenda

**Visual Layout:**
- Light background with numbered grid (1 to 7):
  1. Internship Overview
  2. Problem Statement & Objectives
  3. Solution Design & Technology Stack
  4. Key Features
  5. Implementation Highlights
  6. Testing & Outcomes
  7. Learnings & Conclusion
- Footer: `CoralCove | Summer Internship Project` (Slide 2)

**Speaker Script:**
> "The presentation is organized into seven key segments: first, the internship background; second, the domain problem and project objectives; third, our architectural solution design and tech stack; fourth, the six core features; fifth, the eight-week implementation journey; sixth, testing methodologies and verified outcomes; and finally, technical learnings and future enhancements."

---

### Slide 3: Internship Overview

**Visual Layout:**
- Three distinct structured cards:
  - **1. ORGANIZATION:** `Pratinik Infotech` — Technology services organization offering structured software engineering internship programs across enterprise domains.
  - **2. DOMAIN & MODE:** `Desktop Systems & Java Engineering` — Self-paced engineering internship with weekly milestones and technical code reviews.
  - **3. DURATION:** `10 Jun 2026 - 09 Aug 2026` — Two-month software development internship focusing on architecture, database design, GUI implementation, and automated testing.
- Footer: `CoralCove | Summer Internship Project` (Slide 3)

**Speaker Script:**
> "My internship took place at Pratinik Infotech over an eight-week period between June 10 and August 9, 2026. The focus domain was Desktop Systems and Java Engineering. The internship emphasized independent software engineering, requiring full architectural planning, normalized relational data modeling, asynchronous UI development, and rigorous test coverage."

---

### Slide 4: Problem Statement & Objectives

**Visual Layout:**
- Split layout:
  - **Left Card: PROBLEM STATEMENT:**
    - Luxury resorts in coastal destinations frequently experience internet drops that disrupt cloud-based management systems.
    - Front desk operators need a reliable, offline-first desktop platform that eliminates booking collisions, tracks Aadhaar guest identity documents, and coordinates room turnover without network latency.
  - **Right Card: OBJECTIVES:**
    - Build a resilient offline Property Management System desktop application using Java 21 and JavaFX.
    - Design a transactional SQLite relational database with strict foreign keys and automated migrations.
    - Implement reservation booking with mathematical date collision detection and stay party linking.
    - Integrate regulatory guest KYC compliance with 12-digit Aadhaar checksum validation.
    - Provide itemized folio billing with automated room tariffs, incidental charges, and GST calculation.
- Footer: `CoralCove | Summer Internship Project` (Slide 4)

**Speaker Script:**
> "The core challenge in hospitality management for coastal and island resorts is network instability. Cloud-based PMS solutions freeze when internet lines go down, halting check-ins and creating room allocation conflicts. Our objective was to build CoralCove: a fast, offline-first desktop platform. It guarantees that reservations, guest KYC documents, housekeeping turnover, and billing transactions operate without external cloud dependencies."

---

### Slide 5: Solution Design

**Visual Layout:**
- Header: `Solution Design`
- Subheader: `Four core operational workflows of the CoralCove architecture`
- 4 Process Cards connected with arrows (`->`):
  1. **Authentication & Security:** PBKDF2 salted password verification, role assignment, and mandatory first-time password rotation.
  2. **Front Desk & Inventory:** Multi-zone lodge inventory grid, live stay status, Aadhaar KYC verification, and one-click check-in.
  3. **Folio Billing & POS:** Automated room tariff calculation, dining and spa incidental charges, CGST/SGST taxes, and payment recording.
  4. **Housekeeping & Care:** Room status transitions (READY, DIRTY, OCCUPIED), attendant task queues, and facility maintenance defect logs.
- Bottom Summary: All operational data and state transitions are handled locally via an embedded SQLite database in WAL mode, guaranteeing zero cloud dependency and sub-millisecond query performance.
- Footer: `CoralCove | Summer Internship Project` (Slide 5)

**Speaker Script:**
> "Our solution design maps directly to the operational lifecycle of a luxury resort: Authentication with NIST-grade PBKDF2 password hashing; Front Desk management with room inventory across Beach, Lagoon, Cliff, and Garden zones; Folio Billing integrating automated room rates and incidental POS dining/spa charges; and Housekeeping synchronization that automatically flips room states from OCCUPIED to DIRTY upon checkout and to READY after inspection."

---

### Slide 6: Technology Stack

**Visual Layout:**
- Header: `Technology Stack`
- Subheader: `Built with modern LTS Java enterprise desktop technologies and zero bloated frameworks`
- 4 Technology Cards with single-letter emblems:
  - **`J` — Java 21 (LTS):** Core enterprise runtime with virtual threads, records, and strong type safety.
  - **`F` — JavaFX 21:** Hardware-accelerated desktop UI, Scene Graph, CSS styling, and high-DPI scaling.
  - **`S` — SQLite 3:** Embedded ACID transactional database with WAL mode and zero-config local storage.
  - **`M` — Maven & Git:** Build automation, JUnit 5 test suites, dependency management, and GitHub version control.
- Bottom Meta: `Runtime: OpenJDK 21 | Repository: GitHub`
- Footer: `CoralCove | Summer Internship Project` (Slide 6)

**Speaker Script:**
> "The technology stack was chosen intentionally for performance and zero-dependency reliability: Java 21 LTS brings strong typing and records; JavaFX 21 delivers hardware-accelerated desktop rendering with CSS skinning; SQLite 3 provides transactional ACID storage with Write-Ahead Logging; and Maven manages our build pipeline, dependency isolation, and JUnit 5 automated testing."

---

### Slide 7: Key Features

**Visual Layout:**
- 6 Grid Cards:
  1. **Multi-Zone Room Inventory:** Visual management across Beach, Lagoon, Cliff, and Garden zones with instant status filtering.
  2. **Aadhaar KYC Verification:** Indian regulatory compliance with 12-digit format validation, document staging, and modal inspection.
  3. **Master Folio Accounting:** Itemized room tariffs, meal plans, spa and dining add-ons with automated CGST/SGST tax math.
  4. **Housekeeping Synchronization:** Automated room state transitions upon check-in/out, attendant task queues, and supervisor inspection pass/fail.
  5. **Role-Based Access Control:** Strict permission enforcement for Admin, Manager, Front Desk, and Housekeeping across 12 application modules.
  6. **Offline-First Desktop Speed:** Sub-millisecond query execution on local SQLite storage without internet latency or third-party outages.
- Footer: `CoralCove | Summer Internship Project` (Slide 7)

**Speaker Script:**
> "Six primary features define the system: First, zone-based room inventory across four distinct resort areas. Second, built-in Aadhaar KYC verification for Indian regulatory compliance. Third, master folio accounting calculating exact GST and incidental charges using BigDecimal. Fourth, automated room turnover sync between front-desk and housekeeping. Fifth, role-based access control protecting administrative features. And sixth, offline desktop speed, delivering sub-millisecond query responses from local storage."

---

### Slide 8: Implementation Journey

**Visual Layout:**
- Header: `Implementation Journey`
- Subheader: `8 weeks from domain modeling to production-grade desktop application`
- 8 Sequential Timeline Nodes (Wk 1 to Wk 8):
  - **Wk 1:** Requirements analysis & domain modeling
  - **Wk 2:** SQLite schema design & migrations
  - **Wk 3:** JavaFX clean architecture & base theme
  - **Wk 4:** Reservation engine & date collision logic
  - **Wk 5:** Aadhaar KYC validation & file storage
  - **Wk 6:** Folio billing, GST math & PDF generation
  - **Wk 7:** Dialog spacing, scrolling & luxury branding
  - **Wk 8:** JUnit 5 testing, technical docs & git release
- Footer: `CoralCove | Summer Internship Project` (Slide 8)

**Speaker Script:**
> "The implementation spanned eight structured weeks. Weeks 1 and 2 focused on domain modeling and normalized database schema design. Weeks 3 and 4 established the layered architecture and the reservation date-collision engine. Weeks 5 and 6 delivered Aadhaar KYC handling and the billing calculation engine. Weeks 7 and 8 focused on UI polish, including dialog padding refactoring, scrollpane wrapping, multi-resolution branding, and JUnit 5 automated test verification."

---

### Slide 9: Testing & Outcomes

**Visual Layout:**
- Split comparison:
  - **TESTING CARRIED OUT:**
    - Date collision verification across overlapping, adjacent, and invalid booking dates
    - Aadhaar 12-digit format and Verhoeff-style checksum validation tests
    - Folio balance reconciliation and half-up decimal tax rounding precision tests
    - Role-based permission gating and session authentication flow tests
    - High-DPI desktop scaling, dialog scrolling, and window resize tests
  - **OUTCOMES:**
    - Functional CoralCove resort management desktop application successfully built and tested
    - Comprehensive 12-module operational suite supporting all luxury resort workflows
    - Zero-regression test suite with 18 automated JUnit 5 test cases passing
    - Custom luxury visual design system with multi-resolution desktop branding
    - Production-ready codebase, complete 900-line documentation, and active GitHub repo
- Footer: `CoralCove | Summer Internship Project` (Slide 9)

**Speaker Script:**
> "Our testing methodology covered both automated unit tests and manual boundary verifications. We verified that overlapping bookings are mathematically blocked, Aadhaar checksums are validated, and folio tax calculations maintain exact half-up precision. All eighteen automated JUnit 5 test cases pass cleanly, demonstrating a robust, production-ready system with zero regressions."

---

### Slide 10: Learnings & Conclusion

**Visual Layout:**
- Summary banner: `This internship provided hands-on experience across the entire desktop software engineering lifecycle, from domain requirements analysis and database modeling to GUI implementation, testing, UI refactoring, and documentation.`
- 4 Key Takeaways:
  - Mastered enterprise Java 21 features, virtual threads, and clean layered architecture
  - Strengthened expertise in hardware-accelerated JavaFX desktop GUI design and CSS skinning
  - Deepened understanding of relational schema design, ACID transactions, and JDBC indexing
  - Gained practical skills in automated test-driven verification, git workflow, and technical documentation
- Future Scope: `Future Scope: Multi-terminal peer-to-peer synchronization, automated offsite database backups, payment gateway hardware integration, and biometric scanner support.`
- Footer: `CoralCove | Summer Internship Project` (Slide 10)

**Speaker Script:**
> "In conclusion, this internship provided invaluable practical experience in enterprise software development. I gained hands-on mastery in modern Java 21, JavaFX Scene Graph mechanics, relational database transactions with SQLite, and automated testing. In the future, the platform can be extended with multi-terminal local network synchronization, automated offsite cloud backups, and biometric hardware integration."

---

### Slide 11: Thank You

**Visual Layout:**
- Dark navy slide with centered luxury typography:
  - **`Thank You`**
  - Questions & Answers

**Speaker Script:**
> "Thank you for your time and guidance. I welcome any questions regarding the architecture, database design, or implementation details of CoralCove."

---

## Technical Viva & Exam Defense Q&A

### Q1: Why did you choose JavaFX and SQLite instead of a Web/React + MySQL stack?
**Answer:** Remote coastal luxury resorts frequently face intermittent internet and fiber cuts. A web stack requires either an external cloud server or local server hosting with complex network configuration. JavaFX paired with embedded SQLite creates a zero-configuration, zero-latency desktop application that boots instantly, runs completely offline, and stores data in a single ACID-compliant database file using Write-Ahead Logging (WAL) for sub-millisecond performance.

### Q2: How do you prevent double-booking when two guests reserve overlapping dates?
**Answer:** We enforce date collision validation at the DAO layer using a strict mathematical boundary condition:
```sql
SELECT COUNT(*) FROM stay_bookings
WHERE lodge_id = ?
  AND status NOT IN ('CANCELLED', 'CHECKED_OUT')
  AND NOT (check_out <= ? OR check_in >= ?)
```
If this query returns a count greater than 0, the target room is occupied during that window, and `StayService` throws a validation exception preventing the insert.

### Q3: How is password security implemented?
**Answer:** Plaintext passwords are never stored. We implement PBKDF2 with HMAC-SHA256 complying with NIST SP 800-63B. Each password is salted with 16 cryptographically secure random bytes generated via `SecureRandom` and hashed across 210,000 iterations. Verification uses constant-time array comparison (`MessageDigest.isEqual`) to eliminate timing side-channel attacks.

### Q4: Why use `BigDecimal` instead of `double` for billing?
**Answer:** IEEE 754 floating-point numbers (`double` and `float`) introduce binary rounding imprecision (for example, `0.1 + 0.2 != 0.3`). In financial systems and GST tax calculations, fractional cents compound into auditing discrepancies. We use `BigDecimal` with explicit scale and `RoundingMode.HALF_UP` to guarantee exact mathematical accuracy.

### Q5: How do you keep the desktop UI responsive during database queries?
**Answer:** JavaFX operates on a single UI Application Thread. Running blocking JDBC database calls on that thread would cause the interface to freeze or drop frames. We built an asynchronous helper `Fx.run(backgroundSupplier, uiConsumer)` that executes queries on a background worker thread and posts the resulting data back to the UI thread via `Platform.runLater()`.
