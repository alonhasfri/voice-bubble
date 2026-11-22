import json, math, sys
exec(open('gen.py').read().split('# ---------- food ----------')[0])
S.clear()
OUT=3  # one outline weight
def head(skin,line,r=33,y=55,x=50): return [C(x,y,r+OUT*0.6,line),C(x,y,r,skin)]
def eyes(y=60,dx=13,rx=5.6,ry=6.6,col="#2B2233",iris=None,hl=True):
    s=[]
    for sx in (-1,1):
        x=50+sx*dx
        s.append(E(x,y,rx,ry,col))
        if iris: s.append(E(x,y+0.5,rx*0.62,ry*0.62,iris)); s.append(C(x,y+0.8,rx*0.3,"#1A141C"))
        if hl: s+= [C(x-rx*0.35,y-ry*0.38,rx*0.38,"#FFFFFF"),C(x+rx*0.35,y+ry*0.35,rx*0.15,"#FFFFFF")]
    return s
def blush(y=69,dx=22,c="#FF8FB0A0"): return [E(50-dx,y,5,3,c),E(50+dx,y,5,3,c)]
def smile(y=72,w=6,c="#5A2A35",open_=False):
    if open_: return [PA(f"M {50-w} {y-1} Q 50 {y+w*1.1} {50+w} {y-1} Z","#7A2E3A"),E(50,y+w*0.45,w*0.5,w*0.28,"#FF8FA0")]
    return [PA(f"M {50-w} {y} Q 50 {y+w*0.8} {50+w} {y}",c,stroke=2.4)]
def body(c,y=90,w=44): return [E(50,y+8,w,16,c)]
fix_=None

# ---------- Deltarune ----------
# Jevil
sh=body("#C6FF00",92,40)+[E(50,92,40,10,"#FDD835")]
sh+=[PA("M 22 46 Q 6 30 10 10 Q 26 24 34 36 Z","#7B1FA2"),PA("M 78 46 Q 94 30 90 10 Q 74 24 66 36 Z","#2A1A3A"),C(10,10,5,"#FDD835"),C(90,10,5,"#FDD835"),
     PA("M 18 52 Q 18 22 50 22 Q 82 22 82 52 Z","#6A1B9A"),PA("M 50 22 Q 82 22 82 52 L 50 52 Z","#2A1A3A")]
sh+=head("#BDBDBD","#6E6E78",29,60)+eyes(62,11,5.2,6,"#1E1824",iris="#FDD835")+blush(70,20)
sh+=[PA("M 41 71 Q 50 80 59 71 Z","#3A1A3A"),P([(44,71.5),(46,74),(48,71.5)],"#FFF59D"),P([(52,71.5),(54,74),(56,71.5)],"#FFF59D"),E(50,76,3,1.6,"#CE93D8")]
add("dr_jevil","Jevil","#3A1858",sh,glow="#CE93D8")
# Spamton
sh=body("#1A1A1A",92,40)+[P([(44,84),(50,90),(56,84),(50,80)],"#E53935")]
sh+=head("#FFFFFF","#9E9E9E",31,56)+[PA("M 19 50 Q 18 20 52 18 Q 82 20 82 46 Q 70 32 50 34 Q 30 34 19 50 Z","#1A1A1A")]
sh+=[R(28,52,18,13,"#FF4FA3",5),R(54,52,18,13,"#FFEB3B",5),R(28,52,18,13,"#1A1A1A",5,stroke=2.4),R(54,52,18,13,"#1A1A1A",5,stroke=2.4),L(46,57,54,57,"#1A1A1A",2.4),
     C(33,56,2,"#FFFFFF"),C(59,56,2,"#FFFFFF")]+blush(69,24,"#FF5A6EA0")+smile(73,7,"#3A2A2A")
add("dr_spamton","Spamton","#FFB3D9",sh)
# Rouxls
sh=body("#1A3FB0",92,40)+[P([(50,14),(84,52),(50,90),(16,52)],"#FFFFFF"),P([(50,19),(79,52),(50,85),(21,52)],"#2347C6"),
    PA("M 34 20 Q 42 8 52 14 Q 60 6 68 18 Q 58 16 50 20 Q 42 16 34 20 Z","#FFFFFF")]
sh+=eyes(52,11,5,4.8,"#FFFFFF",hl=False)+[C(39,53,2.4,"#0A1A5A"),C(61,53,2.4,"#0A1A5A"),C(38.2,52,0.9,"#FFFFFF"),C(60.2,52,0.9,"#FFFFFF"),
    PA("M 34 47 Q 39 45 44 47","#FFFFFF",stroke=2),PA("M 56 47 Q 61 45 66 47","#FFFFFF",stroke=2)]
sh+=[PA("M 40 64 Q 50 74 60 64 Z","#FFFFFF"),E(34,62,4,2.4,"#7FA0FFA0"),E(66,62,4,2.4,"#7FA0FFA0")]
add("dr_rouxls","Rouxls Kaard","#0D1640",sh,glow="#FFFFFF")
# Lancer
sh=[PA("M 50 10 Q 82 40 82 62 Q 80 90 50 90 Q 20 90 18 62 Q 18 40 50 10 Z","#5C6BC0"),PA("M 50 14 Q 78 42 78 62 Q 76 86 50 86 Q 24 86 22 62 Q 22 42 50 14 Z","#F5F5F5"),
    PA("M 50 26 Q 72 44 70 54 Q 68 62 58 58 Q 54 56 52 52 L 54 62 L 46 62 L 48 52 Q 46 56 42 58 Q 32 62 30 54 Q 28 44 50 26 Z","#283593"),
    C(41,50,2.4,"#FFFFFF"),C(59,50,2.4,"#FFFFFF")]+blush(70,20)+[PA("M 38 68 Q 50 82 62 68 Z","#3A2A4A"),E(50,76,4.5,3.5,"#448AFF")]
add("dr_lancer","Lancer","#90CAF9",sh)
# Susie
sh=body("#5B4A8A",94,40)+[PA("M 16 90 Q 10 44 30 26 Q 50 14 70 26 Q 90 44 84 90 Z","#5A1F5E")]
sh+=head("#B393DE","#7A55A8",30,60)+[E(50,72,13,8,"#C9AEEA")]
sh+=[PA("M 22 52 Q 34 26 70 30 Q 80 36 78 50 Q 70 40 62 46 Q 54 36 46 44 Q 36 38 22 52 Z","#6E2A72")]
sh+=eyes(58,11,5,5.8,"#FFE14D",hl=False)+[C(39,59,2.8,"#1E1824"),C(61,59,2.8,"#1E1824"),C(38,57.5,1.1,"#FFFFFF"),C(60,57.5,1.1,"#FFFFFF")]
sh+=[PA("M 42 74 Q 50 81 58 74","#4A2050",stroke=2.4),P([(45,74.6),(47,77.5),(49,75.2)],"#FFF59D"),P([(51,75.2),(53,77.5),(55,74.6)],"#FFF59D")]
for (x,y) in [(31,67),(34,70),(69,67),(66,70)]: sh.append(C(x,y,1,"#8A66C2"))
add("dr_susie","Susie","#2E1F4A",sh)
# Ralsei
sh=body("#43A047",94,40)+[PA("M 20 84 Q 50 96 80 84 L 80 92 Q 50 102 20 92 Z","#F06292")]
sh+=[E(22,62,7,11,"#FFFFFF"),E(78,62,7,11,"#FFFFFF")]+head("#FFFFFF","#C8C8D8",28,62)
sh+=[P([(50,4),(82,46),(18,46)],"#2E7D32"),E(50,46,34,6,"#1B5E20")]
sh+=[C(39,62,7.5,"#A5D6A7"),C(61,62,7.5,"#A5D6A7"),C(39,62,7.5,"#2E7D32",stroke=2),C(61,62,7.5,"#2E7D32",stroke=2),L(46.5,62,53.5,62,"#2E7D32",2),
     C(39,63,3.2,"#E91E63"),C(61,63,3.2,"#E91E63"),C(37.8,61.5,1.2,"#FFFFFF"),C(59.8,61.5,1.2,"#FFFFFF")]+blush(72,21)+smile(76,4.5)
add("dr_ralsei","Ralsei","#C8E6C9",sh)
# Kris
sh=body("#7CB342",94,40)+[R(10,88,80,4,"#FDD835")]
sh+=[PA("M 16 86 Q 12 40 32 26 Q 50 16 68 26 Q 88 40 84 86 Z","#5A3820")]+head("#E8B07A","#A8703F",28,60)
sh+=[PA("M 22 58 Q 24 28 52 26 Q 78 28 78 58 Q 72 48 64 52 Q 60 44 50 50 Q 42 44 36 52 Q 30 46 22 58 Z","#6B4428")]
sh+=[E(40,61,4.5,1.8,"#3A2614"),E(60,61,4.5,1.8,"#3A2614")]+blush(68,18)+smile(74,4,"#6D3B1F")
add("dr_kris","Kris","#90CAF9",sh)
# Queen
sh=body("#FFFFFF",94,42)+[PA("M 12 88 Q 30 74 50 82 Q 70 74 88 88","#FFFFFF")]
sh+=[R(20,24,60,58,"#2A2A3A",16),R(22,26,56,54,"#111118",14),PA("M 26 30 Q 50 6 82 22 Q 72 26 62 24 Q 46 22 26 30 Z","#29B6F6")]
sh+=heart_ if False else []
sh+=[PA("M 30 48 Q 30 38 40 40 Q 46 42 50 48 Q 54 42 60 40 Q 70 38 70 48 Q 70 58 50 66 Q 30 58 30 48 Z","#1A237E"),
     T(50,52,8,"LOL","#FF5252"),PA("M 42 72 Q 50 77 58 72","#29B6F6",stroke=2.6),C(68,64,1.3,"#FFFFFF")]
add("dr_queen","Queen","#4FC3F7",sh)
# Tenna
sh=body("#C62828",94,40)+[P([(46,84),(54,84),(50,96)],"#FDD835")]
sh+=[L(38,24,28,8,"#9575CD",2.5),L(62,24,72,8,"#9575CD",2.5),C(28,8,4,"#D1C4E9"),C(72,8,4,"#D1C4E9"),R(14,24,72,58,"#7E57C2",12),R(16,26,68,54,"#B39DDB",11),R(22,32,56,42,"#FAFAFA",8),
     PA("M 46 44 Q 62 46 64 52 Q 56 54 46 52 Z","#E0E0E0"),PA("M 30 60 Q 50 72 70 60 Z","#3A2A4A"),E(50,65,6,2.5,"#FF8FA0"),E(28,52,4,2.4,"#FF8FB0A0"),E(72,52,4,2.4,"#FF8FB0A0")]
add("dr_tenna","Tenna","#2A1A4A",sh)

# ---------- Undertale ----------
sh=body("#1E88E5",94,44)+[PA("M 14 88 Q 50 74 86 88","#E3F2FD",stroke=5)]+head("#FFFFFF","#A8B4C8",32,54)
sh+=[E(38,54,7,7.5,"#20202A"),E(62,54,7,7.5,"#20202A"),C(38,55,2.4,"#FFFFFF"),C(62,55,2.4,"#FFFFFF"),E(50,63,2,1.6,"#20202A"),
     PA("M 32 68 Q 50 80 68 68","#20202A",stroke=2.6)]
for k in (-2,-1,0,1,2): sh.append(L(50+k*5,69.5+abs(k)*0.6,50+k*5,73-abs(k)*0.6,"#20202A",1.4))
add("ut_sans","Sans","#4FC3F7",sh)
sh=body("#E53935",94,42)+[R(18,24,64,56,"#B0B8C8",18),R(20,26,60,52,"#FFFFFF",16)]
sh+=[E(38,48,6.5,7,"#20202A"),E(62,48,6.5,7,"#20202A"),C(38,49,2.2,"#FFFFFF"),C(62,49,2.2,"#FFFFFF"),PA("M 31 39 L 43 41","#20202A",stroke=2.4),PA("M 69 39 L 57 41","#20202A",stroke=2.4),
     E(50,57,2,1.6,"#20202A"),PA("M 34 64 Q 50 74 66 64","#20202A",stroke=2.6),PA("M 12 86 Q 50 76 88 86 L 88 96 L 12 96 Z","#E53935"),P([(72,84),(92,72),(86,94)],"#E53935")]
add("ut_papyrus","Papyrus","#FFAB91",sh)
sh=body("#7B1FA2",94,44)+[E(18,62,8,18,"#FFFFFF"),E(82,62,8,18,"#FFFFFF"),E(18,62,8,18,"#D0C8DA",stroke=2)]+head("#FFFFFF","#D0C8DA",31,56)
sh+=[P([(38,28),(41,18),(44,28)],"#FFF8E1"),P([(56,28),(59,18),(62,28)],"#FFF8E1")]+eyes(58,12,5,5.6,"#2B2233",iris="#B03A3A")+blush(68,20)+smile(73,4.5)
sh+=[C(50,90,5,"#FDD835"),P([(44,93),(56,93),(50,85)],"#FDD835")]
add("ut_toriel","Toriel","#E1BEE7",sh)
sh=[PA("M 50 72 Q 46 88 50 100","#7CB342",stroke=6),PA("M 50 88 Q 32 82 26 92 Q 42 96 50 90","#8BC34A")]
for k in range(6):
    a=math.radians(-90+k*60); sh.append(E(50+22*math.cos(a),48+22*math.sin(a),12,9,"#FFD600"))
sh+=[C(50,48,17,"#E8E0C0"),C(50,48,16,"#FFFFFF")]+[E(44,46,2.6,3.6,"#2B2233"),E(56,46,2.6,3.6,"#2B2233"),C(43,44.6,1.1,"#FFFFFF"),C(55,44.6,1.1,"#FFFFFF"),
    E(39,53,3,1.8,"#FF8FB0A0"),E(61,53,3,1.8,"#FF8FB0A0"),PA("M 45 53 Q 50 58 55 53","#5A2A35",stroke=2)]
add("ut_flowey","Flowey","#A5D6A7",sh)
sh=body("#1A1A1A",94,40)+[PA("M 66 22 Q 96 26 90 72 Q 84 44 66 34 Z","#E53935"),P([(18,52),(6,36),(26,44)],"#E53935"),P([(82,52),(94,36),(74,44)],"#E53935")]
sh+=head("#5EC8F0","#2E8AB8",30,58)+[R(30,52,16,12,"#1A1A1A",4),L(28,51,74,42,"#1A1A1A",2.2)]
sh+=[E(61,58,5.4,6,"#FFE14D"),C(61,59,2.8,"#1E1824"),C(59.5,57,1.1,"#FFFFFF")]+blush(68,20)+[PA("M 42 72 Q 50 79 58 72","#1E3A50",stroke=2.4),P([(53,73.5),(55,77),(57,73)],"#FFF59D")]
add("ut_undyne","Undyne","#1A237E",sh)
sh=[PA("M 22 92 Q 18 52 26 32 Q 50 12 74 32 Q 82 52 78 92 Q 70 84 62 92 Q 54 84 46 92 Q 38 84 30 92 Z","#D8DCE8"),
    PA("M 25 89 Q 21 52 29 34 Q 50 16 71 34 Q 79 52 75 89 Q 68 82 62 89 Q 54 82 46 89 Q 38 82 32 89 Z","#FFFFFF"),
    PA("M 34 56 Q 40 52 46 56","#2B2233",stroke=2.6),PA("M 54 56 Q 60 52 66 56","#2B2233",stroke=2.6)]+blush(64,16)+[PA("M 45 70 Q 50 67 55 70","#5A2A35",stroke=2)]
sh+=tear_ if False else []
sh+=[PA("M 66 60 Q 70 66 66 70 Q 62 66 66 60 Z","#81D4FA"),PA("M 20 46 Q 20 16 50 16 Q 80 16 80 46","#1A1A1A",stroke=4),R(14,40,12,18,"#1A1A1A",4),R(74,40,12,18,"#1A1A1A",4)]
add("ut_napstablook","Napstablook","#4A6572",sh)
sh=body("#42A5F5",94,40)+[PA("M 16 86 Q 14 56 30 50 L 70 50 Q 86 56 84 86 Z","#2A2A2A"),P([(30,32),(36,12),(44,30)],"#FFFFFF"),P([(56,30),(64,12),(70,32)],"#FFFFFF"),
    E(18,56,8,14,"#FFFFFF")]+[E(82,56,8,14,"#FFFFFF")]+head("#FFFFFF","#D0D0D8",26,54)
sh+=[C(42,52,2.6,"#2B2233"),C(58,52,2.6,"#2B2233"),C(41.2,51,1,"#FFFFFF"),C(57.2,51,1,"#FFFFFF"),E(50,58,2,1.4,"#2B2233"),PA("M 44 61 Q 47 65 50 61 Q 53 65 56 61","#2B2233",stroke=1.6),
    E(36,59,4,2.4,"#FF8FB0"),E(64,59,4,2.4,"#FF8FB0")]
add("ut_temmie","Temmie","#FFF59D",sh)
# ---------- JJK ----------
def jjk_face(skin="#F7D9C4",line="#D3A68C"): return head(skin,line,27,60)
sh=body("#1A1A2E",94,40)+jjk_face()
for (x,y) in [(18,40),(24,22),(34,12),(46,6),(58,8),(70,14),(80,26),(84,42)]: sh.append(P([(x-9,52),(x,y),(x+9,52)],"#FAFAFA"))
sh+=[E(50,46,30,10,"#FAFAFA"),R(22,54,56,11,"#1A1A1A",5)]+blush(70,18)+smile(75,4.5)
add("jjk_gojo","Gojo","#4FC3F7",sh,glow="#B3E5FC")
sh=body("#1A1A1A",94,40)+jjk_face("#F3D2BC","#C99A84")
for (x,y) in [(22,30),(34,16),(48,10),(62,14),(76,26)]: sh.append(P([(x-10,46),(x,y),(x+10,46)],"#F06292"))
sh+=[E(50,40,26,9,"#F06292")]+eyes(60,11,4.6,5,"#2B1A1E",iris="#E53935")
sh+=[PA("M 34 68 Q 39 70 44 68","#2B1A1E",stroke=1.6),PA("M 56 68 Q 61 70 66 68","#2B1A1E",stroke=1.6),L(30,63,36,64,"#2B1A1E",1.8),L(70,63,64,64,"#2B1A1E",1.8),
     PA("M 44 76 Q 50 79 58 75","#5A2A35",stroke=2.2)]
add("jjk_sukuna","Sukuna","#B71C1C",sh,glow="#FF5252")
sh=body("#1A1A2E",94,40)+[E(50,86,14,6,"#E53935")]+jjk_face()+[E(50,40,26,9,"#4A3028")]
for (x,y) in [(24,28),(36,16),(50,12),(64,16),(76,28)]: sh.append(P([(x-9,42),(x,y),(x+9,42)],"#F48FB1"))
sh+=eyes(61,11,5,5.6,"#3A2A24",iris="#A87452")+blush(71,18)+smile(76,5,open_=True)
add("jjk_yuji","Yuji","#FFE0B2",sh)
sh=body("#1A1A2E",94,40)
for k in range(13):
    a=math.radians(180+k*15); sh.append(P([(50+20*math.cos(a-0.25),46+18*math.sin(a-0.25)),(50+40*math.cos(a),46+36*math.sin(a)),(50+20*math.cos(a+0.25),46+18*math.sin(a+0.25))],"#1A1A22"))
sh+=jjk_face()+[E(50,40,26,10,"#1A1A22")]+eyes(61,11,5,5.6,"#1A1A22",iris="#3A5A9A")+blush(71,18)+[PA("M 46 76 L 54 76","#5A2A35",stroke=2.2)]
add("jjk_megumi","Megumi","#B0BEC5",sh)
# ---------- Isaac ----------
def isaac_head(skin="#F2D3C2",line="#5A3A3A"): return head(skin,line,33,54)
def big_eyes(y=54): return eyes(y,14,7,8,"#2B2233")
sh=isaac_head()+big_eyes()+blush(66,22)+[PA("M 44 72 Q 50 69 56 72","#5A3A3A",stroke=2.4),PA("M 36 62 Q 34 70 37 76 Q 40 70 36 62 Z","#8FD3FF"),PA("M 64 62 Q 66 70 63 76 Q 60 70 64 62 Z","#8FD3FF")]
add("isaac_isaac","Isaac","#7A5A48",sh)
sh=[E(50,14,15,4,"#FFD34D",stroke=3)]+head("#FFFFFF","#B8C4D8",33,56)+eyes(56,14,7,8,"#3A4050")+blush(68,22,"#C0D8FFA0")+[PA("M 45 73 Q 50 71 55 73","#3A4050",stroke=2.2)]
add("isaac_lost","The Lost","#263238",sh,glow="#FFFFFF")
sh=[PA("M 4 44 Q 14 22 34 44 Q 22 52 4 44 Z","#2A2230"),PA("M 96 44 Q 86 22 66 44 Q 78 52 96 44 Z","#2A2230")]+head("#3A3440","#1A161E",31,56)
sh+=[P([(32,32),(28,14),(42,28)],"#1A161E"),P([(68,32),(72,14),(58,28)],"#1A161E")]+eyes(57,13,6,7,"#120E14",iris="#E53935")+blush(68,20,"#FF6E8A80")+[PA("M 44 73 Q 50 76 56 73","#120E14",stroke=2.2)]
add("isaac_azazel","Azazel","#FF8A80",sh)
sh=isaac_head()+[PA("M 22 36 Q 30 22 50 22 Q 70 22 78 36","#E8A04A",stroke=6),R(34,8,32,20,"#C8202A",5),E(50,28,18,4,"#8E1018"),L(62,10,72,24,"#FDD835",1.8),C(72,24,2.2,"#FDD835")]
sh+=eyes(56,14,6.5,7.5,"#2B2233")+blush(67,22)+smile(73,4.5)
add("isaac_judas","Judas","#6D4C41",sh)
sh=[E(50,58,42,34,"#C97878"),E(50,58,40,32,"#E9A0A0")]+[E(36,40,4.5,5,"#2B2233"),E(64,40,4.5,5,"#2B2233"),C(35,38.6,1.6,"#FFFFFF"),C(63,38.6,1.6,"#FFFFFF")]+blush(50,26)
sh+=[PA("M 24 58 Q 50 86 76 58 Q 50 66 24 58 Z","#7A2E3A")]
for k in range(6):
    if k==3: continue
    sh.append(R(28+k*8.2,58.5+abs(k-2.5)*0.4,6.5,6,"#FFF8E1",2))
sh.append(E(50,74,9,4,"#FF8FA0"))
add("isaac_monstro","Monstro","#4E2A2A",sh)
# keep these two from before
old={e['id']:e for e in json.load(open('chars.json'))}
def fix(o):
    if isinstance(o,dict): return {k:fix(v) for k,v in o.items()}
    if isinstance(o,list): return [fix(v) for v in o]
    if isinstance(o,str) and o.startswith('#') and len(o)==9: return '#'+o[7:9]+o[1:7]
    return o
out=fix(S)+[old['art_isaac_tears'],old['art_isaac_brimstone']]
json.dump(out,open(sys.argv[1],'w')); print(len(out))
