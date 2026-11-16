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
import java.util.function.LongConsumer;
import java.util.function.LongFunction;

/** Fasting views use the same theme, rounded cards and spacing as the diary. */
public final class FitRocketFastingUi {
    private final Activity activity;
    private final SharedPreferences prefs;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private Runnable update,backAction,detailBack;
    private LinearLayout detailRoot;
    private boolean foreground,allHistory;
    private final Runnable tick=new Runnable(){public void run(){
        if(update!=null)update.run();
        if(foreground&&update!=null)handler.postDelayed(this,1000);
    }};
    public FitRocketFastingUi(Activity activity,SharedPreferences prefs){this.activity=activity;this.prefs=prefs;}
    public void clear(){handler.removeCallbacks(tick);update=null;backAction=null;detailRoot=null;detailBack=null;}
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
        clear();detailRoot=root;detailBack=back;root.removeAllViews();root.addView(button("‹ Zurück zum Tagebuch",back));
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
                timer.addView(caption("Starte ab jetzt oder trage ein, wann du zuletzt gegessen hast. Beginn und Ziel kannst du später korrigieren."));
                timer.addView(button("Fasten starten",()->startChoice(root,back)));
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
                timer.addView(button("Fasten beenden",()->finishTime(root,back)));
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
                for(int i=0;i<Math.min(allHistory?FitRocketFasting.HISTORY_LIMIT:7,history.length());i++){
                    final int index=i;JSONObject entry=history.optJSONObject(i);
                    log.addView(text(FitRocketFasting.plan(entry.optInt("planHours"))+" · "+FitRocketFasting.duration(entry.optLong("end")-entry.optLong("start")),17));
                    log.addView(caption(date(entry.optLong("start"))+" – "+date(entry.optLong("end"))+
                        (entry.optLong("end")>=entry.optLong("goal")?" · Ziel erreicht":" · Vor dem Ziel beendet")));
                    Button editEnd=button("Endzeit ändern",()->historyEnd(root,back,index,entry));
                    editEnd.setContentDescription("Endzeit des Fastens vom "+date(entry.optLong("start"))+" ändern");log.addView(editEnd);
                }
                if(history.length()>7)log.addView(button(allHistory?"Weniger anzeigen":"Alle Fastenzeiten anzeigen",()->{allHistory=!allHistory;detail(root,back);}));
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
    private void refreshDetail(){if(detailRoot!=null)detail(detailRoot,detailBack);}
    private void begin(LinearLayout root,Runnable back,long start){
        save(FitRocketFasting.start(state(),start,System.currentTimeMillis()));
        if(prefs.getBoolean(FitRocketFastingAlarmRules.ENABLED_KEY,true)&&!FitRocketFastingReminder.allowed(activity)&&!prefs.getBoolean("fastingNotificationAsked",false))
            FitRocketFastingReminder.requestPermission(activity);
        refreshDetail();
    }
    private void startChoice(LinearLayout root,Runnable back){
        new AlertDialog.Builder(activity).setTitle("Wann hast du mit dem Fasten begonnen?")
            .setPositiveButton("Ab jetzt",(d,w)->run(()->begin(root,back,System.currentTimeMillis())))
            .setNeutralButton("Früher begonnen",(d,w)->run(()->{
                long initial=Math.max(0,System.currentTimeMillis()-FitRocketFasting.HOUR);
                timeDialog("Fasten früher starten",initial,true,"Fasten starten","Abbrechen",
                    "Wähle das Datum und die Uhrzeit deiner letzten Mahlzeit.",
                    value->"Beginn · "+date(value)+"\nZiel · "+date(value+state().optInt("planHours")*FitRocketFasting.HOUR),
                    value->begin(root,back,value));
            })).setNegativeButton("Abbrechen",null).show();
    }
    private void pickTime(LinearLayout root,Runnable back,boolean start){run(()->{
        JSONObject expected=state().optJSONObject("active");if(expected==null)throw new IllegalArgumentException("Kein laufendes Fasten.");long initial=expected.optLong(start?"start":"goal");
        long duration=expected.optLong("goal")-expected.optLong("start");
        timeDialog(start?"Beginn ändern":"Zielzeit ändern",initial,start,"Speichern","Abbrechen",
            start?"Das Ziel verschiebt sich um denselben Zeitraum. Deine eingestellte Fastendauer bleibt erhalten.":"Passe dein geplantes Fastenziel an.",
            value->start?"Beginn · "+date(value)+"\nNeues Ziel · "+date(value+duration):"Neues Ziel · "+date(value),
            value->{
                JSONObject latest=state();requireSame(expected,latest.optJSONObject("active"));
                save(start?FitRocketFasting.moveStart(latest,value,System.currentTimeMillis()):
                    FitRocketFasting.edit(latest,expected.optLong("start"),value,System.currentTimeMillis()));
                refreshDetail();
            });
    });}
    private void finishTime(LinearLayout root,Runnable back){run(()->{
        JSONObject expected=state().optJSONObject("active");if(expected==null)throw new IllegalArgumentException("Kein laufendes Fasten.");
        timeDialog("Fasten beenden",System.currentTimeMillis(),true,"Beenden","Weiterfasten",
            "Prüfe deine tatsächliche Endzeit. Wenn du länger als geplant gefastet hast, wird die gesamte Zeit gespeichert.",
            value->"Begonnen · "+date(expected.optLong("start"))+"\nTatsächliches Ende · "+date(value)+
                "\nFastenzeit · "+FitRocketFasting.duration(value-expected.optLong("start")),
            value->{JSONObject latest=state();requireSame(expected,latest.optJSONObject("active"));
                save(FitRocketFasting.finish(latest,value,System.currentTimeMillis()));refreshDetail();});
    });}
    private void historyEnd(LinearLayout root,Runnable back,int index,JSONObject expected){run(()->{
        timeDialog("Endzeit im Verlauf ändern",expected.optLong("end"),true,"Speichern","Abbrechen",
            "Trage ein, wann du tatsächlich aufgehört hast. Beginn und geplantes Ziel bleiben erhalten.",
            value->"Begonnen · "+date(expected.optLong("start"))+"\nTatsächliches Ende · "+date(value)+
                "\nFastenzeit · "+FitRocketFasting.duration(value-expected.optLong("start")),
            value->{JSONObject latest=state();requireSame(expected,latest.optJSONArray("history").optJSONObject(index));
                JSONObject corrected=FitRocketFasting.editEnd(latest,index,value,System.currentTimeMillis());
                // A log correction must not dismiss or reschedule the current session's reminder.
                prefs.edit().putString(FitRocketFasting.KEY,corrected.toString()).apply();refreshDetail();});
    });}
    private void requireSame(JSONObject expected,JSONObject latest){
        if(latest==null||expected.optLong("start")!=latest.optLong("start")||expected.optLong("goal")!=latest.optLong("goal")||
            expected.optLong("end",-1)!=latest.optLong("end",-1)||expected.optInt("planHours")!=latest.optInt("planHours"))
            throw new IllegalArgumentException("Die Fastenzeiten haben sich inzwischen geändert. Bitte öffne den Dialog erneut.");
    }
    /** Dates/times are a local draft; only a valid confirmation changes persisted state. */
    private void timeDialog(String title,long initial,boolean pastOnly,String positive,String negative,String hint,
                            LongFunction<String> preview,LongConsumer accept){
        final ZonedDateTime[] selected={Instant.ofEpochMilli(initial).atZone(ZoneId.systemDefault())};
        LinearLayout form=column();form.setPadding(dp(20),dp(8),dp(20),dp(8));
        form.addView(caption(hint));TextView summary=text("",16);form.addView(summary);
        Button dateButton=button("",()->{}),timeButton=button("",()->{});form.addView(dateButton);form.addView(timeButton);
        TextView error=text("",14);error.setTextColor(FitRocketTheme.text(activity));error.setVisibility(View.GONE);form.addView(error);
        Runnable refresh=()->{summary.setText(preview.apply(selected[0].toInstant().toEpochMilli()));
            dateButton.setText("Datum · "+selected[0].format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            timeButton.setText("Uhrzeit · "+selected[0].format(DateTimeFormatter.ofPattern("HH:mm")));error.setVisibility(View.GONE);};
        dateButton.setContentDescription("Datum wählen");timeButton.setContentDescription("Uhrzeit wählen");
        dateButton.setOnClickListener(v->{ZonedDateTime time=selected[0];
            DatePickerDialog picker=new DatePickerDialog(activity,(view,y,m,d)->run(()->{
                selected[0]=LocalDateTime.of(LocalDate.of(y,m+1,d),selected[0].toLocalTime()).atZone(ZoneId.systemDefault());refresh.run();
            }),time.getYear(),time.getMonthValue()-1,time.getDayOfMonth());
            if(pastOnly)picker.getDatePicker().setMaxDate(System.currentTimeMillis());picker.setTitle("Datum wählen");picker.show();});
        timeButton.setOnClickListener(v->{ZonedDateTime time=selected[0];
            TimePickerDialog picker=new TimePickerDialog(activity,(view,h,min)->run(()->{
                selected[0]=selected[0].toLocalDate().atTime(h,min).atZone(ZoneId.systemDefault());refresh.run();
            }),time.getHour(),time.getMinute(),true);picker.setTitle("Uhrzeit wählen");picker.show();});
        refresh.run();ScrollView scroll=new ScrollView(activity);scroll.addView(form);
        LinearLayout content=column();content.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout actions=column();actions.setPadding(dp(16),dp(4),dp(16),dp(10));
        content.addView(actions,new LinearLayout.LayoutParams(-1,-2));
        AlertDialog dialog=new AlertDialog.Builder(activity).setTitle(title).setView(content).create();
        Button confirm=button(positive,()->{
            try{accept.accept(selected[0].toInstant().toEpochMilli());dialog.dismiss();}
            catch(Exception e){error.setText(e.getMessage());error.setVisibility(View.VISIBLE);error.announceForAccessibility(e.getMessage());
                scroll.post(()->scroll.smoothScrollTo(0,form.getHeight()));}
        });confirm.setTag("fastingConfirm");actions.addView(confirm);
        Button cancel=button(negative,dialog::dismiss);cancel.setTag("fastingCancel");actions.addView(cancel);
        // The body gets remaining space; both actions stay outside its scroll area even with large fonts.
        dialog.setOnShowListener(v->{android.util.DisplayMetrics metrics=activity.getResources().getDisplayMetrics();
            dialog.getWindow().setLayout((int)(metrics.widthPixels*.94f),(int)(metrics.heightPixels*.90f));});
        dialog.show();
    }
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
