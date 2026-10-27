package de.inrouder.fitrocket;

import android.app.Activity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Gemini REST integration. Sends only the user's current prompt and voluntarily
 * saved taste preferences plus a small, non-private catalogue hint list.
 * User requests only, no background generation; secrets are never embedded in APK.
 */
public final class FitRocketGeminiApi {
    public static final String MODEL_LITE = "gemini-2.5-flash-lite";
    public static final String MODEL_FLASH = "gemini-2.5-flash";
    public static final String DEFAULT_MODEL = MODEL_LITE;

    private static final String BASE_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/";

    private FitRocketGeminiApi() {}

    public static String safeModel(String model) {
        return MODEL_FLASH.equals(model) ? MODEL_FLASH : MODEL_LITE;
    }

    private static byte[] readLimited(InputStream stream, int maxBytes) throws Exception {
        try (InputStream input = stream; ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] part = new byte[4096];
            int n;
            while ((n = input.read(part)) != -1) {
                buffer.write(part, 0, n);
                if (buffer.size() > maxBytes)
                    throw new IllegalArgumentException("KI-Antwort zu groß.");
            }
            return buffer.toByteArray();
        }
    }

    private static JSONObject schema(Activity activity) throws Exception {
        try (InputStream input = activity.getAssets().open("ai_result_schema.json")) {
            return new JSONObject(new String(readLimited(input, 50000), StandardCharsets.UTF_8));
        }
    }

    /**
     * Pure builder to permit unit tests without a real API key.
     * generationConfig.responseJsonSchema is supported for v1beta JSON generation.
     * The model is further validated with FitRocketAiRules before UI display.
     */
    public static JSONObject buildRequest(String mode, String prompt, String preferences,
                                          List<JSONObject> foods, JSONObject resultSchema)
            throws Exception {
        if (resultSchema == null || foods == null || prompt == null)
            throw new IllegalArgumentException("Gemini-Anfrage unvollständig.");

        String instruction = FitRocketAiService.instructions(mode);
        String userText = "Aufgabe: " + mode
            + "\nVorlieben (für diese Anfrage): "
            + (preferences == null || preferences.isEmpty() ? "keine" : preferences)
            + "\nBeschreibung: " + prompt
            + "\nÖffentlicher FitRocket-Katalog (ID = exakter Name [basic/nutrition]): "
            + FitRocketAiService.catalogHints(foods,
                prompt + " " + (preferences == null ? "" : preferences))
            + "\nWICHTIG: Jede Zutat braucht vollständige geschätzte Nährwerte "
            + "je 100 g/ml; kennzeichne Unsicherheiten.";

        JSONObject instructionPart = new JSONObject().put("text", instruction);
        JSONObject messagePart = new JSONObject().put("text", userText);
        JSONObject config = new JSONObject()
            .put("responseMimeType", "application/json")
            .put("responseJsonSchema", resultSchema)
            .put("maxOutputTokens", 4600)
            .put("temperature", 0.5);
        return new JSONObject()
            .put("systemInstruction", new JSONObject()
                .put("parts", new JSONArray().put(instructionPart)))
            .put("contents", new JSONArray().put(
                new JSONObject().put("role", "user")
                    .put("parts", new JSONArray().put(messagePart))))
            .put("generationConfig", config);
    }

    public static String responseJson(JSONObject response) {
        JSONObject feedback = response.optJSONObject("promptFeedback");
        if (feedback != null && !"".equals(feedback.optString("blockReason"))) {
            throw new IllegalArgumentException("Gemini hat diese Anfrage aus Sicherheitsgründen abgelehnt.");
        }
        JSONArray candidates = response.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0)
            throw new IllegalArgumentException("Gemini hat keine Antwort geliefert.");
        JSONObject candidate = candidates.optJSONObject(0);
        if (candidate == null)
            throw new IllegalArgumentException("Gemini-Antwort unvollständig.");
        String finishReason = candidate.optString("finishReason", "");
        if ("MAX_TOKENS".equals(finishReason))
            throw new IllegalArgumentException("KI-Antwort zu lang. Bitte die Anfrage vereinfachen.");
        if ("SAFETY".equals(finishReason) || "PROHIBITED_CONTENT".equals(finishReason))
            throw new IllegalArgumentException("Gemini hat den Inhalt der Anfrage abgelehnt.");
        if (!finishReason.isEmpty() && !"STOP".equals(finishReason))
            throw new IllegalArgumentException("Gemini hat die Antwort nicht vollständig erstellt.");
        JSONObject content = candidate.optJSONObject("content");
        JSONArray parts = content == null ? null : content.optJSONArray("parts");
        if (parts == null)
            throw new IllegalArgumentException("Gemini-Antwort ohne Text.");
        StringBuilder answer = new StringBuilder();
        for (int i = 0; i < parts.length(); i++) {
            JSONObject part = parts.optJSONObject(i);
            if (part != null && part.has("text")) answer.append(part.optString("text"));
        }
        if (answer.length() == 0 || answer.length() > 50000)
            throw new IllegalArgumentException("Gemini-Antwort leer oder zu umfangreich.");
        return answer.toString().trim();
    }

    public static String errorMessage(int status, String details) {
        String code = details == null ? "" : details;
        if (status == 400) return "Gemini hat die Anfrage oder das Antwortschema abgelehnt. "
            + "Bitte weniger Details eingeben oder ein anderes Gemini-Modell wählen (HTTP 400).";
        if (status == 401 || status == 403)
            return "Gemini-API-Schlüssel ungültig, gesperrt oder ohne Zugriffsrechte. "
                + "Prüfe den Schlüssel in Google AI Studio (HTTP " + status + ").";
        if (status == 404)
            return "Das ausgewählte Gemini-Modell ist für diesen API-Schlüssel nicht verfügbar. "
                + "Versuche Flash-Lite oder Flash.";
        if (status == 429 || code.contains("RESOURCE_EXHAUSTED") || code.contains("quota")) {
            return "Gemini-Kontingent oder Anfragelimit erreicht (429). "
                + "Das kostenlose Kontingent kann vorübergehend erschöpft sein. "
                + "Prüfe die Limits in Google AI Studio und versuche es später erneut.";
        }
        if (status == 500 || status == 502 || status == 503 || status == 504)
            return "Gemini ist vorübergehend nicht erreichbar (HTTP " + status + "). "
                + "Bitte später erneut versuchen.";
        return "Gemini hat nicht geantwortet (HTTP " + status + ").";
    }

    public static void request(Activity activity, String mode, String prompt,
                               String preferences, List<JSONObject> foods,
                               FitRocketAiService.Callback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                String key = FitRocketAiSecrets.get(activity, FitRocketAiSecrets.GEMINI);
                if (key == null || key.isEmpty())
                    throw new IllegalArgumentException(
                        "Bitte einen Gemini-API-Schlüssel unter KI-Einstellungen hinterlegen.");
                String model = safeModel(activity.getSharedPreferences("fitrocket",
                    Activity.MODE_PRIVATE).getString("geminiModel", DEFAULT_MODEL));
                JSONObject body = buildRequest(mode, prompt, preferences, foods, schema(activity));
                byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                if (bytes.length > 90000)
                    throw new IllegalArgumentException("KI-Anfrage zu umfangreich.");

                URL url = new URL(BASE_URL + model + ":generateContent");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(90000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("x-goog-api-key", key);
                connection.setRequestProperty("User-Agent", "FitRocket/0.11.0");
                connection.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream stream = connection.getOutputStream()) {
                    stream.write(bytes);
                }

                int status = connection.getResponseCode();
                if (status != 200) {
                    // Never show raw server responses: they may echo sensitive input.
                    throw new IllegalStateException(errorMessage(status, ""));
                }
                String json = new String(readLimited(connection.getInputStream(), 350000),
                    StandardCharsets.UTF_8);
                JSONObject reply = FitRocketAiRules.validateResponse(
                    new JSONObject(responseJson(new JSONObject(json))), mode);
                activity.runOnUiThread(() -> callback.onSuccess(reply));
            } catch (Exception exception) {
                String reason = exception.getMessage() == null
                    ? "Verbindungsproblem beim Gemini-Dienst." : exception.getMessage();
                if (reason.length() > 240) reason = reason.substring(0, 240);
                String finalReason = reason;
                activity.runOnUiThread(() -> callback.onFailure(finalReason));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }, "FitRocket-Gemini-Request").start();
    }
}
