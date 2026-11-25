package de.inrouder.fitrocket;

import android.app.Activity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Explicit BYOK Groq requests; no keys, prompts or raw server failures are logged. */
public final class FitRocketGroqApi {
    public static final String MODEL_SMALL="openai/gpt-oss-20b";
    public static final String MODEL_LARGE="openai/gpt-oss-120b";
    public static final String DEFAULT_MODEL=MODEL_SMALL;
    private FitRocketGroqApi(){}

    public static String safeModel(String model){return MODEL_LARGE.equals(model)?MODEL_LARGE:DEFAULT_MODEL;}

    static JSONObject buildRequest(String model,String mode,String prompt,String preferences,
                                   List<JSONObject> foods,JSONObject schema)throws Exception {
        String taste=preferences==null?"":preferences;
        String input="Aufgabe: "+mode+"\nVorlieben für diese Anfrage: "+taste
            +"\nBeschreibung: "+prompt+"\nÖffentlicher FitRocket-Katalog (ID = exakter Name [Status]): "
            +FitRocketAiService.catalogHints(foods,prompt+" "+taste);
        return new JSONObject().put("model",safeModel(model))
            .put("messages",new JSONArray()
                .put(new JSONObject().put("role","system").put("content",FitRocketAiService.instructions(mode)))
                .put(new JSONObject().put("role","user").put("content",input)))
            .put("response_format",new JSONObject().put("type","json_schema")
                .put("json_schema",new JSONObject().put("name","fitrocket_cooking")
                    .put("strict",true).put("schema",schema)))
            .put("max_completion_tokens",8000).put("stream",false);
    }

    static String responseJson(JSONObject response){
        JSONArray choices=response.optJSONArray("choices");
        JSONObject choice=choices!=null&&choices.length()==1?choices.optJSONObject(0):null;
        if(choice==null)throw new IllegalArgumentException("Groq hat keine eindeutige Antwort geliefert.");
        if(!"stop".equals(choice.optString("finish_reason")))
            throw new IllegalArgumentException("Groq hat die Antwort nicht vollständig erstellt. Bitte die Anfrage vereinfachen.");
        JSONObject message=choice.optJSONObject("message");
        if(message==null||!message.isNull("refusal")||message.has("tool_calls"))
            throw new IllegalArgumentException("Groq konnte diese Anfrage nicht bearbeiten.");
        Object content=message.opt("content");
        if(!(content instanceof String)||((String)content).trim().isEmpty()||((String)content).length()>50000)
            throw new IllegalArgumentException("Groq-Antwort leer oder zu umfangreich.");
        return ((String)content).trim();
    }

    static String errorMessage(int status){
        if(status==401||status==403)return "Groq-Schlüssel ungültig, gesperrt oder ohne Zugriffsrechte (HTTP "+status+"). Bitte in der Groq Console prüfen.";
        if(status==429)return "Groq-Kontingent oder Anfragelimit erreicht (HTTP 429). Bitte die Free-Plan-Limits in der Groq Console prüfen und später erneut versuchen.";
        if(status==404)return "Groq-Modell nicht verfügbar (HTTP 404). Bitte die Modellauswahl prüfen.";
        if(status==400||status==422)return "Groq hat die Anfrage oder das Antwortschema abgelehnt (HTTP "+status+"). Bitte die Anfrage vereinfachen.";
        if(status==413)return "Die Anfrage ist für Groq zu umfangreich (HTTP 413). Bitte kürzer beschreiben.";
        return "Groq ist derzeit nicht verfügbar (HTTP "+status+"). Bitte später erneut versuchen.";
    }

    static void request(Activity activity,String mode,String prompt,String preferences,
                        List<JSONObject> foods,FitRocketAiService.Callback callback){
        String model=safeModel(activity.getSharedPreferences("fitrocket",Activity.MODE_PRIVATE)
            .getString("groqModel",DEFAULT_MODEL));
        new Thread(()->{
            try{
                if(callback.isCancelled())return;
                String key=FitRocketAiSecrets.get(activity,FitRocketAiSecrets.GROQ);
                if(key==null||key.isEmpty())throw new IllegalArgumentException("Bitte einen Groq-API-Schlüssel unter KI-Einstellungen hinterlegen.");
                JSONObject schema=FitRocketAiService.schema(activity);
                byte[] body=buildRequest(model,mode,prompt,preferences,foods,schema).toString().getBytes(StandardCharsets.UTF_8);
                byte[] response=FitRocketAiTransport.postGroq(key,body,new FitRocketAiTransport.Control(){
                    public boolean cancelled(){return callback.isCancelled();}
                    public void retrying(int attempt,int maximum,long wait){
                        activity.runOnUiThread(()->{if(!callback.isCancelled())callback.onRetry(attempt,maximum,wait);});
                    }
                });
                JSONObject result=FitRocketAiRules.validateResponse(new JSONObject(responseJson(
                    new JSONObject(new String(response,StandardCharsets.UTF_8)))),mode);
                activity.runOnUiThread(()->{if(!callback.isCancelled())callback.onSuccess(result);});
            }catch(FitRocketAiTransport.Cancelled ignored){
            }catch(InterruptedException interrupted){Thread.currentThread().interrupt();
            }catch(Exception error){
                String reason;
                if(error instanceof FitRocketAiTransport.HttpFailure){
                    FitRocketAiTransport.HttpFailure f=(FitRocketAiTransport.HttpFailure)error;
                    reason=errorMessage(f.status)+"\nModell: "+model+" · Versuche: "+f.attempts;
                }else if(error instanceof SocketTimeoutException)reason="Groq hat nicht rechtzeitig geantwortet. Bitte später erneut versuchen.";
                else if(error instanceof IOException)reason="Verbindung zu Groq fehlgeschlagen. Bitte Internetverbindung prüfen.";
                else if(error instanceof IllegalArgumentException)reason=error.getMessage();
                else reason="Groq-Antwort oder gespeicherter Schlüssel konnte nicht gelesen werden. Bitte die Einrichtung prüfen und erneut versuchen.";
                String message=reason==null?"Groq-Anfrage fehlgeschlagen.":reason;
                activity.runOnUiThread(()->{if(!callback.isCancelled())callback.onFailure(message);});
            }
        },"FitRocket-Groq-Request").start();
    }
}
