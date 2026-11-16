package de.inrouder.fitrocket;

import android.app.*;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/** Native, scrollable pet minigame with generated raster art and a separate fictional food balance. */
public final class FitRocketPetUi {
    private final Activity activity;
    private final SharedPreferences prefs;
    private final LinearLayout root;
    private final int budget;
    private final List<JSONObject> meals;
    private FitRocketPetView petView;
    private String renderedDate="";
    private boolean foreground=true;
    private final android.os.Handler dayHandler=new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable dayCheck=new Runnable(){public void run(){
        if(!foreground)return;
        if(!renderedDate.equals(LocalDate.now().toString()))render(null);
        else dayHandler.postDelayed(this,30000);
    }};
    public FitRocketPetUi(Activity activity,SharedPreferences prefs,LinearLayout root,int budget,List<JSONObject> meals){
        this.activity=activity;this.prefs=prefs;this.root=root;this.budget=budget;this.meals=meals;
    }
    public void pause(){foreground=false;dayHandler.removeCallbacks(dayCheck);if(petView!=null)petView.setRunning(false);}
    public void resume(){foreground=true;dayHandler.removeCallbacks(dayCheck);if(!renderedDate.equals(LocalDate.now().toString()))render(null);else {if(petView!=null)petView.setRunning(true);dayHandler.postDelayed(dayCheck,30000);}}
    public void clear(){pause();petView=null;}
    private int dp(float n){return (int)(n*activity.getResources().getDisplayMetrics().density+0.5f);}
    private LinearLayout column(){LinearLayout l=new LinearLayout(activity);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView text(String value,int size){TextView t=new TextView(activity);t.setText(value);t.setTextSize(size);t.setTextColor(FitRocketTheme.text(activity));t.setPadding(dp(5),dp(5),dp(5),dp(5));return t;}
    private TextView caption(String value){TextView t=text(value,13);t.setTextColor(FitRocketTheme.secondary(activity));return t;}
    private LinearLayout card(){LinearLayout c=column();c.setPadding(dp(13),dp(12),dp(13),dp(12));c.setBackground(shape(FitRocketTheme.surface(activity),24));c.setElevation(dp(5));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(12));c.setLayoutParams(p);return c;}
    private Button button(String name,Runnable action){
        Button b=new Button(activity);b.setText(name);b.setAllCaps(false);b.setTextSize(13);b.setTextColor(FitRocketTheme.text(activity));
        b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(dp(48));b.setPadding(dp(8),dp(9),dp(8),dp(9));
        b.setBackground(shape(FitRocketTheme.map(activity,Color.rgb(240,231,255)),14));b.setOnClickListener(v->run(action));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(2),dp(5),dp(2),dp(5));b.setLayoutParams(p);return b;
    }
    private void run(Runnable action){try{action.run();}catch(Exception e){Toast.makeText(activity,e.getMessage(),Toast.LENGTH_LONG).show();}}
    private JSONObject state(){try{return FitRocketPet.validated(new JSONObject(prefs.getString(FitRocketPet.KEY,"{}")));}catch(Exception e){throw new IllegalArgumentException("Pet-Daten konnten nicht gelesen werden. Bitte prüfe deine Sicherung.",e);}}
    private void save(JSONObject state){prefs.edit().putString(FitRocketPet.KEY,state.toString()).apply();}
    private void change(JSONObject next,String reaction){save(next);render(reaction);}
    public void render(){render(null);}
    private void render(String reaction){
        dayHandler.removeCallbacks(dayCheck);
        if(petView!=null)petView.setRunning(false);
        root.removeAllViews();renderedDate=LocalDate.now().toString();
        try{
            JSONObject state=state();
            if(state.length()==0){adoption();return;}
            Map<LocalDate,Double> consumed=new HashMap<>();
            for(JSONObject meal:meals){
                LocalDate day;try{day=LocalDate.parse(meal.optString("date"));}catch(Exception ignored){continue;}
                consumed.merge(day,FitRocketPointLedger.value(meal),Double::sum);
            }
            int oldFood=state.optInt("foodTenths");
            state=FitRocketPet.harvest(state,LocalDate.now(),budget,d->consumed.getOrDefault(d,0.0));
            save(state);
            final JSONObject snapshot=state;
            String name=state.optString("name");int stage=FitRocketPet.stage(state),mood=FitRocketPet.mood(state,LocalDate.now());
            LinearLayout hero=card();
            TextView title=text(name,24);title.setTypeface(null,android.graphics.Typeface.BOLD);hero.addView(title);
            TextView stageLabel=text("Stufe "+(stage+1)+" · "+FitRocketPet.STAGE_NAMES[stage],14);stageLabel.setTextColor(FitRocketTheme.accent(activity));hero.addView(stageLabel);
            petView=new FitRocketPetView(activity);petView.show(stage,mood,state.optString("equippedHead"),state.optString("equippedToy"));
            petView.setBackground(shape(FitRocketTheme.map(activity,Color.rgb(240,231,255)),20));petView.setClipToOutline(true);
            hero.addView(petView,new LinearLayout.LayoutParams(-1,dp(270)));petView.setRunning(foreground);
            petView.setOnClickListener(v->run(()->care(false)));
            if(reaction!=null)petView.react();
            String[] moods={"Glücklich","Entspannt","Traurig","Grummelig","Monsterlaune · Zähnchen voraus!"};
            TextView status=text(moods[mood],18);status.setTypeface(null,android.graphics.Typeface.BOLD);hero.addView(status);
            int missed=FitRocketPet.missedDays(state,LocalDate.now());
            hero.addView(caption(missed>0?name+" hat an "+missed+" abgeschlossenen Tag(en) kein Futter bekommen.":"Tippe auf "+name+" zum Streicheln."));
            if(reaction!=null)hero.addView(text(reaction,14));root.addView(hero);

            LinearLayout balances=card(),row=new LinearLayout(activity);row.setOrientation(LinearLayout.HORIZONTAL);
            metric(row,FitRocketPet.points(state.optInt("foodTenths")),"Futterpunkte");
            metric(row,String.valueOf(state.optInt("stars")),"Sterne");metric(row,(stage+1)+" / 5","Entwicklung");
            balances.addView(row);
            if(stage<4){
                int start=FitRocketPet.STAGES[stage],target=FitRocketPet.STAGES[stage+1],fed=state.optInt("fedTenths");
                ProgressBar bar=new ProgressBar(activity,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(target-start);bar.setProgress(fed-start);
                bar.setProgressTintList(android.content.res.ColorStateList.valueOf(FitRocketTheme.accent(activity)));balances.addView(bar,new LinearLayout.LayoutParams(-1,dp(16)));
                balances.addView(caption("Noch "+FitRocketPet.points(target-fed)+" Futterpunkte bis zur nächsten Stufe."));
            }else balances.addView(caption("Sternenwächter erreicht! Dein Begleiter freut sich weiter über eure gemeinsamen Momente."));
            LinearLayout actions=new LinearLayout(activity);actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.addView(button("Füttern",()->feedDialog()),weighted());
            actions.addView(button("Spielen",()->care(true)),weighted());
            actions.addView(button("Streicheln",()->care(false)),weighted());balances.addView(actions);root.addView(balances);

            LinearLayout food=card();food.addView(text("Gespart wird zu Futter",18));
            if(state.optInt("foodTenths")>oldFood)food.addView(text("Neu eingesammelt: "+FitRocketPet.points(state.optInt("foodTenths")-oldFood)+" Futterpunkte",15));
            food.addView(caption("Alle übrigen Tagespunkte werden nach Tagesabschluss zusätzlich zu fiktivem Futter. Dein echtes Tages- und Wochenkonto bleibt vollständig erhalten."));
            double todayUsed=consumed.getOrDefault(LocalDate.now(),0.0);
            food.addView(caption("Für morgen aktuell vorgemerkt: "+FitRocketPet.points((int)Math.round(Math.max(0,budget-todayUsed)*10))+" Futterpunkte. Weitere Mahlzeiten können diesen Wert noch ändern."));
            root.addView(food);

            LinearLayout care=card();care.addView(text("Kleine gemeinsame Momente",18));
            care.addView(caption("Streicheln bringt einmal täglich 1 Stern, Spielen 2 Sterne. Kuscheln und Spielen kannst du so oft du möchtest. Beim Füttern gibt es je vollem Futterpunkt 1 Stern."));
            root.addView(care);
            wardrobe(snapshot);
            LinearLayout personal=card();personal.addView(button("Pet-Namen ändern",()->renameDialog()));root.addView(personal);
        }catch(Exception e){root.addView(caption(e.getMessage()==null?"Pet konnte nicht geladen werden.":e.getMessage()));}
        if(foreground)dayHandler.postDelayed(dayCheck,30000);
    }
    private LinearLayout.LayoutParams weighted(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(2),dp(5),dp(2),dp(5));return p;}
    private void metric(LinearLayout row,String value,String label){LinearLayout c=column();TextView n=text(value,20);n.setTypeface(null,android.graphics.Typeface.BOLD);c.addView(n);c.addView(caption(label));row.addView(c,new LinearLayout.LayoutParams(0,-2,1));}
    private void adoption(){
        LinearLayout card=card();TextView title=text("Ein kleiner Stern wartet auf dich",23);title.setTypeface(null,android.graphics.Typeface.BOLD);card.addView(title);
        petView=new FitRocketPetView(activity);petView.show(0,0,"none","ball");petView.setBackground(shape(FitRocketTheme.map(activity,Color.rgb(240,231,255)),20));petView.setClipToOutline(true);
        card.addView(petView,new LinearLayout.LayoutParams(-1,dp(270)));petView.setRunning(foreground);
        card.addView(caption("Adoptiere deinen Pixel-Begleiter. Mit fiktivem Futter wächst er durch fünf Stufen; Streicheln, Spielen und Zubehör machen ihn zu deinem Pet."));
        EditText name=new EditText(activity);name.setText("Novi");name.setSingleLine(true);name.setTextColor(FitRocketTheme.text(activity));name.setHint("Name deines Pets");name.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(20)});card.addView(name);
        card.addView(button("Ei adoptieren",()->change(FitRocketPet.adopt(state(),LocalDate.now(),name.getText().toString()),"Willkommen bei FitRocket!")));
        card.addView(caption("Du kannst die Restpunkte von gestern als erstes Futter einsammeln. Ab dann zählt jeder abgeschlossene Tag einmal. Deine echten Ernährungspunkte werden nicht verbraucht."));root.addView(card);
    }
    private void care(boolean play){
        JSONObject state=state();String day=LocalDate.now().toString();boolean already=day.equals(state.optString(play?"playedOn":"pettedOn"));
        String name=state.optString("name");
        change(FitRocketPet.care(state,LocalDate.now(),play),play?
            name+" spielt mit "+("rocket_toy".equals(state.optString("equippedToy"))?"der Rakete":"dem Ball")+(already?"!":"! +2 Sterne"):
            name+" genießt deine Streicheleinheit"+(already?"!":"! +1 Stern"));
    }
    private void feedDialog(){
        JSONObject state=state();int available=state.optInt("foodTenths");
        if(available<1){Toast.makeText(activity,"Dein Futtervorrat ist leer. Neue Restpunkte werden nach Tagesabschluss zu Futter.",Toast.LENGTH_LONG).show();return;}
        LinearLayout form=column();form.setPadding(dp(20),dp(8),dp(20),dp(8));
        form.addView(text("Vorrat: "+FitRocketPet.points(available)+" fiktive Futterpunkte",16));
        EditText amount=new EditText(activity);amount.setInputType(8194);amount.setText(available>=10?"1":"0,1");amount.setTextColor(FitRocketTheme.text(activity));amount.setHint("Wie viele Futterpunkte?");form.addView(amount);
        form.addView(caption("Das Futter lässt dein Pet wachsen. Dein echtes Punktekonto bleibt unverändert."));
        AlertDialog dialog=new AlertDialog.Builder(activity).setTitle(state.optString("name")+" füttern").setView(form)
            .setPositiveButton("Füttern",null).setNegativeButton("Abbrechen",null).create();
        dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(btn->run(()->{
            int tenths;
            try{tenths=new BigDecimal(amount.getText().toString().trim().replace(',','.')).movePointRight(1).intValueExact();}
            catch(Exception e){throw new IllegalArgumentException("Bitte Futterpunkte mit höchstens einer Nachkommastelle eingeben.");}
            JSONObject old=state(),next=FitRocketPet.feed(old,tenths,LocalDate.now());
            int previous=FitRocketPet.stage(old),current=FitRocketPet.stage(next);dialog.dismiss();
            change(next,current>previous?next.optString("name")+" ist gewachsen! Stufe "+(current+1)+" erreicht.":"Mmmh! "+next.optString("name")+" freut sich über das Futter.");
        })));dialog.show();
    }
    private void wardrobe(JSONObject state){
        LinearLayout wardrobe=card();wardrobe.addView(text("Zubehör & Spielzeug",18));
        wardrobe.addView(caption("Mit Sternen freischalten, Lieblingsstück anziehen – dein Zubehör bleibt gespeichert."));
        for(int i=0;i<FitRocketPet.ITEMS.length;i++){
            final String id=FitRocketPet.ITEMS[i];final int index=i;
            LinearLayout row=new LinearLayout(activity);row.setGravity(Gravity.CENTER_VERTICAL);
            if(i>0)row.addView(new FitRocketPetView.ItemIcon(activity,i),new LinearLayout.LayoutParams(dp(52),dp(52)));
            LinearLayout info=column();TextView name=text(FitRocketPet.ITEM_NAMES[i],15);name.setTypeface(null,android.graphics.Typeface.BOLD);info.addView(name);
            boolean equipped=id.equals(state.optString(i<4?"equippedHead":"equippedToy")),owned=FitRocketPet.owns(state,id);
            info.addView(caption(equipped?"Ausgerüstet":owned?"Freigeschaltet":FitRocketPet.PRICES[i]+" Sterne"));
            row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            Button equip=button(equipped?"Aktiv":owned?"Wählen":"Freischalten",()->{
                if(FitRocketPet.owns(state(),id)){change(FitRocketPet.equip(state(),id),null);return;}
                new AlertDialog.Builder(activity).setTitle(FitRocketPet.ITEM_NAMES[index]+" freischalten?")
                    .setMessage("Das kostet "+FitRocketPet.PRICES[index]+" deiner Spielsterne.")
                    .setPositiveButton("Freischalten",(d,w)->run(()->change(FitRocketPet.equip(state(),id),"Neues Lieblingsstück freigeschaltet!")))
                    .setNegativeButton("Abbrechen",null).show();
            });equip.setEnabled(!equipped);row.addView(equip,new LinearLayout.LayoutParams(dp(103),-2));wardrobe.addView(row);
        }
        root.addView(wardrobe);
    }
    private void renameDialog(){
        EditText input=new EditText(activity);input.setText(state().optString("name"));input.setSingleLine(true);input.setTextColor(FitRocketTheme.text(activity));input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(20)});
        AlertDialog dialog=new AlertDialog.Builder(activity).setTitle("Pet-Namen ändern").setView(input).setPositiveButton("Speichern",null).setNegativeButton("Abbrechen",null).create();
        dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(btn->run(()->{JSONObject next=FitRocketPet.rename(state(),input.getText().toString());dialog.dismiss();change(next,null);})));dialog.show();
    }
}
