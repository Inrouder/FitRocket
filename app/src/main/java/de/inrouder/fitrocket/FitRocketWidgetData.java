package de.inrouder.fitrocket;

import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Pure data calculation shared by the homescreen widget; never show a past day. */
public final class FitRocketWidgetData {
    public final String date;
    public final double used;
    public final double remaining;
    public final int budget;
    public final int waterMl;
    public final int waterGoalMl;

    private FitRocketWidgetData(String date,double used,int budget,int water,int goal){
        this.date=date;this.used=used;this.budget=budget;
        this.remaining=FitRocketPointLedger.tenth(Math.max(0,budget-used));
        this.waterMl=water;this.waterGoalMl=goal;
    }

    public static FitRocketWidgetData fromJson(String day,int budget,String mealsJson,
                                                 String waterJson,int waterGoal){
        if(day==null)throw new IllegalArgumentException("Datum fehlt");
        List<JSONObject> meals=new ArrayList<>();
        try {
            JSONArray items=new JSONArray(mealsJson==null?"[]":mealsJson);
            for(int i=0;i<items.length();i++){
                JSONObject m=items.optJSONObject(i);
                if(m!=null)meals.add(m);
            }
        }catch(Exception ignored){ /* Invalid saved data must not crash launcher. */ }
        int water=0;
        try { water=new JSONObject(waterJson==null?"{}":waterJson).optInt(day,0); }
        catch(Exception ignored){}
        double used;
        try { used=FitRocketPointLedger.used(meals,day); }
        catch(Exception invalid){used=0;}
        return new FitRocketWidgetData(day,used,Math.max(1,budget),
            Math.max(0,water),Math.max(250,waterGoal));
    }

    public String usedText(){return FitRocketPointLedger.display(used);}
    public String remainingText(){return FitRocketPointLedger.display(remaining);}
    public String waterText(){
        return String.format(java.util.Locale.GERMANY,"%.2f",waterMl/1000.0);
    }
    public String waterGoalText(){
        return String.format(java.util.Locale.GERMANY,"%.2f",waterGoalMl/1000.0);
    }
    public int waterPercent(){return Math.min(100,(int)Math.round(100.0*waterMl/waterGoalMl));}
}
