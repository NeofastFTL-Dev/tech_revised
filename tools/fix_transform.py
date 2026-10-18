from pathlib import Path
import re

root = Path("src/main/java")
for path in root.rglob("*.java"):
    text = path.read_text(encoding="utf-8")
    original = text
    text = text.replace('\\"tech_revised:recipe\\"', '"tech_revised:recipe"')
    text = re.sub(
        r"ingredientToNetwork\(([^,\n]+)\., buffer\)",
        r"ingredientToNetwork(\1, buffer)",
        text,
    )
    if text != original:
        path.write_text(text, encoding="utf-8")
        print("fixed", path)
