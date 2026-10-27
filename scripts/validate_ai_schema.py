#!/usr/bin/env python3
"""Validate a strict OpenAI Responses JSON Schema without exposing API credentials."""
import json
from pathlib import Path

schema_path=Path(__file__).resolve().parent.parent / "app/src/main/assets/ai_result_schema.json"
schema=json.loads(schema_path.read_text(encoding="utf-8"))

def check_structure(obj,location):
    assert obj["type"]=="object", f"Unexpected object type: {location}"
    assert obj.get("additionalProperties") is False, f"Unknown keys allowed at: {location}"
    props=obj.get("properties",{})
    assert props, f"No properties at: {location}"
    assert set(obj.get("required",[])) == set(props), f"Not all keys required: {location}"
    for key,prop in props.items():
        if prop.get("type")=="object":
            check_structure(prop,location+"."+key)
        if prop.get("type")=="array" and prop.get("items",{}).get("type")=="object":
            check_structure(prop["items"],location+"."+key+"[]")

check_structure(schema,"root")
assert set(schema["properties"])=={
 "title","summary","instructions","group","servings","minutes",
 "confidence","uncertainty","ingredients"
}
item=schema["properties"]["ingredients"]["items"]
assert set(item["properties"])=={
 "name","food_id","grams","kcal_100g","saturated_fat_100g",
 "sugar_100g","protein_100g","confidence","note"
}
assert "niedrig" in item["properties"]["confidence"]["enum"]
assert "Getränke" in schema["properties"]["group"]["enum"]
print("FitRocket AI schema validated: strict response shape and ingredients.")
