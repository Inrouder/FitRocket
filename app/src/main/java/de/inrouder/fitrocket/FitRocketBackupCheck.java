package de.inrouder.fitrocket;

import java.time.LocalDate;
import org.json.*;
/** Validate import shape and diary records before existing local data are replaced. */
public final class FitRocketBackupCheck {
 private FitRocketBackupCheck(){}
 public static String preview(JSONObject root){
  if(root==null)throw new IllegalArgumentException("Leeres Backup");
  int schema=root.optInt("schemaVersion",-1);
  if(schema<1||schema>7)throw new IllegalArgumentException("Nicht unterstützte Backup-Version: "+schema);
  JSONArray meals=root.optJSONArray("meals"),weights=root.optJSONArray("weights");
  if(meals==null||weights==null)throw new IllegalArgumentException("Mahlzeiten oder Gewichte fehlen");
  if(meals.length()>50000||weights.length()>50000)throw new IllegalArgumentException("Zu viele Einträge");
  for(int i=0;i<meals.length();i++){
   JSONObject m=meals.optJSONObject(i);
   if(m==null||m.optString("name").trim().isEmpty()||m.optString("name").length()>300||
     m.optInt("points",-1)<0||m.optInt("points",-1)>3000)throw new IllegalArgumentException("Ungültige Mahlzeit "+(i+1));
   try{LocalDate.parse(m.getString("date"));}
   catch(Exception e){throw new IllegalArgumentException("Ungültiges Datum in Mahlzeit "+(i+1));}
  }
  for(int i=0;i<weights.length();i++){
   JSONObject w=weights.optJSONObject(i);
   if(w==null)throw new IllegalArgumentException("Ungültige Gewichtsmessung");
   try{
    LocalDate.parse(w.getString("date"));
    double kg=w.getDouble("kg");
    if(!Double.isFinite(kg)||kg<20||kg>400)throw new Exception();
   }catch(Exception e){throw new IllegalArgumentException("Ungültiges Gewicht "+(i+1));}
  }
  JSONArray activities=root.optJSONArray("activities");
  if(activities!=null&&activities.length()>50000)throw new IllegalArgumentException("Zu viele Aktivitäten");
  JSONArray favorites=root.optJSONArray("favoriteMeals");
  if(favorites!=null){
   if(favorites.length()>300)throw new IllegalArgumentException("Zu viele Favoriten");
   for(int i=0;i<favorites.length();i++){
    JSONObject f=favorites.optJSONObject(i);
    if(f==null||f.optString("name").trim().isEmpty()||
      f.optInt("points",-1)<0||f.optInt("points",-1)>3000)
     throw new IllegalArgumentException("Ungültiger Favorit");
   }
  }
  return "Backup-Format: "+schema+"\nMahlzeiten: "+meals.length()+
   "\nGewichtseinträge: "+weights.length()+
   "\nAktivitäten: "+(activities==null?0:activities.length())+
   "\nGespeicherte Favoriten: "+(favorites==null?0:favorites.length())+
   "\nAlle geprüften Tagebuch-Einträge sind gültig.";
 }
}
