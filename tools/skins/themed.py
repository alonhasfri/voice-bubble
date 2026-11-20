import json, math, random, sys
exec(open('gen.py').read().split('# ---------- food ----------')[0])  # helpers C,E,R,L,P,PA,T,star,add,S
S.clear()
HEART=[".XX...XX.","XXXX.XXXX","XXXXXXXXX","XXXXXXXXX",".XXXXXXX.","..XXXXX..","...XXX...","....X...."]
def pixel_heart(col, light, dark, px=6, ox=23, oy=27):
    sh=[]
    for y,row in enumerate(HEART):
        for x,ch in enumerate(row):
            if ch=='X':
                c=col
                if (x,y) in [(1,1),(2,1),(1,2)]: c=light
                elif y>=4 and x>=5 and HEART[y][x]=='X' and (x+y)>=10: c=dark
                sh.append(R(ox+x*px,oy+y*px,px+0.3,px+0.3,c))
    return sh
souls=[("determination","Determination soul","#FF0000","#FF8080","#B00000"),("patience","Patience soul","#00FFFF","#B0FFFF","#00A0A8"),
       ("bravery","Bravery soul","#FF9A00","#FFD080","#B05A00"),("justice","Justice soul","#FFF200","#FFFAA0","#B0A000"),
       ("kindness","Kindness soul","#00E000","#A0FFA0","#008800"),("perseverance","Perseverance soul","#D535D9","#F0A0F0","#8A1A90"),
       ("integrity","Integrity soul","#0040FF","#90A8FF","#002090")]
for i,name,c,l,d in souls:
    sh=[C(50,50,40,c+"30")] + pixel_heart(c,l,d) + [C(20,24,1,"#FFFFFF"),C(80,30,0.8,"#FFFFFF"),C(76,76,1,"#FFFFFF"),C(24,74,0.7,"#FFFFFF")]
    add("ut_"+i,name,"#000000",sh,glow=c)

# Butterscotch pie slice
sh=[P([(14,70),(86,70),(50,18)],"#C68B3C"),P([(20,66),(80,66),(50,26)],"#E8B04A")]
for k in range(1,5): sh.append(L(50-k*7,26+k*10,50+k*7,26+k*10,"#B87A2C",2.2))
for k in range(-3,4): sh.append(L(50+k*4,28,50+k*10,66,"#B87A2C",1.6))
sh+=[R(12,68,76,8,"#9C6A2A",4),C(34,58,2,"#FFE6A0"),C(60,50,1.6,"#FFE6A0"),E(58,22,6,3,"#FFFFFF"),C(40,64,1.5,"#FFE6A0")]
add("ut_pie","Butterscotch pie","#7E57C2",sh)
# Spaghetti
sh=[C(50,52,40,"#ECEFF1"),C(50,52,34,"#CFD8DC"),C(50,52,26,"#F5D27A")]
random.seed(4)
for k in range(26):
    a=random.uniform(0,6.28); r=random.uniform(4,22); b=a+random.uniform(1,2.5)
    sh.append(PA(f"M {50+r*math.cos(a):.1f} {52+r*math.sin(a):.1f} Q 50 52 {50+r*math.cos(b):.1f} {52+r*math.sin(b):.1f}","#E2B85A",stroke=1.4))
sh+=[PA("M 34 46 Q 44 36 58 42 Q 66 50 58 58 Q 46 64 38 56 Z","#D32F2F"),C(44,48,5,"#6D3B1F"),C(56,52,4.5,"#6D3B1F"),C(48,58,4,"#6D3B1F"),C(43,47,1.4,"#A0663A"),C(55,51,1.2,"#A0663A"),E(62,40,3,1.5,"#43A047")]
add("ut_spaghetti","Spaghetti","#FFCDD2",sh)
# Monster candy
sh=[P([(14,38),(30,50),(14,62),(20,50)],"#F06292"),P([(86,38),(70,50),(86,62),(80,50)],"#F06292"),E(50,50,24,18,"#F48FB1"),PA("M 30 44 Q 50 30 70 44","#FFFFFF90",stroke=3),
    PA("M 34 58 Q 50 66 66 58","#C2185B",stroke=2),L(30,50,70,50,"#FCE4EC",1.2),T(50,51,14,"M","#AD1457")]
add("ut_candy","Monster candy","#6A1B9A",sh)
# Echo flower
sh=[]
random.seed(8)
for k in range(18): sh.append(C(random.uniform(6,94),random.uniform(6,94),random.uniform(0.5,1.3),"#B3E5FC"))
sh+=[PA("M 50 92 Q 46 70 50 52","#26A69A",stroke=3),PA("M 50 76 Q 36 70 32 60 Q 44 62 50 72","#26A69A"),PA("M 50 70 Q 64 64 68 54 Q 56 58 50 66","#26A69A")]
for k in range(6):
    a=math.radians(-90+k*60); sh.append(E(50+12*math.cos(a),40+12*math.sin(a),8,5,"#4FC3F7"))
sh+=[C(50,40,7,"#E1F5FE"),C(50,40,4,"#FFFFFF")]
add("ut_echo","Echo flower","#0A1A3A",sh,glow="#4FC3F7")
# Golden flowers
sh=[PA("M 0 70 Q 50 56 100 70 L 100 100 L 0 100 Z","#33691E"),PA("M 0 80 Q 50 70 100 82 L 100 100 L 0 100 Z","#558B2F")]
for (x,y,s) in [(30,50,1),(56,42,1.2),(74,58,0.9),(42,66,0.8),(18,66,0.7)]:
    sh.append(L(x,y+6*s,x+2,y+22*s,"#689F38",2))
    for k in range(6):
        a=math.radians(k*60); sh.append(E(x+7*s*math.cos(a),y+7*s*math.sin(a),5*s,3*s,"#FFD600"))
    sh+=[C(x,y,4*s,"#FF8F00"),C(x-1,y-1,1.4*s,"#FFE082")]
add("ut_goldflowers","Golden flowers","#1B1B2E",sh)
# Snowy town
sh=[R(0,70,100,30,"#ECEFF1"),PA("M 0 74 Q 30 66 60 74 Q 80 78 100 72 L 100 100 L 0 100 Z","#FFFFFF")]
for (x,s) in [(22,1),(48,1.3),(76,0.9)]:
    for k in range(3): sh.append(P([(x-14*s+k*3*s,70-k*12*s),(x+14*s-k*3*s,70-k*12*s),(x,52*1-(k*12*s)-6*s+18-18)],"#1B5E20"))
    sh.append(R(x-2,70,4,6,"#4E342E"))
random.seed(12)
for k in range(30): sh.append(C(random.uniform(4,96),random.uniform(4,70),random.uniform(0.6,1.6),"#FFFFFF"))
add("ut_snow","Snowy town","#1A237E",sh)
# Blue bones
def bone(x1,y1,x2,y2,c,w=6):
    a=math.atan2(y2-y1,x2-x1); ox,oy=-math.sin(a)*w*0.55,math.cos(a)*w*0.55
    return [L(x1,y1,x2,y2,c,w),C(x1+ox,y1+oy,w*0.62,c),C(x1-ox,y1-oy,w*0.62,c),C(x2+ox,y2+oy,w*0.62,c),C(x2-ox,y2-oy,w*0.62,c)]
sh=bone(24,24,76,76,"#B3E5FC")+bone(76,24,24,76,"#29B6F6")+[L(30,30,70,70,"#FFFFFF80",1.5)]
add("ut_bones","Blue bones","#0B0B14",sh,glow="#29B6F6")
# Spear of justice style: cyan spears
sh=[]
for (x1,y1,x2,y2) in [(14,86,80,20),(20,20,86,80),(50,92,50,10)]:
    sh+=[L(x1,y1,x2,y2,"#00E5FF",3)]
    a=math.atan2(y2-y1,x2-x1); tip=(x2,y2)
    sh.append(P([tip,(x2-10*math.cos(a)+5*math.sin(a),y2-10*math.sin(a)-5*math.cos(a)),(x2-10*math.cos(a)-5*math.sin(a),y2-10*math.sin(a)+5*math.cos(a))],"#80FFFF"))
sh.append(C(50,50,8,"#00E5FF60"))
add("ut_spears","Spears","#001018",sh,glow="#00E5FF")
# Hotland
sh=[PA("M 0 64 Q 25 56 50 64 Q 75 72 100 62 L 100 100 L 0 100 Z","#FF6F00"),PA("M 0 74 Q 30 66 60 76 Q 80 80 100 72 L 100 100 L 0 100 Z","#E65100"),
    R(16,24,10,44,"#5D4037"),R(14,24,14,6,"#795548",2),R(64,30,12,38,"#5D4037"),R(62,30,16,6,"#795548",2),R(26,40,38,6,"#6D4C41",2),C(44,43,3,"#FFAB40")]
for (x,y) in [(30,80),(56,84),(78,78),(44,90)]: sh.append(C(x,y,2,"#FFE082"))
sh.append(PA("M 70 22 Q 74 14 70 8","#FFAB4080",stroke=3))
add("ut_hotland","Hotland","#3E0E0E",sh,glow="#FF6F00")
# Ruins
sh=[]
for row in range(8):
    for col in range(6):
        x=(col*18)-(9 if row%2 else 0); sh.append(R(x,row*13,17,12,"#6A1B9A" if (row+col)%3 else "#7B1FA2",1))
sh+=[PA("M 30 40 Q 40 30 50 40 Q 40 50 30 40 Z","#E53935"),PA("M 50 56 Q 62 44 74 56 Q 62 68 50 56 Z","#C62828"),PA("M 22 64 Q 30 56 38 64 Q 30 72 22 64 Z","#EF5350"),
    L(40,40,50,44,"#8E2420",1),L(62,56,70,50,"#8E2420",1)]
add("ut_ruins","Ruins","#4A148C",sh)
# Temmie flakes
sh=[E(50,64,36,22,"#FAFAFA"),E(50,52,36,12,"#E3F2FD"),E(50,52,32,9,"#FFFFFF")]
random.seed(6)
for k in range(20):
    x=random.uniform(24,76); y=random.uniform(46,58); sh.append(P([(x,y),(x+4,y-1),(x+3,y+3)],random.choice(["#FFCC80","#F8BBD0","#FFF59D"])))
sh+=[PA("M 16 54 Q 50 92 84 54","#BBDEFB",stroke=2.5),T(50,82,9,"tem","#5C6BC0")]
add("ut_temflakes","Tem flakes","#C5CAE9",sh)
# Hot dog
sh=[PA("M 16 58 Q 50 84 84 58 Q 80 72 50 78 Q 20 72 16 58 Z","#E0A050"),E(50,52,36,9,"#C0392B"),PA("M 20 50 Q 30 44 40 50 Q 50 56 60 50 Q 70 44 80 50","#FFD600",stroke=2.5),PA("M 16 50 Q 50 30 84 50 Q 80 42 50 38 Q 20 42 16 50 Z","#E8B060"),C(40,44,1,"#FFF3E0"),C(60,42,1,"#FFF3E0")]
add("ut_hotdog","Hot dog","#0D47A1",sh)

# ---------- Deltarune-inspired ----------
sh=[P(star(50,50,34,15,5,-90),"#4A148C"),P(star(50,50,28,12,5,-90),"#7B1FA2"),P(star(50,50,14,6,5,-90),"#CE93D8"),C(42,40,3,"#FFFFFF90"),C(70,72,1,"#FFFFFF"),C(24,30,1,"#FFFFFF"),C(80,30,1.2,"#FFFFFF")]
add("dr_darkcandy","Dark candy","#0B0618",sh,glow="#7B1FA2")
sh=[PA("M 0 66 Q 50 50 100 66 L 100 100 L 0 100 Z","#283593"),PA("M 0 78 Q 50 66 100 80 L 100 100 L 0 100 Z","#1A237E")]
for (x,y,s) in [(24,56,1),(74,54,1.2),(48,62,0.8)]:
    sh+=[L(x,y,x,y+16*s,"#7986CB",2),C(x,y,8*s,"#5C6BC0"),C(x-2,y-2,3*s,"#9FA8DA")]
random.seed(14)
for k in range(26): sh.append(C(random.uniform(4,96),random.uniform(4,54),random.uniform(0.5,1.4),"#FFFFFF"))
sh.append(P(star(50,22,6,2.4,4,-90),"#FFF59D"))
add("dr_field","Field of dreams","#0D0B2E",sh)
sh=[]
for (x,w,h) in [(4,14,40),(18,12,56),(30,16,34),(46,10,62),(56,16,46),(72,12,58),(84,14,38)]:
    sh.append(R(x,100-h,w,h,"#1A0033"))
    for yy in range(100-h+4,96,6):
        for xx in range(x+2,x+w-2,4): sh.append(R(xx,yy,2,2.5,random.choice(["#00E5FF","#FF4081","#1A0033","#1A0033"])))
for k in range(5): sh.append(L(0,30+k*6,100,30+k*6,"#FF408140",0.8))
add("dr_cyber","Cyber city","#120024",sh,glow="#FF4081")
sh=[P([(50,10),(64,40),(56,80),(44,80),(36,40)],"#3949AB"),P([(50,10),(64,40),(50,46)],"#5C6BC0"),P([(36,40),(50,46),(44,80)],"#283593"),
    P([(24,40),(34,52),(30,76),(20,70)],"#303F9F"),P([(76,44),(84,62),(74,80),(68,60)],"#303F9F"),L(50,12,50,44,"#C5CAE9",1.2),C(54,30,1.5,"#FFFFFF")]
add("dr_crystal","Shadow crystal","#0A0A1A",sh,glow="#7986CB")
sh=[E(50,54,26,32,"#FFF8E1"),E(50,54,26,32,"#BCAAA4",stroke=1.5),C(42,44,3,"#D7CCC8"),C(58,62,2.5,"#D7CCC8"),C(44,70,2,"#D7CCC8"),PA("M 36 40 Q 42 28 54 26","#FFFFFFB0",stroke=3)]
add("dr_pipis","Pipis","#FF4081",sh)
sh=[]
for row in range(-1,6):
    for col in range(-1,6):
        cx_=col*20+(10 if row%2 else 0); cy_=row*20
        sh.append(P([(cx_,cy_-10),(cx_+10,cy_),(cx_,cy_+10),(cx_-10,cy_)],"#6A1B9A" if (row+col)%2 else "#FBC02D"))
sh+=[C(50,50,20,"#1A0A2A"),PA("M 38 46 Q 50 36 62 46","#FBC02D",stroke=3),C(42,52,3,"#FFFFFF"),C(58,52,3,"#FFFFFF"),PA("M 40 60 Q 50 68 60 60","#FFFFFF",stroke=2.5)]
add("dr_jester","Chaos jester","#6A1B9A",sh,glow="#FBC02D")
sh=[R(12,28,76,40,"#FFEB3B",8),R(12,28,76,40,"#FF4081",8,stroke=3),T(50,44,13,"[[  ]]","#FF4081"),T(50,58,8,"DEAL$","#000000"),P([(30,68),(42,68),(28,82)],"#FFEB3B")]
add("dr_hyperlink","Hyperlink","#1A1A1A",sh,glow="#FF4081")
sh=[]
for (x,y,s) in [(18,62,1),(40,52,1.3),(64,58,1.1),(84,64,0.9),(30,78,0.8),(56,80,0.9)]:
    sh+=[R(x-1.5,y,3,16*s,"#3E0A0A"),P([(x,y-20*s),(x+10*s,y+2),(x-10*s,y+2)],"#C62828"),P([(x,y-26*s),(x+7*s,y-10*s),(x-7*s,y-10*s)],"#E53935")]
add("dr_scarlet","Scarlet forest","#1A0505",sh)
sh=[R(34,22,32,58,"#FFFFFF",3),R(38,26,24,50,"#F5F5F5",2),C(58,52,2,"#9E9E9E"),PA("M 30 82 L 70 82","#FFFFFF60",stroke=2)]
random.seed(21)
for k in range(10): sh.append(C(random.uniform(6,94),random.uniform(6,94),0.6,"#FFFFFF90"))
add("dr_door","Dark world door","#000000",sh,glow="#FFFFFF")

# ---------- JJK-inspired ----------
def orb(i,name,core,mid,outer,bg):
    sh=[]
    for k in range(5): sh.append(C(50,50,40-k*3,outer+"" if False else outer,stroke=1.2))
    sh+=[C(50,50,24,outer),C(50,50,17,mid),C(50,50,9,core),C(46,46,3,"#FFFFFF")]
    for k in range(8):
        a=k*0.8; sh.append(PA(f"M {50+30*math.cos(a):.1f} {50+30*math.sin(a):.1f} Q {50+20*math.cos(a+0.5):.1f} {50+20*math.sin(a+0.5):.1f} {50+14*math.cos(a+1.1):.1f} {50+14*math.sin(a+1.1):.1f}",mid,stroke=1.6))
    add(i,name,bg,sh,glow=mid)
orb("jjk_red","Reversal red","#FFFFFF","#FF1744","#B71C1C","#120000")
orb("jjk_blue","Lapse blue","#FFFFFF","#2979FF","#0D47A1","#000814")
sh=[PA("M 40 88 L 42 30 Q 46 14 54 14 Q 62 16 60 32 L 60 88 Z","#8D6E63"),PA("M 44 26 Q 50 18 56 26","#5D4037",stroke=2)]
for y in range(36,86,8): sh.append(PA(f"M 41 {y} Q 50 {y-3} 60 {y+1}","#4E342E",stroke=1.4))
sh+=[R(38,44,26,16,"#F5F5DC",1),T(51,52,7,"封","#B71C1C"),E(50,18,5,3,"#A1887F")]
add("jjk_finger","Cursed finger","#1B0F0A",sh,glow="#8E24AA")
sh=[C(50,28,13,"#D4A64A"),R(40,38,20,30,"#D4A64A",6),L(40,46,18,40,"#D4A64A",7),L(60,46,82,40,"#D4A64A",7),L(44,66,36,90,"#D4A64A",7),L(56,66,64,90,"#D4A64A",7)]
for y in range(20,90,5): sh.append(L(36,y,64,y+2,"#B8862E",0.8))
sh+=[L(30,20,58,40,"#9E9E9E",2.2),L(58,40,60,42,"#616161",3),C(30,20,2.5,"#757575"),R(42,50,16,4,"#B71C1C",1)]
add("jjk_doll","Straw doll","#2A0A0A",sh,glow="#E53935")
sh=[]
for (x1,y1,x2,y2,w) in [(12,20,88,84,5),(22,12,84,72,3),(10,40,72,92,3),(88,16,16,88,4),(76,10,12,70,2.5)]:
    sh+= [L(x1,y1,x2,y2,"#B71C1C",w+2),L(x1,y1,x2,y2,"#FF5252",w),L(x1,y1,x2,y2,"#FFCDD2",w*0.3)]
add("jjk_cleave","Cleave","#0B0000",sh,glow="#FF1744")
sh=[PA("M 0 80 L 100 80 L 100 100 L 0 100 Z","#1A0000"),R(22,40,6,40,"#B71C1C"),R(72,40,6,40,"#B71C1C"),PA("M 10 34 Q 50 24 90 34 L 88 40 Q 50 32 12 40 Z","#C62828"),R(16,46,68,5,"#B71C1C")]
random.seed(31)
for k in range(10):
    x=random.uniform(10,90); y=random.uniform(60,82); sh.append(PA(f"M {x:.1f} {y:.1f} Q {x-4:.1f} {y-10:.1f} {x:.1f} {y-18:.1f} Q {x+4:.1f} {y-10:.1f} {x:.1f} {y:.1f} Z","#FF6D00A0"))
add("jjk_shrine","Shrine","#100000",sh,glow="#FF3D00")
sh=[P([(22,84),(26,40),(34,18),(44,36),(56,36),(66,18),(74,40),(78,84)],"#1A1A2E"),P([(30,46),(42,52),(34,56)],"#E0E0E0"),P([(70,46),(58,52),(66,56)],"#E0E0E0"),
    P([(42,70),(58,70),(50,82)],"#000000"),L(36,40,44,48,"#3949AB",1.5),L(64,40,56,48,"#3949AB",1.5)]
add("jjk_dog","Shadow wolf","#E8EAF6",sh)
sh=[R(0,40,100,18,"#111111"),R(0,40,100,18,"#333333",0,stroke=1.5),PA("M 0 40 L 100 40","#222",stroke=1)]
for (x,y) in [(10,20),(22,10),(34,18),(46,6),(58,16),(70,8),(82,18),(92,12)]: sh.append(P([(x-6,40),(x,y),(x+6,40)],"#F5F5F5"))
sh+=[E(36,66,8,4,"#E3F2FD"),E(64,66,8,4,"#E3F2FD"),C(36,66,3.5,"#29B6F6"),C(64,66,3.5,"#29B6F6"),C(36,66,1.4,"#01579B"),C(64,66,1.4,"#01579B")]
add("jjk_blindfold","Blindfold","#ECEFF1",sh)
sh=[R(32,12,36,76,"#FFF8E1",2),R(32,12,36,76,"#D7CCC8",2,stroke=1.5),C(50,30,9,"#C62828"),C(50,30,9,"#8E0000",stroke=1.5),T(50,31,10,"呪","#FFF8E1"),
    L(40,48,60,48,"#B71C1C",1.5),L(40,56,60,56,"#B71C1C",1.5),L(40,64,56,64,"#B71C1C",1.5),L(40,72,58,72,"#B71C1C",1.5)]
add("jjk_talisman","Talisman","#3E2723",sh)
sh=[PA("M 50 90 Q 20 76 26 48 Q 30 30 42 18 Q 40 34 48 40 Q 52 22 66 12 Q 62 30 70 40 Q 82 56 74 76 Q 66 90 50 90 Z","#651FFF"),
    PA("M 50 84 Q 32 74 36 54 Q 40 42 48 36 Q 48 50 56 52 Q 58 40 64 34 Q 72 52 66 70 Q 60 84 50 84 Z","#7C4DFF"),PA("M 50 78 Q 42 70 44 60 Q 48 52 52 50 Q 56 62 58 66 Q 58 76 50 78 Z","#B388FF")]
add("jjk_flame","Cursed flame","#0A0020",sh,glow="#651FFF")

# Colours with see-through were written as #RRGGBBAA: the app reads #AARRGGBB.
def fix(o):
    if isinstance(o,dict): return {k:fix(v) for k,v in o.items()}
    if isinstance(o,list): return [fix(v) for v in o]
    if isinstance(o,str) and o.startswith('#') and len(o)==9: return '#'+o[7:9]+o[1:7]
    return o
out=fix(S)
print(len(out)); json.dump(out,open(sys.argv[1],'w'))
