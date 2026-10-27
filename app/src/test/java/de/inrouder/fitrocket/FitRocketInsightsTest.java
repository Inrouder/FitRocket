package de.inrouder.fitrocket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class FitRocketInsightsTest {
 @Test public void weekCountsOnlyLoggedDaysAndCurrentWeek() throws Exception {
  JSONArray meals=new JSONArray()
    .put(new JSONObject().put("date","2026-10-26").put("name","Breakfast").put("points",12))
    .put(new JSONObject().put("date","2026-10-27").put("name","Lunch").put("points",26))
    .put(new JSONObject().put("date","2026-10-25").put("name","Old").put("points",90));
  JSONObject water=new JSONObject().put("2026-10-26",1500).put("2026-10-27",250);
  JSONObject summary=FitRocketInsights.week(LocalDate.parse("2026-10-27"),23,1500,meals,water);
  assertEquals(38,summary.getInt("totalPoints"));
  assertEquals(2,summary.getInt("loggedDays"));
  assertEquals(1,summary.getInt("withinBudget"));
  assertEquals(1,summary.getInt("waterGoalDays"));
  assertEquals(1750,summary.getInt("waterTotal"));
 }
 @Test public void missingWeightsDoNotFakeTrends() {
  assertNull(FitRocketInsights.movingAverage(Collections.emptyList(),
     LocalDate.parse("2026-10-27"),7));
  assertNull(FitRocketInsights.periodChange(Collections.emptyList(),
     LocalDate.parse("2026-10-27"),30));
 }
 @Test public void rollingAverageIgnoresOlderMeasurements() throws Exception {
  JSONObject w1=new JSONObject().put("date","2026-10-20").put("kg",90.0);
  JSONObject w2=new JSONObject().put("date","2026-10-25").put("kg",84.0);
  JSONObject w3=new JSONObject().put("date","2026-10-27").put("kg",82.0);
  assertEquals(83.0,FitRocketInsights.movingAverage(Arrays.asList(w1,w2,w3),
     LocalDate.parse("2026-10-27"),7),0.00001);
 }
 @Test public void backupPreviewVerifiesExistingEntries() throws Exception {
  JSONObject backup=new JSONObject().put("schemaVersion",7)
   .put("meals",new JSONArray().put(new JSONObject()
      .put("name","Mahlzeit").put("date","2026-10-27").put("points",11)))
   .put("weights",new JSONArray().put(new JSONObject()
      .put("date","2026-10-27").put("kg",81.2)))
   .put("favoriteMeals",new JSONArray().put(new JSONObject()
      .put("name","Test").put("points",4)));
  assertTrue(FitRocketBackupCheck.preview(backup).contains("Favoriten: 1"));
 }
 @Test(expected=IllegalArgumentException.class)
 public void rejectsBrokenBackupWithoutDestroyingOriginal() throws Exception {
  JSONObject backup=new JSONObject().put("schemaVersion",7)
   .put("meals",new JSONArray().put(new JSONObject()
    .put("name","Bad").put("date","tomorrow").put("points",-1)))
   .put("weights",new JSONArray());
  FitRocketBackupCheck.preview(backup);
 }
}
