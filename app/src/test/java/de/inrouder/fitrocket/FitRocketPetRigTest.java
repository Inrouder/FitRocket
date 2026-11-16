package de.inrouder.fitrocket;

import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketPetRigTest {
    @Test public void earsAreBehindTheFaceAndHatsMountAboveTheEyes(){
        for(int stage=0;stage<5;stage++)for(FitRocketPetMotion.Pose pose:FitRocketPetMotion.Pose.values())for(int phase=0;phase<4;phase++){
            FitRocketPetRig.Head head=FitRocketPetRig.head(stage,pose,phase);
            float eyes=-200+200*head.eyesY;
            for(String item:new String[]{"dog_ears","rocket_cap","star_crown"}){
                FitRocketPetRig.Mount m=FitRocketPetRig.mount(stage,pose,phase,item,180,200,1.5f);
                assertTrue(m.width>0&&m.height>0);assertTrue(Float.isFinite(m.angle));
                assertEquals("dog_ears".equals(item),m.behind);
                if(!m.behind)assertTrue("Headwear must not mask the eyes",m.top+m.height<eyes);
                else assertTrue("The visible ear bridge must clear the eyes",m.top+m.height*.32f<eyes);
            }
        }
    }
    @Test public void mountsFollowHeadLeanAndDifferentStageProportions(){
        FitRocketPetRig.Mount a=FitRocketPetRig.mount(2,FitRocketPetMotion.Pose.PETTED,0,"rocket_cap",180,200,1.5f);
        FitRocketPetRig.Mount b=FitRocketPetRig.mount(2,FitRocketPetMotion.Pose.PETTED,1,"rocket_cap",180,200,1.5f);
        assertNotEquals(a.angle,b.angle,0);assertNotEquals(a.pivotX,b.pivotX,0);
        FitRocketPetRig.Mount egg=FitRocketPetRig.mount(0,FitRocketPetMotion.Pose.IDLE,0,"star_crown",180,200,1.5f);
        FitRocketPetRig.Mount grown=FitRocketPetRig.mount(4,FitRocketPetMotion.Pose.IDLE,0,"star_crown",180,200,1.5f);
        assertNotEquals(egg.width,grown.width,0);
    }
    @Test public void noHeadwearAndToyDoNotCreateAHeadMount(){
        assertNull(FitRocketPetRig.mount(1,FitRocketPetMotion.Pose.IDLE,0,"none",100,100,1));
        assertNull(FitRocketPetRig.mount(1,FitRocketPetMotion.Pose.IDLE,0,"ball",100,100,1));
    }
}
