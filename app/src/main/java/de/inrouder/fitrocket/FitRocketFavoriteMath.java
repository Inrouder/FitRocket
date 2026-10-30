package de.inrouder.fitrocket;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Quantities for saved favorites are references, never frozen daily portions. */
public final class FitRocketFavoriteMath {
    private FitRocketFavoriteMath() {}

    private static final Pattern TRAILING_QUANTITY=Pattern.compile(
        "\\((\\d+(?:[.,]\\d+)?)\\s*(g|ml)\\)\\s*$",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern PORTION_AMOUNT=Pattern.compile(
        "(\\d+(?:[.,]\\d+)?)\\s*(g|ml)\\b",
        Pattern.CASE_INSENSITIVE);

    /** Optional local note on a favorite. Notes never affect nutrition or points. */
    public static String normalizeNote(String raw) {
        String note=raw==null?"":raw.trim();
        if(note.length()>600)throw new IllegalArgumentException("Notizen dürfen maximal 600 Zeichen enthalten.");
        return note;
    }

    public static double positive(double value) {
        if (!Double.isFinite(value) || value<=0 || value>10000)
            throw new IllegalArgumentException("Bitte 0,1 bis 10000 g/ml eingeben.");
        return value;
    }

    public static String cleanName(String value) {
        if (value==null) return "";
        String name=TRAILING_QUANTITY.matcher(value).replaceFirst("").trim();
        return name.replaceFirst("\\s*×\\s*\\d+(?:[.,]\\d+)?(?:\\s*Portion\\(en\\))?$","").trim();
    }

    public static double ingredientGrams(JSONObject meal) {
        JSONArray ingredients=meal.optJSONArray("aiIngredients");
        if (ingredients==null || ingredients.length()==0) return Double.NaN;
        double sum=0;
        for(int i=0;i<ingredients.length();i++){
            JSONObject ingredient=ingredients.optJSONObject(i);
            if(ingredient==null) return Double.NaN;
            double grams=ingredient.optDouble("grams",Double.NaN);
            if(!Double.isFinite(grams)||grams<=0) return Double.NaN;
            sum+=grams;
        }
        return sum>0&&sum<=10000?sum:Double.NaN;
    }

    public static double referenceQuantity(JSONObject favorite, JSONObject food) {
        double stored=favorite.optDouble("referenceQuantity",Double.NaN);
        if(Double.isFinite(stored)&&stored>0&&stored<=10000)return stored;
        stored=favorite.optDouble("quantity",Double.NaN);
        if(Double.isFinite(stored)&&stored>0&&stored<=10000)return stored;
        double aiGrams=ingredientGrams(favorite);
        if(Double.isFinite(aiGrams))return aiGrams;
        Matcher m=TRAILING_QUANTITY.matcher(favorite.optString("name",""));
        if(m.find())return positive(Double.parseDouble(m.group(1).replace(',','.')));
        if(food!=null){
            Matcher portion=PORTION_AMOUNT.matcher(food.optString("portion",""));
            if(portion.find()) {
                double base=Double.parseDouble(portion.group(1).replace(',','.'));
                Matcher multiplier=Pattern.compile("×\\s*(\\d+(?:[.,]\\d+)?)").matcher(
                    favorite.optString("name",""));
                double factor=multiplier.find()?Double.parseDouble(multiplier.group(1).replace(',','.')):1.0;
                return positive(base*factor);
            }
        }
        // Don't invent unknown original amounts for old saved favorites.
        return Double.NaN;
    }

    public static int scaledPoints(JSONObject favorite,JSONObject food,
                                   double refQuantity,double quantity,
                                   List<JSONObject> foods) {
        positive(refQuantity);positive(quantity);
        if(food!=null && "nutrition".equals(food.optString("status"))) {
            JSONObject n=food.optJSONObject("nutrition");
            if(n!=null)return FitRocketAiRules.points(quantity,
                n.optDouble("kcal",Double.NaN),
                n.optDouble("saturatedFat_g",Double.NaN),
                n.optDouble("sugar_g",Double.NaN),
                n.optDouble("protein_g",Double.NaN));
        }
        JSONArray ingredients=favorite.optJSONArray("aiIngredients");
        if(ingredients!=null&&ingredients.length()>0){
            try{
                JSONArray resized=new JSONArray();
                for(int i=0;i<ingredients.length();i++){
                    JSONObject part=ingredients.getJSONObject(i);
                    JSONObject copy=new JSONObject(part.toString());
                    copy.put("grams",positive(part.getDouble("grams")*quantity/refQuantity));
                    resized.put(copy);
                }
                JSONObject summary=new JSONObject().put("servings",1)
                    .put("ingredients",resized);
                return FitRocketAiRules.pointsPerServing(summary,foods);
            }catch(Exception ex){throw new IllegalArgumentException("Zutatenmengen ungültig.");}
        }
        double proportion=favorite.optInt("points",0)*(quantity/refQuantity);
        if(!Double.isFinite(proportion)||proportion<0||proportion>2000)
            throw new IllegalArgumentException("Punkte außerhalb des gültigen Bereichs.");
        return (int)Math.round(proportion);
    }

    /**
     * New decimal path for food favorites. The legacy integer calculation is kept
     * for older unit tests and existing integer-only integration points.
     */
    public static double scaledPointsPrecise(JSONObject favorite,JSONObject food,
                                             double refQuantity,double quantity,
                                             List<JSONObject> foods) {
        positive(refQuantity);positive(quantity);
        if(food!=null && ("nutrition".equals(food.optString("status"))||
                            "basic".equals(food.optString("status"))))
            return FitRocketPointLedger.food(food,quantity);
        JSONArray ingredients=favorite.optJSONArray("aiIngredients");
        if(ingredients!=null&&ingredients.length()>0){
            try{
                JSONArray resized=scaledIngredients(favorite,refQuantity,quantity);
                JSONObject recipe=new JSONObject().put("servings",1).put("ingredients",resized);
                return FitRocketAiRules.pointsPerServingPrecise(recipe,foods);
            }catch(Exception error){throw new IllegalArgumentException("Zutatenmengen prüfen.",error);}
        }
        return FitRocketPointLedger.scaled(
            favorite.optDouble("referencePoints",favorite.optDouble("points",0)),
            refQuantity,quantity);
    }

    public static JSONArray scaledIngredients(JSONObject favorite,double reference,
                                               double quantity) throws Exception {
        JSONArray original=favorite.optJSONArray("aiIngredients");
        if(original==null)return null;
        positive(reference);positive(quantity);
        JSONArray output=new JSONArray();
        for(int i=0;i<original.length();i++){
            JSONObject part=original.getJSONObject(i);
            JSONObject copy=new JSONObject(part.toString());
            copy.put("grams",positive(part.getDouble("grams")*quantity/reference));
            output.put(copy);
        }
        return output;
    }
}
