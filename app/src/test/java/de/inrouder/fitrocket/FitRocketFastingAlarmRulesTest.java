package de.inrouder.fitrocket;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketFastingAlarmRulesTest {
    private static final long NOW=1795032000000L;
    private JSONObject active(){return FitRocketFasting.start(FitRocketFasting.empty(),NOW);}
    private long goal(){return NOW+16*FitRocketFasting.HOUR;}
    @Test public void schedulesSavedGoalAndCatchesUpAfterReboot() {
        assertEquals(goal(),FitRocketFastingAlarmRules.nextTrigger(active(),true,NOW));
        assertEquals(goal()+11000,FitRocketFastingAlarmRules.nextTrigger(active(),true,goal()+10000));
        assertEquals(0,FitRocketFastingAlarmRules.nextTrigger(active(),false,NOW));
        assertEquals(0,FitRocketFastingAlarmRules.nextTrigger(FitRocketFasting.empty(),true,NOW));
    }
    @Test public void cannotNotifyBeforeGoalOrForStaleSession() {
        assertFalse(FitRocketFastingAlarmRules.due(active(),true,NOW,goal(),goal()-1));
        assertTrue(FitRocketFastingAlarmRules.due(active(),true,NOW,goal(),goal()));
        assertFalse(FitRocketFastingAlarmRules.due(active(),true,NOW-1,goal(),goal()));
        assertFalse(FitRocketFastingAlarmRules.due(active(),false,NOW,goal(),goal()));
    }
    @Test public void goalOrStartEditsInvalidateOldPendingIntent() {
        JSONObject extended=FitRocketFasting.edit(active(),NOW,goal()+FitRocketFasting.HOUR,NOW);
        assertFalse(FitRocketFastingAlarmRules.due(extended,true,NOW,goal(),goal()));
        assertEquals(goal()+FitRocketFasting.HOUR,FitRocketFastingAlarmRules.nextTrigger(extended,true,NOW));
        JSONObject moved=FitRocketFasting.edit(active(),NOW-10000,goal(),NOW);
        assertFalse(FitRocketFastingAlarmRules.due(moved,true,NOW,goal(),goal()));
    }
    @Test public void notificationIsDeliveredOnlyOncePerGoalAndMarkerSurvivesReload()throws Exception {
        JSONObject source=active(),sent=FitRocketFastingAlarmRules.notified(source,NOW,goal(),goal());
        assertFalse(source.getJSONObject("active").has("notifiedGoal"));
        JSONObject restored=FitRocketFasting.validated(new JSONObject(sent.toString()));
        assertFalse(FitRocketFastingAlarmRules.due(restored,true,NOW,goal(),goal()+10000));
        assertEquals(0,FitRocketFastingAlarmRules.nextTrigger(restored,true,goal()+10000));
        try{FitRocketFastingAlarmRules.notified(restored,NOW,goal(),goal()+10000);fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void newGoalAfterAlreadyNotifyingCanNotifyAgain() {
        JSONObject sent=FitRocketFastingAlarmRules.notified(active(),NOW,goal(),goal());
        JSONObject changed=FitRocketFasting.edit(sent,NOW,goal()+FitRocketFasting.HOUR,goal());
        assertTrue(FitRocketFastingAlarmRules.due(changed,true,NOW,goal()+FitRocketFasting.HOUR,goal()+FitRocketFasting.HOUR));
    }
    @Test public void endingOrCancellingPreventsDelivery() {
        JSONObject finished=FitRocketFasting.finish(active(),goal()),cancelled=FitRocketFasting.cancel(active());
        for(JSONObject state:new JSONObject[]{finished,cancelled}){
            assertEquals(0,FitRocketFastingAlarmRules.nextTrigger(state,true,goal()));
            assertFalse(FitRocketFastingAlarmRules.due(state,true,NOW,goal(),goal()));
        }
    }
    @Test public void movingStartReplacesGoalAndClearsOldNotificationMarker()throws Exception {
        JSONObject sent=FitRocketFastingAlarmRules.notified(active(),NOW,goal(),goal());
        JSONObject moved=FitRocketFasting.moveStart(sent,NOW+FitRocketFasting.HOUR,goal());
        long newGoal=goal()+FitRocketFasting.HOUR;
        assertEquals(newGoal,FitRocketFastingAlarmRules.nextTrigger(moved,true,goal()));
        assertFalse(FitRocketFastingAlarmRules.due(moved,true,NOW,goal(),newGoal));
        assertTrue(FitRocketFastingAlarmRules.due(moved,true,NOW+FitRocketFasting.HOUR,newGoal,newGoal));
        JSONObject reverted=FitRocketFasting.moveStart(moved,NOW,goal());
        assertTrue(FitRocketFastingAlarmRules.due(reverted,true,NOW,goal(),goal()));
        assertFalse(sent.getJSONObject("active").isNull("notifiedGoal"));
    }

}
