package de.inrouder.fitrocket;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketAiFallbackTest {
    private static class Target implements FitRocketAiService.Callback {
        int successes,failures,notices,retries;boolean cancelled,cancelOnNotice;
        public boolean isCancelled(){return cancelled;}
        public void onSuccess(JSONObject result){successes++;}
        public void onFailure(String message){failures++;}
        public void onRetry(int attempt,int maximum,long wait){retries++;}
        public void onProviderFallback(){notices++;if(cancelOnNotice)cancelled=true;}
    }
    @Test public void unavailableWithoutOptInRemainsAnOrdinaryFailure(){
        Target target=new Target();target.onUnavailable("503");assertEquals(1,target.failures);assertEquals(0,target.notices);
    }
    @Test public void reserveStartsExactlyOnceAndNeverReceivesLatePrimaryResult(){
        Target target=new Target();int[] started={0};
        FitRocketAiFallback fallback=new FitRocketAiFallback(target,()->started[0]++);
        fallback.onRetry(2,3,2000);fallback.onUnavailable("503");fallback.onUnavailable("503");
        fallback.onSuccess(new JSONObject());fallback.onFailure("late");
        assertEquals(1,started[0]);assertEquals(1,target.notices);assertEquals(1,target.retries);
        assertEquals(0,target.successes);assertEquals(0,target.failures);
    }
    @Test public void normalFailuresAndSuccessesNeverStartReserve(){
        for(boolean success:new boolean[]{true,false}){
            Target target=new Target();FitRocketAiFallback fallback=new FitRocketAiFallback(target,()->fail("No failover"));
            if(success)fallback.onSuccess(new JSONObject());else fallback.onFailure("quota/key/invalid response/network");
            fallback.onUnavailable("late");assertEquals(1,target.successes+target.failures);assertEquals(0,target.notices);
        }
    }
    @Test public void cancellationBeforeOrDuringNoticePreventsReserve(){
        for(boolean before:new boolean[]{true,false}){
            Target target=new Target();target.cancelled=before;target.cancelOnNotice=!before;
            new FitRocketAiFallback(target,()->fail("Cancelled reserve")).onUnavailable("503");
            assertEquals(0,target.successes+target.failures);
        }
    }
}
