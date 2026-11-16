package de.inrouder.fitrocket;

/** Anatomical attachment slots relative to the visible sprite, shared by all outfits and movement frames. */
public final class FitRocketPetRig {
    // Slots measured on the visible PNG content: x, eye line, cranial width.
    private static final float[][] IDLE={{.53f,.33f,.64f},{.58f,.44f,.62f},{.64f,.37f,.52f},{.62f,.37f,.52f},{.53f,.29f,.49f}};
    private static final float[][] WALK={{.52f,.35f,.62f},{.63f,.42f,.60f},{.66f,.39f,.49f},{.66f,.37f,.48f},{.65f,.28f,.55f}};
    private static final float[][][] EAT={
        {{.52f,.38f,.64f},{.63f,.55f,.60f},{.69f,.56f,.50f},{.71f,.59f,.47f},{.73f,.48f,.47f}},
        {{.51f,.37f,.64f},{.62f,.54f,.60f},{.71f,.55f,.50f},{.70f,.57f,.47f},{.72f,.41f,.47f}}};
    private static final float[][][] POUNCE={
        {{.54f,.27f,.67f},{.66f,.40f,.60f},{.76f,.36f,.48f},{.75f,.40f,.46f},{.68f,.32f,.48f}},
        {{.49f,.37f,.65f},{.68f,.48f,.60f},{.72f,.48f,.48f},{.68f,.43f,.46f},{.74f,.36f,.45f}}};
    private static final float[][][] PETTED={
        {{.62f,.32f,.65f},{.70f,.39f,.60f},{.73f,.42f,.51f},{.79f,.38f,.49f},{.65f,.27f,.48f}},
        {{.50f,.50f,.65f},{.62f,.40f,.60f},{.67f,.50f,.51f},{.67f,.49f,.49f},{.68f,.39f,.48f}}};
    public static final class Head {
        public final float x,eyesY,width,angle;
        Head(float x,float eyesY,float width,float angle){this.x=x;this.eyesY=eyesY;this.width=width;this.angle=angle;}
    }
    public static final class Mount {
        public final float left,top,width,height,pivotX,pivotY,angle;
        public final boolean behind;
        Mount(float left,float top,float width,float height,float x,float y,float angle,boolean behind){
            this.left=left;this.top=top;this.width=width;this.height=height;this.pivotX=x;this.pivotY=y;this.angle=angle;this.behind=behind;
        }
    }
    private FitRocketPetRig(){}
    public static Head head(int stage,FitRocketPetMotion.Pose pose,int frame){
        if(stage<0||stage>4)throw new IllegalArgumentException("Unbekannte Entwicklungsstufe.");
        int phase=Math.floorMod(frame,2);float[] slot;float angle=0;
        switch(pose){
            case WALK:slot=WALK[stage];angle=phase==0?-1:1;break;
            case EAT:slot=EAT[phase][stage];angle=phase==0?8:5;break;
            case POUNCE:slot=POUNCE[phase][stage];angle=phase==0?0:-4;break;
            case PETTED:slot=PETTED[phase][stage];angle=phase==0?-5:5;break;
            default:slot=IDLE[stage];break;
        }
        return new Head(slot[0],slot[1],slot[2],angle);
    }
    /** Ear headbands sit behind the face. Caps/crowns end above the eye line instead of covering it. */
    public static Mount mount(int stage,FitRocketPetMotion.Pose pose,int frame,String item,float bodyWidth,float bodyHeight,float itemAspect){
        if(bodyWidth<=0||bodyHeight<=0||!Float.isFinite(itemAspect)||itemAspect<=0)throw new IllegalArgumentException("Ungültige Pet-Proportionen.");
        int index=FitRocketPet.itemIndex(item);if(index<1||index>3)return null;
        Head head=head(stage,pose,frame);float cranialWidth=bodyWidth*head.width;
        float x=-bodyWidth/2+bodyWidth*head.x,eyes=-bodyHeight+bodyHeight*head.eyesY;
        boolean ears=index==1;float width=cranialWidth*(ears?2.10f:index==2?1.22f:1.10f),height=width/itemAspect;
        float forehead=eyes-cranialWidth*(ears?0.38f:0.14f);
        float top=ears?forehead-height*0.20f:forehead-height;
        return new Mount(x-width/2,top,width,height,x,forehead,head.angle,ears);
    }
}
