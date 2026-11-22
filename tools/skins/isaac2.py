import json, math, sys
src=open('chars2.py').read()
exec(src.split('# ---------- Deltarune ----------')[0])
S.clear()
def isaac_head(skin="#F2D3C2",line="#5A3A3A",y=54): return head(skin,line,33,y)
# Brimstone: Isaac with curved demon horns, charging a blood laser
sh=[R(0,64,100,18,"#B71C1C"),R(0,67,100,12,"#FF3B30"),R(0,70.5,100,5,"#FFCDD2")]
sh+=isaac_head(y=52)
for hp in ("M 30 30 Q 14 22 18 4 Q 28 14 40 24 Z","M 70 30 Q 86 22 82 4 Q 72 14 60 24 Z"):
    sh+=[PA(hp,"#2A0E0E"),PA(hp,"#FF6E6E",stroke=1.8)]
sh+=eyes(50,14,7,8,"#2B2233")+blush(62,22)
sh+=[C(50,68,9,"#FF3B30"),C(50,68,5,"#FFCDD2"),PA("M 30 40 Q 36 38 42 42","#5A3A3A",stroke=2.4),PA("M 70 40 Q 64 38 58 42","#5A3A3A",stroke=2.4)]
add("isaac_brimstone","Brimstone","#7A1A1A",sh,glow="#FF3B30")
# Magdalene: curly blonde hair
sh=[]
for (x,y,r) in [(20,40,10),(26,24,11),(40,14,11),(56,12,11),(72,20,11),(82,36,10),(18,58,9),(84,56,9),(22,74,8),(80,74,8)]: sh.append(C(x,y,r,"#F4C430"))
sh+=isaac_head(y=56)+[C(34,30,8,"#F4C430"),C(50,26,9,"#F4C430"),C(66,30,8,"#F4C430")]+eyes(58,14,6.5,7.5,"#2B2233")+blush(68,22,"#FF7EA0B0")+smile(74,4.5)
add("isaac_magdalene","Magdalene","#F8BBD0",sh)
# Cain: brown messy hair, eyepatch
sh=isaac_head(y=56)+[PA("M 20 44 Q 22 18 50 18 Q 78 18 80 44 Q 70 32 62 38 Q 58 28 50 34 Q 42 28 38 36 Q 30 32 20 44 Z","#6D4C41")]
sh+=[E(64,58,6.5,7.5,"#2B2233"),C(61.7,55.2,2.5,"#FFFFFF"),E(36,58,8,7,"#1A1A1A"),L(26,48,74,42,"#1A1A1A",2.2)]+blush(68,22)+smile(74,4.5)
add("isaac_cain","Cain","#A1887F",sh)
# ??? (Blue Baby)
sh=isaac_head("#7EB6E8","#2E5A8A",56)+eyes(56,14,7,8,"#1A2A44")+blush(68,22,"#C8B0FFA0")+[PA("M 44 74 Q 50 71 56 74","#1A2A44",stroke=2.4),
    PA("M 36 64 Q 34 72 37 78 Q 40 72 36 64 Z","#B3E5FC")]
add("isaac_bluebaby","??? (Blue Baby)","#1A2A44",sh,glow="#7EB6E8")
# Eve: black bob, soft dark eyes
sh=[PA("M 16 82 Q 12 30 50 20 Q 88 30 84 82 Z","#1A1A22")]+isaac_head(y=58)+[PA("M 18 52 Q 20 22 50 22 Q 80 22 82 52 L 18 52 Z","#1A1A22")]
sh+=eyes(60,14,6.5,7.5,"#2B2233")+[PA("M 36 68 Q 35 72 37 75","#5A4A5A",stroke=1.4),PA("M 64 68 Q 65 72 63 75","#5A4A5A",stroke=1.4)]+blush(70,22)+[PA("M 45 76 Q 50 79 55 76","#C2185B",stroke=2.6)]
add("isaac_eve","Eve","#B39DDB",sh)
# Samson: messy orange hair, angry brows
sh=isaac_head(y=56)
for (x,y) in [(22,38),(30,24),(42,16),(56,14),(68,20),(78,32)]: sh.append(P([(x-9,44),(x,y-6),(x+9,44)],"#E8742A"))
sh+=[E(50,36,28,8,"#E8742A")]+eyes(60,14,6.5,7,"#2B2233")+[L(28,50,42,54,"#5A3A3A",2.6),L(72,50,58,54,"#5A3A3A",2.6)]+blush(70,22)+[PA("M 44 76 Q 50 73 56 76","#5A3A3A",stroke=2.4)]
add("isaac_samson","Samson","#FFCC80",sh)
# Lilith: long black hair, blindfold
sh=[PA("M 14 96 Q 10 30 50 18 Q 90 30 86 96 Z","#1A1A22")]+isaac_head(y=56)+[PA("M 20 46 Q 24 20 50 20 Q 76 20 80 46 Q 64 34 50 40 Q 36 34 20 46 Z","#1A1A22")]
sh+=[R(18,52,64,11,"#2A2A2A",5)]+blush(68,22)+smile(74,4.5)
add("isaac_lilith","Lilith","#7E57C2",sh)
# Eden: spiky multicolour hair
sh=isaac_head(y=58)
cols=["#E53935","#FFB300","#43A047","#1E88E5","#8E24AA","#00ACC1"]
for i,(x,y) in enumerate([(20,40),(30,22),(44,12),(58,12),(72,20),(82,38)]): sh.append(P([(x-9,46),(x,y-4),(x+9,46)],cols[i]))
sh+=[E(50,38,28,8,"#5D4037")]+eyes(60,14,6.5,7.5,"#2B2233")+blush(70,22)+smile(76,5,open_=True)
add("isaac_eden","Eden","#ECEFF1",sh)
# The Forgotten: skeleton with a bone club
sh=[L(76,90,88,30,"#E0D8C8",6),C(88,28,6,"#E0D8C8"),C(84,24,5,"#E0D8C8")]
sh+=head("#F5F0E6","#9E9688",31,56)+[E(38,56,7,7.5,"#2B2233"),E(62,56,7,7.5,"#2B2233"),C(36,53.5,2.4,"#FFFFFF"),C(60,53.5,2.4,"#FFFFFF"),E(50,65,2,1.6,"#2B2233"),
    PA("M 38 72 Q 50 80 62 72","#2B2233",stroke=2.4)]
for k in (-1,0,1): sh.append(L(50+k*5,73,50+k*5,76.5,"#2B2233",1.3))
add("isaac_forgotten","The Forgotten","#5D4037",sh)
def fix(o):
    if isinstance(o,dict): return {k:fix(v) for k,v in o.items()}
    if isinstance(o,list): return [fix(v) for v in o]
    if isinstance(o,str) and o.startswith('#') and len(o)==9: return '#'+o[7:9]+o[1:7]
    return o
out=fix(S); json.dump(out,open(sys.argv[1],'w')); print(len(out))
