package de.inrouder.fitrocket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class FitRocketFavoriteMathTest {
    private JSONObject pasta() throws Exception {
        return new JSONObject().put("id","CALC001").put("name","Vollkornnudeln")
            .put("status","nutrition").put("category","Beilagen")
            .put("nutrition",new JSONObject().put("kcal",130)
                .put("saturatedFat_g",0).put("sugar_g",0).put("protein_g",5));
    }

    @Test public void nutritionFavoriteRecalculatesForEachGram() throws Exception{
        JSONObject f=pasta();
        JSONObject saved=new JSONObject().put("foodId","CALC001")
            .put("points",6).put("referenceQuantity",150);
        int for100=FitRocketFavoriteMath.scaledPoints(saved,f,150,100,Arrays.asList(f));
        int for250=FitRocketFavoriteMath.scaledPoints(saved,f,150,250,Arrays.asList(f));
        assertEquals(FitRocketAiRules.points(100,130,0,0,5),for100);
        assertEquals(FitRocketAiRules.points(250,130,0,0,5),for250);
        assertTrue(for250>for100);
    }

    @Test public void legacyQuantityIsRecoveredFromMealName() throws Exception{
        JSONObject old=new JSONObject().put("name","Nudeln (160 g)").put("points",7);
        assertEquals(160,FitRocketFavoriteMath.referenceQuantity(old,null),0.001);
        assertEquals("Nudeln",FitRocketFavoriteMath.cleanName(old.getString("name")));
    }

    @Test public void oldFavoriteWithoutReferenceMustAskUser()throws Exception{
        JSONObject old=new JSONObject().put("name","Meine Suppe").put("points",8);
        assertTrue(Double.isNaN(FitRocketFavoriteMath.referenceQuantity(old,null)));
    }

    @Test public void manualFavoriteScalesFromOriginalPortion()throws Exception{
        JSONObject saved=new JSONObject().put("points",8);
        assertEquals(4,FitRocketFavoriteMath.scaledPoints(saved,null,200,100,Collections.emptyList()));
        assertEquals(16,FitRocketFavoriteMath.scaledPoints(saved,null,200,400,Collections.emptyList()));
    }

    @Test public void aiRecipeScalesIngredientsIndividually()throws Exception{
        JSONArray parts=new JSONArray()
            .put(new JSONObject().put("name","Mehl").put("food_id","")
                .put("grams",100).put("kcal_100g",370)
                .put("saturated_fat_100g",0).put("sugar_100g",0).put("protein_100g",10))
            .put(new JSONObject().put("name","Butter").put("food_id","")
                .put("grams",20).put("kcal_100g",740)
                .put("saturated_fat_100g",50).put("sugar_100g",0).put("protein_100g",1));
        JSONObject saved=new JSONObject().put("points",10).put("aiIngredients",parts);
        assertEquals(120,FitRocketFavoriteMath.referenceQuantity(saved,null),0.001);
        JSONArray less=FitRocketFavoriteMath.scaledIngredients(saved,120,60);
        assertEquals(50,less.getJSONObject(0).getDouble("grams"),0.001);
        assertEquals(10,less.getJSONObject(1).getDouble("grams"),0.001);
        int half=FitRocketFavoriteMath.scaledPoints(saved,null,120,60,Collections.emptyList());
        int doubleAmount=FitRocketFavoriteMath.scaledPoints(saved,null,120,240,Collections.emptyList());
        assertTrue(doubleAmount>half);
    }

    @Test public void originalPortionMultiplierIsParsed()throws Exception{
        JSONObject food=new JSONObject().put("portion","1 Portion (150g)").put("status","portion");
        JSONObject meal=new JSONObject().put("name","Nudeln × 0.5").put("points",3);
        assertEquals(75,FitRocketFavoriteMath.referenceQuantity(meal,food),0.01);
    }

    @Test(expected=IllegalArgumentException.class)
    public void zeroQuantityIsRejected()throws Exception{
        FitRocketFavoriteMath.scaledPoints(new JSONObject().put("points",5),null,
            100,0,Collections.emptyList());
    }
}
