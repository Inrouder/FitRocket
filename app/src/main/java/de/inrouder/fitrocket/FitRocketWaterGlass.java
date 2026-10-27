package de.inrouder.fitrocket;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Tap-friendly illustrated 250 ml water glass, filled or empty. */
public final class FitRocketWaterGlass extends View {
    private boolean full;
    private final float density;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);

    public FitRocketWaterGlass(Context context,boolean filled){
        super(context);full=filled;density=getResources().getDisplayMetrics().density;
        setMinimumWidth(dp(43));setMinimumHeight(dp(62));
        setContentDescription(full?"250 Milliliter Wasser getrunken":"Noch ein 250-Milliliter-Glas");
    }
    public void setFull(boolean value){full=value;invalidate();}
    private int dp(float n){return (int)(n*density+.5f);}

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float cx=getWidth()/2f,top=dp(7),bottom=getHeight()-dp(12);
        float width=Math.min(getWidth()-dp(14),dp(32));
        float half=width/2f;
        RectF outer=new RectF(cx-half,top,cx+half,bottom);
        p.setShader(null);p.setStyle(Paint.Style.FILL);
        p.setColor(FitRocketTheme.isDark(getContext())?Color.rgb(38,47,60):Color.rgb(248,250,255));c.drawRoundRect(outer,dp(6),dp(6),p);
        if(full){
            RectF fluid=new RectF(outer.left+dp(2),top+dp(14),outer.right-dp(2),bottom-dp(2));
            p.setShader(new LinearGradient(fluid.left,fluid.top,fluid.right,fluid.bottom,
                Color.rgb(173,222,244),Color.rgb(104,187,236),Shader.TileMode.CLAMP));
            c.drawRoundRect(fluid,dp(4),dp(4),p);p.setShader(null);
        }
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));
        p.setColor(full?Color.rgb(96,166,209):(FitRocketTheme.isDark(getContext())?Color.rgb(105,123,156):Color.rgb(203,213,232)));
        c.drawRoundRect(outer,dp(6),dp(6),p);
        p.setStyle(Paint.Style.FILL);p.setColor(FitRocketTheme.secondary(getContext()));
        p.setTextAlign(Paint.Align.CENTER);p.setTextSize(dp(10));
        c.drawText(full?"✓":"+",cx,top+(bottom-top)/2+dp(4),p);
        p.setColor(Color.rgb(139,150,170));p.setTextSize(dp(9));
        c.drawText("250",cx,getHeight()-dp(1),p);
    }
}
