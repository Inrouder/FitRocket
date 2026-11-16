package de.inrouder.fitrocket;

import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketPetMotionTest {
    @Test public void roamingActuallyTravelsBothDirectionsWithFourLegFrames(){
        FitRocketPetMotion motion=new FitRocketPetMotion(0);boolean left=false,right=false;boolean[] phases=new boolean[4];float min=1,max=0;
        for(long time=0;time<24000;time+=70){
            FitRocketPetMotion.Frame f=motion.frame(time);min=Math.min(min,f.x);max=Math.max(max,f.x);
            left|=f.facing<0;right|=f.facing>0;if(f.pose==FitRocketPetMotion.Pose.WALK)phases[f.sprite]=true;
            assertTrue(f.x>=0&&f.x<=1);
        }
        assertTrue(left&&right);assertTrue(max-min>0.8f);for(boolean phase:phases)assertTrue(phase);
    }
    @Test public void eatingAlternatesChewingFramesAndReturnsToRoaming(){
        FitRocketPetMotion m=new FitRocketPetMotion(0);m.act(FitRocketPetMotion.Action.FEED,0);
        FitRocketPetMotion.Frame a=m.frame(1000),b=m.frame(1250);
        assertEquals(FitRocketPetMotion.Pose.EAT,a.pose);assertEquals(FitRocketPetMotion.Pose.EAT,b.pose);assertNotEquals(a.sprite,b.sprite);
        assertEquals(a.x,b.x,0);assertTrue(b.phase>a.phase);
        assertEquals(FitRocketPetMotion.Action.ROAM,m.frame(4600).action);
    }
    @Test public void playHasPouncesAndAFlyingRollingBall(){
        FitRocketPetMotion m=new FitRocketPetMotion(0);m.act(FitRocketPetMotion.Action.PLAY,0);
        boolean pounce=false,bounce=false,walk=false;float min=1,max=0;
        for(int t=0;t<8000;t+=100){FitRocketPetMotion.Frame f=m.frame(t);pounce|=f.pose==FitRocketPetMotion.Pose.POUNCE;walk|=f.pose==FitRocketPetMotion.Pose.WALK;bounce|=f.toyY>0.1f;min=Math.min(min,f.toyX);max=Math.max(max,f.toyX);assertTrue(Float.isFinite(f.toyAngle));}
        assertTrue(pounce&&walk&&bounce);assertTrue(max-min>0.5f);assertEquals(FitRocketPetMotion.Action.ROAM,m.frame(8000).action);
    }
    @Test public void pettingUsesRelaxedPosesAndHearts(){
        FitRocketPetMotion m=new FitRocketPetMotion(0);m.act(FitRocketPetMotion.Action.PET,0);
        FitRocketPetMotion.Frame a=m.frame(100),b=m.frame(550);
        assertEquals(FitRocketPetMotion.Pose.PETTED,a.pose);assertNotEquals(a.sprite,b.sprite);assertTrue(a.heart>0);
        assertEquals(FitRocketPetMotion.Action.ROAM,m.frame(3500).action);
    }
    @Test public void dressingStopsAndFacesTheOutfitForward(){
        FitRocketPetMotion m=new FitRocketPetMotion(0);float x=m.frame(7000).x;m.act(FitRocketPetMotion.Action.DRESS,7000);
        FitRocketPetMotion.Frame f=m.frame(7500);assertEquals(x,f.x,0);assertEquals(1,f.facing);assertEquals(FitRocketPetMotion.Pose.IDLE,f.pose);
        assertEquals(FitRocketPetMotion.Action.ROAM,m.frame(8800).action);
    }
    @Test public void backgroundTimeDoesNotSkipOrCompleteAnInteraction(){
        FitRocketPetMotion m=new FitRocketPetMotion(0);m.act(FitRocketPetMotion.Action.PLAY,0);
        m.setRunning(false,1700);FitRocketPetMotion.Frame before=m.frame(1700),paused=m.frame(600000);
        assertEquals(before.x,paused.x,0);assertEquals(before.phase,paused.phase,0);assertEquals(before.sprite,paused.sprite);
        m.setRunning(true,600000);FitRocketPetMotion.Frame resumed=m.frame(600100);
        assertEquals(FitRocketPetMotion.Action.PLAY,resumed.action);assertTrue(resumed.phase>before.phase);assertTrue(resumed.phase-before.phase<0.02f);
    }
    @Test public void clockRollbackAndInterruptedActionsStayFinite(){
        FitRocketPetMotion m=new FitRocketPetMotion(1000);m.act(FitRocketPetMotion.Action.FEED,1200);float p=m.frame(2000).phase;
        assertEquals(p,m.frame(1500).phase,0);m.act(FitRocketPetMotion.Action.PET,2200);
        assertEquals(FitRocketPetMotion.Action.PET,m.frame(2300).action);
        for(FitRocketPetMotion.Action action:FitRocketPetMotion.Action.values()){
            m.act(action,3000);FitRocketPetMotion.Frame f=m.frame(3300);assertTrue(Float.isFinite(f.x));assertTrue(Float.isFinite(f.bob));assertTrue(f.x>=0&&f.x<=1);
        }
    }
    @Test public void finishingAnActionDoesNotTeleportThePet(){
        FitRocketPetMotion m=new FitRocketPetMotion(0);m.act(FitRocketPetMotion.Action.PLAY,0);
        float near=m.frame(7999).x,after=m.frame(8000).x;assertEquals(near,after,0.002f);
        assertEquals(after,m.frame(8500).x,0.002f);
    }
    @Test public void approachKickAndSettlementKeepContinuousPositions(){
        for(int boundary:new int[]{1200,7040,8000}){
            FitRocketPetMotion m=new FitRocketPetMotion(0);m.act(FitRocketPetMotion.Action.PLAY,0);
            FitRocketPetMotion.Frame before=m.frame(boundary-1),after=m.frame(boundary);
            assertEquals("Pet jumps at play phase boundary",before.x,after.x,0.003f);
            assertEquals("Toy jumps at play phase boundary",before.toyX,after.toyX,0.015f);
        }
    }

}
