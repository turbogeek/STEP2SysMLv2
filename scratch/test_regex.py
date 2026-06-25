import json

text = "#32=CARTESIAN_POINT('',(46.765612334,29.,5.));"
import re
regex = re.compile(r"CARTESIAN_POINT\s*\([^,]*,*\(([^)]+)\)\s*\)")
m = regex.search(text)
if m:
    print("Match:", m.group(1))
else:
    print("No match")
