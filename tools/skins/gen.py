import json, math, sys
S = []  # (id, name, spec)

def C(x,y,r,col,stroke=None): d={"type":"circle","x":x,"y":y,"r":r,"color":col}; (d.update(stroke=stroke) if stroke else None); return d
def E(x,y,rx,ry,col,stroke=None): d={"type":"ellipse","x":x,"y":y,"rx":rx,"ry":ry,"color":col}; (d.update(stroke=stroke) if stroke else None); return d
def R(x,y,w,h,col,rad=0,stroke=None): d={"type":"rect","x":x,"y":y,"w":w,"h":h,"radius":rad,"color":col}; (d.update(stroke=stroke) if stroke else None); return d
def L(x1,y1,x2,y2,col,w=2): return {"type":"line","x1":x1,"y1":y1,"x2":x2,"y2":y2,"color":col,"width":w}
def P(pts,col,stroke=None): d={"type":"polygon","points":[[round(a,1),round(b,1)] for a,b in pts],"color":col}; (d.update(stroke=stroke) if stroke else None); return d
def PA(dd,col,stroke=None): d={"type":"path","d":dd,"color":col}; (d.update(stroke=stroke) if stroke else None); return d
def T(x,y,size,t,col): return {"type":"text","x":x,"y":y,"size":size,"text":t,"color":col}
def ring(cx,cy,r,n,f):  # n points around
    return [f(cx+r*math.cos(2*math.pi*i/n), cy+r*math.sin(2*math.pi*i/n), i) for i in range(n)]
def star(cx,cy,ro,ri,n=5,rot=-90):
    pts=[]
    for i in range(n*2):
        r=ro if i%2==0 else ri; a=math.radians(rot+i*180/n); pts.append((cx+r*math.cos(a),cy+r*math.sin(a)))
    return pts
def add(i,name,bg,shapes,glow=None,mic=None):
    S.append({"id":"art_"+i,"name":name,"spec":{"name":name,"background":bg if isinstance(bg,list) else [bg],"glow":glow,"mic":mic,"shapes":shapes}})
W="#FFFFFF"; K="#1A1A1A"

# ---------- food ----------
sh=[C(50,50,46,"#D9822B"),C(50,50,40,"#F5C542"),C(50,50,38,"#F7D35E")]
for (x,y) in [(35,32),(62,30),(70,52),(55,68),(33,60),(48,48),(26,45),(66,72)]:
    sh+= [C(x,y,7,"#C0392B"),C(x-2,y-2,2,"#E35D4F")]
for (x,y,a) in [(42,40,0),(58,42,1),(44,58,2),(64,60,0),(30,52,1)]:
    sh.append(E(x,y,3,1.6,"#2E7D32"))
for (x,y) in [(52,36),(38,70),(72,40),(28,38)]: sh.append(C(x,y,2.5,"#FFF3C4"))
add("pizza","Pizza","#7A4A1E",sh,glow="#F5C542")

sh=[C(50,50,44,"#E0A060"),C(50,50,38,"#F27BB0")]
sh+= [PA("M 14 52 Q 20 64 26 56 Q 32 70 40 60 Q 50 72 58 61 Q 66 70 72 58 Q 80 66 86 52","#F27BB0")]
sh+= [C(50,50,13,"#7A4A1E"),C(50,50,11,"#1B1B2A")]
cols=["#FFFFFF","#4FC3F7","#FFEB3B","#81C784","#BA68C8","#FF8A65"]
import random; random.seed(3)
for i in range(26):
    a=random.uniform(0,2*math.pi); r=random.uniform(17,34); x=50+r*math.cos(a); y=50+r*math.sin(a); b=a+random.uniform(0,3)
    sh.append(L(x,y,x+4*math.cos(b),y+4*math.sin(b),cols[i%6],2.2))
sh.append(PA("M 22 34 Q 36 16 58 18","#FFFFFF80",stroke=3))
add("donut","Donut","#1B1B2A",sh)

sh=[C(50,50,46,"#2E7D32"),C(50,50,41,"#A5D6A7"),C(50,50,38,"#EF5350"),C(50,50,30,"#F2545B")]
for i in range(14):
    a=2*math.pi*i/14; r=18 if i%2 else 28; x=50+r*math.cos(a); y=50+r*math.sin(a)
    sh.append(E(x,y,1.6,3,"#1B1B1B"))
sh.append(PA("M 22 30 Q 34 18 50 16","#FFFFFF60",stroke=3))
add("watermelon","Watermelon","#1B5E20",sh)

def citrus(i,name,rind,flesh,seg,pith):
    sh=[C(50,50,46,rind),C(50,50,41,pith),C(50,50,38,flesh)]
    for k in range(10):
        a=2*math.pi*k/10; b=2*math.pi*(k+1)/10
        sh.append(P([(50,50),(50+34*math.cos(a+0.08),50+34*math.sin(a+0.08)),(50+36*math.cos((a+b)/2),50+36*math.sin((a+b)/2)),(50+34*math.cos(b-0.08),50+34*math.sin(b-0.08))],seg))
    sh.append(C(50,50,5,pith))
    for k in range(10):
        a=2*math.pi*k/10; sh.append(L(50,50,50+37*math.cos(a),50+37*math.sin(a),pith,1.5))
    sh.append(PA("M 24 28 Q 38 16 56 16","#FFFFFF70",stroke=3))
    add(i,name,rind,sh)
citrus("orange","Orange slice","#EF6C00","#FFA726","#FFB74D","#FFF3E0")
citrus("lemon","Lemon slice","#F9A825","#FFEE58","#FFF176","#FFFDE7")
citrus("lime","Lime slice","#33691E","#9CCC65","#AED581","#F1F8E9")

sh=[C(50,50,46,"#6D4C41"),C(50,50,41,"#8BC34A"),C(50,50,30,"#AED581"),E(50,50,9,7,"#F1F8E9")]
for k in range(16):
    a=2*math.pi*k/16; sh.append(E(50+18*math.cos(a),50+18*math.sin(a),1.4,2.4,"#1B1B1B"))
    sh.append(L(50+10*math.cos(a),50+10*math.sin(a),50+36*math.cos(a),50+36*math.sin(a),"#C5E1A580",1))
add("kiwi","Kiwi","#4E342E",sh)

sh=[C(50,50,44,"#C68642"),C(50,50,40,"#D9A066")]
random.seed(7)
for i in range(11):
    a=random.uniform(0,2*math.pi); r=random.uniform(5,30)
    sh.append(P([(50+r*math.cos(a)+dx,50+r*math.sin(a)+dy) for dx,dy in [(-4,0),(-1,-4),(4,-2),(3,3),(-2,4)]],"#4E2A14"))
for i in range(14):
    a=random.uniform(0,2*math.pi); r=random.uniform(8,34); sh.append(C(50+r*math.cos(a),50+r*math.sin(a),1,"#B8743A"))
add("cookie","Cookie","#8D5524",sh)

sh=[P([(28,56),(72,56),(66,90),(34,90)],"#4FC3F7")]
for x in range(32,70,6): sh.append(L(x,58,x+2,89,"#0288D1",2))
sh+=[E(50,52,26,10,"#F8BBD0"),E(50,42,22,10,"#F48FB1"),E(50,32,15,9,"#F8BBD0"),C(50,20,6,"#E53935"),L(50,15,55,8,"#2E7D32",2),C(48,18,1.6,"#FFFFFF")]
for (x,y,c) in [(40,44,"#FFEB3B"),(58,40,"#81C784"),(46,34,"#4FC3F7"),(56,50,"#BA68C8"),(36,52,"#FF8A65"),(62,52,"#FFFFFF")]:
    sh.append(L(x,y,x+3,y+1,c,2))
add("cupcake","Cupcake","#E1F5FE",sh)

sh=[P([(36,54),(64,54),(50,94)],"#D7A15A")]
for i in range(-3,4): sh+= [L(43+i*4,56,50+i*1.5,90,"#B07A35",1.4), L(57-i*4,56,50-i*1.5,90,"#B07A35",1.4)]
sh+=[C(40,48,13,"#FFF3E0"),C(60,48,13,"#F8BBD0"),C(50,36,15,"#795548"),E(50,24,6,4,"#5D4037"),C(52,18,4,"#E53935"),C(53,17,1.2,"#FFFFFF")]
sh+=[PA("M 36 50 Q 40 60 44 52","#FFF3E0"),PA("M 56 50 Q 60 62 64 52","#F8BBD0")]
add("icecream","Ice cream","#80DEEA",sh)

sh=[C(50,50,44,"#1B2A1B"),C(50,50,40,"#263826"),C(50,50,33,"#FAFAFA"),C(50,50,15,"#FF7043"),C(46,47,6,"#FFAB91"),C(56,55,5,"#AED581"),C(54,44,4,"#FFF59D")]
random.seed(11)
for i in range(30):
    a=random.uniform(0,2*math.pi); r=random.uniform(17,31); sh.append(E(50+r*math.cos(a),50+r*math.sin(a),1.6,1,"#E0E0E0"))
add("sushi","Sushi","#3E2723",sh)

sh=[E(50,35,34,18,"#E0A050"),E(50,32,32,15,"#F0B860")]
for (x,y) in [(38,26),(50,22),(62,27),(44,34),(58,33),(32,32),(68,34)]: sh.append(E(x,y,2,1.2,"#FFF8E1"))
sh+=[PA("M 16 46 Q 24 40 30 47 Q 38 40 46 47 Q 54 40 62 47 Q 70 40 76 47 Q 82 42 86 46 L 86 50 L 16 50 Z","#7CB342"),
     R(17,48,66,6,"#F9A825",3),R(15,53,70,11,"#5D4037",5),R(17,64,66,5,"#E53935",2),E(50,74,34,9,"#E0A050")]
add("burger","Burger","#FFE0B2",sh)

sh=[PA("M 50 92 Q 18 70 20 42 Q 26 24 50 30 Q 74 24 80 42 Q 82 70 50 92 Z","#E53935"),PA("M 50 86 Q 28 68 28 46 Q 34 34 46 36","#FF8A80",stroke=2)]
random.seed(5)
for (x,y) in [(36,46),(50,44),(64,46),(42,58),(58,58),(50,70),(32,58),(68,58),(46,80),(54,80),(40,68),(60,68)]: sh+= [E(x,y,1.4,2.2,"#FFEB3B"),C(x,y-1,0.6,"#FFFFFF")]
sh+=[P(star(50,28,18,7,5,-90),"#43A047"),R(48,8,4,14,"#2E7D32",2)]
add("strawberry","Strawberry","#FCE4EC",sh)

sh=[PA("M 20 54 Q 18 30 38 26 Q 50 24 56 34 Q 78 30 82 52 Q 84 74 60 80 Q 40 86 26 74 Q 18 66 20 54 Z","#FAFAFA"),
    C(52,52,15,"#FFB300"),C(52,52,13,"#FFC107"),C(47,47,4,"#FFE082")]
add("egg","Fried egg","#455A64",sh)

sh=[PA("M 50 8 Q 80 20 82 56 Q 82 90 50 92 Q 18 90 18 56 Q 20 20 50 8 Z","#33691E"),PA("M 50 14 Q 76 24 77 56 Q 76 86 50 87 Q 24 86 23 56 Q 24 24 50 14 Z","#C5E1A5"),
    PA("M 50 20 Q 70 30 72 56 Q 70 80 50 82 Q 30 80 28 56 Q 30 30 50 20 Z","#DCEDC8"),C(50,60,15,"#8D5524"),C(46,56,4,"#A1887F")]
add("avocado","Avocado","#558B2F",sh)

# ---------- sports ----------
sh=[C(50,50,46,"#E8711A"),C(50,50,46,"#4A2511",stroke=2),L(50,4,50,96,"#3E2010",2.5),L(4,50,96,50,"#3E2010",2.5),
    PA("M 20 14 Q 36 50 20 86","#3E2010",stroke=2.5),PA("M 80 14 Q 64 50 80 86","#3E2010",stroke=2.5),PA("M 22 22 Q 34 12 48 10","#FFFFFF50",stroke=3)]
add("basketball","Basketball","#E8711A",sh)

sh=[C(50,50,46,"#FAFAFA")]
pent=lambda cx,cy,r,rot=-90: [(cx+r*math.cos(math.radians(rot+i*72)),cy+r*math.sin(math.radians(rot+i*72))) for i in range(5)]
sh.append(P(pent(50,50,12),"#1B1B1B"))
for i in range(5):
    a=math.radians(-90+36+i*72); sh.append(P(pent(50+32*math.cos(a),50+32*math.sin(a),11,math.degrees(a)+180),"#1B1B1B"))
    b=math.radians(-90+i*72); sh.append(L(50+12*math.cos(b),50+12*math.sin(b),50+24*math.cos(b),50+24*math.sin(b),"#555555",1.5))
sh.append(C(50,50,46,"#9E9E9E",stroke=1.5))
add("soccer","Soccer ball","#FAFAFA",sh)

sh=[C(50,50,46,"#CDDC39"),PA("M 18 18 Q 50 50 18 82","#FFFFFF",stroke=3.5),PA("M 82 18 Q 50 50 82 82","#FFFFFF",stroke=3.5)]
random.seed(2)
for i in range(40):
    a=random.uniform(0,2*math.pi); r=random.uniform(4,44); sh.append(C(50+r*math.cos(a),50+r*math.sin(a),0.8,"#AFB42B"))
add("tennis","Tennis ball","#CDDC39",sh)

sh=[C(50,50,46,"#FAFAFA"),PA("M 22 12 Q 40 50 22 88","#E53935",stroke=1.5),PA("M 78 12 Q 60 50 78 88","#E53935",stroke=1.5)]
for k in range(1,10):
    t=k/10
    for side in (1,-1):
        x0=(22 if side==1 else 78); xm=(40 if side==1 else 60)
        y=12+76*t; x=(1-t)**2*x0+2*(1-t)*t*xm+t*t*x0
        sh.append(L(x-3,y-2,x+3,y+2,"#E53935",1.3)); sh.append(L(x-3,y+2,x+3,y-2,"#E53935",1.3))
add("baseball","Baseball","#ECEFF1",sh)

sh=[C(50,50,46,"#FFFFFF"),PA("M 50 50 Q 30 20 50 4","#FFC107"),PA("M 50 50 Q 80 40 96 54 Q 70 70 50 50","#1E88E5"),
    PA("M 50 50 Q 46 80 24 90","#1E88E5",stroke=3),PA("M 50 50 Q 20 46 6 36","#FFC107",stroke=3),PA("M 50 50 Q 72 24 90 26","#1E88E5",stroke=3),PA("M 50 50 Q 66 76 82 84","#FFC107",stroke=3),C(50,50,46,"#90A4AE",stroke=1.5)]
add("volleyball","Volleyball","#FFFFFF",sh)

sh=[C(50,50,46,"#111111"),C(38,38,18,"#FAFAFA"),T(38,39,22,"8","#111111"),PA("M 64 16 Q 82 26 86 46","#FFFFFF40",stroke=4)]
add("eightball","8-ball","#111111",sh)

sh=[C(50,50,46,"#6A1B9A"),C(50,50,46,"#9C27B0"),PA("M 10 60 Q 40 40 92 62","#BA68C880",stroke=6),C(40,30,5,"#2A0A3A"),C(56,26,5,"#2A0A3A"),C(52,42,5,"#2A0A3A"),PA("M 24 22 Q 36 12 52 10","#FFFFFF60",stroke=3)]
add("bowling","Bowling ball","#4A148C",sh)

sh=[C(50,50,46,"#FFFFFF")]
cols=["#E53935","#FFEB3B","#1E88E5","#43A047","#FB8C00","#8E24AA"]
for i in range(6):
    a=2*math.pi*i/6-math.pi/2; b=2*math.pi*(i+1)/6-math.pi/2
    sh.append(PA(f"M 50 50 L {50+46*math.cos(a):.1f} {50+46*math.sin(a):.1f} Q {50+60*math.cos((a+b)/2):.1f} {50+60*math.sin((a+b)/2):.1f} {50+46*math.cos(b):.1f} {50+46*math.sin(b):.1f} Z",cols[i]))
sh+=[C(50,50,9,"#FFFFFF"),C(50,50,9,"#BDBDBD",stroke=1.5),PA("M 24 20 Q 38 10 56 10","#FFFFFF80",stroke=4)]
add("beachball","Beach ball","#FFFFFF",sh)

# ---------- space ----------
sh=[]
random.seed(9)
for i in range(30): sh.append(C(random.uniform(4,96),random.uniform(4,96),random.uniform(0.4,1.1),"#FFFFFFC0"))
sh+=[E(50,52,44,12,"#C8A26B",stroke=4),C(50,50,24,"#E6B566"),PA("M 28 44 Q 50 40 72 44","#C99A50",stroke=3),PA("M 27 54 Q 50 50 73 54","#D4A55B",stroke=3),PA("M 30 62 Q 50 58 70 62","#C99A50",stroke=2),
     PA("M 6 54 Q 50 72 94 54","#C8A26B",stroke=4),PA("M 10 54 Q 50 68 90 54","#8D6E4F",stroke=1.5)]
add("saturn","Saturn","#0B0E2A",sh,glow="#E6B566")

sh=[C(50,50,40,"#1E88E5"),PA("M 30 22 Q 40 18 44 28 Q 40 36 46 42 Q 40 52 30 46 Q 22 40 24 32 Z","#43A047"),
    PA("M 56 50 Q 66 44 74 52 Q 78 64 70 74 Q 60 80 56 70 Q 52 60 56 50 Z","#43A047"),PA("M 60 20 Q 70 18 76 28 Q 70 32 62 28 Z","#43A047"),
    E(42,62,8,4,"#66BB6A"),PA("M 18 40 Q 30 30 46 34","#FFFFFFB0",stroke=2.5),PA("M 52 82 Q 66 80 76 70","#FFFFFFA0",stroke=2.5),C(50,50,40,"#90CAF9",stroke=2)]
for i in range(20): sh.append(C(random.uniform(2,98),random.uniform(2,98),0.6,"#FFFFFFB0"))
add("earth","Earth","#050818",sh,glow="#64B5F6")

sh=[C(50,50,42,"#CFD8DC"),C(36,36,9,"#B0BEC5"),C(36,36,7,"#A7B4BB"),C(62,30,6,"#B0BEC5"),C(64,58,11,"#B0BEC5"),C(64,58,9,"#A7B4BB"),C(40,66,6,"#B0BEC5"),C(26,54,4,"#B0BEC5"),C(52,48,3,"#B0BEC5"),C(76,44,3,"#B0BEC5"),
    PA("M 70 20 Q 92 46 74 80 Q 90 50 70 20 Z","#90A4AE80")]
add("moon","Moon","#101426",sh,glow="#ECEFF1")

sh=[]
for i in range(16):
    a=2*math.pi*i/16; b=a+math.pi/16; c=a-math.pi/16
    sh.append(P([(50+30*math.cos(c),50+30*math.sin(c)),(50+48*math.cos(a),50+48*math.sin(a)),(50+30*math.cos(b),50+30*math.sin(b))],"#FFB300" if i%2 else "#FF7043"))
sh+=[C(50,50,30,"#FFC107"),C(50,50,24,"#FFD54F"),C(42,44,3,"#5D4037"),C(58,44,3,"#5D4037"),PA("M 40 56 Q 50 64 60 56","#5D4037",stroke=2.5),C(36,54,3,"#FF8A65A0"),C(64,54,3,"#FF8A65A0")]
add("sun","Sun","#FFF3E0",sh,glow="#FFC107")

sh=[]
for i in range(6): sh.append(E(50,50,46-i*5,14-i*1.5,["#FF6F00","#FFA000","#FFCA28","#FFE082","#FFF8E1","#FFFFFF"][i]+"C0",stroke=2.5))
sh+=[C(50,50,13,"#000000"),C(50,50,14,"#FFB74D",stroke=1),PA("M 37 50 Q 50 30 63 50","#FFE0B2",stroke=1.5)]
add("blackhole","Black hole","#05030A",sh,glow="#FF9800")

sh=[PA("M 50 10 Q 66 26 64 56 L 36 56 Q 34 26 50 10 Z","#ECEFF1"),PA("M 50 10 Q 58 18 61 30 L 39 30 Q 42 18 50 10 Z","#E53935"),C(50,40,7,"#4FC3F7"),C(50,40,7,"#455A64",stroke=2),
    P([(36,46),(24,64),(36,60)],"#E53935"),P([(64,46),(76,64),(64,60)],"#E53935"),R(42,56,16,6,"#607D8B",2),
    PA("M 42 62 Q 50 92 58 62 Z","#FF9800"),PA("M 45 62 Q 50 80 55 62 Z","#FFEB3B")]
for i in range(14): sh.append(C(random.uniform(4,96),random.uniform(4,96),0.7,"#FFFFFFB0"))
add("rocket","Rocket","#0D1B3E",sh)

sh=[C(50,40,14,"#80DEEAA0"),C(46,36,4,"#FFFFFF90"),E(50,52,36,10,"#90A4AE"),E(50,49,30,6,"#CFD8DC"),
    C(30,54,2.5,"#FFEB3B"),C(42,57,2.5,"#FF5252"),C(58,57,2.5,"#69F0AE"),C(70,54,2.5,"#40C4FF"),P([(38,60),(62,60),(74,94),(26,94)],"#B2FF5940")]
for i in range(16): sh.append(C(random.uniform(4,96),random.uniform(4,40),0.7,"#FFFFFFB0"))
add("ufo","UFO","#120A2A",sh,glow="#B2FF59")

sh=[]
for k in range(3):
    for i in range(40):
        t=i/40; a=t*4*math.pi+k*2*math.pi/3; r=4+t*42
        sh.append(C(50+r*math.cos(a),50+r*math.sin(a),2.6*(1-t)+0.6,["#CE93D8","#80DEEA","#F48FB1"][k]+"B0"))
sh.append(C(50,50,6,"#FFFFFF"))
for i in range(24): sh.append(C(random.uniform(2,98),random.uniform(2,98),0.5,"#FFFFFF"))
add("spiral","Spiral galaxy","#0A0420",sh,glow="#CE93D8")

# ---------- animal faces ----------
def face_eyes(y=48,dx=13,r=5,col=K,shine=True):
    s=[C(50-dx,y,r,col),C(50+dx,y,r,col)]
    if shine: s+=[C(50-dx+1.6,y-1.6,r*0.35,W),C(50+dx+1.6,y-1.6,r*0.35,W)]
    return s
sh=[P([(14,30),(22,6),(38,22)],"#F5A623"),P([(86,30),(78,6),(62,22)],"#F5A623"),P([(19,26),(23,12),(32,22)],"#FFCCBC"),P([(81,26),(77,12),(68,22)],"#FFCCBC"),
    C(50,54,38,"#F5A623"),PA("M 30 26 Q 34 36 32 44","#D9821C",stroke=3),PA("M 50 18 L 50 30","#D9821C",stroke=3),PA("M 70 26 Q 66 36 68 44","#D9821C",stroke=3),
    E(50,66,14,10,"#FFF3E0")]+face_eyes(50,14,5.5)+[P([(46,60),(54,60),(50,65)],"#F06292"),PA("M 50 65 Q 46 70 42 68","#5D4037",stroke=1.5),PA("M 50 65 Q 54 70 58 68","#5D4037",stroke=1.5)]
for s in (-1,1):
    for k in range(3): sh.append(L(50+s*18,62+k*3,50+s*36,58+k*5,"#5D4037",1))
add("cat","Cat","#FFE0B2",sh)

sh=[E(20,40,11,22,"#8D6E63"),E(80,40,11,22,"#8D6E63"),C(50,54,36,"#D7A86E"),E(50,68,18,13,"#FFF3E0"),E(36,46,7,8,"#8D6E63")]+face_eyes(48,14,5)+[E(50,60,6,4.5,K),C(48,58.5,1.5,W),PA("M 50 64 Q 45 72 40 68","#5D4037",stroke=1.5),PA("M 50 64 Q 55 72 60 68","#5D4037",stroke=1.5),E(50,74,4,5,"#F06292")]
add("dog","Dog","#BBDEFB",sh)

sh=[C(22,22,12,K),C(78,22,12,K),C(50,54,38,"#FAFAFA"),E(35,48,10,12,K),E(65,48,10,12,K),C(36,48,4,W),C(64,48,4,W),C(37,47,2,K),C(63,47,2,K),E(50,64,6,4,K),PA("M 50 67 Q 46 72 42 70","#333",stroke=1.5),PA("M 50 67 Q 54 72 58 70","#333",stroke=1.5),C(30,62,4,"#F8BBD0A0"),C(70,62,4,"#F8BBD0A0")]
add("panda","Panda","#A5D6A7",sh)

sh=[P([(14,10),(36,36),(18,44)],"#E65100"),P([(86,10),(64,36),(82,44)],"#E65100"),P([(18,16),(30,34),(21,38)],"#3E2723"),P([(82,16),(70,34),(79,38)],"#3E2723"),
    P([(12,40),(50,28),(88,40),(70,74),(50,88),(30,74)],"#F57C00"),P([(24,54),(50,88),(76,54),(64,66),(50,72),(36,66)],"#FFF3E0")]+face_eyes(50,15,4.5)+[C(50,80,5,K),C(48.5,79,1.3,W)]
add("fox","Fox","#3E2723",sh)

sh=[C(30,30,14,"#66BB6A"),C(70,30,14,"#66BB6A"),E(50,58,40,30,"#66BB6A"),C(30,30,10,W),C(70,30,10,W),C(31,31,6,K),C(71,31,6,K),C(33,29,2,W),C(73,29,2,W),
    PA("M 26 62 Q 50 80 74 62","#2E7D32",stroke=3),C(26,56,4,"#F48FB1A0"),C(74,56,4,"#F48FB1A0"),C(44,50,1.4,"#2E7D32"),C(56,50,1.4,"#2E7D32")]
add("frog","Frog","#E8F5E9",sh)

sh=[C(50,52,40,"#263238"),PA("M 30 34 Q 50 22 70 34 Q 80 60 50 86 Q 20 60 30 34 Z","#FAFAFA")]+face_eyes(46,10,4.5)+[P([(42,56),(58,56),(50,66)],"#FFA000"),C(30,60,4,"#F8BBD0A0"),C(70,60,4,"#F8BBD0A0")]
add("penguin","Penguin","#81D4FA",sh)

sh=[P([(16,18),(34,30),(24,36)],"#6D4C41"),P([(84,18),(66,30),(76,36)],"#6D4C41"),C(50,54,38,"#8D6E63"),E(50,72,22,16,"#D7CCC8")]
for k in range(4):
    for j in range(5): sh.append(PA(f"M {32+j*9} {68+k*5} q 2 2 4 0","#A1887F",stroke=1))
sh+=[C(34,46,13,"#FFF8E1"),C(66,46,13,"#FFF8E1"),C(34,46,7,"#FFB300"),C(66,46,7,"#FFB300"),C(34,46,4,K),C(66,46,4,K),C(35.5,44.5,1.5,W),C(67.5,44.5,1.5,W),P([(46,56),(54,56),(50,64)],"#FF8F00")]
add("owl","Owl","#4E342E",sh)

sh=[C(22,24,12,"#6D4C41"),C(78,24,12,"#6D4C41"),C(22,24,6,"#A1887F"),C(78,24,6,"#A1887F"),C(50,54,38,"#795548"),E(50,66,16,12,"#BCAAA4")]+face_eyes(48,14,4.5)+[E(50,60,6,4,K),PA("M 50 63 Q 46 70 42 67","#3E2723",stroke=1.5),PA("M 50 63 Q 54 70 58 67","#3E2723",stroke=1.5)]
add("bear","Bear","#C8E6C9",sh)

sh=[E(36,20,8,22,"#FAFAFA"),E(64,20,8,22,"#FAFAFA"),E(36,20,4,16,"#F8BBD0"),E(64,20,4,16,"#F8BBD0"),C(50,58,34,"#FAFAFA")]+face_eyes(54,12,4.5)+[P([(46,64),(54,64),(50,68)],"#F06292"),L(50,68,50,72,"#9E9E9E",1.2),C(32,66,4,"#F8BBD0B0"),C(68,66,4,"#F8BBD0B0")]
add("bunny","Bunny","#CE93D8",sh)

sh=[P([(18,22),(34,26),(26,40)],"#F48FB1"),P([(82,22),(66,26),(74,40)],"#F48FB1"),C(50,54,38,"#F8BBD0")]+face_eyes(44,14,4)+[E(50,62,14,10,"#F48FB1"),E(45,62,2.5,3.5,"#AD1457"),E(55,62,2.5,3.5,"#AD1457"),C(28,58,4,"#F06292A0"),C(72,58,4,"#F06292A0")]
add("pig","Pig","#FFF9C4",sh)

sh=[C(50,52,40,"#FFEB3B"),PA("M 46 14 Q 50 4 54 14","#FBC02D",stroke=3)]+face_eyes(46,12,4.5)+[P([(44,56),(56,56),(50,64)],"#FF9800"),C(30,58,4,"#FF8A65A0"),C(70,58,4,"#FF8A65A0"),E(18,62,6,10,"#FDD835"),E(82,62,6,10,"#FDD835")]
add("chick","Chick","#B3E5FC",sh)

sh=[C(50,56,38,"#E53935"),L(50,20,50,94,K,3),C(50,24,16,K),C(44,20,3,W),C(56,20,3,W)]
for (x,y,r) in [(32,46,7),(68,46,7),(30,68,6),(70,68,6),(42,82,4),(58,82,4)]: sh.append(C(x,y,r,K))
sh+=[PA("M 42 10 Q 36 2 30 6","#1A1A1A",stroke=2),PA("M 58 10 Q 64 2 70 6","#1A1A1A",stroke=2),PA("M 30 34 Q 40 28 46 32","#FFFFFF70",stroke=2.5)]
add("ladybug","Ladybug","#A5D6A7",sh)

sh=[E(30,32,14,10,"#E1F5FEC0"),E(70,32,14,10,"#E1F5FEC0"),E(50,58,30,32,"#FFCA28"),R(20,46,60,8,K),R(20,62,60,8,K),R(24,78,52,6,K,3),C(50,30,18,K),C(44,28,4,W),C(56,28,4,W),C(45,29,2,K),C(57,29,2,K),
    PA("M 44 12 Q 40 4 34 6","#1A1A1A",stroke=2),PA("M 56 12 Q 60 4 66 6","#1A1A1A",stroke=2),C(34,6,2,K),C(66,6,2,K)]
add("bee","Bee","#B2EBF2",sh)

# ---------- objects ----------
sh=[C(50,50,46,"#111111")]
for r in range(14,44,3): sh.append(C(50,50,r,"#2A2A2A",stroke=0.8))
sh+=[C(50,50,13,"#E53935"),C(50,50,13,"#B71C1C",stroke=1),T(50,47,5,"VOICE","#FFFFFF"),T(50,54,4,"BUBBLE","#FFFFFF"),C(50,50,2,"#111111"),PA("M 20 30 Q 30 16 46 12","#FFFFFF30",stroke=5),PA("M 80 70 Q 70 84 54 88","#FFFFFF20",stroke=5)]
add("vinyl","Vinyl record","#111111",sh)

sh=[C(50,50,46,"#212121"),C(50,50,40,"#424242"),C(50,50,34,"#111111")]
for i in range(6):
    a=2*math.pi*i/6; sh.append(P([(50,50),(50+30*math.cos(a),50+30*math.sin(a)),(50+30*math.cos(a+1.05),50+30*math.sin(a+1.05))],"#1A237E"))
sh+=[C(50,50,20,"#0D47A1"),C(50,50,13,"#1A237E"),C(50,50,7,"#000000"),C(42,40,6,"#FFFFFF80"),C(58,58,3,"#E1BEE780"),C(50,50,40,"#757575",stroke=1)]
for i in range(24):
    a=2*math.pi*i/24; sh.append(L(50+42*math.cos(a),50+42*math.sin(a),50+45*math.cos(a),50+45*math.sin(a),"#9E9E9E",1))
add("lens","Camera lens","#212121",sh)

sh=[C(50,50,46,"#5D4037"),C(50,50,40,"#FFF8E1")]
for i in range(60):
    a=2*math.pi*i/60; r1=36 if i%5==0 else 38
    sh.append(L(50+r1*math.cos(a),50+r1*math.sin(a),50+40*math.cos(a)*0.99,50+40*math.sin(a)*0.99,"#3E2723",1.6 if i%5==0 else 0.7))
for i,n in enumerate(["12","3","6","9"]):
    a=math.radians(-90+i*90); sh.append(T(50+29*math.cos(a),50+29*math.sin(a),9,n,"#3E2723"))
sh+=[L(50,50,50,26,"#3E2723",3),L(50,50,66,58,"#3E2723",2.5),L(50,56,50,18,"#E53935",1),C(50,50,3,"#E53935")]
add("clock","Clock","#5D4037",sh)

sh=[C(50,50,46,"#B8860B"),C(50,50,41,"#F5F0E1"),C(50,50,41,"#8D6E63",stroke=1)]
for i in range(16):
    a=2*math.pi*i/16; sh.append(L(50+36*math.cos(a),50+36*math.sin(a),50+40*math.cos(a),50+40*math.sin(a),"#5D4037",1.5 if i%4==0 else 0.8))
for i,n in enumerate("NESW"):
    a=math.radians(-90+i*90); sh.append(T(50+29*math.cos(a),50+29*math.sin(a),9,n,"#B71C1C" if n=="N" else "#3E2723"))
sh+=[P([(50,22),(56,50),(50,50)],"#E53935"),P([(50,22),(44,50),(50,50)],"#B71C1C"),P([(50,78),(56,50),(50,50)],"#B0BEC5"),P([(50,78),(44,50),(50,50)],"#78909C"),C(50,50,3,"#B8860B")]
add("compass","Compass","#3E2723",sh)

sh=[C(50,46,38,"#7E57C2C0"),C(50,46,30,"#B39DDB60"),PA("M 30 40 Q 46 28 60 36 Q 66 50 52 56 Q 40 58 42 48","#E1BEE7",stroke=2),C(40,30,8,"#FFFFFFA0"),
    C(62,54,1.5,W),C(36,56,1,W),C(58,30,1,W),R(28,80,44,10,"#6D4C41",4),R(32,76,36,6,"#8D6E63",3)]
add("crystal","Crystal ball","#1A0B2E",sh,glow="#B39DDB")

sh=[C(50,50,44,"#E53935"),C(50,50,26,"#00ACC1")]
for i in range(4):
    a=math.radians(45+i*90); b=a+math.radians(40); c_=a-math.radians(20)
    sh.append(PA(f"M {50+26*math.cos(c_):.1f} {50+26*math.sin(c_):.1f} L {50+44*math.cos(c_):.1f} {50+44*math.sin(c_):.1f} A 0 0 0 0 0 0 0 L {50+44*math.cos(b-math.radians(20)):.1f} {50+44*math.sin(b-math.radians(20)):.1f} L {50+26*math.cos(b-math.radians(20)):.1f} {50+26*math.sin(b-math.radians(20)):.1f} Z".replace(" A 0 0 0 0 0 0 0",""),"#FAFAFA"))
sh+=[C(50,50,35,"#B71C1C",stroke=1.2),PA("M 22 30 Q 34 16 52 12","#FFFFFF60",stroke=3)]
add("lifering","Life ring","#00838F",sh)

sh=[C(50,50,44,K),PA("M 50 6 A 44 44 0 0 1 50 94 A 22 22 0 0 1 50 50 A 22 22 0 0 0 50 6 Z","#FAFAFA"),C(50,28,6,K),C(50,72,6,"#FAFAFA"),C(50,50,44,"#9E9E9E",stroke=1.5)]
add("yinyang","Yin yang","#90A4AE",sh)

sh=[R(20,26,60,52,"#B0BEC5",12),R(26,34,48,30,"#263238",8),C(38,48,6,"#00E5FF"),C(62,48,6,"#00E5FF"),C(38,48,2.5,W),C(62,48,2.5,W),
    R(36,68,28,5,"#78909C",2),L(42,68,42,73,"#455A64",1),L(50,68,50,73,"#455A64",1),L(58,68,58,73,"#455A64",1),L(50,26,50,14,"#78909C",3),C(50,12,4,"#FF5252"),C(20,50,4,"#78909C"),C(80,50,4,"#78909C")]
add("robot","Robot","#37474F",sh,glow="#00E5FF")

sh=[PA("M 26 88 L 26 44 Q 26 16 50 16 Q 74 16 74 44 L 74 88 L 66 80 L 58 88 L 50 80 L 42 88 L 34 80 Z","#FAFAFA"),E(40,44,5,7,K),E(60,44,5,7,K),C(41,42,1.6,W),C(61,42,1.6,W),E(50,60,5,6,K),C(34,54,4,"#F8BBD0B0"),C(66,54,4,"#F8BBD0B0")]
add("ghost","Ghost","#5E35B1",sh,glow="#FFFFFF")

sh=[PA("M 12 84 Q 10 50 30 36 Q 50 22 70 36 Q 90 50 88 84 Z","#76FF03C0"),PA("M 24 70 Q 22 52 34 44","#CCFF90",stroke=3)]+face_eyes(56,12,5)+[PA("M 42 70 Q 50 76 58 70","#1B5E20",stroke=2.5),C(70,40,3,"#CCFF90")]
add("slime","Slime","#1B5E20",sh,glow="#76FF03")

sh=[E(50,50,32,40,"#81C784"),E(36,46,10,14,K),E(64,46,10,14,K),E(33,42,3,4,W),E(61,42,3,4,W),L(40,10,30,0,"#81C784",3),L(60,10,70,0,"#81C784",3),C(30,2,4,"#C5E1A5"),C(70,2,4,"#C5E1A5"),PA("M 44 72 Q 50 76 56 72","#1B5E20",stroke=2)]
add("alien","Alien","#1A237E",sh,glow="#81C784")

sh=[R(18,18,64,64,"#FAFAFA",14),R(18,18,64,64,"#BDBDBD",14,stroke=2)]
for (x,y) in [(32,32),(68,32),(50,50),(32,68),(68,68)]: sh+=[C(x,y,6,"#E53935"),C(x-1.5,y-1.5,2,"#FF8A80")]
add("dice","Dice","#C62828",sh)

# Colours with see-through were written as #RRGGBBAA: the app reads #AARRGGBB.
def fix(o):
    if isinstance(o,dict): return {k:fix(v) for k,v in o.items()}
    if isinstance(o,list): return [fix(v) for v in o]
    if isinstance(o,str) and o.startswith('#') and len(o)==9: return '#'+o[7:9]+o[1:7]
    return o
S=fix(S)
print(len(S))
json.dump(S, open(sys.argv[1],"w"))
