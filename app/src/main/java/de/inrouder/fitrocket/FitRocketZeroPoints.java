package de.inrouder.fitrocket;

import org.json.JSONObject;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.*;

/** Explicit natural-food exceptions. Never infer total fat from saturated fat or match mixed dishes. */
public final class FitRocketZeroPoints {
    public static final String TOMATOES="FRZERO001", COTTAGE="FRZERO002";
    private static final String CHEESE="(?:k[oö]rniger frischk[aä]se|koerniger frischkaese|h[uü]ttenk[aä]se|huettenkaese|cottage cheese)";
    private static final Pattern TOMATO=Pattern.compile("^(?:bio )?(?:cocktail[ -]?tomaten|cherry[ -]?tomaten|kirsch[ -]?tomaten|cherry tomatoes|tomaten)(?: (?:natur|frisch|roh))?$");
    private static final Pattern COTTAGE_NAME=Pattern.compile("^(?:bio )?"+CHEESE+"(?: (?:natur|light|fettarm))?(?: (?:bis|max\\.?|maximal))?(?: ([0-9]+(?:[.,][0-9]+)?)\\s*(?:%|prozent)\\s*(?:fett)?)?(?: (?:natur|light|fettarm))?$");
    private FitRocketZeroPoints(){}
    private static String folded(String text){
        return Normalizer.normalize(text==null?"":text,Normalizer.Form.NFD).replaceAll("\\p{M}+","")
            .toLowerCase(Locale.ROOT).replace('(', ' ').replace(')', ' ')
            .replaceAll("(?<![0-9]),|,(?![0-9])"," ").replaceAll("\\s+-\\s+"," ").replaceAll("\\s+"," ").trim();
    }
    public static String catalogId(String name,double totalFat){
        String value=folded(name);
        if(Double.isInfinite(totalFat))return null;
        if(TOMATO.matcher(value).matches())return TOMATOES;
        Matcher match=COTTAGE_NAME.matcher(value);
        if(!match.matches())return null;
        double declared=Double.NaN;
        if(match.group(1)!=null)declared=Double.parseDouble(match.group(1).replace(',','.'));
        // Every available source must agree with the limit. Missing fat remains unknown.
        if(Double.isFinite(totalFat)&&(totalFat<0||totalFat>0.5))return null;
        if(Double.isFinite(declared)&&(declared<0||declared>0.5))return null;
        return Double.isFinite(declared)||Double.isFinite(totalFat)?COTTAGE:null;
    }
    public static String catalogId(String name,double totalFat,String brand){
        String direct=catalogId(name,totalFat);if(direct!=null)return direct;
        String value=folded(name);
        if(brand!=null)for(String label:brand.split(",")){
            String prefix=folded(label);
            if(!prefix.isEmpty()&&value.startsWith(prefix+" ")){
                String matched=catalogId(value.substring(prefix.length()).trim(),totalFat);if(matched!=null)return matched;
            }
        }
        return null;
    }
    public static String catalogId(JSONObject food){
        if(food==null)return null;
        JSONObject nutrition=food.optJSONObject("nutrition");
        double fat=nutrition==null?Double.NaN:nutrition.optDouble("fat_g",Double.NaN);
        if(nutrition!=null&&nutrition.has("fat_g")&&!nutrition.isNull("fat_g")&&!Double.isFinite(fat))return null;
        return catalogId(food.optString("name"),fat,food.optString("id").startsWith("SCAN")?food.optString("alias"):null);
    }
    public static JSONObject apply(JSONObject food){
        if(food==null)return null;
        try{
            JSONObject copy=new JSONObject(food.toString());
            if(catalogId(copy)!=null){copy.put("status","basic").put("points",0).put("estimatedPoints100",0);}
            return copy;
        }catch(Exception e){throw new IllegalArgumentException("Lebensmittel konnte nicht geprüft werden.",e);}
    }
}
