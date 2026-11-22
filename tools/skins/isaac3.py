import json, math, sys
exec(open('gen.py').read().split('# ---------- food ----------')[0])
S.clear()
K="#121012"; SK="#F3CEC4"; SH="#E3AFA6"
def hd(skin=SK, shade=SH, y=52, r=33):
    # thick black outline, round head, soft shade lower right
    return [C(50,y,r+4,K),C(50,y,r,skin),PA(f"M {50+r*0.2} {y+r*0.95} Q {50+r*0.95} {y+r*0.6} {50+r*0.96} {y-r*0.1} Q {50+r*0.7} {y+r*0.55} {50+r*0.2} {y+r*0.95} Z",shade)]
def ey(y=52,dx=13,rx=7,ry=8,col=K,glint="#FFFFFF",tears="#A6DCFF",tl="#5AA7E0",only=None):
    s=[]
    for sx in (-1,1):
        if only and sx!=only: continue
        x=50+sx*dx
        if tears: s+= [PA(f"M {x-3} {y+4} Q {x-4} {y+16} {x} {y+22} Q {x+4} {y+16} {x+3} {y+4} Z",tears),PA(f"M {x-3} {y+4} Q {x-4} {y+16} {x} {y+22} Q {x+4} {y+16} {x+3} {y+4}",tl,stroke=1.2)]
        s+=[E(x,y,rx,ry,col),C(x-rx*0.35,y-ry*0.4,rx*0.3,glint),C(x+rx*0.3,y+ry*0.3,rx*0.13,glint)]
    return s
def mo(y=68,col="#3A1A20",w=3.4,h=2.6): return [E(50,y,w,h,col)]
def add2(i,name,bg,sh,glow=None): add(i,name,bg,sh,glow=glow)

# ---------- characters ----------
add2("isaac_isaac","Isaac","#5B3A2E",hd()+ey()+mo())
sh=[]
for (x,y,r) in [(18,44,11),(22,26,12),(36,14,12),(52,10,12),(68,14,12),(80,28,11),(84,46,10),(16,62,9),(84,62,9)]: sh+=[C(x,y,r+3,K),C(x,y,r,"#F4C430")]
sh+=hd(y=56)+[C(34,30,9,"#F4C430"),C(50,26,9,"#F4C430"),C(66,30,9,"#F4C430"),P([(70,22),(86,14),(86,30)],"#E53935"),P([(70,22),(54,14),(54,30)],"#E53935"),C(70,22,4,"#B71C1C")]+ey(58)+mo(73)
add2("isaac_magdalene","Magdalene","#F8BBD0",sh)
sh=hd()+[E(63,52,9,9,K),L(26,40,78,46,K,2.6)]+ey(only=-1)+mo()
add2("isaac_cain","Cain","#8D6E63",sh)
sh=hd()+[R(35,8,30,20,K,5),R(37,10,26,16,"#C8202A",4),E(50,26,17,3.5,K),L(62,12,72,22,"#FDD835",1.8),C(72,23,2.4,"#FDD835")]+ey()+mo()
add2("isaac_judas","Judas","#4E342E",sh)
sh=hd("#7F9CCB","#5E7CAE")+ey(tears="#C9E6FF",tl="#8FB8E8")+[PA("M 44 70 Q 50 65 56 70","#1A2440",stroke=2.6)]
add2("isaac_bluebaby","??? (Blue Baby)","#1E2A44",sh)
sh=hd(y=54)+[PA("M 14 60 Q 14 16 50 16 Q 86 16 86 60 Q 80 34 60 34 Q 40 34 30 46 Q 24 54 14 60 Z",K),C(74,18,8,K),C(74,18,4,"#9E9E9E")]+ey(56)+mo(71)
add2("isaac_eve","Eve","#B39DDB",sh)
sh=[PA("M 12 92 Q 8 40 30 24 Q 50 12 70 24 Q 92 40 88 92 Z",K),PA("M 15 90 Q 11 42 31 27 Q 50 16 69 27 Q 89 42 85 90 Z","#7A4A2A")]+hd(y=56)+[PA("M 18 46 Q 22 22 50 22 Q 78 22 82 46 Q 66 36 50 38 Q 34 36 18 46 Z","#7A4A2A"),R(16,40,68,7,"#D32F2F",3),R(16,40,68,7,K,3,stroke=1.6)]+ey(58)+mo(73)
add2("isaac_samson","Samson","#FFCC80",sh)
sh=[PA("M 2 44 Q 12 20 32 42 Q 20 52 2 44 Z",K),PA("M 98 44 Q 88 20 68 42 Q 80 52 98 44 Z",K)]+hd("#2A2228","#1A1418")+[P([(32,26),(26,8),(42,22)],K),P([(68,26),(74,8),(58,22)],K)]+ey(col="#D7262E",glint="#FFD0D0",tears=None)+mo(68,"#000000")
add2("isaac_azazel","Azazel","#E57373",sh)
sh=hd(y=54)+[PA("M 18 44 Q 20 18 50 18 Q 82 18 82 42 Q 70 30 56 34 Q 44 28 34 36 Q 26 34 18 44 Z","#E8742A"),PA("M 18 44 Q 20 18 50 18 Q 82 18 82 42",K,stroke=2.4)]+ey(56)+mo(71)
add2("isaac_lazarus","Lazarus","#90A4AE",sh)
sh=hd(y=54)+[PA("M 18 44 Q 20 18 50 18 Q 82 18 82 42 Q 72 32 64 36 Q 56 26 48 34 Q 38 28 30 38 Q 24 36 18 44 Z","#F5F0D0"),PA("M 18 44 Q 20 18 50 18 Q 82 18 82 42",K,stroke=2.4)]+ey(56)+mo(71)
add2("isaac_eden","Eden","#80CBC4",sh)
sh=[PA("M 26 76 Q 30 96 50 92 Q 44 86 52 82 Z","#FFFFFF"),PA("M 26 76 Q 30 96 50 92",K,stroke=2.4)]+[C(50,48,37,K),C(50,48,33,"#FFFFFF")]+ey(48,tears=None)+mo(64,"#2A2A2A")
add2("isaac_lost","The Lost","#263238",sh,glow="#FFFFFF")
sh=[PA("M 12 94 Q 8 40 30 24 Q 50 12 70 24 Q 92 40 88 94 Z","#B71C1C")]+hd("#2E2630","#1E1820",y=56)+[P([(30,28),(22,8),(40,24)],K),P([(70,28),(78,8),(60,24)],K),
    PA("M 18 46 Q 22 22 50 22 Q 78 22 82 46 Q 66 34 50 38 Q 34 34 18 46 Z","#B71C1C"),R(17,50,66,11,K,5)]+mo(72,"#000000")
add2("isaac_lilith","Lilith","#7E57C2",sh)
sh=hd("#9E9E9E","#7E7E7E")+ey(50,13,6.5,7.5,tears=None)+[E(50,70,11,8,K),R(42,64,16,3,"#E0E0E0",1),C(26,32,4,"#FBC02D"),C(26,32,2.5,"#FFE082")]
add2("isaac_keeper","Keeper","#4E342E",sh)
sh=hd("#A8A8B0","#88889A")+[P([(34,24),(30,10),(42,22)],K),P([(66,24),(70,10),(58,22)],K)]+ey(tears=None)+[PA("M 36 66 Q 50 76 64 66 Q 50 70 36 66 Z",K)]
for k in (-2,-1,0,1,2): sh.append(R(48+k*5-1.5,66.5+abs(k)*0.5,3.2,3,"#FFFFFF",0.8))
add2("isaac_apollyon","Apollyon","#311B92",sh)
sh=[L(76,94,88,30,K,8),L(76,94,88,30,"#E8E0D0",5),C(88,28,7,K),C(88,28,5,"#E8E0D0")]+hd("#D8D2C8","#B8B0A4")+ey(tears=None)+[E(50,62,2.4,2,K),PA("M 38 70 Q 50 78 62 70",K,stroke=2.4)]
for k in (-1,0,1): sh.append(L(50+k*5,71,50+k*5,75,K,1.4))
add2("isaac_forgotten","The Forgotten","#5D4037",sh)
sh=[C(16,46,11,K),C(16,46,8.5,"#7A4A2A"),C(84,46,11,K),C(84,46,8.5,"#7A4A2A")]+hd(y=54)+[PA("M 18 44 Q 20 18 50 18 Q 82 18 82 44 Q 66 32 50 34 Q 34 32 18 44 Z","#7A4A2A")]+ey(56)+mo(71)+[E(34,64,4,2.4,"#FF8FA0"),E(66,64,4,2.4,"#FF8FA0")]
add2("isaac_bethany","Bethany","#C5E1A5",sh)
def small_head(cx,skin,hair,hairc):
    s=[C(cx,56,23,K),C(cx,56,20,skin)]
    if hair: s.append(PA(f"M {cx-20} 50 Q {cx-18} 32 {cx} 32 Q {cx+18} 32 {cx+20} 50 Q {cx+8} 42 {cx} 44 Q {cx-8} 42 {cx-20} 50 Z",hairc))
    s+=[E(cx-8,58,4.2,5,K),E(cx+8,58,4.2,5,K),C(cx-9.4,56,1.3,"#FFFFFF"),C(cx+6.6,56,1.3,"#FFFFFF"),E(cx,68,2,1.6,"#3A1A20")]
    return s
add2("isaac_jacobesau","Jacob & Esau","#90CAF9",small_head(30,SK,True,"#7A4A2A")+small_head(70,"#D9534F",True,"#3A1A10"))

# ---------- tainted ----------
def blood(y=52,dx=13): return ey(y,dx,tears="#D32F2F",tl="#8E0000")
add2("isaac_t_isaac","Tainted Isaac","#2A1A1A",hd()+blood()+mo())
sh=[]
for (x,y,r) in [(14,50,11),(18,30,12),(32,16,12),(50,12,12),(68,16,12),(82,30,12),(86,50,11),(12,70,10),(88,70,10),(16,86,9),(84,86,9)]: sh+=[C(x,y,r+3,K),C(x,y,r,"#E8C268")]
sh+=hd(y=56)+[C(34,30,9,"#E8C268"),C(50,26,9,"#E8C268"),C(66,30,9,"#E8C268")]+blood(58)+mo(73)
add2("isaac_t_magdalene","Tainted Magdalene","#3A1A2A",sh)
sh=hd()+[PA("M 20 40 Q 30 20 50 22 Q 40 36 46 52 Q 30 56 20 40 Z","#C62828A0"),E(63,52,9,9,K),L(26,40,78,46,K,2.6)]+ey(only=-1,tears="#D32F2F",tl="#8E0000")+mo()
add2("isaac_t_cain","Tainted Cain","#3E2723",sh)
sh=[C(50,52,37,K),C(50,52,33,"#141014"),R(47,24,6,18,"#D32F2F"),R(41,30,18,6,"#D32F2F")]+[C(38,54,6,"#E53935"),C(62,54,6,"#E53935"),C(36,52,1.8,"#FFD0D0"),C(60,52,1.8,"#FFD0D0")]
add2("isaac_t_judas","Tainted Judas","#5A0A0A",sh,glow="#E53935")
sh=hd("#7F9CCB","#5E7CAE")+ey(tears="#C9E6FF",tl="#8FB8E8")+[E(50,72,10,7,"#6D4C2A"),E(50,70,4,3,"#1A2440"),C(40,76,2.5,"#6D4C2A"),C(60,74,2,"#6D4C2A")]
add2("isaac_t_bluebaby","Tainted ???","#1E2A44",sh)
sh=[PA("M 12 96 Q 8 30 50 14 Q 92 30 88 96 Z",K)]+hd(y=56)+[PA("M 14 70 Q 14 18 50 18 Q 86 18 86 70 Q 82 40 66 36 Q 56 52 40 46 Q 30 44 22 56 Q 18 62 14 70 Z",K)]+ey(58,only=1,tears="#D32F2F",tl="#8E0000")+mo(74)
add2("isaac_t_eve","Tainted Eve","#4A148C",sh)
sh=hd(y=56)
for (x,y) in [(20,38),(30,22),(44,14),(58,14),(70,20),(80,34)]: sh+=[P([(x-10,46),(x,y-6),(x+10,46)],K),P([(x-7,46),(x,y-2),(x+7,46)],"#E8742A")]
sh+=[E(50,38,28,8,"#E8742A")]+blood(60)+[L(28,48,42,53,K,3),L(72,48,58,53,K,3)]+[PA("M 43 76 Q 50 72 57 76",K,stroke=2.6)]
add2("isaac_t_samson","Tainted Samson","#BF360C",sh)
sh=[PA("M 2 44 Q 12 20 32 42 Q 20 52 2 44 Z",K),PA("M 98 44 Q 88 20 68 42 Q 80 52 98 44 Z",K)]+hd("#2A2228","#1A1418",y=56)
for (x,y) in [(22,36),(32,18),(46,10),(60,12),(72,20),(80,34)]: sh.append(P([(x-10,46),(x,y-6),(x+10,46)],K))
sh+=ey(58,col="#D7262E",glint="#FFD0D0",tears=None)+mo(72,"#000000")
add2("isaac_t_azazel","Tainted Azazel","#E57373",sh)
sh=hd(y=54)+[PA("M 18 44 Q 20 18 50 18 Q 82 18 82 42 Q 72 32 62 36 Q 54 26 46 34 Q 36 28 28 38 Q 22 36 18 44 Z","#7A4A2A")]+ey(56)+mo(71)+[PA("M 70 60 Q 76 70 70 80","#9C27B0A0",stroke=3)]
add2("isaac_t_lazarus","Tainted Lazarus","#4A148C",sh)
sh=hd(y=54)+[PA("M 16 48 Q 16 16 50 16 Q 84 16 84 46 Q 78 30 70 36 Q 66 22 56 32 Q 50 20 42 32 Q 34 22 28 36 Q 20 32 16 48 Z","#F5F5F0"),PA("M 16 48 Q 16 16 50 16 Q 84 16 84 46",K,stroke=2.4),
    R(60,62,14,3,"#00E5FF"),R(26,66,10,2.5,"#FF4081")]+ey(56)+mo(71)
add2("isaac_t_eden","Tainted Eden","#263238",sh)
sh=[PA("M 26 76 Q 30 96 50 92 Q 44 86 52 82 Z","#CFD8DC")]+[C(50,48,37,K),C(50,48,33,"#CFD8DC"),PA("M 60 20 L 54 34 L 62 40 L 56 52","#7E8A90",stroke=1.6)]+ey(48,tears=None)+mo(64,"#2A2A2A")
add2("isaac_t_lost","Tainted Lost","#000000",sh,glow="#B0BEC5")
sh=[PA("M 10 96 Q 6 34 30 22 Q 50 10 70 22 Q 94 34 90 96 Z","#C62828")]+hd("#2E2630","#1E1820",y=56)+[P([(30,28),(22,8),(40,24)],K),P([(70,28),(78,8),(60,24)],K),
    PA("M 18 48 Q 22 22 50 22 Q 78 22 82 48 Q 66 36 50 40 Q 34 36 18 48 Z","#C62828")]+ey(58,col="#000000",glint="#FF6E6E",tears=None)+mo(72,"#000000")
add2("isaac_t_lilith","Tainted Lilith","#311B92",sh)
sh=hd("#9E9E9E","#7E7E7E")+[C(37,50,8,"#FBC02D"),C(63,50,8,"#FBC02D"),C(37,50,8,K,stroke=1.6),C(63,50,8,K,stroke=1.6),T(37,50.5,10,"$",K),T(63,50.5,10,"$",K),E(50,70,10,7,K)]
for k in range(5): sh.append(L(30+k*10,30,34+k*10,26,K,1.6))
add2("isaac_t_keeper","Tainted Keeper","#3E2723",sh)
sh=hd("#A8A8B0","#88889A")+[PA("M 36 52 C 36 44.3 42.3 38 50 38 C 57.7 38 64 44.3 64 52","#E53935",stroke=3),PA("M 42 52 C 42 47.6 45.6 44 50 44 C 54.4 44 58 47.6 58 52","#E53935",stroke=2.4)]+ey(tears=None)+mo(70,K)
add2("isaac_t_apollyon","Tainted Apollyon","#1A0A2A",sh)
sh=[E(50,56,38,30,K),E(50,56,34,26,"#D8D2C8")]+[E(37,54,7,7,K),E(63,54,7,7,K),C(35,52,2,"#FFFFFF"),C(61,52,2,"#FFFFFF"),PA("M 36 70 Q 50 78 64 70",K,stroke=2.4)]
add2("isaac_t_forgotten","Tainted Forgotten","#4E342E",sh)
sh=[C(16,46,11,K),C(16,46,8.5,"#7A4A2A"),C(84,46,11,K),C(84,46,8.5,"#7A4A2A")]+hd(y=54)+[PA("M 18 44 Q 20 18 50 18 Q 82 18 82 44 Q 66 32 50 34 Q 34 32 18 44 Z","#7A4A2A"),
    P([(50,30),(55,36),(50,42),(45,36)],"#9C27B0"),P([(50,30),(55,36),(50,42),(45,36)],K,stroke=1.2)]+blood(56)+mo(71)
add2("isaac_t_bethany","Tainted Bethany","#311B92",sh)
sh=hd(y=54)+[PA("M 16 48 Q 16 18 50 16 Q 84 18 84 46 Q 76 32 66 36 Q 58 24 50 32 Q 40 24 32 34 Q 22 32 16 48 Z","#7A4A2A")]+ey(56)+mo(71)
add2("isaac_t_jacob","Tainted Jacob","#263238",sh)

# ---------- items ----------
def it(i,name,bg,sh,glow=None): add("isaac_item_"+i,name,bg,sh,glow=glow)
# Brimstone: the sulfur (brimstone) alchemical symbol in a red glow
def leviathan(col,w):
    return [L(50,18,50,72,col,w),L(38,28,62,28,col,w),L(32,42,68,42,col,w),
            PA("M 50 72 Q 42 60 32 62 Q 20 66 22 76 Q 26 86 38 82 Q 46 78 50 72 Q 54 78 62 82 Q 74 86 78 76 Q 80 66 68 62 Q 58 60 50 72",col,stroke=w)]
it("brimstone","Brimstone","#120000",[C(50,52,40,"#B71C1C"),C(50,52,34,"#E53935")]+leviathan("#FF6E6E",11)+leviathan(K,6)+[E(32,74,4,2.5,"#E53935"),E(68,74,4,2.5,"#E53935")],glow="#FF1744")
it("sacredheart","Sacred Heart","#2A1A2A",[PA("M 50 26 Q 44 14 50 4 Q 52 14 58 10 Q 60 20 50 26 Z","#FF9800"),PA("M 50 24 Q 47 16 50 10 Q 52 18 54 14 Q 55 20 50 24 Z","#FFE082"),
    C(36,44,17,K),C(64,44,17,K),P([(18,50),(82,50),(50,90)],K),C(36,44,14,"#C62828"),C(64,44,14,"#C62828"),P([(22,50),(78,50),(50,84)],"#C62828"),C(36,40,5,"#EF5350"),
    R(47,42,6,22,"#FFE082",1),R(40,48,20,6,"#FFE082",1)])
it("holymantle","Holy Mantle","#0D1B3E",[C(50,50,30,"#4FC3F730")]+[L(50,50,50+40*math.cos(a),50+40*math.sin(a),"#B3E5FC",1.4) for a in [k*math.pi/6 for k in range(12)]]+
   [R(42,18,16,64,K,4),R(26,32,48,16,K,4),R(45,21,10,58,"#29B6F6",2),R(29,35,42,10,"#29B6F6",2),R(47,23,3,54,"#B3E5FC",1)],glow="#4FC3F7")
it("godhead","Godhead","#1A1A2A",[P([(14,24),(86,24),(50,88)],K),P([(20,28),(80,28),(50,80)],"#F5F5F5"),P([(20,28),(32,28),(26,38)],"#29B6F6"),P([(68,28),(80,28),(74,38)],"#FFEB3B"),P([(44,72),(56,72),(50,80)],"#E53935"),
   E(50,46,14,9,K),E(50,46,11,7,"#FFFFFF"),C(50,46,5,"#1565C0"),C(50,46,2.4,K),C(48,44,1.4,"#FFFFFF")],glow="#FFFFFF")
it("momsknife","Mom's Knife","#3E2723",[PA("M 40 74 L 40 22 Q 58 30 60 56 L 60 74 Z",K),PA("M 43 72 L 43 27 Q 56 34 57 56 L 57 72 Z","#E0E0E0"),PA("M 45 66 L 45 32 Q 50 36 51 50","#FFFFFF",stroke=2),
   R(38,72,24,6,K,2),R(42,76,16,18,K,4),R(44,78,12,14,"#6D4C41",3)])
it("mushroom","Magic Mushroom","#2E7D32",[R(38,56,24,30,K,8),R(41,56,18,28,"#FFF3E0",6),PA("M 12 60 Q 12 18 50 16 Q 88 18 88 60 Z",K),PA("M 16 57 Q 16 22 50 20 Q 84 22 84 57 Z","#E53935"),
   C(32,40,6,"#FFFFFF"),C(56,30,7,"#FFFFFF"),C(70,48,5,"#FFFFFF"),C(46,50,4,"#FFFFFF"),E(44,66,2.4,3,K),E(56,66,2.4,3,K)])
it("d6","The D6","#1A1A1A",[R(18,18,64,64,K,14),R(22,22,56,56,"#B71C1C",11),R(22,22,56,10,"#D32F2F",8)]+[C(x,y,5,K) for (x,y) in [(36,36),(64,36),(36,50),(64,50),(36,64),(64,64)]])
it("polyphemus","Polyphemus","#311B1B",[C(50,52,36,K),C(50,52,32,"#FAFAFA")]+[PA(f"M {50+30*math.cos(a):.1f} {52+30*math.sin(a):.1f} Q {50+20*math.cos(a+0.3):.1f} {52+20*math.sin(a+0.3):.1f} {50+16*math.cos(a+0.1):.1f} {52+16*math.sin(a+0.1):.1f}","#E57373",stroke=1.2) for a in [k*0.9 for k in range(7)]]+
   [C(50,52,16,"#1E88E5"),C(50,52,8,K),C(44,46,4,"#FFFFFF")])
it("sadonion","The Sad Onion","#C5E1A5",[PA("M 50 10 Q 44 22 46 30 L 54 30 Q 56 22 50 10 Z","#7CB342"),PA("M 42 6 Q 40 18 46 30","#558B2F",stroke=3),E(50,58,30,28,K),E(50,58,26,24,"#E8F5C8"),
   PA("M 50 34 Q 36 50 38 80","#C5D8A0",stroke=1.5),PA("M 50 34 Q 64 50 62 80","#C5D8A0",stroke=1.5)]+[E(40,58,4,5,K),E(60,58,4,5,K),C(39,56.5,1.3,"#FFFFFF"),C(59,56.5,1.3,"#FFFFFF"),
   PA("M 38 63 Q 37 70 39 74","#5AA7E0",stroke=2),PA("M 62 63 Q 63 70 61 74","#5AA7E0",stroke=2),PA("M 44 72 Q 50 68 56 72",K,stroke=2)])
it("guppy","Guppy's Head","#4E342E",[P([(18,40),(26,14),(40,30)],K),P([(82,40),(74,14),(60,30)],K),E(50,56,34,28,K),E(50,56,30,24,"#9E9E9E"),P([(22,36),(27,20),(36,30)],"#F48FB1"),P([(78,36),(73,20),(64,30)],"#F48FB1"),
   L(34,48,44,58,K,2.6),L(44,48,34,58,K,2.6),L(56,48,66,58,K,2.6),L(66,48,56,58,K,2.6),P([(46,64),(54,64),(50,68)],"#F48FB1"),PA("M 44 72 Q 50 76 56 72",K,stroke=1.8)]+
   [L(50+s*14,66+k*3,50+s*30,62+k*5,K,1) for s in (-1,1) for k in range(2)])
it("deadcat","Dead Cat","#ECEFF1",[P([(18,40),(26,14),(40,30)],K),P([(82,40),(74,14),(60,30)],K),E(50,56,34,28,K),E(50,56,31,25,"#2A2A2A"),
   L(34,48,44,58,"#FAFAFA",2.6),L(44,48,34,58,"#FAFAFA",2.6),L(56,48,66,58,"#FAFAFA",2.6),L(66,48,56,58,"#FAFAFA",2.6),P([(46,64),(54,64),(50,68)],"#F48FB1"),E(66,24,14,4,"#FFD34D",stroke=2.4)])
it("pentagram","Pentagram","#120000",[C(50,52,36,"#B71C1C",stroke=4),P(star(50,52,34,13,5,90),"#B71C1C"),P(star(50,52,28,10,5,90),"#E53935")],glow="#FF1744")
it("technology","Technology","#0D1B2A",[R(16,30,68,42,K,12),R(20,34,60,34,"#78909C",9),C(50,51,13,K),C(50,51,10,"#E53935"),C(46,47,3,"#FFCDD2"),R(4,48,14,6,"#E53935",2),R(82,48,14,6,"#E53935",2)],glow="#E53935")
it("spoonbender","Spoon Bender","#4A148C",[E(34,34,12,15,K),E(34,34,9,12,"#CFD8DC"),E(31,30,3,4,"#FFFFFF"),PA("M 40 46 Q 50 66 70 60 Q 84 56 82 76",K,stroke=8),PA("M 40 46 Q 50 66 70 60 Q 84 56 82 76","#B0BEC5",stroke=4.5)],glow="#CE93D8")
it("polaroid","The Polaroid","#5D4037",[R(18,16,64,68,K,4),R(21,19,58,62,"#FAFAFA",3),R(26,24,48,40,"#90CAF9",2),C(40,46,8,"#F3CEC4"),C(60,46,8,"#F3CEC4"),C(38,45,1.5,K),C(42,45,1.5,K),C(58,45,1.5,K),C(62,45,1.5,K),R(30,54,20,10,"#F8BBD0",3),R(50,54,20,10,"#A5D6A7",3)])
it("ipecac","Ipecac","#33691E",[R(30,22,40,8,K,2),R(32,24,36,4,"#9E9E9E",1),R(26,30,48,58,K,8),R(29,33,42,52,"#7B5A78",6),R(34,50,32,14,"#FAFAFA",2),L(38,55,62,55,K,1.6),L(38,59,56,59,K,1.6),R(32,36,6,20,"#9C7A98",3)])
def fix(o):
    if isinstance(o,dict): return {k:fix(v) for k,v in o.items()}
    if isinstance(o,list): return [fix(v) for v in o]
    if isinstance(o,str) and o.startswith('#') and len(o)==9: return '#'+o[7:9]+o[1:7]
    return o
out=fix(S); json.dump(out,open(sys.argv[1],'w')); print(len(out))
