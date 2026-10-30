package de.inrouder.fitrocket;

import org.json.JSONObject;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

/**
 * Decimal FitRocket food ledger.
 * One decimal place is stored/added without losing a small (e.g. 0.2 P) serving.
 * Existing integer meal and favorite records remain compatible.
 */
public final class FitRocketPointLedger {
    private FitRocketPointLedger() {}

    public static double tenth(double number) {
        if (!Double.isFinite(number) || Math.abs(number)>200000)
            throw new IllegalArgumentException("Ungültiger Punktwert.");
        return Math.round(number*10.0)/10.0;
    }

    public static double validPoints(double number) {
        if(!Double.isFinite(number)||number<0||number>2000)
            throw new IllegalArgumentException("Punkte zwischen 0 und 2000 eingeben.");
        return tenth(number);
    }

    public static String display(double points) {
        double rounded=tenth(points);
        if(Math.abs(rounded-Math.rint(rounded))<0.00001)
            return String.format(Locale.GERMANY,"%.0f",rounded);
        return String.format(Locale.GERMANY,"%.1f",rounded);
    }

    /** Percent of reference portion (100 g = 1 P -> 20 g = 0.2 P). */
    public static double scaled(double referencePoints,double referenceAmount,double amount) {
        validPoints(referencePoints);
        if(!Double.isFinite(referenceAmount)||referenceAmount<=0||referenceAmount>10000||
           !Double.isFinite(amount)||amount<=0||amount>10000)
            throw new IllegalArgumentException("Ungültige Referenzmenge oder Portion.");
        return validPoints(referencePoints*amount/referenceAmount);
    }

    public static double food(JSONObject food,double grams) {
        if(food==null)throw new IllegalArgumentException("Lebensmittel fehlt.");
        if(!Double.isFinite(grams)||grams<=0||grams>10000)throw new IllegalArgumentException("Menge");
        if("basic".equals(food.optString("status")))return 0.0;
        JSONObject nutrition=food.optJSONObject("nutrition");
        if(nutrition==null)throw new IllegalArgumentException("Nährwerte fehlen.");
        double factor=grams/100.0;
        return validPoints(FitRocketMath.foodPointsPrecise(
            nutrition.optDouble("kcal",Double.NaN)*factor,
            nutrition.optDouble("saturatedFat_g",Double.NaN)*factor,
            nutrition.optDouble("sugar_g",Double.NaN)*factor,
            nutrition.optDouble("protein_g",Double.NaN)*factor));
    }

    public static double value(JSONObject meal) {
        if(meal==null)return 0;
        return validPoints(meal.optDouble("points",0));
    }

    public static double used(List<JSONObject> meals,String date) {
        long tenths=0;
        for(JSONObject meal:meals) {
            if(date.equals(meal.optString("date")))tenths+=Math.round(value(meal)*10);
        }
        return tenths/10.0;
    }

    public static double dailyTransfer(int budget,double used) {
        return tenth(Math.min(4.0,Math.max(0,budget-used)));
    }

    public static double weekRollovers(LocalDate day,int budget,ToDoubleFunction<LocalDate> used) {
        double result=0;
        for(LocalDate d=FitRocketWeeklyWallet.weekMonday(day);d.isBefore(day);d=d.plusDays(1))
            result+=dailyTransfer(budget,used.applyAsDouble(d));
        return tenth(result);
    }

    public static double weekSpent(LocalDate day,int budget,ToDoubleFunction<LocalDate> used) {
        double result=0;
        for(LocalDate d=FitRocketWeeklyWallet.weekMonday(day);!d.isAfter(day);d=d.plusDays(1))
            result+=Math.max(0,used.applyAsDouble(d)-budget);
        return tenth(result);
    }

    public static double weeklyAvailable(int initial,double transferred,int activity,double spent) {
        return tenth(Math.max(0,initial+transferred+activity-spent));
    }
}
