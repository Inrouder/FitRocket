package de.inrouder.fitrocket;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Simple, offline weight chart in FitRocket's pastel design. */
public final class FitRocketWeightChart extends View {
    private final TreeMap<LocalDate,Double> data=new TreeMap<>();
    private final Double goal;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int navy;
    private final int lavender;
    private float density;

    public FitRocketWeightChart(Context context,List<JSONObject> weights,Double goal){
        super(context);
        this.navy=FitRocketTheme.text(context);
        this.lavender=FitRocketTheme.isDark(context)?Color.rgb(185,166,245):Color.rgb(130,132,212);
        this.goal=goal;
        this.density=getResources().getDisplayMetrics().density;
        for(JSONObject w:weights){
            try{
                LocalDate date=LocalDate.parse(w.getString("date"));
                double kg=w.getDouble("kg");
                if(Double.isFinite(kg)&&kg>0)data.put(date,kg);
            }catch(Exception ignored){}
        }
        setMinimumHeight(dp(220));
    }

    private int dp(float value){return (int)(value*density+.5f);}
    private void paint(int color,float width){
        p.reset();p.setAntiAlias(true);p.setColor(FitRocketTheme.map(getContext(),color));p.setStrokeWidth(dp(width));
        p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        float w=getWidth(),h=getHeight();
        float left=dp(44),right=w-dp(18),top=dp(20),bottom=h-dp(38);
        if(right<=left||bottom<=top)return;

        if(data.isEmpty()){
            paint(navy,1);p.setTextSize(dp(14));p.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Dein Gewichtsverlauf erscheint hier",w/2,h/2,p);return;
        }
        double low=Double.MAX_VALUE,high=-Double.MAX_VALUE;
        for(double v:data.values()){low=Math.min(low,v);high=Math.max(high,v);}
        if(goal!=null){low=Math.min(low,goal);high=Math.max(high,goal);}
        low=Math.floor(low-2);high=Math.ceil(high+2);
        if(high-low<5){double mid=(high+low)/2;low=mid-2.5;high=mid+2.5;}
        final double ymin=low,ymax=high;
        java.util.function.DoubleFunction<Float> yFor=value->(float)(bottom-(value-ymin)/(ymax-ymin)*(bottom-top));
        paint(Color.rgb(228,231,238),1);
        for(int tick=0;tick<=4;tick++){
            float yy=top+(bottom-top)*tick/4f;canvas.drawLine(left,yy,right,yy,p);
            paint(Color.rgb(130,141,160),1);p.setTextSize(dp(10));
            p.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.format(java.util.Locale.GERMAN,"%.0f",high-(high-low)*tick/4),left-dp(8),yy+dp(3),p);
            paint(Color.rgb(228,231,238),1);
        }
        if(goal!=null){
            float y=yFor.apply(goal);
            paint(Color.rgb(94,179,141),1.4f);p.setPathEffect(new DashPathEffect(new float[]{dp(7),dp(5)},0));
            canvas.drawLine(left,y,right,y,p);p.setPathEffect(null);
            p.setTextSize(dp(10));p.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("Ziel "+String.format(java.util.Locale.GERMAN,"%.1f kg",goal),right,y-dp(6),p);
        }
        int size=data.size(),index=0;
        Path graph=new Path();
        Path fill=new Path();boolean first=true;
        float firstX=left,lastX=left,lastY=bottom;
        for(Map.Entry<LocalDate,Double> entry:data.entrySet()){
            float x=size<2?(left+right)/2:left+(right-left)*index/(size-1f);
            float y=yFor.apply(entry.getValue());
            if(first){graph.moveTo(x,y);fill.moveTo(x,bottom);fill.lineTo(x,y);firstX=x;first=false;}
            else{graph.lineTo(x,y);fill.lineTo(x,y);}
            lastX=x;lastY=y;index++;
        }
        fill.lineTo(lastX,bottom);fill.close();
        paint(Color.rgb(225,225,251),1);p.setStyle(Paint.Style.FILL);canvas.drawPath(fill,p);
        paint(lavender,3);p.setStyle(Paint.Style.STROKE);canvas.drawPath(graph,p);
        index=0;
        for(Map.Entry<LocalDate,Double> entry:data.entrySet()){
            float x=size<2?(left+right)/2:left+(right-left)*index/(size-1f);
            float y=yFor.apply(entry.getValue());
            // Avoid the palette mapper darkening the latest data point.
            // A white point with a lavender outline remains visible on dark cards.
            boolean latest=index==size-1;
            if(latest && FitRocketTheme.isDark(getContext())){
                paint(Color.rgb(194,159,244),2);p.setStyle(Paint.Style.FILL);
                canvas.drawCircle(x,y,dp(7),p);
                paint(Color.WHITE,1);p.setStyle(Paint.Style.FILL);
                canvas.drawCircle(x,y,dp(5),p);
            }else{
                paint(latest?navy:lavender,1);p.setStyle(Paint.Style.FILL);
                canvas.drawCircle(x,y,dp(latest?5:3.4f),p);
            }
            index++;
        }
        if(!data.isEmpty()){
            paint(navy,1);p.setTextSize(dp(11));p.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(data.firstKey().format(DateTimeFormatter.ofPattern("dd.MM")),left,h-dp(12),p);
            p.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(data.lastKey().format(DateTimeFormatter.ofPattern("dd.MM")),right,h-dp(12),p);
        }
    }
}
