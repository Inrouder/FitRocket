package de.inrouder.fitrocket;

import android.graphics.*;
import android.os.SystemClock;
import android.view.MotionEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class FitRocketPetSceneTest {
    private FitRocketPetMotion.Frame frame(FitRocketPetMotion.Action action,long time){FitRocketPetMotion motion=new FitRocketPetMotion(0);if(action!=null)motion.act(action,0);return motion.frame(time);}
    private void save(Bitmap b,String name)throws Exception {File dir=new File("build/outputs/pet-previews");assertTrue(dir.isDirectory()||dir.mkdirs());try(FileOutputStream out=new FileOutputStream(new File(dir,name))){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
    @Test public void everyOutfitRendersOnAllStagesAndInteractionPoses()throws Exception {
        FitRocketPetScene scene=new FitRocketPetScene(RuntimeEnvironment.getApplication());
        FitRocketPetMotion.Action[] actions={null,null,FitRocketPetMotion.Action.FEED,FitRocketPetMotion.Action.PLAY,FitRocketPetMotion.Action.PET};
        long[] times={0,2000,2500,1550,1300};
        for(String item:new String[]{"dog_ears","rocket_cap","star_crown"}){
            Bitmap b=Bitmap.createBitmap(1400,1300,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);
            Paint text=new Paint();text.setColor(Color.WHITE);text.setTextSize(13);
            for(int row=0;row<5;row++)for(int stage=0;stage<5;stage++){
                c.save();c.translate(stage*280,row*260);scene.draw(c,280,240,stage,0,item,"ball",frame(actions[row],times[row]));
                c.drawText((stage+1)+" · "+(row==0?"Ruhe":row==1?"Laufen":actions[row].name()),8,254,text);c.restore();
            }
            save(b,"pet-outfit-"+item+".png");assertNotEquals(b.getPixel(10,10),b.getPixel(130,120));
        }
    }
    @Test public void actionSequencesProduceDifferentNativeRasterFrames()throws Exception {
        FitRocketPetScene scene=new FitRocketPetScene(RuntimeEnvironment.getApplication());
        for(FitRocketPetMotion.Action action:new FitRocketPetMotion.Action[]{FitRocketPetMotion.Action.FEED,FitRocketPetMotion.Action.PLAY,FitRocketPetMotion.Action.PET}){
            Bitmap b=Bitmap.createBitmap(1440,270,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);
            for(int i=0;i<4;i++){c.save();c.translate(i*360,0);scene.draw(c,360,270,2,0,"rocket_cap","ball",frame(action,400+i*650));c.restore();}
            save(b,"pet-motion-"+action.name().toLowerCase()+".png");
            int difference=0;for(int y=0;y<270;y+=4)for(int x=0;x<360;x+=4)if(b.getPixel(x,y)!=b.getPixel(360+x,y))difference++;
            assertTrue("Animation frames must visibly change",difference>100);
        }
    }
    @Test public void strokeGestureCallsCareOnlyOnceAndViewCanBeReused(){
        FitRocketPetView view=new FitRocketPetView(RuntimeEnvironment.getApplication());view.layout(0,0,500,350);view.show(2,0,"dog_ears","ball");
        AtomicInteger count=new AtomicInteger();view.setOnClickListener(v->count.incrementAndGet());long t=SystemClock.uptimeMillis();
        view.onTouchEvent(MotionEvent.obtain(t,t,MotionEvent.ACTION_DOWN,100,100,0));
        view.onTouchEvent(MotionEvent.obtain(t,t+20,MotionEvent.ACTION_MOVE,180,100,0));
        view.onTouchEvent(MotionEvent.obtain(t,t+40,MotionEvent.ACTION_MOVE,220,100,0));
        view.onTouchEvent(MotionEvent.obtain(t,t+60,MotionEvent.ACTION_UP,220,100,0));assertEquals(1,count.get());
        view.animate(FitRocketPetMotion.Action.PET);view.setRunning(false);view.setRunning(true);
        view.draw(new Canvas(Bitmap.createBitmap(500,350,Bitmap.Config.ARGB_8888)));
    }
}
