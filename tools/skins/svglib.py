import json, sys, os, subprocess, math
def col(c):
    if not isinstance(c,str) or not c.startswith('#'): return ('#ffffff',1)
    if len(c)==9: return ('#'+c[3:], int(c[1:3],16)/255)
    return (c,1)
def paint(s):
    c,o=col(s.get('color'))
    st=s.get('stroke',0)
    if s['type']=='line': return f'fill="none" stroke="{c}" stroke-opacity="{o}" stroke-width="{s.get("width",st or 3)}" stroke-linecap="round"'
    if st: return f'fill="none" stroke="{c}" stroke-opacity="{o}" stroke-width="{st}" stroke-linecap="round" stroke-linejoin="round"'
    return f'fill="{c}" fill-opacity="{o}"'
_n=[0]
def svg(spec, size=160):
    _n[0]+=1; u=str(_n[0])
    bg=spec.get('background') or ['#3A5BD9']
    b1=col(bg[0])[0]; b2=col(bg[1])[0] if len(bg)>1 else b1
    out=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="-5 -5 110 110">',
         f'<defs><linearGradient id="g{u}" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="{b1}"/><stop offset="1" stop-color="{b2}"/></linearGradient><clipPath id="c{u}"><circle cx="50" cy="50" r="50"/></clipPath></defs>',
         '<rect x="-5" y="-5" width="110" height="110" fill="#2b2b2b"/>',
         f'<circle cx="50" cy="50" r="50" fill="url(#g{u})"/><g clip-path="url(#c{u})">']
    for s in spec.get('shapes',[]):
        t=s['type']; p=paint(s)
        if t=='circle': out.append(f'<circle cx="{s["x"]}" cy="{s["y"]}" r="{s["r"]}" {p}/>')
        elif t=='ellipse': out.append(f'<ellipse cx="{s["x"]}" cy="{s["y"]}" rx="{s["rx"]}" ry="{s["ry"]}" {p}/>')
        elif t=='rect': out.append(f'<rect x="{s["x"]}" y="{s["y"]}" width="{s["w"]}" height="{s["h"]}" rx="{s.get("radius",0)}" {p}/>')
        elif t=='line': out.append(f'<line x1="{s["x1"]}" y1="{s["y1"]}" x2="{s["x2"]}" y2="{s["y2"]}" {p}/>')
        elif t=='polygon': out.append(f'<polygon points="{" ".join(f"{a},{b}" for a,b in s["points"])}" {p}/>')
        elif t=='path': out.append(f'<path d="{s["d"]}" {p}/>')
        elif t=='text': out.append(f'<text x="{s["x"]}" y="{s["y"]}" font-size="{s["size"]}" text-anchor="middle" dominant-baseline="central" font-weight="bold" font-family="sans-serif" fill="{col(s["color"])[0]}">{s["text"]}</text>')
    out.append('</g>')
    if spec.get('mic'): out.append(f'<rect x="44" y="34" width="12" height="20" rx="6" fill="{spec["mic"]}"/>')
    out.append('</svg>'); return '\n'.join(out)
