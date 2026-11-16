package de.inrouder.fitrocket;

import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketFastingBackupTest {
    private JSONObject backup()throws Exception{return new JSONObject().put("schemaVersion",7).put("meals",new JSONArray()).put("weights",new JSONArray());}
    @Test public void oldBackupsDefaultToEnabledRolloverAndEmptyFasting()throws Exception {
        for(int schema=1;schema<=7;schema++){
            JSONObject old=backup().put("schemaVersion",schema);
            assertTrue(FitRocketBackupCheck.rolloverEnabled(old));
            assertEquals(16,FitRocketBackupCheck.fasting(old).getInt("planHours"));
            assertFalse(FitRocketBackupCheck.fasting(old).has("active"));
            assertTrue(FitRocketBackupCheck.preview(old).contains("Fastenzeiten: 0"));
        }
    }
    @Test public void activeAndHistoryAndDisabledRolloverSurviveBackupRoundTrip()throws Exception {
        JSONObject state=FitRocketFasting.finish(FitRocketFasting.start(FitRocketFasting.empty(),100000),200000);
        state=FitRocketFasting.start(state,300000);
        state.getJSONObject("active").put("futureField","preserved");
        JSONObject exported=backup().put(FitRocketFasting.KEY,state).put(FitRocketPointLedger.ROLLOVER_KEY,false);
        JSONObject imported=new JSONObject(exported.toString());
        assertFalse(FitRocketBackupCheck.rolloverEnabled(imported));
        assertEquals(state.toString(),FitRocketBackupCheck.fasting(imported).toString());
        assertTrue(FitRocketBackupCheck.preview(imported).contains("1 + laufendes Fasten"));
        assertTrue(FitRocketBackupCheck.preview(imported).contains("Punkteübernahme: Aus"));
    }
    @Test public void malformedOptionalDataRejectsBackupBeforeReplacement()throws Exception {
        for(Object invalid:new Object[]{JSONObject.NULL,"oops",new JSONObject(),new JSONArray()}){
            try{FitRocketBackupCheck.preview(backup().put(FitRocketFasting.KEY,invalid));fail();}catch(IllegalArgumentException expected){}
        }
        JSONObject invalid=FitRocketFasting.start(FitRocketFasting.empty(),100000);
        invalid.getJSONObject("active").put("goal",0);
        try{FitRocketBackupCheck.preview(backup().put(FitRocketFasting.KEY,invalid));fail();}catch(IllegalArgumentException expected){}
        try{FitRocketBackupCheck.preview(backup().put(FitRocketPointLedger.ROLLOVER_KEY,"false"));fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void correctedHistoryAndMovedStartKeepExistingBackupSchema()throws Exception {
        long now=1795032000000L;
        JSONObject state=FitRocketFasting.start(FitRocketFasting.empty(),now-30*FitRocketFasting.HOUR,now);
        state=FitRocketFasting.finish(state,now-20*FitRocketFasting.HOUR,now);
        state=FitRocketFasting.editEnd(state,0,now-12*FitRocketFasting.HOUR,now);
        state=FitRocketFasting.start(state,now-FitRocketFasting.HOUR,now);
        state=FitRocketFasting.moveStart(state,now-2*FitRocketFasting.HOUR,now);
        JSONObject imported=FitRocketBackupCheck.fasting(new JSONObject(backup().put("fasting",state).toString()));
        assertEquals(state.toString(),imported.toString());assertEquals(1,imported.getJSONArray("history").length());
        assertEquals(now+14*FitRocketFasting.HOUR,imported.getJSONObject("active").getLong("goal"));
    }

}
