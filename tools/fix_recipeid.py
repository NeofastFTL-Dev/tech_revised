from pathlib import Path

for path in Path("src/main/java/com/neofast/tech_revised/recipe").glob("*.java"):
    text = path.read_text(encoding="utf-8")
    updated = text.replace("recipeId", '"unknown"')
    if updated != text:
        path.write_text(updated, encoding="utf-8")
        print(path)
