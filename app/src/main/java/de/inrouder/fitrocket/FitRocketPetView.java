package de.inrouder.fitrocket;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.*;

/** Live raster scene: a paused foreground clock drives actual walking and interaction sprite frames. */
public final class FitRocketPetView extends View {
    private final FitRocketPetScene scene;
    private final FitRocketPetMotion motion;
    private int stage,mood;
    private String head="none",toy="ball";
    private boolean foreground=true,dragged;
    private float downX,downY;
    private final int touchSlop;
    public FitRocketPetView(Context context){
        super(context);scene=new FitRocketPetScene(context);motion=new FitRocketPetMotion(SystemClock.uptimeMillis());
        touchSlop=ViewConfiguration.get(context).getScaledTouchSlop();
        setFocusable(true);setClickable(true);setContentDescription("Dein Pixel-Pet. Antippen oder darüberstreichen zum Streicheln.");
    }
    public void show(int stage,int mood,String head,String toy){this.stage=stage;this.mood=mood;this.head=head;this.toy=toy;invalidate();}
    public void animate(FitRocketPetMotion.Action action){motion.act(action,SystemClock.uptimeMillis());invalidate();}
    public void setRunning(boolean value){foreground=value;syncClock();}
    private void syncClock(){motion.setRunning(foreground&&isAttachedToWindow()&&getWindowVisibility()==VISIBLE,SystemClock.uptimeMillis());if(motion.isRunning())invalidate();}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);scene.draw(canvas,getWidth(),getHeight(),stage,mood,head,toy,motion.frame(SystemClock.uptimeMillis()));
        if(motion.isRunning())postInvalidateDelayed(33);
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        if(!hasOnClickListeners())return false;
        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:downX=event.getX();downY=event.getY();dragged=false;if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(true);return true;
            case MotionEvent.ACTION_MOVE:
                if(!dragged&&Math.hypot(event.getX()-downX,event.getY()-downY)>=touchSlop){dragged=true;performClick();}
                return true;
            case MotionEvent.ACTION_UP:if(!dragged)performClick();if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(false);return true;
            case MotionEvent.ACTION_CANCEL:if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(false);return true;
            default:return super.onTouchEvent(event);
        }
    }
    @Override public boolean performClick(){return super.performClick();}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();syncClock();}
    @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);if(motion!=null)syncClock();}
    @Override protected void onDetachedFromWindow(){motion.setRunning(false,SystemClock.uptimeMillis());super.onDetachedFromWindow();}
    public static final class ItemIcon extends View {
        private final int index;
        private final FitRocketPetScene scene;
        public ItemIcon(Context c,int itemIndex){super(c);index=itemIndex;scene=new FitRocketPetScene(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){scene.drawItem(canvas,index,getWidth(),getHeight());}
    }
}
