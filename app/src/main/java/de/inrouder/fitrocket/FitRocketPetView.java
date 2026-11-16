package de.inrouder.fitrocket;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;

/** Raster-only pet scene. Alpha bounds normalize atlas padding without altering the original PNGs. */
public final class FitRocketPetView extends View {
    private static Bitmap creatures,items,garden;
    private static Rect[] creatureFrames,itemFrames;
    private static final int[] stageHeights=new int[5];
    private static final float[] stageSize={0.43f,0.55f,0.64f,0.74f,0.88f};
    private static final float[] headX={0.50f,0.53f,0.67f,0.61f,0.50f};
    private static final float[] foreheadY={0.14f,0.31f,0.20f,0.18f,0.15f};
    private final Paint paint=new Paint();
    private final RectF destination=new RectF();
    private int stage,mood;
    private String head="none",toy="ball";
    private boolean running=true;
    private long reactionAt=-10000;
    private static void load(Context context){
        if(creatures!=null)return;
        BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;options.inSampleSize=2;
        creatures=BitmapFactory.decodeResource(context.getResources(),R.drawable.fitrocket_pet_atlas,options);
        items=BitmapFactory.decodeResource(context.getResources(),R.drawable.fitrocket_pet_items,options);
        garden=BitmapFactory.decodeResource(context.getResources(),R.drawable.fitrocket_pet_garden,options);
        creatureFrames=bounds(creatures,5,5);itemFrames=bounds(items,3,2);
        for(int stage=0;stage<5;stage++)for(int row=0;row<5;row++)
            stageHeights[stage]=Math.max(stageHeights[stage],creatureFrames[row*5+stage].height());
    }
    private static Rect[] bounds(Bitmap bitmap,int columns,int rows){
        Rect[] result=new Rect[columns*rows];
        if(bitmap==null){for(int i=0;i<result.length;i++)result[i]=new Rect(0,0,1,1);return result;}
        int w=bitmap.getWidth(),h=bitmap.getHeight();int[] pixels=new int[w*h];bitmap.getPixels(pixels,0,w,0,0,w,h);
        for(int row=0;row<rows;row++)for(int col=0;col<columns;col++){
            int left=Math.round(col*w/(float)columns),right=Math.round((col+1)*w/(float)columns);
            int top=Math.round(row*h/(float)rows),bottom=Math.round((row+1)*h/(float)rows);
            int x1=right,y1=bottom,x2=left,y2=top;
            for(int y=top;y<bottom;y++)for(int x=left;x<right;x++)if((pixels[y*w+x]>>>24)>96){
                x1=Math.min(x1,x);y1=Math.min(y1,y);x2=Math.max(x2,x+1);y2=Math.max(y2,y+1);
            }
            result[row*columns+col]=x2>x1&&y2>y1?new Rect(Math.max(left,x1-1),Math.max(top,y1-1),Math.min(right,x2+1),Math.min(bottom,y2+1)):new Rect(left,top,right,bottom);
        }
        return result;
    }
    public FitRocketPetView(Context context){
        super(context);load(context);paint.setFilterBitmap(false);paint.setAntiAlias(false);
        setFocusable(true);setClickable(true);setContentDescription("Dein Pixel-Pet. Antippen zum Streicheln.");
    }
    public void show(int stage,int mood,String head,String toy){this.stage=stage;this.mood=mood;this.head=head;this.toy=toy;invalidate();}
    public void setRunning(boolean value){running=value;if(value)invalidate();}
    public void react(){reactionAt=SystemClock.uptimeMillis();invalidate();}
    private void draw(Canvas canvas,Bitmap bitmap,Rect source,float x,float y,float width,float height){
        if(bitmap==null)return;destination.set(x,y,x+width,y+height);canvas.drawBitmap(bitmap,source,destination,paint);
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);if(getWidth()==0||getHeight()==0)return;
        float w=getWidth(),h=getHeight();
        if(garden!=null){
            float ratio=Math.max(w/garden.getWidth(),h/garden.getHeight());float gw=garden.getWidth()*ratio,gh=garden.getHeight()*ratio;
            draw(canvas,garden,null,(w-gw)/2,(h-gh)/2,gw,gh);
        }
        long now=SystemClock.uptimeMillis();float bounce=running?(float)Math.sin(now/480.0)*h*0.006f:0;
        Rect frame=creatureFrames[mood*5+stage];
        float scale=h*stageSize[stage]/Math.max(1,stageHeights[stage]);scale=Math.min(scale,w*0.78f/Math.max(1,frame.width()));
        float width=frame.width()*scale,height=frame.height()*scale,left=w*0.46f-width/2,top=h*0.91f-height+bounce;
        draw(canvas,creatures,frame,left,top,width,height);
        int item=FitRocketPet.itemIndex(head);
        if(item>=1&&item<=3){
            Rect hat=itemFrames[item-1];float hw=width*(stage==4?0.44f:0.54f),hh=hw*hat.height()/Math.max(1,hat.width());
            float x=left+width*headX[stage]-hw/2,y=top+height*foreheadY[stage]-hh*(item==1?0.22f:0.87f);
            draw(canvas,items,hat,x,y,hw,hh);
        }
        Rect toyFrame=itemFrames["rocket_toy".equals(toy)?4:3];
        float toyHeight=Math.min(w,h)*0.16f,toyWidth=toyHeight*toyFrame.width()/Math.max(1,toyFrame.height());
        draw(canvas,items,toyFrame,w*0.83f-toyWidth/2,h*0.93f-toyHeight,toyWidth,toyHeight);
        float progress=(now-reactionAt)/1700f;
        if(progress>=0&&progress<1){
            Rect heart=itemFrames[5];float size=h*0.13f;paint.setAlpha((int)(255*(1-progress)));
            draw(canvas,items,heart,w*0.72f,top+height*0.12f-progress*h*0.18f,size,size*heart.height()/Math.max(1,heart.width()));paint.setAlpha(255);
        }
        if(running&&getWindowVisibility()==VISIBLE)postInvalidateDelayed(60);
    }
    public static final class ItemIcon extends View {
        private final int frame;
        private final Paint paint=new Paint();
        private final RectF destination=new RectF();
        public ItemIcon(Context c,int itemIndex){super(c);load(c);frame=itemIndex-1;paint.setFilterBitmap(false);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){
            if(items==null||frame<0)return;Rect source=itemFrames[frame];
            float scale=Math.min(getWidth()/(float)source.width(),getHeight()/(float)source.height());
            float width=source.width()*scale,height=source.height()*scale;
            destination.set((getWidth()-width)/2,(getHeight()-height)/2,(getWidth()+width)/2,(getHeight()+height)/2);
            canvas.drawBitmap(items,source,destination,paint);
        }
    }
    @Override public boolean performClick(){super.performClick();return true;}
    @Override protected void onDetachedFromWindow(){running=false;super.onDetachedFromWindow();}
}
