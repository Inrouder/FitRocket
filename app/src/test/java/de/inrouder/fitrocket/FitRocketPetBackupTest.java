package de.inrouder.fitrocket;

import org.json.*;
import org.junit.Test;
import java.time.LocalDate;
import static org.junit.Assert.*;

public class FitRocketPetBackupTest {
    private JSONObject backup()throws Exception{return new JSONObject().put("schemaVersion",7).put("meals",new JSONArray()).put("weights",new JSONArray());}
    @Test public void oldBackupsHaveNoPetAndEnableFastingReminder()throws Exception {
        for(int schema=1;schema<=7;schema++){
            JSONObject old=backup().put("schemaVersion",schema);
            assertEquals(0,FitRocketBackupCheck.pet(old).length());assertTrue(FitRocketBackupCheck.fastingReminderEnabled(old));
            assertTrue(FitRocketBackupCheck.preview(old).contains("Pet: Nicht adoptiert"));
        }
    }
    @Test public void completeGameAndAlarmPreferencesSurviveRoundTrip()throws Exception {
        LocalDate day=LocalDate.of(2026,11,18);
        JSONObject pet=FitRocketPet.adopt(FitRocketPet.empty(),day,"Novi");
        pet=FitRocketPet.harvest(pet,day,23,d->0);pet=FitRocketPet.feed(pet,100,day);pet=FitRocketPet.care(pet,day,false);pet=FitRocketPet.equip(pet,"dog_ears");
        pet.put("futureField","preserved");
        JSONObject root=backup().put(FitRocketPet.KEY,pet).put(FitRocketFastingAlarmRules.ENABLED_KEY,false);
        JSONObject restored=new JSONObject(root.toString());
        assertEquals(pet.toString(),FitRocketBackupCheck.pet(restored).toString());assertFalse(FitRocketBackupCheck.fastingReminderEnabled(restored));
        assertTrue(FitRocketBackupCheck.preview(restored).contains("Novi · Stufe 2"));
        assertEquals(130,FitRocketBackupCheck.pet(restored).getInt("foodTenths"));
        assertEquals(130,FitRocketPet.harvest(FitRocketBackupCheck.pet(restored),day,23,d->0).getInt("foodTenths"));
    }
    @Test public void corruptPetOrPreferenceRejectsBackupBeforeReplacement()throws Exception {
        for(Object invalid:new Object[]{JSONObject.NULL,"bad",new JSONArray(),new JSONObject().put("name","Novi")}){
            try{FitRocketBackupCheck.preview(backup().put(FitRocketPet.KEY,invalid));fail();}catch(IllegalArgumentException expected){}
        }
        for(Object invalid:new Object[]{"true",1,JSONObject.NULL}){
            try{FitRocketBackupCheck.preview(backup().put(FitRocketFastingAlarmRules.ENABLED_KEY,invalid));fail();}catch(IllegalArgumentException expected){}
        }
    }
    @Test public void corruptCurrencyAndEquipmentAreRejected()throws Exception {
        JSONObject source=FitRocketPet.adopt(FitRocketPet.empty(),LocalDate.of(2026,11,18),"Novi");
        for(Object invalid:new Object[]{-1,1.5,"10",JSONObject.NULL}){
            JSONObject changed=new JSONObject(source.toString()).put("foodTenths",invalid);
            try{FitRocketPet.validated(changed);fail();}catch(IllegalArgumentException expected){}
        }
        JSONObject changed=new JSONObject(source.toString()).put("equippedHead","star_crown");
        try{FitRocketPet.validated(changed);fail();}catch(IllegalArgumentException expected){}
    }
}
