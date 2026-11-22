import json, math, sys
exec(open('gen.py').read().split('# ---------- food ----------')[0])
S.clear()
def eyes_dot(y,dx,r,c="#1A1A1A"): return [C(50-dx,y,r,c),C(50+dx,y,r,c)]
# ---------- Deltarune ----------
# Jevil: purple/black two-horned jester hood with yellow bells, grey round face, black lines across eyes, black eyes with yellow pupils, sharp yellow grin, purple tongue, green/yellow ruff
sh=[P([(20,48),(6,14),(34,34)],"#6A1B9A"),P([(80,48),(94,14),(66,34)],"#1A1A1A"),C(8,14,5,"#FDD835"),C(92,14,5,"#FDD835"),
    PA("M 18 52 Q 18 26 50 24 Q 82 26 82 52 Z","#4A148C"),PA("M 50 24 Q 66 26 82 52 L 50 52 Z","#1A1A1A"),
    C(50,60,26,"#9E9E9E")]
for x in (40,60): sh+=[L(x,44,x,68,"#1A1A1A",2)]
sh+=[E(40,56,6,5,"#1A1A1A"),E(60,56,6,5,"#1A1A1A"),C(40,56,2,"#FDD835"),C(60,56,2,"#FDD835"),
     PA("M 30 66 Q 50 84 70 66 Q 50 74 30 66 Z","#1A1A1A")]
for k in range(7): sh.append(P([(33+k*5,67.5+abs(k-3)*-0.3),(35.5+k*5,72),(38+k*5,67.5)],"#FDD835"))
sh+=[E(50,76,5,3,"#AB47BC")]
for k in range(8):
    a=math.radians(200+k*20); sh.append(E(50+30*math.cos(a)*1.05,90+8*math.sin(a),6,4,"#C6FF00" if k%2 else "#FDD835"))
add("dr_jevil","Jevil","#2A1040",sh,glow="#AB47BC")
# Spamton: white big head, black slicked pompadour, pink/yellow glasses, wide grin
sh=[C(50,56,34,"#FAFAFA"),PA("M 18 46 Q 20 14 54 14 Q 84 16 82 40 Q 70 26 52 30 Q 30 32 18 46 Z","#111111"),PA("M 50 14 Q 66 4 76 16","#111111"),
    R(26,46,20,12,"#FF4FA3",3),R(54,46,20,12,"#FFEB3B",3),L(46,50,54,50,"#111111",2),R(26,46,20,12,"#111111",3,stroke=1.5),R(54,46,20,12,"#111111",3,stroke=1.5),
    PA("M 28 68 Q 50 86 72 68 Q 50 76 28 68 Z","#111111")]
for k in range(8): sh.append(R(31+k*5,69,4,4,"#FAFAFA",1))
add("dr_spamton","Spamton","#FF4FA3",sh,glow="#FFEB3B")
# Rouxls Kaard: diamond royal-blue head, white outline/hair curl, drooping eyes, grin with dark streak
sh=[P([(50,8),(88,50),(50,92),(12,50)],"#FFFFFF"),P([(50,13),(83,50),(50,87),(17,50)],"#1A3FB0"),PA("M 30 20 Q 50 4 70 20 Q 60 14 50 18 Q 40 14 30 20 Z","#FFFFFF"),
    PA("M 34 44 Q 40 40 46 46","#FFFFFF",stroke=3),PA("M 54 46 Q 60 40 66 44","#FFFFFF",stroke=3),C(40,48,2.2,"#FFFFFF"),C(60,48,2.2,"#FFFFFF"),
    PA("M 32 60 Q 50 76 68 60 Q 50 66 32 60 Z","#FFFFFF"),PA("M 36 62 Q 34 74 36 82","#0A1A5A",stroke=3),PA("M 44 52 Q 50 56 56 52","#FFFFFF",stroke=1.5)]
add("dr_rouxls","Rouxls Kaard","#0A0F30",sh,glow="#FFFFFF")
# Lancer: teardrop head, spade shadow over upper face, big mouth with blue tongue
sh=[PA("M 50 8 Q 84 40 82 62 Q 80 88 50 90 Q 20 88 18 62 Q 16 40 50 8 Z","#EDEDED"),PA("M 50 24 Q 74 44 72 56 Q 70 64 60 60 Q 56 58 54 54 L 56 66 L 44 66 L 46 54 Q 44 58 40 60 Q 30 64 28 56 Q 26 44 50 24 Z","#1A237E"),
    PA("M 30 66 Q 50 88 70 66 Z","#1A1A1A"),PA("M 40 72 Q 50 92 58 74 Z","#2962FF"),L(34,67,66,67,"#FFFFFF",2)]
add("dr_lancer","Lancer","#2962FF",sh)
# Susie: lavender face, aubergine messy hair, yellow eyes black pupils, snout, sharp yellow teeth, freckles
sh=[PA("M 14 90 Q 8 40 30 22 Q 50 10 72 22 Q 92 40 86 90 Z","#4A1A4A"),E(50,60,26,28,"#B38BD8"),E(50,72,16,10,"#C9A6E6"),
    PA("M 22 46 Q 40 22 74 30 Q 62 34 66 46 Q 54 36 44 44 Q 36 36 22 46 Z","#5B1F5B"),
    E(40,54,5,3.5,"#FFEB3B"),E(60,54,5,3.5,"#FFEB3B"),C(41,54,1.8,"#1A1A1A"),C(61,54,1.8,"#1A1A1A"),C(44,72,1.2,"#6A3A8A"),C(56,72,1.2,"#6A3A8A"),
    PA("M 36 78 Q 50 86 64 78","#1A1A1A",stroke=2)]
for k in range(4): sh.append(P([(40+k*6,78.6),(43+k*6,84),(46+k*6,79.6)],"#FFEB3B"))
for (x,y) in [(32,64),(35,67),(68,64),(65,67)]: sh.append(C(x,y,0.9,"#7E57C2"))
add("dr_susie","Susie","#311B4D",sh)
# Ralsei: white fluffy face, pointed green hat, round green glasses, pink scarf
sh=[P([(50,2),(84,44),(16,44)],"#2E7D32"),E(50,44,36,7,"#1B5E20"),C(50,58,26,"#FAFAFA"),E(26,58,6,10,"#FAFAFA"),E(74,58,6,10,"#FAFAFA"),
    C(40,58,7,"#66BB6A"),C(60,58,7,"#66BB6A"),C(40,58,7,"#1B5E20",stroke=1.5),C(60,58,7,"#1B5E20",stroke=1.5),L(47,58,53,58,"#1B5E20",1.5),
    C(40,58,2.4,"#1A1A1A"),C(60,58,2.4,"#1A1A1A"),PA("M 44 70 Q 50 74 56 70","#1A1A1A",stroke=1.6),P([(46,71),(47,74),(48,71)],"#FFFFFF"),P([(52,71),(53,74),(54,71)],"#FFFFFF"),
    PA("M 20 80 Q 50 92 80 80 L 82 92 Q 50 100 18 92 Z","#F06292"),E(72,92,6,8,"#EC407A")]
add("dr_ralsei","Ralsei","#A5D6A7",sh)
# Kris: amber skin, brown shoulder-length hair with bangs shading eyes, one red glow
sh=[PA("M 14 92 Q 10 36 30 20 Q 50 8 70 20 Q 90 36 86 92 Z","#4E2C1A"),E(50,62,24,26,"#D99A5B"),
    PA("M 24 58 Q 26 24 52 22 Q 78 24 76 58 Q 70 44 62 50 Q 58 40 50 48 Q 42 40 34 50 Q 30 44 24 58 Z","#5D3A1F"),
    E(50,56,22,5,"#00000040"),C(42,58,1.6,"#2A1A10"),C(58,58,2.2,"#FF1744"),PA("M 44 74 L 56 74","#6D3B1F",stroke=1.5),R(28,86,44,14,"#1565C0",4),R(28,90,44,3,"#FDD835")]
add("dr_kris","Kris","#90CAF9",sh)
# Queen: black face, azure crown & glasses, white collar, azure lips, beauty spot
sh=[PA("M 8 92 Q 30 72 50 80 Q 70 72 92 92 L 92 100 L 8 100 Z","#FFFFFF"),E(50,52,24,30,"#111111"),P([(34,26),(38,10),(44,20),(50,6),(56,20),(62,10),(66,26)],"#29B6F6"),
    R(32,44,14,9,"#B3E5FC",3),R(54,44,14,9,"#B3E5FC",3),L(46,48,54,48,"#29B6F6",2),R(32,44,14,9,"#29B6F6",3,stroke=1.5),R(54,44,14,9,"#29B6F6",3,stroke=1.5),
    PA("M 42 68 Q 50 74 58 68 Q 50 70 42 68 Z","#29B6F6"),C(72,56,1.4,"#FFFFFF")]
add("dr_queen","Queen","#0D47A1",sh)
# Tenna: lilac TV head, two antennas, white screen face with long nose and wide grin
sh=[L(36,24,24,4,"#B39DDB",2.5),L(64,24,76,4,"#B39DDB",2.5),C(24,4,3.5,"#D1C4E9"),C(76,4,3.5,"#D1C4E9"),R(12,24,76,60,"#B39DDB",10),R(18,30,64,46,"#FAFAFA",8),
    P([(50,40),(70,52),(50,54)],"#E0E0E0"),PA("M 28 62 Q 50 76 72 62 Q 50 68 28 62 Z","#1A1A1A"),R(30,84,10,8,"#7E57C2",2),R(60,84,10,8,"#7E57C2",2)]
add("dr_tenna","Tenna","#2A1A4A",sh)
# ---------- Undertale ----------
sh=[PA("M 8 100 Q 12 74 50 72 Q 88 74 92 100 Z","#1E88E5"),PA("M 16 86 Q 50 70 84 86","#FFFFFF",stroke=5),C(50,46,30,"#FAFAFA"),
    C(38,44,7,"#111111"),C(62,44,7,"#111111"),C(38,44,2,"#FFFFFF"),C(62,44,2,"#FFFFFF"),P([(48,52),(52,52),(50,56)],"#111111"),
    PA("M 30 60 Q 50 74 70 60 Q 50 66 30 60 Z","#111111")]
for k in range(-3,4): sh.append(L(50+k*5,61+abs(k)*-0.6,50+k*5,66-abs(k)*0.8,"#FAFAFA",1.2))
add("ut_sans","Sans","#0D1A2A",sh,glow="#29B6F6")
sh=[P([(20,26),(80,26),(84,62),(70,76),(30,76),(16,62)],"#FAFAFA"),E(37,44,6,9,"#111111"),E(63,44,6,9,"#111111"),L(30,34,44,38,"#111111",2.5),L(70,34,56,38,"#111111",2.5),
    P([(48,52),(52,52),(50,58)],"#111111"),R(28,62,44,8,"#111111",2)]
for k in range(1,9): sh.append(L(28+k*4.9,62,28+k*4.9,70,"#FAFAFA",1.2))
sh+=[PA("M 10 86 Q 50 74 90 86 L 92 100 L 8 100 Z","#E53935"),P([(70,82),(92,70),(84,92)],"#E53935")]
add("ut_papyrus","Papyrus","#FF8A80",sh)
sh=[E(20,58,9,22,"#FAFAFA"),E(80,58,9,22,"#FAFAFA"),C(50,50,28,"#FAFAFA"),P([(40,24),(42,16),(45,24)],"#FFF8E1"),P([(55,24),(58,16),(60,24)],"#FFF8E1"),
    E(40,48,3,4,"#8D1E1E"),E(60,48,3,4,"#8D1E1E"),PA("M 34 42 L 30 40","#111",stroke=1.2),PA("M 66 42 L 70 40","#111",stroke=1.2),E(50,58,4,3,"#F48FB1"),
    PA("M 44 64 Q 50 68 56 64","#111",stroke=1.5),PA("M 12 100 Q 14 78 50 78 Q 86 78 88 100 Z","#7B1FA2"),C(50,90,5,"#FDD835"),P([(44,92),(56,92),(50,84)],"#FDD835")]
add("ut_toriel","Toriel","#CE93D8",sh)
sh=[PA("M 50 70 Q 46 86 50 100","#7CB342",stroke=5),PA("M 50 84 Q 34 80 28 90 Q 42 94 50 88","#8BC34A")]
for k in range(6):
    a=math.radians(-90+k*60); sh.append(E(50+20*math.cos(a),46+20*math.sin(a),11,8,"#FFD600"))
sh+=[C(50,46,15,"#FAFAFA"),E(45,43,2,3,"#111"),E(55,43,2,3,"#111"),PA("M 43 50 Q 50 56 57 50","#111",stroke=1.8)]
add("ut_flowey","Flowey","#1B1B1B",sh,glow="#FFD600")
sh=[PA("M 70 20 Q 96 30 84 70 Q 80 40 64 30 Z","#E53935"),P([(16,46),(6,30),(26,40)],"#E53935"),P([(84,46),(94,30),(74,40)],"#E53935"),C(50,52,30,"#4FC3F7"),
    R(30,40,16,12,"#1A1A1A",3),L(28,40,72,32,"#1A1A1A",2),E(62,46,6,4,"#FFEB3B"),E(62,46,1.6,3.5,"#1A1A1A"),PA("M 56 40 Q 62 36 70 40","#E53935",stroke=2),
    PA("M 34 64 Q 50 74 66 64","#1A1A1A",stroke=2),P([(40,64),(44,72),(46,65)],"#FFEB3B"),P([(54,65),(56,72),(60,64)],"#FFEB3B")]
add("ut_undyne","Undyne","#1A237E",sh)
sh=[PA("M 22 90 Q 18 50 26 30 Q 50 10 74 30 Q 82 50 78 90 Q 70 82 62 90 Q 54 82 46 90 Q 38 82 30 90 Z","#FAFAFA"),
    E(40,46,7,5,"#111"),E(60,46,7,5,"#111"),C(40,47,1.4,"#FAFAFA"),C(60,47,1.4,"#FAFAFA"),L(50,40,50,52,"#111",1),PA("M 44 62 Q 50 58 56 62","#111",stroke=1.5),
    PA("M 20 40 Q 20 14 50 14 Q 80 14 80 40","#111",stroke=3),R(14,34,10,16,"#111",3),R(76,34,10,16,"#111",3)]
add("ut_napstablook","Napstablook","#455A64",sh)
sh=[R(18,74,64,26,"#42A5F5",6),PA("M 16 82 Q 14 56 30 50 L 70 50 Q 86 56 84 82 Z","#212121"),P([(28,30),(34,10),(42,28)],"#FAFAFA"),P([(58,28),(66,10),(72,30)],"#FAFAFA"),
    E(18,52,8,14,"#FAFAFA"),E(82,52,8,14,"#FAFAFA"),C(50,50,24,"#FAFAFA"),C(42,48,3,"#111"),C(58,48,3,"#111"),E(50,58,2.5,1.8,"#111"),
    PA("M 44 62 Q 47 66 50 62 Q 53 66 56 62","#111",stroke=1.4),C(38,56,3,"#F48FB1A0"),C(62,56,3,"#F48FB1A0")]
add("ut_temmie","Temmie","#FFF9C4",sh)
# ---------- JJK ----------
sh=[PA("M 8 92 Q 14 70 50 70 Q 86 70 92 92 L 92 100 L 8 100 Z","#1A1A2E"),E(50,56,24,28,"#F5D6C4")]
for (x,y) in [(16,30),(24,12),(36,22),(44,2),(56,20),(66,4),(76,22),(86,14),(90,34)]: sh.append(P([(x-8,44),(x,y),(x+8,44)],"#FAFAFA"))
sh+=[R(22,44,56,12,"#111111",4),PA("M 40 72 Q 50 78 60 72","#8D5A4A",stroke=2),L(50,60,49,66,"#C89A88",1.2)]
add("jjk_gojo","Gojo","#4FC3F7",sh,glow="#4FC3F7")
sh=[PA("M 8 92 Q 14 70 50 70 Q 86 70 92 92 L 92 100 L 8 100 Z","#212121"),E(50,56,24,28,"#F2CDB8")]
for (x,y) in [(22,22),(34,12),(48,8),(62,12),(76,22)]: sh.append(P([(x-9,38),(x,y),(x+9,38)],"#F06292"))
sh+=[E(50,34,24,8,"#F06292"),E(40,52,5,3,"#FFFFFF"),E(60,52,5,3,"#FFFFFF"),C(40,52,2.2,"#C62828"),C(60,52,2.2,"#C62828"),
    L(32,58,46,58,"#111",1.6),L(54,58,68,58,"#111",1.6),L(34,62,44,61,"#111",1.2),L(56,61,66,62,"#111",1.2),
    PA("M 40 72 Q 50 76 62 70","#5D2A2A",stroke=2),L(26,46,34,48,"#111",1.6),L(74,46,66,48,"#111",1.6)]
add("jjk_sukuna","Sukuna","#7F0000",sh,glow="#FF1744")
sh=[PA("M 8 92 Q 14 70 50 70 Q 86 70 92 92 L 92 100 L 8 100 Z","#0D1B4A"),E(50,56,24,28,"#F5D3BC"),E(50,34,26,9,"#3E2723")]
for (x,y) in [(20,26),(30,14),(42,8),(56,8),(68,14),(80,26)]: sh.append(P([(x-8,36),(x,y),(x+8,36)],"#F48FB1"))
sh+=[E(40,52,4.5,4,"#FFFFFF"),E(60,52,4.5,4,"#FFFFFF"),C(40,52.5,2.6,"#8D6E63"),C(60,52.5,2.6,"#8D6E63"),L(34,45,46,46,"#3E2723",2),L(54,46,66,45,"#3E2723",2),
    PA("M 42 70 Q 50 77 58 70","#8D4A3A",stroke=2),R(20,90,60,10,"#FFD54F",2)]
add("jjk_yuji","Yuji","#FFCC80",sh)
sh=[PA("M 8 92 Q 14 70 50 70 Q 86 70 92 92 L 92 100 L 8 100 Z","#0D1B4A")]
for k in range(13):
    a=math.radians(180+k*15); sh.append(P([(50+18*math.cos(a-0.25),40+16*math.sin(a-0.25)),(50+40*math.cos(a),40+34*math.sin(a)),(50+18*math.cos(a+0.25),40+16*math.sin(a+0.25))],"#111111"))
sh+=[E(50,58,22,26,"#F3DCCB"),E(50,38,24,10,"#111111"),E(41,54,4.5,3,"#FFFFFF"),E(59,54,4.5,3,"#FFFFFF"),C(41,54,2,"#1E3A5F"),C(59,54,2,"#1E3A5F"),
    L(34,48,46,50,"#111",2),L(66,48,54,50,"#111",2),L(44,72,56,72,"#8D5A4A",1.8)]
add("jjk_megumi","Megumi","#B0BEC5",sh)
# ---------- The Binding of Isaac ----------
def isaac_base(skin="#F6D2C4",out="#C99A8A"):
    return [C(50,52,36,out),C(50,52,34,skin)]
sh=isaac_base()+[E(36,48,9,10,"#111"),E(64,48,9,10,"#111"),C(33,45,2.5,"#FFFFFF"),C(61,45,2.5,"#FFFFFF"),PA("M 40 72 Q 50 66 60 72","#111",stroke=2.5),
    PA("M 34 58 Q 30 74 34 88","#4FC3F7",stroke=4),PA("M 66 58 Q 70 74 66 88","#4FC3F7",stroke=4),C(34,88,3,"#4FC3F7"),C(66,88,3,"#4FC3F7"),C(30,58,2,"#E57373A0"),C(70,58,2,"#E57373A0")]
add("isaac_isaac","Isaac","#3E2723",sh)
sh=[C(50,52,36,"#B0BEC5"),C(50,52,34,"#ECEFF1"),E(36,48,9,10,"#263238"),E(64,48,9,10,"#263238"),PA("M 42 70 Q 50 66 58 70","#263238",stroke=2),
    PA("M 14 30 Q 4 50 14 74","#FFFFFF80",stroke=3),PA("M 86 30 Q 96 50 86 74","#FFFFFF80",stroke=3),E(50,14,16,4,"#FFF59D",stroke=2.5)]
add("isaac_lost","The Lost","#1A1A1A",sh,glow="#FFFFFF")
sh=[PA("M 2 40 Q 12 20 30 40 Q 20 50 2 40 Z","#212121"),PA("M 98 40 Q 88 20 70 40 Q 80 50 98 40 Z","#212121"),C(50,54,34,"#424242"),C(50,54,32,"#5A5A5A"),
    P([(30,28),(26,10),(40,24)],"#212121"),P([(70,28),(74,10),(60,24)],"#212121"),E(37,50,8,9,"#111"),E(63,50,8,9,"#111"),C(35,47,1.8,"#FF5252"),C(61,47,1.8,"#FF5252"),
    PA("M 38 70 Q 50 78 62 70","#111",stroke=2.5)]
add("isaac_azazel","Azazel","#B71C1C",sh,glow="#FF1744")
sh=isaac_base()+[R(34,8,32,18,"#C62828",4),E(50,26,17,4,"#8E0000"),L(62,10,70,22,"#FDD835",1.5),C(70,22,2,"#FDD835"),
    E(36,48,8,8,"#111"),E(64,48,8,8,"#111"),C(34,46,2,"#FFFFFF"),C(62,46,2,"#FFFFFF"),PA("M 34 66 Q 42 60 50 64 Q 58 60 66 66 Q 58 64 50 68 Q 42 64 34 66 Z","#4E342E"),PA("M 42 76 Q 50 72 58 76","#111",stroke=2)]
add("isaac_judas","Judas","#4E342E",sh)
sh=[E(50,56,42,36,"#E9A0A0"),E(50,56,42,36,"#B56565",stroke=2),C(36,34,5,"#C62828"),C(64,34,5,"#C62828"),C(36,34,2,"#111"),C(64,34,2,"#111"),
    PA("M 18 56 Q 50 96 82 56 Q 50 66 18 56 Z","#5A1A1A"),E(50,78,14,7,"#C2185B")]
for k in range(7): sh.append(P([(22+k*8.5,57+abs(k-3)*0.6),(26+k*8.5,66),(30+k*8.5,58+abs(k-3)*0.6)],"#FFF8E1"))
add("isaac_monstro","Monstro","#3E1F1F",sh)
sh=[C(50,50,46,"#2A1A12")]
for (x,y,r) in [(30,30,4),(70,26,3),(64,70,5),(24,66,3),(48,84,3),(80,52,4),(40,52,3)]: sh+=[C(x,y,r,"#4FC3F7"),C(x-r*0.3,y-r*0.3,r*0.35,"#E1F5FE")]
sh+=[PA("M 50 20 Q 60 36 50 44 Q 40 36 50 20 Z","#4FC3F7")]
add("isaac_tears","Tears","#2A1A12",sh)
sh=[C(50,50,46,"#1A0000"),R(0,40,100,20,"#B71C1C"),R(0,45,100,10,"#FF5252"),R(0,48.5,100,3,"#FFCDD2"),C(14,50,12,"#7F0000"),C(14,50,6,"#FF1744")]
add("isaac_brimstone","Brimstone","#1A0000",sh,glow="#FF1744")

def fix(o):
    if isinstance(o,dict): return {k:fix(v) for k,v in o.items()}
    if isinstance(o,list): return [fix(v) for v in o]
    if isinstance(o,str) and o.startswith('#') and len(o)==9: return '#'+o[7:9]+o[1:7]
    return o
out=fix(S); print(len(out)); json.dump(out,open(sys.argv[1],'w'))
