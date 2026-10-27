package de.inrouder.fitrocket;

import org.json.JSONObject;
import java.util.Locale;

/** Human-facing portion descriptions. Does not change food nutrition or points. */
public final class FitRocketFoodDisplay {
    private FitRocketFoodDisplay(){}

    public static boolean isDrink(JSONObject food) {
        if (food == null) return false;
        String category=food.optString("category","").trim().toLowerCase(Locale.ROOT);
        if(category.equals("getränke") || category.startsWith("getränke /")
            || category.startsWith("getränke &"))return true;
        // Example: milk catalogued under "Milch & Käse" as "1 Glas (250ml)".
        // Do not inspect nutritional entries containing a generic "beim Getränk"
        // instruction; that legacy text also appears on solid foods.
        if("portion".equals(food.optString("status")))
            return java.util.regex.Pattern.compile("\\d+(?:[.,]\\d+)?\\s*ml\\b",
                java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(food.optString("portion","")).find();
        return false;
    }

    public static String unit(JSONObject food) {
        return isDrink(food) ? "ml" : "g";
    }

    public static String referencePortion(JSONObject food) {
        if (food == null) return "";
        String portion=food.optString("portion","");
        if ("nutrition".equals(food.optString("status"))) {
            // Older food catalog uses the same mixed label for all solid and
            // liquid products; derive a readable unit from actual category.
            return "100 "+unit(food);
        }
        return portion;
    }

    public static String pointsLabel(JSONObject food) {
        if (food == null) return "";
        if ("nutrition".equals(food.optString("status"))) {
            return "ca. "+food.optInt("estimatedPoints100")+" P / 100 "+unit(food);
        }
        return food.isNull("points")?"Punkte wählen":food.optInt("points")+" P";
    }

    public static String quantityLabel(JSONObject food,double amount) {
        return String.format(Locale.GERMAN,"%s %s",
            (amount == Math.rint(amount))
                ?String.format(Locale.GERMAN,"%.0f",amount)
                :String.format(Locale.GERMAN,"%.1f",amount),
            unit(food));
    }
}
