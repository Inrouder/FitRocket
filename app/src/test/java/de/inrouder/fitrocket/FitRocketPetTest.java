package de.inrouder.fitrocket;

import org.json.*;
import org.junit.Test;
import java.time.LocalDate;
import java.util.*;
import static org.junit.Assert.*;

public class FitRocketPetTest {
    private static final LocalDate DAY=LocalDate.of(2026,11,18);
    private JSONObject pet(){return FitRocketPet.adopt(FitRocketPet.empty(),DAY,"Novi");}
    private JSONObject stocked(){return FitRocketPet.harvest(pet(),DAY,23,d->3);}
    @Test public void allUnusedPointsBecomeFictionalFoodWithoutFourPointCap()throws Exception {
        JSONObject source=pet(),next=FitRocketPet.harvest(source,DAY,23,d->{assertTrue(d.isBefore(DAY));return 3;});
        assertEquals(200,next.getInt("foodTenths")); // all 20 points, not the four-point weekly cap
        assertEquals(0,source.getInt("foodTenths"));
        assertEquals(DAY.minusDays(1).toString(),next.getString("lastHarvestDate"));
        assertEquals(0,next.getInt("fedTenths"));
    }
    @Test public void decimalSavingsAndOverBudgetDaysAreHandled()throws Exception {
        JSONObject partial=FitRocketPet.harvest(pet(),DAY,23,d->22.8);
        assertEquals(2,partial.getInt("foodTenths"));
        assertEquals(0,FitRocketPet.harvest(pet(),DAY,23,d->26).getInt("foodTenths"));
        assertEquals(230,FitRocketPet.harvest(pet(),DAY,23,d->0).getInt("foodTenths"));
    }
    @Test public void repeatedOpensAndHistoricalCorrectionsDoNotCreditDateAgain()throws Exception {
        JSONObject first=stocked();
        JSONObject again=FitRocketPet.harvest(first,DAY,23,d->0);
        assertEquals(200,again.getInt("foodTenths"));
        JSONObject fed=FitRocketPet.feed(again,10,DAY);
        assertEquals(190,FitRocketPet.harvest(fed,DAY,23,d->0).getInt("foodTenths"));
        JSONObject tomorrow=FitRocketPet.harvest(fed,DAY.plusDays(1),23,d->6.3);
        assertEquals(357,tomorrow.getInt("foodTenths"));
    }
    @Test public void unopenedDaysAreCollectedOnceAndClockRollbackDoesNotDuplicate()throws Exception {
        JSONObject first=FitRocketPet.harvest(pet(),DAY.plusDays(4),23,d->3);
        assertEquals(1000,first.getInt("foodTenths")); // Tue through Sat
        JSONObject rollback=FitRocketPet.harvest(first,DAY,23,d->0);
        assertEquals(first.toString(),rollback.toString());
        JSONObject second=FitRocketPet.harvest(first,DAY.plusDays(5),23,d->3);
        assertEquals(1200,second.getInt("foodTenths"));
    }
    @Test public void adoptionDoesNotHarvestYearsOfPreExistingDiary()throws Exception {
        List<LocalDate> requested=new ArrayList<>();
        FitRocketPet.harvest(pet(),DAY,23,d->{requested.add(d);return 0;});
        assertEquals(Collections.singletonList(DAY.minusDays(1)),requested);
        try{FitRocketPet.adopt(pet(),DAY,"Another");fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void gameDoesNotChangeMealsFoodPointsOrRealWeeklyWallet()throws Exception {
        List<JSONObject> meals=Arrays.asList(
            new JSONObject().put("date",DAY.minusDays(2).toString()).put("points",19),
            new JSONObject().put("date",DAY.minusDays(1).toString()).put("points",3),
            new JSONObject().put("date",DAY.toString()).put("points",0.2));
        String before=meals.toString();
        double realBefore=FitRocketPointLedger.weeklyAvailable(28,
            FitRocketPointLedger.weekRollovers(DAY,23,d->FitRocketPointLedger.used(meals,d.toString())),5,
            FitRocketPointLedger.weekSpent(DAY,23,d->FitRocketPointLedger.used(meals,d.toString())));
        JSONObject state=FitRocketPet.harvest(pet(),DAY,23,d->FitRocketPointLedger.used(meals,d.toString()));
        state=FitRocketPet.feed(state,200,DAY);
        state=FitRocketPet.care(state,DAY,false);state=FitRocketPet.equip(state,"dog_ears");
        assertEquals(before,meals.toString());
        double realAfter=FitRocketPointLedger.weeklyAvailable(28,
            FitRocketPointLedger.weekRollovers(DAY,23,d->FitRocketPointLedger.used(meals,d.toString())),5,
            FitRocketPointLedger.weekSpent(DAY,23,d->FitRocketPointLedger.used(meals,d.toString())));
        assertEquals(realBefore,realAfter,0);assertEquals(41,realAfter,0);
        assertEquals(0.2,FitRocketPointLedger.used(meals,DAY.toString()),0);
    }
    @Test public void feedingChangesOnlyPetAndFractionalStarsAccumulate()throws Exception {
        JSONObject state=stocked();
        for(int i=0;i<10;i++)state=FitRocketPet.feed(state,1,DAY);
        assertEquals(190,state.getInt("foodTenths"));assertEquals(10,state.getInt("fedTenths"));
        assertEquals(1,state.getInt("stars"));assertEquals(DAY.toString(),state.getString("lastFedOn"));
        assertEquals("0,2",FitRocketPet.points(2));assertEquals("23",FitRocketPet.points(230));
    }
    @Test public void feedingGrowsThroughFiveStagesWithoutRegression()throws Exception {
        JSONObject state=FitRocketPet.harvest(pet(),DAY.plusDays(10),23,d->0);
        assertEquals(0,FitRocketPet.stage(state));
        for(int i=1;i<5;i++){
            int amount=FitRocketPet.STAGES[i]-state.getInt("fedTenths");
            state=FitRocketPet.feed(state,amount,DAY.plusDays(10));assertEquals(i,FitRocketPet.stage(state));
        }
        assertEquals(4,FitRocketPet.stage(state));assertEquals(4,FitRocketPet.mood(state,DAY.plusDays(15)));
    }
    @Test public void oneMissedDayIsSadThenGrumpyThenToothyMonster()throws Exception {
        JSONObject state=pet();
        assertEquals(1,FitRocketPet.mood(state,DAY));
        assertEquals(2,FitRocketPet.mood(state,DAY.plusDays(1)));
        assertEquals(3,FitRocketPet.mood(state,DAY.plusDays(2)));
        assertEquals(4,FitRocketPet.mood(state,DAY.plusDays(3)));
        JSONObject cared=FitRocketPet.care(state,DAY.plusDays(2),false);
        assertEquals(2,FitRocketPet.mood(cared,DAY.plusDays(2)));
        JSONObject fed=FitRocketPet.feed(FitRocketPet.harvest(cared,DAY.plusDays(3),23,d->0),10,DAY.plusDays(3));
        assertEquals(0,FitRocketPet.mood(fed,DAY.plusDays(3)));
        assertEquals(0,FitRocketPet.missedDays(fed,DAY.plusDays(4)));
        assertEquals(2,FitRocketPet.mood(fed,DAY.plusDays(5)));
    }
    @Test public void careRewardsOncePerCalendarDayAndDoesNotConsumeFood()throws Exception {
        JSONObject state=FitRocketPet.care(stocked(),DAY,false);
        state=FitRocketPet.care(state,DAY,false);state=FitRocketPet.care(state,DAY,true);state=FitRocketPet.care(state,DAY,true);
        assertEquals(3,state.getInt("stars"));assertEquals(200,state.getInt("foodTenths"));assertEquals(0,state.getInt("fedTenths"));
        assertEquals(0,FitRocketPet.mood(state,DAY));
        state=FitRocketPet.care(state,DAY.plusDays(1),false);assertEquals(4,state.getInt("stars"));
        final JSONObject tomorrow=state;
        try{FitRocketPet.care(tomorrow,DAY,true);fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void accessoriesChargeOnceAndEquippingKeepsToyAndHeadIndependent()throws Exception {
        JSONObject state=FitRocketPet.feed(stocked(),100,DAY);
        state=FitRocketPet.equip(state,"dog_ears");assertEquals(5,state.getInt("stars"));
        state=FitRocketPet.equip(state,"none");state=FitRocketPet.equip(state,"dog_ears");assertEquals(5,state.getInt("stars"));
        assertEquals("dog_ears",state.getString("equippedHead"));assertEquals("ball",state.getString("equippedToy"));
        state=FitRocketPet.feed(state,50,DAY);state=FitRocketPet.equip(state,"rocket_toy");
        assertEquals("dog_ears",state.getString("equippedHead"));assertEquals("rocket_toy",state.getString("equippedToy"));
        assertEquals(0,state.getInt("stars"));
    }
    @Test public void invalidActionsDoNotMutateSavedState()throws Exception {
        JSONObject source=stocked();String saved=source.toString();
        Runnable[] invalid={()->FitRocketPet.feed(source,0,DAY),()->FitRocketPet.feed(source,201,DAY),
            ()->FitRocketPet.equip(source,"star_crown"),()->FitRocketPet.equip(source,"unknown"),
            ()->FitRocketPet.rename(source,"   "),()->FitRocketPet.harvest(source,DAY,0,d->0),
            ()->FitRocketPet.harvest(source,DAY.plusDays(1),23,d->Double.NaN)};
        for(Runnable action:invalid){try{action.run();fail();}catch(IllegalArgumentException expected){}assertEquals(saved,source.toString());}
    }
    @Test public void renameAndJsonReloadKeepFoodEquipmentAndUnknownMetadata()throws Exception {
        JSONObject state=FitRocketPet.feed(stocked(),100,DAY);state=FitRocketPet.equip(state,"dog_ears");
        state.put("futureField",new JSONObject().put("keep",true));
        state=FitRocketPet.rename(state,"  Lila  ");
        JSONObject restored=FitRocketPet.validated(new JSONObject(state.toString()));
        assertEquals("Lila",restored.getString("name"));assertTrue(restored.getJSONObject("futureField").getBoolean("keep"));
        assertEquals("dog_ears",restored.getString("equippedHead"));assertEquals(100,restored.getInt("foodTenths"));
    }
}
