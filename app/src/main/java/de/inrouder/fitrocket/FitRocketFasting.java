package de.inrouder.fitrocket;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Locale;

/** Local fasting log. Absolute timestamps keep the timer alive across process death and DST. */
public final class FitRocketFasting {
    public static final String KEY="fasting";
    public static final int DEFAULT_HOURS=16, HISTORY_LIMIT=100;
    public static final long HOUR=3600000L;
    private FitRocketFasting(){}

    public static boolean supported(int hours){
        return hours==12||hours==14||hours==16||hours==17||hours==18||hours==20;
    }
    public static String plan(int hours){return hours+":"+(24-hours);}
    public static JSONObject empty(){
        try{return new JSONObject().put("planHours",DEFAULT_HOURS).put("history",new JSONArray());}
        catch(Exception e){throw new IllegalStateException(e);}
    }
    private static void timestamp(long value){
        if(value<0||value>4102444800000L)throw new IllegalArgumentException("Ungültiger Zeitpunkt.");
    }
    private static void window(long start,long goal){
        timestamp(start);timestamp(goal);
        if(goal<=start||goal-start>24*HOUR)
            throw new IllegalArgumentException("Das Ziel muss nach dem Start und höchstens 24 Stunden später liegen.");
    }
    public static JSONObject validated(JSONObject source){
        if(source==null)throw new IllegalArgumentException("Fastendaten fehlen.");
        try{
            JSONObject state=new JSONObject(source.toString());
            if(!supported(state.getInt("planHours")))throw new IllegalArgumentException("Unbekannter Fastenplan.");
            if(state.has("active")){
                JSONObject active=state.getJSONObject("active");
                window(active.getLong("start"),active.getLong("goal"));
                if(!supported(active.getInt("planHours")))throw new IllegalArgumentException("Unbekannter Fastenplan.");
            }
            JSONArray history=state.getJSONArray("history");
            if(history.length()>HISTORY_LIMIT)throw new IllegalArgumentException("Zu viele Fasteneinträge.");
            for(int i=0;i<history.length();i++){
                JSONObject entry=history.getJSONObject(i);
                long start=entry.getLong("start"),end=entry.getLong("end");
                window(start,entry.getLong("goal"));timestamp(end);
                if(end<start||!supported(entry.getInt("planHours")))throw new IllegalArgumentException("Ungültiger Fasteneintrag.");
            }
            return state;
        }catch(IllegalArgumentException e){throw e;}
        catch(Exception e){throw new IllegalArgumentException("Ungültige Fastendaten.",e);}
    }
    public static JSONObject selectPlan(JSONObject source,int hours){
        if(!supported(hours))throw new IllegalArgumentException("Unbekannter Fastenplan.");
        JSONObject state=validated(source);
        try{
            state.put("planHours",hours);
            JSONObject active=state.optJSONObject("active");
            if(active!=null)active.put("planHours",hours).put("goal",active.getLong("start")+hours*HOUR);
            return validated(state);
        }catch(Exception e){throw new IllegalArgumentException("Plan konnte nicht geändert werden.",e);}
    }
    public static JSONObject start(JSONObject source,long now){
        timestamp(now);JSONObject state=validated(source);
        if(state.has("active"))throw new IllegalArgumentException("Es läuft bereits ein Fasten.");
        try{
            int hours=state.getInt("planHours");
            state.put("active",new JSONObject().put("start",now).put("goal",now+hours*HOUR).put("planHours",hours));
            return validated(state);
        }catch(Exception e){throw new IllegalArgumentException("Fasten konnte nicht gestartet werden.",e);}
    }
    public static JSONObject edit(JSONObject source,long start,long goal,long now){
        timestamp(now);window(start,goal);
        if(start>now)throw new IllegalArgumentException("Der Start darf nicht in der Zukunft liegen.");
        JSONObject state=validated(source);
        try{
            state.getJSONObject("active").put("start",start).put("goal",goal);
            return state;
        }catch(Exception e){throw new IllegalArgumentException("Kein laufendes Fasten.",e);}
    }
    public static JSONObject finish(JSONObject source,long now){
        timestamp(now);JSONObject state=validated(source);
        try{
            JSONObject entry=state.getJSONObject("active");
            if(now<entry.getLong("start"))throw new IllegalArgumentException("Das Ende liegt vor dem Start. Bitte den Start korrigieren.");
            entry.put("end",now);
            JSONArray history=new JSONArray().put(entry),old=state.getJSONArray("history");
            for(int i=0;i<old.length()&&history.length()<HISTORY_LIMIT;i++)history.put(old.get(i));
            state.remove("active");state.put("history",history);
            return validated(state);
        }catch(IllegalArgumentException e){throw e;}
        catch(Exception e){throw new IllegalArgumentException("Kein laufendes Fasten.",e);}
    }
    public static JSONObject cancel(JSONObject source){
        JSONObject state=validated(source);state.remove("active");return state;
    }
    public static long elapsed(JSONObject active,long now){return Math.max(0,now-active.optLong("start"));}
    public static double progress(JSONObject active,long now){
        long duration=active.optLong("goal")-active.optLong("start");
        return duration<=0?0:Math.min(1,elapsed(active,now)/(double)duration);
    }
    public static String duration(long millis){
        long seconds=Math.max(0,millis)/1000;
        return String.format(Locale.GERMANY,"%02d:%02d:%02d",seconds/3600,(seconds/60)%60,seconds%60);
    }
}
