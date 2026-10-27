#!/usr/bin/env python3
"""Validate bundled FitRocket foods and recipes before Android APK builds."""
import json
from pathlib import Path
import math

ROOT = Path(__file__).resolve().parent.parent
foods = json.loads((ROOT / "app/src/main/assets/foods.json").read_text(encoding="utf-8"))
recipes = json.loads((ROOT / "app/src/main/assets/recipes.json").read_text(encoding="utf-8"))

assert isinstance(foods, list) and len(foods) >= 356, "Food catalog unexpectedly reduced"
assert not any(f.get("id") in {"BEISPIEL001", "BEISPIEL002"} for f in foods), "Ambiguous pasta/rice entries returned"
assert isinstance(recipes, list) and len(recipes) >= 10, "Recipe catalog unexpectedly reduced"
by_id = {}
statuses = {"basic", "nutrition", "portion", "custom"}
for food in foods:
    key = food.get("id")
    assert isinstance(key, str) and key and key not in by_id, f"Duplicate food id: {key}"
    assert food.get("status") in statuses, f"Invalid food status: {key}"
    assert isinstance(food.get("name"), str) and food["name"].strip(), f"Unnamed food: {key}"
    if food["status"] == "basic":
        assert food.get("points") == 0, f"Basic food not zero: {key}"
    if food["status"] == "nutrition":
        nutrient = food.get("nutrition") or {}
        for field in ("kcal", "saturatedFat_g", "sugar_g", "protein_g"):
            value = nutrient.get(field)
            assert isinstance(value, (int, float)) and math.isfinite(value) and 0 <= value <= 2000, (
                f"Missing/invalid {field} in {key}"
            )
    by_id[key] = food

ids = set()
for recipe in recipes:
    key = recipe.get("id")
    assert isinstance(key, str) and key not in ids, f"Recipe id repeated: {key}"
    ids.add(key)
    assert recipe.get("group") in ("Frühstück", "Mittagessen", "Abendessen", "Snack"), key
    assert isinstance(recipe.get("instructions"), str) and recipe["instructions"].strip(), key
    servings = recipe.get("servings")
    assert isinstance(servings, int) and 1 <= servings <= 20, key
    parts = recipe.get("ingredients")
    assert isinstance(parts, list) and parts, f"No ingredients: {key}"
    for part in parts:
        ingredient_id = part.get("id")
        ingredient = by_id.get(ingredient_id)
        assert ingredient is not None, f"Unknown ingredient {ingredient_id} in {key}"
        assert ingredient["status"] in ("basic", "nutrition"), f"Ambiguous points {ingredient_id} in {key}"
        grams = part.get("grams")
        assert isinstance(grams, (int, float)) and math.isfinite(grams) and 0 < grams <= 10000, key
    assert isinstance(recipe.get("name"), str) and recipe["name"].strip(), key

print(f"FitRocket data verified: {len(foods)} foods, {len(recipes)} recipes, all recipe links valid.")
