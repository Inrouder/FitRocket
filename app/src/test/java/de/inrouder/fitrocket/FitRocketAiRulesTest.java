package de.inrouder.fitrocket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

/** Verifies that AI output never supplies the actual FitRocket points. */
public class FitRocketAiRulesTest {
    private JSONObject food(String id,String name,String status,double kcal) throws Exception {
        JSONObject data=new JSONObject();
        data.put("id",id).put("name",name).put("status",status);
        if("nutrition".equals(status)){
            JSONObject n=new JSONObject();
            n.put("kcal",kcal);n.put("saturatedFat_g",0);
            n.put("sugar_g",0);n.put("protein_g",0);
            data.put("nutrition",n);
        }
        return data;
    }

    private JSONObject item(String name,String foodId,int grams,double kcal) throws Exception {
        return new JSONObject()
            .put("name",name).put("food_id",foodId).put("grams",grams)
            .put("kcal_100g",kcal).put("saturated_fat_100g",0)
            .put("sugar_100g",0).put("protein_100g",0)
            .put("confidence","niedrig").put("note","KI-Schätzung");
    }

    private JSONObject proposal(int servings,JSONObject... parts)throws Exception{
        JSONArray ingredients=new JSONArray();
        for(JSONObject part:parts)ingredients.put(part);
        return new JSONObject()
            .put("title","Mahlzeit")
            .put("summary","unsicher")
            .put("instructions","kochen")
            .put("minutes",20)
            .put("group","Mittagessen")
            .put("servings",servings)
            .put("confidence","niedrig")
            .put("uncertainty","Mengen geraten")
            .put("ingredients",ingredients);
    }

    @Test public void exactCatalogMatchAllowsZeroPoints()throws Exception{
        JSONObject basis=food("ZP214","Tomaten","basic",0);
        JSONObject x=proposal(1,item("Tomaten","ZP214",250,200));
        assertNotNull(FitRocketAiRules.matchCatalog("ZP214","Tomaten",Arrays.asList(basis)));
        assertEquals(0,FitRocketAiRules.pointsPerServing(x,Arrays.asList(basis)));
    }

    @Test public void forgedCatalogIdCannotForceZero()throws Exception{
        JSONObject basis=food("ZP214","Tomaten","basic",0);
        JSONObject x=proposal(1,item("Schokoladenkuchen","ZP214",200,400));
        assertNull(FitRocketAiRules.matchCatalog("ZP214","Schokoladenkuchen",Arrays.asList(basis)));
        assertEquals(FitRocketAiRules.points(200,400,0,0,0),
            FitRocketAiRules.pointsPerServing(x,Arrays.asList(basis)));
        assertTrue(FitRocketAiRules.pointsPerServing(x,Arrays.asList(basis))>0);
    }

    @Test public void catalogNutritionOverridesModelEstimate()throws Exception{
        JSONObject rice=food("CALC012","Basmatireis gekocht","nutrition",130);
        JSONObject x=proposal(1,item("Basmatireis gekocht","CALC012",200,600));
        assertEquals(FitRocketAiRules.points(200,130,0,0,0),
            FitRocketAiRules.pointsPerServing(x,Arrays.asList(rice)));
    }

    @Test public void exactNameWithoutIdReusesExistingBasicFood()throws Exception{
        JSONObject basis=food("ZP214","Tomaten","basic",0);
        JSONObject x=proposal(1,item("Tomaten","",200,100));
        assertSame(basis,FitRocketAiRules.existingByName("tomaten",Arrays.asList(basis)));
        assertEquals(0,FitRocketAiRules.pointsPerServing(x,Arrays.asList(basis)));
    }

    @Test public void rejectingCatalogMatchKeepsOriginalZeroPointFoodUntouched()throws Exception{
        JSONObject zero=food("ZP214","Tomaten","basic",0);
        JSONObject overridden=item("Tomaten","ZP214",100,140)
            .put("catalogOptOut",true);
        JSONObject custom=proposal(1,overridden);
        assertTrue(FitRocketAiRules.pointsPerServingPrecise(custom,Arrays.asList(zero))>0);
        assertEquals(0,FitRocketAiRules.pointsPerServingPrecise(
            proposal(1,item("Tomaten","ZP214",250,400)),Arrays.asList(zero)),0.0001);
    }

    @Test public void namesMustMatchExactlyNotBySubstring()throws Exception{
        JSONObject basis=food("N1","Vollkornnudeln gekocht","nutrition",140);
        assertNull(FitRocketAiRules.existingByName("Nudeln gekocht",Arrays.asList(basis)));
    }

    @Test public void missingIdUsesExistingNutritionInsteadOfAiEstimate()throws Exception{
        JSONObject rice=food("R1","Basmatireis gekocht","nutrition",130);
        JSONObject x=proposal(4,item("basmatireis gekocht","",400,900));
        assertEquals(FitRocketAiRules.points(100,130,0,0,0),
            FitRocketAiRules.pointsPerServing(x,Arrays.asList(rice)));
    }

    @Test public void recipePointsAreCalculatedPerPortion()throws Exception{
        JSONObject x=proposal(4,item("Nudeln","",400,150));
        assertEquals(FitRocketAiRules.points(100,150,0,0,0),
            FitRocketAiRules.pointsPerServing(x,Collections.emptyList()));
    }

    @Test public void fullPancakeDishSumsAllIngredientsRatherThanJustOne()throws Exception{
        JSONObject egg=food("ZP071","Eier","basic",150);
        JSONObject pancake=proposal(1,
            item("Eier","ZP071",110,150),
            item("Mehl","",80,360),
            item("Milch","",120,65),
            item("Butter","",10,750));
        FitRocketAiRules.validateResponse(pancake,"meal");
        int sum=FitRocketAiRules.points(80,360,0,0,0)
               +FitRocketAiRules.points(120,65,0,0,0)
               +FitRocketAiRules.points(10,750,0,0,0);
        assertEquals(sum,FitRocketAiRules.pointsPerServing(pancake,Arrays.asList(egg)));
        assertTrue(sum>FitRocketAiRules.points(80,360,0,0,0));
    }

    @Test public void portionsScaleTotalRecipeButMealsRemainSingleServing()throws Exception{
        JSONObject recipe=proposal(2,item("Teig","",400,250),item("Öl","",20,900));
        int recipeTotal=FitRocketAiRules.pointsPerServing(
            FitRocketAiRules.validateResponse(recipe,"recipe"),Collections.emptyList());
        int expected=FitRocketAiRules.points(200,250,0,0,0)
               +FitRocketAiRules.points(10,900,0,0,0);
        assertEquals(expected,recipeTotal);
    }

    @Test public void newProductHasComputedNotModelPoints()throws Exception{
        JSONObject x=item("Erdnussmus","",100,650);
        JSONObject food=FitRocketAiRules.newFood(x,"AI");
        assertEquals("nutrition",food.getString("status"));
        assertTrue(food.getInt("estimatedPoints100")>0);
        assertTrue(food.isNull("points"));
        assertTrue(food.getString("id").startsWith("AI"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectsNegativeMacro()throws Exception{
        FitRocketAiRules.validateResponse(
            proposal(1,item("Butter","",80,-2)),"meal");
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectsTooManyIngredients()throws Exception{
        JSONObject x=proposal(1,item("Karotte","",100,30));
        JSONArray ingredients=x.getJSONArray("ingredients");
        for(int i=0;i<13;i++)ingredients.put(item("Apfel","",100,55));
        FitRocketAiRules.validateResponse(x,"recipe");
    }

    @Test public void plainManualNutritionDoesNotRequireInternet()throws Exception{
        assertEquals(FitRocketMath.foodPoints(250,5,6,9),
                     FitRocketAiRules.points(100,250,5,6,9));
    }
}
