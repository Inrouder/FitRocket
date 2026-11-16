package de.inrouder.fitrocket;

import org.json.JSONObject;

/** Pure alarm rules shared by scheduling and delivery. A changed goal invalidates old alarms. */
public final class FitRocketFastingAlarmRules {
    public static final String ENABLED_KEY="fastingReminderEnabled";
    private FitRocketFastingAlarmRules(){}
    public static long nextTrigger(JSONObject state,boolean enabled,long now){
        if(!enabled)return 0;
        JSONObject active=FitRocketFasting.validated(state).optJSONObject("active");
        if(active==null||active.optLong("notifiedGoal",-1)==active.optLong("goal"))return 0;
        return Math.max(now+1000,active.optLong("goal"));
    }
    public static boolean due(JSONObject state,boolean enabled,long start,long goal,long now){
        JSONObject active=FitRocketFasting.validated(state).optJSONObject("active");
        return enabled&&active!=null&&active.optLong("start")==start&&active.optLong("goal")==goal
            &&now>=goal&&active.optLong("notifiedGoal",-1)!=goal;
    }
    public static JSONObject notified(JSONObject state,long start,long goal,long now){
        if(!due(state,true,start,goal,now))throw new IllegalArgumentException("Veraltete Fastenerinnerung.");
        JSONObject result=FitRocketFasting.validated(state);
        try{result.getJSONObject("active").put("notifiedGoal",goal);return result;}
        catch(Exception e){throw new IllegalArgumentException("Fastenerinnerung konnte nicht gespeichert werden.",e);}
    }
}
