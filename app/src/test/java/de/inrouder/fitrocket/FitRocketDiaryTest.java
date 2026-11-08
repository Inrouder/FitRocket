package de.inrouder.fitrocket;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketDiaryTest {
    @Test public void movingPreservesObjectAndAllFieldsExceptDateAndGroup() throws Exception {
        JSONObject meal=FitRocketQuickMeal.create("Lasagne",5.5,2,"2026-11-08","Mittagessen");
        meal.put("note","behalten").put("aiIngredients",new JSONArray().put("Tomaten"));
        JSONObject expected=new JSONObject(meal.toString());
        expected.put("date","2024-02-29").put("group","Abendessen");
        java.util.List<JSONObject> meals=Collections.singletonList(meal);
        FitRocketDiary.move(meal,LocalDate.of(2024,2,29),"Abendessen",LocalDate.of(2026,11,8));
        assertSame(meal,meals.get(0));
        assertEquals(1,meals.size());
        JSONObject restored=new JSONObject(meal.toString());
        assertEquals(expected.length(),restored.length());
        java.util.Iterator<String> keys=expected.keys();
        while(keys.hasNext()) {
            String key=keys.next();
            assertEquals(key,expected.get(key).toString(),restored.get(key).toString());
        }
        assertEquals(0,FitRocketPointLedger.used(meals,"2026-11-08"),0);
        assertEquals(11,FitRocketPointLedger.used(meals,"2024-02-29"),0);
    }

    @Test public void movingAcrossWeeksRecalculatesBothAccounts() throws Exception {
        JSONObject meal=FitRocketQuickMeal.create("Gericht",25,1,"2026-11-02","Snack");
        java.util.List<JSONObject> meals=Collections.singletonList(meal);
        LocalDate thisWeek=LocalDate.of(2026,11,8),lastWeek=thisWeek.minusWeeks(1);
        assertEquals(2,FitRocketPointLedger.weekSpent(thisWeek,23,
            d->FitRocketPointLedger.used(meals,d.toString())),0);
        FitRocketDiary.move(meal,LocalDate.of(2026,10,31),"Frühstück",thisWeek);
        assertEquals(0,FitRocketPointLedger.weekSpent(thisWeek,23,
            d->FitRocketPointLedger.used(meals,d.toString())),0);
        assertEquals(2,FitRocketPointLedger.weekSpent(lastWeek,23,
            d->FitRocketPointLedger.used(meals,d.toString())),0);
    }

    @Test public void sameDayMoveAndZeroPointsRemainUnchanged() throws Exception {
        JSONObject meal=new JSONObject().put("date","2026-11-08").put("group","Snack")
            .put("foodId","ZERO").put("points",0);
        FitRocketDiary.move(meal,LocalDate.of(2026,11,8),"Getränke",LocalDate.of(2026,11,8));
        assertEquals("Getränke",meal.getString("group"));
        assertEquals(0,meal.getDouble("points"),0);
        assertEquals("ZERO",meal.getString("foodId"));
    }

    @Test public void invalidMoveDoesNotChangeOriginal() throws Exception {
        JSONObject meal=new JSONObject().put("date","2020-01-01").put("group","Snack").put("points",5);
        String original=meal.toString();
        LocalDate today=LocalDate.of(2026,11,8);
        for(LocalDate day:Arrays.asList(null,today.plusDays(1))) {
            try { FitRocketDiary.move(meal,day,"Snack",today);fail("Invalid date accepted"); }
            catch(IllegalArgumentException expected) { assertEquals(original,meal.toString()); }
        }
        try { FitRocketDiary.move(meal,today,"Unbekannt",today);fail("Invalid group accepted"); }
        catch(IllegalArgumentException expected) { assertEquals(original,meal.toString()); }
    }

    @Test public void selectedDayWorksAcrossYearAndWeekBoundaries() {
        for(String day:Arrays.asList("1999-12-31","2024-02-29","2026-10-25"))
            assertEquals(day,FitRocketDiary.bookingDate(null,LocalDate.parse(day)));
        assertEquals("2026-10-26",FitRocketDiary.bookingDate("2026-10-26",LocalDate.of(2026,11,8)));
    }

    @Test public void historicalQuickMealAndCorrectionPreserveBackupFields() throws Exception {
        JSONObject meal=FitRocketQuickMeal.create("Lasagne",5.5,2,
            FitRocketDiary.bookingDate(null,LocalDate.of(2020,2,29)),"Abendessen");
        meal.put("customFutureField","keep");
        FitRocketDiary.editPoints(meal,null,7.5);
        JSONObject restored=new JSONObject(meal.toString());
        assertEquals("2020-02-29",restored.getString("date"));
        assertEquals("Abendessen",restored.getString("group"));
        assertEquals("keep",restored.getString("customFutureField"));
        assertEquals(7.5,restored.getDouble("points"),0.001);
        assertEquals(3.75,restored.getDouble("pointsPerPortion"),0.001);
        assertEquals(2,restored.getDouble("portions"),0.001);
    }

    @Test public void correctionsRecalculateDailyWeeklyAndLeaveOtherWeeksAlone() throws Exception {
        LocalDate sunday=LocalDate.of(2026,11,8);
        JSONObject meal=FitRocketQuickMeal.create("Gericht",25,1,"2026-11-02","Mittagessen");
        JSONObject older=FitRocketQuickMeal.create("Alt",100,1,"2026-10-25","Snack");
        java.util.List<JSONObject> meals=Arrays.asList(meal,older);
        assertEquals(2,FitRocketPointLedger.weekSpent(sunday,23,
            d->FitRocketPointLedger.used(meals,d.toString())),0.001);
        FitRocketDiary.editPoints(meal,null,21.5);
        assertEquals(21.5,FitRocketPointLedger.used(meals,"2026-11-02"),0.001);
        assertEquals(0,FitRocketPointLedger.weekSpent(sunday,23,
            d->FitRocketPointLedger.used(meals,d.toString())),0.001);
        assertEquals(21.5,FitRocketPointLedger.weekRollovers(sunday,23,
            d->FitRocketPointLedger.used(meals,d.toString())),0.001);
        assertEquals(100,older.getDouble("points"),0.001);
    }

    @Test public void zeroPointsStayZeroForFoodRecipeAndManualCorrection() throws Exception {
        JSONObject food=new JSONObject().put("id","ZERO").put("status","basic");
        String unchanged=food.toString();
        assertEquals(0,FitRocketPointLedger.food(food,100),0);
        assertEquals(0,FitRocketPointLedger.food(food,10000),0);
        assertEquals(0,FitRocketDiary.points(food,42),0);
        JSONObject meal=new JSONObject().put("foodId","ZERO").put("date","2020-01-01").put("points",0);
        FitRocketDiary.editPoints(meal,food,42);
        assertEquals(0,meal.getDouble("points"),0);
        JSONObject recipe=new JSONObject().put("servings",1).put("ingredients",
            new JSONArray().put(new JSONObject().put("id","ZERO").put("grams",10000)));
        assertEquals(0,FitRocketRecipe.pointsPerServingPrecise(recipe,Collections.singletonList(food)),0);
        assertEquals(unchanged,food.toString());
    }

    @Test public void invalidCorrectionsDoNotMutateBooking() throws Exception {
        JSONObject meal=new JSONObject().put("points",3).put("date","2020-01-01");
        String unchanged=meal.toString();
        for(double invalid:new double[]{-1,Double.NaN,Double.POSITIVE_INFINITY,2001}) {
            try { FitRocketDiary.editPoints(meal,null,invalid); fail("Invalid correction accepted"); }
            catch(IllegalArgumentException expected) { assertEquals(unchanged,meal.toString()); }
        }
    }
}

