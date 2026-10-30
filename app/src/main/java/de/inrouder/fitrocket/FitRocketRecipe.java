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
        return (int)Math.round(pointsPerServingPrecise(recipe,foods));
    }

    /** Always respect actual catalog zero-point (basic) foods.
     * A recipe-local nutrient override is allowed without modifying catalog entries.
     */
    public static double pointsPerServingPrecise(JSONObject recipe,List<JSONObject> foods){
        if(recipe==null||foods==null)throw new IllegalArgumentException("Rezept fehlt");
        int servings=recipe.optInt("servings",0);
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
            if(!Double.isFinite(grams)||grams<=0||grams>10000)throw new IllegalArgumentException("Menge");
            JSONObject n=part.optJSONObject("nutritionOverride");
            if(n==null){
                if(food==null)throw new IllegalArgumentException("Fehlende Zutat "+part.optString("id"));
                if("basic".equals(food.optString("status")))continue; // immutable ZeroPoints
                if(!"nutrition".equals(food.optString("status")))throw new IllegalArgumentException("Ungeprüfte Zutat");
                n=food.optJSONObject("nutrition");
            }
            if(n==null)throw new IllegalArgumentException("Nährwerte fehlen");
            double k=n.optDouble("kcal",Double.NaN),fat=n.optDouble("saturatedFat_g",Double.NaN);
            double su=n.optDouble("sugar_g",Double.NaN),pro=n.optDouble("protein_g",Double.NaN);
            FitRocketAiRules.required(k,1000);FitRocketAiRules.required(fat,100);
            FitRocketAiRules.required(su,100);FitRocketAiRules.required(pro,100);
            double factor=grams/100.0;
            kcal+=k*factor;saturated+=fat*factor;sugar+=su*factor;protein+=pro*factor;
        }
        return FitRocketMath.foodPointsPrecise(kcal,saturated,sugar,protein);
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
