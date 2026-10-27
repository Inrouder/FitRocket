package de.inrouder.fitrocket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Tests Gemini request/response transport rules without an API key or network access.
 */
public class FitRocketGeminiApiTest {

    @Test public void defaultIsFreeTierFlashLite() {
        assertEquals("gemini-2.5-flash-lite", FitRocketGeminiApi.DEFAULT_MODEL);
        assertEquals(FitRocketGeminiApi.MODEL_LITE, FitRocketGeminiApi.safeModel("random-model"));
        assertEquals(FitRocketGeminiApi.MODEL_FLASH,
            FitRocketGeminiApi.safeModel(FitRocketGeminiApi.MODEL_FLASH));
    }

    @Test public void requestCarriesSystemPromptAndSchemaButNoPrivateCatalog() throws Exception {
        JSONObject schema = new JSONObject().put("type", "object")
            .put("properties", new JSONObject().put("title",
                new JSONObject().put("type", "string")))
            .put("required", new JSONArray().put("title"));

        JSONObject privateFood = new JSONObject()
            .put("id", "AI_PRIVATE")
            .put("name", "Privates Familienrezept")
            .put("status", "nutrition");

        JSONObject publicFood = new JSONObject()
            .put("id", "ZP007")
            .put("name", "Äpfel")
            .put("status", "basic");

        JSONObject request = FitRocketGeminiApi.buildRequest(
            "recipe", "Kuchen mit Äpfeln", "ohne Milch",
            Arrays.asList(privateFood, publicFood), schema);

        JSONObject config = request.getJSONObject("generationConfig");
        assertEquals("application/json", config.getString("responseMimeType"));
        assertEquals("object", config.getJSONObject("responseJsonSchema").getString("type"));
        assertTrue(config.getInt("maxOutputTokens") >= 3000);
        assertFalse(request.has("apiKey"));

        String contents = request.getJSONArray("contents")
            .getJSONObject(0).getJSONArray("parts").getJSONObject(0).getString("text");
        assertTrue(contents.contains("Kuchen mit Äpfeln"));
        assertTrue(contents.contains("ohne Milch"));
        assertTrue(contents.contains("ZP007"));
        assertFalse(contents.contains("Privates Familienrezept"));
        assertFalse(contents.contains("AI_PRIVATE"));
        assertTrue(request.getJSONObject("systemInstruction")
            .getJSONArray("parts").getJSONObject(0).getString("text")
            .contains("FitRocket"));
    }

    @Test public void extractsGeminiCandidateText() throws Exception {
        JSONObject result = new JSONObject()
            .put("candidates", new JSONArray().put(
                new JSONObject().put("finishReason", "STOP")
                .put("content", new JSONObject().put("parts", new JSONArray().put(
                    new JSONObject().put("text", new JSONObject().put("title", "Test").toString()))))));

        assertEquals("Test",
            new JSONObject(FitRocketGeminiApi.responseJson(result)).getString("title"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void rejectsTruncatedResponse() throws Exception {
        JSONObject result = new JSONObject()
            .put("candidates", new JSONArray().put(
                new JSONObject().put("finishReason", "MAX_TOKENS")
                .put("content", new JSONObject().put("parts", new JSONArray().put(
                    new JSONObject().put("text", "unvollständig"))))));
        FitRocketGeminiApi.responseJson(result);
    }

    @Test public void distinguishesGeminiQuotaFromOpenAiBilling() {
        String message = FitRocketGeminiApi.errorMessage(429, "");
        assertTrue(message.contains("Gemini"));
        assertTrue(message.contains("Kontingent"));
        assertFalse(message.contains("OpenAI"));
        assertFalse(message.contains("Guthaben aufladen"));
        assertTrue(FitRocketGeminiApi.errorMessage(403, "").contains("Schlüssel"));
    }
}
