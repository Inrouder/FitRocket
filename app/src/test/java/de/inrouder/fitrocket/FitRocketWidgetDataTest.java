package de.inrouder.fitrocket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketWidgetDataTest {
    @Test public void pointsAndWaterUseTodayOnly() throws Exception {
        String meals=new JSONArray()
            .put(new JSONObject().put("date","2026-10-27").put("points",4.2))
            .put(new JSONObject().put("date","2026-10-27").put("points",0.2))
            .put(new JSONObject().put("date","2026-10-26").put("points",9))
            .toString();
        FitRocketWidgetData data=FitRocketWidgetData.fromJson(
            "2026-10-27",23,meals,"{\"2026-10-27\":750,\"2026-10-26\":1500}",1500);
        assertEquals("4,4",data.usedText());
        assertEquals("18,6",data.remainingText());
        assertEquals("0,75",data.waterText());
        assertEquals("1,50",data.waterGoalText());
        assertEquals(50,data.waterPercent());
    }
    @Test public void midnightDisplaysNewDayWithoutOldValues() throws Exception {
        String meals="[{ \"date\":\"2026-10-27\", \"points\":12 }]";
        FitRocketWidgetData next=FitRocketWidgetData.fromJson(
            "2026-10-28",23,meals,"{\"2026-10-27\":1250}",1500);
        assertEquals("0",next.usedText());
        assertEquals("23",next.remainingText());
        assertEquals("0,00",next.waterText());
        assertEquals(0,next.waterPercent());
    }
    @Test public void exceedingWaterGoalCapsBarButShowsActualAmount(){
        FitRocketWidgetData data=FitRocketWidgetData.fromJson(
            "2026-10-27",23,"[]","{\"2026-10-27\":2250}",1500);
        assertEquals(100,data.waterPercent());
        assertEquals("2,25",data.waterText());
    }
    @Test public void overBudgetDoesNotShowNegativeRemaining()throws Exception{
        FitRocketWidgetData data=FitRocketWidgetData.fromJson(
            "2026-10-27",23,"[{\"date\":\"2026-10-27\",\"points\":24}]",
            "{}",1500);
        assertEquals("24",data.usedText());
        assertEquals("0",data.remainingText());
    }
    @Test public void invalidSavedJsonDoesNotCrashWidget(){
        FitRocketWidgetData data=FitRocketWidgetData.fromJson(
            "2026-10-27",23,"not-json","bad-water",-9);
        assertEquals("23",data.remainingText());
        assertEquals("0,00",data.waterText());
        assertEquals(0,data.waterPercent());
    }
}
