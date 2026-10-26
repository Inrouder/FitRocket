package de.inrouder.fitrocket;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.text.*;
import org.json.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
  private static final int EXPORT_REQUEST=81,IMPORT_REQUEST=82;
  private final ArrayList<JSONObject> foods=new ArrayList<>(), meals=new ArrayList<>(), weights=new ArrayList<>(), activities=new ArrayList<>();
  private android.content.SharedPreferences prefs;
  private LinearLayout page, entries;
  private GlassView glass;
  private TextView summary;
  private int budget=23, weekly=0;
  private final int NAVY=Color.rgb(37,46,76), BLUE=Color.rgb(112,117,225), LIGHT=Color.rgb(255,247,242), PEACH=Color.rgb(255,220,204), MINT=Color.rgb(212,242,216), LAV=Color.rgb(240,231,255);
  private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
  private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
  private GradientDrawable gradient(int a,int b,int radius){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});d.setCornerRadius(dp(radius));return d;}
  private LinearLayout card(int color){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(16),dp(15),dp(16),dp(15));l.setBackground(shape(color,22));l.setElevation(dp(2));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,dp(12));l.setLayoutParams(lp);return l;}
  private void heading(String title){TextView t=label(title,19);t.setTypeface(null,1);page.addView(t);}
  private TextView chip(String s,int color){TextView t=label(s,13);t.setBackground(shape(color,30));t.setPadding(dp(12),dp(7),dp(12),dp(7));return t;}
  private void action(LinearLayout parent,String name,Runnable fn){Button b=button(name);parent.addView(b);b.setOnClickListener(v->fn.run());}

  private String today(){return LocalDate.now().toString();}
  private String pretty(String date){try{return LocalDate.parse(date).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));}catch(Exception e){return date;}}
  private TextView label(String text,int sp){TextView t=new TextView(this);t.setText(text);t.setTextSize(sp);t.setTextColor(NAVY);t.setPadding(dp(8),dp(7),dp(8),dp(7));return t;}
  private Button button(String text){Button b=new Button(this);b.setText(text);b.setTextSize(14);b.setAllCaps(false);b.setTextColor(NAVY);b.setBackground(gradient(Color.rgb(255,221,204),Color.rgb(255,239,229),19));return b;}
  private EditText edit(String hint,String value,int input){EditText e=new EditText(this);e.setHint(hint);e.setSingleLine(true);e.setInputType(input);if(value!=null)e.setText(value);return e;}
  private void msg(String text){Toast.makeText(this,text,Toast.LENGTH_LONG).show();}
  @Override public void onCreate(Bundle saved){super.onCreate(saved);prefs=getSharedPreferences("fitrocket",MODE_PRIVATE);budget=prefs.getInt("budget",23);weekly=prefs.getInt("weekly",0);loadFoods();readArray("meals",meals);readArray("weights",weights);readArray("activities",activities);home();}
  private void readArray(String key,ArrayList<JSONObject> into){into.clear();try{JSONArray arr=new JSONArray(prefs.getString(key,"[]"));for(int i=0;i<arr.length();i++)into.add(arr.getJSONObject(i));}catch(Exception ignored){}}
  private void persist(String key,ArrayList<JSONObject> items){JSONArray a=new JSONArray();for(JSONObject x:items)a.put(x);prefs.edit().putString(key,a.toString()).apply();}
  private void loadFoods(){try(InputStream in=getAssets().open("foods.json")){ByteArrayOutputStream o=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)o.write(b,0,n);JSONArray a=new JSONArray(new String(o.toByteArray(),StandardCharsets.UTF_8));for(int i=0;i<a.length();i++)foods.add(a.getJSONObject(i));}catch(Exception e){msg("Lebensmitteldatenbank konnte nicht geladen werden.");}}
  private void shell(String heading){
    LinearLayout frame=new LinearLayout(this);frame.setOrientation(1);
    frame.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(255,247,241),Color.rgb(247,239,255),Color.rgb(255,249,244)}));
    setContentView(frame);
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);
    page=new LinearLayout(this);page.setOrientation(1);page.setPadding(dp(18),dp(17),dp(18),dp(28));
    scroll.addView(page);frame.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout hero=card(Color.rgb(255,240,230));hero.setBackground(gradient(Color.rgb(255,237,224),Color.rgb(240,231,255),24));
    TextView title=label(heading.equals("FitRocket")?"🚀 FitRocket":heading,25);title.setTypeface(null,1);hero.addView(title);
    hero.addView(label(heading.equals("FitRocket")?"Deine Tagesbalance":"Alles an einem Ort",13));page.addView(hero);
    LinearLayout nav=new LinearLayout(this);nav.setPadding(dp(12),dp(7),dp(12),dp(9));
    nav.setOrientation(LinearLayout.HORIZONTAL);nav.setBackground(shape(Color.rgb(255,254,253),20));
    String[] pages={"⌂ Heute","⌕ Suche","▦ Tagebuch","⋯ Mehr"};
    for(int i=0;i<pages.length;i++){final int index=i;TextView item=label(pages[i],12);item.setGravity(Gravity.CENTER);
      if((heading.equals("FitRocket")&&i==0)||(heading.equals("Lebensmittel")&&i==1)||(heading.equals("Tagebuch")&&i==2)||(heading.equals("Mehr")&&i==3)){item.setBackground(shape(Color.rgb(255,230,224),16));}
      nav.addView(item,new LinearLayout.LayoutParams(0,dp(46),1));item.setOnClickListener(v->{switch(index){case 0:home();break;case 1:searchScreen();break;case 2:history();break;default:more();}});
    }
    nav.setPadding(dp(12),dp(7),dp(12),dp(40));
    frame.addView(nav);
  }
  private int used(String date){int total=0;for(JSONObject x:meals)if(date.equals(x.optString("date")))total+=x.optInt("points");return total;}
  private int count(String date){int n=0;for(JSONObject x:meals)if(date.equals(x.optString("date")))n++;return n;}
  private String kind(JSONObject f){return f.isNull("points")?"Punkte wählen":f.optInt("points")+" P";}
  private void home(){
    shell("FitRocket");
    LinearLayout balance=card(Color.WHITE);
    TextView subtitle=label("Deine Tagesbalance · "+pretty(today()),18);subtitle.setTypeface(null,1);balance.addView(subtitle);
    glass=new GlassView();balance.addView(glass,new LinearLayout.LayoutParams(-1,dp(225)));
    int n=used(today()),remaining=Math.max(0,budget-n);
    LinearLayout summaries=new LinearLayout(this);summaries.setOrientation(LinearLayout.HORIZONTAL);
    TextView usedChip=chip("🍴 "+n+" P\ngenutzt",Color.rgb(255,241,229));
    TextView freeChip=chip("🌿 "+remaining+" P\nfrei",MINT);
    TextView extraChip=chip("★ "+availableWeek()+" P\nWochenpuffer",LAV);
    for(TextView x:new TextView[]{usedChip,freeChip,extraChip}){x.setGravity(Gravity.CENTER);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(2),0,dp(2),0);summaries.addView(x,lp);}balance.addView(summaries);page.addView(balance);
    LinearLayout activityCard=card(MINT);activityCard.addView(label("🚶 Bewegung · "+earnedWeek()+" Aktivpunkte diese Woche",16));action(activityCard,"Aktivität erfassen ›",()->activityPage());page.addView(activityCard);
    LinearLayout main=card(Color.rgb(255,248,242));
    action(main,"⌕  Lebensmittel suchen    ›",()->searchScreen());
    LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);
    Button manual=button("＋ Schnell manuell"),assistant=button("☰ Textassistent");
    actions.addView(manual,new LinearLayout.LayoutParams(0,dp(52),1));
    actions.addView(assistant,new LinearLayout.LayoutParams(0,dp(52),1));
    manual.setOnClickListener(v->manual());assistant.setOnClickListener(v->textHelper());main.addView(actions);page.addView(main);
    heading("Heute");
    entries=new LinearLayout(this);entries.setOrientation(1);page.addView(entries);
    int found=0;for(int i=meals.size()-1;i>=0;i--){JSONObject m=meals.get(i);if(today().equals(m.optString("date"))){mealRow(m,entries);found++;}}
    if(found==0){LinearLayout empty=card(Color.WHITE);empty.addView(label("🌷 Noch keine Mahlzeit eingetragen",16));empty.addView(label("Dein Tag beginnt hier – wähle dein erstes Lebensmittel.",13));entries.addView(empty);}
  }
  private void mealRow(JSONObject m,LinearLayout target){
    LinearLayout c=card(Color.WHITE);LinearLayout upper=new LinearLayout(this);upper.setOrientation(LinearLayout.HORIZONTAL);upper.setGravity(Gravity.CENTER_VERTICAL);
    TextView name=label("🍽  "+m.optString("name"),16);name.setTypeface(null,1);
    upper.addView(name,new LinearLayout.LayoutParams(0,-2,1));
    upper.addView(chip(m.optInt("points")+" P",m.optInt("points")==0?MINT:Color.rgb(255,227,208)));
    c.addView(upper);
    c.addView(label(m.optString("group","Mahlzeit")+" · "+pretty(m.optString("date")),12));
    target.addView(c);
    c.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(m.optString("name")).setItems(new String[]{"Punkte ändern","Eintrag löschen"},(d,which)->{if(which==0)editMeal(m);else new AlertDialog.Builder(this).setMessage("Eintrag löschen?").setPositiveButton("Löschen",(dd,w)->{meals.remove(m);persist("meals",meals);home();}).setNegativeButton("Abbrechen",null).show();}).show());
  }
  private void editMeal(JSONObject m){EditText e=edit("Punkte",String.valueOf(m.optInt("points")),2);new AlertDialog.Builder(this).setTitle("Punkte anpassen").setView(e).setPositiveButton("Speichern",(d,w)->{try{int n=Integer.parseInt(e.getText().toString());if(n<0||n>2000)throw new Exception();m.put("points",n);m.put("quality","Manuell korrigiert");persist("meals",meals);home();}catch(Exception ignored){msg("Ungültige Punkte.");}}).setNegativeButton("Abbrechen",null).show();}
  private void searchScreen(){
    shell("Lebensmittel");LinearLayout searchCard=card(Color.WHITE);
    TextView title=label("⌕ Lebensmittel suchen",18);title.setTypeface(null,1);searchCard.addView(title);
    EditText search=edit("Suche nach Lebensmitteln…","",1);
    search.setSingleLine(true);search.setPadding(dp(14),dp(12),dp(14),dp(12));search.setBackground(shape(Color.rgb(248,243,253),20));searchCard.addView(search);
    page.addView(searchCard);
    heading("Deine Lebensmittelauswahl");
    LinearLayout results=new LinearLayout(this);results.setOrientation(1);page.addView(results);
    Runnable filter=()->{results.removeAllViews();String q=search.getText().toString().toLowerCase(Locale.ROOT).trim();
      if(q.length()<2){LinearLayout hint=card(Color.WHITE);hint.addView(label("🍎 Suche nach Obst, Gemüse oder deinem Lieblingsessen",15));results.addView(hint);return;}
      int count=0;for(JSONObject f:foods){String value=(f.optString("name")+" "+f.optString("alias")+" "+f.optString("category")).toLowerCase(Locale.ROOT);
        if(!value.contains(q))continue;count++;
        LinearLayout item=card(Color.WHITE);
        LinearLayout top=new LinearLayout(this);top.setOrientation(LinearLayout.HORIZONTAL);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView name=label(f.optString("name"),16);name.setTypeface(null,1);top.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        String text="calculated_estimate".equals(f.optString("status"))?"ca. "+f.optInt("estimatedPoints100")+" P / 100 g":(f.isNull("points")?"Punkte wählen":f.optInt("points")+" P");
        top.addView(chip(text, f.optInt("points")==0&&!f.isNull("points")?MINT:Color.rgb(255,227,208)));
        item.addView(top);item.addView(label(f.optString("category")+" · "+f.optString("portion"),12));
        results.addView(item);item.setOnClickListener(v->foodDialog(f));if(count>=70)break;
      }
      if(count==0){LinearLayout empty=card(Color.WHITE);empty.addView(label("Keine Treffer – du kannst eine Mahlzeit manuell erfassen.",15));results.addView(empty);}
    };
    search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){filter.run();}public void afterTextChanged(Editable s){}});
    filter.run();
  }
  private double num(String s) { return Double.parseDouble(s.trim().replace(',','.')); }
  // Publicly reconstructed approximation; not the proprietary historic WW formula.
  private int estimatedSmartPoints(double kcal,double saturated,double sugar,double protein) {
    if(!Double.isFinite(kcal)||!Double.isFinite(saturated)||!Double.isFinite(sugar)||!Double.isFinite(protein)||
      kcal<0||saturated<0||sugar<0||protein<0) throw new IllegalArgumentException();
    return Math.max(0,(int)Math.round(kcal*0.0305+saturated*0.275+sugar*0.12-protein*0.098));
  }
  private int calcForGrams(JSONObject f,double grams){
    JSONObject n=f.optJSONObject("nutrition");
    if(n==null||grams<=0||grams>10000)throw new IllegalArgumentException();
    double factor=grams/100.0;
    return estimatedSmartPoints(n.optDouble("kcal")*factor,n.optDouble("saturatedFat_g")*factor,n.optDouble("sugar_g")*factor,n.optDouble("protein_g")*factor);
  }
  private void nutritionDialog(JSONObject f) {
    LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(20),dp(8),dp(20),dp(8));
    content.addView(label(f.optString("name")+" · ca. Punkte nach Nährwerten",16));
    content.addView(label("Menge in Gramm bzw. Milliliter",13));
    EditText amount=edit("Menge","100",8194);content.addView(amount);
    JSONObject n=f.optJSONObject("nutrition");if(n==null)return;
    content.addView(label("Werte je 100 g/ml: "+n.optDouble("kcal")+" kcal · "+n.optDouble("sugar_g")+" g Zucker · "+n.optDouble("saturatedFat_g")+" g gesättigte Fette · "+n.optDouble("protein_g")+" g Protein",13));
    TextView estimate=label("ca. "+calcForGrams(f,100)+" Punkte",17);content.addView(estimate);
    amount.addTextChangedListener(new TextWatcher(){
      public void beforeTextChanged(CharSequence s,int a,int b,int c){}
      public void onTextChanged(CharSequence s,int a,int b,int c){
        try {estimate.setText("ca. "+calcForGrams(f,num(s.toString()))+" Punkte");}
        catch(Exception ex){estimate.setText("Menge eingeben");}
      }
      public void afterTextChanged(Editable e){}
    });
    Spinner group=groupSpinner();content.addView(group);
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Portion berechnen").setView(content).setNegativeButton("Abbrechen",null).setPositiveButton("Eintragen",null).create();
    dialog.setOnShowListener(x->dialog.getButton(-1).setOnClickListener(v->{
      try{double grams=num(amount.getText().toString());int pts=calcForGrams(f,grams);
        addMeal(f.optString("name")+" ("+grams+" g/ml)",pts,f.optString("id"),"Nährwertschätzung",group.getSelectedItem().toString());
        dialog.dismiss();home();
      }catch(Exception ex){msg("Bitte eine gültige Menge angeben.");}
    }));
    dialog.show();
  }
  private void foodDialog(JSONObject f){if("calculated_estimate".equals(f.optString("status"))){nutritionDialog(f);return;}LinearLayout content=new LinearLayout(this);content.setPadding(22,8,22,8);content.setOrientation(1);content.addView(label(f.optString("category")+"\n"+kind(f)+"\nPortion: "+f.optString("portion")+"\n"+f.optString("notes"),14));EditText mult=edit("Portionsfaktor","1",8194),points=edit("Punkte pro Portion",f.isNull("points")?"":String.valueOf(f.optInt("points")),2);content.addView(label("Portionsfaktor: bei 150 g Referenzportion z. B. 0,5 für 75 g",13));content.addView(mult);content.addView(points);Spinner group=groupSpinner();content.addView(group);AlertDialog d=new AlertDialog.Builder(this).setTitle(f.optString("name")).setView(content).setNegativeButton("Abbrechen",null).setPositiveButton("Buchen",null).create();d.setOnShowListener(v->d.getButton(-1).setOnClickListener(btn->{try{double factor=Double.parseDouble(mult.getText().toString().replace(',','.'));int pointsPer=Integer.parseInt(points.getText().toString());if(!Double.isFinite(factor)||factor<=0||factor>100||pointsPer<0||pointsPer>2000)throw new Exception();int total=(int)Math.round(pointsPer*factor);if(total>2000)throw new Exception();addMeal(f.optString("name")+" × "+factor,total,f.optString("id"),kind(f),group.getSelectedItem().toString());d.dismiss();home();}catch(Exception ex){msg("Bitte gültige Portion und Punkte eingeben.");}}));d.show();}
  private Spinner groupSpinner(){Spinner s=new Spinner(this);String[] groups={"Frühstück","Mittagessen","Abendessen","Snack","Getränke"};ArrayAdapter<String> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,groups);s.setAdapter(a);return s;}
  private void addMeal(String name,int points,String foodId,String quality,String group){try{JSONObject x=new JSONObject();x.put("name",name);x.put("points",points);x.put("foodId",foodId);x.put("quality",quality);x.put("group",group);x.put("date",today());meals.add(x);persist("meals",meals);}catch(Exception e){msg("Speichern fehlgeschlagen.");}}
  private void manual(){LinearLayout l=new LinearLayout(this);l.setPadding(22,4,22,4);l.setOrientation(1);EditText name=edit("Lebensmittel / Gericht","",1),points=edit("Punkte (manuell)",null,2);l.addView(name);l.addView(points);Spinner g=groupSpinner();l.addView(g);AlertDialog d=new AlertDialog.Builder(this).setTitle("Manuell erfassen").setView(l).setNegativeButton("Abbrechen",null).setPositiveButton("Eintragen",null).create();d.setOnShowListener(v->d.getButton(-1).setOnClickListener(btn->{try{String n=name.getText().toString().trim();int p=Integer.parseInt(points.getText().toString());if(n.length()<2||p<0||p>2000)throw new Exception();addMeal(n,p,"","Manuell",g.getSelectedItem().toString());d.dismiss();home();}catch(Exception ex){msg("Bitte Name und gültige Punkte angeben.");}}));d.show();}
  private void textHelper(){EditText input=new EditText(this);input.setMinLines(3);input.setHint("Beispiel: Ich hatte eine Banane und einen Apfel");new AlertDialog.Builder(this).setTitle("Offline-Texterfassung (keine KI)").setMessage("Erkennt eindeutige Lebensmittelnamen aus deiner Datenbank. Mengen und Zutaten müssen vor dem Eintragen geprüft werden.").setView(input).setPositiveButton("Analysieren",(d,w)->interpret(input.getText().toString())).setNegativeButton("Abbrechen",null).show();}
  private void interpret(String source){String q=source.toLowerCase(Locale.ROOT);ArrayList<JSONObject> matches=new ArrayList<>();for(JSONObject f:foods){String name=f.optString("name").toLowerCase(Locale.ROOT);if(name.length()>=5&&q.contains(name))matches.add(f);}if(matches.size()!=1){new AlertDialog.Builder(this).setTitle("Bitte Lebensmittel auswählen").setMessage(matches.isEmpty()?"Keine eindeutige Übereinstimmung gefunden. Bitte suche das Lebensmittel manuell.":"Mehrere Lebensmittel erkannt. Bitte erfasse die Zutaten einzeln über die Suche.").setPositiveButton("Zur Suche",(d,w)->searchScreen()).show();return;}foodDialog(matches.get(0));}
  private void history(){shell("Tagebuch");heading("Mein Tagebuch");page.addView(label("Alle Mahlzeiten auf einen Blick",14));for(int i=meals.size()-1;i>=0;i--)mealRow(meals.get(i),page);if(meals.isEmpty())page.addView(label("Noch keine gespeicherten Einträge.",15));}
  private void more(){shell("Mehr");Button act=button("🚶 Aktivitäten & Bonuspunkte");page.addView(act);act.setOnClickListener(v->activityPage());Button week=button("📈 Wochen- und Monatsauswertung");page.addView(week);week.setOnClickListener(v->statistics());Button weight=button("⚖ Gewicht erfassen und Verlauf ansehen");page.addView(weight);weight.setOnClickListener(v->weightDialog());Button settings=button("⚙ Budget & Wochen-Extra");page.addView(settings);settings.setOnClickListener(v->settingsDialog());Button backup=button("⬆ Backup exportieren (JSON)");page.addView(backup);backup.setOnClickListener(v->startExport());Button restore=button("⬇ Backup importieren");page.addView(restore);restore.setOnClickListener(v->startImport());}
  private void settingsDialog(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(25,5,25,5);EditText b=edit("Tagesbudget",String.valueOf(budget),2),w=edit("Wochen-Extra",String.valueOf(weekly),2);l.addView(label("Tagesbudget (persönlich festlegen)",14));l.addView(b);l.addView(label("Wochen-Extra (frei einstellbar; keine automatische WW-Ermittlung)",14));l.addView(w);new AlertDialog.Builder(this).setTitle("Budget").setView(l).setPositiveButton("Speichern",(d,n)->{try{int bb=Integer.parseInt(b.getText().toString()),ww=Integer.parseInt(w.getText().toString());if(bb<1||bb>200||ww<0||ww>500)throw new Exception();budget=bb;weekly=ww;prefs.edit().putInt("budget",bb).putInt("weekly",ww).apply();more();}catch(Exception e){msg("Ungültige Budgetwerte.");}}).setNegativeButton("Abbrechen",null).show();}
  private void statistics(){shell("Statistiken");LocalDate now=LocalDate.now(),start=now.minusDays(now.getDayOfWeek().getValue()-1);page.addView(label("Diese Woche",20));int weeklyUsed=0;for(int i=0;i<7;i++){LocalDate date=start.plusDays(i);int n=used(date.toString());weeklyUsed+=n;page.addView(label(date.getDayOfWeek().toString().substring(0,2)+" "+pretty(date.toString())+": "+n+" Punkte",15));}page.addView(label("Gesamt: "+weeklyUsed+" Punkte; Ø "+String.format(Locale.GERMAN,"%.1f",weeklyUsed/7.0)+" pro Kalendertag",15));page.addView(label("Dieser Monat",20));LocalDate first=now.withDayOfMonth(1);int total=0,tracked=0,days=now.getDayOfMonth();for(int i=0;i<days;i++){String day=first.plusDays(i).toString();total+=used(day);if(count(day)>0)tracked++;}page.addView(label("Erfasste Punkte: "+total+" · Tage mit Einträgen: "+tracked+" / "+days,16));page.addView(label("Ø je dokumentiertem Tag: "+(tracked>0?String.format(Locale.GERMAN,"%.1f",total/(double)tracked):"–"),16));page.addView(label("Achtung: Fehlende Tage gelten nicht als Null-Punkte-Tage.",13));if(!weights.isEmpty()){page.addView(label("Gewichtseinträge: "+weights.size(),16));for(int i=weights.size()-1;i>=Math.max(0,weights.size()-8);i--)page.addView(label(pretty(weights.get(i).optString("date"))+" · "+weights.get(i).optString("kg")+" kg",14));}}
  private void weightDialog(){LinearLayout l=new LinearLayout(this);l.setPadding(20,5,20,5);l.setOrientation(1);EditText e=edit("Gewicht in kg, z. B. 78,5","",8194);l.addView(e);new AlertDialog.Builder(this).setTitle("Gewicht dokumentieren").setView(l).setPositiveButton("Speichern",(d,w)->{try{double kg=Double.parseDouble(e.getText().toString().replace(',','.'));if(!Double.isFinite(kg)||kg<20||kg>400)throw new Exception();JSONObject x=new JSONObject();x.put("date",today());x.put("kg",kg);weights.add(x);persist("weights",weights);statistics();}catch(Exception ex){msg("Gewicht ungültig.");}}).setNegativeButton("Abbrechen",null).show();}

  private LocalDate weekStart(){LocalDate d=LocalDate.now();return d.minusDays(d.getDayOfWeek().getValue()-1);}
  private int earnedWeek(){int sum=0;String start=weekStart().toString(),end=weekStart().plusDays(7).toString();for(JSONObject a:activities){String date=a.optString("date");if(date.compareTo(start)>=0&&date.compareTo(end)<0)sum+=a.optInt("points");}return sum;}
  private int spentWeek(){int sum=0;LocalDate start=weekStart();for(int i=0;i<7;i++){int used=used(start.plusDays(i).toString());sum+=Math.max(0,used-budget);}return sum;}
  private int availableWeek(){return Math.max(0,weekly+activityCredit()-spentWeek());}
  private int activityCredit(){int option=prefs.getInt("activityShare",0);return (int)Math.floor(earnedWeek()*option/100.0);}
  // A transparent MET-based net activity estimate. This is NOT the WW proprietary FitPoints algorithm.
  private int estimateActivity(double kg,int duration,double met){if(!Double.isFinite(kg)||!Double.isFinite(met)||kg<=0||duration<0)throw new IllegalArgumentException();double net=Math.max(0,met-1)*3.5*kg/200.0*duration;return Math.max(0,(int)Math.round(net/65.0));}
  private void activityPage(){
    shell("Aktivität");heading("Deine Bewegung");
    LinearLayout overview=card(Color.WHITE);
    overview.addView(label("Diese Woche: "+earnedWeek()+" Aktivpunkte",20));
    overview.addView(label("Für Wochenpuffer freigegeben: "+activityCredit()+" Punkte",16));
    overview.addView(label("Wochenpuffer: "+weekly+" Basis + "+activityCredit()+" Aktivität − "+spentWeek()+" verbraucht = "+availableWeek()+" übrig",14));
    overview.addView(label("Tagesbudget bleibt "+budget+" Punkte",13));page.addView(overview);
    LinearLayout settings=card(MINT);settings.addView(label("So nutzt du Aktivpunkte",17));
    String[] modes={"0 % · nur sammeln","50 % · teilweise verwenden","100 % · vollständig verwenden"};
    Spinner share=new Spinner(this);share.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,modes));
    int chosen=prefs.getInt("activityShare",0);share.setSelection(chosen==100?2:chosen==50?1:0);settings.addView(share);
    action(settings,"Auswahl speichern",()->{prefs.edit().putInt("activityShare",share.getSelectedItemPosition()*50).apply();activityPage();});page.addView(settings);
    LinearLayout input=card(Color.WHITE);action(input,"＋ Training hinzufügen",()->addActivityDialog());page.addView(input);
    heading("Bewegungsprotokoll");
    String start=weekStart().toString(),end=weekStart().plusDays(7).toString();int shown=0;
    for(int i=activities.size()-1;i>=0;i--){JSONObject act=activities.get(i);String date=act.optString("date");if(date.compareTo(start)<0||date.compareTo(end)>=0)continue;shown++;
      LinearLayout tile=card(Color.WHITE);tile.addView(label(act.optString("name")+" · +"+act.optInt("points")+" P",16));tile.addView(label(pretty(date)+" · "+act.optInt("minutes")+" Min.",13));page.addView(tile);
      tile.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Aktivität löschen?").setPositiveButton("Löschen",(d,w)->{activities.remove(act);persist("activities",activities);activityPage();}).setNegativeButton("Abbrechen",null).show());
    }
    if(shown==0)page.addView(label("Noch keine Aktivitäten diese Woche.",14));
    page.addView(label("Schätzmodell: MET-Intensität über Ruheverbrauch, etwa 65 aktive kcal je Punkt. Kein offizieller WW-Rechner.",12));
  }
  private void addActivityDialog(){
    LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(20),dp(5),dp(20),dp(5));
    Spinner kind=new Spinner(this);String[] types={"Gehen","Radfahren","Schwimmen","Laufen","Krafttraining","Yoga","Tanzen","Gartenarbeit","Andere Aktivität"};
    kind.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,types));box.addView(label("Aktivitätsart",14));box.addView(kind);
    Spinner intensity=new Spinner(this);intensity.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Niedrig","Moderat","Hoch"}));box.addView(label("Intensität",14));box.addView(intensity);
    EditText minutes=edit("Minuten","30",2),kg=edit("Gewicht in kg",prefs.getString("activityWeight","75"),8194);
    box.addView(label("Dauer in Minuten",14));box.addView(minutes);box.addView(label("Körpergewicht",14));box.addView(kg);
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Training erfassen").setView(box).setNegativeButton("Abbrechen",null).setPositiveButton("Berechnen",null).create();
    dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(w->{
      try{int time=Integer.parseInt(minutes.getText().toString());double weight=num(kg.getText().toString());if(time<1||time>600||weight<25||weight>400)throw new IllegalArgumentException();
        double[] mets={2.5,4.5,7};int level=intensity.getSelectedItemPosition();int earned=estimateActivity(weight,time,mets[level]);
        dialog.dismiss();new AlertDialog.Builder(this).setTitle("Aktivität speichern").setMessage(types[kind.getSelectedItemPosition()]+" · "+time+" Minuten\\nGeschätzte Punkte: "+earned+"\\nAktive Bewegung, keine offiziellen FitPoints.")
          .setPositiveButton("Gutschreiben",(d,which)->{try{JSONObject a=new JSONObject();a.put("date",today());a.put("name",types[kind.getSelectedItemPosition()]);a.put("minutes",time);a.put("intensity",level);a.put("weight",weight);a.put("points",earned);activities.add(a);persist("activities",activities);prefs.edit().putString("activityWeight",String.valueOf(weight)).apply();activityPage();}catch(Exception e){msg("Konnte Aktivität nicht speichern.");}})
          .setNegativeButton("Abbrechen",null).show();
      }catch(Exception e){msg("Bitte gültiges Gewicht und Minuten angeben.");}
    }));dialog.show();
  }
  private void startExport(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"FitRocket-Backup-"+today()+".json");startActivityForResult(i,EXPORT_REQUEST);}
  private void startImport(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");startActivityForResult(i,IMPORT_REQUEST);}
  @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();if(request==EXPORT_REQUEST){try(OutputStream os=getContentResolver().openOutputStream(uri)){JSONObject root=new JSONObject();root.put("schemaVersion",1);root.put("budget",budget);root.put("weekly",weekly);root.put("meals",new JSONArray(prefs.getString("meals","[]")));root.put("weights",new JSONArray(prefs.getString("weights","[]")));root.put("activities",new JSONArray(prefs.getString("activities","[]")));os.write(root.toString(2).getBytes(StandardCharsets.UTF_8));msg("Backup gespeichert.");}catch(Exception e){msg("Backup fehlgeschlagen.");}}else if(request==IMPORT_REQUEST){try(InputStream in=getContentResolver().openInputStream(uri)){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>5_000_000)throw new IOException("Datei zu groß");}JSONObject root=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));if(root.optInt("schemaVersion")!=1||!root.has("meals")||!root.has("weights"))throw new Exception();JSONArray ma=root.getJSONArray("meals"),we=root.getJSONArray("weights"),ac=root.optJSONArray("activities");if(ac==null)ac=new JSONArray();final JSONArray importedActivities=ac;if(ma.length()>50000||we.length()>50000||ac.length()>50000)throw new Exception();int bb=root.optInt("budget",23),ww=root.optInt("weekly",0);if(bb<1||bb>200||ww<0||ww>500)throw new Exception();new AlertDialog.Builder(this).setTitle("Backup wirklich importieren?").setMessage("Vorhandene Mahlzeiten und Gewichtseinträge werden ersetzt. "+ma.length()+" Mahlzeiten im Backup.").setPositiveButton("Importieren",(d,w)->{prefs.edit().putString("meals",ma.toString()).putString("weights",we.toString()).putString("activities",importedActivities.toString()).putInt("budget",bb).putInt("weekly",ww).apply();budget=bb;weekly=ww;readArray("meals",meals);readArray("weights",weights);readArray("activities",activities);home();msg("Backup importiert.");}).setNegativeButton("Abbrechen",null).show();}catch(Exception e){msg("Import fehlgeschlagen: Datei ungültig.");}}}
  private class GlassView extends View {
    final Paint p=new Paint(3);GlassView(){super(MainActivity.this);}
    @Override protected void onDraw(Canvas c){
      super.onDraw(c);float w=getWidth(),h=getHeight(),cx=w/2,cy=h*.75f;
      int n=used(today());float ratio=n/(float)Math.max(1,budget);
      float rad=Math.min(w*.34f,h*.62f);RectF arc=new RectF(cx-rad,cy-rad,cx+rad,cy+rad);
      p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(dp(19));
      p.setColor(Color.rgb(250,235,231));c.drawArc(arc,190,160,false,p);
      p.setShader(new SweepGradient(cx,cy,new int[]{Color.rgb(119,211,154),Color.rgb(173,222,98),Color.rgb(255,184,122),Color.rgb(247,115,119),Color.rgb(119,211,154)},new float[]{0f,.34f,.62f,.84f,1f}));
      c.drawArc(arc,190,160*Math.min(1f,Math.max(0f,ratio)),false,p);p.setShader(null);p.setStyle(Paint.Style.FILL);
      p.setColor(NAVY);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(dp(28));
      c.drawText(n+" / "+budget+" P",cx,cy-dp(25),p);
      p.setTypeface(Typeface.DEFAULT);p.setTextSize(dp(13));p.setColor(Color.rgb(115,116,145));c.drawText("Heute genutzt",cx,cy-dp(4),p);
      if(ratio>1){p.setTextSize(dp(12));p.setColor(Color.rgb(205,91,91));c.drawText("Etwas über deinem Ziel",cx,cy+dp(18),p);}
    }
  }
}
