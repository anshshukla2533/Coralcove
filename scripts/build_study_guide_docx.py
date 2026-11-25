import os
import re
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

# Palette matching CoralCove theme.css
NAVY = RGBColor(0x1B, 0x24, 0x30)
NAVY_HEX = "1B2430"
SLATE = RGBColor(0x33, 0x41, 0x55)
GOLD = RGBColor(0xA6, 0x7C, 0x2E)
GOLD_HEX = "C4A35A"
SAND_HEX = "F7F4EE"
CODE_BG_HEX = "F3F5F8"
ZEBRA_HEX = "F8FAFC"
BORDER_HEX = "D0D7DE"
TEXT_DARK = RGBColor(0x1E, 0x29, 0x3B)
TEXT_MUTED = RGBColor(0x64, 0x74, 0x8B)
CODE_COLOR = RGBColor(0x0F, 0x17, 0x2A)
INLINE_CODE_COLOR = RGBColor(0x9A, 0x34, 0x12)

ASCII_ARCH = """+------------------------------------------------------------------------------+
| LAYER 1: PRESENTATION TIER (coralcove.ui + Main.java)                        |
|   JavaFX Views, Dialogs, Sidebar, TopBar, ScreenHeader, theme.css            |
|   Runs on FX Application Thread; dispatches DB work via Fx.run()             |
+------------------------------------------------------------------------------+
                      |  Passes Model DTOs / Primitives
                      v
+------------------------------------------------------------------------------+
| LAYER 2: BUSINESS SERVICE TIER (coralcove.service)                           |
|   AuthorizationService (RBAC), ResortScope, Validation, Business Invariants  |
|   Opens ACID Transactions via Transactions.in() & calls Session.dataChanged()|
+------------------------------------------------------------------------------+
                      |  Passes active java.sql.Connection
                      v
+------------------------------------------------------------------------------+
| LAYER 3: DATA ACCESS OBJECT TIER (coralcove.dao)                             |
|   14 DAO Classes executing Parameterized PreparedStatements (zero SQLi)      |
|   Maps SQLite ResultSets <-> coralcove.model POJOs                           |
+------------------------------------------------------------------------------+
                      |  Executes SQL / Reads ResultSet
                      v
+------------------------------------------------------------------------------+
| LAYER 4: EMBEDDED STORAGE TIER (coralcove.database + SQLite 3)               |
|   DatabaseManager, DatabaseMigration (v1 -> v2), DatabaseInitializer         |
|   23 Relational Tables in data/coralcove.db (foreign_keys=ON, timeout=5000)  |
+------------------------------------------------------------------------------+

  [Cross-Cutting Layers used across Tiers 1-3]
  * coralcove.model : 20 pure POJOs & Enums (zero SQL, zero JavaFX)
  * coralcove.util  : Money, PasswordHasher, DateUtils, PDFGenerator, CSVExporter"""

ASCII_MIGRATION = """[DatabaseManager.init(dbPath)]
       |
       v
1. Load org.sqlite.JDBC & create parent folder (data/)
       |
       v
2. open() Connection -> PRAGMA foreign_keys = ON; PRAGMA busy_timeout = 5000;
       |
       v
3. DatabaseMigration.migrate(conn) -> ensures schema_version table exists
       |
       +--> Is MAX(version) < 1?
       |       YES -> DatabaseInitializer.createSchema() (22 tables + 10 indexes)
       |           -> DatabaseInitializer.seed() (default resort, admin, 7 categories, 8 menu items)
       |           -> INSERT version 1 into schema_version
       |
       +--> Is MAX(version) < 2?
       |       YES -> ALTER TABLE users  ADD display_name, email, phone, avatar_path
       |           -> ALTER TABLE guests ADD aadhar_verified, aadhar_image_path
       |           -> Backfill admin profile ('Aniket Sharma (Owner)')
       |           -> INSERT version 2 into schema_version
       |
       v
[Database Ready at data/coralcove.db -> 23 Tables Total!]"""

ASCII_ER = """                         +--------------------+
                         |      RESORTS       |
                         +--------------------+
                         | 1     | 1   | 1    | 1
         +---------------+       |     |      +---------------------+
         | *                     | *   | *                          | *
+------------------+   +----------------+   +--------------+   +-----------+
|      LODGES      |   |   EMPLOYEES    |   | EXPERIENCES  |   | INVENTORY |
+------------------+   +----------------+   +--------------+   |  _ITEMS   |
  ^ *      | 1           | 1      | 1             | 1          +-----------+
  |        |             | 0..1   | *             | *            ^ *    | 1
  | 1      | *           v        v               v              |      v *
+--------+ |      +----------+ +------------+ +------------+ +-------+ +--------+
| LODGE_ | |      |  USERS   | | EMPLOYEE_  | | EXPERIENCE | |SUPPLI | | STOCK_ |
|CATEGOR.| |      +----------+ | ATTENDANCE | | _BOOKINGS  | | ERS   | | TRANS. |
+--------+ |       | 1    | 1  +------------+ +------------+ +-------+ +--------+
           |       | *    | *                        ^ *
           |       v      v                          |
           |  +--------+ +----------+                | 1
           |  | AUDIT_ | | PAYMENTS |<---+           |
           |  | LOGS   | +----------+    | *         |
           |  +--------+                 |           |
           |        +--------------------+           |
           |        | 1                              |
           |   +----------+   1     *   +---------------+
           |   | INVOICES |------------>| INVOICE_ITEMS |
           |   +----------+             +---------------+
           |        ^ 0..1
           |        | 1
           +-->+---------------+   1    0..1   +------------+
               | STAY_BOOKINGS |-------------->|  ARRIVALS  |
               +---------------+               +------------+
                 ^ *        | 1
                 | 1        | *
             +--------+  +------------+        [Also linked to LODGES (1 -> *)]
             | GUESTS |->| STAY_PARTY |        * LODGE_CARE_TASKS (assigned to EMPLOYEES)
             +--------+  +------------+        * ESTATE_REPAIRS"""


def set_cell_shading(cell, fill_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}" w:val="clear"/>')
    tcPr.append(shd)


def set_cell_margins(cell, top=80, bottom=80, left=120, right=120):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
        node = OxmlElement(f'w:{m}')
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)


def set_cell_borders(cell, color=BORDER_HEX, sz="4", sides=("top", "bottom", "left", "right")):
    tcPr = cell._tc.get_or_add_tcPr()
    tcBorders = OxmlElement('w:tcBorders')
    for side in ("top", "bottom", "left", "right"):
        el = OxmlElement(f'w:{side}')
        if side in sides:
            el.set(qn('w:val'), 'single')
            el.set(qn('w:sz'), sz)
            el.set(qn('w:space'), '0')
            el.set(qn('w:color'), color)
        else:
            el.set(qn('w:val'), 'nil')
        tcBorders.append(el)
    tcPr.append(tcBorders)


def set_row_cant_split(row):
    trPr = row._tr.get_or_add_trPr()
    trPr.append(OxmlElement('w:cantSplit'))


def set_row_header(row):
    trPr = row._tr.get_or_add_trPr()
    trPr.append(OxmlElement('w:tblHeader'))


def add_field(run, field_text):
    fldChar1 = parse_xml(r'<w:fldChar %s w:fldCharType="begin"/>' % nsdecls('w'))
    instrText = parse_xml(r'<w:instrText %s xml:space="preserve"> %s </w:instrText>' % (nsdecls('w'), field_text))
    fldChar2 = parse_xml(r'<w:fldChar %s w:fldCharType="separate"/>' % nsdecls('w'))
    fldChar3 = parse_xml(r'<w:fldChar %s w:fldCharType="end"/>' % nsdecls('w'))
    run._r.append(fldChar1)
    run._r.append(instrText)
    run._r.append(fldChar2)
    run._r.append(fldChar3)


def add_toc_field(doc):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after = Pt(10)
    run = p.add_run()
    fldChar1 = parse_xml(r'<w:fldChar %s w:fldCharType="begin" w:dirty="true"/>' % nsdecls('w'))
    instrText = parse_xml(r'<w:instrText %s xml:space="preserve"> TOC \o "1-3" \h \z \u </w:instrText>' % nsdecls('w'))
    fldChar2 = parse_xml(r'<w:fldChar %s w:fldCharType="separate"/>' % nsdecls('w'))
    fldText = parse_xml(r'<w:t %s>Right-click here and choose "Update Field" in Microsoft Word to populate exact page numbers.</w:t>' % nsdecls('w'))
    fldChar3 = parse_xml(r'<w:fldChar %s w:fldCharType="end"/>' % nsdecls('w'))
    run._r.append(fldChar1)
    run._r.append(instrText)
    run._r.append(fldChar2)
    run._r.append(fldText)
    run._r.append(fldChar3)


def add_bottom_border(paragraph, color=GOLD_HEX, sz="8"):
    pPr = paragraph._p.get_or_add_pPr()
    pBdr = parse_xml(
        f'<w:pBdr {nsdecls("w")}>'
        f'<w:bottom w:val="single" w:sz="{sz}" w:space="4" w:color="{color}"/>'
        f'</w:pBdr>'
    )
    pPr.append(pBdr)


def add_rich_runs(p, text, base_size=10, base_color=TEXT_DARK, base_font="Segoe UI", bold_default=False):
    # Clean HTML <br> tags into spaces/newlines cleanly
    text = re.sub(r'<br\s*/?>', '  |  ', text)
    # Tokenize: bold **...** (allowing single * inside like java.sql.*), inline code `...`, italic *...*
    pattern = re.compile(r'(\*\*(?:(?!\*\*).)+\*\*|`[^`]+`|\*[^*]+\*)')
    pos = 0
    for m in pattern.finditer(text):
        if m.start() > pos:
            r = p.add_run(text[pos:m.start()])
            r.font.name = base_font
            r.font.size = Pt(base_size)
            r.font.color.rgb = base_color
            if bold_default:
                r.bold = True
        tok = m.group(0)
        if tok.startswith('`') and tok.endswith('`'):
            r = p.add_run(tok[1:-1])
            r.font.name = "Consolas"
            r.font.size = Pt(max(8.5, base_size - 0.5))
            r.font.color.rgb = INLINE_CODE_COLOR
            if bold_default:
                r.bold = True
        elif tok.startswith('**') and tok.endswith('**'):
            inner = tok[2:-2]
            # Handle code inside bold like **`ClassName`**
            sub_pat = re.compile(r'(`[^`]+`)')
            sp = 0
            for sm in sub_pat.finditer(inner):
                if sm.start() > sp:
                    r = p.add_run(inner[sp:sm.start()])
                    r.font.name = base_font
                    r.font.size = Pt(base_size)
                    r.font.color.rgb = base_color
                    r.bold = True
                r = p.add_run(sm.group(0)[1:-1])
                r.font.name = "Consolas"
                r.font.size = Pt(max(8.5, base_size - 0.5))
                r.font.color.rgb = NAVY
                r.bold = True
                sp = sm.end()
            if sp < len(inner):
                r = p.add_run(inner[sp:])
                r.font.name = base_font
                r.font.size = Pt(base_size)
                r.font.color.rgb = base_color
                r.bold = True
        elif tok.startswith('*') and tok.endswith('*'):
            r = p.add_run(tok[1:-1])
            r.font.name = base_font
            r.font.size = Pt(base_size)
            r.font.color.rgb = base_color
            r.italic = True
            if bold_default:
                r.bold = True
        pos = m.end()
    if pos < len(text):
        r = p.add_run(text[pos:])
        r.font.name = base_font
        r.font.size = Pt(base_size)
        r.font.color.rgb = base_color
        if bold_default:
            r.bold = True


def add_callout(doc, lines, border_hex=GOLD_HEX, bg_hex=SAND_HEX):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    tbl.autofit = False
    cell = tbl.rows[0].cells[0]
    cell.width = Inches(6.9)
    set_cell_shading(cell, bg_hex)
    set_cell_margins(cell, top=100, bottom=100, left=160, right=140)
    tcPr = cell._tc.get_or_add_tcPr()
    tcBorders = parse_xml(
        f'<w:tcBorders {nsdecls("w")}>'
        f'<w:left w:val="single" w:sz="24" w:space="0" w:color="{border_hex}"/>'
        f'<w:top w:val="single" w:sz="4" w:space="0" w:color="{BORDER_HEX}"/>'
        f'<w:right w:val="single" w:sz="4" w:space="0" w:color="{BORDER_HEX}"/>'
        f'<w:bottom w:val="single" w:sz="4" w:space="0" w:color="{BORDER_HEX}"/>'
        f'</w:tcBorders>'
    )
    tcPr.append(tcBorders)
    set_row_cant_split(tbl.rows[0])
    for idx, ln in enumerate(lines):
        p = cell.paragraphs[0] if idx == 0 else cell.add_paragraph()
        p.paragraph_format.space_before = Pt(0 if idx == 0 else 3)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.line_spacing = 1.15
        add_rich_runs(p, ln, base_size=9.5, base_color=TEXT_DARK)
    sp = doc.add_paragraph()
    sp.paragraph_format.space_before = Pt(0)
    sp.paragraph_format.space_after = Pt(4)


def add_code_block(doc, code_lines, caption=None):
    if caption:
        pc = doc.add_paragraph()
        pc.paragraph_format.space_before = Pt(6)
        pc.paragraph_format.space_after = Pt(2)
        pc.paragraph_format.keep_with_next = True
        rc = pc.add_run(caption)
        rc.font.name = "Segoe UI"
        rc.font.size = Pt(9)
        rc.bold = True
        rc.font.color.rgb = GOLD

    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    tbl.autofit = False
    cell = tbl.rows[0].cells[0]
    cell.width = Inches(6.9)
    set_cell_shading(cell, CODE_BG_HEX)
    set_cell_margins(cell, top=90, bottom=90, left=130, right=120)
    set_cell_borders(cell, color=BORDER_HEX, sz="4")
    # Allow long diagrams/trees to split cleanly if over a page, but keep short ones together
    if len(code_lines) <= 28:
        set_row_cant_split(tbl.rows[0])

    for idx, line in enumerate(code_lines):
        p = cell.paragraphs[0] if idx == 0 else cell.add_paragraph()
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(0)
        p.paragraph_format.line_spacing = 1.05
        r = p.add_run(line)
        r.font.name = "Consolas"
        r.font.size = Pt(7.8 if max((len(x) for x in code_lines), default=0) > 85 else 8.3)
        r.font.color.rgb = CODE_COLOR

    sp = doc.add_paragraph()
    sp.paragraph_format.space_before = Pt(0)
    sp.paragraph_format.space_after = Pt(4)


def add_styled_table(doc, headers, rows):
    ncols = len(headers)
    if ncols == 0:
        return
    tbl = doc.add_table(rows=1 + len(rows), cols=ncols)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    tbl.autofit = False

    total_w = 6.9
    # Smart proportional widths based on column count
    if ncols == 2:
        widths = [2.1, 4.8]
    elif ncols == 3:
        widths = [1.8, 1.5, 3.6]
    elif ncols == 4:
        widths = [1.75, 1.15, 2.95, 1.05]
    elif ncols == 5:
        widths = [0.4, 1.25, 1.45, 1.75, 2.05]
    elif ncols == 6:
        widths = [1.2, 2.1, 0.9, 0.9, 0.9, 0.9]
    else:
        widths = [total_w / ncols] * ncols

    # Header row
    hdr_row = tbl.rows[0]
    set_row_header(hdr_row)
    set_row_cant_split(hdr_row)
    for c_idx, htext in enumerate(headers):
        cell = hdr_row.cells[c_idx]
        cell.width = Inches(widths[c_idx])
        cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        set_cell_shading(cell, NAVY_HEX)
        set_cell_margins(cell, top=80, bottom=80, left=90, right=90)
        set_cell_borders(cell, color=NAVY_HEX, sz="4")
        p = cell.paragraphs[0]
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(0)
        p.paragraph_format.line_spacing = 1.1
        clean_h = htext.replace("**", "").strip()
        r = p.add_run(clean_h)
        r.font.name = "Segoe UI"
        r.font.size = Pt(8.8)
        r.bold = True
        r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)

    # Data rows
    for r_idx, rdata in enumerate(rows):
        row = tbl.rows[r_idx + 1]
        set_row_cant_split(row)
        bg = ZEBRA_HEX if r_idx % 2 == 1 else "FFFFFF"
        for c_idx in range(ncols):
            val = rdata[c_idx] if c_idx < len(rdata) else ""
            cell = row.cells[c_idx]
            cell.width = Inches(widths[c_idx])
            cell.vertical_alignment = WD_ALIGN_VERTICAL.TOP
            set_cell_shading(cell, bg)
            set_cell_margins(cell, top=65, bottom=65, left=90, right=90)
            set_cell_borders(cell, color=BORDER_HEX, sz="4")
            p = cell.paragraphs[0]
            p.paragraph_format.space_before = Pt(0)
            p.paragraph_format.space_after = Pt(0)
            p.paragraph_format.line_spacing = 1.12
            add_rich_runs(p, val.strip(), base_size=8.6, base_color=TEXT_DARK)

    sp = doc.add_paragraph()
    sp.paragraph_format.space_before = Pt(0)
    sp.paragraph_format.space_after = Pt(5)


def add_heading(doc, level, text, page_break=False):
    if page_break:
        doc.add_page_break()
    style_name = f"Heading {min(level, 4)}"
    p = doc.add_paragraph(style=style_name)
    p.paragraph_format.keep_with_next = True
    clean = re.sub(r'`([^`]+)`', r'\1', text).strip()
    if level == 1:
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(8)
        r = p.add_run(clean)
        r.font.name = "Segoe UI"
        r.font.size = Pt(16.5)
        r.bold = True
        r.font.color.rgb = NAVY
        add_bottom_border(p, color=GOLD_HEX, sz="10")
    elif level == 2:
        p.paragraph_format.space_before = Pt(12)
        p.paragraph_format.space_after = Pt(5)
        r = p.add_run(clean)
        r.font.name = "Segoe UI"
        r.font.size = Pt(13)
        r.bold = True
        r.font.color.rgb = NAVY
    elif level == 3:
        p.paragraph_format.space_before = Pt(9)
        p.paragraph_format.space_after = Pt(4)
        r = p.add_run(clean)
        r.font.name = "Segoe UI"
        r.font.size = Pt(11)
        r.bold = True
        r.font.color.rgb = GOLD if "Viva" in clean else SLATE
    else:
        p.paragraph_format.space_before = Pt(7)
        p.paragraph_format.space_after = Pt(3)
        r = p.add_run(clean)
        r.font.name = "Segoe UI"
        r.font.size = Pt(10.2)
        r.bold = True
        r.font.color.rgb = NAVY
    return p


def add_image_figure(doc, img_path, caption, width_in=5.8):
    if not os.path.exists(img_path):
        return
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(8)
    p.paragraph_format.space_after = Pt(3)
    p.paragraph_format.keep_with_next = True
    p.add_run().add_picture(img_path, width=Inches(width_in))

    pc = doc.add_paragraph()
    pc.alignment = WD_ALIGN_PARAGRAPH.CENTER
    pc.paragraph_format.space_before = Pt(0)
    pc.paragraph_format.space_after = Pt(10)
    rc = pc.add_run(caption)
    rc.font.name = "Segoe UI"
    rc.font.size = Pt(8.8)
    rc.italic = True
    rc.font.color.rgb = TEXT_MUTED


def parse_md_table(lines):
    def split_row(ln):
        ln = ln.strip()
        if ln.startswith('|'):
            ln = ln[1:]
        if ln.endswith('|'):
            ln = ln[:-1]
        return [c.strip() for c in ln.split('|')]

    headers = split_row(lines[0])
    rows = []
    for ln in lines[2:]:
        if ln.strip():
            rows.append(split_row(ln))
    return headers, rows


def render_markdown(doc, md_text, skip_top_toc=False, page_break_h1=True):
    lines = md_text.splitlines()
    i = 0
    in_toc = False
    first_h1 = True

    while i < len(lines):
        raw = lines[i]
        stripped = raw.strip()

        # Skip markdown TOC in STUDY_GUIDE.md because we build a native Word TOC + roadmap
        if skip_top_toc and stripped == "## Table of Contents":
            in_toc = True
            i += 1
            continue
        if in_toc:
            if stripped.startswith("## ") and stripped != "## Table of Contents":
                in_toc = False
            else:
                i += 1
                continue

        # Skip horizontal rules
        if stripped == "---":
            i += 1
            continue

        # Blank line
        if not stripped:
            i += 1
            continue

        # Fenced code block
        if stripped.startswith("```"):
            lang = stripped[3:].strip().lower()
            i += 1
            block = []
            while i < len(lines) and not lines[i].strip().startswith("```"):
                block.append(lines[i])
                i += 1
            i += 1  # consume closing ```
            joined = "\n".join(block)
            if lang == "mermaid":
                if "LAYER 1" in joined.upper() or "Presentation Tier" in joined:
                    add_code_block(doc, ASCII_ARCH.splitlines(), caption="Diagram: Four-Layer Clean Architecture")
                elif "DatabaseManager.init" in joined:
                    add_code_block(doc, ASCII_MIGRATION.splitlines(), caption="Diagram: Database Initialization & Migration Pipeline (v1 -> v2)")
                elif "erDiagram" in joined:
                    add_code_block(doc, ASCII_ER.splitlines(), caption="Diagram: Entity-Relationship (ER) Overview of All 23 SQLite Tables")
                else:
                    add_code_block(doc, block)
            else:
                add_code_block(doc, block)
            continue

        # Headings
        hm = re.match(r'^(#{1,4})\s+(.+)$', stripped)
        if hm:
            lvl = len(hm.group(1))
            htext = hm.group(2).strip()
            # Skip duplicate doc title if first H1
            if skip_top_toc and lvl == 1 and htext.startswith("CoralCove — Complete Phase-by-Phase"):
                i += 1
                continue
            pb = False
            if page_break_h1 and lvl == 1:
                if not first_h1:
                    pb = True
                first_h1 = False
            elif lvl == 2 and re.match(r'^Phase \d+:', htext):
                pb = True
            add_heading(doc, lvl, htext, page_break=pb)
            i += 1
            continue

        # Blockquote -> styled Callout Box
        if stripped.startswith(">"):
            q_lines = []
            while i < len(lines) and lines[i].strip().startswith(">"):
                q_lines.append(lines[i].strip().lstrip(">").strip())
                i += 1
            add_callout(doc, q_lines)
            continue

        # Markdown Table
        if stripped.startswith("|") and i + 1 < len(lines) and re.match(r'^\|[\s:\-\|]+\|$', lines[i + 1].strip()):
            t_lines = []
            while i < len(lines) and lines[i].strip().startswith("|"):
                t_lines.append(lines[i])
                i += 1
            headers, rows = parse_md_table(t_lines)
            add_styled_table(doc, headers, rows)
            continue

        # Bullet list (including nested 2/3-space indented bullets)
        bm = re.match(r'^(\s*)[-*]\s+(.+)$', raw)
        if bm:
            indent_spaces = len(bm.group(1))
            content = bm.group(2).strip()
            p = doc.add_paragraph(style='List Bullet' if indent_spaces < 2 else 'List Bullet 2')
            p.paragraph_format.space_before = Pt(1)
            p.paragraph_format.space_after = Pt(2.5)
            p.paragraph_format.line_spacing = 1.15
            add_rich_runs(p, content, base_size=9.5)
            i += 1
            continue

        # Numbered list
        nm = re.match(r'^(\s*)\d+\.\s+(.+)$', raw)
        if nm:
            content = nm.group(2).strip()
            p = doc.add_paragraph(style='List Number')
            p.paragraph_format.space_before = Pt(2)
            p.paragraph_format.space_after = Pt(2.5)
            p.paragraph_format.line_spacing = 1.15
            add_rich_runs(p, content, base_size=9.5)
            i += 1
            continue

        # Regular paragraph (merge consecutive plain lines)
        para_lines = [stripped]
        i += 1
        while i < len(lines):
            nxt = lines[i].strip()
            if (not nxt or nxt.startswith("#") or nxt.startswith(">") or nxt.startswith("```")
                    or nxt.startswith("|") or nxt == "---"
                    or re.match(r'^\s*[-*]\s+', lines[i]) or re.match(r'^\s*\d+\.\s+', lines[i])):
                break
            para_lines.append(nxt)
            i += 1
        full_p = " ".join(para_lines)
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(2)
        p.paragraph_format.space_after = Pt(5)
        p.paragraph_format.line_spacing = 1.18
        add_rich_runs(p, full_p, base_size=9.8)


def extract_doc_section(doc_md, start_heading, end_heading=None):
    lines = doc_md.splitlines()
    start_idx = None
    end_idx = len(lines)
    for idx, ln in enumerate(lines):
        if start_idx is None and ln.strip().startswith(start_heading):
            start_idx = idx
        elif start_idx is not None and end_heading and ln.strip().startswith(end_heading):
            end_idx = idx
            break
    if start_idx is None:
        return ""
    return "\n".join(lines[start_idx:end_idx])


def build():
    with open("STUDY_GUIDE.md", encoding="utf-8") as f:
        study_md = f.read()
    with open("doc.md", encoding="utf-8") as f:
        doc_md = f.read()

    doc = Document()

    # Set clean 0.8-inch margins so rich API tables fit comfortably with zero cramping
    for section in doc.sections:
        section.page_width = Inches(8.5)
        section.page_height = Inches(11.0)
        section.top_margin = Inches(0.8)
        section.bottom_margin = Inches(0.8)
        section.left_margin = Inches(0.8)
        section.right_margin = Inches(0.8)
        section.different_first_page_header_footer = True

        # Running Header (pages 2+)
        hp = section.header.paragraphs[0]
        hp.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        hr = hp.add_run("CoralCove (JavaFX 21 + SQLite)  |  Complete Project & Viva Study Guide")
        hr.font.name = "Segoe UI"
        hr.font.size = Pt(8.5)
        hr.font.color.rgb = TEXT_MUTED

        # Running Footer (pages 2+) with Page X of Y
        fp = section.footer.paragraphs[0]
        fp.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r1 = fp.add_run("CoralCove Master Study Guide   |   Page ")
        r1.font.name = "Segoe UI"
        r1.font.size = Pt(8.5)
        r1.font.color.rgb = TEXT_MUTED
        r_pg = fp.add_run()
        r_pg.font.name = "Segoe UI"
        r_pg.font.size = Pt(8.5)
        r_pg.font.color.rgb = NAVY
        r_pg.bold = True
        add_field(r_pg, "PAGE")
        r2 = fp.add_run(" of ")
        r2.font.name = "Segoe UI"
        r2.font.size = Pt(8.5)
        r2.font.color.rgb = TEXT_MUTED
        r_tot = fp.add_run()
        r_tot.font.name = "Segoe UI"
        r_tot.font.size = Pt(8.5)
        r_tot.font.color.rgb = NAVY
        r_tot.bold = True
        add_field(r_tot, "NUMPAGES")

    # Configure default Normal style font
    normal_style = doc.styles['Normal']
    normal_style.font.name = 'Segoe UI'
    normal_style.font.size = Pt(9.8)
    normal_style.font.color.rgb = TEXT_DARK

    # =========================================================================
    # COVER PAGE
    # =========================================================================
    if os.path.exists("docs/screenshots/figure4_app_icon_clean.png"):
        p_logo = doc.add_paragraph()
        p_logo.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_logo.paragraph_format.space_before = Pt(36)
        p_logo.paragraph_format.space_after = Pt(16)
        p_logo.add_run().add_picture("docs/screenshots/figure4_app_icon_clean.png", width=Inches(1.5))

    p_eye = doc.add_paragraph()
    p_eye.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_eye.paragraph_format.space_after = Pt(8)
    r_eye = p_eye.add_run("COMPLETE PROJECT ARCHITECTURE & VIVA STUDY GUIDE")
    r_eye.font.name = "Segoe UI"
    r_eye.font.size = Pt(11)
    r_eye.bold = True
    r_eye.font.color.rgb = GOLD

    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_after = Pt(8)
    r_t = p_title.add_run("CoralCove\nLuxury Resort Management System")
    r_t.font.name = "Segoe UI"
    r_t.font.size = Pt(28)
    r_t.bold = True
    r_t.font.color.rgb = NAVY

    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_after = Pt(24)
    r_sub = p_sub.add_run("From Zero JavaFX Experience to 100% End-to-End Codebase Mastery\nOffline-First Desktop PMS built with Java 21, JavaFX 21 & Embedded SQLite 3")
    r_sub.font.name = "Segoe UI"
    r_sub.font.size = Pt(12)
    r_sub.font.color.rgb = TEXT_MUTED

    # Stat Ribbon Table
    stats = [
        ("97", "Java Source Files"),
        ("23", "SQLite Tables"),
        ("12", "App Modules"),
        ("111", "Inbuilt APIs Covered"),
        ("18", "JUnit 5 Tests"),
    ]
    stbl = doc.add_table(rows=1, cols=5)
    stbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    stbl.autofit = False
    for idx, (num, lbl) in enumerate(stats):
        cell = stbl.rows[0].cells[idx]
        cell.width = Inches(1.38)
        set_cell_shading(cell, NAVY_HEX)
        set_cell_margins(cell, top=120, bottom=120, left=60, right=60)
        set_cell_borders(cell, color=GOLD_HEX, sz="6")
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_after = Pt(2)
        rn = p.add_run(num)
        rn.font.name = "Segoe UI"
        rn.font.size = Pt(18)
        rn.bold = True
        rn.font.color.rgb = RGBColor(0xE8, 0xC9, 0x7A)
        p2 = cell.add_paragraph()
        p2.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p2.paragraph_format.space_after = Pt(0)
        rl = p2.add_run(lbl)
        rl.font.name = "Segoe UI"
        rl.font.size = Pt(8)
        rl.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)

    p_sp = doc.add_paragraph()
    p_sp.paragraph_format.space_before = Pt(24)

    add_callout(doc, [
        "**Goal of This Study Guide:** Once you and your teammates read this document from start to finish, you will know **every single detail of CoralCove** — why the system was designed this way, how JavaFX works from scratch, every inbuilt library function, every database table and column, every DAO/Service/Utility/UI method (exact `Input -> Processing -> Output`), every security mechanism, all 18 automated tests, all 5 end-to-end pipelines, and how to defend every architectural decision in your viva exam.",
        "**Unified Master Edition:** Combines the beginner-friendly walkthrough from `STUDY_GUIDE.md` with the exhaustive schema catalog, RBAC matrix, UI screenshots, directory tree, runbook, and deep technical defense Q&A from `doc.md` — cross-verified directly against all 97 Java source files."
    ])

    p_meta = doc.add_paragraph()
    p_meta.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_meta.paragraph_format.space_before = Pt(36)
    rm = p_meta.add_run("Repository: https://github.com/applejuice093/Coralcove\nRuntime: OpenJDK 21  |  JavaFX 21.0.6  |  SQLite JDBC 3.49.1.0  |  JUnit Jupiter 5.11.4")
    rm.font.name = "Segoe UI"
    rm.font.size = Pt(9)
    rm.font.color.rgb = TEXT_MUTED

    doc.add_page_break()

    # =========================================================================
    # PAGE 2: TABLE OF CONTENTS & STUDY ROADMAP
    # =========================================================================
    add_heading(doc, 1, "Table of Contents", page_break=False)
    add_toc_field(doc)

    add_heading(doc, 2, "Quick Study Roadmap", page_break=False)
    add_styled_table(
        doc,
        ["Section", "What You Will Master", "Best For"],
        [
            ["4-Member Viva Split", "How to divide deep technical ownership cleanly across 4 teammates", "Team prep"],
            ["Part 0: Crash Course", "JavaFX Theater Analogy, 5 Golden Rules, tech trade-offs & all 111 inbuilt APIs", "JavaFX beginners"],
            ["Part 1: Architecture", "Problem statement, offline solution & 4-layer Clean Architecture", "Opening slides & Q&A"],
            ["Phase 1: Boot & Runbook", "pom.xml, directory tree, AppConfig, Main.java, run commands & demo credentials", "Member 1 + Demo"],
            ["Phase 2: Models & Enums", "All 7 Enums & 13 Model POJOs (including meal plan codes & sample flag)", "Member 1"],
            ["Phase 3: Database & Schema", "DatabaseManager, Migrations (v1->v2), Transactions, ER Diagram & ALL 23 table columns", "Member 2"],
            ["Phase 4: DAO Layer", "All 14 DAO classes & every parameterized SQL query (Input -> Processing -> Output)", "Member 2"],
            ["Phase 5: Security & Utils", "PBKDF2-HMAC-SHA256, BigDecimal Money, Dates, Validation, CSV & Raw PDF 1.4 compiler", "Member 3"],
            ["Phase 6: Service & RBAC", "All 19 Services, LodgeStatusSync priority machine & full 12x4 RBAC permission matrix", "Member 3"],
            ["Phase 7: JavaFX UI Layer", "All 29 UI classes, Fx.run() threading, Dialogs.form() & embedded app screenshots", "Member 4"],
            ["Phase 8: Automated Tests", "DbFixture temp-DB isolation & all 18 JUnit 5 tests across 6 suites", "Member 4"],
            ["Part 3: Master Pipelines", "5 end-to-end Input -> Processing -> Output click-to-database walkthroughs", "All 4 Members"],
            ["Part 4: Viva Defense Bank", "6 deep architectural defense answers (SQLite, Threading, BigDecimal, PBKDF2, Overlap Math, Aadhaar)", "Exam & Viva"],
        ]
    )

    doc.add_page_break()

    # =========================================================================
    # PREPARE ENRICHED SECTIONS FROM doc.md TO WEAVE INTO STUDY_GUIDE.md
    # =========================================================================
    dir_tree = extract_doc_section(doc_md, "## 3. Project Directory & Package Structure", "---")
    dir_tree = dir_tree.replace("## 3. Project Directory & Package Structure", "### Project Directory & Package Tree")

    # Verified runbook (using exact DemoDataService credentials!)
    runbook_md = """### Setup, Execution & Verified Default Credentials Runbook

Execute the following commands from the project root directory (`c:\\A\\PROJECT\\corlcovejavafx`):

```powershell
# 1. Clean and compile all Java 21 sources
.\\mvnw.cmd clean compile

# 2. Run the complete automated test suite (18 headless JUnit 5 tests)
.\\mvnw.cmd test

# 3. Launch the JavaFX CoralCove desktop application
.\\mvnw.cmd javafx:run
```

**Verified Default & Showcase Login Accounts (Seeded Automatically on First Launch):**

| Username | Password | Role | Assigned Resort | Full Display Name | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `admin` | `Admin@123` | `ADMIN` | All Resorts (`null`) | Aniket Sharma (Owner) | Forces password change on first login (`must_change_password = 1`). |
| `beach.desk` | `Desk@123` | `FRONT_DESK` | CoralCove Beach (Kochi) | Anita George | Seeded by `DemoDataService`; scoped strictly to Beach resort. |
| `cliff.mgr` | `Manager@123` | `MANAGER` | CoralCove Cliff (Varkala) | Fathima Noor | Seeded by `DemoDataService`; manages Cliff resort (no Settings tab). |

**Database Reset Options:**
- **Full Blank Reset:** Delete `data/coralcove.db` and re-launch the app; `DatabaseMigration` + `DatabaseInitializer` rebuild all 23 tables and re-seed showcase data cleanly.
- **Keep Real Data / Clear Demo Only:** Sign in as `admin`, open **Settings**, and click **"Reset demonstration data"** (`DemoDataService.reset()` deletes only `is_sample = 1` rows plus `beach.desk`/`cliff.mgr`).
"""

    # Column-by-column 23-table catalog from doc.md
    table_catalog = extract_doc_section(doc_md, "### 4.4 Detailed Table Catalog & Relational Schema", "## 5. Domain Model")
    table_catalog = table_catalog.replace("### 4.4 Detailed Table Catalog & Relational Schema", "### Exhaustive Column-by-Column Schema Catalog (All 23 SQLite Tables)")
    er_section = "### Entity-Relationship (ER) Diagram\n\n```mermaid\nerDiagram\n```\n\n" + table_catalog

    # RBAC Matrix from doc.md
    rbac_matrix = extract_doc_section(doc_md, "### 9.2 Role-Based Access Control (RBAC) Matrix", "---")
    rbac_matrix = rbac_matrix.replace("### 9.2 Role-Based Access Control (RBAC) Matrix", "### Complete Role-Based Access Control (RBAC) Matrix (12 Modules × 4 Roles)")

    # Part 4: Deep Viva Defense Bank from doc.md Section 12 (convert $$ LaTeX to crisp Unicode)
    viva_bank = extract_doc_section(doc_md, "## 12. Exam & Viva Technical Defense Guide", "## 13. Setup")
    viva_bank = viva_bank.replace("## 12. Exam & Viva Technical Defense Guide", "# Part 4: Master Exam & Viva Technical Defense Bank")
    viva_bank = viva_bank.replace(
        r"$$\text{Existing.checkout} \le \text{New.checkin} \quad \text{OR} \quad \text{Existing.checkin} \ge \text{New.checkout}$$",
        "```\nNON-OVERLAP CONDITION:  Existing.check_out <= New.check_in   OR   Existing.check_in >= New.check_out\n```"
    )
    viva_bank = viva_bank.replace(
        r"$$\text{NOT } (\text{check\_out} \le \text{new\_in} \quad \text{OR} \quad \text{check\_in} \ge \text{new\_out})$$",
        "```\nCOLLISION CONDITION (De Morgan's Law):  check_in < new_out  AND  check_out > new_in\n```"
    )
    viva_bank = viva_bank.replace("status <> 'CANCELLED'", "status IN ('PENDING','CONFIRMED','ARRIVED')")

    # Splice enrichments directly into STUDY_GUIDE.md at exact phase markers!
    enriched_md = study_md
    enriched_md = enriched_md.replace(
        "### Phase 1 — Likely Viva Questions Checkpoint",
        dir_tree + "\n\n" + runbook_md + "\n\n### Phase 1 — Likely Viva Questions Checkpoint"
    )
    enriched_md = enriched_md.replace(
        "### Phase 3 — Likely Viva Questions Checkpoint",
        er_section + "\n\n### Phase 3 — Likely Viva Questions Checkpoint"
    )
    enriched_md = enriched_md.replace(
        "### Phase 6 — Likely Viva Questions Checkpoint",
        rbac_matrix + "\n\n### Phase 6 — Likely Viva Questions Checkpoint"
    )

    # Split before Phase 7 Viva Checkpoint so we can embed crisp UI screenshots right inside Phase 7!
    marker_p7 = "### Phase 7 — Likely Viva Questions Checkpoint"
    part_before_figs, part_after_figs = enriched_md.split(marker_p7, 1)

    render_markdown(doc, part_before_figs, skip_top_toc=True, page_break_h1=True)

    # Embed real CoralCove UI screenshots inside Phase 7
    add_heading(doc, 3, "Visual UI Walkthrough (Live Application Screenshots)", page_break=False)
    add_image_figure(
        doc,
        "docs/screenshots/figure1_stays_dashboard.png",
        "Figure 1: Stays Screen — ScreenHeader hero banner (220px), FlowPane filter toolbar, and TableView with status chips.",
        width_in=6.5
    )
    add_image_figure(
        doc,
        "docs/screenshots/figure2_new_stay_dialog_clean.png",
        "Figure 2: New Stay Modal Dialog — Standardized Dialogs.form() GridPane spacing, DatePickers, and Save/Cancel action bar.",
        width_in=4.0
    )
    add_image_figure(
        doc,
        "docs/screenshots/figure3_profile_dialog_clean.png",
        "Figure 3: User Profile Dialog — Circular clipped avatar preview, preset style selector, editable fields, and pinned Save Profile footer.",
        width_in=4.0
    )

    # Render remaining STUDY_GUIDE.md (Phase 7 Viva Checkpoint -> Phase 8 -> Part 3 Master Pipelines)
    render_markdown(doc, marker_p7 + part_after_figs, skip_top_toc=False, page_break_h1=True)

    # Render Part 4: Master Exam & Viva Technical Defense Bank
    render_markdown(doc, viva_bank, skip_top_toc=False, page_break_h1=True)

    out_path = "CoralCove_Complete_Study_Guide.docx"
    doc.save(out_path)
    print(f"Successfully built {out_path}!")


if __name__ == "__main__":
    build()
