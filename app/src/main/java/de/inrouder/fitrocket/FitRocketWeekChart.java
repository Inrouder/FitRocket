package de.inrouder.fitrocket;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/** Compact, accessible visual of logged daily food points for the current week.
 * Only days included in the report are drawn; missing entries remain unlogged.
 */
public final class FitRocketWeekChart extends View {
    private final JSONArray entries;
    private final int dailyBudget;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private final boolean dark;

    public FitRocketWeekChart(Context context,JSONArray entries,int dailyBudget) {
        super(context);
        this.entries=entries==null?new JSONArray():entries;
        this.dailyBudget=Math.max(1,dailyBudget);
        this.density=context.getResources().getDisplayMetrics().density;
        this.dark=FitRocketTheme.isDark(context);
        setMinimumHeight(dp(192));
        setContentDescription(accessibleSummary());
    }

    public String accessibleSummary(){
        StringBuilder result=new StringBuilder("Punkte pro Tag diese Woche. ");
        for(int i=0;i<entries.length();i++){
            JSONObject d=entries.optJSONObject(i);if(d==null)continue;
            result.append(d.optString("date")).append(": ");
            if(d.optBoolean("logged"))result.append(FitRocketPointLedger.display(d.optDouble("points"))).append(" Punkte");
            else result.append("kein Eintrag");
            result.append(". ");
        }
        return result.toString();
    }

    private int dp(float value){return (int)(value*density+.5f);}
    private void style(int color, float width){
        p.reset();p.setAntiAlias(true);p.setColor(color);p.setStrokeWidth(dp(width));
        p.setStrokeCap(Paint.Cap.ROUND);
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        float w=getWidth(),h=getHeight(),left=dp(16),right=w-dp(14),
            top=dp(28),bottom=h-dp(35);
        if(w<dp(170)||bottom<=top)return;
        double max=dailyBudget*1.15;
        for(int i=0;i<entries.length();i++){
            JSONObject row=entries.optJSONObject(i);
            if(row!=null)max=Math.max(max,row.optDouble("points",0)*1.14);
        }
        float baseline=bottom;
        float goalY=(float)(bottom-(bottom-top)*dailyBudget/max);

        style(dark?Color.rgb(75,79,97):Color.rgb(213,217,227),1);
        canvas.drawLine(left,baseline,right,baseline,p);
        style(dark?Color.rgb(170,191,212):Color.rgb(132,138,157),1);
        p.setPathEffect(new android.graphics.DashPathEffect(new float[]{dp(5),dp(4)},0));
        canvas.drawLine(left,goalY,right,goalY,p);
        p.setPathEffect(null);
        p.setTextSize(dp(10));
        p.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Ziel "+dailyBudget+" P",left,Math.max(dp(12),goalY-dp(5)),p);

        // Seven equally spaced slots, with future dates simply left empty.
        float unit=(right-left)/7f;
        for(int i=0;i<7;i++){
            float x=left+unit*(i+.5f);
            JSONObject row=i<entries.length()?entries.optJSONObject(i):null;
            boolean hasEntry=row!=null && row.optBoolean("logged");
            float measured=hasEntry?(float)Math.max(0,row.optDouble("points",0)):0f;
            float barHeight=(float)((bottom-top)*measured/max);
            float barWidth=Math.min(dp(25),unit*.56f);
            if(hasEntry){
                style(measured>dailyBudget
                    ? (dark?Color.rgb(250,163,145):Color.rgb(226,137,112))
                    : (dark?Color.rgb(171,211,190):Color.rgb(111,179,146)),1);
                canvas.drawRoundRect(x-barWidth/2,bottom-Math.max(dp(3),barHeight),
                    x+barWidth/2,bottom,dp(7),dp(7),p);
                style(dark?Color.WHITE:Color.rgb(34,44,62),1);
                p.setTextSize(dp(10));p.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(FitRocketPointLedger.display(measured),
                    x,Math.max(dp(12),bottom-barHeight-dp(6)),p);
            }else if(row!=null) {
                style(dark?Color.rgb(88,92,109):Color.rgb(224,226,235),1);
                canvas.drawRoundRect(x-barWidth/2,bottom-dp(4),x+barWidth/2,bottom,dp(3),dp(3),p);
            }
            LocalDate date;
            try {date=LocalDate.parse(row!=null?row.optString("date"):
                LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue()-1).plusDays(i).toString());}
            catch(Exception ex){date=LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue()-1).plusDays(i);}
            style(dark?Color.rgb(218,222,237):Color.rgb(85,94,112),1);
            p.setTextAlign(Paint.Align.CENTER);p.setTextSize(dp(11));
            canvas.drawText(date.getDayOfWeek().getDisplayName(TextStyle.SHORT,Locale.GERMAN),
                x,bottom+dp(20),p);
        }
    }
}
