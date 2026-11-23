package de.inrouder.fitrocket;

import org.json.*;
import org.junit.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.Assert.*;

public class FitRocketZeroPointsTest {
    private List<JSONObject> catalog()throws Exception {
        Path path=Path.of("src/main/assets/foods.json");if(!Files.exists(path))path=Path.of("app/src/main/assets/foods.json");
        JSONArray data=new JSONArray(Files.readString(path));List<JSONObject> foods=new ArrayList<>();
        for(int i=0;i<data.length();i++)foods.add(data.getJSONObject(i));return foods;
    }
    private JSONObject food(String name)throws Exception {
        return new JSONObject().put("id","SCAN12345678").put("name",name).put("status","nutrition").put("points",JSONObject.NULL)
            .put("estimatedPoints100",3).put("barcode","12345678").put("futureField","keep")
            .put("nutrition",new JSONObject().put("kcal",72).put("saturatedFat_g",0.2).put("sugar_g",3).put("protein_g",12).put("per",100));
    }
    @Test public void tomatoNamesAndQualifiedCottageNamesResolveToActualZeroCatalogRows()throws Exception {
        List<JSONObject> catalog=catalog();
        for(String name:new String[]{"Cocktailtomaten","Cocktail-Tomaten","Bio Cherrytomaten","Kirschtomaten","Tomaten frisch",
            "Körniger Frischkäse 0,5 % Fett","Körniger Frischkäse natur bis 0,5 % Fett","Hüttenkäse 0.4% Fett","Cottage Cheese 0,1 %","Körniger Frischkäse, natur (0,5 % Fett)"}){
            JSONObject known=FitRocketAiRules.existingByName(name,catalog);assertNotNull(name,known);assertEquals("basic",known.getString("status"));
            assertEquals(0,FitRocketPointLedger.food(known,100),0);assertEquals(0,FitRocketPointLedger.food(known,10000),0);
        }
    }
    @Test public void naturalVariantsStayZeroAtHundredGramsAndTenKilograms()throws Exception {
        for(String name:new String[]{"Cocktailtomaten","Cherrytomaten","Körniger Frischkäse 0,5 % Fett","Hüttenkäse 0.1% Fett"}){
            JSONObject old=food(name);String original=old.toString();JSONObject corrected=FitRocketZeroPoints.apply(old);
            assertEquals("basic",corrected.getString("status"));assertEquals(0,corrected.getInt("points"));assertEquals(0,corrected.getInt("estimatedPoints100"));
            assertEquals(0,FitRocketPointLedger.food(old,100),0);assertEquals(0,FitRocketPointLedger.food(corrected,10000),0);
            assertEquals("0 P",FitRocketFoodDisplay.pointsLabel(old));assertEquals(original,old.toString());
            assertEquals(old.getString("id"),corrected.getString("id"));assertEquals("keep",corrected.getString("futureField"));
            assertEquals(old.getJSONObject("nutrition").toString(),corrected.getJSONObject("nutrition").toString());
        }
    }
    @Test public void totalFatIsRequiredForUnqualifiedCottageAndNeverInferredFromSaturatedFat()throws Exception {
        assertNull(FitRocketZeroPoints.catalogId(food("Körniger Frischkäse")));
        JSONObject cottage=food("Körniger Frischkäse");cottage.getJSONObject("nutrition").put("fat_g",0.5);
        assertEquals(FitRocketZeroPoints.COTTAGE,FitRocketZeroPoints.catalogId(cottage));
        cottage.getJSONObject("nutrition").put("fat_g",0.5001);assertNull(FitRocketZeroPoints.catalogId(cottage));
        cottage.getJSONObject("nutrition").put("fat_g","invalid");assertNull(FitRocketZeroPoints.catalogId(cottage));
    }
    @Test public void higherFatAdditionsAndMisleadingPercentagesNeverBecomeZero()throws Exception {
        for(String name:new String[]{"Hüttenkäse 4 % Fett","Hüttenkäse 1 % Fett","Körniger Frischkäse 0,6 % Fett",
            "Frischkäse Doppelrahmstufe","Tomaten getrocknet in Öl","Cocktailtomaten mit Öl","Salat mit Cocktailtomaten",
            "Tomatensauce mit Zucker","Körniger Frischkäse 0,5 % Fett mit Sahne","Hüttenkäse light","Frischkäse 0,5 % Fett",
            "Hüttenkäse 0,5 % Fett i. Tr.","Schokoladenkuchen"}){
            JSONObject corrected=FitRocketZeroPoints.apply(food(name));assertEquals(name,"nutrition",corrected.getString("status"));assertTrue(name,FitRocketPointLedger.food(corrected,100)>0);
        }
        JSONObject contradictory=food("Hüttenkäse 0,5 % Fett");contradictory.getJSONObject("nutrition").put("fat_g",4);
        assertNull(FitRocketZeroPoints.catalogId(contradictory));
        assertNull(FitRocketZeroPoints.catalogId("Hüttenkäse",Double.POSITIVE_INFINITY));
    }
    @Test public void recipeAndAiPathsRespectZerosButRetainExplicitLocalOverrides()throws Exception {
        List<JSONObject> catalog=catalog();
        for(String name:new String[]{"Cocktailtomaten","Körniger Frischkäse 0,5 % Fett"}){
            JSONObject known=FitRocketAiRules.existingByName(name,catalog);
            JSONObject part=new JSONObject().put("id",known.getString("id")).put("grams",10000);
            JSONObject recipe=new JSONObject().put("servings",1).put("ingredients",new JSONArray().put(part));
            assertEquals(0,FitRocketRecipe.pointsPerServingPrecise(recipe,catalog),0);
            part.put("nutritionOverride",food(name).getJSONObject("nutrition"));assertTrue(FitRocketRecipe.pointsPerServingPrecise(recipe,catalog)>0);
            JSONObject item=new JSONObject().put("name",name).put("food_id","").put("grams",10000)
                .put("kcal_100g",72).put("saturated_fat_100g",0.2).put("sugar_100g",3).put("protein_100g",12);
            JSONObject generated=FitRocketAiRules.newFood(item,"AI");assertEquals("basic",generated.getString("status"));
            assertEquals(0,generated.getInt("estimatedPoints100"));
            JSONObject proposal=new JSONObject().put("servings",1).put("ingredients",new JSONArray().put(item));
            assertEquals(0,FitRocketAiRules.pointsPerServingPrecise(proposal,catalog),0);
            item.put("catalogOptOut",true);assertTrue(FitRocketAiRules.pointsPerServingPrecise(proposal,catalog)>0);
            assertEquals("basic",known.getString("status"));assertEquals(0,known.getInt("points"));
        }
        assertNull(FitRocketAiRules.matchCatalog(FitRocketZeroPoints.TOMATOES,"Schokoladenkuchen",catalog));
        assertNull(FitRocketAiRules.existingByName("Tomaten in Öl",catalog));
    }
    @Test public void oldFoodIdsAndBackupMetadataStayIntactAndFavoritesUseCorrectedZero()throws Exception {
        JSONObject old=food("Cocktailtomaten"),favorite=new JSONObject().put("points",3).put("referenceQuantity",100).put("note","keep");
        JSONObject restored=FitRocketZeroPoints.apply(new JSONObject(old.toString()));
        assertEquals(old.getString("id"),restored.getString("id"));assertEquals("12345678",restored.getString("barcode"));
        assertEquals(0,FitRocketFavoriteMath.scaledPointsPrecise(favorite,restored,100,10000,Collections.singletonList(restored)),0);
        JSONObject meal=new JSONObject().put("points",3).put("date","2026-11-20").put("note","keep");
        FitRocketDiary.editPoints(meal,restored,3);assertEquals(0,meal.getDouble("points"),0);assertEquals("keep",meal.getString("note"));
        assertEquals("2026-11-20",meal.getString("date"));assertEquals("keep",favorite.getString("note"));
    }
    @Test public void barcodeLookupUsesTotalFatAndPreservesNutritionAndBarcode()throws Exception {
        JSONObject response=new JSONObject().put("product",new JSONObject().put("product_name","Körniger Frischkäse")
            .put("nutriments",new JSONObject().put("energy-kcal_100g",72).put("saturated-fat_100g",0.2)
                .put("sugars_100g",3).put("proteins_100g",12).put("fat_100g",0.5)));
        FitRocketProductLookup.Product product=FitRocketProductLookup.parse("12345678",response);
        assertEquals(0,product.points(10000));JSONObject scanned=product.toFood();assertEquals("basic",scanned.getString("status"));
        response.getJSONObject("product").put("product_name","Beispielmarke Körniger Frischkäse").put("brands","Beispielmarke");
        assertEquals(0,FitRocketProductLookup.parse("12345678",response).points(100));
        assertEquals("basic",FitRocketProductLookup.parse("12345678",response).toFood().getString("status"));
        assertNull(FitRocketZeroPoints.catalogId("Salat mit Cocktailtomaten",Double.NaN,"Andere Marke"));
        assertEquals(0.5,scanned.getJSONObject("nutrition").getDouble("fat_g"),0);assertEquals("SCAN12345678",scanned.getString("id"));
        response.getJSONObject("product").getJSONObject("nutriments").put("fat_100g",4);
        assertTrue(FitRocketProductLookup.parse("12345678",response).points(100)>0);
        response.getJSONObject("product").getJSONObject("nutriments").remove("fat_100g");
        assertEquals("nutrition",FitRocketProductLookup.parse("12345678",response).toFood().getString("status"));
    }
}
