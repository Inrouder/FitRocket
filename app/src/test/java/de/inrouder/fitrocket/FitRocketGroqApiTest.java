package de.inrouder.fitrocket;

import org.json.*;
import org.junit.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.Assert.*;

public class FitRocketGroqApiTest {
    private JSONObject wrap(Object content,String finish)throws Exception {
        return new JSONObject().put("choices",new JSONArray().put(new JSONObject()
            .put("finish_reason",finish).put("message",new JSONObject().put("content",content))));
    }
    @Test public void requestUsesDocumentedStrictSchemaAndOnlyPublicHints()throws Exception {
        JSONObject schema=new JSONObject(Files.readString(Path.of("src/main/assets/ai_result_schema.json")));
        JSONObject publicFood=new JSONObject().put("id","ZP007").put("name","Äpfel").put("status","basic");
        JSONObject privateFood=new JSONObject().put("id","OWNSECRET").put("name","Privatrezept").put("status","basic");
        for(String model:new String[]{FitRocketGroqApi.MODEL_SMALL,FitRocketGroqApi.MODEL_LARGE}){
            JSONObject body=FitRocketGroqApi.buildRequest(model,"recipe","Kuchen mit Äpfeln","ohne Milch",Arrays.asList(publicFood,privateFood),schema);
            assertEquals(model,body.getString("model"));
            JSONObject format=body.getJSONObject("response_format");assertEquals("json_schema",format.getString("type"));
            assertTrue(format.getJSONObject("json_schema").getBoolean("strict"));
            assertEquals(schema.toString(),format.getJSONObject("json_schema").getJSONObject("schema").toString());
            assertFalse(body.getBoolean("stream"));assertTrue(body.getInt("max_completion_tokens")>=3800);
            assertEquals(4000,body.getInt("max_completion_tokens"));assertEquals("low",body.getString("reasoning_effort"));
            JSONArray messages=body.getJSONArray("messages");assertEquals(2,messages.length());
            assertTrue(messages.getJSONObject(0).getString("content").contains("KEINE Punkte"));
            String input=messages.getJSONObject(1).getString("content");
            assertTrue(input.contains("Kuchen mit Äpfeln"));assertTrue(input.contains("ohne Milch"));assertTrue(input.contains("ZP007"));
            assertFalse(input.contains("OWNSECRET"));assertFalse(input.contains("Privatrezept"));
            assertFalse(body.has("apiKey"));assertFalse(body.has("tools"));
        }
        assertEquals(FitRocketGroqApi.DEFAULT_MODEL,FitRocketGroqApi.safeModel("arbitrary/paid-model"));
    }
    @Test public void largeCatalogHintsAreBoundedAtWholeEntries()throws Exception {
        List<JSONObject> foods=new ArrayList<>();
        for(int i=0;i<300;i++)foods.add(new JSONObject().put("id","CALC"+i)
            .put("name","Öffentliches Lebensmittel "+i+" mit längerem Sortennamen").put("status","nutrition"));
        String hint=FitRocketAiService.catalogHints(foods,"Lebensmittel",2400);
        assertTrue(hint.length()<=2400);assertTrue(hint.endsWith("]; "));assertTrue(hint.contains("CALC"));
        assertTrue(FitRocketAiService.catalogHints(foods,"Lebensmittel").length()>hint.length());
    }
    @Test public void completedContentPassesExistingValidationAndLocalPointRules()throws Exception {
        JSONObject ingredient=new JSONObject().put("name","Schokolade").put("food_id","").put("grams",100)
            .put("kcal_100g",500).put("saturated_fat_100g",10).put("sugar_100g",30).put("protein_100g",5)
            .put("confidence","niedrig").put("note","geschätzt");
        JSONObject result=new JSONObject().put("title","Schokolade").put("summary","Schätzung").put("instructions","")
            .put("minutes",0).put("group","Snack").put("servings",1).put("confidence","niedrig")
            .put("uncertainty","Etikett prüfen").put("ingredients",new JSONArray().put(ingredient)).put("points",0);
        JSONObject valid=FitRocketAiRules.validateResponse(new JSONObject(FitRocketGroqApi.responseJson(wrap(result.toString(),"stop"))),"food");
        assertTrue(FitRocketAiRules.pointsPerServingPrecise(valid,Collections.emptyList())>0);
        ingredient.put("protein_100g",-1);
        try{FitRocketAiRules.validateResponse(result,"food");fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void refusesPartialRefusedToolAndEmptyOutputs()throws Exception {
        for(String finish:new String[]{"length","content_filter","tool_calls",""}){
            try{FitRocketGroqApi.responseJson(wrap("{}",finish));fail(finish);}catch(IllegalArgumentException expected){}
        }
        for(Object content:new Object[]{JSONObject.NULL,"",new JSONArray(),"x".repeat(50001)}){
            try{FitRocketGroqApi.responseJson(wrap(content,"stop"));fail();}catch(IllegalArgumentException expected){}
        }
        JSONObject refused=wrap("{}","stop");refused.getJSONArray("choices").getJSONObject(0).getJSONObject("message").put("refusal","private raw refusal");
        try{FitRocketGroqApi.responseJson(refused);fail();}catch(IllegalArgumentException expected){assertFalse(expected.getMessage().contains("private raw"));}
        try{FitRocketGroqApi.responseJson(new JSONObject());fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void errorMessagesDistinguishQuotaKeyModelAndAvailability(){
        assertTrue(FitRocketGroqApi.errorMessage(429).contains("Kontingent"));
        assertTrue(FitRocketGroqApi.errorMessage(401).contains("Schlüssel"));
        assertTrue(FitRocketGroqApi.errorMessage(404).contains("Modell"));
        assertTrue(FitRocketGroqApi.errorMessage(503).contains("503"));
        assertFalse(FitRocketGroqApi.errorMessage(503).contains("OpenAI"));
    }
}
