package de.inrouder.fitrocket;

import android.content.Context;
import android.graphics.*;

/** Renders the same raster scene for the live view and deterministic Android preview tests. */
public final class FitRocketPetScene {
    private static final float[] SIZE={0.43f,0.55f,0.64f,0.73f,0.83f};
    private static Atlas idle,walk,actions,items,props;
    private static Bitmap garden;
    private static final Envelope[][] envelopes=new Envelope[5][4];
    private static final class Envelope {
        float left=0,right=0,top=0;
        void include(float x,float y){left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);}
    }
    private final Paint paint=new Paint();
    private final RectF destination=new RectF();
    private static final class Atlas {
        final Bitmap bitmap;
        final Rect[] frames;
        final int[] heights;
        Atlas(Context c,int resource,int columns,int rows){this(c,resource,columns,rows,null,null);}
        Atlas(Context c,int resource,int columns,int rows,int[] cutsX,int[] cutsY){
            bitmap=decode(c,resource);frames=new Rect[columns*rows];heights=new int[columns];
            if(bitmap==null)throw new IllegalStateException("Pet-Grafik fehlt.");
            int w=bitmap.getWidth(),h=bitmap.getHeight();int[] pixels=new int[w*h];bitmap.getPixels(pixels,0,w,0,0,w,h);
            for(int row=0;row<rows;row++)for(int col=0;col<columns;col++){
                int l=cutsX==null?Math.round(col*w/(float)columns):Math.round(cutsX[col]*w/1254f);
                int r=cutsX==null?Math.round((col+1)*w/(float)columns):Math.round(cutsX[col+1]*w/1254f);
                int t=cutsY==null?Math.round(row*h/(float)rows):Math.round(cutsY[row]*h/1254f);
                int b=cutsY==null?Math.round((row+1)*h/(float)rows):Math.round(cutsY[row+1]*h/1254f);
                int x1=r,y1=b,x2=l,y2=t;
                for(int y=t;y<b;y++)for(int x=l;x<r;x++)if((pixels[y*w+x]>>>24)>96){
                    x1=Math.min(x1,x);y1=Math.min(y1,y);x2=Math.max(x2,x+1);y2=Math.max(y2,y+1);
                }
                Rect frame=x2>x1&&y2>y1?new Rect(Math.max(l,x1-1),Math.max(t,y1-1),Math.min(r,x2+1),Math.min(b,y2+1)):new Rect(l,t,r,b);
                frames[row*columns+col]=frame;heights[col]=Math.max(heights[col],frame.height());
            }
        }
    }
    private static Bitmap decode(Context c,int resource){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;o.inSampleSize=2;return BitmapFactory.decodeResource(c.getResources(),resource,o);}
    private static void load(Context c){
        if(idle!=null)return;
        // Raster sheets have hand-composed gutters, not uniform cells. Keep complete sprites.
        idle=new Atlas(c,R.drawable.fitrocket_pet_atlas,5,5,new int[]{0,260,485,720,980,1254},new int[]{0,260,500,730,965,1254});
        walk=new Atlas(c,R.drawable.fitrocket_pet_walk,5,4,new int[]{0,260,488,727,990,1254},new int[]{0,300,590,880,1254});
        actions=new Atlas(c,R.drawable.fitrocket_pet_actions,5,6,new int[]{0,260,480,712,970,1254},new int[]{0,250,460,667,846,1035,1254});
        items=new Atlas(c,R.drawable.fitrocket_pet_items,3,2);
        props=new Atlas(c,R.drawable.fitrocket_pet_props,3,2);
        garden=decode(c,R.drawable.fitrocket_pet_garden);
        for(int stage=0;stage<5;stage++)for(int gear=0;gear<4;gear++)envelopes[stage][gear]=envelope(stage,gear);
    }
    /** One envelope per growth stage/outfit keeps scale and travel bounds constant across poses. */
    private static Envelope envelope(int stage,int gear){
        Envelope result=new Envelope();
        for(Atlas atlas:new Atlas[]{idle,walk,actions})for(int row=0;row<atlas.frames.length/5;row++){
            FitRocketPetMotion.Pose pose=atlas==idle?FitRocketPetMotion.Pose.IDLE:atlas==walk?FitRocketPetMotion.Pose.WALK:
                row<2?FitRocketPetMotion.Pose.EAT:row<4?FitRocketPetMotion.Pose.POUNCE:FitRocketPetMotion.Pose.PETTED;
            Rect frame=atlas.frames[row*5+stage];float width=frame.width()/(float)atlas.heights[stage],height=frame.height()/(float)atlas.heights[stage];
            include(result,-width/2,-height,width,height,0,0,0);
            if(gear>0){
                Rect item=items.frames[gear-1];String name=gear==1?"dog_ears":gear==2?"rocket_cap":"star_crown";
                FitRocketPetRig.Mount mount=FitRocketPetRig.mount(stage,pose,row%2,name,width,height,item.width()/(float)item.height());
                include(result,mount.left,mount.top,mount.width,mount.height,mount.pivotX,mount.pivotY,mount.angle);
            }
        }
        return result;
    }
    private static void include(Envelope e,float left,float top,float width,float height,float pivotX,float pivotY,float angle){
        double a=Math.toRadians(angle),cos=Math.cos(a),sin=Math.sin(a);
        for(float x:new float[]{left,left+width})for(float y:new float[]{top,top+height}){
            float rx=pivotX+(float)(cos*(x-pivotX)-sin*(y-pivotY));
            float ry=pivotY+(float)(sin*(x-pivotX)+cos*(y-pivotY));
            // Reserve the whole actor's maximum lean in either direction, too.
            for(double root:new double[]{-0.053,0,0.053})e.include((float)(Math.cos(root)*rx-Math.sin(root)*ry),(float)(Math.sin(root)*rx+Math.cos(root)*ry));
        }
    }
    public FitRocketPetScene(Context c){load(c);paint.setAntiAlias(false);paint.setFilterBitmap(false);}
    private void draw(Canvas c,Bitmap b,Rect source,float x,float y,float width,float height){
        destination.set(x,y,x+width,y+height);c.drawBitmap(b,source,destination,paint);
    }
    private void item(Canvas c,Atlas atlas,int frame,float x,float y,float width){
        Rect source=atlas.frames[frame];draw(c,atlas.bitmap,source,x,y,width,width*source.height()/Math.max(1,source.width()));
    }
    private void mount(Canvas c,FitRocketPetRig.Mount m,int index){
        if(m==null)return;c.save();c.rotate(m.angle,m.pivotX,m.pivotY);
        draw(c,items.bitmap,items.frames[index-1],m.left,m.top,m.width,m.height);c.restore();
    }
    public void draw(Canvas c,float w,float h,int stage,int mood,String head,String toy,FitRocketPetMotion.Frame motion){
        if(w<=0||h<=0)return;
        c.save();c.clipRect(0,0,w,h);
        float ratio=Math.max(w/garden.getWidth(),h/garden.getHeight()),gw=garden.getWidth()*ratio,gh=garden.getHeight()*ratio;
        draw(c,garden,null,(w-gw)/2,(h-gh)/2,gw,gh);
        Atlas atlas=idle;int frame=mood*5+stage;FitRocketPetMotion.Pose rigPose=motion.pose;
        switch(motion.pose){
            case WALK:
                if(mood<2){atlas=walk;frame=(motion.sprite%4)*5+stage;}
                else rigPose=FitRocketPetMotion.Pose.IDLE; // keep sad/angry/monster faces while shuffling
                break;
            case EAT:atlas=actions;frame=(motion.sprite%2)*5+stage;break;
            case POUNCE:atlas=actions;frame=(2+motion.sprite%2)*5+stage;break;
            case PETTED:atlas=actions;frame=(4+motion.sprite%2)*5+stage;break;
            default:break;
        }
        Rect source=atlas.frames[frame];int gearIndex=FitRocketPet.itemIndex(head);
        if(gearIndex<1||gearIndex>3)gearIndex=0;
        Envelope envelope=envelopes[stage][gearIndex];
        float unit=Math.min(h*SIZE[stage],Math.min(w*0.85f/(2*Math.max(-envelope.left,envelope.right)),h*0.80f/-envelope.top));
        float scale=unit/atlas.heights[stage],bw=source.width()*scale,bh=source.height()*scale;
        FitRocketPetRig.Mount gear=null;
        if(gearIndex>0){Rect g=items.frames[gearIndex-1];gear=FitRocketPetRig.mount(stage,rigPose,motion.sprite,head,bw,bh,g.width()/(float)g.height());}
        float extent=Math.max(-envelope.left,envelope.right)*unit;
        float padding=w*0.035f,minX=extent+padding,maxX=w-extent-padding;
        float center=minX+(maxX-minX)*motion.x,ground=h*0.91f-motion.bob*h;
        FitRocketPetRig.Head slot=FitRocketPetRig.head(stage,rigPose,motion.sprite);
        float mouthLocalX=-bw/2+bw*slot.x+bw*slot.width*0.13f;
        float mouthLocalY=-bh+bh*slot.eyesY+bw*slot.width*0.40f;
        float angle=(float)Math.toRadians(motion.tilt);
        float mouthX=center+motion.facing*((float)Math.cos(angle)*mouthLocalX-(float)Math.sin(angle)*mouthLocalY);
        float mouthY=ground+(float)Math.sin(angle)*mouthLocalX+(float)Math.cos(angle)*mouthLocalY;
        float bowlWidth=Math.max(h*0.15f,Math.min(h*0.29f,bw*0.52f));
        float bowlX=clamp(mouthX-bowlWidth/2,w*0.04f,w*0.96f-bowlWidth);
        float bowlY=stage==4?Math.min(h*0.88f,mouthY+h*0.10f):h*0.91f;
        if(motion.action==FitRocketPetMotion.Action.FEED)item(c,props,motion.phase>0.76f?1:0,bowlX,bowlY-bowlWidth*0.45f,bowlWidth);
        c.save();c.translate(center,ground);c.scale(motion.facing,1);c.rotate(motion.tilt);
        if(gear!=null&&gear.behind)mount(c,gear,gearIndex);
        draw(c,atlas.bitmap,source,-bw/2,-bh,bw,bh);
        if(gear!=null&&!gear.behind)mount(c,gear,gearIndex);
        if(gear!=null&&gear.behind){
            // Only the headband bridge is in front; ears hang behind the cheeks/gills.
            c.save();c.rotate(gear.angle,gear.pivotX,gear.pivotY);
            c.clipRect(gear.left+gear.width*.30f,gear.top,gear.left+gear.width*.70f,gear.top+gear.height*.32f);
            draw(c,items.bitmap,items.frames[gearIndex-1],gear.left,gear.top,gear.width,gear.height);c.restore();
        }
        if(motion.action==FitRocketPetMotion.Action.PET){
            float handWidth=bw*slot.width*0.85f,stroke=(float)Math.sin(motion.phase*Math.PI*8);
            float x=-bw/2+bw*slot.x-handWidth/2+stroke*handWidth*0.12f;
            float y=-bh+bh*slot.eyesY-bw*slot.width*0.55f;
            item(c,props,2,x,y,handWidth);
        }
        c.restore();
        Rect ts=items.frames["rocket_toy".equals(toy)?4:3];float th=Math.min(w,h)*0.16f,tw=th*ts.width()/Math.max(1,ts.height());
        float tx=clamp(w*motion.toyX,tw/2+w*0.025f,w-tw/2-w*0.025f),ty=h*0.93f-th-motion.toyY*h;
        c.save();c.rotate("rocket_toy".equals(toy)?(float)Math.sin(motion.phase*18)*15*(motion.phase>0.88f?(1-motion.phase)/0.12f:1):motion.toyAngle,tx,ty+th/2);
        draw(c,items.bitmap,ts,tx-tw/2,ty,tw,th);c.restore();
        if(motion.action==FitRocketPetMotion.Action.FEED&&motion.phase>0.12f&&motion.phase<0.82f){
            float p=(motion.phase*7)%1,px=mix(bowlX+bowlWidth/2,mouthX,p),py=mix(bowlY-bowlWidth*0.3f,mouthY,p)-(float)Math.sin(p*Math.PI)*h*0.08f;
            item(c,props,3,px-h*0.025f,py,h*0.05f);
        }
        if(motion.heart>0){
            for(int i=0;i<3;i++){
                float p=(motion.phase*2+i*0.31f)%1,sz=h*(0.07f+i*0.012f);
                paint.setAlpha((int)(255*(1-p)*motion.heart));
                item(c,items,5,clamp(mouthX+(i-1)*h*0.10f,0,w-sz),mouthY-h*0.17f-p*h*0.22f,sz);
            }
            paint.setAlpha(255);
        }
        if(motion.action==FitRocketPetMotion.Action.PLAY&&motion.pose==FitRocketPetMotion.Pose.POUNCE)item(c,props,4,tx-h*0.07f,h*0.83f,h*0.14f);
        c.restore();
    }
    public void drawItem(Canvas c,int itemIndex,float w,float h){
        if(itemIndex<1||itemIndex>5)return;Rect source=items.frames[itemIndex-1];float scale=Math.min(w/source.width(),h/source.height());
        draw(c,items.bitmap,source,(w-source.width()*scale)/2,(h-source.height()*scale)/2,source.width()*scale,source.height()*scale);
    }
    private static float clamp(float n,float a,float b){return Math.max(a,Math.min(b,n));}
    private static float mix(float a,float b,float p){return a+(b-a)*p;}
}
