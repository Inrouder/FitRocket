package de.inrouder.fitrocket;

import org.json.*;
import org.junit.Test;
import java.time.*;
import static org.junit.Assert.*;

public class FitRocketFastingTest {
    private static final long NOW=Instant.parse("2026-11-16T20:00:00Z").toEpochMilli();
    private JSONObject started(){return FitRocketFasting.start(FitRocketFasting.empty(),NOW);}
    @Test public void allSixPlansSetCorrectTargetAndEatingWindow()throws Exception {
        for(int h:new int[]{12,14,16,17,18,20}){
            JSONObject selected=FitRocketFasting.selectPlan(FitRocketFasting.empty(),h);
            JSONObject active=FitRocketFasting.start(selected,NOW).getJSONObject("active");
            assertEquals(h+":"+(24-h),FitRocketFasting.plan(h));
            assertEquals(NOW+h*FitRocketFasting.HOUR,active.getLong("goal"));
        }
    }
    @Test public void timerSurvivesJsonReloadAndMidnightWithoutTickWrites()throws Exception {
        JSONObject state=started();String saved=state.toString();
        JSONObject restored=FitRocketFasting.validated(new JSONObject(saved));
        JSONObject active=restored.getJSONObject("active");
        long nextDay=NOW+5*FitRocketFasting.HOUR+42000;
        assertEquals("05:00:42",FitRocketFasting.duration(FitRocketFasting.elapsed(active,nextDay)));
        assertEquals(5.0/16,FitRocketFasting.progress(active,NOW+5*FitRocketFasting.HOUR),0.00001);
        assertEquals(saved,state.toString());
        assertEquals(1,FitRocketFasting.progress(active,NOW+18*FitRocketFasting.HOUR),0);
        assertEquals(0,FitRocketFasting.elapsed(active,NOW-1000));
    }
    @Test public void dstChangeUsesActualElapsedHours()throws Exception {
        long start=ZonedDateTime.of(2026,10,24,20,0,0,0,ZoneId.of("Europe/Berlin")).toInstant().toEpochMilli();
        JSONObject active=FitRocketFasting.start(FitRocketFasting.empty(),start).getJSONObject("active");
        assertEquals(16*FitRocketFasting.HOUR,active.getLong("goal")-start);
        assertEquals(11,Instant.ofEpochMilli(active.getLong("goal")).atZone(ZoneId.of("Europe/Berlin")).getHour());
    }
    @Test public void editsAndPlanChangeKeepStartAndUnknownFields()throws Exception {
        JSONObject source=started().put("futureField","keep");
        String original=source.toString();
        JSONObject edited=FitRocketFasting.edit(source,NOW-FitRocketFasting.HOUR,NOW+12*FitRocketFasting.HOUR,NOW);
        assertEquals(original,source.toString());
        JSONObject changed=FitRocketFasting.selectPlan(edited,18);
        assertEquals(NOW-FitRocketFasting.HOUR,changed.getJSONObject("active").getLong("start"));
        assertEquals(NOW+17*FitRocketFasting.HOUR,changed.getJSONObject("active").getLong("goal"));
        assertEquals("keep",changed.getString("futureField"));
    }
    @Test public void earlyFinishAndCompletedFinishAreSavedOnce()throws Exception {
        JSONObject source=started();
        JSONObject finished=FitRocketFasting.finish(source,NOW+2*FitRocketFasting.HOUR);
        assertFalse(finished.has("active"));assertTrue(source.has("active"));
        assertEquals(NOW+2*FitRocketFasting.HOUR,finished.getJSONArray("history").getJSONObject(0).getLong("end"));
        try{FitRocketFasting.finish(finished,NOW+3*FitRocketFasting.HOUR);fail();}catch(IllegalArgumentException expected){}
        JSONObject restarted=FitRocketFasting.start(finished,NOW+24*FitRocketFasting.HOUR);
        JSONObject completed=FitRocketFasting.finish(restarted,NOW+42*FitRocketFasting.HOUR);
        assertEquals(2,completed.getJSONArray("history").length());
        assertEquals(NOW+42*FitRocketFasting.HOUR,completed.getJSONArray("history").getJSONObject(0).getLong("end"));
    }
    @Test public void cancelPreservesHistoryAndDoesNotCreateAnEntry()throws Exception {
        JSONObject finished=FitRocketFasting.finish(started(),NOW+FitRocketFasting.HOUR);
        JSONObject state=FitRocketFasting.start(finished,NOW+24*FitRocketFasting.HOUR);
        JSONObject cancelled=FitRocketFasting.cancel(state);
        assertEquals(finished.toString(),cancelled.toString());assertTrue(state.has("active"));
    }
    @Test public void historyIsBoundedAndNewestFirst()throws Exception {
        JSONObject state=FitRocketFasting.empty();
        for(int i=0;i<105;i++){
            long start=NOW+i*24*FitRocketFasting.HOUR;
            state=FitRocketFasting.finish(FitRocketFasting.start(state,start),start+12*FitRocketFasting.HOUR);
        }
        JSONArray history=state.getJSONArray("history");
        assertEquals(100,history.length());
        assertEquals(NOW+104*24*FitRocketFasting.HOUR,history.getJSONObject(0).getLong("start"));
    }
    @Test public void invalidChangesNeverMutatePersistedState()throws Exception {
        JSONObject source=started();String saved=source.toString();
        Runnable[] invalid={
            ()->FitRocketFasting.start(source,NOW),
            ()->FitRocketFasting.selectPlan(source,23),
            ()->FitRocketFasting.edit(source,NOW+1,NOW+10000,NOW),
            ()->FitRocketFasting.edit(source,NOW,NOW,NOW),
            ()->FitRocketFasting.edit(source,NOW,NOW+25*FitRocketFasting.HOUR,NOW),
            ()->FitRocketFasting.finish(source,NOW-1)
        };
        for(Runnable action:invalid){try{action.run();fail();}catch(IllegalArgumentException expected){}assertEquals(saved,source.toString());}
    }
}
