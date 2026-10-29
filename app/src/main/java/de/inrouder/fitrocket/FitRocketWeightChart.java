package de.inrouder.fitrocket;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.TreeMap;

/**
 * Weight history with a proportional date axis, clear circular measurements and a
 * dashed goal line. All colours are explicit for reliable light/dark contrast.
 * Values are read-only; the chart never changes saved measurements.
 */
public final class FitRocketWeightChart extends View {
    private final TreeMap<LocalDate,Double> data=new TreeMap<>();
    private final Double goal;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final boolean dark;
    private final float density;
    private final int lineColor, textColor, axisColor, gridColor, goalColor, surfaceColor;

    public FitRocketWeightChart(Context context,List<JSONObject> weights,Double goal){
        super(context);
        dark=FitRocketTheme.isDark(context);
        density=getResources().getDisplayMetrics().density;
        lineColor=dark?Color.rgb(144,204,245):Color.rgb(36,54,78);
        textColor=dark?Color.rgb(229,234,246):Color.rgb(76,85,98);
        axisColor=dark?Color.rgb(127,139,163):Color.rgb(180,188,197);
        gridColor=dark?Color.rgb(58,68,87):Color.rgb(237,240,245);
        goalColor=dark?Color.rgb(244,192,117):Color.rgb(117,126,138);
        surfaceColor=dark?Color.rgb(31,35,49):Color.WHITE;
        this.goal=goal!=null&&Double.isFinite(goal)&&goal>0?goal:null;
        for(JSONObject entry:weights){
            try{
                LocalDate day=LocalDate.parse(entry.getString("date"));
                double kg=entry.getDouble("kg");
                if(Double.isFinite(kg)&&kg>0&&kg<500)data.put(day,kg);
            }catch(Exception ignored){}
        }
        setMinimumHeight(dp(255));
        setContentDescription("Gewichtsdiagramm mit "+data.size()+" Messungen"+
            (this.goal==null?"":", Ziel "+String.format(Locale.GERMAN,"%.1f Kilogramm",this.goal)));
    }
    private int dp(float n){return Math.round(n*density);}
    private void style(int color,float width){
        p.reset();p.setAntiAlias(true);p.setColor(color);p.setStrokeWidth(dp(width));
        p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
    }
    private void label(Canvas canvas,String value,float x,float y,Paint.Align align,int color,int sp){
        style(color,1);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
        p.setTextSize(dp(sp));p.setTextAlign(align);canvas.drawText(value,x,y,p);
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        float width=getWidth(),height=getHeight();
        float left=dp(46),right=width-dp(17),top=dp(20),bottom=height-dp(49);
        if(right-left<dp(65)||bottom-top<dp(80))return;
        canvas.drawColor(surfaceColor);
        if(data.isEmpty()){
            label(canvas,"Noch keine Gewichtsmessungen",width/2,height/2,
                Paint.Align.CENTER,textColor,14);
            return;
        }
        double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
        for(double weight:data.values()){min=Math.min(min,weight);max=Math.max(max,weight);}
        if(goal!=null){min=Math.min(min,goal);max=Math.max(max,goal);}
        double span=max-min;
        double pad=Math.max(1.5,span*0.12);
        double minY=Math.max(0,Math.floor(min-pad)),maxY=Math.ceil(max+pad);
        if(maxY-minY<4){maxY=minY+4;}
        final double low=minY,range=maxY-minY;
        java.util.function.DoubleFunction<Float> y=value->
            (float)(bottom-(value-low)/range*(bottom-top));

        // Sparse horizontal reference labels, not a busy grid.
        for(int i=0;i<=4;i++){
            float yy=bottom-(bottom-top)*i/4f;
            double weight=low+range*i/4d;
            if(i==0||i==4){
                style(axisColor,0.8f);canvas.drawLine(left,yy,right,yy,p);
            }else{
                style(gridColor,0.7f);canvas.drawLine(left,yy,right,yy,p);
            }
            label(canvas,String.format(Locale.GERMAN,"%.0f",weight),
                left-dp(8),yy+dp(4),Paint.Align.RIGHT,textColor,11);
        }
        style(axisColor,0.8f);canvas.drawLine(left,top,left,bottom,p);
        canvas.drawLine(left,bottom,right,bottom,p);

        // Match the reference: dashed horizontal target line with right-aligned weight.
        if(goal!=null){
            float targetY=y.apply(goal);
            style(goalColor,1.3f);
            p.setPathEffect(new DashPathEffect(new float[]{dp(7),dp(5)},0));
            canvas.drawLine(left,targetY,right,targetY,p);p.setPathEffect(null);
            // Keep annotation inside plot, above line unless near top.
            float captionY=targetY-dp(7);
            if(captionY<top+dp(12))captionY=targetY+dp(15);
            label(canvas,String.format(Locale.GERMAN,"Ziel %.1f kg",goal),
                right-dp(3),captionY,Paint.Align.RIGHT,goalColor,11);
        }

        LocalDate first=data.firstKey(),last=data.lastKey();
        long days=Math.max(1,ChronoUnit.DAYS.between(first,last));
        java.util.function.Function<LocalDate,Float> x=day->
            left+(right-left)*(float)ChronoUnit.DAYS.between(first,day)/days;
        Path path=new Path();boolean started=false;
        for(Map.Entry<LocalDate,Double> entry:data.entrySet()){
            float px=x.apply(entry.getKey()),py=y.apply(entry.getValue());
            if(!started){path.moveTo(px,py);started=true;}
            else path.lineTo(px,py);
        }
        style(lineColor,2.6f);p.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path,p);
        for(Map.Entry<LocalDate,Double> entry:data.entrySet()){
            float px=x.apply(entry.getKey()),py=y.apply(entry.getValue());
            style(lineColor,1);p.setStyle(Paint.Style.FILL);
            canvas.drawCircle(px,py,dp(3.7f),p);
        }
        // A visible latest-point halo, without hiding its true value.
        float lastX=x.apply(last),lastY=y.apply(data.get(last));
        style(surfaceColor,1);canvas.drawCircle(lastX,lastY,dp(6.8f),p);
        style(lineColor,1);canvas.drawCircle(lastX,lastY,dp(4.7f),p);

        // Calendar-accurate labels, spaced to avoid collisions on narrow phones.
        DateTimeFormatter fmt=DateTimeFormatter.ofPattern("dd.MM",Locale.GERMAN);
        int tickCount=(right-left)>=dp(320)?5:4;
        if(days<tickCount-1)tickCount=(int)days+1;
        tickCount=Math.max(1,tickCount);
        for(int i=0;i<tickCount;i++){
            long offset=tickCount==1?0:Math.round(days*i/(double)(tickCount-1));
            LocalDate tick=first.plusDays(offset);
            float xx=x.apply(tick);
            Paint.Align align=i==0?Paint.Align.LEFT:i==tickCount-1?Paint.Align.RIGHT:Paint.Align.CENTER;
            label(canvas,tick.format(fmt),xx,height-dp(17),align,textColor,10);
        }
    }
}
