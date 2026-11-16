package de.inrouder.fitrocket;

import org.json.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.ToDoubleFunction;

/** A fictional pet economy. Never writes meals, food points or the weekly wallet. */
public final class FitRocketPet {
    public static final String KEY="pet";
    public static final int[] STAGES={0,80,240,600,1200}; // tenths of fictional food points
    public static final String[] STAGE_NAMES={"Sternen-Ei","Kleiner Entdecker","Wirbelwind","Treuer Begleiter","Sternenwächter"};
    public static final String[] ITEMS={"none","dog_ears","rocket_cap","star_crown","ball","rocket_toy"};
    public static final String[] ITEM_NAMES={"Ohne Kopfschmuck","Hundeöhrchen","Raketenmütze","Sternenkrone","Spielball","Spielzeugrakete"};
    public static final int[] PRICES={0,5,12,20,0,10};
    private static final int LIMIT=100000000;
    private FitRocketPet(){}
    public static JSONObject empty(){return new JSONObject();}
    private static LocalDate date(String value){
        try{LocalDate d=LocalDate.parse(value);if(d.getYear()<1999||d.getYear()>2101)throw new Exception();return d;}
        catch(Exception e){throw new IllegalArgumentException("Ungültiges Pet-Datum.");}
    }
    private static int integer(JSONObject state,String key){
        Object value=state.opt(key);
        if(!(value instanceof Number))throw new IllegalArgumentException("Ungültiger Pet-Zähler: "+key);
        double n=((Number)value).doubleValue();
        if(!Double.isFinite(n)||n<0||n>LIMIT||n!=Math.rint(n))throw new IllegalArgumentException("Ungültiger Pet-Zähler: "+key);
        return (int)n;
    }
    private static String name(String value){
        if(value==null||value.trim().isEmpty()||value.trim().length()>20)throw new IllegalArgumentException("Bitte einen Pet-Namen mit 1–20 Zeichen eingeben.");
        return value.trim();
    }
    public static JSONObject validated(JSONObject source){
        if(source==null)throw new IllegalArgumentException("Pet-Daten fehlen.");
        try{
            JSONObject state=new JSONObject(source.toString());
            if(state.length()==0)return state;
            name(state.getString("name"));
            LocalDate adopted=date(state.getString("adoptedOn")),harvest=date(state.getString("lastHarvestDate"));
            if(harvest.isBefore(adopted.minusDays(2)))throw new IllegalArgumentException("Ungültiger Futterverlauf.");
            integer(state,"foodTenths");integer(state,"fedTenths");integer(state,"stars");
            for(String key:new String[]{"lastFedOn","pettedOn","playedOn"}){
                String value=state.getString(key);
                if(!value.isEmpty()&&date(value).isBefore(adopted))throw new IllegalArgumentException("Pet-Aktion vor Adoption.");
            }
            JSONArray owned=state.getJSONArray("ownedItems");
            if(owned.length()>ITEMS.length)throw new IllegalArgumentException("Zu viele Pet-Gegenstände.");
            Set<String> unique=new HashSet<>();
            for(int i=0;i<owned.length();i++){
                String id=owned.getString(i);
                if(itemIndex(id)<0||!unique.add(id))throw new IllegalArgumentException("Ungültiger Pet-Gegenstand.");
            }
            if(!unique.contains("none")||!unique.contains("ball"))throw new IllegalArgumentException("Pet-Grundausstattung fehlt.");
            String head=state.getString("equippedHead"),toy=state.getString("equippedToy");
            if(!unique.contains(head)||!unique.contains(toy)||itemIndex(head)>3||itemIndex(toy)<4)
                throw new IllegalArgumentException("Ungültige Pet-Ausstattung.");
            return state;
        }catch(IllegalArgumentException e){throw e;}
        catch(Exception e){throw new IllegalArgumentException("Ungültige Pet-Daten.",e);}
    }
    public static JSONObject adopt(JSONObject source,LocalDate today,String petName){
        if(validated(source).length()>0)throw new IllegalArgumentException("Du hast bereits ein Pet.");
        date(today.toString());String n=name(petName);
        try{return validated(new JSONObject().put("name",n).put("adoptedOn",today.toString())
            .put("lastHarvestDate",today.minusDays(2).toString())
            .put("foodTenths",0).put("fedTenths",0).put("stars",0)
            .put("lastFedOn","").put("pettedOn","").put("playedOn","")
            .put("ownedItems",new JSONArray().put("none").put("ball"))
            .put("equippedHead","none").put("equippedToy","ball"));}
        catch(Exception e){throw new IllegalArgumentException("Pet konnte nicht adoptiert werden.",e);}
    }
    private static JSONObject adopted(JSONObject source){
        JSONObject state=validated(source);
        if(state.length()==0)throw new IllegalArgumentException("Adoptiere zuerst dein Pet.");
        return state;
    }
    /** Credit each completed date once, including yesterday as welcome food on adoption. */
    public static JSONObject harvest(JSONObject source,LocalDate today,int budget,ToDoubleFunction<LocalDate> consumed){
        JSONObject state=adopted(source);date(today.toString());
        if(budget<1||budget>200)throw new IllegalArgumentException("Ungültiges Tagesbudget.");
        LocalDate last=date(state.optString("lastHarvestDate")),end=today.minusDays(1);
        if(!end.isAfter(last))return state; // clock rollback / repeated open must not award again
        long days=ChronoUnit.DAYS.between(last,end);
        if(days>37000)throw new IllegalArgumentException("Futterzeitraum zu groß.");
        long earned=0;
        for(LocalDate d=last.plusDays(1);!d.isAfter(end);d=d.plusDays(1)){
            double used=consumed.applyAsDouble(d);
            if(!Double.isFinite(used)||used<0)throw new IllegalArgumentException("Ungültiger Tagesverbrauch.");
            earned+=Math.round(Math.max(0,budget-used)*10);
        }
        long total=integer(state,"foodTenths")+earned;
        if(total>LIMIT)throw new IllegalArgumentException("Futtervorrat zu groß.");
        try{return validated(state.put("foodTenths",total).put("lastHarvestDate",end.toString()));}
        catch(Exception e){throw new IllegalArgumentException("Futter konnte nicht eingesammelt werden.",e);}
    }
    private static void actionDate(JSONObject state,LocalDate today){
        date(today.toString());
        if(today.isBefore(date(state.optString("adoptedOn"))))throw new IllegalArgumentException("Bitte die Gerätezeit prüfen.");
        for(String key:new String[]{"lastFedOn","pettedOn","playedOn"}){
            String day=state.optString(key);
            if(!day.isEmpty()&&today.isBefore(date(day)))throw new IllegalArgumentException("Bitte die Gerätezeit prüfen.");
        }
    }
    public static JSONObject feed(JSONObject source,int tenths,LocalDate today){
        JSONObject state=adopted(source);actionDate(state,today);
        int food=integer(state,"foodTenths"),fed=integer(state,"fedTenths");
        if(tenths<1||tenths>food||((long)fed+tenths)>LIMIT)throw new IllegalArgumentException("Nicht genug fiktives Futter vorhanden.");
        try{return validated(state.put("foodTenths",food-tenths).put("fedTenths",fed+tenths)
            .put("stars",integer(state,"stars")+(fed+tenths)/10-fed/10).put("lastFedOn",today.toString()));}
        catch(Exception e){throw new IllegalArgumentException("Füttern fehlgeschlagen.",e);}
    }
    public static JSONObject care(JSONObject source,LocalDate today,boolean play){
        JSONObject state=adopted(source);actionDate(state,today);
        String key=play?"playedOn":"pettedOn";
        if(today.toString().equals(state.optString(key)))return state;
        try{return validated(state.put(key,today.toString()).put("stars",integer(state,"stars")+(play?2:1)));}
        catch(Exception e){throw new IllegalArgumentException("Pet-Aktion konnte nicht gespeichert werden.",e);}
    }
    public static JSONObject rename(JSONObject source,String petName){
        JSONObject state=adopted(source);String n=name(petName);
        try{return validated(state.put("name",n));}catch(Exception e){throw new IllegalArgumentException(e);}
    }
    public static int itemIndex(String id){for(int i=0;i<ITEMS.length;i++)if(ITEMS[i].equals(id))return i;return -1;}
    public static boolean owns(JSONObject state,String id){
        JSONArray list=state.optJSONArray("ownedItems");
        for(int i=0;list!=null&&i<list.length();i++)if(id.equals(list.optString(i)))return true;
        return false;
    }
    /** Buying and equipping is atomic; owned items never charge a second time. */
    public static JSONObject equip(JSONObject source,String id){
        JSONObject state=adopted(source);int index=itemIndex(id);
        if(index<0)throw new IllegalArgumentException("Unbekannter Pet-Gegenstand.");
        try{
            if(!owns(state,id)){
                int stars=integer(state,"stars");
                if(stars<PRICES[index])throw new IllegalArgumentException("Dafür fehlen noch Sterne.");
                state.put("stars",stars-PRICES[index]);state.getJSONArray("ownedItems").put(id);
            }
            return validated(state.put(index<4?"equippedHead":"equippedToy",id));
        }catch(IllegalArgumentException e){throw e;}
        catch(Exception e){throw new IllegalArgumentException("Ausstattung konnte nicht geändert werden.",e);}
    }
    public static int stage(JSONObject state){int xp=state.optInt("fedTenths");int stage=0;while(stage<4&&xp>=STAGES[stage+1])stage++;return stage;}
    public static int missedDays(JSONObject state,LocalDate today){
        String fed=state.optString("lastFedOn");
        LocalDate anchor=fed.isEmpty()?date(state.optString("adoptedOn")).minusDays(1):date(fed);
        return (int)Math.max(0,ChronoUnit.DAYS.between(anchor,today)-1);
    }
    /** Sprite rows: happy, calm, sad, grumpy, toothy monster. Care can soften a short bad mood. */
    public static int mood(JSONObject state,LocalDate today){
        int missing=missedDays(state,today);
        boolean cared=today.toString().equals(state.optString("pettedOn"))||today.toString().equals(state.optString("playedOn"));
        if(missing>=3)return 4;
        if(missing>0)return Math.max(1,missing+1-(cared?1:0));
        return cared||today.toString().equals(state.optString("lastFedOn"))?0:1;
    }
    public static String points(int tenths){return tenths%10==0?String.valueOf(tenths/10):(tenths/10)+","+(tenths%10);}
}
