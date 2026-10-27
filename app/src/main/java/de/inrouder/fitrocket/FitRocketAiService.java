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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Calls a fixed HTTPS API endpoint for explicit user requests only.
 * Never sends profile, weights, diary, or health records.
 * No secret is embedded in code. A user-supplied key stays encrypted on device.
 * Before a public release, an authenticated backend proxy is preferable to mobile BYOK.
 */
public final class FitRocketAiService {
    public static final String MODEL="gpt-5-mini";
    private static final String ENDPOINT="https://api.openai.com/v1/responses";
    private FitRocketAiService(){}

    public interface Callback {
        void onSuccess(JSONObject output);
        void onFailure(String message);
    }

    private static byte[] readLimited(InputStream stream,int limit) throws Exception {
        try(InputStream in=stream;ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buffer=new byte[4096];int n;
            while((n=in.read(buffer))!=-1){
                out.write(buffer,0,n);
                if(out.size()>limit)throw new IllegalStateException("Antwort zu umfangreich.");
            }
            return out.toByteArray();
        }
    }

    private static String outputText(JSONObject response) throws Exception {
        JSONArray outputs=response.optJSONArray("output");
        if(outputs==null)throw new IllegalStateException("KI-Antwort ohne Inhalt.");
        StringBuilder combined=new StringBuilder();
        for(int i=0;i<outputs.length();i++){
            JSONObject output=outputs.optJSONObject(i);
            if(output==null||!"message".equals(output.optString("type")))continue;
            JSONArray parts=output.optJSONArray("content");
            if(parts==null)continue;
            for(int j=0;j<parts.length();j++){
                JSONObject part=parts.optJSONObject(j);
                if(part==null)continue;
                if("refusal".equals(part.optString("type")))
                    throw new IllegalStateException("Die KI konnte diese Anfrage nicht bearbeiten.");
                if("output_text".equals(part.optString("type"))){
                    combined.append(part.optString("text"));
                }
            }
        }
        if(combined.length()==0 || combined.length()>250000)
            throw new IllegalStateException("Keine vollständige Antwort erhalten.");
        return combined.toString();
    }

    static String catalogHints(List<JSONObject> foods,String prompt){
        // Constrain zero-point claims to exact FitRocket IDs and names, not model guesses.
        String[] popular={
            "ZP007","ZP014","ZP019","ZP233","ZP193","ZP129","ZP111","ZP016",
            "ZP071","ZP214","ZP134","ZP236","ZP064","ZP095","ZP210","ZP213",
            "ZP085","ZP084","ZP100","ZP194","ZP062","ZP023","ZP105","ZP104",
            "ZP039","ZP013","ZP080","ZP037","ZP015","ZP209","ZP156","ZP140",
            "ZP070","ZP067","ZP050","ZP107","ZP143","ZP220",
            "CALC045","CALC044","CALC014","CALC012","CALC013","CALC003",
            "CALC011","CALC010","CALC004","CALC008","CALC020","CALC001",
            "CALC018","CALC028","CALC032","CALC015","CALC021","CALC035"
        };
        String lowered=FitRocketAiRules.normalize(prompt);
        List<JSONObject> selected=new ArrayList<>();
        for(String wanted:popular){
            for(JSONObject f:foods)if(wanted.equals(f.optString("id")) && !selected.contains(f)){
                selected.add(f);break;
            }
        }
        // Only bundled, non-private entries may be sent as catalogue hints.
        for(JSONObject f:foods){
            if(selected.size()>=145)break;
            String id=f.optString("id");
            if(!(id.startsWith("ZP")||id.startsWith("EX")||
                 id.startsWith("USBLUE")||id.startsWith("CALC")))continue;
            String key=FitRocketAiRules.normalize(f.optString("name"));
            if(key.length()>=4 && lowered.contains(key) && !selected.contains(f)
                && ("basic".equals(f.optString("status"))||"nutrition".equals(f.optString("status"))))
                selected.add(f);
        }
        for(JSONObject f:foods){
            if(selected.size()>=210)break;
            String id=f.optString("id");
            if(!(id.startsWith("ZP")||id.startsWith("EX")||
                 id.startsWith("USBLUE")||id.startsWith("CALC")))continue;
            if(("basic".equals(f.optString("status"))||"nutrition".equals(f.optString("status")))
                 && !selected.contains(f))selected.add(f);
        }
        StringBuilder text=new StringBuilder();
        for(JSONObject f:selected){
            if(text.length()>11000)break;
            text.append(f.optString("id")).append(" = ").append(f.optString("name"))
                .append(" [").append(f.optString("status")).append("]; ");
        }
        return text.toString();
    }

    static String instructions(String mode) {
        String base="Du bist FitRocket, ein deutschsprachiger Küchen- und Ernährungsvorschlags-Assistent. "
            +"Du generierst nur Vorschläge, KEINE Punkte. FitRocket berechnet die Punkte lokal. "
            +"Antworte ausschließlich gemäß JSON-Schema. Alle Makros sind Werte pro 100 g oder pro 100 ml, "
            +"nicht pro Portion: kcal_100g, saturated_fat_100g, sugar_100g und protein_100g. "
            +"Liefer alle vier Nährwerte für JEDE Zutat als nichtnegative realistische Zahlen. "
            +"Fehlende Daten dürfen ausdrücklich als Schätzung eingesetzt werden, aber kennzeichne geringe Sicherheit "
            +"mit confidence=niedrig und note/uncertainty. Erfinde keine offiziell geprüften Marken-Nährwerte. "
            +"Für Gemüse, Fleisch, Eier usw. verwende nur dann die exakt geschriebene food_id aus der mitgegebenen "
            +"FitRocket-Liste, wenn die Zutat exakt dem Katalogeintrag entspricht (name muss identisch sein); "
            +"sonst food_id als leere Zeichenkette. Verarbeitetes, paniertes oder mit Öl zubereitetes Essen darf "
            +"nicht allein als 0-Punkte-Grundzutat klassifiziert werden; Öl, Dressing, Soße, Käse und Zucker "
            +"als separate Zutaten angeben. Alle Grammangaben positiv. Keine erfundenen Punktwerte. "
            +"Lege höchstens 12 Komponenten an, gruppiere Frühstück/Mittagessen/Abendessen/Snack/Getränke. "
            +"Antworte nur mit passenden essbaren Zutaten. "
            +"Energieaufnahmen und Punkte sind grobe Näherungen und dienen nur der Protokollierung.";
        if("recipe".equals(mode)){
            return base+" Erstelle ein konkretes, vollständiges, alltagstaugliches Rezept nach Vorlieben der Person. "
              +"Schreibe sinnvolle Zubereitungsschritte in instructions, die Gesamtmengen aller Zutaten in Gramm "
              +"für servings Portionen. Vermeide unnötige Komponenten.";
        }
        if("meal".equals(mode)){
            return base+" Analysiere eine frei beschriebene, bereits gegessene Mahlzeit. "
              +"Schätze plausible Mengen und mögliche Zubereitungsfette nur mit klarer Unsicherheitskennzeichnung. "
              +"Erfinde keine besonders genaue Nährstoffanalyse aus einer ungenauen Beschreibung. "
              +"Setze servings immer 1 und schreibe in uncertainty, was du geraten hast.";
        }
        return base+" Erzeuge GENAU eine Zutat für ein neu beschriebenes einzelnes Lebensmittel oder Produkt. "
            +"Die Nährwerte müssen pro 100 g/100 ml geschätzt werden, wenn keine belegten Produktdaten vorliegen. "
            +"Setze grams=100 und servings=1, instructions leer und food_id leer, sofern keine Eindeutigkeit. "
            +"Bei Markennamen ausdrücklich sagen, dass Etikettwerte genauer sind.";
    }

    private static JSONObject requestBody(Activity activity,String mode,String prompt,
                                          String preferences,List<JSONObject> foods) throws Exception {
        JSONObject schema;
        try(InputStream in=activity.getAssets().open("ai_result_schema.json")){
            schema=new JSONObject(new String(readLimited(in,50000),StandardCharsets.UTF_8));
        }
        JSONObject format=new JSONObject()
            .put("type","json_schema").put("name","fitrocket_cooking").put("strict",true)
            .put("schema",schema);
        JSONObject text=new JSONObject().put("format",format);
        String content="Aufgabe: "+mode+"\nPersönliche Essensvorlieben (nur für diese Anfrage): "+
            (preferences.isEmpty()?"nicht angegeben":preferences)+"\nAnfrage: "+prompt+
            "\nFitRocket-Katalog (ID, exakter Name, Status) – bei passender Zutat berücksichtigen: "+
            catalogHints(foods,prompt+" "+preferences);
        return new JSONObject()
            .put("model",MODEL)
            .put("instructions",instructions(mode))
            .put("input",content)
            .put("text",text)
            .put("store",false)
            .put("max_output_tokens",3800);
    }

    public static void request(Activity activity,String mode,String prompt,String preferences,
                               List<JSONObject> foods,Callback callback){
        if(!("recipe".equals(mode)||"meal".equals(mode)||"food".equals(mode))){
            callback.onFailure("Unbekannter KI-Modus");return;
        }
        if(prompt==null||prompt.trim().length()<5||prompt.length()>1400){
            callback.onFailure("Bitte beschreibe dein Essen in 5–1400 Zeichen.");return;
        }
        if(preferences==null)preferences="";
        if(preferences.length()>500){
            callback.onFailure("Die Vorlieben dürfen höchstens 500 Zeichen enthalten.");return;
        }
        // Gemini is the default from v0.11.0; OpenAI remains available with its old key.
        String provider=activity.getSharedPreferences("fitrocket",Activity.MODE_PRIVATE)
            .getString("aiProvider",FitRocketAiSecrets.GEMINI);
        if(FitRocketAiSecrets.GEMINI.equals(provider)){
            FitRocketGeminiApi.request(activity,mode,prompt,preferences,
                new ArrayList<>(foods),callback);
            return;
        }
        if(!FitRocketAiSecrets.OPENAI.equals(provider)){
            callback.onFailure("Unbekannter KI-Anbieter.");return;
        }
        final String finalPreferences=preferences;
        final List<JSONObject> snapshot=new ArrayList<>(foods);
        new Thread(()->{
            HttpURLConnection connection=null;
            try{
                String key=FitRocketAiSecrets.get(activity);
                if(key==null||key.length()<8)throw new IllegalStateException("Bitte zuerst deinen API-Schlüssel hinterlegen.");
                JSONObject body=requestBody(activity,mode,prompt.trim(),finalPreferences,snapshot);
                connection=(HttpURLConnection)new URL(ENDPOINT).openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(90000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type","application/json; charset=utf-8");
                connection.setRequestProperty("Accept","application/json");
                connection.setRequestProperty("Authorization","Bearer "+key);
                connection.setRequestProperty("User-Agent","FitRocket/0.10.0");
                byte[] bytes=body.toString().getBytes(StandardCharsets.UTF_8);
                if(bytes.length>80000)throw new IllegalStateException("Anfrage zu lang.");
                connection.setFixedLengthStreamingMode(bytes.length);
                try(OutputStream out=connection.getOutputStream()){out.write(bytes);}
                int status=connection.getResponseCode();
                if(status!=200){
                    String message;
                    if(status==401)message="API-Schlüssel ungültig oder gesperrt.";
                    else if(status==429)message="API-Kontingent erreicht oder zu viele Anfragen. API-Guthaben und Limits prüfen.";
                    else if(status==403)message="Zugriff auf den KI-Dienst nicht erlaubt.";
                    else if(status==400)message="Die Anfrage wurde vom KI-Dienst abgelehnt (HTTP 400).";
                    else message="KI-Dienst aktuell nicht verfügbar (HTTP "+status+").";
                    throw new IllegalStateException(message);
                }
                JSONObject response=new JSONObject(new String(readLimited(connection.getInputStream(),400000),StandardCharsets.UTF_8));
                if(!"completed".equals(response.optString("status")))
                    throw new IllegalStateException("Die KI konnte keine vollständige Antwort erzeugen. Erneut versuchen.");
                JSONObject output=FitRocketAiRules.validateResponse(new JSONObject(outputText(response)),mode);
                activity.runOnUiThread(()->callback.onSuccess(output));
            }catch(Exception e){
                String error=e.getMessage();
                if(error==null||error.trim().isEmpty())error="Verbindung oder KI-Antwort fehlgeschlagen.";
                String finalMessage=error.length()>180?error.substring(0,180):error;
                activity.runOnUiThread(()->callback.onFailure(finalMessage));
            }finally{if(connection!=null)connection.disconnect();}
        },"FitRocket-AI-Request").start();
    }
}
