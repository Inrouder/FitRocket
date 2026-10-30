package de.inrouder.fitrocket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

/** Regression tests: explicit ZeroPoints foods ALWAYS remain zero. */
public class FitRocketRecipeTest {
  private JSONObject food(String id,String name,String status,double kcal) throws Exception {
    JSONObject x=new JSONObject().put("id",id).put("name",name).put("status",status);
    if("nutrition".equals(status))
      x.put("nutrition",new JSONObject().put("kcal",kcal)
        .put("saturatedFat_g",0).put("sugar_g",0).put("protein_g",0));
    return x;
  }
  private JSONObject recipe(int servings,JSONObject...parts)throws Exception {
    JSONArray arr=new JSONArray();
    for(JSONObject part:parts)arr.put(part);
    return new JSONObject().put("servings",servings).put("ingredients",arr);
  }
  private JSONObject part(String id,double grams)throws Exception{
    return new JSONObject().put("id",id).put("grams",grams);
  }

  @Test public void explicitZeroPointFoodsStayZeroAtEveryAmount()throws Exception {
    JSONObject tomato=food("ZERO","Tomaten","basic",0);
    List<JSONObject> catalog=Arrays.asList(tomato);
    assertEquals(0,FitRocketPointLedger.food(tomato,10000),0.0001);
    assertEquals(0,FitRocketRecipe.pointsPerServingPrecise(
      recipe(4,part("ZERO",6000)),catalog),0.0001);
  }

  @Test public void fourPortionsDivideTheFullDishWithoutIngredientRounding()throws Exception {
    JSONObject pasta=food("PASTA","Weizennudeln gekocht","nutrition",150);
    JSONObject vegetables=food("VEG","Gemüse","basic",0);
    List<JSONObject> catalog=Arrays.asList(pasta,vegetables);
    double one=FitRocketRecipe.pointsPerServingPrecise(
      recipe(4,part("PASTA",400),part("VEG",800)),catalog);
    assertEquals(FitRocketMath.foodPointsPrecise(150,0,0,0),one,0.0001);
    assertEquals(0,FitRocketMath.foodPointsPrecise(0,0,0,0),0.0001);
  }

  @Test public void decimalScoringDoesNotRound100gToZeroFirst()throws Exception {
    JSONObject x=food("TEST","Kleine Portion","nutrition",15);
    double one=FitRocketPointLedger.food(x,100);
    assertTrue(one>0);
    assertEquals(FitRocketMath.foodPointsPrecise(75,0,0,0),
      FitRocketPointLedger.food(x,500),0.0001);
  }

  @Test public void perRecipeOverrideDoesNotChangeZeroPointCatalog()throws Exception {
    JSONObject tomato=food("ZERO","Tomaten","basic",0);
    JSONObject special=part("ZERO",100).put("nutritionOverride",
      new JSONObject().put("kcal",100).put("saturatedFat_g",1)
        .put("sugar_g",0).put("protein_g",0));
    assertTrue(FitRocketRecipe.pointsPerServingPrecise(
      recipe(1,special),Arrays.asList(tomato))>0);
    assertEquals(0,FitRocketRecipe.pointsPerServingPrecise(
      recipe(1,part("ZERO",100)),Arrays.asList(tomato)),0.0001);
  }

  @Test public void manualIngredientsResolveExistingFoodIds()throws Exception {
    JSONObject tomato=food("ZERO","Tomaten","basic",0);
    JSONArray items=FitRocketRecipe.linkManualIngredients(
      "250 g Tomaten\nTomaten 50 g",Arrays.asList(tomato));
    assertEquals(2,items.length());
    assertEquals("ZERO",items.getJSONObject(0).getString("id"));
    assertEquals(0,FitRocketRecipe.pointsPerServingPrecise(
      new JSONObject().put("servings",2).put("ingredients",items),
      Arrays.asList(tomato)),0.0001);
  }
  @Test public void editingOldManualRecipeFromTwoToFourPortionsHalvesPoints() {
    assertEquals(6.0,FitRocketRecipe.rescaleManualPoints(12,2,4),0.0001);
    assertEquals(24.0,FitRocketRecipe.rescaleManualPoints(12,2,1),0.0001);
  }
  @Test(expected=IllegalArgumentException.class)
  public void unknownManualIngredientMustNotDefaultToZero()throws Exception{
    FitRocketRecipe.linkManualIngredients("300 g Mysteriöse Zutat",Arrays.asList(
      food("ZERO","Tomaten","basic",0)));
  }
}
