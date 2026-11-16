package de.inrouder.fitrocket;

/** Deterministic, foreground-only animation clock. It never awards or spends game/food points. */
public final class FitRocketPetMotion {
    public enum Action { ROAM, FEED, PLAY, PET, DRESS }
    public enum Pose { IDLE, WALK, EAT, POUNCE, PETTED }
    public static final long FEED_MS=4600,PLAY_MS=8000,PET_MS=3500,DRESS_MS=1800;
    public static final class Frame {
        public final Action action;
        public final Pose pose;
        public final int sprite,facing;
        public final float x,bob,tilt,toyX,toyY,toyAngle,phase,heart;
        Frame(Action action,Pose pose,int sprite,int facing,float x,float bob,float tilt,
              float toyX,float toyY,float toyAngle,float phase,float heart){
            this.action=action;this.pose=pose;this.sprite=sprite;this.facing=facing;
            this.x=x;this.bob=bob;this.tilt=tilt;this.toyX=toyX;this.toyY=toyY;
            this.toyAngle=toyAngle;this.phase=phase;this.heart=heart;
        }
    }
    private long lastNow,elapsed,started,roamStarted;
    private boolean running=true;
    private Action action=Action.ROAM;
    private float origin=0.35f,actionX=0.35f;
    private int direction=1,actionFacing=1;
    public FitRocketPetMotion(long now){lastNow=now;}
    private void advance(long now){
        if(now<lastNow)return; // monotonic clock rollback must not rewind a sequence
        if(running)elapsed+=now-lastNow;
        lastNow=now;
    }
    public void setRunning(boolean value,long now){advance(now);running=value;}
    public boolean isRunning(){return running;}
    public Action action(){return action;}
    public void act(Action next,long now){
        if(next==null)return;
        Frame current=frame(now);actionX=current.x;actionFacing=current.facing;started=elapsed;action=next;
        if(next==Action.ROAM){origin=current.x;direction=current.facing;roamStarted=elapsed;}
        if(next==Action.DRESS)actionFacing=1;
    }
    public Frame frame(long now){
        advance(now);
        if(action!=Action.ROAM&&elapsed-started>=duration(action)){
            Frame end=interaction(duration(action));origin=end.x;direction=end.facing;
            action=Action.ROAM;roamStarted=elapsed;
        }
        return action==Action.ROAM?roam():interaction(elapsed-started);
    }
    public static long duration(Action action){
        switch(action){case FEED:return FEED_MS;case PLAY:return PLAY_MS;case PET:return PET_MS;case DRESS:return DRESS_MS;default:return Long.MAX_VALUE;}
    }
    private Frame roam(){
        long t=Math.max(0,elapsed-roamStarted-900);
        boolean initial=elapsed-roamStarted<900,walking=!initial&&t%7200<6000;
        long travel=(t/7200)*6000+Math.min(6000,t%7200);
        float raw=origin+direction*travel*0.00014f;
        float p=mod(raw,2),x=p<=1?p:2-p;
        int facing=p==0?1:p==1?-1:direction*(p<1?1:-1);
        int sprite=(int)((travel/140)%4);
        float bob=walking?(float)Math.abs(Math.sin(travel*Math.PI/280))*0.009f:0;
        return new Frame(Action.ROAM,walking?Pose.WALK:Pose.IDLE,sprite,facing,x,bob,
            walking?(float)Math.sin(travel*Math.PI/280)*1.3f:0,0.84f,0,0,0,0);
    }
    private Frame interaction(long t){
        float p=clamp(t/(float)duration(action));
        switch(action){
            case FEED:
                return new Frame(action,p<0.10f?Pose.IDLE:Pose.EAT,(int)(t/240)%2,actionFacing,actionX,
                    (float)Math.sin(t/130.0)*0.004f,0,0.84f,0,0,p,p>0.78f?(1-p)/0.22f:0);
            case PET:
                return new Frame(action,Pose.PETTED,(int)(t/420)%2,actionFacing,actionX,
                    (float)Math.sin(t/340.0)*0.005f,(float)Math.sin(t/420.0)*2.5f,0.84f,0,0,p,0.8f);
            case DRESS:
                return new Frame(action,Pose.IDLE,0,1,actionX,0,(float)Math.sin(p*Math.PI*2)*2.0f,0.84f,0,0,p,1-p);
            case PLAY:
                float toy=ball(p),lag=ball(Math.max(0,p-0.075f));
                float x=p<0.15f?mix(actionX,clamp((ball(0.075f)-0.13f)/0.74f),p/0.15f):clamp((lag-0.13f)/0.74f);
                int facing=p<0.15f?(x>=actionX?1:-1):(ball(Math.min(1,p+0.01f))>=toy?1:-1);
                boolean pounce=(p>=0.13f&&p<0.26f)||(p>=0.62f&&p<0.74f);
                if(p>0.88f){x=mix(clamp((ball(0.805f)-0.13f)/0.74f),0.62f,(p-0.88f)/0.12f);toy=mix(ball(0.88f),0.84f,(p-0.88f)/0.12f);facing=1;}
                return new Frame(action,p>0.995f?Pose.IDLE:pounce?Pose.POUNCE:Pose.WALK,
                    (int)(t/(pounce?260:120))%(pounce?2:4),facing,x,
                    pounce?(float)Math.abs(Math.sin((p<0.3f?(p-0.13f)/0.13f:(p-0.62f)/0.12f)*Math.PI))*0.055f:0,
                    pounce?-3f:0,toy,p<0.15f||p>0.96f?0:(float)Math.abs(Math.sin((p-0.15f)*Math.PI*6))*0.19f*(1-p*0.45f),
                    p*1080,p,pounce?0.5f:0);
            default:throw new IllegalStateException("Unbekannte Pet-Animation.");
        }
    }
    private static float ball(float p){
        if(p<0.15f)return 0.84f;
        float raw=0.70f+(p-0.15f)*2.55f;
        float n=mod(raw,1.44f);return 0.13f+(n<=0.72f?n:1.44f-n);
    }
    private static float mod(float value,float divisor){return ((value%divisor)+divisor)%divisor;}
    private static float clamp(float p){return Math.max(0,Math.min(1,p));}
    private static float mix(float a,float b,float p){return a+(b-a)*clamp(p);}
}
