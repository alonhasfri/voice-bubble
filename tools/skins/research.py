import json, re, sys, urllib.request, urllib.parse
W={"dr":"https://deltarune.wiki/api.php","ut":"https://undertale.wiki/api.php","jjk":"https://jujutsu-kaisen.fandom.com/api.php","isaac":"https://bindingofisaacrebirth.wiki.gg/api.php"}
def get(api,params):
    u=api+"?"+urllib.parse.urlencode({**params,"format":"json"})
    return json.load(urllib.request.urlopen(urllib.request.Request(u,headers={"User-Agent":"VoiceBubble/1.0"}),timeout=20))
def clean(t):
    t=re.sub(r"\[\[(?:File|Image):[^\]]*\]\]","",t); t=re.sub(r"\{\{[^{}]*\}\}","",t); t=re.sub(r"<ref[^>]*>.*?</ref>","",t,flags=re.S)
    t=re.sub(r"\[\[(?:[^|\]]*\|)?([^\]]*)\]\]",r"\1",t); t=re.sub(r"'{2,}","",t); t=re.sub(r"<[^>]+>","",t); t=re.sub(r"=+[^=]+=+","",t)
    return re.sub(r"\s+"," ",t).strip()
out={}
for w,page in [l.split(":",1) for l in sys.argv[1:]]:
    try:
        secs=get(W[w],{"action":"parse","page":page,"prop":"sections","redirects":1})["parse"]["sections"]
        idx=next((s["index"] for s in secs if "ppearance" in s["line"]),None)
        txt=get(W[w],{"action":"parse","page":page,"prop":"wikitext","section":idx or 0,"redirects":1})["parse"]["wikitext"]["*"]
        out[page]=clean(txt)[:900]
    except Exception as e: out[page]="ERR "+str(e)
json.dump(out,open("research/appearance.json","w"),indent=1)
for k,v in out.items(): print("##",k,":",v[:420]); print()
