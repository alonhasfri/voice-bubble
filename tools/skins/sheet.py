import json, sys
sys.path.insert(0, sys.argv[3])
from svglib import svg
data=json.load(open(sys.argv[1]))
cells=''.join(f'<div class="c">{svg(e["spec"],110)}<div>{e["name"]}</div></div>' for e in data)
open(sys.argv[2],'w').write(f'<html><body style="background:#202020;color:#eee;font:12px sans-serif;margin:8px"><div style="display:grid;grid-template-columns:repeat(8,120px);gap:6px">{cells}</div></body></html>'.replace('.c','').replace('class="c"','style="text-align:center"'))
