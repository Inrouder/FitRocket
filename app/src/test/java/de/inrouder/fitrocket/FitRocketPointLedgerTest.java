package de.inrouder.fitrocket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class FitRocketPointLedgerTest {

    @Test public void gramsCanProduceTwoTenthsInsteadOfZero() {
        assertEquals(0.2,FitRocketPointLedger.scaled(1,100,20),0.0001);
        assertEquals(1.0,FitRocketPointLedger.scaled(1,100,100),0.0001);
        assertEquals(1.5,FitRocketPointLedger.scaled(1,100,150),0.0001);
    }

    @Test public void quickPortionAndExactGramsMatchForFeta()throws Exception {
        JSONObject feta=new JSONObject().put("name","Feta").put("status","portion")
            .put("portion","1 Portion (50g)").put("points",4);
        double ref=FitRocketFoodDisplay.portionAmount(feta);
        assertEquals(50,ref,0.0001);
        assertEquals(4,FitRocketPointLedger.scaled(4,ref,50),0.0001);
        assertEquals(1.6,FitRocketPointLedger.scaled(4,ref,20),0.0001);
        assertEquals(2,FitRocketPointLedger.scaled(4,ref,25),0.0001);
    }

    @Test public void handfulAndDecimalReferenceWeightsAreParsed()throws Exception {
        JSONObject nuts=new JSONObject().put("portion","1 Handvoll (25g)")
            .put("category","Nüsse").put("status","portion").put("points",3);
        assertEquals(25,FitRocketFoodDisplay.portionAmount(nuts),0.01);
        assertEquals(1.2,FitRocketPointLedger.scaled(3,25,10),0.0001);
        JSONObject milk=new JSONObject().put("portion","1 Glas (250ml)")
            .put("category","Milch & Käse").put("status","portion");
        assertEquals("ml",FitRocketFoodDisplay.unit(milk));
        assertEquals(250,FitRocketFoodDisplay.portionAmount(milk),0.01);
    }

    @Test public void unknownPortionWeightIsNotGuessed()throws Exception {
        JSONObject food=new JSONObject().put("portion","Ein Stück nach Hunger")
            .put("status","portion");
        assertTrue(Double.isNaN(FitRocketFoodDisplay.portionAmount(food)));
    }

    @Test public void foodNutrientsUseReferencePointsForPreciseEntry()throws Exception {
        JSONObject nutritional=new JSONObject()
          .put("status","nutrition")
          .put("nutrition",new JSONObject().put("kcal",50)
            .put("saturatedFat_g",0).put("sugar_g",0).put("protein_g",2));
        int hundred=FitRocketMath.foodPoints(50,0,0,2);
        assertEquals(1,hundred);
        assertEquals(0.2,FitRocketPointLedger.food(nutritional,20),0.0001);
    }

    @Test public void decimalMealsAddToDailyAndWeeklyBalances()throws Exception {
        ArrayList<JSONObject> meals=new ArrayList<>();
        meals.add(new JSONObject().put("date","2026-10-27").put("points",0.2));
        meals.add(new JSONObject().put("date","2026-10-27").put("points",1.6));
        meals.add(new JSONObject().put("date","2026-10-27").put("points",4));
        assertEquals(5.8,FitRocketPointLedger.used(meals,"2026-10-27"),0.0001);
        assertEquals("5,8",FitRocketPointLedger.display(5.8));
        assertEquals("2",FitRocketPointLedger.display(2));
    }

    @Test public void fourPointCarryoverCapsFractionalSavings() {
        LocalDate tue=LocalDate.of(2026,10,27);
        assertEquals(4,FitRocketPointLedger.dailyTransfer(23,18.5),0.0001);
        assertEquals(0.2,FitRocketPointLedger.dailyTransfer(23,22.8),0.0001);
        assertEquals(0.2,FitRocketPointLedger.weekRollovers(tue,23,
            d->22.8),0.0001);
        assertEquals(0.4,FitRocketPointLedger.weekSpent(tue,23,
            d->d.equals(tue)?23.4:0),0.0001);
        assertEquals(28.7,FitRocketPointLedger.weeklyAvailable(28,0.9,1,1.2),0.0001);
    }

    @Test public void decimalFavoritesCanBeRescaledAndSaved()throws Exception {
        JSONObject fav=new JSONObject().put("name","Portion").put("points",1)
            .put("referenceQuantity",100);
        assertEquals(0.2,FitRocketFavoriteMath.scaledPointsPrecise(
            fav,null,100,20,Collections.emptyList()),0.0001);
        assertEquals(2,FitRocketFavoriteMath.scaledPointsPrecise(
            fav,null,100,200,Collections.emptyList()),0.0001);
    }

    @Test public void backupAcceptsTenthPointsAndRejectsNegativeFractions()throws Exception {
        JSONObject valid=new JSONObject().put("schemaVersion",7)
            .put("meals",new JSONArray().put(new JSONObject()
                .put("name","Nüsse").put("date","2026-10-27").put("points",0.2)))
            .put("weights",new JSONArray());
        assertTrue(FitRocketBackupCheck.preview(valid).contains("Mahlzeiten: 1"));
        valid.getJSONArray("meals").getJSONObject(0).put("points",-0.2);
        try{
            FitRocketBackupCheck.preview(valid);fail("Negative points accepted");
        }catch(IllegalArgumentException expected){}
    }
}
