package de.inrouder.fitrocket;

import java.time.LocalDate;
import org.json.JSONObject;

/** Rules shared by every diary input path; catalog records are read-only. */
public final class FitRocketDiary {
    private FitRocketDiary() {}

    public static String bookingDate(String explicitDate, LocalDate selectedDay) {
        return (explicitDate == null ? selectedDay : LocalDate.parse(explicitDate)).toString();
    }

    public static double points(JSONObject food, double requested) {
        double valid = FitRocketPointLedger.validPoints(requested);
        return food != null && "basic".equals(food.optString("status")) ? 0 : valid;
    }

    /** Correct only this booking, preserving its date, identity and unknown backup fields. */
    public static void editPoints(JSONObject meal, JSONObject food, double requested) throws Exception {
        double value = points(food, requested);
        meal.put("points", value);
        meal.put("quality", "Manuell korrigiert");
        if (meal.has("pointsPerPortion")) {
            double portions = meal.optDouble("portions", 1);
            if (Double.isFinite(portions) && portions > 0)
                meal.put("pointsPerPortion", value / portions);
        }
    }
}
