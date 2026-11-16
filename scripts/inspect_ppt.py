import pptx

prs = pptx.Presentation('CoralCove_Presentation.pptx')
for idx, slide in enumerate(prs.slides):
    print(f'=== SLIDE {idx+1} ===')
    for s_idx, s in enumerate(slide.shapes):
        if s.has_text_frame:
            for p_idx, p in enumerate(s.text_frame.paragraphs):
                txt = p.text.strip()
                if txt:
                    print(f'Shape {s_idx} ({s.name}), Para {p_idx}: "{txt}"')
