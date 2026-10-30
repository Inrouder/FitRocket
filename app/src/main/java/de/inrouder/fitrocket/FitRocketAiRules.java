package de.inrouder.fitrocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * FitRocket remains the only points calculator. Never accepts an AI-supplied points figure.
 * A zero-point catalogue match requires both the ID AND exact normalized food name.
 */
public final class FitRocketAiRules {
    private FitRocketAiRules(){}

    public static String normalize(String input){
        if(input==null)return "";
        return Normalizer.normalize(input,Normalizer.Form.NFD)
            .replaceAll("\\p{M}+","")
            .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+"," ").trim();
    }

    public static JSONObject matchCatalog(String id,String proposedName,List<JSONObject> foods){
        if(id==null||id.isEmpty()||foods==null)return null;
        String expected=normalize(proposedName);
        for(JSONObject f:foods){
            if(id.equals(f.optString("id")) && expected.equals(normalize(f.optString("name")))
                && ("basic".equals(f.optString("status"))||"nutrition".equals(f.optString("status"))))
                return f;
        }
        return null;
    }

    /**
     * Reuse an existing food on an exact normalized NAME match. This prevents
     * duplicate catalog rows when AI omits food_id. A valid ID/name match wins.
     * Never match vague substrings (e.g. wheat vs wholegrain pasta).
     */
    public static JSONObject existingByName(String proposedName,List<JSONObject> foods){
        String wanted=normalize(proposedName);
        if(wanted.isEmpty()||foods==null)return null;
        for(JSONObject food:foods){
            if(wanted.equals(normalize(food.optString("name")))
                && ("basic".equals(food.optString("status"))||
                    "nutrition".equals(food.optString("status"))))return food;
        }
        return null;
    }

    public static double required(double value,double upper){
        if(!Double.isFinite(value)||value<0||value>upper)
            throw new IllegalArgumentException("Nährwert oder Menge außerhalb des gültigen Bereichs");
        return value;
    }

    public static int points(double grams,double kcal,double saturated,double sugar,double protein){
        required(grams,10000);
        if(grams<=0)throw new IllegalArgumentException("Die Portionsmenge muss positiv sein.");
        required(kcal,1000);required(saturated,100);required(sugar,100);required(protein,100);
        double ratio=grams/100.0;
        return FitRocketMath.foodPoints(kcal*ratio,saturated*ratio,sugar*ratio,protein*ratio);
    }

    public static JSONObject validateResponse(JSONObject root,String mode) throws JSONException {
        if(root==null)throw new IllegalArgumentException("Leere KI-Antwort");
        String title=root.optString("title","").trim();
        if(title.length()<2||title.length()>140)throw new IllegalArgumentException("Der Name ist ungültig.");
        int minutes=root.optInt("minutes",-1);
        if(minutes<0||minutes>240||("recipe".equals(mode)&&minutes<1))
            throw new IllegalArgumentException("Unplausible Zubereitungszeit.");
        int servings=root.optInt("servings",0);
        if(servings<1||servings>12)throw new IllegalArgumentException("Ungültige Anzahl Portionen.");
        if("meal".equals(mode)||"food".equals(mode))servings=1;
        String group=root.optString("group","Snack");
        if(!("Frühstück".equals(group)||"Mittagessen".equals(group)||
             "Abendessen".equals(group)||"Snack".equals(group)||"Getränke".equals(group)))
            throw new IllegalArgumentException("Mahlzeitengruppe unbekannt.");
        JSONArray ingredients=root.optJSONArray("ingredients");
        if(ingredients==null||ingredients.length()<1||ingredients.length()>12)
            throw new IllegalArgumentException("Die KI soll 1–12 Zutaten liefern.");
        if("food".equals(mode)&&ingredients.length()!=1)
            throw new IllegalArgumentException("Ein Produkt darf nur aus einem Nährwertprofil bestehen.");
        for(int i=0;i<ingredients.length();i++){
            JSONObject x=ingredients.optJSONObject(i);
            if(x==null)throw new IllegalArgumentException("Eine Zutat fehlt.");
            String name=x.optString("name","").trim();
            if(name.length()<2||name.length()>110)throw new IllegalArgumentException("Zutatenname ungültig.");
            double grams=x.optDouble("grams",Double.NaN);
            if(!Double.isFinite(grams)||grams<=0||grams>10000)
                throw new IllegalArgumentException("Unplausible Zutatenmenge.");
            points(grams,
                x.optDouble("kcal_100g",Double.NaN),
                x.optDouble("saturated_fat_100g",Double.NaN),
                x.optDouble("sugar_100g",Double.NaN),
                x.optDouble("protein_100g",Double.NaN));
        }
        root.put("servings",servings);
        return root;
    }

    public static int pointsPerServing(JSONObject result,List<JSONObject> catalog) {
        if(result==null||catalog==null)throw new IllegalArgumentException("Daten fehlen");
        int servings=result.optInt("servings",0);
        if(servings<1||servings>12)throw new IllegalArgumentException("Portionen");
        JSONArray ingredients=result.optJSONArray("ingredients");
        if(ingredients==null||ingredients.length()==0||ingredients.length()>12)
            throw new IllegalArgumentException("Zutaten fehlen");
        double kcal=0,saturated=0,sugar=0,protein=0;
        for(int i=0;i<ingredients.length();i++){
            JSONObject item=ingredients.optJSONObject(i);
            if(item==null)throw new IllegalArgumentException("Zutat");
            double grams=item.optDouble("grams",Double.NaN)/servings;
            JSONObject known=matchCatalog(item.optString("food_id"),item.optString("name"),catalog);
            if(known==null)known=existingByName(item.optString("name"),catalog);
            if(known!=null && "basic".equals(known.optString("status")))continue;
            JSONObject macros=known!=null&&"nutrition".equals(known.optString("status"))?
                known.optJSONObject("nutrition"):null;
            double factor=grams/100.0;
            if(!Double.isFinite(factor)||factor<=0||factor>100)throw new IllegalArgumentException("Menge");
            double k=macros==null?item.optDouble("kcal_100g",Double.NaN):macros.optDouble("kcal",Double.NaN);
            double sat=macros==null?item.optDouble("saturated_fat_100g",Double.NaN):macros.optDouble("saturatedFat_g",Double.NaN);
            double su=macros==null?item.optDouble("sugar_100g",Double.NaN):macros.optDouble("sugar_g",Double.NaN);
            double pr=macros==null?item.optDouble("protein_100g",Double.NaN):macros.optDouble("protein_g",Double.NaN);
            required(k,1000);required(sat,100);required(su,100);required(pr,100);
            kcal+=k*factor;saturated+=sat*factor;sugar+=su*factor;protein+=pr*factor;
        }
        return FitRocketMath.foodPoints(kcal,saturated,sugar,protein);
    }

    public static JSONObject newFood(JSONObject item,String prefix) throws JSONException {
        String name=item.optString("name","").trim();
        if(name.length()<2||name.length()>110)throw new IllegalArgumentException("Lebensmittelname");
        double kcal=required(item.optDouble("kcal_100g",Double.NaN),1000);
        double sat=required(item.optDouble("saturated_fat_100g",Double.NaN),100);
        double sugar=required(item.optDouble("sugar_100g",Double.NaN),100);
        double protein=required(item.optDouble("protein_100g",Double.NaN),100);
        JSONObject n=new JSONObject();
        n.put("kcal",kcal);n.put("saturatedFat_g",sat);n.put("sugar_g",sugar);
        n.put("protein_g",protein);n.put("per",100);
        JSONObject food=new JSONObject();
        food.put("id",prefix+UUID.randomUUID().toString().replace("-",""));
        food.put("name",name);food.put("alias","");
        food.put("category","Eigene Lebensmittel");
        food.put("portion","100 g / 100 ml");
        food.put("status","nutrition");
        food.put("points",JSONObject.NULL);
        food.put("nutrition",n);
        food.put("estimatedPoints100",points(100,kcal,sat,sugar,protein));
        food.put("notes","Geschätzte oder selbst geprüfte Nährwerte. Etikett bei Markenprodukten prüfen.");
        food.put("source","FitRocket Nutzer / KI-Schätzung");
        return food;
    }
}
