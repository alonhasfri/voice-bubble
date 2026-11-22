import json, math, random, sys
exec(open('gen.py').read().split('# ---------- food ----------')[0])
TH={}
def theme(i,bg,shapes,name): TH["art_"+i]={"name":name,"background":bg if isinstance(bg,list) else [bg],"glow":None,"mic":None,"shapes":shapes}
def scatter(fn,n,seed,r0=8,r1=44):
    random.seed(seed); out=[]
    for k in range(n):
        a=random.uniform(0,6.283); r=random.uniform(r0,r1); out+=fn(50+r*math.cos(a),50+r*math.sin(a),k)
    return out
def diamonds(c1,c2,size=20):
    sh=[]
    for row in range(-1,7):
        for col in range(-1,7):
            cx=col*size+(size/2 if row%2 else 0); cy=row*size*0.75
            sh.append(P([(cx,cy-size*0.5),(cx+size*0.5,cy),(cx,cy+size*0.5),(cx-size*0.5,cy)],c1 if (row+col)%2 else c2))
    return sh
def spade(cx,cy,s,c):
    return [C(cx-s*0.45,cy+s*0.1,s*0.5,c),C(cx+s*0.45,cy+s*0.1,s*0.5,c),P([(cx,cy-s),(cx-s*0.93,cy+s*0.05),(cx+s*0.93,cy+s*0.05)],c),P([(cx,cy+s*0.2),(cx-s*0.35,cy+s*1.05),(cx+s*0.35,cy+s*1.05)],c)]
def heart(cx,cy,s,c):
    return [C(cx-s*0.5,cy-s*0.2,s*0.55,c),C(cx+s*0.5,cy-s*0.2,s*0.55,c),P([(cx-s*1.02,cy-s*0.05),(cx+s*1.02,cy-s*0.05),(cx,cy+s*1.05)],c)]
def bone(x1,y1,x2,y2,c,w=5):
    a=math.atan2(y2-y1,x2-x1); ox,oy=-math.sin(a)*w*0.55,math.cos(a)*w*0.55
    return [L(x1,y1,x2,y2,c,w),C(x1+ox,y1+oy,w*0.62,c),C(x1-ox,y1-oy,w*0.62,c),C(x2+ox,y2+oy,w*0.62,c),C(x2-ox,y2-oy,w*0.62,c)]
def spear(x1,y1,x2,y2,c):
    a=math.atan2(y2-y1,x2-x1)
    return [L(x1,y1,x2,y2,c,3),P([(x2+6*math.cos(a),y2+6*math.sin(a)),(x2-6*math.cos(a)+5*math.sin(a),y2-6*math.sin(a)-5*math.cos(a)),(x2-6*math.cos(a)-5*math.sin(a),y2-6*math.sin(a)+5*math.cos(a))],c)]
def tear(cx,cy,s,c): return [PA(f"M {cx} {cy-s*1.6} Q {cx+s} {cy} {cx} {cy+s} Q {cx-s} {cy} {cx} {cy-s*1.6} Z",c),C(cx-s*0.3,cy-s*0.1,s*0.25,"#FFFFFFB0")]
def flame(cx,cy,s,c1,c2): return [PA(f"M {cx} {cy+s} Q {cx-s} {cy} {cx-s*0.3} {cy-s*0.6} Q {cx-s*0.2} {cy-s*0.1} {cx} {cy-s*1.2} Q {cx+s*0.6} {cy-s*0.4} {cx+s*0.6} {cy+s*0.2} Q {cx+s*0.5} {cy+s} {cx} {cy+s} Z",c1),PA(f"M {cx} {cy+s*0.8} Q {cx-s*0.45} {cy+s*0.2} {cx} {cy-s*0.4} Q {cx+s*0.4} {cy+s*0.2} {cx} {cy+s*0.8} Z",c2)]
def wing(cx,cy,s,c,flip=1): return [PA(f"M {cx} {cy} Q {cx+flip*s*1.4} {cy-s*1.2} {cx+flip*s*2} {cy-s*0.2} Q {cx+flip*s*1.4} {cy-s*0.1} {cx+flip*s*1.6} {cy+s*0.5} Q {cx+flip*s} {cy+s*0.2} {cx+flip*s*1.1} {cy+s*0.9} Q {cx+flip*s*0.5} {cy+s*0.4} {cx} {cy} Z",c)]
def star_(cx,cy,s,c): return [P(star(cx,cy,s,s*0.42),c)]

# Jevil: harlequin purple/yellow, devilsknife (spinning spade-bladed scythe), curled J tail, bells
sh=diamonds("#4A148C","#1A0A2A",22)
sh+=[C(50,50,30,"#1A0A2AC0"),L(26,74,74,26,"#E1BEE7",3.5)]
sh+=spade(74,26,9,"#FDD835")+spade(26,74,9,"#FDD835")
sh+=[PA("M 70 30 Q 86 44 80 60","#B39DDB",stroke=3),PA("M 30 70 Q 14 56 20 40","#B39DDB",stroke=3),C(50,50,5,"#FDD835"),
     PA("M 52 88 Q 70 92 70 80 Q 70 72 62 76","#7B1FA2",stroke=3.5),C(22,20,3.5,"#FDD835"),C(80,84,3,"#FDD835"),T(50,50,7,"J","#4A148C")]
theme("dr_jevil",["#2A0F45"],sh,"Jevil theme")
# Spamton: pink/yellow split, [[ ]] , $, deal tag
sh=[P([(0,0),(100,0),(0,100)],"#FF4FA3"),P([(100,0),(100,100),(0,100)],"#FFEB3B"),T(50,42,22,"[[ ]]","#111111"),T(30,72,16,"$","#111111"),T(70,72,16,"$","#111111"),
    R(30,52,40,10,"#111111",2),T(50,57.5,7,"BIG SHOT","#FFEB3B")]
sh+=scatter(lambda x,y,k:[C(x,y,1.2,"#FFFFFF")],12,5,30,46)
theme("dr_spamton",["#FF4FA3"],sh,"Spamton theme")
# Rouxls: royal blue diamonds, white mustache curl, puzzle piece
sh=diamonds("#1A3FB0","#0D2470",22)+[C(50,50,28,"#0D2470D0"),R(38,38,24,24,"#FFFFFF",2),C(50,36,5,"#FFFFFF"),C(64,50,5,"#FFFFFF"),C(50,64,5,"#0D2470"),C(36,50,5,"#0D2470"),
    PA("M 30 80 Q 40 72 50 78 Q 60 72 70 80","#FFFFFF",stroke=3)]
theme("dr_rouxls",["#0D2470"],sh,"Rouxls theme")
# Lancer: spades everywhere, big central spade, blue/black
sh=[]
for (x,y,s) in [(18,20,6),(82,22,5),(16,78,5),(84,80,6),(50,12,4),(50,90,4)]: sh+=spade(x,y,s,"#1E88E5")
sh+=spade(50,46,22,"#111111")+spade(50,46,18,"#2962FF")+[C(44,42,3,"#FFFFFF80")]
theme("dr_lancer",["#E3F2FD","#90CAF9"],sh,"Lancer theme")
# Susie: purple, red axe, chalk
sh=[L(30,84,66,20,"#5D4037",5),PA("M 58 12 Q 86 14 88 38 Q 76 32 66 36 Z","#E53935"),PA("M 58 12 Q 46 22 52 34 Q 58 30 66 36 Z","#C62828"),L(60,24,84,32,"#FFCDD2",1.2)]
sh+=scatter(lambda x,y,k:[R(x,y,6,2.5,["#FFFFFF","#F8BBD0","#B3E5FC"][k%3],1)],8,7,10,40)
theme("dr_susie",["#4A1A6A","#7B3FA0"],sh,"Susie theme")
# Ralsei: green, pink scarf stripe, cake, heart
sh=[R(0,62,100,14,"#F06292"),R(0,68,100,3,"#F8BBD0"),P([(30,52),(70,52),(66,38),(34,38)],"#FFF3E0"),R(32,40,36,6,"#F48FB1",2),C(50,34,4,"#E53935")]+heart(50,22,6,"#111111")
sh+=scatter(lambda x,y,k:star_(x,y,2,"#FFFFFFA0"),8,9,30,46)
theme("dr_ralsei",["#43A047","#1B5E20"],sh,"Ralsei theme")
# Kris: blue/yellow stripes, red soul, sword
sh=[R(0,0,100,100,"#1565C0")]
for y in range(14,100,22): sh.append(R(0,y,100,6,"#FDD835"))
sh+=[L(26,80,74,22,"#ECEFF1",4),L(30,70,40,80,"#5D4037",3),C(26,80,3,"#5D4037")]+heart(56,58,10,"#FF0000")+[C(52,54,2,"#FF8080")]
theme("dr_kris",["#1565C0"],sh,"Kris theme")
# Queen: azure crown, circuit lines, acid glass
sh=[]
for k in range(8): sh.append(PA(f"M {k*14} 100 L {k*14} {70-k%3*10} L {k*14+8} {60-k%3*10}","#29B6F680",stroke=1.5))
sh+=[P([(28,48),(34,22),(42,38),(50,16),(58,38),(66,22),(72,48)],"#29B6F6"),R(28,46,44,8,"#0288D1",2),C(50,16,3,"#FFFFFF"),C(34,22,2.4,"#FFFFFF"),C(66,22,2.4,"#FFFFFF"),
     P([(40,62),(60,62),(54,76),(46,76)],"#B3E5FC"),L(50,76,50,86,"#B3E5FC",2),L(44,86,56,86,"#B3E5FC",2),P([(42,64),(58,64),(54,72),(46,72)],"#C6FF00")]
theme("dr_queen",["#111111","#0D2A40"],sh,"Queen theme")
# Tenna: TV colour bars + antennas + static
cols=["#FFFFFF","#FFEB3B","#00E5FF","#00E676","#FF4081","#F44336","#2962FF"]
sh=[R(i*100/7,0,100/7+0.5,70,c) for i,c in enumerate(cols)]+[R(0,70,100,30,"#1A1A1A")]
sh+=scatter(lambda x,y,k:[R(x,y,2,1,"#FFFFFF")],30,3,0,48)
sh+=[L(40,20,28,4,"#B39DDB",2.5),L(60,20,72,4,"#B39DDB",2.5)]
theme("dr_tenna",["#2A1A4A"],sh,"Tenna theme")
# Sans: blue hoodie, bones, blue eye glow
sh=bone(20,30,80,30,"#FAFAFA")+bone(24,76,76,76,"#FAFAFA")+[C(50,53,10,"#00B0FF60"),C(50,53,6,"#00E5FF"),C(50,53,2.5,"#FFFFFF"),PA("M 44 53 Q 50 44 56 53","#FFEB3B",stroke=1.5)]
theme("ut_sans",["#1565C0","#0D47A1"],sh,"Sans theme")
# Papyrus: red scarf, spaghetti, bone
sh=[PA("M 0 22 Q 50 34 100 20 L 100 36 Q 50 48 0 38 Z","#E53935"),P([(64,36),(84,70),(70,72)],"#C62828")]+bone(18,70,62,52,"#FAFAFA",5)
sh+=[E(36,82,14,5,"#ECEFF1"),PA("M 26 80 Q 34 74 46 80","#F5D27A",stroke=2),PA("M 28 82 Q 36 78 44 84","#F5D27A",stroke=2),C(38,79,2,"#C62828")]
theme("ut_papyrus",["#FF8A80","#FFCDD2"],sh,"Papyrus theme")
# Toriel: purple, delta rune emblem, pie
sh=[C(50,44,22,"#FFFFFF30"),E(50,30,14,8,"#FFFFFF"),E(50,30,8,4,"#7B1FA2"),P([(34,56),(66,56),(50,40)],"#FFFFFF"),P([(30,64),(44,56),(36,72)],"#FFFFFF"),P([(70,64),(56,56),(64,72)],"#FFFFFF"),
    P([(36,84),(64,84),(50,72)],"#E8B04A"),R(34,84,32,4,"#C68B3C",2)]
theme("ut_toriel",["#7B1FA2","#4A148C"],sh,"Toriel theme")
# Flowey: friendliness pellets around, yellow petals ring
sh=[]
for k in range(12):
    a=math.radians(k*30); sh.append(E(50+40*math.cos(a),50+40*math.sin(a),8,5,"#FFD600"))
sh+=scatter(lambda x,y,k:[C(x,y,3,"#FFFFFF"),C(x,y,1.5,"#E0E0E0")],14,13,4,28)+[PA("M 50 92 Q 46 70 50 56","#7CB342",stroke=4)]
theme("ut_flowey",["#1B1B1B"],sh,"Flowey theme")
# Undyne: spears + red ponytail swoosh + armour blue
sh=[PA("M 6 14 Q 40 36 30 90","#E53935",stroke=8)]+spear(14,86,80,20,"#00E5FF")+spear(30,92,86,40,"#00E5FF")+spear(10,60,70,10,"#80DEEA")
theme("ut_undyne",["#0D47A1","#01579B"],sh,"Undyne theme")
# Napstablook: headphones, tears, gloomy blue
sh=[PA("M 22 56 Q 22 18 50 18 Q 78 18 78 56","#FAFAFA",stroke=4),R(14,50,14,22,"#FAFAFA",4),R(72,50,14,22,"#FAFAFA",4)]+tear(50,58,6,"#B3E5FC")+tear(38,74,4,"#B3E5FC")+tear(62,76,4,"#B3E5FC")
theme("ut_napstablook",["#37474F","#263238"],sh,"Napstablook theme")
# Temmie: tem flakes + hOI text
sh=scatter(lambda x,y,k:[P([(x,y),(x+5,y-1),(x+4,y+4)],["#FFCC80","#F8BBD0","#FFF59D"][k%3])],20,15,4,44)+[R(22,36,56,26,"#FFFFFF",8),T(50,50,15,"hOI!!","#3949AB")]
theme("ut_temmie",["#90CAF9","#E3F2FD"],sh,"Temmie theme")
# Gojo: infinity, blue/white, blindfold strip
sh=[R(0,30,100,12,"#111111")]
for k in range(5): sh.append(C(50,62,30-k*5.5,"#FFFFFF",stroke=1.2))
sh+=[C(50,62,4,"#29B6F6"),PA("M 26 62 Q 38 48 50 62 Q 62 76 74 62 Q 62 48 50 62 Q 38 76 26 62 Z","#FFFFFF",stroke=2)]
theme("jjk_gojo",["#4FC3F7","#0277BD"],sh,"Gojo theme")
# Sukuna: red/black, cursed marks, shrine flames
sh=[L(14,30,40,34,"#111111",3),L(16,40,38,42,"#111111",2),L(86,30,60,34,"#111111",3),L(84,40,62,42,"#111111",2)]
for x in (22,38,62,78): sh+=flame(x,76,9,"#FF3D00","#FFAB00")
sh+=[R(44,46,12,36,"#7F0000"),R(34,46,32,5,"#B71C1C",1)]
theme("jjk_sukuna",["#7F0000","#1A0000"],sh,"Sukuna theme")
# Yuji: black flash sparks + collar
sh=[R(0,74,100,26,"#0D1B4A"),C(50,74,6,"#FFD54F")]
for (x1,y1,x2,y2) in [(20,20,48,56),(80,18,54,54),(14,60,44,48),(86,58,58,48)]: sh+=[L(x1,y1,x2,y2,"#111111",5),L(x1,y1,x2,y2,"#FF1744",2.5)]
theme("jjk_yuji",["#F48FB1","#880E4F"],sh,"Yuji theme")
# Megumi: shadow, paw prints, divine dog silhouettes
sh=[PA("M 0 70 Q 50 56 100 70 L 100 100 L 0 100 Z","#111111")]
for (x,y) in [(30,30),(52,42),(72,24),(40,56)]: sh+=[E(x,y,5,4,"#111111"),C(x-5,y-6,2,"#111111"),C(x-1.5,y-8,2,"#111111"),C(x+2,y-8,2,"#111111"),C(x+5,y-6,2,"#111111")]
theme("jjk_megumi",["#B0BEC5","#546E7A"],sh,"Megumi theme")
# Isaac: basement brown, tears, red heart
sh=[R(0,0,100,100,"#5D4037")]
for y in range(0,100,14):
    for x in range(-10 if (y//14)%2 else 0,100,22): sh.append(R(x,y,20,12,"#4E342E",1))
sh+=heart(50,50,14,"#E53935")+[C(44,44,3,"#FF8A80")]+tear(24,26,5,"#4FC3F7")+tear(78,72,5,"#4FC3F7")+tear(74,24,4,"#4FC3F7")
theme("isaac_isaac",["#5D4037"],sh,"Isaac theme")
# The Lost: halo, white wings, pale
sh=wing(50,56,14,"#FAFAFA",1)+wing(50,56,14,"#FAFAFA",-1)+[E(50,30,16,5,"#FFF59D",stroke=3),C(50,58,6,"#FFFFFF")]
theme("isaac_lost",["#263238","#000000"],sh,"The Lost theme")
# Azazel: black wings, red beam
sh=wing(50,40,14,"#111111",1)+wing(50,40,14,"#111111",-1)+[R(0,62,100,12,"#B71C1C"),R(0,65,100,6,"#FF5252"),R(0,67,100,2,"#FFCDD2")]
theme("isaac_azazel",["#5D0000","#1A0000"],sh,"Azazel theme")
# Judas: red fez, coins
sh=[R(30,28,40,24,"#C62828",5),E(50,52,22,5,"#8E0000"),L(62,30,74,48,"#FDD835",2),C(74,48,3,"#FDD835")]
sh+=scatter(lambda x,y,k:[C(x,y,4,"#FBC02D"),C(x,y,2.5,"#FFE082")],8,17,34,44)
theme("isaac_judas",["#4E342E","#3E2723"],sh,"Judas theme")
# Monstro: pink drips, teeth row
sh=[PA("M 0 0 L 100 0 L 100 30 Q 90 42 84 30 Q 76 50 68 30 Q 58 44 50 30 Q 42 48 34 30 Q 24 42 16 30 Q 8 40 0 30 Z","#E9A0A0")]
for k in range(8): sh.append(P([(k*13,60),(k*13+6.5,74),(k*13+13,60)],"#FFF8E1"))
sh+=[R(0,56,100,6,"#5A1A1A")]
theme("isaac_monstro",["#3E1F1F"],sh,"Monstro theme")

def fix(o):
    if isinstance(o,dict): return {k:fix(v) for k,v in o.items()}
    if isinstance(o,list): return [fix(v) for v in o]
    if isinstance(o,str) and o.startswith('#') and len(o)==9: return '#'+o[7:9]+o[1:7]
    return o
TH=fix(TH)
chars=json.load(open('chars.json'))
for e in chars:
    if e['id'] in TH: e['theme']=TH[e['id']]
json.dump(chars,open('chars_th.json','w'))
json.dump([{"id":k,"name":v["name"],"spec":v} for k,v in TH.items()],open('themes_only.json','w'))
print(len(TH), sum(1 for e in chars if 'theme' in e))
