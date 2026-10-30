package de.inrouder.fitrocket;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/** Estimate a recipe from actual FitRocket food catalogue entries and entered ingredient amounts. */
public final class FitRocketRecipe {
    private FitRocketRecipe(){}

    public static int pointsPerServing(JSONObject recipe,List<JSONObject> foods){
        if(recipe==null||foods==null)throw new IllegalArgumentException("Rezept fehlt");
        int servings=recipe.optInt("servings",1);
        if(servings<1||servings>20)throw new IllegalArgumentException("Portionen");
        Map<String,JSONObject> byId=new HashMap<>();
        for(JSONObject food:foods)byId.put(food.optString("id"),food);
        JSONArray ingredients=recipe.optJSONArray("ingredients");
        if(ingredients==null||ingredients.length()==0)throw new IllegalArgumentException("Keine Zutaten");
        double kcal=0,saturated=0,sugar=0,protein=0;
        for(int i=0;i<ingredients.length();i++){
            JSONObject part=ingredients.optJSONObject(i);
            if(part==null)throw new IllegalArgumentException("Zutat");
            JSONObject food=byId.get(part.optString("id"));
            double grams=part.optDouble("grams",Double.NaN)/servings;
            if(food==null||!Double.isFinite(grams)||grams<=0||grams>10000)throw new IllegalArgumentException("Fehlende Zutat");
            String status=food.optString("status");
            if("basic".equals(status))continue;
            if(!"nutrition".equals(status))throw new IllegalArgumentException("Ungeprüfte Zutat");
            JSONObject n=food.optJSONObject("nutrition");
            if(n==null||!n.has("kcal")||!n.has("saturatedFat_g")||!n.has("sugar_g")||!n.has("protein_g"))
                throw new IllegalArgumentException("Nährwerte fehlen");
            double factor=grams/100.0;
            kcal+=n.optDouble("kcal")*factor;
            saturated+=n.optDouble("saturatedFat_g")*factor;
            sugar+=n.optDouble("sugar_g")*factor;
            protein+=n.optDouble("protein_g")*factor;
        }
        // Round once per serving, not once for every ingredient.\n        return FitRocketMath.foodPoints(kcal,saturated,sugar,protein);
    }

    public static String ingredients(JSONObject recipe,List<JSONObject> foods){
        StringBuilder text=new StringBuilder();
        JSONArray arr=recipe.optJSONArray("ingredients");
        if(arr==null)return "";
        for(int i=0;i<arr.length();i++){
            JSONObject part=arr.optJSONObject(i);if(part==null)continue;
            String name=part.optString("id");
            for(JSONObject food:foods)if(name.equals(food.optString("id"))){name=food.optString("name");break;}
            text.append("• ").append(part.optInt("grams")).append(" g ").append(name).append("\n");
        }
        return text.toString().trim();
    }
}
