package de.inrouder.fitrocket;

import android.app.*;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/** Fasting views use the same theme, rounded cards and spacing as the diary. */
public final class FitRocketFastingUi {
    private final Activity activity;
    private final SharedPreferences prefs;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private Runnable update,backAction;
    private boolean foreground;
    private final Runnable tick=new Runnable(){public void run(){
        if(update!=null)update.run();
        if(foreground&&update!=null)handler.postDelayed(this,1000);
    }};
    public FitRocketFastingUi(Activity activity,SharedPreferences prefs){this.activity=activity;this.prefs=prefs;}
    public void clear(){handler.removeCallbacks(tick);update=null;backAction=null;}
    public boolean handleBack(){if(backAction==null)return false;backAction.run();return true;}
    public void pause(){foreground=false;handler.removeCallbacks(tick);}
    public void resume(){foreground=true;handler.removeCallbacks(tick);tick.run();}
    private void bind(Runnable callback){update=callback;handler.removeCallbacks(tick);tick.run();}
    private int dp(float value){return (int)(value*activity.getResources().getDisplayMetrics().density+0.5f);}
    private int accent(){return FitRocketTheme.accent(activity);}
    private LinearLayout column(){LinearLayout l=new LinearLayout(activity);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView text(String value,int size){
        TextView t=new TextView(activity);t.setText(value);t.setTextSize(size);t.setTextColor(FitRocketTheme.text(activity));
        t.setPadding(dp(4),dp(5),dp(4),dp(5));return t;
    }
    private TextView caption(String value){TextView t=text(value,13);t.setTextColor(FitRocketTheme.secondary(activity));return t;}
    private LinearLayout card(){
        LinearLayout c=column();c.setPadding(dp(14),dp(13),dp(14),dp(13));
        c.setBackground(shape(FitRocketTheme.surface(activity),24));c.setElevation(dp(6));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(11));c.setLayoutParams(lp);return c;
    }
    private Button button(String title,Runnable action){
        Button b=new Button(activity);b.setText(title);b.setAllCaps(false);b.setTextSize(14);b.setMinHeight(dp(48));
        b.setTextColor(FitRocketTheme.text(activity));b.setBackground(shape(FitRocketTheme.map(activity,Color.rgb(240,231,255)),14));
        b.setPadding(dp(12),dp(9),dp(12),dp(9));b.setOnClickListener(v->action.run());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(6),0,dp(6));b.setLayoutParams(lp);return b;
    }
    private JSONObject state(){
        try{return FitRocketFasting.validated(new JSONObject(prefs.getString(FitRocketFasting.KEY,FitRocketFasting.empty().toString())));}
        catch(Exception e){throw new IllegalArgumentException("Fastendaten konnten nicht gelesen werden.",e);}
    }
    private void save(JSONObject state){
        FitRocketFastingReminder.dismiss(activity);
        prefs.edit().putString(FitRocketFasting.KEY,state.toString()).apply();
        FitRocketFastingReminder.schedule(activity);
    }
    private void run(Runnable action){try{action.run();}catch(Exception e){Toast.makeText(activity,e.getMessage(),Toast.LENGTH_LONG).show();}}
    private String date(long timestamp){
        ZonedDateTime time=Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault());
        LocalDate day=time.toLocalDate(),today=LocalDate.now();
        String prefix=day.equals(today)?"Heute":day.equals(today.plusDays(1))?"Morgen":day.equals(today.minusDays(1))?"Gestern":day.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        return prefix+", "+time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    public View compact(Runnable open){
        LinearLayout c=card();TextView title=text("Intervallfasten",17);title.setTypeface(null,android.graphics.Typeface.BOLD);c.addView(title);
        try{
            JSONObject state=state(),active=state.optJSONObject("active");
            if(active==null){
                c.addView(caption("Dein Plan · "+FitRocketFasting.plan(state.optInt("planHours"))+" · freiwillig und flexibel"));
                c.addView(button("Fasten öffnen",open));
            }else{
                TextView timer=text("",23),status=caption("");c.addView(timer);c.addView(status);
                c.addView(button("Laufendes Fasten ansehen",open));
                bind(()->{long now=System.currentTimeMillis();timer.setText(FitRocketFasting.duration(FitRocketFasting.elapsed(active,now)));
                    status.setText(now<active.optLong("start")?"Gerätezeit liegt vor dem Start · bitte prüfen":now>=active.optLong("goal")?"Ziel erreicht · du kannst dein Fasten beenden":"Ziel · "+date(active.optLong("goal")));});
            }
        }catch(Exception e){c.addView(caption(e.getMessage()));}
        return c;
    }
    public void detail(LinearLayout root,Runnable back){
        clear();root.removeAllViews();root.addView(button("‹ Zurück zum Tagebuch",back));
        try{
            JSONObject state=state(),active=state.optJSONObject("active");
            LinearLayout plan=card();TextView title=text("Dein Fastenplan · "+FitRocketFasting.plan(state.optInt("planHours")),19);
            title.setTypeface(null,android.graphics.Typeface.BOLD);plan.addView(title);
            plan.addView(caption(state.optInt("planHours")+" Stunden Fasten · "+(24-state.optInt("planHours"))+" Stunden Essensfenster"));
            if(active!=null&&active.optLong("goal")-active.optLong("start")!=state.optInt("planHours")*FitRocketFasting.HOUR)
                plan.addView(caption("Angepasstes Ziel · "+FitRocketFasting.duration(active.optLong("goal")-active.optLong("start"))+" nach dem Start"));
            plan.addView(button("Plan auswählen / ändern",()->plans(root,back)));root.addView(plan);
            LinearLayout timer=card();root.addView(timer);
            if(active==null){
                timer.addView(text("Dein Rhythmus. Dein Tempo.",21));
                timer.addView(caption("Starte nach deiner letzten Mahlzeit. Den Beginn und dein Ziel kannst du anschließend anpassen."));
                timer.addView(button("Fasten jetzt starten",()->run(()->{save(FitRocketFasting.start(state(),System.currentTimeMillis()));
                    if(prefs.getBoolean(FitRocketFastingAlarmRules.ENABLED_KEY,true)&&!FitRocketFastingReminder.allowed(activity)&&!prefs.getBoolean("fastingNotificationAsked",false))
                        FitRocketFastingReminder.requestPermission(activity);
                    detail(root,back);})));
            }else{
                TextView elapsedCaption=caption("Vergangene Zeit");elapsedCaption.setGravity(Gravity.CENTER);timer.addView(elapsedCaption);
                TextView elapsed=text("",38);elapsed.setGravity(Gravity.CENTER);elapsed.setTypeface(null,android.graphics.Typeface.BOLD);elapsed.setSingleLine(true);
                elapsed.setAutoSizeTextTypeUniformWithConfiguration(20,38,1,android.util.TypedValue.COMPLEX_UNIT_SP);timer.addView(elapsed);
                TextView status=text("",15);status.setGravity(Gravity.CENTER);status.setTextColor(accent());timer.addView(status);
                ProgressBar progress=new ProgressBar(activity,null,android.R.attr.progressBarStyleHorizontal);
                progress.setMax(1000);progress.setProgressTintList(android.content.res.ColorStateList.valueOf(accent()));
                progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(FitRocketTheme.divider(activity)));
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(18));p.setMargins(dp(5),dp(18),dp(5),dp(14));timer.addView(progress,p);
                LinearLayout times=new LinearLayout(activity);times.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout begun=column(),goal=column();
                begun.setPadding(0,0,dp(6),0);goal.setPadding(dp(6),0,0,0);
                begun.addView(caption("Begonnen"));begun.addView(text(date(active.optLong("start")),16));
                Button editStart=button("Bearbeiten",()->pickTime(root,back,true));editStart.setContentDescription("Start bearbeiten");begun.addView(editStart);
                goal.addView(caption("Ziel"));goal.addView(text(date(active.optLong("goal")),16));
                Button editGoal=button("Bearbeiten",()->pickTime(root,back,false));editGoal.setContentDescription("Ziel bearbeiten");goal.addView(editGoal);
                times.addView(begun,new LinearLayout.LayoutParams(0,-2,1));times.addView(goal,new LinearLayout.LayoutParams(0,-2,1));timer.addView(times);
                timer.addView(button("Fasten beenden",()->new AlertDialog.Builder(activity).setTitle("Fasten beenden?")
                    .setMessage("Deine bisherige Fastenzeit wird im Verlauf gespeichert, auch wenn du vor dem Ziel aufhörst.")
                    .setPositiveButton("Beenden",(d,w)->run(()->{save(FitRocketFasting.finish(state(),System.currentTimeMillis()));detail(root,back);}))
                    .setNegativeButton("Weiterfasten",null).show()));
                timer.addView(button("Fasten abbrechen",()->new AlertDialog.Builder(activity).setTitle("Fasten abbrechen?")
                    .setMessage("Dieses Fasten wird ohne Verlaufseintrag verworfen.")
                    .setPositiveButton("Verwerfen",(d,w)->run(()->{save(FitRocketFasting.cancel(state()));detail(root,back);}))
                    .setNegativeButton("Zurück",null).show()));
                bind(()->{long now=System.currentTimeMillis(),remaining=active.optLong("goal")-now;
                    elapsed.setText(FitRocketFasting.duration(FitRocketFasting.elapsed(active,now)));
                    status.setText(now<active.optLong("start")?"Gerätezeit liegt vor dem Start · bitte prüfen":remaining<=0?"Ziel erreicht · du entscheidest, wann du beendest":"Noch "+FitRocketFasting.duration(remaining)+" bis zum Ziel");
                    progress.setProgress((int)(1000*FitRocketFasting.progress(active,now)));});
            }
            LinearLayout reminder=card();
            Switch enable=new Switch(activity);enable.setText("Benachrichtigung zum Fastenziel");
            enable.setTextColor(FitRocketTheme.text(activity));enable.setChecked(prefs.getBoolean(FitRocketFastingAlarmRules.ENABLED_KEY,true));
            reminder.addView(enable);
            enable.setOnCheckedChangeListener((view,checked)->{
                prefs.edit().putBoolean(FitRocketFastingAlarmRules.ENABLED_KEY,checked).apply();
                FitRocketFastingReminder.schedule(activity);
                if(checked&&!FitRocketFastingReminder.allowed(activity))FitRocketFastingReminder.requestPermission(activity);
                detail(root,back);
            });
            if(enable.isChecked()){
                if(!FitRocketFastingReminder.allowed(activity)){
                    reminder.addView(caption("Android hat Benachrichtigungen noch nicht freigegeben."));
                    reminder.addView(button("Benachrichtigungen erlauben",()->FitRocketFastingReminder.requestPermission(activity)));
                }else reminder.addView(caption("Du wirst auch bei geschlossener App an dein Fastenziel erinnert."));
                if(!FitRocketFastingReminder.exact(activity)){
                    reminder.addView(caption("Ohne Alarmzugriff kann Android die Erinnerung verzögern."));
                    reminder.addView(button("Zeitgenaue Erinnerung zulassen",()->FitRocketFastingReminder.requestExact(activity)));
                }
            }
            root.addView(reminder);
            root.addView(caption("Die Zeitangaben beschreiben deinen Plan. Sie messen keine körperlichen Fastenphasen. Dein Punktebudget bleibt unabhängig davon."));
            JSONArray history=state.optJSONArray("history");
            if(history.length()>0){
                LinearLayout log=card();log.addView(text("Deine letzten Fastenzeiten",18));
                for(int i=0;i<Math.min(7,history.length());i++){
                    JSONObject entry=history.optJSONObject(i);
                    log.addView(text(FitRocketFasting.plan(entry.optInt("planHours"))+" · "+FitRocketFasting.duration(entry.optLong("end")-entry.optLong("start")),17));
                    log.addView(caption(date(entry.optLong("start"))+" – "+date(entry.optLong("end"))+
                        (entry.optLong("end")>=entry.optLong("goal")?" · Ziel erreicht":" · Vor dem Ziel beendet")));
                }
                root.addView(log);
            }
        }catch(Exception e){root.addView(caption(e.getMessage()));}
    }
    private void plans(LinearLayout root,Runnable back){
        clear();backAction=()->detail(root,back);root.removeAllViews();root.addView(button("‹ Zurück zu deinem Fasten",()->detail(root,back)));
        int[] hours={12,14,16,17,18,20};
        String[] names={"Zirkadianes Fasten","Anfängerfasten","Dein täglicher Rhythmus","Längere Fastenzeit","Expertenfasten","Kurzes Essensfenster"};
        String[] descriptions={"12 Stunden Fasten und 12 Stunden Zeit für deine Mahlzeiten.","14 Stunden Fasten und ein Essensfenster von 10 Stunden.","16 Stunden Fasten und 8 Stunden für deine Mahlzeiten.","17 Stunden Fasten und ein Essensfenster von 7 Stunden.","18 Stunden Fasten und 6 Stunden für deine Mahlzeiten.","20 Stunden Fasten und ein Essensfenster von 4 Stunden."};
        for(int i=0;i<hours.length;i++){
            final int h=hours[i];
            if(i==0||i==2||i==4){TextView group=text(i==0?"Einstieg":i==2?"Fortgeschritten":"Erfahren",20);group.setTypeface(null,android.graphics.Typeface.BOLD);root.addView(group);}
            LinearLayout c=card(),row=new LinearLayout(activity);row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(new PlanRing(h),new LinearLayout.LayoutParams(dp(64),dp(64)));
            LinearLayout labels=column();labels.setPadding(dp(9),0,0,0);
            int selected=state().optInt("planHours",16);
            TextView title=text(FitRocketFasting.plan(h)+" · "+names[i]+(h==selected?" ✓":""),17);title.setTypeface(null,android.graphics.Typeface.BOLD);labels.addView(title);
            labels.addView(caption(descriptions[i]));row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));c.addView(row);
            c.setContentDescription(FitRocketFasting.plan(h)+" "+names[i]);
            c.setOnClickListener(v->run(()->{
                JSONObject current=state();
                if(current.has("active"))new AlertDialog.Builder(activity).setTitle("Fastenplan ändern?")
                    .setMessage("Das Ziel des laufenden Fastens wird auf "+h+" Stunden nach dem gespeicherten Start gesetzt.")
                    .setPositiveButton("Übernehmen",(d,w)->run(()->{save(FitRocketFasting.selectPlan(state(),h));detail(root,back);}))
                    .setNegativeButton("Abbrechen",null).show();
                else{save(FitRocketFasting.selectPlan(current,h));detail(root,back);}
            }));root.addView(c);
        }
    }
    private void pickTime(LinearLayout root,Runnable back,boolean start){run(()->{
        JSONObject active=state().optJSONObject("active");
        ZonedDateTime time=Instant.ofEpochMilli(active.optLong(start?"start":"goal")).atZone(ZoneId.systemDefault());
        DatePickerDialog picker=new DatePickerDialog(activity,(v,y,m,d)->{
            TimePickerDialog clock=new TimePickerDialog(activity,(view,h,min)->run(()->{
                long value=LocalDateTime.of(y,m+1,d,h,min).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                JSONObject latest=state(),a=latest.optJSONObject("active");
                save(FitRocketFasting.edit(latest,start?value:a.optLong("start"),start?a.optLong("goal"):value,System.currentTimeMillis()));
                detail(root,back);
            }),time.getHour(),time.getMinute(),true);
            clock.setTitle(start?"Beginn des Fastens":"Zielzeit des Fastens");clock.show();
        },time.getYear(),time.getMonthValue()-1,time.getDayOfMonth());
        if(start)picker.getDatePicker().setMaxDate(System.currentTimeMillis());
        picker.setTitle(start?"Startdatum":"Zieldatum");picker.show();
    });}
    private final class PlanRing extends View {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final int hours;
        PlanRing(int hours){super(activity);this.hours=hours;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){
            float size=Math.min(getWidth(),getHeight()),pad=dp(8),stroke=dp(8);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(stroke);
            RectF rect=new RectF(pad,pad,size-pad,size-pad);
            paint.setColor(FitRocketTheme.map(activity,Color.rgb(212,242,216)));canvas.drawArc(rect,-90,360,false,paint);
            paint.setColor(accent());canvas.drawArc(rect,-90,360f*hours/24,false,paint);
        }
    }
}
