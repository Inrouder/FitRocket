package de.inrouder.fitrocket;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketFoodDisplayTest {
    private JSONObject food(String name,String category,String status) throws Exception {
        return new JSONObject().put("name",name).put("category",category)
            .put("status",status).put("estimatedPoints100",3)
            .put("portion","100 g (beim Getränk 100 ml)").put("points",JSONObject.NULL);
    }

    @Test public void noodlesShowOnlyGrams() throws Exception {
        JSONObject pasta=food("Vollkornnudeln gekocht","Beilagen","nutrition");
        assertEquals("100 g",FitRocketFoodDisplay.referencePortion(pasta));
        assertEquals("ca. 3 P / 100 g",FitRocketFoodDisplay.pointsLabel(pasta));
        assertEquals("150 g",FitRocketFoodDisplay.quantityLabel(pasta,150));
    }

    @Test public void drinksShowOnlyMilliliters() throws Exception {
        JSONObject juice=food("Orangensaft","Getränke","nutrition");
        assertEquals("100 ml",FitRocketFoodDisplay.referencePortion(juice));
        assertEquals("ca. 3 P / 100 ml",FitRocketFoodDisplay.pointsLabel(juice));
        assertEquals("250 ml",FitRocketFoodDisplay.quantityLabel(juice,250));
    }

    @Test public void mixedDrinksCategoryUsesMilliliters() throws Exception {
        JSONObject coconut=food("Kokosmilch Dose","Getränke / Kochen","nutrition");
        assertEquals("100 ml",FitRocketFoodDisplay.referencePortion(coconut));
    }

    @Test public void solidWithWaterInNameRemainsGrams() throws Exception {
        JSONObject watermelon=food("Wassermelone","Obst","nutrition");
        assertEquals("100 g",FitRocketFoodDisplay.referencePortion(watermelon));
    }

    @Test public void existingPortionIsPreserved() throws Exception {
        JSONObject noodles=food("Nudeln gekocht","Beilagen","portion");
        noodles.put("portion","1 Portion (150g)");
        noodles.put("points",5);
        assertEquals("1 Portion (150g)",FitRocketFoodDisplay.referencePortion(noodles));
        assertEquals("5 P",FitRocketFoodDisplay.pointsLabel(noodles));
    }

    @Test public void localeUsesDecimalComma() throws Exception {
        JSONObject rice=food("Reis","Beilagen","nutrition");
        assertEquals("125,5 g",FitRocketFoodDisplay.quantityLabel(rice,125.5));
    }
}
