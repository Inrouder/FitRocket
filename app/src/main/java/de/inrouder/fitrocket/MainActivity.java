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
  private static final int EXPORT_REQUEST=81,IMPORT_REQUEST=82,HEALTH_PERMISSION_REQUEST=83;
  private final ArrayList<JSONObject> foods=new ArrayList<>(), recipes=new ArrayList<>(), meals=new ArrayList<>(), weights=new ArrayList<>(), activities=new ArrayList<>();
  private android.content.SharedPreferences prefs;
  private LinearLayout page, entries;
  private GlassView glass;
  private TextView summary;
  private int budget=23, weekly=FitRocketWeeklyWallet.DEFAULT_WEEKLY_POINTS;
  private boolean healthBusy=false;
  private String currentScreen="FitRocket";
  private String lastRenderedDate=null;
  private LocalDate diaryDate=LocalDate.now();
  private String bookingDate=null,bookingGroup=null,bookingReturn=null;
  private String recipeFilter="Alle";
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
  @Override public void onCreate(Bundle saved){super.onCreate(saved);prefs=getSharedPreferences("fitrocket",MODE_PRIVATE);
    budget=prefs.getInt("budget",23);
    if(!prefs.getBoolean("weekly28MigrationDone",false)){
      // One-time move to the new FitRocket weekly start allowance.
      prefs.edit().putInt("weekly",FitRocketWeeklyWallet.DEFAULT_WEEKLY_POINTS)
        .putBoolean("weekly28MigrationDone",true).apply();
    }
    weekly=prefs.getInt("weekly",FitRocketWeeklyWallet.DEFAULT_WEEKLY_POINTS);
    loadFoods();loadScannedFoods();loadRecipes();readArray("meals",meals);readArray("weights",weights);readArray("activities",activities);home();}
  private void readArray(String key,ArrayList<JSONObject> into){into.clear();try{JSONArray arr=new JSONArray(prefs.getString(key,"[]"));for(int i=0;i<arr.length();i++)into.add(arr.getJSONObject(i));}catch(Exception ignored){}}
  private void persist(String key,ArrayList<JSONObject> items){JSONArray a=new JSONArray();for(JSONObject x:items)a.put(x);prefs.edit().putString(key,a.toString()).apply();}
  private void loadFoods(){try(InputStream in=getAssets().open("foods.json")){ByteArrayOutputStream o=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)o.write(b,0,n);JSONArray a=new JSONArray(new String(o.toByteArray(),StandardCharsets.UTF_8));for(int i=0;i<a.length();i++)foods.add(a.getJSONObject(i));}catch(Exception e){msg("Lebensmitteldatenbank konnte nicht geladen werden.");}}
  private void loadRecipes(){
    recipes.clear();
    try(InputStream in=getAssets().open("recipes.json")){
      ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int count;
      while((count=in.read(buffer))!=-1){out.write(buffer,0,count);if(out.size()>250000)throw new IOException("Rezeptliste zu groß");}
      JSONArray list=new JSONArray(new String(out.toByteArray(),StandardCharsets.UTF_8));
      for(int i=0;i<list.length();i++)recipes.add(list.getJSONObject(i));
    }catch(Exception e){msg("Rezeptvorschläge konnten nicht geladen werden.");}
  }
  private void loadScannedFoods(){
    try{
      JSONArray saved=new JSONArray(prefs.getString("scannedProducts","[]"));
      for(int i=0;i<saved.length();i++){JSONObject f=saved.optJSONObject(i);if(f!=null)foods.add(0,f);}
    }catch(Exception ignored){}
  }
  private void saveScannedFood(JSONObject food){
    try{
      JSONArray original=new JSONArray(prefs.getString("scannedProducts","[]"));
      JSONArray saved=new JSONArray();
      saved.put(food);
      for(int i=0;i<original.length()&&saved.length()<500;i++){
        JSONObject f=original.optJSONObject(i);
        if(f!=null&&!f.optString("id").equals(food.optString("id")))saved.put(f);
      }
      prefs.edit().putString("scannedProducts",saved.toString()).apply();
      foods.removeIf(item->food.optString("id").equals(item.optString("id")));
      foods.add(0,food);
    }catch(Exception e){msg("Gescannter Artikel konnte nicht lokal gespeichert werden.");}
  }
  private void scanBarcode(){
    try{
      com.google.mlkit.vision.codescanner.GmsBarcodeScanning.getClient(this)
        .startScan()
        .addOnSuccessListener(barcode->{
          String code=barcode.getRawValue();
          if(FitRocketProductLookup.validBarcode(code))lookupBarcode(code);
          else msg("Bitte einen Lebensmittelbarcode (EAN/UPC) scannen.");
        })
        .addOnFailureListener(error->
          new AlertDialog.Builder(this).setTitle("Scanner nicht verfügbar")
           .setMessage("Der Kamera-Scanner benötigt Google Play-Dienste. Du kannst die Nummer auf der Verpackung alternativ eingeben.")
           .setPositiveButton("Nummer eingeben",(d,w)->barcodeManualDialog())
           .setNegativeButton("Abbrechen",null).show());
    }catch(Exception e){barcodeManualDialog();}
  }
  private void barcodeManualDialog(){
    EditText field=edit("EAN / UPC (8–14 Ziffern)","",2);
    LinearLayout holder=new LinearLayout(this);holder.setPadding(dp(20),dp(10),dp(20),dp(10));holder.addView(field);
    new AlertDialog.Builder(this).setTitle("Barcode eingeben").setView(holder)
      .setPositiveButton("Produkt suchen",(d,w)->lookupBarcode(field.getText().toString().trim()))
      .setNegativeButton("Abbrechen",null).show();
  }
  private void lookupBarcode(String code){
    if(!FitRocketProductLookup.validBarcode(code)){msg("Bitte einen Barcode mit 8–14 Ziffern eingeben.");return;}
    for(JSONObject food:foods){
      if(code.equals(food.optString("barcode"))){
        new AlertDialog.Builder(this).setTitle("Gespeicherter Artikel")
          .setMessage(food.optString("name")+"\\nMöchtest du ihn direkt auswählen oder online aktualisieren?")
          .setPositiveButton("Auswählen",(d,w)->foodDialog(food))
          .setNeutralButton("Online prüfen",(d,w)->fetchBarcodeOnline(code))
          .setNegativeButton("Abbrechen",null).show();
        return;
      }
    }
    fetchBarcodeOnline(code);
  }
  private void fetchBarcodeOnline(String code){
    AlertDialog loading=new AlertDialog.Builder(this).setTitle("Artikel suchen")
      .setMessage("Lade Nährwerte aus Open Food Facts …").setCancelable(false).create();
    loading.show();
    FitRocketProductLookup.lookup(this,code,new FitRocketProductLookup.Callback(){
      @Override public void onProduct(FitRocketProductLookup.Product product){
        loading.dismiss();
        if(product.complete()){
          try{
            JSONObject food=product.toFood();saveScannedFood(food);
            new AlertDialog.Builder(MainActivity.this).setTitle("Produkt gefunden")
              .setMessage(product.name+(product.brand.isEmpty()?"":" · "+product.brand)
                +"\\n"+String.format(Locale.GERMAN,"%.0f kcal / 100 g",product.kcal)
                +"\\nFitRocket: ca. "+product.points(100)+" P / 100 g"
                +"\\nBitte Nährwertetikett mit den Angaben vergleichen.")
              .setPositiveButton("Portion auswählen",(d,w)->foodDialog(food))
              .setNeutralButton("Nährwerte korrigieren",(d,w)->editBarcodeProduct(product))
              .setNegativeButton("Schließen",null).show();
          }catch(Exception e){msg("Nährwertangaben ungültig.");}
        }else editBarcodeProduct(product);
      }
      @Override public void onError(String error){
        loading.dismiss();
        new AlertDialog.Builder(MainActivity.this).setTitle("Produkt nicht verfügbar")
          .setMessage(error+"\\nDu kannst den Barcode und die Nährwerte selbst hinzufügen.")
          .setPositiveButton("Selbst erfassen",(d,w)->editBarcodeProduct(
              new FitRocketProductLookup.Product(code,"","", "",null,null,null,null)))
          .setNegativeButton("Abbrechen",null).show();
      }
    });
  }
  private void editBarcodeProduct(FitRocketProductLookup.Product data){
    LinearLayout content=new LinearLayout(this);content.setPadding(dp(20),dp(8),dp(20),dp(8));content.setOrientation(1);
    content.addView(label("Produktdaten je 100 g / 100 ml",16));
    EditText name=edit("Produktname",data.name,1);
    content.addView(name);
    EditText[] fields={
      edit("Kilokalorien",data.kcal==null?"":String.valueOf(data.kcal),8194),
      edit("Gesättigte Fettsäuren (g)",data.saturated==null?"":String.valueOf(data.saturated),8194),
      edit("Zucker (g)",data.sugar==null?"":String.valueOf(data.sugar),8194),
      edit("Eiweiß (g)",data.protein==null?"":String.valueOf(data.protein),8194)
    };
    String[] labels={"kcal","Gesättigte Fettsäuren (g)","Zucker (g)","Eiweiß (g)"};
    for(int i=0;i<fields.length;i++){content.addView(label(labels[i],12));content.addView(fields[i]);}
    content.addView(label("Fehlende Nährwerte müssen von der Verpackung ergänzt werden. Sie werden niemals als 0 angenommen.",12));
    ScrollView scrolling=new ScrollView(this);scrolling.addView(content);
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Produkt vervollständigen").setView(scrolling)
        .setNegativeButton("Abbrechen",null).setPositiveButton("Speichern",null).create();
    dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(btn->{
      try{
        String n=name.getText().toString().trim();
        if(n.length()<2||n.length()>140)throw new IllegalArgumentException();
        double k=num(fields[0].getText().toString()),sat=num(fields[1].getText().toString()),
          su=num(fields[2].getText().toString()),pro=num(fields[3].getText().toString());
        if(!Double.isFinite(k)||!Double.isFinite(sat)||!Double.isFinite(su)||!Double.isFinite(pro)
            ||k<0||k>2000||sat<0||sat>100||su<0||su>100||pro<0||pro>100)throw new IllegalArgumentException();
        FitRocketProductLookup.Product product=new FitRocketProductLookup.Product(
            data.code,n,data.brand,data.quantity,k,sat,su,pro);
        JSONObject food=product.toFood();
        saveScannedFood(food);dialog.dismiss();foodDialog(food);
      }catch(Exception e){msg("Bitte alle vier Nährwerte und den Produktnamen korrekt eingeben.");}
    }));
    dialog.show();
  }
  private void shell(String heading){
    currentScreen=heading;
    lastRenderedDate=today();
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
      if((heading.equals("FitRocket")&&i==0)||( (heading.equals("Lebensmittel")||heading.equals("Rezepte"))&&i==1)||(heading.equals("Tagebuch")&&i==2)||((heading.equals("Mehr")||heading.equals("Gewicht"))&&i==3)){item.setBackground(shape(Color.rgb(255,230,224),16));}
      nav.addView(item,new LinearLayout.LayoutParams(0,dp(46),1));item.setOnClickListener(v->{switch(index){case 0:home();break;case 1:bookingDate=null;bookingGroup=null;bookingReturn=null;searchScreen();break;case 2:history();break;default:more();}});
    }
    nav.setPadding(dp(12),dp(7),dp(12),dp(40));
    frame.addView(nav);
  }
  private int used(String date){int total=0;for(JSONObject x:meals)if(date.equals(x.optString("date")))total+=x.optInt("points");return total;}
  private int count(String date){int n=0;for(JSONObject x:meals)if(date.equals(x.optString("date")))n++;return n;}
  private String kind(JSONObject f){return f.isNull("points")?"Punkte eingeben":f.optInt("points")+" P";}
  private JSONObject waterDays(){
    try{return new JSONObject(prefs.getString("waterDays","{}"));}
    catch(Exception e){return new JSONObject();}
  }
  private int waterMl(String day){return waterDays().optInt(day,0);}
  private void setWaterMl(String day,int ml){
    try{
      if(ml<0||ml>10000)throw new IllegalArgumentException();
      JSONObject map=waterDays();map.put(day,ml);
      prefs.edit().putString("waterDays",map.toString()).apply();
    }catch(Exception e){msg("Trinkmenge konnte nicht gespeichert werden.");}
  }
  private int waterGoal(){return prefs.getInt("waterGoal",1500);}
  private void waterGoalDialog(LinearLayout target,String day){
    LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(20),dp(10),dp(20),dp(8));
    content.addView(label("Dein frei wählbares Trinkziel pro Tag (ml)",14));
    EditText amount=edit("Tagesziel (ml)",String.valueOf(waterGoal()),2);content.addView(amount);
    new AlertDialog.Builder(this).setTitle("Trinkziel einstellen").setView(content)
      .setPositiveButton("Speichern",(dialog,w)->{
        try{
          int goal=Integer.parseInt(amount.getText().toString().trim());
          if(goal<250||goal>6000||goal%250!=0)throw new Exception();
          prefs.edit().putInt("waterGoal",goal).apply();
          paintWaterCard(target,day);
        }catch(Exception e){msg("Bitte 250 bis 6000 ml in 250-ml-Schritten eingeben.");}
      }).setNegativeButton("Abbrechen",null).show();
  }
  private LinearLayout waterCard(String day){
    LinearLayout block=card(Color.WHITE);
    paintWaterCard(block,day);
    return block;
  }
  private void paintWaterCard(LinearLayout target,String day){
    target.removeAllViews();
    int current=waterMl(day),goal=waterGoal();
    LinearLayout titleRow=new LinearLayout(this);titleRow.setOrientation(0);titleRow.setGravity(Gravity.CENTER_VERTICAL);
    TextView title=label("💧 Wasser-Challenge",18);title.setTypeface(null,1);
    titleRow.addView(title,new LinearLayout.LayoutParams(0,-2,1));
    TextView amount=chip(String.format(Locale.GERMAN,"%.2f / %.2f L",current/1000.0,goal/1000.0),Color.rgb(220,242,255));
    titleRow.addView(amount);target.addView(titleRow);
    target.addView(label("Jedes Glas steht für 250 ml · Antippen zum Ändern",12));
    int glasses=Math.max(6,Math.max(goal,current)/250);
    glasses=Math.min(24,glasses);
    android.widget.GridLayout grid=new android.widget.GridLayout(this);grid.setColumnCount(6);
    for(int i=0;i<glasses;i++){
      final int index=i;
      FitRocketWaterGlass cup=new FitRocketWaterGlass(this,(index+1)*250<=current);
      android.widget.GridLayout.LayoutParams p=new android.widget.GridLayout.LayoutParams();
      p.width=dp(43);p.height=dp(80);p.setMargins(dp(1),dp(1),dp(1),dp(1));
      grid.addView(cup,p);
      cup.setOnClickListener(v->{
        int currentValue=waterMl(day);
        int next=(index+1)*250<=currentValue?index*250:(index+1)*250;
        setWaterMl(day,next);paintWaterCard(target,day);
      });
    }
    target.addView(grid);
    LinearLayout controls=new LinearLayout(this);controls.setOrientation(0);
    Button less=button("− 250 ml"),more=button("＋ 250 ml");
    controls.addView(less,new LinearLayout.LayoutParams(0,dp(46),1));
    controls.addView(more,new LinearLayout.LayoutParams(0,dp(46),1));
    less.setOnClickListener(v->{setWaterMl(day,Math.max(0,waterMl(day)-250));paintWaterCard(target,day);});
    more.setOnClickListener(v->{setWaterMl(day,Math.min(10000,waterMl(day)+250));paintWaterCard(target,day);});
    target.addView(controls);
    TextView configure=label("⚙ Trinkziel anpassen",13);
    configure.setTextColor(BLUE);target.addView(configure);
    configure.setOnClickListener(v->waterGoalDialog(target,day));
  }
  private void home(){
    shell("FitRocket");
    LinearLayout balance=card(Color.WHITE);
    TextView subtitle=label("Heute · "+pretty(today()),18);subtitle.setTypeface(null,1);
    balance.addView(subtitle);
    glass=new GlassView();balance.addView(glass,new LinearLayout.LayoutParams(-1,dp(190)));
    int usedToday=used(today()),remaining=Math.max(0,budget-usedToday);
    LinearLayout chips=new LinearLayout(this);chips.setOrientation(0);
    for(TextView item:new TextView[]{
      chip("🍽 "+usedToday+" P\\ngenutzt",Color.rgb(255,241,229)),
      chip("🌿 "+remaining+" P\\nübrig",MINT),
      chip("★ "+availableWeek()+" P\\nWoche",LAV)}){
      item.setGravity(Gravity.CENTER);
      LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(2),0,dp(2),0);
      chips.addView(item,p);
    }
    balance.addView(chips);page.addView(balance);

    LinearLayout actions=card(Color.rgb(255,249,245));
    TextView quick=label("Schnell hinzufügen",17);quick.setTypeface(null,1);actions.addView(quick);
    LinearLayout row1=new LinearLayout(this);row1.setOrientation(0);
    Button scanner=button("▥ Barcode"),recipes=button("🍲 Rezepte");
    row1.addView(scanner,new LinearLayout.LayoutParams(0,dp(49),1));
    row1.addView(recipes,new LinearLayout.LayoutParams(0,dp(49),1));
    scanner.setOnClickListener(v->scanBarcode());recipes.setOnClickListener(v->recipeScreen());
    actions.addView(row1);
    LinearLayout row2=new LinearLayout(this);row2.setOrientation(0);
    Button find=button("⌕ Lebensmittel"),manual=button("＋ Manuell");
    row2.addView(find,new LinearLayout.LayoutParams(0,dp(49),1));
    row2.addView(manual,new LinearLayout.LayoutParams(0,dp(49),1));
    find.setOnClickListener(v->searchScreen());manual.setOnClickListener(v->manual());
    actions.addView(row2);page.addView(actions);

    heading("Deine Mahlzeiten");
    dayMealGroups(page,today());

    page.addView(waterCard(today()));

    LinearLayout weightCard=card(Color.WHITE);
    TextView title=label("⚖ Gewicht & Verlauf",18);title.setTypeface(null,1);weightCard.addView(title);
    double last=latestWeight(),goal=goalWeight();
    weightCard.addView(label(last>0?String.format(Locale.GERMAN,"Aktuelles Gewicht: %.1f kg",last):
       "Erfasse dein Gewicht und verfolge deinen Verlauf.",15));
    if(goal>0)weightCard.addView(label(String.format(Locale.GERMAN,"Dein Ziel: %.1f kg",goal),13));
    if(!weights.isEmpty())weightCard.addView(new FitRocketWeightChart(this,weights,goal>0?goal:null),
       new LinearLayout.LayoutParams(-1,dp(150)));
    action(weightCard,"Gewicht eintragen / Verlauf öffnen ›",()->weightScreen());
    page.addView(weightCard);

    LinearLayout weekCard=card(LAV);
    weekCard.addView(label("⭐ Wochenpunkte: "+availableWeek()+" P übrig",18));
    weekCard.addView(label(weekly+" P Wochenstart · +"+rolloversWeek()+" Tagesübertrag · +"+
       activityCredit()+" Bewegung · −"+spentWeek()+" verwendet",13));
    page.addView(weekCard);

    LinearLayout movement=card(MINT);movement.addView(label("🚶 Bewegung · "+earnedWeek()+" P diese Woche",16));
    action(movement,"Aktivitäten & Smartwatch ›",()->activityPage());page.addView(movement);
  }
  private void beginGroupBooking(String group,String date){
    bookingDate=date;bookingGroup=group;
    bookingReturn="Tagebuch".equals(currentScreen)?"Tagebuch":"FitRocket";
    searchScreen();
  }
  private void afterMealAdded(){
    String returnTo=bookingReturn;
    bookingDate=null;bookingGroup=null;bookingReturn=null;
    if("Tagebuch".equals(returnTo))history();
    else home();
  }
  private void refreshMealContext(){
    if("Tagebuch".equals(currentScreen))history();else home();
  }
  private void dayMealGroups(LinearLayout target,String date){
    final String[] names={"Frühstück","Mittagessen","Abendessen","Snack","Getränke"};
    final String[] icons={"☀️","🥗","🌙","🍓","🥤"};
    for(int i=0;i<names.length;i++){
      final String group=names[i];
      LinearLayout section=card(Color.WHITE);
      LinearLayout header=new LinearLayout(this);header.setOrientation(0);header.setGravity(Gravity.CENTER_VERTICAL);
      int count=0,points=0;
      for(JSONObject m:meals){
        if(date.equals(m.optString("date"))&&group.equals(m.optString("group"))){
          count++;points+=m.optInt("points");
        }
      }
      TextView name=label(icons[i]+" "+group,17);name.setTypeface(null,1);
      header.addView(name,new LinearLayout.LayoutParams(0,-2,1));
      header.addView(chip(points+" P",count==0?Color.rgb(244,245,250):MINT));
      TextView plus=chip("＋",Color.rgb(255,232,217));plus.setTextSize(18);
      LinearLayout.LayoutParams addP=new LinearLayout.LayoutParams(-2,-2);addP.setMargins(dp(10),0,0,0);
      header.addView(plus,addP);
      plus.setOnClickListener(v->beginGroupBooking(group,date));
      section.addView(header);
      if(count==0)section.addView(label("Noch nichts eingetragen · Tippe auf ＋",13));
      else{
        for(JSONObject m:meals){
          if(date.equals(m.optString("date"))&&group.equals(m.optString("group")))
            mealRow(m,section);
        }
      }
      target.addView(section);
    }
  }
  private void mealRow(JSONObject m,LinearLayout target){
    LinearLayout item=new LinearLayout(this);item.setOrientation(0);item.setGravity(Gravity.CENTER_VERTICAL);
    item.setPadding(dp(9),dp(8),dp(5),dp(8));item.setBackground(shape(Color.rgb(250,250,254),13));
    LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,-2);ip.setMargins(0,dp(5),0,0);
    TextView name=label(m.optString("name"),14);name.setMaxLines(2);name.setEllipsize(android.text.TextUtils.TruncateAt.END);
    item.addView(name,new LinearLayout.LayoutParams(0,-2,1));
    item.addView(chip(m.optInt("points")+" P",m.optInt("points")==0?MINT:Color.rgb(255,232,219)));
    target.addView(item,ip);
    item.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(m.optString("name"))
      .setItems(new String[]{"Punkte ändern","Eintrag löschen"},(d,which)->{
        if(which==0)editMeal(m);
        else new AlertDialog.Builder(this).setMessage("Eintrag wirklich löschen?")
          .setPositiveButton("Löschen",(dd,w)->{
            meals.remove(m);persist("meals",meals);refreshMealContext();
          }).setNegativeButton("Abbrechen",null).show();
      }).show());
  }
  private void editMeal(JSONObject m){EditText e=edit("Punkte",String.valueOf(m.optInt("points")),2);new AlertDialog.Builder(this).setTitle("Punkte anpassen").setView(e).setPositiveButton("Speichern",(d,w)->{try{int n=Integer.parseInt(e.getText().toString());if(n<0||n>2000)throw new Exception();m.put("points",n);m.put("quality","Manuell korrigiert");persist("meals",meals);refreshMealContext();}catch(Exception ignored){msg("Ungültige Punkte.");}}).setNegativeButton("Abbrechen",null).show();}
  private void searchScreen(){
    shell("Lebensmittel");LinearLayout searchCard=card(Color.WHITE);
    TextView title=label("⌕ Lebensmittel suchen",18);title.setTypeface(null,1);searchCard.addView(title);
    EditText search=edit("Suche nach Lebensmitteln…","",1);
    search.setSingleLine(true);search.setPadding(dp(14),dp(12),dp(14),dp(12));search.setBackground(shape(Color.rgb(248,243,253),20));searchCard.addView(search);
    LinearLayout barcodeRow=new LinearLayout(this);barcodeRow.setOrientation(0);
    Button scanner=button("▥ Barcode scannen"),manualCode=button("123 Nummer eingeben");
    barcodeRow.addView(scanner,new LinearLayout.LayoutParams(0,dp(50),1));
    barcodeRow.addView(manualCode,new LinearLayout.LayoutParams(0,dp(50),1));
    scanner.setOnClickListener(v->scanBarcode());manualCode.setOnClickListener(v->barcodeManualDialog());
    searchCard.addView(barcodeRow);
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
        String text="nutrition".equals(f.optString("status"))?"ca. "+f.optInt("estimatedPoints100")+" P / 100 g":(f.isNull("points")?"Punkte wählen":f.optInt("points")+" P");
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
  // FitRocket-only nutritional scoring, independent of any third-party brand.
  private int fitRocketFoodScore(double kcal,double saturated,double sugar,double protein){
    return FitRocketMath.foodPoints(kcal,saturated,sugar,protein);
  }
  private int calcForGrams(JSONObject f,double grams){
    JSONObject n=f.optJSONObject("nutrition");
    if(n==null||grams<=0||grams>10000)throw new IllegalArgumentException();
    double factor=grams/100.0;
    return fitRocketFoodScore(n.optDouble("kcal")*factor,n.optDouble("saturatedFat_g")*factor,n.optDouble("sugar_g")*factor,n.optDouble("protein_g")*factor);
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
        dialog.dismiss();afterMealAdded();
      }catch(Exception ex){msg("Bitte eine gültige Menge angeben.");}
    }));
    dialog.show();
  }
  private void foodDialog(JSONObject f){if("nutrition".equals(f.optString("status"))){nutritionDialog(f);return;}LinearLayout content=new LinearLayout(this);content.setPadding(22,8,22,8);content.setOrientation(1);content.addView(label(f.optString("category")+"\n"+kind(f)+"\nPortion: "+f.optString("portion")+"\n"+f.optString("notes"),14));EditText mult=edit("Portionsfaktor","1",8194),points=edit("Punkte pro Portion",f.isNull("points")?"":String.valueOf(f.optInt("points")),2);content.addView(label("Portionsfaktor: bei 150 g Referenzportion z. B. 0,5 für 75 g",13));content.addView(mult);content.addView(points);Spinner group=groupSpinner();content.addView(group);AlertDialog d=new AlertDialog.Builder(this).setTitle(f.optString("name")).setView(content).setNegativeButton("Abbrechen",null).setPositiveButton("Buchen",null).create();d.setOnShowListener(v->d.getButton(-1).setOnClickListener(btn->{try{double factor=Double.parseDouble(mult.getText().toString().replace(',','.'));int pointsPer=Integer.parseInt(points.getText().toString());if(!Double.isFinite(factor)||factor<=0||factor>100||pointsPer<0||pointsPer>2000)throw new Exception();int total=(int)Math.round(pointsPer*factor);if(total>2000)throw new Exception();addMeal(f.optString("name")+" × "+factor,total,f.optString("id"),kind(f),group.getSelectedItem().toString());d.dismiss();afterMealAdded();}catch(Exception ex){msg("Bitte gültige Portion und Punkte eingeben.");}}));d.show();}
  private Spinner groupSpinner(){
    Spinner s=new Spinner(this);
    String[] groups={"Frühstück","Mittagessen","Abendessen","Snack","Getränke"};
    ArrayAdapter<String> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,groups);
    s.setAdapter(a);
    if(bookingGroup!=null){
      for(int i=0;i<groups.length;i++)if(groups[i].equals(bookingGroup))s.setSelection(i);
    }
    return s;
  }
  private void addMeal(String name,int points,String foodId,String quality,String group){try{JSONObject x=new JSONObject();x.put("name",name);x.put("points",points);x.put("foodId",foodId);x.put("quality",quality);x.put("group",group);x.put("date",bookingDate==null?today():bookingDate);meals.add(x);persist("meals",meals);}catch(Exception e){msg("Speichern fehlgeschlagen.");}}
  private void manual(){LinearLayout l=new LinearLayout(this);l.setPadding(22,4,22,4);l.setOrientation(1);EditText name=edit("Lebensmittel / Gericht","",1),points=edit("Punkte (manuell)",null,2);l.addView(name);l.addView(points);Spinner g=groupSpinner();l.addView(g);AlertDialog d=new AlertDialog.Builder(this).setTitle("Manuell erfassen").setView(l).setNegativeButton("Abbrechen",null).setPositiveButton("Eintragen",null).create();d.setOnShowListener(v->d.getButton(-1).setOnClickListener(btn->{try{String n=name.getText().toString().trim();int p=Integer.parseInt(points.getText().toString());if(n.length()<2||p<0||p>2000)throw new Exception();addMeal(n,p,"","Manuell",g.getSelectedItem().toString());d.dismiss();home();}catch(Exception ex){msg("Bitte Name und gültige Punkte angeben.");}}));d.show();}
  private void textHelper(){EditText input=new EditText(this);input.setMinLines(3);input.setHint("Beispiel: Ich hatte eine Banane und einen Apfel");new AlertDialog.Builder(this).setTitle("Offline-Texterfassung (keine KI)").setMessage("Erkennt eindeutige Lebensmittelnamen aus deiner Datenbank. Mengen und Zutaten müssen vor dem Eintragen geprüft werden.").setView(input).setPositiveButton("Analysieren",(d,w)->interpret(input.getText().toString())).setNegativeButton("Abbrechen",null).show();}
  private void interpret(String source){String q=source.toLowerCase(Locale.ROOT);ArrayList<JSONObject> matches=new ArrayList<>();for(JSONObject f:foods){String name=f.optString("name").toLowerCase(Locale.ROOT);if(name.length()>=5&&q.contains(name))matches.add(f);}if(matches.size()!=1){new AlertDialog.Builder(this).setTitle("Bitte Lebensmittel auswählen").setMessage(matches.isEmpty()?"Keine eindeutige Übereinstimmung gefunden. Bitte suche das Lebensmittel manuell.":"Mehrere Lebensmittel erkannt. Bitte erfasse die Zutaten einzeln über die Suche.").setPositiveButton("Zur Suche",(d,w)->searchScreen()).show();return;}foodDialog(matches.get(0));}
  private void history(){
    shell("Tagebuch");
    heading("Dein Ernährungstagebuch");
    LinearLayout dateCard=card(Color.WHITE);
    LinearLayout dayBar=new LinearLayout(this);dayBar.setOrientation(0);dayBar.setGravity(Gravity.CENTER_VERTICAL);
    Button prev=button("‹"),next=button("›");
    TextView center=label(pretty(diaryDate.toString()),17);center.setTypeface(null,1);
    center.setGravity(Gravity.CENTER);
    dayBar.addView(prev,new LinearLayout.LayoutParams(dp(54),dp(48)));
    dayBar.addView(center,new LinearLayout.LayoutParams(0,dp(48),1));
    dayBar.addView(next,new LinearLayout.LayoutParams(dp(54),dp(48)));
    prev.setOnClickListener(v->{diaryDate=diaryDate.minusDays(1);history();});
    next.setEnabled(diaryDate.isBefore(LocalDate.now()));
    next.setOnClickListener(v->{if(diaryDate.isBefore(LocalDate.now())){diaryDate=diaryDate.plusDays(1);history();}});
    center.setOnClickListener(v->{
      android.app.DatePickerDialog dp=new android.app.DatePickerDialog(this,(picker,y,m,d)->{
         diaryDate=LocalDate.of(y,m+1,d);history();
      },diaryDate.getYear(),diaryDate.getMonthValue()-1,diaryDate.getDayOfMonth());
      dp.getDatePicker().setMaxDate(System.currentTimeMillis());dp.show();
    });
    dateCard.addView(dayBar);
    int consumed=used(diaryDate.toString());
    dateCard.addView(label("🍽 "+consumed+" P gegessen  ·  "+Math.max(0,budget-consumed)+" P Tagesrest",16));
    if(consumed>budget)dateCard.addView(label("Wochenkonto belastet: "+(consumed-budget)+" P",13));
    page.addView(dateCard);
    dayMealGroups(page,diaryDate.toString());
    page.addView(waterCard(diaryDate.toString()));
  }
  private void recipeScreen(){
    shell("Rezepte");
    LinearLayout intro=card(Color.rgb(255,249,239));
    intro.addView(label("🍲 Heute etwas Leckeres kochen?",19));
    intro.addView(label("Unsere FitRocket-Rezeptideen mit Zutatenmengen und ungefähr berechneten Punkten pro Portion.",13));
    page.addView(intro);
    LinearLayout filters=new LinearLayout(this);filters.setOrientation(0);
    String[] tabs={"Alle","Frühstück","Mittagessen","Abendessen","Snacks"};
    for(String tab:tabs){
      TextView item=chip(tab.equals("Frühstück")?"Morgens":tab.equals("Mittagessen")?"Mittags":tab.equals("Abendessen")?"Abends":tab,
             tab.equals(recipeFilter)?MINT:Color.WHITE);
      item.setTextSize(11);item.setGravity(Gravity.CENTER);
      filters.addView(item,new LinearLayout.LayoutParams(0,-2,1));
      item.setOnClickListener(v->{recipeFilter=tab;recipeScreen();});
    }
    page.addView(filters);
    int count=0;
    for(JSONObject recipe:recipes){
      String category=recipe.optString("group");
      if(!"Alle".equals(recipeFilter) && !("Snacks".equals(recipeFilter)?"Snack":recipeFilter).equals(category))continue;
      int points;
      try{points=FitRocketRecipe.pointsPerServing(recipe,foods);}catch(Exception e){continue;}
      LinearLayout row=card(Color.WHITE);
      LinearLayout nameRow=new LinearLayout(this);nameRow.setOrientation(0);nameRow.setGravity(Gravity.CENTER_VERTICAL);
      TextView name=label(recipe.optString("emoji","🍽")+"  "+recipe.optString("name"),17);name.setTypeface(null,1);
      nameRow.addView(name,new LinearLayout.LayoutParams(0,-2,1));
      nameRow.addView(chip("ca. "+points+" P",points==0?MINT:PEACH));
      row.addView(nameRow);
      row.addView(label(category+" · "+recipe.optInt("minutes")+" Min. · "+
          recipe.optInt("servings")+" Portion(en)",13));
      action(row,"Rezept & Zutaten ansehen ›",()->recipeDialog(recipe));
      page.addView(row);count++;
    }
    if(count==0)page.addView(label("Für diese Kategorie sind noch keine Rezeptideen vorhanden.",14));
  }
  private void recipeDialog(JSONObject recipe){
    int points;
    try{points=FitRocketRecipe.pointsPerServing(recipe,foods);}
    catch(Exception e){msg("Für dieses Rezept fehlen Nährwerte.");return;}
    LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(20),dp(8),dp(20),dp(8));
    content.addView(label(recipe.optString("emoji","🍽")+" "+
        recipe.optString("name")+" · ca. "+points+" P pro Portion",18));
    content.addView(label("Zutaten für "+recipe.optInt("servings")+" Portion(en):",15));
    content.addView(label(FitRocketRecipe.ingredients(recipe,foods),14));
    content.addView(label("Zubereitung",16));
    content.addView(label(recipe.optString("instructions"),14));
    content.addView(label("Wie viele Portionen möchtest du eintragen?",13));
    EditText amount=edit("Portionen","1",2);content.addView(amount);
    Spinner meal=groupSpinner();String[] groups={"Frühstück","Mittagessen","Abendessen","Snack","Getränke"};
    for(int i=0;i<groups.length;i++)if(groups[i].equals(recipe.optString("group")))meal.setSelection(i);
    content.addView(label("Mahlzeit",13));content.addView(meal);
    content.addView(label("Die Zutaten und Portionen sind Vorschläge. Ergänzungen wie weitere Soßen, Butter oder Öl separat eintragen.",12));
    ScrollView scroll=new ScrollView(this);scroll.addView(content);
    final int pts=points;
    AlertDialog d=new AlertDialog.Builder(this).setTitle("Rezeptvorschlag").setView(scroll)
      .setPositiveButton("Zum Tagebuch",null).setNegativeButton("Schließen",null).create();
    d.setOnShowListener(v->d.getButton(-1).setOnClickListener(btn->{
      try{
        int count=Integer.parseInt(amount.getText().toString().trim());
        if(count<1||count>10||pts*count>2000)throw new IllegalArgumentException();
        addMeal(recipe.optString("name")+" × "+count+" Portion(en)",pts*count,
            recipe.optString("id"),"FitRocket-Rezept",meal.getSelectedItem().toString());
        d.dismiss();afterMealAdded();
      }catch(Exception e){msg("Bitte eine gültige Anzahl Portionen eingeben.");}
    }));
    d.show();
  }

  private void more(){
    shell("Mehr");
    LinearLayout progress=card(Color.WHITE);progress.addView(label("Deine Fortschritte",18));
    action(progress,"⚖ Gewicht & Verlauf",()->weightScreen());
    action(progress,"🚶 Bewegungs- und Smartwatch-Konto",()->activityPage());
    action(progress,"📈 Statistiken",()->statistics());page.addView(progress);
    LinearLayout ideas=card(Color.WHITE);ideas.addView(label("Ernährung & Inspiration",18));
    action(ideas,"🍲 Rezeptvorschläge",()->recipeScreen());
    action(ideas,"▥ Barcode scannen",()->scanBarcode());
    action(ideas,"💧 Trinkmenge heute",()->waterScreen());page.addView(ideas);
    LinearLayout options=card(Color.WHITE);options.addView(label("Einstellungen & Daten",18));
    action(options,"⚙ Tagesbudget & Wochenpunkte",()->settingsDialog());
    action(options,"⬆ Backup exportieren (JSON)",()->startExport());
    action(options,"⬇ Backup importieren",()->startImport());page.addView(options);
  }
  private void waterScreen(){
    shell("Trinken");heading("Deine Wasserbilanz");
    page.addView(waterCard(today()));
    page.addView(label("Jedes Glas zählt 250 ml. Du bestimmst dein Tagesziel selbst.",13));
  }
  private void settingsDialog(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(25,5,25,5);EditText b=edit("Tagesbudget",String.valueOf(budget),2),w=edit("Wochenstart-Punkte",String.valueOf(weekly),2);
    l.addView(label("Tagesbudget (persönlich festlegen)",14));l.addView(b);
    l.addView(label("Wochenstart-Punkte (Standard 28)",14));l.addView(w);
    l.addView(label("Montags neu verfügbar. Ungenutzte Tagespunkte werden nach Mitternacht automatisch übertragen, maximal 4 pro Tag.",12));new AlertDialog.Builder(this).setTitle("Budget").setView(l).setPositiveButton("Speichern",(d,n)->{try{int bb=Integer.parseInt(b.getText().toString()),ww=Integer.parseInt(w.getText().toString());if(bb<1||bb>200||ww<0||ww>500)throw new Exception();budget=bb;weekly=ww;prefs.edit().putInt("budget",bb).putInt("weekly",ww).apply();more();}catch(Exception e){msg("Ungültige Budgetwerte.");}}).setNegativeButton("Abbrechen",null).show();}
  private void statistics(){shell("Statistiken");LocalDate now=LocalDate.now(),start=now.minusDays(now.getDayOfWeek().getValue()-1);page.addView(label("Diese Woche",20));int weeklyUsed=0;for(int i=0;i<7;i++){LocalDate date=start.plusDays(i);int n=used(date.toString());weeklyUsed+=n;page.addView(label(date.getDayOfWeek().toString().substring(0,2)+" "+pretty(date.toString())+": "+n+" Punkte",15));}page.addView(label("Gesamt: "+weeklyUsed+" Punkte; Ø "+String.format(Locale.GERMAN,"%.1f",weeklyUsed/7.0)+" pro Kalendertag",15));page.addView(label("Dieser Monat",20));LocalDate first=now.withDayOfMonth(1);int total=0,tracked=0,days=now.getDayOfMonth();for(int i=0;i<days;i++){String day=first.plusDays(i).toString();total+=used(day);if(count(day)>0)tracked++;}page.addView(label("Erfasste Punkte: "+total+" · Tage mit Einträgen: "+tracked+" / "+days,16));page.addView(label("Wochenkonto: "+weekly+" Start + "+rolloversWeek()+" Tagesübertrag + "
   +activityCredit()+" Bewegungsbonus − "+spentWeek()+" verbraucht = "+availableWeek()+" Punkte übrig",15));page.addView(label("Ø je dokumentiertem Tag: "+(tracked>0?String.format(Locale.GERMAN,"%.1f",total/(double)tracked):"–"),16));page.addView(label("Achtung: Fehlende Tage gelten nicht als Null-Punkte-Tage.",13));if(!weights.isEmpty()){page.addView(label("Gewichtseinträge: "+weights.size(),16));for(int i=weights.size()-1;i>=Math.max(0,weights.size()-8);i--)page.addView(label(pretty(weights.get(i).optString("date"))+" · "+weights.get(i).optString("kg")+" kg",14));}}
  private double latestWeight(){
    double value=0;String lastDate="";
    for(JSONObject x:weights){
      String day=x.optString("date");
      double kg=x.optDouble("kg",0);
      if(kg>0 && day.compareTo(lastDate)>=0){value=kg;lastDate=day;}
    }
    if(value<=0&&profileReady())return weightKg();
    return value;
  }
  private double earliestWeight(){
    double value=0;String firstDate="9999-12-31";
    for(JSONObject x:weights){
      String day=x.optString("date");double kg=x.optDouble("kg",0);
      if(kg>0&&day.compareTo(firstDate)<0){value=kg;firstDate=day;}
    }
    return value;
  }
  private double goalWeight(){
    try{
      String val=prefs.getString("weightGoal","");
      return val.isEmpty()?0:Double.parseDouble(val);
    }catch(Exception e){return 0;}
  }
  private void goalWeightDialog(){
    LinearLayout content=new LinearLayout(this);content.setPadding(dp(18),dp(10),dp(18),dp(10));content.setOrientation(1);
    content.addView(label("Dein persönliches Wunschgewicht in Kilogramm",14));
    EditText edit=edit("Zielgewicht",goalWeight()>0?String.format(Locale.GERMAN,"%.1f",goalWeight()):"",8194);
    content.addView(edit);
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Gewichtsziel").setView(content)
      .setPositiveButton("Speichern",null).setNegativeButton("Abbrechen",null).create();
    dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(btn->{
      try{
        double kg=num(edit.getText().toString());
        if(!Double.isFinite(kg)||kg<20||kg>300)throw new IllegalArgumentException();
        prefs.edit().putString("weightGoal",String.valueOf(kg)).apply();
        dialog.dismiss();weightScreen();
      }catch(Exception e){msg("Bitte ein Zielgewicht zwischen 20 und 300 kg eingeben.");}
    }));
    dialog.show();
  }
  private void weightScreen(){
    shell("Gewicht");
    LinearLayout summaryCard=card(Color.WHITE);
    TextView title=label("⚖ Dein Gewichtsverlauf",20);title.setTypeface(null,1);
    summaryCard.addView(title);
    LinearLayout metrics=new LinearLayout(this);metrics.setOrientation(0);
    double first=earliestWeight(),latest=latestWeight(),goal=goalWeight();
    String[] values={
      first>0?String.format(Locale.GERMAN,"%.1f kg",first):"–",
      latest>0?String.format(Locale.GERMAN,"%.1f kg",latest):"–",
      goal>0?String.format(Locale.GERMAN,"%.1f kg",goal):"–"
    };
    String[] names={"Start","Aktuell","Ziel"};
    for(int i=0;i<3;i++){
      LinearLayout metric=new LinearLayout(this);metric.setOrientation(1);
      TextView val=label(values[i],17);val.setTypeface(null,1);val.setGravity(Gravity.CENTER);
      TextView caption=label(names[i],12);caption.setGravity(Gravity.CENTER);
      metric.addView(val);metric.addView(caption);metrics.addView(metric,new LinearLayout.LayoutParams(0,-2,1));
    }
    summaryCard.addView(metrics);
    if(first>0&&latest>0)summaryCard.addView(label(
      "Seit Beginn: "+String.format(Locale.GERMAN,"%+.1f kg",latest-first),15));
    action(summaryCard,"＋ Gewicht eintragen",()->weightDialog());
    action(summaryCard,"🎯 Zielgewicht festlegen",()->goalWeightDialog());
    page.addView(summaryCard);
    LinearLayout chart=card(Color.WHITE);
    chart.addView(label("Deine Entwicklung",18));
    chart.addView(new FitRocketWeightChart(this,weights,goal>0?goal:null),
      new LinearLayout.LayoutParams(-1,dp(265)));
    page.addView(chart);
    heading("Letzte Messungen");
    ArrayList<JSONObject> sorted=new ArrayList<>(weights);
    sorted.sort((one,two)->two.optString("date").compareTo(one.optString("date")));
    int n=0;
    for(JSONObject item:sorted){
      LinearLayout line=card(Color.WHITE);
      line.addView(label(pretty(item.optString("date"))+"  ·  "+
          String.format(Locale.GERMAN,"%.1f kg",item.optDouble("kg")),16));
      page.addView(line);
      line.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Messung löschen?")
        .setMessage(pretty(item.optString("date"))+" · "+item.optString("kg")+" kg")
        .setPositiveButton("Löschen",(d,w)->{
          weights.remove(item);persist("weights",weights);weightScreen();
        }).setNegativeButton("Abbrechen",null).show());
      if(++n>=12)break;
    }
    if(sorted.isEmpty())page.addView(label("Noch keine Messungen eingetragen.",14));
  }
  private void weightDialog(){
    LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(20),dp(6),dp(20),dp(8));
    content.addView(label("Wie viel wiegst du heute?",16));
    double initial=latestWeight();
    if(initial<30||initial>250)initial=80;
    TextView preview=label(String.format(Locale.GERMAN,"%.1f kg",initial),22);preview.setGravity(Gravity.CENTER);
    content.addView(preview);
    SeekBar scale=new SeekBar(this);scale.setMax(2200);
    scale.setProgress((int)Math.round(initial*10)-300);
    content.addView(scale);
    TextView range=label("30 kg                                                  250 kg",11);
    range.setGravity(Gravity.CENTER);content.addView(range);
    EditText input=edit("Kilogramm (z. B. 81,4)",String.format(Locale.GERMAN,"%.1f",initial),8194);
    content.addView(input);
    scale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
      public void onProgressChanged(SeekBar bar,int progress,boolean fromUser){
        if(fromUser){
          double value=(progress+300)/10.0;
          preview.setText(String.format(Locale.GERMAN,"%.1f kg",value));
          input.setText(String.format(Locale.GERMAN,"%.1f",value));
        }
      }
      public void onStartTrackingTouch(SeekBar b){}
      public void onStopTrackingTouch(SeekBar b){}
    });
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Gewicht erfassen").setView(content)
      .setPositiveButton("Speichern",null).setNegativeButton("Abbrechen",null).create();
    dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(btn->{
      try{
        double kg=num(input.getText().toString());
        if(!Double.isFinite(kg)||kg<20||kg>400)throw new IllegalArgumentException();
        JSONObject x=new JSONObject();x.put("date",today());x.put("kg",kg);
        // One measurement per day; replace the prior value without multiplying chart markers.
        weights.removeIf(item->today().equals(item.optString("date")));
        weights.add(x);persist("weights",weights);
        if(profileReady())prefs.edit().putString("profile_weight",String.valueOf(kg)).apply();
        dialog.dismiss();weightScreen();
      }catch(Exception e){msg("Bitte ein gültiges Gewicht in Kilogramm eingeben.");}
    }));
    dialog.show();
  }

  private LocalDate weekStart(){LocalDate d=LocalDate.now();return d.minusDays(d.getDayOfWeek().getValue()-1);}
  private boolean profileReady(){
    return prefs.contains("profile_age") && prefs.contains("profile_height")
        && prefs.contains("profile_weight") && prefs.contains("profile_sex");
  }
  private double weightKg(){return Double.parseDouble(prefs.getString("profile_weight","75"));}
  private double restDaily(){return FitRocketMath.restingCalories(
      prefs.getInt("profile_age",35), prefs.getInt("profile_height",170),
      weightKg(),prefs.getInt("profile_sex",2));}
  private void profileDialog(){
    LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(20),dp(8),dp(20),dp(8));
    box.addView(label("Deine Werte für die persönliche Aktivitätsrechnung",16));
    EditText age=edit("Alter",prefs.contains("profile_age")?String.valueOf(prefs.getInt("profile_age",35)):"",2);
    EditText height=edit("Körpergröße (cm)",prefs.contains("profile_height")?String.valueOf(prefs.getInt("profile_height",170)):"",2);
    EditText weight=edit("Körpergewicht (kg)",prefs.getString("profile_weight",prefs.getString("activityWeight","")),8194);
    box.addView(label("Alter",13));box.addView(age);box.addView(label("Größe (cm)",13));box.addView(height);
    box.addView(label("Gewicht (kg)",13));box.addView(weight);
    Spinner type=new Spinner(this);
    type.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
      new String[]{"Weibliches Referenzprofil","Männliches Referenzprofil","Mittelwert / nicht angeben"}));
    type.setSelection(prefs.getInt("profile_sex",2));box.addView(label("Stoffwechsel-Referenzprofil",13));box.addView(type);
    AlertDialog d=new AlertDialog.Builder(this).setTitle("Dein Bewegungsprofil").setView(box).setNegativeButton("Abbrechen",null).setPositiveButton("Speichern",null).create();
    d.setOnShowListener(v->d.getButton(-1).setOnClickListener(btn->{
      try{
        int a=Integer.parseInt(age.getText().toString().trim());
        int h=Integer.parseInt(height.getText().toString().trim());
        double w=num(weight.getText().toString());
        int t=type.getSelectedItemPosition();
        double kcal=FitRocketMath.restingCalories(a,h,w,t);
        prefs.edit().putInt("profile_age",a).putInt("profile_height",h).putString("profile_weight",String.valueOf(w))
            .putInt("profile_sex",t).apply();
        d.dismiss();activityPage();
      }catch(Exception e){msg("Bitte Alter (18–100), Größe (120–230 cm) und Gewicht (30–300 kg) prüfen.");}
    }));d.show();
  }
  private JSONObject stepMap(){
    try{return new JSONObject(prefs.getString("stepDays","{}"));}
    catch(Exception e){return new JSONObject();}
  }
  private JSONObject healthStepMap(){
    try{return new JSONObject(prefs.getString("healthStepDays","{}"));}
    catch(Exception e){return new JSONObject();}
  }
  private int stepsFor(String date){
    JSONObject manual=stepMap().optJSONObject(date);
    int manualCount=manual==null?0:manual.optInt("total");
    if(prefs.getBoolean("healthUse",false) && prefs.getBoolean("healthStepsPreferred",true) && healthPermissionsGranted()){
      JSONObject watch=healthStepMap().optJSONObject(date);
      if(watch!=null && watch.optInt("total")>0)return watch.optInt("total");
    }
    return manualCount;
  }
  private int cadenceFor(String date){JSONObject x=stepMap().optJSONObject(date);return x==null?100:x.optInt("cadence",100);}
  private int workoutStepsFor(String date){
    long total=0;for(JSONObject a:activities){
      if(!date.equals(a.optString("date")))continue;
      if(a.has("workoutSteps"))total+=a.optInt("workoutSteps");
      else if("Gehen".equals(a.optString("name")))total+=(long)a.optInt("minutes")*100;
      else if("Laufen".equals(a.optString("name")))total+=(long)a.optInt("minutes")*150;
    }
    return (int)Math.min(100000,total);
  }
  private double stepKcalFor(String date){
    if(!profileReady())return 0;
    int steps=stepsFor(date);
    if(steps==0)return 0;
    int threshold=prefs.getInt("stepBaseline",3000);
    int eligible=FitRocketMath.extraSteps(steps,threshold,workoutStepsFor(date));
    return FitRocketMath.stepCalories(restDaily(),eligible,cadenceFor(date));
  }
  private int stepPointsFor(String date){
    return FitRocketMath.points(stepKcalFor(date),prefs.getInt("kcalPerPoint",65));
  }
  private int totalStepPointsWeek(){
    int n=0;for(int i=0;i<7;i++)n+=stepPointsFor(weekStart().plusDays(i).toString());return n;
  }
  private int pointsOfActivity(JSONObject a){return a.has("calories")?FitRocketMath.points(a.optDouble("calories"),prefs.getInt("kcalPerPoint",65)):a.optInt("points");}
  private int workoutPointsWeek(){
    int sum=0;String start=weekStart().toString(),end=weekStart().plusDays(7).toString();
    for(JSONObject a:activities){String date=a.optString("date");if(date.compareTo(start)>=0&&date.compareTo(end)<0)sum+=pointsOfActivity(a);}
    return sum;
  }
  private void stepDialog(){
    if(!profileReady()){profileDialog();return;}
    JSONObject x=stepMap().optJSONObject(today());
    LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(20),dp(10),dp(20),dp(10));
    EditText stepField=edit("Schritte gesamt",x==null?"":String.valueOf(x.optInt("total")),2);
    content.addView(label("Schritte heute (Gesamtwert)",14));content.addView(stepField);
    Spinner pace=new Spinner(this);pace.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
        new String[]{"Gemütlich (80 Schritte/Min.)","Normal (100 Schritte/Min.)","Zügig (120 Schritte/Min.)"}));
    int current=x==null?100:x.optInt("cadence",100);pace.setSelection(current==80?0:current==120?2:1);
    content.addView(label("Gehtempo",14));content.addView(pace);
    content.addView(label("FitRocket zählt nur Schritte über dem Grundwert und berücksichtigt Schritte aus eingetragenen Workouts.",12));
    if(prefs.getBoolean("healthUse",false) && prefs.getBoolean("healthStepsPreferred",true)){
      content.addView(label("Hinweis: Zurzeit werden bevorzugt die Uhr-Schritte genutzt. Wechsle im Aktivitätskonto zur manuellen Schrittquelle.",12));
    }
    AlertDialog d=new AlertDialog.Builder(this).setTitle("Schritte aktualisieren").setView(content).setNegativeButton("Abbrechen",null).setPositiveButton("Speichern",null).create();
    d.setOnShowListener(v->d.getButton(-1).setOnClickListener(btn->{
      try{int steps=Integer.parseInt(stepField.getText().toString());if(steps<0||steps>100000)throw new IllegalArgumentException();
        int[] speeds={80,100,120};JSONObject map=stepMap();JSONObject day=new JSONObject();
        day.put("total",steps);day.put("cadence",speeds[pace.getSelectedItemPosition()]);
        map.put(today(),day);prefs.edit().putString("stepDays",map.toString()).apply();
        d.dismiss();activityPage();
      }catch(Exception e){msg("Bitte eine gültige Schrittzahl eingeben.");}
    }));d.show();
  }

  private int earnedWeek(){return workoutPointsWeek()+totalStepPointsWeek();}
  private int spentWeek(){
    return FitRocketWeeklyWallet.spentWeek(LocalDate.now(),budget,
        date->used(date.toString()));
  }
  private int rolloversWeek(){
    return FitRocketWeeklyWallet.rolloversWeek(LocalDate.now(),budget,
        date->used(date.toString()));
  }
  private int activityCredit(){return FitRocketMath.credit(earnedWeek(),prefs.getInt("activityShare",0));}
  private int availableWeek(){
    return FitRocketWeeklyWallet.remaining(weekly,rolloversWeek(),activityCredit(),spentWeek());
  }

  private void activityPage(){
    shell("Aktivität");heading("Dein Aktivitätskonto");
    LinearLayout cardProfile=card(Color.WHITE);
    if(profileReady()){
      cardProfile.addView(label("Dein Profil · "+prefs.getInt("profile_age",0)+" J. · "+
        prefs.getInt("profile_height",0)+" cm · "+weightKg()+" kg",16));
      cardProfile.addView(label("Geschätzter Ruhebedarf: "+(int)Math.round(restDaily())+" kcal/Tag",13));
    }else{
      cardProfile.addView(label("Richte dein Bewegungsprofil für persönliche Punkte ein.",15));
    }
    action(cardProfile,profileReady()?"Profil ändern":"Profil einrichten",()->profileDialog());page.addView(cardProfile);
    LinearLayout overview=card(Color.WHITE);
    overview.addView(label("Diese Woche: "+earnedWeek()+" Bewegungspunkte",20));
    overview.addView(label("Training: "+workoutPointsWeek()+" · Schritte: "+totalStepPointsWeek(),15));
    overview.addView(label("Zum Wochenbudget: +"+activityCredit()+" Punkte",16));
    overview.addView(label("Wochenkonto: "+weekly+" Start + "+rolloversWeek()+" Übertrag + "
        +activityCredit()+" Bewegung − "+spentWeek()+" verbraucht = "+availableWeek()+" übrig",14));
    overview.addView(label("Täglich höchstens 4 ungenutzte Punkte, Gutschrift erst am Folgetag. Jeden Montag beginnt das Konto neu.",12));
    overview.addView(label("Tagesbudget bleibt: "+budget+" Punkte",13));page.addView(overview);
    LinearLayout transfers=card(Color.WHITE);
    transfers.addView(label("🌟 Tagesüberträge dieser Woche",17));
    LocalDate startDay=weekStart(),currentDay=LocalDate.now();
    boolean daysFinished=false;
    for(LocalDate day=startDay;day.isBefore(currentDay);day=day.plusDays(1)){
      daysFinished=true;
      int saved=FitRocketWeeklyWallet.dailyTransfer(budget,used(day.toString()));
      transfers.addView(label(pretty(day.toString())+" · +"+saved+" / 4 P",14));
    }
    if(!daysFinished)transfers.addView(label("Der erste Tagesübertrag wird nach Tagesabschluss gutgeschrieben.",13));
    page.addView(transfers);
    LinearLayout stepCard=card(MINT);
    int all=stepsFor(today());int w=workoutStepsFor(today());int eligible=FitRocketMath.extraSteps(all,prefs.getInt("stepBaseline",3000),w);
    stepCard.addView(label("👟 Schritte heute: "+all,18));
    stepCard.addView(label("Für Punkte: "+eligible+" Schritte · "+stepPointsFor(today())+" Punkte",14));
    stepCard.addView(label("Basis "+prefs.getInt("stepBaseline",3000)+" Schritte · Workouts "+w+" Schritte abgezogen",12));
    if(prefs.getBoolean("healthUse",false) && prefs.getBoolean("healthStepsPreferred",true) && healthPermissionsGranted() && healthStepMap().optJSONObject(today())!=null){
      stepCard.addView(label("Schrittquelle: Health Connect (Synchronisation)",12));
    }
    action(stepCard,"Schritte eintragen / bearbeiten",()->stepDialog());page.addView(stepCard);
    healthSection();
    LinearLayout settings=card(Color.WHITE);
    settings.addView(label("Verrechnung deiner Bewegung",17));
    Spinner share=new Spinner(this);share.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
        new String[]{"0 % · nur sammeln","50 % · teilweise verwenden","100 % · alles verwenden"}));
    int chosen=prefs.getInt("activityShare",0);share.setSelection(chosen==100?2:chosen==50?1:0);
    settings.addView(share);
    action(settings,"Auswahl speichern",()->{prefs.edit().putInt("activityShare",share.getSelectedItemPosition()*50).apply();activityPage();});
    action(settings,"Punkte-Regeln anpassen",()->activityRulesDialog());
    page.addView(settings);
    LinearLayout activity=card(Color.WHITE);
    action(activity,"＋ Training hinzufügen",()->addActivityDialog());
    page.addView(activity);
    heading("Deine Workouts diese Woche");
    String start=weekStart().toString(),end=weekStart().plusDays(7).toString();int shown=0;
    for(int i=activities.size()-1;i>=0;i--){
      JSONObject act=activities.get(i);String date=act.optString("date");
      if(date.compareTo(start)<0||date.compareTo(end)>=0)continue;shown++;
      LinearLayout tile=card(Color.WHITE);tile.addView(label(act.optString("name")+" · +"+pointsOfActivity(act)+" P",16));
      tile.addView(label(pretty(date)+" · "+act.optInt("minutes")+" Min. · "+act.optInt("workoutSteps")+" Workout-Schritte"
          +("health_connect".equals(act.optString("source"))?" · ⌚ Health Connect":" · manuell"),13));
      page.addView(tile);
      tile.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Eintrag löschen?").setPositiveButton("Löschen",(d,which)->{
        if("health_connect".equals(act.optString("source"))){
          try{JSONArray hidden=new JSONArray(prefs.getString("healthIgnored","[]"));hidden.put(act.optString("externalId"));
            prefs.edit().putString("healthIgnored",hidden.toString()).apply();}catch(Exception ignored){}
        }
        activities.remove(act);persist("activities",activities);activityPage();}).setNegativeButton("Abbrechen",null).show());
    }
    if(shown==0)page.addView(label("Noch keine Workouts für diese Woche.",14));
  }
  @Override protected void onResume(){
    super.onResume();
    if(prefs!=null && lastRenderedDate!=null && !today().equals(lastRenderedDate)){
      if("FitRocket".equals(currentScreen))home();
      else if("Aktivität".equals(currentScreen))activityPage();
      else if("Statistiken".equals(currentScreen))statistics();
    }
    if(prefs!=null && prefs.getBoolean("healthUse",false) && !healthBusy
         && android.os.Build.VERSION.SDK_INT>=34 && HealthSync.supported(this) && healthPermissionsGranted()){
      long now=System.currentTimeMillis();
      if(now-prefs.getLong("healthLastAttempt",0)>20L*60*1000)syncHealth(false);
    }
  }
  private boolean healthPermissionsGranted(){
    if(android.os.Build.VERSION.SDK_INT<34)return false;
    return checkSelfPermission(android.health.connect.HealthPermissions.READ_STEPS)==android.content.pm.PackageManager.PERMISSION_GRANTED
        && checkSelfPermission(android.health.connect.HealthPermissions.READ_EXERCISE)==android.content.pm.PackageManager.PERMISSION_GRANTED;
  }
  private void healthSection(){
    LinearLayout health=card(LAV);
    health.addView(label("⌚ Smartwatch & Health Connect",18));
    if(android.os.Build.VERSION.SDK_INT<34 || !HealthSync.supported(this)){
      health.addView(label("Der direkte Health-Connect-Abgleich benötigt Android 14 oder neuer. Manuelle Schritte und Training bleiben verfügbar.",13));
      page.addView(health);return;
    }
    boolean enabled=prefs.getBoolean("healthUse",false);
    boolean granted=healthPermissionsGranted();
    health.addView(label(enabled&&granted?"Verbunden · schreibgeschützter Zugriff":"Noch nicht verbunden",14));
    health.addView(label("Schritte und Workouts der laufenden Woche einlesen. "
        +"Deine Uhr muss sie über ihre Fitness-App in Health Connect bereitstellen.",13));
    if(enabled&&granted){
      long when=prefs.getLong("healthLastSuccess",0);
      health.addView(label(when==0?"Noch nicht synchronisiert":"Letzter Abgleich: "+
           java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT,java.text.DateFormat.SHORT).format(new Date(when)),12));
      action(health,healthBusy?"Synchronisiere …":"Jetzt synchronisieren",()->syncHealth(true));
      action(health,prefs.getBoolean("healthStepsPreferred",true)
          ?"Schrittquelle: Uhr → auf manuell umstellen"
          :"Schrittquelle: manuell → auf Uhr umstellen",()->{
          boolean newValue=!prefs.getBoolean("healthStepsPreferred",true);
          prefs.edit().putBoolean("healthStepsPreferred",newValue).apply();
          activityPage();
      });
      action(health,"Berechtigungen verwalten",()->openHealthSettings());
      action(health,"Health Connect trennen",()->new AlertDialog.Builder(this)
          .setTitle("Verbindung trennen?")
          .setMessage("Die eingelesenen Schritte und Trainingseinheiten werden aus FitRocket entfernt. "
              +"Deine manuellen Einträge bleiben erhalten. Die Android-Zugriffsrechte kannst du separat in Health Connect widerrufen.")
          .setNegativeButton("Abbrechen",null)
          .setPositiveButton("Trennen",(d,w)->disconnectHealth()).show());
    }else{
      action(health,"Health Connect verbinden",()->connectHealth());
    }
    page.addView(health);
  }
  private void connectHealth(){
    if(android.os.Build.VERSION.SDK_INT<34 || !HealthSync.supported(this)){msg("Für den direkten Abgleich wird Android 14 oder neuer benötigt.");return;}
    if(!profileReady()){
      new AlertDialog.Builder(this).setTitle("Bewegungsprofil erforderlich")
        .setMessage("Für die Bewegungspunkte benötigt FitRocket zunächst dein Profil. "
            +"Danach kannst du Health Connect verbinden.")
        .setPositiveButton("Profil öffnen",(d,w)->profileDialog())
        .setNegativeButton("Abbrechen",null).show();
      return;
    }
    if(healthPermissionsGranted()){prefs.edit().putBoolean("healthUse",true).apply();syncHealth(true);return;}
    new AlertDialog.Builder(this).setTitle("Fitnessdaten einlesen?")
      .setMessage("FitRocket möchte deine Schritte und Trainingseinheiten aus Health Connect lesen. "
          +"Es werden keine Daten in Health Connect geschrieben oder an FitRocket-Server versendet. "
          +"Du kannst die Verbindung jederzeit trennen. Die Daten werden nur lokal gespeichert.")
      .setPositiveButton("Berechtigungen auswählen",(d,w)->requestPermissions(new String[]{
          android.health.connect.HealthPermissions.READ_STEPS,
          android.health.connect.HealthPermissions.READ_EXERCISE},HEALTH_PERMISSION_REQUEST))
      .setNegativeButton("Abbrechen",null).show();
  }
  @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grantResults){
    super.onRequestPermissionsResult(request,permissions,grantResults);
    if(request!=HEALTH_PERMISSION_REQUEST)return;
    if(healthPermissionsGranted()){
      prefs.edit().putBoolean("healthUse",true).apply();
      syncHealth(true);
    }else{
      msg("Für die gemeinsame Synchronisation werden die Freigaben für Schritte und Training benötigt.");
      activityPage();
    }
  }
  private void openHealthSettings(){
    if(android.os.Build.VERSION.SDK_INT<34)return;
    try{Intent i=new Intent(android.health.connect.HealthConnectManager.ACTION_MANAGE_HEALTH_PERMISSIONS);
      i.putExtra(Intent.EXTRA_PACKAGE_NAME,getPackageName());startActivity(i);
    }catch(Exception e){msg("Öffne Health Connect über die Android-Einstellungen.");}
  }
  private void syncHealth(boolean showResult){
    if(healthBusy || android.os.Build.VERSION.SDK_INT<34 || !HealthSync.supported(this))return;
    if(!healthPermissionsGranted()){
      prefs.edit().putBoolean("healthUse",false).apply();
      if(showResult)msg("Die Health-Connect-Berechtigungen fehlen. Bitte erneut verbinden.");
      if("Aktivität".equals(currentScreen))activityPage();
      return;
    }
    if(!profileReady()){
      if(showResult)msg("Bitte zuerst dein Bewegungsprofil einrichten.");
      return;
    }
    healthBusy=true;
    prefs.edit().putLong("healthLastAttempt",System.currentTimeMillis()).apply();
    HealthSync.readWeek(this,weekStart(),new HealthSync.Callback(){
      @Override public void onSuccess(HealthSync.Result result){
        if(!prefs.getBoolean("healthUse",false)){healthBusy=false;return;}
        try{
          int updated=applyHealthResult(result);
          prefs.edit().putLong("healthLastSuccess",System.currentTimeMillis()).apply();
          if(showResult)msg("Health Connect: 7 Tage Schritte und "+updated+" Trainings übernommen.");
          healthBusy=false;
          if("Aktivität".equals(currentScreen))activityPage();
          else if("FitRocket".equals(currentScreen))home();
        }catch(Exception e){healthBusy=false;if(showResult)msg("Abgleich nicht gespeichert: "+e.getMessage());}
      }
      @Override public void onFailure(String reason){
        healthBusy=false;
        if(showResult)msg("Synchronisation fehlgeschlagen: "+reason);
        if("Aktivität".equals(currentScreen))activityPage();
      }
    });
  }
  private boolean nearDuplicate(JSONObject saved,String date,int type,long startMs,int minutes){
    int oldType=saved.optInt("typeIndex",-1);
    if(oldType<0){
      String title=saved.optString("name");
      if("Gehen".equals(title))oldType=0;
      else if("Radfahren".equals(title))oldType=1;
      else if("Schwimmen".equals(title))oldType=2;
      else if("Laufen".equals(title))oldType=3;
      else if("Krafttraining".equals(title))oldType=4;
      else if("Yoga".equals(title))oldType=5;
      else if("Tanzen".equals(title))oldType=6;
    }
    return FitRocketSyncRules.isDuplicate(saved.optString("date"),date,oldType,type,
        saved.optInt("minutes"),minutes,saved.optLong("startEpochMs"),startMs);
  }
  private int applyHealthResult(HealthSync.Result result) throws JSONException {
    final String start=weekStart().toString(),end=weekStart().plusDays(7).toString();
    // Build the replacement in memory first; never partially modify the journal on sync errors.
    ArrayList<JSONObject> nextActivities=new ArrayList<>();
    for(JSONObject a:activities){
      boolean inWeek=a.optString("date").compareTo(start)>=0&&a.optString("date").compareTo(end)<0;
      if(!inWeek||!"health_connect".equals(a.optString("source")))nextActivities.add(a);
    }
    Set<String> ignored=new HashSet<>();
    JSONArray hidden=new JSONArray(prefs.getString("healthIgnored","[]"));
    for(int i=0;i<hidden.length();i++)ignored.add(hidden.optString(i));
    Set<String> encountered=new HashSet<>();
    int added=0;
    for(HealthSync.Workout w:result.workouts){
      String date=w.date().toString();
      int type=FitRocketHealthType.typeIndex(w.type);
      int minutes=w.minutes();
      if(date.compareTo(start)<0||date.compareTo(end)>=0||ignored.contains(w.id)
          ||!encountered.add(w.id))continue;
      long epoch=w.start.toEpochMilli();
      boolean duplicate=false;
      for(JSONObject a:nextActivities)if(nearDuplicate(a,date,type,epoch,minutes)){duplicate=true;break;}
      if(duplicate)continue;
      double met=FitRocketMath.activityMet(type,1);
      double active=FitRocketMath.activeCalories(restDaily(),met,minutes);
      int steps=type==0?minutes*100:type==3?minutes*150:0;
      JSONObject a=new JSONObject();
      a.put("date",date);a.put("name",FitRocketHealthType.name(type));
      a.put("minutes",minutes);a.put("typeIndex",type);a.put("intensity",1);
      a.put("met",met);a.put("calories",active);
      a.put("points",FitRocketMath.points(active,prefs.getInt("kcalPerPoint",65)));
      a.put("startEpochMs",epoch);a.put("workoutSteps",steps);
      a.put("source","health_connect");a.put("externalId",w.id);a.put("sourceApp",w.source);
      a.put("calcVersion",3);nextActivities.add(a);added++;
    }
    JSONObject stepCopy=healthStepMap();
    for(Map.Entry<String,Integer> e:result.dailySteps.entrySet()){
      JSONObject x=new JSONObject();x.put("total",e.getValue());x.put("cadence",100);
      stepCopy.put(e.getKey(),x);
    }
    // Commit both sync datasets together. Manual step values are never overwritten.
    prefs.edit().putString("healthStepDays",stepCopy.toString())
      .putString("activities",new JSONArray(nextActivities).toString()).apply();
    activities.clear();activities.addAll(nextActivities);
    return added;
  }
  private void disconnectHealth(){
    healthBusy=false;
    ArrayList<JSONObject> retained=new ArrayList<>();
    for(JSONObject x:activities)if(!"health_connect".equals(x.optString("source")))retained.add(x);
    activities.clear();activities.addAll(retained);
    prefs.edit().putBoolean("healthUse",false).remove("healthStepDays")
      .remove("healthIgnored").remove("healthLastAttempt").remove("healthLastSuccess").remove("healthStepsPreferred")
      .putString("activities",new JSONArray(activities).toString()).apply();
    msg("Health-Connect-Daten wurden aus FitRocket entfernt.");
    activityPage();
  }
  private void activityRulesDialog(){
    LinearLayout box=new LinearLayout(this);box.setPadding(dp(18),dp(10),dp(18),dp(10));box.setOrientation(1);
    box.addView(label("Grundschritte pro Tag",14));
    EditText threshold=edit("Grundschritte",String.valueOf(prefs.getInt("stepBaseline",3000)),2);box.addView(threshold);
    box.addView(label("Aktive Kalorien je Bewegungspunkt",14));
    EditText kcal=edit("Kalorien je Punkt",String.valueOf(prefs.getInt("kcalPerPoint",65)),2);box.addView(kcal);
    AlertDialog d=new AlertDialog.Builder(this).setTitle("FitRocket-Punkteregeln").setView(box).setNegativeButton("Abbrechen",null).setPositiveButton("Speichern",null).create();
    d.setOnShowListener(v->d.getButton(-1).setOnClickListener(btn->{
      try{int steps=Integer.parseInt(threshold.getText().toString().trim());
        int value=Integer.parseInt(kcal.getText().toString().trim());
        if(steps<0||steps>20000||value<30||value>150)throw new IllegalArgumentException();
        prefs.edit().putInt("stepBaseline",steps).putInt("kcalPerPoint",value).apply();
        d.dismiss();activityPage();
      }catch(Exception e){msg("Bitte Grundschritte (0–20000) und kcal je Punkt (30–150) prüfen.");}
    }));d.show();
  }
  private void addActivityDialog(){
    if(!profileReady()){new AlertDialog.Builder(this).setTitle("Bewegungsprofil fehlt")
      .setMessage("Bitte zuerst Alter, Größe, Gewicht und Referenzprofil hinterlegen.")
      .setPositiveButton("Profil öffnen",(d,w)->profileDialog()).setNegativeButton("Abbrechen",null).show();return;}
    LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(20),dp(5),dp(20),dp(5));
    String[] types={"Gehen","Radfahren","Schwimmen","Laufen","Krafttraining","Yoga","Tanzen","Gartenarbeit","Andere Aktivität"};
    Spinner kind=new Spinner(this);kind.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,types));
    box.addView(label("Sportart",14));box.addView(kind);
    Spinner intensity=new Spinner(this);intensity.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
        new String[]{"Locker","Mittel","Intensiv"}));box.addView(label("Intensität",14));box.addView(intensity);
    EditText duration=edit("Minuten","30",2);
    box.addView(label("Trainingsdauer (Minuten)",14));box.addView(duration);
    EditText workoutSteps=edit("Workout-Schritte (optional)","",2);
    box.addView(label("Schritte dieser Einheit, falls im Tageszähler enthalten",12));box.addView(workoutSteps);
    AlertDialog d=new AlertDialog.Builder(this).setTitle("Training erfassen").setView(box).setNegativeButton("Abbrechen",null).setPositiveButton("Berechnen",null).create();
    d.setOnShowListener(v->d.getButton(-1).setOnClickListener(btn->{
      try{
        int minutes=Integer.parseInt(duration.getText().toString().trim());
        int sport=kind.getSelectedItemPosition(),level=intensity.getSelectedItemPosition();
        if(minutes<1||minutes>600)throw new IllegalArgumentException();
        double met=FitRocketMath.activityMet(sport,level);
        double kcal=FitRocketMath.activeCalories(restDaily(),met,minutes);
        int points=FitRocketMath.points(kcal,prefs.getInt("kcalPerPoint",65));
        int defaultSteps=(sport==0?minutes*100:sport==3?minutes*150:0);
        int counted=workoutSteps.getText().toString().trim().isEmpty()?defaultSteps:Integer.parseInt(workoutSteps.getText().toString().trim());
        if(counted<0||counted>100000)throw new IllegalArgumentException();
        d.dismiss();
        final int finalSteps=counted;
        new AlertDialog.Builder(this).setTitle("Training speichern")
          .setMessage(types[sport]+" · "+minutes+" Min.\n"+String.format(Locale.GERMAN,"%.0f",kcal)+" aktive kcal\n"+points+" Bewegungspunkte\n"+counted+" Schritte nicht doppelt zählen")
          .setPositiveButton("Speichern",(dialog,which)->{
            try{JSONObject a=new JSONObject();
              a.put("date",today());a.put("name",types[sport]);a.put("typeIndex",sport);
              a.put("startEpochMs",System.currentTimeMillis()-minutes*60000L);
              a.put("minutes",minutes);a.put("intensity",level);a.put("met",met);
              a.put("restingKcal",restDaily());a.put("calories",kcal);a.put("workoutSteps",finalSteps);
              a.put("points",points);a.put("calcVersion",2);
              activities.add(a);persist("activities",activities);activityPage();
            }catch(Exception e){msg("Eintrag konnte nicht gespeichert werden.");}
          }).setNegativeButton("Abbrechen",null).show();
      }catch(Exception e){msg("Bitte Minuten und Schritte prüfen.");}
    }));d.show();
  }
  private void startExport(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"FitRocket-Backup-"+today()+".json");startActivityForResult(i,EXPORT_REQUEST);}
  private void startImport(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");startActivityForResult(i,IMPORT_REQUEST);}
  @Override protected void onActivityResult(int request,int result,Intent data){
    super.onActivityResult(request,result,data);
    if(result!=RESULT_OK||data==null||data.getData()==null)return;
    Uri uri=data.getData();
    if(request==EXPORT_REQUEST){
      try(OutputStream os=getContentResolver().openOutputStream(uri)){
        JSONObject payload=new JSONObject();
        payload.put("schemaVersion",4);payload.put("budget",budget);payload.put("weekly",weekly);
        payload.put("meals",new JSONArray(prefs.getString("meals","[]")));
        payload.put("weights",new JSONArray(prefs.getString("weights","[]")));
        payload.put("activities",new JSONArray(prefs.getString("activities","[]")));
        payload.put("steps",stepMap());
        payload.put("healthSteps",healthStepMap());
        payload.put("healthIgnored",new JSONArray(prefs.getString("healthIgnored","[]")));
        payload.put("healthStepsPreferred",prefs.getBoolean("healthStepsPreferred",true));
        payload.put("waterDays",waterDays());
        payload.put("waterGoal",waterGoal());
        payload.put("scannedProducts",new JSONArray(prefs.getString("scannedProducts","[]")));
        if(goalWeight()>0)payload.put("weightGoal",goalWeight());
        payload.put("activityShare",prefs.getInt("activityShare",0));
        payload.put("stepBaseline",prefs.getInt("stepBaseline",3000));
        payload.put("kcalPerPoint",prefs.getInt("kcalPerPoint",65));
        if(profileReady()){
          JSONObject p=new JSONObject();p.put("age",prefs.getInt("profile_age",35));p.put("height",prefs.getInt("profile_height",170));
          p.put("weight",weightKg());p.put("metabolicProfile",prefs.getInt("profile_sex",2));
          payload.put("profile",p);
        }
        os.write(payload.toString(2).getBytes(StandardCharsets.UTF_8));
        msg("Dein FitRocket-Backup wurde gespeichert.");
      }catch(Exception e){msg("Backup fehlgeschlagen.");}
    }else if(request==IMPORT_REQUEST){
      try(InputStream in=getContentResolver().openInputStream(uri)){
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;
        while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>5000000)throw new IOException("Backup zu groß");}
        JSONObject payload=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));
        int schema=payload.optInt("schemaVersion",0);
        if((schema!=1&&schema!=2&&schema!=3&&schema!=4)||!payload.has("meals")||!payload.has("weights"))throw new IllegalArgumentException();
        JSONArray ma=payload.getJSONArray("meals"),we=payload.getJSONArray("weights");
        JSONArray ac=payload.optJSONArray("activities");if(ac==null)ac=new JSONArray();
        final JSONArray importedActivities=ac;
        if(ma.length()>50000||we.length()>50000||ac.length()>50000)throw new IllegalArgumentException();
        int bb=payload.optInt("budget",23),ww=payload.optInt("weekly",FitRocketWeeklyWallet.DEFAULT_WEEKLY_POINTS);
        if(schema<=3 && ww==0)ww=FitRocketWeeklyWallet.DEFAULT_WEEKLY_POINTS;
        final int importedWeekly=ww;
        int share=payload.optInt("activityShare",0),baseline=payload.optInt("stepBaseline",3000),kpp=payload.optInt("kcalPerPoint",65);
        if(bb<1||bb>200||ww<0||ww>500||!(share==0||share==50||share==100)||
          baseline<0||baseline>20000||kpp<30||kpp>150)throw new IllegalArgumentException();
        JSONObject steps=payload.optJSONObject("steps");if(steps==null)steps=new JSONObject();
        if(steps.length()>2000)throw new IllegalArgumentException();
        JSONObject waterBackup=payload.optJSONObject("waterDays");
        if(waterBackup==null)waterBackup=new JSONObject();
        if(waterBackup.length()>2000)throw new IllegalArgumentException();
        for(java.util.Iterator<String> it=waterBackup.keys();it.hasNext();){
          String day=it.next();int ml=waterBackup.optInt(day,-1);
          if(ml<0||ml>10000||ml%250!=0)throw new IllegalArgumentException();
        }
        final JSONObject importedWater=waterBackup;
        int waterTarget=payload.optInt("waterGoal",1500);
        if(waterTarget<250||waterTarget>6000||waterTarget%250!=0)throw new IllegalArgumentException();
        final int importedWaterGoal=waterTarget;
        JSONArray scanned=payload.optJSONArray("scannedProducts");
        if(scanned==null)scanned=new JSONArray();
        if(scanned.length()>500)throw new IllegalArgumentException();
        final JSONArray importedScanned=scanned;
        double weightTarget=payload.optDouble("weightGoal",0);
        if(!Double.isFinite(weightTarget)||weightTarget<0||weightTarget>400)throw new IllegalArgumentException();
        final double importedWeightGoal=weightTarget;
        final JSONObject importedSteps=steps;
        JSONObject importedHealthSteps=payload.optJSONObject("healthSteps");
        if(importedHealthSteps==null)importedHealthSteps=new JSONObject();
        if(importedHealthSteps.length()>2000)throw new IllegalArgumentException();
        final JSONObject healthSteps=importedHealthSteps;
        JSONArray importedIgnored=payload.optJSONArray("healthIgnored");
        if(importedIgnored==null)importedIgnored=new JSONArray();
        if(importedIgnored.length()>5000)throw new IllegalArgumentException();
        final JSONArray ignoredHealth=importedIgnored;
        JSONObject profile=payload.optJSONObject("profile");
        if(profile!=null){FitRocketMath.restingCalories(profile.getInt("age"),profile.getDouble("height"),
                profile.getDouble("weight"),profile.getInt("metabolicProfile"));}
        final JSONObject importedProfile=profile;
        new AlertDialog.Builder(this).setTitle("Backup importieren?")
          .setMessage("Gespeicherte Mahlzeiten, Gewichte, Aktivitäten und Schrittwerte werden durch das Backup ersetzt.")
          .setPositiveButton("Importieren",(d,w)->{
            try{
              android.content.SharedPreferences.Editor e=prefs.edit();
              e.putString("meals",ma.toString()).putString("weights",we.toString())
                .putString("activities",importedActivities.toString()).putString("stepDays",importedSteps.toString())
                .putString("waterDays",importedWater.toString()).putInt("waterGoal",importedWaterGoal)
                .putString("scannedProducts",importedScanned.toString())
                .putString("healthStepDays",healthSteps.toString())
                .putString("healthIgnored",ignoredHealth.toString())
                .putBoolean("healthStepsPreferred",payload.optBoolean("healthStepsPreferred",true))
                .putBoolean("healthUse",false).remove("healthLastAttempt").remove("healthLastSuccess")
                .putInt("budget",bb).putInt("weekly",importedWeekly).putBoolean("weekly28MigrationDone",true).putInt("activityShare",share)
                .putInt("stepBaseline",baseline).putInt("kcalPerPoint",kpp);
              if(importedProfile!=null){
                e.putInt("profile_age",importedProfile.getInt("age"))
                  .putInt("profile_height",importedProfile.getInt("height"))
                  .putString("profile_weight",String.valueOf(importedProfile.getDouble("weight")))
                  .putInt("profile_sex",importedProfile.getInt("metabolicProfile"));
              }
              if(importedWeightGoal>0)e.putString("weightGoal",String.valueOf(importedWeightGoal));
              else e.remove("weightGoal");
              e.apply();budget=bb;weekly=importedWeekly;
              readArray("meals",meals);readArray("weights",weights);readArray("activities",activities);
              foods.clear();loadFoods();loadScannedFoods();loadRecipes();
              home();msg("Backup importiert.");
            }catch(Exception e){msg("Import konnte nicht abgeschlossen werden.");}
          }).setNegativeButton("Abbrechen",null).show();
      }catch(Exception e){msg("Die Backup-Datei ist ungültig.");}
    }
  }
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
