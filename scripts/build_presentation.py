import pptx
import shutil

src = r'C:/Users/anike/.gemini/antigravity/brain/3bec7cb7-a6aa-4ab6-88a4-f16f1dc57947/.user_uploaded/media_1794830000784_30d7edc3.pptx'
dst = 'CoralCove_Presentation.pptx'
shutil.copy(src, dst)
prs = pptx.Presentation(dst)

def set_para_text(p, new_text):
    if p.runs:
        p.runs[0].text = new_text
        for r in p.runs[1:]:
            r.text = ""
    else:
        p.text = new_text

def update_footer(slide):
    for s in slide.shapes:
        if s.has_text_frame:
            for p in s.text_frame.paragraphs:
                if "Summer Internship Project" in p.text:
                    set_para_text(p, p.text.replace("QUIZLY", "CoralCove"))

# SLIDE 1
s1 = prs.slides[0]
for s in s1.shapes:
    if s.has_text_frame:
        for p in s.text_frame.paragraphs:
            if "SUMMER INTERNSHIP" in p.text:
                set_para_text(p, "SUMMER INTERNSHIP - FINAL PROJECT PRESENTATION")
            elif "QUIZLY" in p.text:
                set_para_text(p, "CORALCOVE")
            elif "Interactive Quiz" in p.text:
                set_para_text(p, "Luxury Resort Management Desktop Application - JavaFX & SQLite")
            elif "Name-Roll Number" in p.text or "SRM University" in p.text:
                set_para_text(p, "Aniket Sharma - AP24110010141\nPratinik Infotech - Remote\nSRM University, AP")

# SLIDE 2 (Agenda)
s2 = prs.slides[1]
update_footer(s2)

# SLIDE 3 (Internship Overview)
s3 = prs.slides[2]
update_footer(s3)
for s in s3.shapes:
    if s.has_text_frame:
        for p in s.text_frame.paragraphs:
            txt = p.text.strip()
            if "IT services company" in txt:
                set_para_text(p, "Technology services organization offering structured software engineering internship programs across enterprise domains.")
            elif txt == "Front-End Development":
                set_para_text(p, "Desktop Systems & Java Engineering")
            elif "Two-month Front-End" in txt:
                set_para_text(p, "Two-month software development internship focusing on architecture, database design, GUI implementation, and automated testing.")

# SLIDE 4 (Problem Statement & Objectives)
s4 = prs.slides[3]
update_footer(s4)
for s in s4.shapes:
    if s.has_text_frame:
        for p in s.text_frame.paragraphs:
            txt = p.text.strip()
            if "Students need an engaging" in txt:
                set_para_text(p, "Luxury resorts in coastal destinations frequently experience internet drops that disrupt cloud-based management systems. Front desk operators need a reliable, offline-first desktop platform that eliminates booking collisions, tracks Aadhaar guest identity documents, and coordinates room turnover without network latency.")
            elif "Develop an interactive" in txt:
                set_para_text(p, "Build a resilient offline Property Management System desktop application using Java 21 and JavaFX.")
            elif "Provide category-based" in txt:
                set_para_text(p, "Design a transactional SQLite relational database with strict foreign keys and automated migrations.")
            elif "Implement timed questions" in txt:
                set_para_text(p, "Implement reservation booking with mathematical date collision detection and stay party linking.")
            elif "Provide automatic scoring" in txt:
                set_para_text(p, "Integrate regulatory guest KYC compliance with 12-digit Aadhaar checksum validation.")
            elif "Ensure a responsive" in txt:
                set_para_text(p, "Provide itemized folio billing with automated room tariffs, incidental charges, and GST calculation.")

# SLIDE 5 (Solution Design)
s5 = prs.slides[4]
update_footer(s5)
for s in s5.shapes:
    if s.has_text_frame:
        for p in s.text_frame.paragraphs:
            txt = p.text.strip()
            if "Four core sections" in txt:
                set_para_text(p, "Four core operational workflows of the CoralCove architecture")
            elif "Home / Registration" in txt:
                set_para_text(p, "Authentication & Security")
            elif "Landing page where" in txt:
                set_para_text(p, "PBKDF2 salted password verification, role assignment, and mandatory first-time password rotation.")
            elif "Dashboard" in txt:
                set_para_text(p, "Front Desk & Inventory")
            elif "Category cards" in txt:
                set_para_text(p, "Multi-zone lodge inventory grid, live stay status, Aadhaar KYC verification, and one-click check-in.")
            elif "Quiz Interface" in txt:
                set_para_text(p, "Folio Billing & POS")
            elif "Displays one question" in txt:
                set_para_text(p, "Automated room tariff calculation, dining and spa incidental charges, CGST/SGST taxes, and payment recording.")
            elif "Results" in txt:
                set_para_text(p, "Housekeeping & Care")
            elif "Display the user" in txt:
                set_para_text(p, "Room status transitions (READY, DIRTY, OCCUPIED), attendant task queues, and facility maintenance defect logs.")
            elif "Data (questions, categories" in txt:
                set_para_text(p, "All operational data and state transitions are handled locally via an embedded SQLite database in WAL mode, guaranteeing zero cloud dependency and sub-millisecond query performance.")

# SLIDE 6 (Technology Stack)
s6 = prs.slides[5]
update_footer(s6)
for s in s6.shapes:
    if s.has_text_frame:
        for p in s.text_frame.paragraphs:
            txt = p.text.strip()
            if "Built entirely with" in txt:
                set_para_text(p, "Built with modern LTS Java enterprise desktop technologies and zero bloated frameworks")
            elif txt == "H":
                set_para_text(p, "J")
            elif txt == "HTML5":
                set_para_text(p, "Java 21 (LTS)")
            elif "Structure and sematic" in txt:
                set_para_text(p, "Core enterprise runtime with virtual threads, records, and strong type safety.")
            elif txt == "C":
                set_para_text(p, "F")
            elif txt == "CSS3":
                set_para_text(p, "JavaFX 21")
            elif "Responsive layout, Styling" in txt:
                set_para_text(p, "Hardware-accelerated desktop UI, Scene Graph, CSS styling, and high-DPI scaling.")
            elif txt == "J":
                set_para_text(p, "S")
            elif "JavaScript (ES6)" in txt:
                set_para_text(p, "SQLite 3")
            elif "Quiz logic, user" in txt:
                set_para_text(p, "Embedded ACID transactional database with WAL mode and zero-config local storage.")
            elif txt == "V":
                set_para_text(p, "M")
            elif txt == "Vercel":
                set_para_text(p, "Maven & Git")
            elif "Deployment / build tool" in txt:
                set_para_text(p, "Build automation, JUnit 5 test suites, dependency management, and GitHub version control.")
            elif "Deployment: Vercel" in txt:
                set_para_text(p, "Runtime: OpenJDK 21\nRepository: GitHub")

# SLIDE 7 (Key Features)
s7 = prs.slides[6]
update_footer(s7)
for s in s7.shapes:
    if s.has_text_frame:
        for p in s.text_frame.paragraphs:
            txt = p.text.strip()
            if "Category-Based Quizzes" in txt:
                set_para_text(p, "Multi-Zone Room Inventory")
            elif "Users can select from the available" in txt:
                set_para_text(p, "Visual management across Beach, Lagoon, Cliff, and Garden zones with instant status filtering.")
            elif "Timed Questions" in txt:
                set_para_text(p, "Aadhaar KYC Verification")
            elif "Questions include timed" in txt:
                set_para_text(p, "Indian regulatory compliance with 12-digit format validation, document staging, and modal inspection.")
            elif "Automatic Scoring and Badges" in txt:
                set_para_text(p, "Master Folio Accounting")
            elif "The application calculates the user" in txt:
                set_para_text(p, "Itemized room tariffs, meal plans, spa and dining add-ons with automated CGST/SGST tax math.")
            elif "User Registration & Sign-In" in txt:
                set_para_text(p, "Housekeeping Synchronization")
            elif "The application provides a client-side sign-up" in txt:
                set_para_text(p, "Automated room state transitions upon check-in/out, attendant task queues, and supervisor inspection pass/fail.")
            elif "Responsive Interface" in txt:
                set_para_text(p, "Role-Based Access Control")
            elif "The interface is designed to work across" in txt:
                set_para_text(p, "Strict permission enforcement for Admin, Manager, Front Desk, and Housekeeping across 12 application modules.")
            elif "Interactive User Experience" in txt:
                set_para_text(p, "Offline-First Desktop Speed")
            elif "Dynamic JavaScript functionality" in txt:
                set_para_text(p, "Sub-millisecond query execution on local SQLite storage without internet latency or third-party outages.")

# SLIDE 8 (Implementation Journey)
s8 = prs.slides[7]
update_footer(s8)
for s in s8.shapes:
    if s.has_text_frame:
        for p in s.text_frame.paragraphs:
            txt = p.text.strip()
            if "8 weeks, from planning" in txt:
                set_para_text(p, "8 weeks from domain modeling to production-grade desktop application")
            elif "Project planning & requirement" in txt:
                set_para_text(p, "Requirements analysis & domain modeling")
            elif "HTML structure & page" in txt:
                set_para_text(p, "SQLite schema design & migrations")
            elif "CSS styling & responsive" in txt:
                set_para_text(p, "JavaFX clean architecture & base theme")
            elif "Quiz data & JavaScript" in txt:
                set_para_text(p, "Reservation engine & date collision logic")
            elif "Timer, scoring & interaction" in txt:
                set_para_text(p, "Aadhaar KYC validation & file storage")
            elif "Sign-up / sign –in" in txt:
                set_para_text(p, "Folio billing, GST math & PDF generation")
            elif "Testing, debugging & responsive" in txt:
                set_para_text(p, "Dialog spacing, scrolling & luxury branding")
            elif "Vite build configuration" in txt:
                set_para_text(p, "JUnit 5 testing, technical docs & git release")

# SLIDE 9 (Testing & Outcomes)
s9 = prs.slides[8]
update_footer(s9)
for s in s9.shapes:
    if s.has_text_frame:
        for p in s.text_frame.paragraphs:
            txt = p.text.strip()
            if "Scoring verified across" in txt:
                set_para_text(p, "Date collision verification across overlapping, adjacent, and invalid booking dates")
            elif "Timer, Customization and" in txt:
                set_para_text(p, "Aadhaar 12-digit format and Verhoeff-style checksum validation tests")
            elif "Sign-up and sign-in flow" in txt:
                set_para_text(p, "Folio balance reconciliation and half-up decimal tax rounding precision tests")
            elif "Responsive layout testing" in txt:
                set_para_text(p, "Role-based permission gating and session authentication flow tests")
            elif "Final build and deployment" in txt:
                set_para_text(p, "High-DPI desktop scaling, dialog scrolling, and window resize tests")
            elif "Functional QUIZLY quiz" in txt:
                set_para_text(p, "Functional CoralCove resort management desktop application successfully built and tested")
            elif "Multiple working quiz" in txt:
                set_para_text(p, "Comprehensive 12-module operational suite supporting all luxury resort workflows")
            elif "Interactive question, timer" in txt:
                set_para_text(p, "Zero-regression test suite with 18 automated JUnit 5 test cases passing")
            elif "Client-side sign-up and sign-in" in txt:
                set_para_text(p, "Custom luxury visual design system with multi-resolution desktop branding")
            elif "Application successfully built and deployed" in txt:
                set_para_text(p, "Production-ready codebase, complete 900-line documentation, and active GitHub repo")

# SLIDE 10 (Learnings & Conclusion)
s10 = prs.slides[9]
update_footer(s10)
for s in s10.shapes:
    if s.has_text_frame:
        for p in s.text_frame.paragraphs:
            txt = p.text.strip()
            if "This internship provided hands-on" in txt:
                set_para_text(p, "This internship provided hands-on experience across the entire desktop software engineering lifecycle, from domain requirements analysis and database modeling to GUI implementation, testing, UI refactoring, and documentation.")
            elif "Strengthened HTML" in txt:
                set_para_text(p, "Mastered enterprise Java 21 features, virtual threads, and clean layered architecture")
            elif "Improved understanding of responsive" in txt:
                set_para_text(p, "Strengthened expertise in hardware-accelerated JavaFX desktop GUI design and CSS skinning")
            elif "Built independent problem" in txt:
                set_para_text(p, "Deepened understanding of relational schema design, ACID transactions, and JDBC indexing")
            elif "Gained experience with JavaScript" in txt:
                set_para_text(p, "Gained practical skills in automated test-driven verification, git workflow, and technical documentation")
            elif "Future Scope:" in txt:
                set_para_text(p, "Future Scope: Multi-terminal peer-to-peer synchronization, automated offsite database backups, payment gateway hardware integration, and biometric scanner support.")

# SLIDE 11 (Thank You)
s11 = prs.slides[10]

prs.save('CoralCove_Presentation.pptx')
print("Successfully built CoralCove_Presentation.pptx!")
