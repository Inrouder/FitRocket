package de.inrouder.fitrocket;

import java.time.LocalDate;
import java.util.*;
import org.json.*;

/** Read-only local weekly and weight trend statistics. */
public final class FitRocketInsights {
 private FitRocketInsights(){}
 public static Double movingAverage(List<JSONObject> weights,LocalDate today,int days){
  if(days<1)throw new IllegalArgumentException("days");
  TreeMap<LocalDate,Double> map=new TreeMap<>();
  for(JSONObject w:weights)try{
   LocalDate d=LocalDate.parse(w.getString("date"));double kg=w.getDouble("kg");
   if(Double.isFinite(kg)&&kg>0&&!d.isAfter(today))map.put(d,kg);
  }catch(Exception ignored){}
  double sum=0;int count=0;
  for(Map.Entry<LocalDate,Double> e:map.tailMap(today.minusDays(days-1),true).entrySet()){
   sum+=e.getValue();count++;
  }
  return count>0?sum/count:null;
 }
 public static Double periodChange(List<JSONObject> weights,LocalDate today,int days){
  Double current=movingAverage(weights,today,7);
  Double previous=movingAverage(weights,today.minusDays(days),7);
  return current!=null&&previous!=null?current-previous:null;
 }
 public static JSONObject week(LocalDate today,int budget,int waterGoal,JSONArray meals,JSONObject water){
  LocalDate monday=today.minusDays(today.getDayOfWeek().getValue()-1);
  int days=0,loggedDays=0,points=0,within=0,waterDays=0,waterTotal=0;
  JSONArray daily=new JSONArray();
  for(LocalDate d=monday;!d.isAfter(today);d=d.plusDays(1)){
   int food=0;boolean logged=false;
   for(int i=0;i<meals.length();i++){
    JSONObject m=meals.optJSONObject(i);
    if(m!=null&&d.toString().equals(m.optString("date"))){
     food+=Math.max(0,m.optInt("points"));logged=true;
    }
   }
   int ml=water.optInt(d.toString(),0);
   JSONObject row=new JSONObject();
   try{row.put("date",d.toString()).put("points",food).put("waterMl",ml).put("logged",logged);daily.put(row);}
   catch(JSONException e){throw new IllegalStateException(e);}
   days++;points+=food;waterTotal+=Math.max(0,ml);
   if(logged){loggedDays++;if(food<=budget)within++;}
   if(ml>=waterGoal)waterDays++;
  }
  JSONObject result=new JSONObject();
  try{result.put("from",monday.toString()).put("through",today.toString())
     .put("days",days).put("loggedDays",loggedDays).put("withinBudget",within)
     .put("totalPoints",points).put("waterTotal",waterTotal)
     .put("waterGoalDays",waterDays).put("daysDetail",daily);}
  catch(JSONException e){throw new IllegalStateException(e);}
  return result;
 }
}
