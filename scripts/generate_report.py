import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn
import os

def create_report():
    doc = Document()
    
    # Page setup - 1 inch margins
    for section in doc.sections:
        section.top_margin = Inches(1.0)
        section.bottom_margin = Inches(1.0)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(1.0)
        
        # Configure header
        header = section.header
        hp = header.paragraphs[0]
        hp.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        hrun = hp.add_run("assodean.ps@srmap.edu.in (c)SRM UNIVERSITY - AP, ANDHRA PRADESH")
        hrun.font.name = "Calibri"
        hrun.font.size = Pt(8.5)
        hrun.font.color.rgb = RGBColor(120, 120, 120)
        
        # Configure footer
        footer = section.footer
        fp = footer.paragraphs[0]
        fp.alignment = WD_ALIGN_PARAGRAPH.CENTER
        frun = fp.add_run("CoralCove - Summer Internship Project Report")
        frun.font.name = "Calibri"
        frun.font.size = Pt(9)
        frun.font.color.rgb = RGBColor(120, 120, 120)

    # Styles helper
    def add_title(text, space_before=0, space_after=12):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(space_before)
        p.paragraph_format.space_after = Pt(space_after)
        run = p.add_run(text)
        run.bold = True
        run.font.name = "Calibri"
        run.font.size = Pt(16)
        run.font.color.rgb = RGBColor(27, 36, 48)
        return p

    def add_h1(text, space_before=14, space_after=6):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(space_before)
        p.paragraph_format.space_after = Pt(space_after)
        run = p.add_run(text)
        run.bold = True
        run.font.name = "Calibri"
        run.font.size = Pt(13)
        run.font.color.rgb = RGBColor(27, 36, 48)
        return p

    def add_h2(text, space_before=10, space_after=4):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(space_before)
        p.paragraph_format.space_after = Pt(space_after)
        run = p.add_run(text)
        run.bold = True
        run.font.name = "Calibri"
        run.font.size = Pt(11.5)
        run.font.color.rgb = RGBColor(40, 50, 60)
        return p

    def add_body(text, space_after=6, space_before=0, align=WD_ALIGN_PARAGRAPH.JUSTIFY):
        p = doc.add_paragraph()
        p.alignment = align
        p.paragraph_format.space_before = Pt(space_before)
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.line_spacing = 1.15
        run = p.add_run(text)
        run.font.name = "Calibri"
        run.font.size = Pt(11)
        run.font.color.rgb = RGBColor(30, 30, 30)
        return p

    def add_bullet(text, space_after=4):
        p = doc.add_paragraph(style='List Bullet')
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.line_spacing = 1.15
        run = p.add_run(text)
        run.font.name = "Calibri"
        run.font.size = Pt(11)
        run.font.color.rgb = RGBColor(30, 30, 30)
        return p

    # -------------------------------------------------------------
    # PAGE 1: TITLE PAGE
    # -------------------------------------------------------------
    p_top = doc.add_paragraph()
    p_top.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_top.paragraph_format.space_before = Pt(40)
    p_top.paragraph_format.space_after = Pt(10)
    r_rep = p_top.add_run("A REPORT\nON\n")
    r_rep.bold = True
    r_rep.font.name = "Calibri"
    r_rep.font.size = Pt(14)
    
    r_proj = p_top.add_run("SYSTEM DESIGN AND DEVELOPMENT OF CORALCOVE -\nLUXURY RESORT MANAGEMENT DESKTOP APPLICATION")
    r_proj.bold = True
    r_proj.font.name = "Calibri"
    r_proj.font.size = Pt(15)
    r_proj.font.color.rgb = RGBColor(196, 136, 40)

    p_by = doc.add_paragraph()
    p_by.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_by.paragraph_format.space_before = Pt(30)
    p_by.paragraph_format.space_after = Pt(20)
    r_by = p_by.add_run("By\n\nName of the student: Aniket Sharma\nRegistration No.: AP24110010141\n--------------------------------   --------------------------")
    r_by.font.name = "Calibri"
    r_by.font.size = Pt(12)

    p_mid = doc.add_paragraph()
    p_mid.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_mid.paragraph_format.space_before = Pt(40)
    p_mid.paragraph_format.space_after = Pt(10)
    r_mid = p_mid.add_run("Prepared in the partial fulfillment of the\nSummer Internship Course\nAT\nPRATINIK INFOTECH, Pune, Maharashtra, India")
    r_mid.font.name = "Calibri"
    r_mid.font.size = Pt(12)

    p_bot = doc.add_paragraph()
    p_bot.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_bot.paragraph_format.space_before = Pt(60)
    r_bot = p_bot.add_run("SRM UNIVERSITY, AP\n(July, 2026)")
    r_bot.bold = True
    r_bot.font.name = "Calibri"
    r_bot.font.size = Pt(13)

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGE 2: CERTIFICATE OF INTERNSHIP
    # -------------------------------------------------------------
    add_title("CERTIFICATE OF INTERNSHIP", space_before=20, space_after=30)
    
    add_body("This is to certify that Mr. Aniket Sharma, Registration No. AP24110010141, a student of SRM University, AP, has successfully completed his summer internship at Pratinik Infotech from 10th June 2026 to 09th August 2026.")
    add_body("During this period, he worked on the project titled \"CoralCove - Luxury Resort Management Desktop Application\" under the domain of Desktop Systems and Java Engineering.")
    add_body("He demonstrated strong technical initiative, dedication, and problem-solving skills throughout the development lifecycle, including system architecture, relational database design with SQLite, JavaFX user interface engineering, and automated testing.")
    add_body("We found him sincere, hardworking, and result-oriented. We wish him all success in his academic and professional future.")
    
    p_sig = doc.add_paragraph()
    p_sig.paragraph_format.space_before = Pt(80)
    p_sig.paragraph_format.line_spacing = 1.3
    r_sig = p_sig.add_run("Authorized Signatory\nPratinik Infotech\nPune, Maharashtra, India\nDate: 09th August 2026")
    r_sig.font.name = "Calibri"
    r_sig.font.size = Pt(11)

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGE 3: APPENDIX A - INTERNSHIP ACCEPTANCE CONFIRMATION
    # -------------------------------------------------------------
    add_title("APPENDIX A\nINTERNSHIP ACCEPTANCE CONFIRMATION", space_before=20, space_after=20)
    
    p_comp = doc.add_paragraph()
    r_cname = p_comp.add_run("Pratinik Infotech\nAcceptance Number: #6174\n")
    r_cname.bold = True
    r_cname.font.size = Pt(12)
    
    add_body("Dear Aniket Sharma,")
    add_body("Thank you for completing the acceptance policy with Pratinik Infotech.")
    add_body("You can expect to receive tasks relevant to your internship domain, along with detailed instructions, sent to your registered email address on the Internship Starting Date.")
    add_body("Refer to the technical documentation and repository guidelines for any clarifications.")
    
    add_h2("Important Dates")
    add_bullet("Offer Letters Issued: 10th June 2026")
    add_bullet("Internship Start Date: 10th June 2026")
    add_bullet("Duration: 2 Months (Ending 09th August 2026)")
    add_bullet("Internship Domain: Desktop Systems and Java Engineering")
    
    add_body("The internship domain and schedule are fixed and cannot be changed after acceptance. Acceptance of the internship is final; no revocation requests will be entertained.", space_before=12)
    
    p_close = doc.add_paragraph()
    p_close.paragraph_format.space_before = Pt(30)
    r_cl = p_close.add_run("Pratinik Infotech\nPune, Maharashtra, India")
    r_cl.font.name = "Calibri"
    r_cl.font.size = Pt(11)

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGE 4: ACKNOWLEDGEMENT
    # -------------------------------------------------------------
    add_title("ACKNOWLEDGEMENT", space_before=20, space_after=20)
    
    add_body("I express my sincere gratitude to everyone who supported and guided me during my summer internship at Pratinik Infotech.")
    add_body("I am thankful to SRM University, AP for providing me with the opportunity to undertake this internship as part of the Summer Internship Course. The internship gave me an opportunity to understand how concepts learned in the classroom can be applied while working on a practical software project.")
    add_body("I thank my faculty mentor for providing guidance throughout the internship and for helping me understand the academic requirements of the project report.")
    add_body("I am grateful to my industry mentor and the team at Pratinik Infotech for their guidance, suggestions, and technical feedback during the development of the project. Their input helped me appreciate the importance of clean architecture, relational constraints, error handling, and test-driven refinement in production software.")
    add_body("Finally, I am thankful for the opportunity to build CoralCove - Luxury Resort Management Desktop Application, which gave me practical exposure to enterprise desktop engineering, relational database operations, and desktop user interface design.")

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGE 5: ABSTRACT
    # -------------------------------------------------------------
    add_title("ABSTRACT", space_before=20, space_after=20)
    
    add_body("The summer internship at Pratinik Infotech provided practical exposure to enterprise desktop application development and the process of converting complex domain requirements into a reliable software system. As part of the internship, CoralCove - Luxury Resort Management Desktop Application was developed to provide hospitality operators with a fast, offline-capable platform for managing multi-zone resorts.")
    add_body("Many remote resort destinations face unstable internet connectivity. Web-based property management systems often freeze or lose data during outages, leading to check-in delays and billing errors. CoralCove addresses this issue by running locally on Java 21 and JavaFX, using an embedded SQLite 3 database with Write-Ahead Logging (WAL) for sub-millisecond query performance and zero cloud dependency.")
    add_body("The project encompasses twelve operational modules, including reservation management with mathematical date-collision validation, guest KYC compliance with 12-digit Indian Aadhaar verification, real-time housekeeping coordination, master folio billing with GST calculation, employee attendance tracking, and executive analytics. The architecture follows a strict four-layer separation: Presentation, Service, Data Access Object (DAO), and Database.")
    add_body("Testing was conducted using JUnit 5 across unit and integration levels. Automated tests verified date collision checks, Aadhaar format verification, password hashing, and monetary calculations. User interface refinements resolved dialog spacing, scroll handling, and modal button pinning.")
    add_body("The internship bridged theoretical academic concepts with professional software development practices, including version control, clean code architecture, database indexing, and comprehensive technical documentation.")

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGES 6-7: TABLE OF CONTENTS
    # -------------------------------------------------------------
    add_title("TABLE OF CONTENTS", space_before=20, space_after=20)
    
    toc_table = doc.add_table(rows=1, cols=2)
    toc_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    hdr_cells = toc_table.rows[0].cells
    hdr_cells[0].paragraphs[0].add_run("CONTENTS").bold = True
    hdr_cells[1].paragraphs[0].add_run("Page No.").bold = True
    hdr_cells[1].paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.RIGHT
    
    toc_items = [
        ("1. Introduction", "8"),
        ("   1.1 Introduction to the Internship", "8"),
        ("   1.2 Purpose of the Project", "8"),
        ("   1.3 Objectives", "9"),
        ("2. Organization and Business Sector", "10"),
        ("   2.1 Introduction to the Hospitality Management Software Sector", "10"),
        ("3. Overview of Pratinik Infotech", "11"),
        ("   3.1 Organization Overview", "11"),
        ("   3.2 Nature of Work", "11"),
        ("   3.3 Importance of the Organization to the Sector", "11"),
        ("4. Internship Plan", "12"),
        ("   4.1 Internship Area", "12"),
        ("   4.2 Project Title", "12"),
        ("   4.3 Project Plan (Stages 1 to 6)", "12"),
        ("5. Background and Problem Description", "13"),
        ("   5.1 Background", "13"),
        ("   5.2 Problem Statement", "13"),
        ("   5.3 Proposed Solution", "13"),
        ("6. System Design and Development", "14"),
        ("   6.1 Application Structure", "14"),
        ("   6.2 User Interface Design", "14"),
        ("   6.3 Interactions", "14"),
        ("   6.4 Feedback and System Status", "14"),
        ("   6.5 Billing and Financial Calculation", "14"),
        ("   6.6 Desktop Responsive and Dialog Layout", "14"),
        ("7. Implementation and Testing", "15"),
        ("   7.1 Development Process", "15"),
        ("   7.2 Implementation of Components", "15"),
        ("   7.3 User Interaction", "15"),
        ("   7.4 Testing Approach", "15"),
        ("   7.5 Boundary and Functional Testing", "15"),
        ("   7.6 Improvements After Testing", "16"),
        ("   Figures and Screenshots", "16"),
        ("8. Outcomes", "18"),
        ("   8.1 Functional Outcome", "18"),
        ("   8.2 User Experience Outcome", "18"),
        ("   8.3 Technical Outcome", "18"),
        ("   8.4 Professional Outcome", "18"),
        ("   8.5 Learning Outcome", "18"),
        ("9. Conclusion and Recommendations", "19"),
        ("   9.1 Conclusion", "19"),
        ("   9.2 Recommendations / Future Scope", "19"),
        ("References and Project Links", "20"),
    ]
    
    for title, pg in toc_items:
        row = toc_table.add_row()
        c0, c1 = row.cells
        p0 = c0.paragraphs[0]
        p0.paragraph_format.space_after = Pt(2)
        p0.paragraph_format.line_spacing = 1.1
        r0 = p0.add_run(title)
        r0.font.name = "Calibri"
        r0.font.size = Pt(10)
        if not title.startswith("   "):
            r0.bold = True
            
        p1 = c1.paragraphs[0]
        p1.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        p1.paragraph_format.space_after = Pt(2)
        p1.paragraph_format.line_spacing = 1.1
        r1 = p1.add_run(pg)
        r1.font.name = "Calibri"
        r1.font.size = Pt(10)
        if not title.startswith("   "):
            r1.bold = True

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGES 8-9: 1. INTRODUCTION
    # -------------------------------------------------------------
    add_h1("1. INTRODUCTION", space_before=10)
    
    add_h2("1.1 Introduction to the Internship")
    add_body("A summer internship provides an opportunity to experience the practical side of software engineering in a professional setting. Academic courses introduce fundamental concepts such as object-oriented programming, relational databases, data structures, and algorithmic complexity. Applying these concepts to a complete, standalone software system requires an understanding of architectural structure, transaction lifecycles, user interface state management, and testing methodologies.")
    add_body("During the summer internship at Pratinik Infotech, I was tasked with designing and implementing an enterprise desktop management system for luxury resort operations. The internship involved analyzing domain requirements, designing normalized relational database tables, implementing a responsive desktop interface using JavaFX, coordinating business logic through services, and verifying system behavior with unit tests.")

    add_h2("1.2 Purpose of the Project")
    add_body("Luxury resorts often operate across large properties in coastal, cliffside, or island locations. These remote locations frequently suffer from intermittent or slow internet connectivity. Modern web-based Property Management Systems (PMS) depend heavily on constant cloud access. When internet connections drop, front-desk staff cannot check in guests, retrieve room availability, or generate billing invoices, leading to customer frustration and operational bottlenecks.")
    add_body("The purpose of CoralCove is to provide an offline-first desktop management platform that runs locally on front-desk computers. By using an embedded SQLite 3 database and modern Java 21, the application delivers immediate response times with zero network latency. The application manages guest reservations, room inventory across distinct zones (Beach, Lagoon, Cliff, Garden), Indian regulatory KYC compliance, room turnover synchronization, and master folio billing without requiring continuous external internet access.")

    add_h2("1.3 Objectives")
    add_body("The major objectives of the CoralCove project were:")
    add_bullet("To develop an offline-capable, standalone desktop Property Management System using Java 21 and JavaFX.")
    add_bullet("To design an embedded relational database schema in SQLite with primary keys, foreign keys, and indexes for fast data retrieval.")
    add_bullet("To build a reservation engine that prevents room booking collisions through date-overlap validation.")
    add_bullet("To implement regulatory guest KYC verification, including 12-digit Aadhaar format checks and document image staging.")
    add_bullet("To create an automated folio billing module that calculates room tariffs, meal plans, incidental services, and GST taxes.")
    add_bullet("To synchronize housekeeping workflows with front-desk check-in and check-out events.")
    add_bullet("To establish role-based access control with secure PBKDF2 password hashing.")
    add_bullet("To create a clean user interface with custom CSS styling and multi-resolution application icons.")

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGE 10: 2. ORGANIZATION AND BUSINESS SECTOR
    # -------------------------------------------------------------
    add_h1("2. ORGANIZATION AND BUSINESS SECTOR", space_before=10)
    
    add_h2("2.1 Introduction to the Hospitality Management Software Sector")
    add_body("The hospitality sector is an essential component of the global service economy, comprising luxury resorts, hotels, boutique stays, and ecotourism lodges. Effective resort operations depend heavily on Property Management Systems (PMS). These systems manage room inventories, guest lifecycles, billing, and back-of-house coordination.")
    add_body("Traditionally, independent resorts relied on manual ledgers or generic spreadsheets. While spreadsheets offer initial flexibility, they lack relational constraints, concurrency safety, and audit logs. Multiple staff members editing spreadsheets concurrently often create duplicate reservations, conflicting rates, and billing inaccuracies.")
    add_body("In recent years, cloud PMS solutions have gained popularity. However, coastal and remote properties in developing tourist corridors often face unreliable broadband connectivity. A service disruption renders cloud interfaces inaccessible, leaving front-desk staff unable to verify arriving guests or issue invoices. Consequently, there is steady demand for resilient, offline-first desktop applications that provide rich local data storage, strong transactional guarantees, and instantaneous query responses while remaining simple to operate.")

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGE 11: 3. OVERVIEW OF PRATINIK INFOTECH
    # -------------------------------------------------------------
    add_h1("3. OVERVIEW OF PRATINIK INFOTECH", space_before=10)
    
    add_h2("3.1 Organization Overview")
    add_body("Pratinik Infotech is a software development and technical services organization located in Pune, Maharashtra, India. The company focuses on custom software engineering, web application development, desktop platforms, and student technical training programs.")
    add_body("The organization provides an internship structure that allows students to gain direct experience in practical software engineering. Weekly task reviews, code architecture discussions, and milestone submissions help students understand how real software projects are organized and executed.")

    add_h2("3.2 Nature of Work")
    add_body("The engineering work at Pratinik Infotech spans several technical domains:")
    add_bullet("Enterprise application architecture and domain-driven design.")
    add_bullet("Relational database design, query optimization, and schema migrations.")
    add_bullet("Front-end and desktop user interface engineering.")
    add_bullet("Software verification, unit testing, and technical documentation.")

    add_h2("3.3 Importance of the Organization to the Sector")
    add_body("Pratinik Infotech helps bridge the gap between academic theory and industry engineering standards. By assigning realistic business requirements, the organization trains students to consider real-world constraints such as data integrity, offline resilience, and user experience. This preparation creates software developers capable of building robust business applications.")

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGE 12: 4. INTERNSHIP PLAN
    # -------------------------------------------------------------
    add_h1("4. INTERNSHIP PLAN", space_before=10)
    
    add_h2("4.1 Internship Area")
    add_body("Department / Domain: Desktop Systems and Java Engineering")
    add_body("The internship focused on the architecture, design, and implementation of an offline-first desktop Property Management System for luxury resort groups.")

    add_h2("4.2 Project Title")
    add_body("CoralCove - Luxury Resort Management Desktop Application")

    add_h2("4.3 Project Plan")
    add_body("The project was carried out in six planned stages across the eight-week internship duration:")
    add_bullet("Stage 1 - Requirement Understanding: Analyzed resort operational workflows, front-desk check-in steps, room inventories, and Indian KYC compliance rules.")
    add_bullet("Stage 2 - Database Architecture: Designed fourteen normalized relational tables in SQLite, configured foreign keys, constraints, and initial migration scripts.")
    add_bullet("Stage 3 - Core DAO & Service Implementation: Implemented Data Access Objects using parameterized JDBC queries and built business services for reservations, authentication, and billing.")
    add_bullet("Stage 4 - User Interface Development: Built JavaFX desktop screens, integrated custom CSS theme styling, and set up the desktop shell with a sidebar navigation rail.")
    add_bullet("Stage 5 - Refinement & Integration: Added dialog form spacing, scrollpane containers, pinned action buttons, and high-resolution logo icon assets.")
    add_bullet("Stage 6 - Testing & Documentation: Conducted JUnit 5 automated test verification, manual functional testing, and produced comprehensive technical documentation.")

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGE 13: 5. BACKGROUND AND PROBLEM DESCRIPTION
    # -------------------------------------------------------------
    add_h1("5. BACKGROUND AND PROBLEM DESCRIPTION", space_before=10)
    
    add_h2("5.1 Background")
    add_body("Luxury resorts offer premium accommodations spread across multiple geographic zones, including private beachfront villas, overwater lagoon suites, cliffside chalets, and garden residences. Unlike compact city hotels, luxury resorts manage diverse revenue streams, such as in-room dining, wellness spa therapies, scuba excursions, and private boat charters. Furthermore, hospitality regulations in India require mandatory guest KYC verification, specifically recording and validating 12-digit Aadhaar identity documents.")

    add_h2("5.2 Problem Statement")
    add_body("Resort operators face several recurring operational challenges:")
    add_bullet("Network Instability: Remote coastal locations frequently experience broadband outages. Cloud PMS software freezes, preventing guest arrivals, check-outs, and billing.")
    add_bullet("Double-Booking Risks: Manual booking records or unsynchronized spreadsheets lead to overlapping reservations for the same room.")
    add_bullet("KYC Record Disorganization: Paper ID copies or unindexed digital scans cause compliance risks and delays during guest registration.")
    add_bullet("Billing Discrepancies: Manual calculation of room nights, meal plan rates, incidental services, and Goods and Services Tax (GST) creates billing errors.")
    add_bullet("Housekeeping Miscommunication: Delays in notifying cleaning staff when a guest departs result in rooms remaining marked dirty when new guests arrive.")

    add_h2("5.3 Proposed Solution")
    add_body("CoralCove solves these problems through an integrated, offline-first desktop platform. Operating locally on Java 21 and an embedded SQLite 3 database, CoralCove eliminates network latency. The application enforces mathematical date-collision algorithms to prevent double-booking, provides integrated 12-digit Aadhaar validation with secure local image staging, automates folio billing with GST calculation, and synchronizes room turnover states immediately upon guest check-in and check-out.")

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGE 14: 6. SYSTEM DESIGN AND DEVELOPMENT
    # -------------------------------------------------------------
    add_h1("6. SYSTEM DESIGN AND DEVELOPMENT", space_before=10)
    
    add_h2("6.1 Application Structure")
    add_body("CoralCove follows a clean four-tier architectural pattern:")
    add_bullet("Presentation Tier: JavaFX views, dialogs, custom components, and CSS styles.")
    add_bullet("Service Tier: Business logic, validation rules, transaction coordination, and invariants.")
    add_bullet("Data Access Object (DAO) Tier: SQL query execution and ResultSet object mapping.")
    add_bullet("Database Tier: Embedded SQLite 3 relational database running in WAL mode.")

    add_h2("6.2 User Interface Design")
    add_body("The user interface uses a luxury coastal aesthetic. The visual theme combines a warm sand backdrop (#F5F0E6) with a deep slate navy navigation rail (#1B2430) and warm gold accents (#C4A35A). Resort zones are visually differentiated with dedicated photo cards and status badges.")

    add_h2("6.3 Interactions")
    add_body("The interaction flow was designed for speed. Front-desk staff can search reservations by reference or guest name, select check-in dates using visual calendars, and transition room states with single-click actions.")

    add_h2("6.4 Feedback and System Status")
    add_body("The application provides clear visual feedback. Active rooms display real-time status chips: READY (green), OCCUPIED (blue), DIRTY (amber), HELD (purple), and OUT_OF_ORDER (red). Error dialogs explain validation failures clearly, such as date collision conflicts or weak passwords.")

    add_h2("6.5 Billing and Financial Calculation")
    add_body("Monetary values are computed using Java's BigDecimal with half-up rounding to prevent floating-point calculation errors. Master folios aggregate room tariffs, meal plan supplements, and incidental POS charges, automatically computing 9% CGST and 9% SGST (18% total GST) with itemized receipts.")

    add_h2("6.6 Desktop Responsive and Dialog Layout")
    add_body("Dialog forms are configured with generous padding (Insets of 20, 26, 16, 26) and horizontal and vertical gaps (18px and 12px). Form bodies are wrapped in scrollable containers with column constraints set to grow automatically. This prevents text from crowding dialog edges across varying screen resolutions.")

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGES 15-17: 7. IMPLEMENTATION AND TESTING
    # -------------------------------------------------------------
    add_h1("7. IMPLEMENTATION AND TESTING", space_before=10)
    
    add_h2("7.1 Development Process")
    add_body("Development was executed modularly using Git version control. Features were developed in isolation: database tables and DAOs first, followed by business service rules, and finally the JavaFX presentation components. This modular approach enabled targeted unit testing before assembling the full desktop application.")

    add_h2("7.2 Implementation of Components")
    add_body("Key components implemented in CoralCove include:")
    add_bullet("StayService & StayBookingDAO: Coordinates reservation creation, validates date ranges, and verifies lodge vacancy using mathematical overlap queries.")
    add_bullet("ArrivalService: Coordinates front-desk check-in, verifies Aadhaar status, updates lodge state to OCCUPIED, and opens master billing folios.")
    add_bullet("BillingService & InvoiceDAO: Manages master folios, records incidental charges, applies discounts, logs payments, and calculates GST taxes.")
    add_bullet("LodgeCareService: Manages housekeeping queues, attendant task assignments, and facility maintenance defect reports.")
    add_bullet("PasswordHasher: Hashes passwords using PBKDF2 with HMAC-SHA256 across 210,000 iterations and 16-byte random salts.")

    add_h2("7.3 User Interaction")
    add_body("The main window layout (ShellView) features a persistent vertical sidebar for navigation and a responsive content area. Asynchronous background execution (Fx.run) ensures database queries execute off the JavaFX UI thread, keeping the user interface smooth and responsive.")

    add_h2("7.4 Testing Approach")
    add_body("Testing combined automated unit tests and manual functional verification. Automated tests utilized JUnit 5 running against isolated temporary databases. This approach guaranteed that database tests did not modify production data files.")

    add_h2("7.5 Boundary and Functional Testing")
    add_body("Extensive boundary testing was conducted across several critical areas:")
    add_bullet("Date Overlap Logic: Verified that reservations reject overlapping date ranges for the same lodge while allowing back-to-back bookings where check-out matches the next check-in.")
    add_bullet("Aadhaar KYC Validation: Tested valid 12-digit Indian Aadhaar numbers, rejecting short numbers, alphabetic characters, and invalid checksum sequences.")
    add_bullet("Monetary Rounding: Tested folio tax calculations across multiple incidental line items to verify half-up rounding precision.")
    add_bullet("Authentication & Permissions: Verified that non-admin accounts cannot access restricted modules like Settings and Financial Reports.")

    add_h2("7.6 Improvements After Testing")
    add_body("Iterative testing identified several user interface areas that required refinement:")
    add_bullet("Dialog Spacing: Modal dialog forms previously crowded window borders. Dialogs.java was refactored with generous edge insets and column constraints.")
    add_bullet("Profile Dialog Actions: ProfileDialog was updated with an explicit pinned bottom bar containing Save Profile and Cancel buttons.")
    add_bullet("Window Taskbar Branding: Multi-resolution application icons (16x16 to 256x256) were integrated into the primary application stage to ensure clear branding in Windows taskbars.")

    doc.add_page_break()

    # -------------------------------------------------------------
    # SCREENSHOTS / FIGURES (Pages 16-17)
    # -------------------------------------------------------------
    add_h1("Figures and Screenshots", space_before=10, space_after=12)
    
    figures = [
        ("docs/screenshots/figure1_stays_dashboard.png", "Figure 1: CoralCove Stays and Reservation Dashboard with Hero Banner and Table View"),
        ("docs/screenshots/figure2_new_stay_dialog_clean.png", "Figure 2: New Stay Booking Modal Dialog with Date Selection and Form Spacing"),
        ("docs/screenshots/figure3_profile_dialog_clean.png", "Figure 3: User Profile Dialog with Avatar Style Selector, Contact Fields, and Action Buttons"),
        ("docs/screenshots/figure4_app_icon_clean.png", "Figure 4: CoralCove Application Brand Icon and Visual Identity Asset")
    ]
    
    for img_path, caption in figures:
        if os.path.exists(img_path):
            p_img = doc.add_paragraph()
            p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p_img.paragraph_format.space_before = Pt(8)
            p_img.paragraph_format.space_after = Pt(4)
            # Size appropriately
            if "figure4" in img_path:
                p_img.add_run().add_picture(img_path, width=Inches(2.0))
            elif "figure1" in img_path:
                p_img.add_run().add_picture(img_path, width=Inches(5.5))
            else:
                p_img.add_run().add_picture(img_path, width=Inches(3.8))
                
            p_cap = doc.add_paragraph()
            p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p_cap.paragraph_format.space_after = Pt(12)
            rc = p_cap.add_run(caption)
            rc.font.name = "Calibri"
            rc.font.size = Pt(9.5)
            rc.font.italic = True
            rc.font.color.rgb = RGBColor(80, 80, 80)

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGE 18: 8. OUTCOMES
    # -------------------------------------------------------------
    add_h1("8. OUTCOMES", space_before=10)
    
    add_h2("8.1 Functional Outcome")
    add_body("A fully functional resort management desktop application was developed, covering twelve core modules. The system successfully handles reservations, guest check-ins, Aadhaar verification, master folio billing, housekeeping tasks, employee attendance, and inventory tracking without requiring an active internet connection.")

    add_h2("8.2 User Experience Outcome")
    add_body("The user interface offers straightforward navigation, fast response times, and consistent visual styling. Screen headers, zone photography, status chips, and formatted dialogs make operations intuitive for front-desk operators and housekeeping staff.")

    add_h2("8.3 Technical Outcome")
    add_body("The project established a clean four-tier architecture in Java 21 and JavaFX. The SQLite database operates with foreign key constraints, indexes, and Write-Ahead Logging for durability. Automated unit testing with JUnit 5 validated core invariants with eighteen passing tests and zero regressions.")

    add_h2("8.4 Professional Outcome")
    add_body("The internship provided valuable insight into how software projects are planned, structured, and refined. Working with modular architectures, database schemas, and version control reinforced the importance of systematic engineering practices.")

    add_h2("8.5 Learning Outcome")
    add_body("The project enhanced my proficiency in Java 21, JavaFX Scene Graph mechanics, CSS UI skinning, relational data modeling, JDBC transaction coordination, and cryptographic password hashing.")

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGE 19: 9. CONCLUSION AND RECOMMENDATIONS
    # -------------------------------------------------------------
    add_h1("9. CONCLUSION AND RECOMMENDATIONS", space_before=10)
    
    add_h2("9.1 Conclusion")
    add_body("The summer internship at Pratinik Infotech provided practical exposure to enterprise desktop software engineering. Building CoralCove demonstrated how real-world hospitality management challenges can be addressed through thoughtful software architecture.")
    add_body("The project unified room inventory management, guest KYC compliance, folio accounting, and housekeeping operations into a cohesive desktop platform. Implementing the application in Java 21 with embedded SQLite 3 proved that offline-first desktop systems provide superior responsiveness, data privacy, and operational resilience for remote resorts.")
    add_body("Working through the complete development process, from requirements analysis to testing and documentation, provided hands-on experience that reinforced and extended classroom learning.")

    add_h2("9.2 Recommendations / Future Scope")
    add_body("The current application can be expanded in several valuable directions:")
    add_bullet("Multi-Terminal LAN Synchronization: Implement a local network synchronization protocol allowing multiple front-desk terminals to share the SQLite database concurrently.")
    add_bullet("Automated Offsite Cloud Backup: Add an encrypted background backup service to upload daily database snapshots when internet connectivity is available.")
    add_bullet("Hardware Device Integration: Integrate magnetic card door key encoders and passport/Aadhaar optical scanners directly into the check-in view.")
    add_bullet("Payment Gateway Terminals: Connect physical POS card swipe terminals via local serial/USB interfaces for automated charge authorization.")
    add_bullet("Guest Mobile Companion: Provide a lightweight local Wi-Fi web portal for guests to browse spa menus and review current folio charges.")

    doc.add_page_break()

    # -------------------------------------------------------------
    # PAGE 20: REFERENCES & PROJECT LINKS
    # -------------------------------------------------------------
    add_h1("REFERENCES", space_before=10)
    
    add_body("1. SRM University, AP, Summer Internship Course - Project Report Guidelines, 2026-27.")
    add_body("2. Oracle Corporation, Java Platform, Standard Edition 21 Documentation, Oracle Technology Network, 2023.")
    add_body("3. OpenJFX Project, JavaFX 21 API Specification and CSS Reference Guide, openjfx.io, 2023.")
    add_body("4. SQLite Development Team, SQLite 3 Documentation and Write-Ahead Logging Specifications, sqlite.org, 2024.")
    add_body("5. Pratinik Infotech, Internship Project Guidelines, Architecture Standards, and Task Specifications, Pune, Maharashtra, 2026.")

    add_h1("PROJECT LINKS", space_before=20)
    add_body("Source Code Repository (GitHub):")
    add_body("https://github.com/applejuice093/Coralcove")
    add_body("Complete Technical Specification Documentation:")
    add_body("Refer to doc.md in the project root directory for exhaustive table catalogs, DAO methods, service APIs, and sequence diagrams.")

    output_path = "CoralCove_Internship_Project_Report.docx"
    doc.save(output_path)
    print(f"Successfully generated {output_path}!")

if __name__ == "__main__":
    create_report()
