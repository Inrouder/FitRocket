package de.inrouder.fitrocket;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketQuickMealTest {
    @Test public void fourPortionsMultiplySuppliedPoints() throws Exception {
        JSONObject meal=FitRocketQuickMeal.create("Lasagne",5.5,4,"2026-11-02","Abendessen");
        assertEquals(22.0,meal.getDouble("points"),0.001);
        assertEquals(5.5,meal.getDouble("pointsPerPortion"),0.001);
        assertEquals(4.0,meal.getDouble("portions"),0.001);
        assertEquals("Abendessen",meal.getString("group"));
        assertEquals("2026-11-02",meal.getString("date"));
        assertEquals("",meal.getString("foodId"));
    }
    @Test public void fractionalPortionsAndDecimalPointsWork() throws Exception {
        JSONObject meal=FitRocketQuickMeal.create("Suppe",2.4,1.5,"2026-11-02","Mittagessen");
        assertEquals(3.6,meal.getDouble("points"),0.001);
    }
    @Test public void manuallyEnteredZeroIsAllowedWithoutChangingZeroPointsCatalog() throws Exception {
        JSONObject meal=FitRocketQuickMeal.create("Spontane Mahlzeit",0,2,"2026-11-02","Snack");
        assertEquals(0,meal.getDouble("points"),0.001);
        assertFalse(meal.has("status"));
    }
    @Test(expected=IllegalArgumentException.class)
    public void blankNameRejected() throws Exception {
        FitRocketQuickMeal.create(" ",1,1,"2026-11-02","Snack");
    }
    @Test(expected=IllegalArgumentException.class)
    public void negativePortionsRejected() throws Exception {
        FitRocketQuickMeal.create("Gericht",1,-1,"2026-11-02","Snack");
    }
    @Test(expected=IllegalArgumentException.class)
    public void overflowRejected() throws Exception {
        FitRocketQuickMeal.create("Gericht",2000,2,"2026-11-02","Snack");
    }
}
