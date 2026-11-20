import json, sys, math
d=json.load(open(sys.argv[1]))
def area(p): return abs(sum(p[i][0]*p[(i+1)%len(p)][1]-p[(i+1)%len(p)][0]*p[i][1] for i in range(len(p))))/2
def keep(s,k):
    st=s.get('stroke',0); t=s['type']
    if t=='circle': return s['r']>=3.5*k and (st==0 or st>=2.0*k)
    if t=='ellipse': return s['rx']*s['ry']>=20*k and (st==0 or st>=2.0*k)
    if t=='rect': return s['w']*s['h']>=50*k
    if t=='line': return s.get('width',2)>=2.4*k and math.hypot(s['x2']-s['x1'],s['y2']-s['y1'])>=14*k
    if t=='polygon': return area(s['points'])>=25*k
    if t=='path': return st==0 or st>=1.4*k
    if t=='text': return s['size']>=12*k
for e in d:
    src=e['spec']['shapes']; out=[s for s in src if keep(s,1)]
    if len(out)<4 or len(out)<len(src)//5: out=[s for s in src if keep(s,0.35)]
    e['spec']['shapes']=out
json.dump(d,open(sys.argv[2],'w'))
