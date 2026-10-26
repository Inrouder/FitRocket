package de.inrouder.fitrocket;

import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketSyncRulesTest {
    @Test public void sameWorkoutFromTwoSourcesIsDuplicate() {
        long start=1_800_000_000_000L;
        assertTrue(FitRocketSyncRules.isDuplicate("2026-10-26","2026-10-26",0,0,30,31,start,start+90_000L));
        assertFalse(FitRocketSyncRules.isDuplicate("2026-10-26","2026-10-27",0,0,30,31,start,start));
    }
    @Test public void differentSessionsAreNotAccidentallyMerged() {
        long start=1_800_000_000_000L;
        assertFalse(FitRocketSyncRules.isDuplicate("2026-10-26","2026-10-26",3,0,30,30,start,start+50*60_000L));
        assertFalse(FitRocketSyncRules.isDuplicate("2026-10-26","2026-10-26",3,3,30,60,start,start+60_000L));
    }
    @Test public void oldManualWorkoutCanMatchBySportAndDuration() {
        assertTrue(FitRocketSyncRules.isDuplicate("2026-10-26","2026-10-26",3,3,30,31,0,100000));
        assertFalse(FitRocketSyncRules.isDuplicate("2026-10-26","2026-10-26",1,3,30,31,0,100000));
    }
    @Test public void weekIntervalIsHalfOpen(){
        assertTrue(FitRocketSyncRules.isSameWeek("2026-10-26","2026-10-26","2026-11-02"));
        assertTrue(FitRocketSyncRules.isSameWeek("2026-11-01","2026-10-26","2026-11-02"));
        assertFalse(FitRocketSyncRules.isSameWeek("2026-11-02","2026-10-26","2026-11-02"));
    }
}
