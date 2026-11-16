package de.inrouder.fitrocket;

import org.junit.Test;
import java.io.File;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

/** Verify actual raster assets contain every required growth/mood frame, rather than empty placeholders. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class FitRocketPetArtTest {
    private Bitmap image(String file)throws Exception {
        File path=new File("src/main/res/drawable-nodpi/"+file);
        if(!path.exists())path=new File("app/"+path);
        assertTrue(path.isFile());Bitmap image=BitmapFactory.decodeFile(path.getAbsolutePath());assertNotNull(image);return image;
    }
    private void atlas(Bitmap image,int columns,int rows){
        assertTrue(image.hasAlpha());
        int[] pixels=new int[image.getWidth()*image.getHeight()];image.getPixels(pixels,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
        for(int row=0;row<rows;row++)for(int col=0;col<columns;col++){
            int left=Math.round(col*image.getWidth()/(float)columns),right=Math.round((col+1)*image.getWidth()/(float)columns);
            int top=Math.round(row*image.getHeight()/(float)rows),bottom=Math.round((row+1)*image.getHeight()/(float)rows);
            int visible=0,transparent=0;
            for(int y=top;y<bottom;y++)for(int x=left;x<right;x++){
                int alpha=pixels[y*image.getWidth()+x]>>>24;if(alpha>180)visible++;if(alpha<10)transparent++;
            }
            assertTrue("Missing sprite at "+col+","+row,visible>500);
            assertTrue("No transparent padding at "+col+","+row,transparent>500);
        }
    }
    @Test public void everyGrowthStageHasAllFiveMoodSprites()throws Exception {atlas(image("fitrocket_pet_atlas.png"),5,5);}
    @Test public void allAccessoriesToysAndHeartAreRasterSprites()throws Exception {atlas(image("fitrocket_pet_items.png"),3,2);}
    @Test public void everyStageHasFourWalkFrames()throws Exception {atlas(image("fitrocket_pet_walk.png"),5,4);}
    @Test public void eatingPlayingAndPettingHaveSeparatePixelPoses()throws Exception {atlas(image("fitrocket_pet_actions.png"),5,6);}
    @Test public void bowlsHandAndFoodAreRasterSprites()throws Exception {atlas(image("fitrocket_pet_props.png"),3,2);}
    @Test public void gardenIsAnActualLandscapeBitmap()throws Exception {
        Bitmap garden=image("fitrocket_pet_garden.png");assertTrue(garden.getWidth()>garden.getHeight());assertTrue(garden.getHeight()>=512);
        assertNotEquals(garden.getPixel(100,100),garden.getPixel(garden.getWidth()/2,garden.getHeight()/2));
    }
}
