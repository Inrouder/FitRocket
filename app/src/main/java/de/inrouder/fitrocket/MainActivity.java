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
  private String currentScreen=FitRocketNavigation.START_SCREEN;
  private String lastRenderedDate=null;
  private LocalDate diaryDate=LocalDate.now();
  private String bookingDate=null,bookingGroup=null,bookingReturn=null;
  private String recipeFilter="Alle";
  private final int NAVY=Color.rgb(37,46,76), BLUE=Color.rgb(112,117,225), LIGHT=Color.rgb(255,247,242), PEACH=Color.rgb(255,220,204), MINT=Color.rgb(212,242,216), LAV=Color.rgb(240,231,255);
  private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
  private boolean dark(){return FitRocketTheme.isDark(this);}
  private int ink(){return FitRocketTheme.text(this);}
  private int muted(){return FitRocketTheme.secondary(this);}
  private int surface(){return FitRocketTheme.surface(this);}
  private int tint(int color){return FitRocketTheme.map(this,color);}
  private GradientDrawable shape(int color,int radius){
    GradientDrawable d=new GradientDrawable();
    d.setColor(tint(color));d.setCornerRadius(dp(radius));return d;
  }
  private GradientDrawable gradient(int a,int b,int radius){
    GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
        new int[]{tint(a),tint(b)});
    d.setCornerRadius(dp(radius));return d;
  }
  private LinearLayout card(int color){
    LinearLayout l=new LinearLayout(this);
    l.setOrientation(1);l.setPadding(dp(14),dp(13),dp(14),dp(13));
    l.setBackground(shape(color,24));l.setElevation(dp(6));
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
    lp.setMargins(0,0,0,dp(11));l.setLayoutParams(lp);return l;
  }
  private void heading(String title){
    TextView t=label(title,18);t.setTypeface(null,1);
    t.setPadding(dp(2),dp(13),dp(2),dp(8));page.addView(t);
  }
  private TextView chip(String s,int color){
    TextView t=label(s,12);t.setBackground(shape(color,18));
    t.setPadding(dp(10),dp(7),dp(10),dp(7));return t;
  }
  private void action(LinearLayout parent,String name,Runnable fn){
    TextView item=label(name+"  ›",14);
    item.setMinHeight(dp(43));item.setGravity(Gravity.CENTER_VERTICAL);
    item.setPadding(dp(12),dp(12),dp(9),dp(12));
    item.setBackground(shape(dark()?Color.rgb(35,39,51):Color.rgb(248,247,249),12));
    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
    lp.setMargins(0,dp(3),0,dp(3));parent.addView(item,lp);
    item.setOnClickListener(v->fn.run());
  }
  private void settingsRow(LinearLayout parent,String title,String subtitle,Runnable callback){
    LinearLayout row=new LinearLayout(this);row.setOrientation(0);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(dp(3),dp(9),dp(3),dp(9));
    LinearLayout texts=new LinearLayout(this);texts.setOrientation(1);
    TextView name=label(title,15);name.setTypeface(null,1);
    name.setPadding(dp(4),dp(2),dp(4),dp(2));texts.addView(name);
    if(subtitle!=null && !subtitle.isEmpty()){
      TextView hint=label(subtitle,12);hint.setTextColor(muted());
      hint.setPadding(dp(4),dp(1),dp(4),dp(3));texts.addView(hint);
    }
    row.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
    TextView arrow=label("›",24);arrow.setTextColor(muted());row.addView(arrow);
    parent.addView(row);row.setOnClickListener(v->callback.run());
  }

  private String today(){return LocalDate.now().toString();}
  private String pretty(String date){try{return LocalDate.parse(date).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));}catch(Exception e){return date;}}
  private TextView label(String text,int sp){TextView t=new TextView(this);t.setText(text);t.setTextSize(sp);t.setTextColor(ink());t.setPadding(dp(6),dp(5),dp(6),dp(5));return t;}
  private Button button(String text){
    Button b=new Button(this);b.setText(text);b.setTextSize(13);b.setAllCaps(false);
    b.setTextColor(ink());b.setMinHeight(dp(46));b.setElevation(0);
    b.setPadding(dp(8),dp(7),dp(8),dp(7));
    b.setBackground(shape(dark()?Color.rgb(71,54,59):Color.rgb(250,230,221),13));
    return b;
  }
  private EditText edit(String hint,String value,int input){
    EditText e=new EditText(this);e.setHint(hint);e.setSingleLine(true);e.setInputType(input);
    e.setTextColor(ink());e.setHintTextColor(muted());
    e.setBackgroundTintList(android.content.res.ColorStateList.valueOf(FitRocketTheme.accent(this)));
    if(value!=null)e.setText(value);return e;
  }
  private void msg(String text){Toast.makeText(this,text,Toast.LENGTH_LONG).show();}
  @Override public void onCreate(Bundle saved){
    super.onCreate(saved);
    prefs=getSharedPreferences("fitrocket",MODE_PRIVATE);
    setTheme(dark()?android.R.style.Theme_Material_NoActionBar:android.R.style.Theme_Material_Light_NoActionBar);
    getWindow().setStatusBarColor(FitRocketTheme.background(this));
    getWindow().setNavigationBarColor(FitRocketTheme.background(this));
    getWindow().getDecorView().setSystemUiVisibility(dark()?0:
        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    budget=prefs.getInt("budget",23);
    if(!prefs.getBoolean("weekly28MigrationDone",false)){
      // One-time move to the new FitRocket weekly start allowance.
      prefs.edit().putInt("weekly",FitRocketWeeklyWallet.DEFAULT_WEEKLY_POINTS)
        .putBoolean("weekly28MigrationDone",true).apply();
    }
    weekly=prefs.getInt("weekly",FitRocketWeeklyWallet.DEFAULT_WEEKLY_POINTS);
    // Existing installs: activate immediate activity credit once. The user can still
    // explicitly select 0% or 50% afterwards in activity settings.
    if(!prefs.getBoolean("immediateActivityCreditMigrationDone",false)){
      prefs.edit().putInt("activityShare",100)
        .putBoolean("immediateActivityCreditMigrationDone",true).apply();
    }
    loadFoods();loadScannedFoods();loadAiFoods();loadRecipes();readArray("meals",meals);readArray("weights",weights);readArray("activities",activities);
    FitRocketWaterReminder.schedule(this);
    FitRocketWidget.refresh(this);
    maybeCheckUpdatesOnLaunch();
    if(saved!=null){
      String selectedDate=saved.getString("fitrocketDiaryDate",today());
      try{diaryDate=LocalDate.parse(selectedDate);}
      catch(Exception ignored){diaryDate=LocalDate.now();}
      if(diaryDate.isAfter(LocalDate.now()))diaryDate=LocalDate.now();
    }
    if(saved!=null && "Profil".equals(saved.getString("fitrocketScreen")))profilePage();
    else if(saved!=null && "Fortschritt".equals(saved.getString("fitrocketScreen")))progressPage();
    else history();
  }
  @Override protected void onSaveInstanceState(Bundle out){
    out.putString("fitrocketScreen",currentScreen);
    out.putString("fitrocketDiaryDate",diaryDate.toString());
    super.onSaveInstanceState(out);
  }
  private void readArray(String key,ArrayList<JSONObject> into){into.clear();try{JSONArray arr=new JSONArray(prefs.getString(key,"[]"));for(int i=0;i<arr.length();i++)into.add(arr.getJSONObject(i));}catch(Exception ignored){}}
  private void persist(String key,ArrayList<JSONObject> items){JSONArray a=new JSONArray();for(JSONObject x:items)a.put(x);prefs.edit().putString(key,a.toString()).apply();if("meals".equals(key))FitRocketWidget.refresh(this);}
  private void loadFoods(){try(InputStream in=getAssets().open("foods.json")){ByteArrayOutputStream o=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)o.write(b,0,n);JSONArray a=new JSONArray(new String(o.toByteArray(),StandardCharsets.UTF_8));for(int i=0;i<a.length();i++)foods.add(a.getJSONObject(i));}catch(Exception e){msg("Lebensmitteldatenbank konnte nicht geladen werden.");}}
  private void loadRecipes(){
    recipes.clear();
    try(InputStream in=getAssets().open("recipes.json")){
      ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int count;
      while((count=in.read(buffer))!=-1){out.write(buffer,0,count);if(out.size()>250000)throw new IOException("Rezeptliste zu groß");}
      JSONArray list=new JSONArray(new String(out.toByteArray(),StandardCharsets.UTF_8));
      for(int i=0;i<list.length();i++)recipes.add(list.getJSONObject(i));
      try{
        JSONArray saved=new JSONArray(prefs.getString("savedAiRecipes","[]"));
        for(int i=0;i<saved.length();i++){
          JSONObject recipe=saved.optJSONObject(i);
          if(recipe!=null)recipes.add(0,recipe);
        }
      }catch(Exception ignored){}

    }catch(Exception e){msg("Rezeptvorschläge konnten nicht geladen werden.");}
  }
  private void loadAiFoods(){
    try{
      JSONArray arr=new JSONArray(prefs.getString("aiFoods","[]"));
      for(int i=0;i<arr.length();i++){
        JSONObject x=arr.optJSONObject(i);
        if(x!=null)foods.add(0,x);
      }
    }catch(Exception e){msg("Eigene KI-Lebensmittel konnten nicht geladen werden.");}
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
          .setMessage(food.optString("name")+"\nMöchtest du ihn direkt auswählen oder online aktualisieren?")
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
                +"\n"+String.format(Locale.GERMAN,"%.0f kcal / 100 g",product.kcal)
                +"\nFitRocket: ca. "+product.points(100)+" P / 100 g"
                +"\nBitte Nährwertetikett mit den Angaben vergleichen.")
              .setPositiveButton("Portion auswählen",(d,w)->foodDialog(food))
              .setNeutralButton("Nährwerte korrigieren",(d,w)->editBarcodeProduct(product))
              .setNegativeButton("Schließen",null).show();
          }catch(Exception e){msg("Nährwertangaben ungültig.");}
        }else editBarcodeProduct(product);
      }
      @Override public void onError(String error){
        loading.dismiss();
        new AlertDialog.Builder(MainActivity.this).setTitle("Produkt nicht verfügbar")
          .setMessage(error+"\nDu kannst den Barcode und die Nährwerte selbst hinzufügen.")
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
    GradientDrawable sky=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
      dark()?new int[]{Color.rgb(22,31,58),Color.rgb(44,43,70),Color.rgb(55,51,74)}:
             new int[]{Color.rgb(209,226,251),Color.rgb(245,232,230),Color.rgb(227,235,250)});
    android.graphics.drawable.Drawable rocket=getDrawable(R.drawable.fitrocket_background_rocket);
    rocket.setAlpha(dark()?50:42);
    android.graphics.drawable.LayerDrawable backdrop=new android.graphics.drawable.LayerDrawable(
      new android.graphics.drawable.Drawable[]{sky,rocket});
    backdrop.setLayerGravity(1,Gravity.TOP|Gravity.RIGHT);
    backdrop.setLayerWidth(1,dp(190));backdrop.setLayerHeight(1,dp(230));
    backdrop.setLayerInset(1,0,dp(15),dp(9),0);
    frame.setBackground(backdrop);
    // Android 15+ draws content edge-to-edge. Give the header the top inset
    // and the bottom navigation its own bottom inset (never overlap 3-button
    // controls or the gesture handle). Return unconsumed insets to children.
    if(Build.VERSION.SDK_INT>=30){
      frame.setOnApplyWindowInsetsListener((view,insets)->{
        android.graphics.Insets bars=insets.getInsets(
          WindowInsets.Type.statusBars()|WindowInsets.Type.displayCutout());
        view.setPadding(bars.left,bars.top,bars.right,0);
        return insets;
      });
    }
    setContentView(frame);
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);
    scroll.setVerticalScrollBarEnabled(false);
    page=new LinearLayout(this);page.setOrientation(1);
    page.setPadding(dp(17),dp(12),dp(17),dp(26));
    scroll.addView(page);frame.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

    TextView title=label("Tagebuch".equals(heading)?"":heading,23);
    title.setTypeface(null,1);
    if(!"Tagebuch".equals(heading)){title.setPadding(dp(3),dp(9),dp(3),dp(13));page.addView(title);}

    LinearLayout nav=new LinearLayout(this);nav.setOrientation(0);
    final int navLeft=dp(8),navTop=dp(6),navRight=dp(8),navBottom=dp(6);
    nav.setPadding(navLeft,navTop,navRight,navBottom);
    if(Build.VERSION.SDK_INT>=30){
      nav.setOnApplyWindowInsetsListener((view,insets)->{
        android.graphics.Insets bars=insets.getInsets(
          WindowInsets.Type.navigationBars()|WindowInsets.Type.systemGestures());
        // Some 3-button OEM implementations report no nav inset until after layout.
        int bottom=dp(6);
        view.setPadding(navLeft,navTop,navRight,navBottom+bottom);
        return insets;
      });
    }else{
      // Legacy fitsSystemWindows keeps the OS button area outside the app.
      frame.setFitsSystemWindows(true);
    }
    nav.setBackground(shape(surface(),30));nav.setElevation(dp(16));
    String[] labels=FitRocketNavigation.TABS;
    int active=FitRocketNavigation.selectedTab(heading);
    for(int i=0;i<labels.length;i++){
      final int index=i;
      LinearLayout tab=new LinearLayout(this);tab.setOrientation(1);tab.setGravity(Gravity.CENTER);
      String[] symbols={"🚀","🔎","👨‍🍳","📊","👤"};
      String[] names={"Tagebuch","Suche","Kochbuch","Fortschritt","Profil"};
      TextView icon=label(symbols[i],29);icon.setGravity(Gravity.CENTER);icon.setIncludeFontPadding(false);
      TextView name=label(names[i],10);name.setSingleLine(true);
      name.setGravity(Gravity.CENTER);name.setIncludeFontPadding(false);
      name.setEllipsize(android.text.TextUtils.TruncateAt.END);
      name.setTextColor(new int[]{Color.rgb(236,96,55),Color.rgb(56,112,210),Color.rgb(222,121,45),Color.rgb(23,155,111),Color.rgb(120,84,210)}[i]);
      tab.addView(icon,new LinearLayout.LayoutParams(-1,dp(43)));
      tab.addView(name,new LinearLayout.LayoutParams(-1,dp(28)));
      tab.setPadding(dp(1),dp(6),dp(1),dp(6));
      if(i==active){
        name.setTypeface(null,1);
        tab.setBackground(shape(dark()?Color.rgb(75,58,85):Color.rgb(255,232,214),22));
      }
      nav.addView(tab,new LinearLayout.LayoutParams(0,dp(92),1));
      tab.setOnClickListener(v->{
        bookingDate=null;bookingGroup=null;bookingReturn=null;
        switch(index){
          case 0:diaryDate=LocalDate.now();history();break;
          case 1:searchScreen();break;
          case 2:recipeScreen();break;
          case 3:progressPage();break;
          default:profilePage();
        }
      });
    }
    LinearLayout navDock=new LinearLayout(this);navDock.setClipChildren(false);navDock.setClipToPadding(false);
    navDock.setPadding(dp(10),dp(4),dp(10),dp(14));navDock.addView(nav,new LinearLayout.LayoutParams(-1,-2));
    frame.addView(navDock);
    if(Build.VERSION.SDK_INT>=30){
      frame.requestApplyInsets();
      nav.requestApplyInsets();
    }
  }
  private double used(String date){return FitRocketPointLedger.used(meals,date);}
  private String pt(double points){return FitRocketPointLedger.display(points);}
  private double pts(JSONObject meal){return FitRocketPointLedger.value(meal);}
  private boolean showHints(){return prefs.getBoolean("showHints",false);}
  private void hint(LinearLayout parent,String value){
    if(showHints()){TextView h=label(value,12);h.setTextColor(muted());parent.addView(h);}
  }
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
      FitRocketWidget.refresh(this);
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
          FitRocketWidget.refresh(this);
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
    hint(target,"Jedes Glas entspricht 250 ml. Antippen zum Ändern.");
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
    configure.setTextColor(FitRocketTheme.accent(this));target.addView(configure);
    configure.setOnClickListener(v->waterGoalDialog(target,day));
  }
  private void beginGroupBooking(String group,String date){
    bookingDate=date;bookingGroup=group;
    searchScreen();
  }
  private void afterMealAdded(){
    if(bookingDate!=null){
      try{diaryDate=LocalDate.parse(bookingDate);}catch(Exception ignored){}
    }
    bookingDate=null;bookingGroup=null;bookingReturn=null;
    history();
  }
  private void refreshMealContext(){
    history();
  }
  private String mealGroup(JSONObject meal){
    String raw=meal.optString("group","Snack");
    if(raw==null||raw.isEmpty()||"Mahlzeit".equals(raw)||"Snacks".equals(raw))return "Snack";
    return raw;
  }
  private void dayMealGroups(LinearLayout target,String date){
    final String[] names={"Frühstück","Mittagessen","Abendessen","Snack","Getränke"};
    final String[] icons={"☀️","🥗","🌙","🍓","🥤"};
    for(int i=0;i<names.length;i++){
      final String group=names[i];
      LinearLayout section=card(Color.WHITE);
      LinearLayout header=new LinearLayout(this);header.setOrientation(0);header.setGravity(Gravity.CENTER_VERTICAL);
      int count=0;double points=0;
      for(JSONObject m:meals){
        if(date.equals(m.optString("date"))&&group.equals(mealGroup(m))){
          count++;points+=pts(m);
        }
      }
      TextView name=label(icons[i]+" "+group,17);name.setTypeface(null,1);
      header.addView(name,new LinearLayout.LayoutParams(0,-2,1));
      header.addView(chip(pt(points)+" P",count==0?Color.rgb(244,245,250):MINT));
      TextView plus=chip("＋",Color.rgb(255,232,217));plus.setTextSize(18);
      LinearLayout.LayoutParams addP=new LinearLayout.LayoutParams(-2,-2);addP.setMargins(dp(10),0,0,0);
      header.addView(plus,addP);
      plus.setOnClickListener(v->beginGroupBooking(group,date));
      section.addView(header);
      if(count==0)section.addView(label("Noch nichts eingetragen · Tippe auf ＋",13));
      else{
        for(JSONObject m:meals){
          if(date.equals(m.optString("date"))&&group.equals(mealGroup(m)))
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
    item.addView(chip(pt(pts(m))+" P",pts(m)==0?MINT:Color.rgb(255,232,219)));
    target.addView(item,ip);
    item.setOnClickListener(v->{
      boolean hasComposition=m.optJSONArray("aiIngredients")!=null;
      String[] choices=hasComposition?
          new String[]{"Zutaten & Berechnung ansehen","★ Als Favorit speichern","Punkte ändern","Eintrag löschen"}:
          new String[]{"★ Als Favorit speichern","Punkte ändern","Eintrag löschen"};
      new AlertDialog.Builder(this).setTitle(m.optString("name"))
        .setItems(choices,(d,which)->{
          if(hasComposition && which==0){showAiMealDetails(m);return;}
          int actionIndex=hasComposition?which-1:which;
          if(actionIndex==0){saveFavorite(m);return;}
          if(actionIndex==1)editMeal(m);
          else new AlertDialog.Builder(this).setMessage("Eintrag wirklich löschen?")
            .setPositiveButton("Löschen",(dd,w)->{
              meals.remove(m);persist("meals",meals);refreshMealContext();
            }).setNegativeButton("Abbrechen",null).show();
        }).show();
    });
    // Swipe left to delete one mistaken diary entry. A tap still opens options.
    attachSwipeDelete(item,()->confirmDeleteMeal(m));
  }

  private void confirmDeleteMeal(JSONObject meal){
    new AlertDialog.Builder(this).setTitle("Eintrag löschen?")
      .setMessage("„"+meal.optString("name")+"“ ("+pt(pts(meal))+
        " Punkte) aus dem Tagebuch löschen? Favoriten und die "+
        "Lebensmitteldatenbank bleiben unverändert.")
      .setPositiveButton("Löschen",(d,w)->{
        if(meals.remove(meal)){
          persist("meals",meals);
          refreshMealContext();
          msg("Eintrag gelöscht – Tages- und Wochenpunkte sind aktualisiert.");
        }
      }).setNegativeButton("Abbrechen",null).show();
  }

  private void attachSwipeDelete(View item,Runnable delete){
    final float[] touch=new float[2];
    final float threshold=dp(85);
    item.setOnTouchListener((view,event)->{
      switch(event.getActionMasked()){
        case MotionEvent.ACTION_DOWN:
          touch[0]=event.getRawX();touch[1]=event.getRawY();
          return true;
        case MotionEvent.ACTION_MOVE:
          float dx=event.getRawX()-touch[0];
          if(Math.abs(dx)>dp(18) && Math.abs(dx)>
             Math.abs(event.getRawY()-touch[1])*1.5f){
            view.getParent().requestDisallowInterceptTouchEvent(true);
            view.setTranslationX(Math.max(-dp(110),Math.min(0,dx)));
          }
          return true;
        case MotionEvent.ACTION_UP:
          float distance=event.getRawX()-touch[0];
          view.animate().translationX(0).setDuration(140).start();
          if(distance < -threshold && Math.abs(distance)>
             Math.abs(event.getRawY()-touch[1])*1.5f)delete.run();
          else if(Math.abs(distance)<dp(18)&&
             Math.abs(event.getRawY()-touch[1])<dp(18))view.performClick();
          return true;
        case MotionEvent.ACTION_CANCEL:
          view.animate().translationX(0).setDuration(140).start();
          return true;
      }
      return false;
    });
  }

  private void showAiMealDetails(JSONObject meal){
    JSONArray parts=meal.optJSONArray("aiIngredients");
    if(parts==null){msg("Keine Zutatenliste für diesen Eintrag.");return;}
    LinearLayout box=new LinearLayout(this);box.setOrientation(1);
    box.setPadding(dp(20),dp(8),dp(20),dp(8));
    box.addView(label(meal.optString("name")+" · "+pt(pts(meal))+" P",18));
    box.addView(label("KI-Schätzung · Zutaten und Nährwerte können ungenau sein.",13));
    for(int i=0;i<parts.length();i++){
      JSONObject ingredient=parts.optJSONObject(i);
      if(ingredient==null)continue;
      int ingredientPoints=0;
      try{
        JSONObject existing=FitRocketAiRules.matchCatalog(
          ingredient.optString("food_id"),ingredient.optString("name"),foods);
        if(existing==null||!"basic".equals(existing.optString("status"))){
          JSONObject macros=existing!=null?existing.optJSONObject("nutrition"):null;
          ingredientPoints=FitRocketAiRules.points(ingredient.optDouble("grams"),
            macros!=null?macros.optDouble("kcal"):ingredient.optDouble("kcal_100g"),
            macros!=null?macros.optDouble("saturatedFat_g"):ingredient.optDouble("saturated_fat_100g"),
            macros!=null?macros.optDouble("sugar_g"):ingredient.optDouble("sugar_100g"),
            macros!=null?macros.optDouble("protein_g"):ingredient.optDouble("protein_100g"));
        }
      }catch(Exception ignored){}
      box.addView(label("• "+ingredient.optString("name")+" · "+
        String.format(Locale.GERMAN,"%.0f g",ingredient.optDouble("grams"))+
        " · "+ingredientPoints+" P",14));
    }
    String uncertainty=meal.optString("aiUncertainty","");
    if(!uncertainty.isEmpty())box.addView(label("Unsicherheiten: "+uncertainty,12));
    ScrollView scroll=new ScrollView(this);scroll.addView(box);
    new AlertDialog.Builder(this).setTitle("Zutaten des Gerichts")
      .setView(scroll).setPositiveButton("Schließen",null).show();
  }

  private void editMeal(JSONObject m){EditText e=edit("Punkte",pt(pts(m)),8194);new AlertDialog.Builder(this).setTitle("Punkte anpassen").setView(e).setPositiveButton("Speichern",(d,w)->{try{double n=FitRocketPointLedger.validPoints(num(e.getText().toString()));m.put("points",n);m.put("quality","Manuell korrigiert");persist("meals",meals);refreshMealContext();}catch(Exception ignored){msg("Ungültige Punkte.");}}).setNegativeButton("Abbrechen",null).show();}
  /** Search home: separate text lookup, barcode actions and AI tools into readable cards. */
  private void searchScreen(){
    shell("Lebensmittel");
    LinearLayout searchCard=card(Color.WHITE);
    TextView title=label("🔎 Lebensmittel finden",19);title.setTypeface(null,1);
    searchCard.addView(title);
    searchCard.addView(label("Suche nach Namen, Kategorien oder bekannten Zutaten.",13));
    EditText search=edit("Lebensmittel suchen …","",1);
    search.setSingleLine(true);
    search.setPadding(dp(17),dp(15),dp(17),dp(15));
    GradientDrawable searchField=new GradientDrawable();
    searchField.setColor(Color.WHITE);
    searchField.setCornerRadius(dp(24));
    searchField.setStroke(dp(1),Color.rgb(213,218,228));
    search.setBackground(searchField);search.setBackgroundTintList(null);
    search.setTextColor(Color.rgb(28,34,47));
    search.setHintTextColor(Color.rgb(103,112,129));
    search.setHighlightColor(Color.rgb(211,219,234));
    LinearLayout.LayoutParams searchLp=new LinearLayout.LayoutParams(-1,-2);
    searchLp.setMargins(0,dp(10),0,dp(10));searchCard.addView(search,searchLp);
    page.addView(searchCard);

    LinearLayout barcodeCard=card(dark()?Color.rgb(32,38,54):Color.rgb(242,248,255));
    TextView barcodeTitle=label("▥ Barcode & Produktnummer",17);barcodeTitle.setTypeface(null,1);
    barcodeCard.addView(barcodeTitle);
    barcodeCard.addView(label("Verpackung scannen oder den Barcode von Hand eingeben.",12));
    LinearLayout barcodeActions=new LinearLayout(this);barcodeActions.setOrientation(1);
    Button scanner=button("▥  Barcode scannen");
    Button manualCode=button("123  Nummer eingeben");
    scanner.setBackground(shape(dark()?Color.rgb(70,58,72):Color.rgb(255,230,214),20));
    manualCode.setBackground(shape(dark()?Color.rgb(48,66,90):Color.rgb(221,236,252),20));
    LinearLayout.LayoutParams scannerLp=new LinearLayout.LayoutParams(-1,dp(53));
    scannerLp.setMargins(0,dp(9),0,dp(7));
    barcodeActions.addView(scanner,scannerLp);
    barcodeActions.addView(manualCode,new LinearLayout.LayoutParams(-1,dp(53)));
    scanner.setOnClickListener(v->scanBarcode());
    manualCode.setOnClickListener(v->barcodeManualDialog());
    barcodeCard.addView(barcodeActions);page.addView(barcodeCard);

    LinearLayout aiCard=card(dark()?Color.rgb(38,34,58):Color.rgb(248,241,255));
    TextView aiTitle=label("✨ KI-Küchenhelfer",17);aiTitle.setTypeface(null,1);
    aiCard.addView(aiTitle);
    aiCard.addView(label("Beschreibe ein ganzes Gericht oder ergänze ein einzelnes Lebensmittel.",12));
    if(bookingGroup!=null){
      aiCard.addView(label("🍽 Für "+bookingGroup+" · "+pretty(bookingDate==null?today():bookingDate),13));
    }
    action(aiCard,"🍲 Ganzes Gericht berechnen",()->aiCompose("meal"));
    action(aiCard,"🍎 Lebensmittel mit KI hinzufügen",()->aiCompose("food"));
    page.addView(aiCard);

    LinearLayout resultsHeading=card(dark()?Color.rgb(32,38,54):Color.rgb(246,249,255));
    TextView resultTitle=label("🥗 Deine Lebensmittelauswahl",18);resultTitle.setTypeface(null,1);
    resultsHeading.addView(resultTitle);
    resultsHeading.addView(label("Passende Lebensmittel erscheinen hier während der Suche.",12));
    page.addView(resultsHeading);
    LinearLayout results=new LinearLayout(this);results.setOrientation(1);page.addView(results);
    Runnable filter=()->{results.removeAllViews();String q=search.getText().toString().toLowerCase(Locale.ROOT).trim();
      if(q.length()<2){LinearLayout hint=card(Color.WHITE);hint.addView(label("Tippe oben mindestens zwei Buchstaben ein, um Lebensmittel zu finden.",15));results.addView(hint);return;}
      int count=0;for(JSONObject f:foods){String value=(f.optString("name")+" "+f.optString("alias")+" "+f.optString("category")).toLowerCase(Locale.ROOT);
        if(!value.contains(q))continue;count++;
        LinearLayout item=card(Color.WHITE);
        LinearLayout top=new LinearLayout(this);top.setOrientation(LinearLayout.HORIZONTAL);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView name=label(f.optString("name"),16);name.setTypeface(null,1);top.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        String text=FitRocketFoodDisplay.pointsLabel(f);
        top.addView(chip(text, f.optInt("points")==0&&!f.isNull("points")?MINT:Color.rgb(255,227,208)));
        item.addView(top);item.addView(label(f.optString("category")+" · "+FitRocketFoodDisplay.referencePortion(f),12));
        results.addView(item);
        item.setOnClickListener(v->foodDialog(f));
        if(isPersonalFood(f)){
          // Only personally created foods are removable. Bundled basic foods are protected.
          attachSwipeDelete(item,()->confirmDeletePersonalFood(f));
          item.setContentDescription(f.optString("name")+
            ". Antippen zum Erfassen. Nach links wischen zum Löschen.");
        }
        if(count>=70)break;
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
  private boolean isPersonalFood(JSONObject food){
    String id=food.optString("id","");
    if(!(id.startsWith("AI")||id.startsWith("OWN")||id.startsWith("SCAN")))
      return false;
    String key=id.startsWith("SCAN")?"scannedProducts":"aiFoods";
    JSONArray stored=safeArray(key);
    for(int i=0;i<stored.length();i++){
      JSONObject x=stored.optJSONObject(i);
      if(x!=null&&id.equals(x.optString("id")))return true;
    }
    return false;
  }

  private void confirmDeletePersonalFood(JSONObject food){
    new AlertDialog.Builder(this).setTitle("Eigenes Lebensmittel löschen?")
      .setMessage("„"+food.optString("name")+"“ aus deiner Lebensmittelliste löschen? "
       +"Bereits gebuchte Tagebucheinträge bleiben erhalten. Bei gespeicherten "
       +"Rezepten kann die Zutatenverknüpfung danach fehlen.")
      .setPositiveButton("Löschen",(dialog,which)->{
        String id=food.optString("id","");
        String key=id.startsWith("SCAN")?"scannedProducts":"aiFoods";
        JSONArray old=safeArray(key),next=new JSONArray();
        for(int i=0;i<old.length();i++){
          JSONObject entry=old.optJSONObject(i);
          if(entry!=null&&!id.equals(entry.optString("id")))next.put(entry);
        }
        if(prefs.edit().putString(key,next.toString()).commit()){
          foods.removeIf(item->id.equals(item.optString("id")));
          searchScreen();
          msg("Eigenes Lebensmittel gelöscht.");
        }else msg("Lebensmittel konnte nicht gelöscht werden.");
      }).setNegativeButton("Abbrechen",null).show();
  }

  private TextWatcher valueWatcher(Runnable onChanged){
    return new TextWatcher(){
      public void beforeTextChanged(CharSequence s,int start,int count,int after){}
      public void onTextChanged(CharSequence s,int start,int before,int count){onChanged.run();}
      public void afterTextChanged(Editable s){}
    };
  }

  /**
   * One dialog for foods with nutrient values (100 g/100 ml reference).
   * Both quick portions and an exact amount use the SAME decimal calculation.
   */
  private void nutritionDialog(JSONObject f) {
    String unit=FitRocketFoodDisplay.unit(f);
    LinearLayout content=new LinearLayout(this);content.setOrientation(1);
    content.setPadding(dp(20),dp(8),dp(20),dp(8));
    content.addView(label(f.optString("name"),18));
    hint(content,"Berechnung anhand der hinterlegten Nährwerte je 100 "+unit+".");
    Spinner mode=new Spinner(this);
    mode.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
        new String[]{"Portionen (je 100 "+unit+")","Menge in "+unit+" (genau)"}));
    content.addView(label("Eingabeart",13));content.addView(mode);
    EditText qty=edit("Menge","1",8194);
    content.addView(qty);
    TextView preview=label("Punkte",19);preview.setTypeface(null,1);
    content.addView(preview);
    Spinner group=groupSpinner();content.addView(group);
    Runnable refresh=()->{
      boolean inGrams=mode.getSelectedItemPosition()==1;
      qty.setHint(inGrams?"Menge in "+unit:"Anzahl Portionen");
      try{
        double amount=FitRocketFavoriteMath.positive(num(qty.getText().toString()));
        double grams=inGrams?amount:amount*100;
        preview.setText(pt(FitRocketPointLedger.food(f,grams))+" P für "+
          FitRocketFoodDisplay.quantityLabel(f,grams));
      }catch(Exception ignored){preview.setText("Bitte eine Menge eingeben.");}
    };
    mode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
      public void onItemSelected(android.widget.AdapterView<?> p,View v,int position,long id){
        qty.setText(position==1?"100":"1");refresh.run();
      }
      public void onNothingSelected(android.widget.AdapterView<?> p){}
    });
    qty.addTextChangedListener(valueWatcher(refresh));
    refresh.run();
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Lebensmittel eintragen")
      .setView(content).setNegativeButton("Abbrechen",null)
      .setPositiveButton("Eintragen",null).create();
    dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(btn->{
      try{
        double amount=FitRocketFavoriteMath.positive(num(qty.getText().toString()));
        double grams=mode.getSelectedItemPosition()==1?amount:amount*100;
        double score=FitRocketPointLedger.food(f,grams);
        addMeal(f.optString("name")+" ("+FitRocketFoodDisplay.quantityLabel(f,grams)+")",
          score,f.optString("id"),"Nährwertschätzung",group.getSelectedItem().toString());
        if(!meals.isEmpty()){
          JSONObject saved=meals.get(meals.size()-1);
          saved.put("baseName",f.optString("name"));
          saved.put("quantity",grams);
          saved.put("quantityUnit",unit);
          saved.put("referenceQuantity",100);
          saved.put("referencePoints",FitRocketPointLedger.food(f,100));
          persist("meals",meals);
        }
        dialog.dismiss();afterMealAdded();
      }catch(Exception e){msg("Bitte gültige Mengen eingeben.");}
    }));
    dialog.show();
  }

  /**
   * Catalog foods with defined points/portion (e.g. 1 handful = 25 g).
   * Use the reference portion for quick entry or enter any grams/ml precisely.
   * For an old product without reference weight, do not invent a conversion.
   */
  private void foodDialog(JSONObject f){
    if("nutrition".equals(f.optString("status"))){nutritionDialog(f);return;}
    LinearLayout content=new LinearLayout(this);content.setOrientation(1);
    content.setPadding(dp(20),dp(8),dp(20),dp(8));
    content.addView(label(f.optString("name"),18));
    final String unit=FitRocketFoodDisplay.unit(f);
    final double reference=FitRocketFoodDisplay.portionAmount(f);
    final int fixed=f.optInt("points",0);
    final boolean isBaseZero="basic".equals(f.optString("status"))&&fixed==0;
    final boolean needsManual=f.isNull("points");
    String portion=f.optString("portion","");
    content.addView(label("Referenz: "+portion,14));
    Spinner mode=new Spinner(this);
    mode.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,
        new String[]{"Portionen (schnell)","Menge in "+unit+" (genau)"}));
    content.addView(label("Eingabeart",13));content.addView(mode);
    EditText amount=edit("Portionen","1",8194);content.addView(amount);
    EditText per=edit("Punkte pro Referenzportion",
       needsManual?"":String.valueOf(fixed),8194);
    if(needsManual){
      content.addView(label("Punkte je Referenzportion eingeben",13));
      content.addView(per);
    }
    EditText portionWeight=edit("Gewicht einer Portion",
        Double.isFinite(reference)?pt(reference):"",8194);
    LinearLayout missingWeight=new LinearLayout(this);missingWeight.setOrientation(1);
    missingWeight.addView(label("Wie viel "+unit+" wiegt eine Referenzportion?",13));
    missingWeight.addView(portionWeight);
    content.addView(missingWeight);
    TextView preview=label("Punkte",19);preview.setTypeface(null,1);
    content.addView(preview);
    Spinner group=groupSpinner();content.addView(group);
    Runnable refresh=()->{
      boolean exact=mode.getSelectedItemPosition()==1;
      missingWeight.setVisibility(exact&&!Double.isFinite(reference)&&!isBaseZero?View.VISIBLE:View.GONE);
      amount.setHint(exact?"Menge in "+unit:"Anzahl Portionen");
      try{
        double quantity=FitRocketFavoriteMath.positive(num(amount.getText().toString()));
        double value=needsManual?FitRocketPointLedger.validPoints(num(per.getText().toString())):fixed;
        double weight=Double.isFinite(reference)?reference:
           (exact&&!isBaseZero?FitRocketFavoriteMath.positive(num(portionWeight.getText().toString())):Double.NaN);
        double points=isBaseZero?0:(exact?FitRocketPointLedger.scaled(value,weight,quantity):
          FitRocketPointLedger.validPoints(value*quantity));
        String desc=exact?FitRocketFoodDisplay.quantityLabel(f,quantity):
          (pt(quantity)+" Portion(en)");
        preview.setText(pt(points)+" P · "+desc);
      }catch(Exception e){preview.setText("Menge oder Referenz prüfen.");}
    };
    mode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
      public void onItemSelected(android.widget.AdapterView<?> parent,View v,int position,long id){
        amount.setText(position==1&&Double.isFinite(reference)?pt(reference):"1");
        refresh.run();
      }
      public void onNothingSelected(android.widget.AdapterView<?> p){}
    });
    amount.addTextChangedListener(valueWatcher(refresh));
    per.addTextChangedListener(valueWatcher(refresh));
    portionWeight.addTextChangedListener(valueWatcher(refresh));
    refresh.run();
    ScrollView scroll=new ScrollView(this);scroll.addView(content);
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Portion wählen")
      .setView(scroll).setNegativeButton("Abbrechen",null)
      .setPositiveButton("Buchen",null).create();
    dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(btn->{
      try{
        boolean exact=mode.getSelectedItemPosition()==1;
        double quantity=FitRocketFavoriteMath.positive(num(amount.getText().toString()));
        double value=needsManual?FitRocketPointLedger.validPoints(num(per.getText().toString())):fixed;
        double weight=Double.isFinite(reference)?reference:
          (exact&&!isBaseZero?FitRocketFavoriteMath.positive(num(portionWeight.getText().toString())):Double.NaN);
        double points=isBaseZero?0:(exact?FitRocketPointLedger.scaled(value,weight,quantity):
          FitRocketPointLedger.validPoints(value*quantity));
        double effectiveWeight=exact?quantity:(Double.isFinite(reference)?reference*quantity:Double.NaN);
        String title=f.optString("name")+(Double.isFinite(effectiveWeight)?
          " ("+FitRocketFoodDisplay.quantityLabel(f,effectiveWeight)+")":
          " ("+pt(quantity)+" Portion(en))");
        addMeal(title,points,f.optString("id"),kind(f),group.getSelectedItem().toString());
        if(!meals.isEmpty()){
          JSONObject saved=meals.get(meals.size()-1);
          saved.put("baseName",f.optString("name"));
          saved.put("referencePoints",value);
          if(Double.isFinite(effectiveWeight)){
            saved.put("quantity",effectiveWeight);
            saved.put("referenceQuantity",weight);
            saved.put("quantityUnit",unit);
          }else{
            saved.put("portionCount",quantity);
          }
          persist("meals",meals);
        }
        dialog.dismiss();afterMealAdded();
      }catch(Exception ex){msg("Bitte Portion, Referenzmenge und Punkte prüfen.");}
    }));
    dialog.show();
  }
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
  private void addMeal(String name,double points,String foodId,String quality,String group){try{JSONObject x=new JSONObject();x.put("name",name);x.put("points",FitRocketPointLedger.validPoints(points));x.put("foodId",foodId);x.put("quality",quality);x.put("group",group);x.put("date",bookingDate==null?today():bookingDate);meals.add(x);persist("meals",meals);}catch(Exception e){msg("Speichern fehlgeschlagen.");}}
  private void manual(){LinearLayout l=new LinearLayout(this);l.setPadding(22,4,22,4);l.setOrientation(1);EditText name=edit("Lebensmittel / Gericht","",1),points=edit("Punkte (manuell, z. B. 0,5)",null,8194);l.addView(name);l.addView(points);Spinner g=groupSpinner();l.addView(g);AlertDialog d=new AlertDialog.Builder(this).setTitle("Manuell erfassen").setView(l).setNegativeButton("Abbrechen",null).setPositiveButton("Eintragen",null).create();d.setOnShowListener(v->d.getButton(-1).setOnClickListener(btn->{try{String n=name.getText().toString().trim();double p=FitRocketPointLedger.validPoints(num(points.getText().toString()));if(n.length()<2)throw new Exception();addMeal(n,p,"","Manuell",g.getSelectedItem().toString());d.dismiss();history();}catch(Exception ex){msg("Bitte Name und gültige Punkte angeben.");}}));d.show();}
  private void textHelper(){EditText input=new EditText(this);input.setMinLines(3);input.setHint("Beispiel: Ich hatte eine Banane und einen Apfel");new AlertDialog.Builder(this).setTitle("Offline-Texterfassung (keine KI)").setMessage("Erkennt eindeutige Lebensmittelnamen aus deiner Datenbank. Mengen und Zutaten müssen vor dem Eintragen geprüft werden.").setView(input).setPositiveButton("Analysieren",(d,w)->interpret(input.getText().toString())).setNegativeButton("Abbrechen",null).show();}
  private void interpret(String source){String q=source.toLowerCase(Locale.ROOT);ArrayList<JSONObject> matches=new ArrayList<>();for(JSONObject f:foods){String name=f.optString("name").toLowerCase(Locale.ROOT);if(name.length()>=5&&q.contains(name))matches.add(f);}if(matches.size()!=1){new AlertDialog.Builder(this).setTitle("Bitte Lebensmittel auswählen").setMessage(matches.isEmpty()?"Keine eindeutige Übereinstimmung gefunden. Bitte suche das Lebensmittel manuell.":"Mehrere Lebensmittel erkannt. Bitte erfasse die Zutaten einzeln über die Suche.").setPositiveButton("Zur Suche",(d,w)->searchScreen()).show();return;}foodDialog(matches.get(0));}
  private void history(){
    shell("Tagebuch");
    final String date=diaryDate.toString();

    // The diary is the single home screen. Its day selector and points card
    // replace the now-removed separate "Heute" page.
    LinearLayout dayPicker=card(Color.WHITE);
    LinearLayout dayBar=new LinearLayout(this);
    dayBar.setOrientation(0);
    dayBar.setGravity(Gravity.CENTER_VERTICAL);
    Button prev=button("‹"),next=button("›");
    TextView center=label(diaryDate.equals(LocalDate.now())
      ?"Heute · "+pretty(date):pretty(date),17);
    center.setTypeface(null,1);center.setGravity(Gravity.CENTER);
    dayBar.addView(prev,new LinearLayout.LayoutParams(dp(48),dp(47)));
    dayBar.addView(center,new LinearLayout.LayoutParams(0,dp(47),1));
    dayBar.addView(next,new LinearLayout.LayoutParams(dp(48),dp(47)));
    prev.setOnClickListener(v->{diaryDate=diaryDate.minusDays(1);history();});
    next.setEnabled(diaryDate.isBefore(LocalDate.now()));
    next.setOnClickListener(v->{
      if(diaryDate.isBefore(LocalDate.now())){diaryDate=diaryDate.plusDays(1);history();}
    });
    center.setOnClickListener(v->{
      android.app.DatePickerDialog picker=new android.app.DatePickerDialog(this,(view,y,m,d)->{
        diaryDate=LocalDate.of(y,m+1,d);history();
      },diaryDate.getYear(),diaryDate.getMonthValue()-1,diaryDate.getDayOfMonth());
      picker.getDatePicker().setMaxDate(System.currentTimeMillis());
      picker.show();
    });
    dayPicker.addView(dayBar);
    if(!diaryDate.equals(LocalDate.now())){
      TextView todayLink=label("Zurück zu heute ›",13);
      todayLink.setTextColor(FitRocketTheme.accent(this));
      todayLink.setGravity(Gravity.CENTER);
      dayPicker.addView(todayLink);
      todayLink.setOnClickListener(v->{diaryDate=LocalDate.now();history();});
    }
    page.addView(dayPicker);

    LinearLayout balance=card(Color.WHITE);
    TextView balanceTitle=label("Deine Punkte",17);
    balanceTitle.setTypeface(null,1);balance.addView(balanceTitle);
    glass=new GlassView(date);
    balance.addView(glass,new LinearLayout.LayoutParams(-1,dp(156)));

    double consumed=used(date);
    double remaining=Math.max(0,budget-consumed);
    boolean isToday=diaryDate.equals(LocalDate.now());
    String[] labels={
      pt(consumed)+" P genutzt",
      pt(remaining)+" P übrig",
      isToday?pt(availableWeek())+" P Woche":budget+" P Ziel"
    };
    int[] colors={Color.rgb(255,241,229),MINT,LAV};
    LinearLayout chips=new LinearLayout(this);chips.setOrientation(0);
    for(int i=0;i<labels.length;i++){
      TextView value=chip(labels[i],colors[i]);
      value.setGravity(Gravity.CENTER);
      value.setTextSize(11);
      value.setMaxLines(2);
      LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,dp(42),1);
      params.setMargins(dp(2),0,dp(2),0);
      chips.addView(value,params);
    }
    balance.addView(chips);
    if(consumed>budget){
      TextView excess=label("Tagesbudget um "+pt(consumed-budget)+" P überschritten",12);
      excess.setTextColor(muted());balance.addView(excess);
    }
    page.addView(balance);

    LinearLayout shortcuts=card(Color.WHITE);
    TextView quick=label("Schnell hinzufügen",15);quick.setTypeface(null,1);
    shortcuts.addView(quick);
    LinearLayout row=new LinearLayout(this);row.setOrientation(0);
    Button food=button("⌕ Lebensmittel"),saved=button("★ Favoriten");
    row.addView(food,new LinearLayout.LayoutParams(0,dp(46),1));
    row.addView(saved,new LinearLayout.LayoutParams(0,dp(46),1));
    food.setOnClickListener(v->searchScreen());
    saved.setOnClickListener(v->favoritesScreen());
    shortcuts.addView(row);
    page.addView(shortcuts);

    heading("Mahlzeiten");
    dayMealGroups(page,date);
    page.addView(waterCard(date));
  }
  private String selectedAiProvider(){
    String p=prefs.getString("aiProvider",FitRocketAiSecrets.GEMINI);
    return FitRocketAiSecrets.OPENAI.equals(p)?FitRocketAiSecrets.OPENAI:FitRocketAiSecrets.GEMINI;
  }

  private String selectedAiProviderName(){
    return FitRocketAiSecrets.GEMINI.equals(selectedAiProvider())?"Google Gemini":"OpenAI";
  }

  private void selectAiProvider(){
    String[] options={"Google Gemini (kostenloses Kontingent)","OpenAI (optional)"};
    int current=FitRocketAiSecrets.GEMINI.equals(selectedAiProvider())?0:1;
    new AlertDialog.Builder(this).setTitle("KI-Anbieter auswählen")
      .setSingleChoiceItems(options,current,(d,index)->{
        prefs.edit().putString("aiProvider",index==0
            ?FitRocketAiSecrets.GEMINI:FitRocketAiSecrets.OPENAI).apply();
        d.dismiss();aiHub();
      }).setNegativeButton("Abbrechen",null).show();
  }

  private void selectGeminiModel(){
    String[] choices={"Gemini 3.5 Flash-Lite · sparsam, Standard",
        "Gemini 3.8 Flash · anspruchsvollere Rezepte"};
    int current=FitRocketGeminiApi.MODEL_FLASH.equals(
        FitRocketGeminiApi.safeModel(
          prefs.getString("geminiModel",FitRocketGeminiApi.DEFAULT_MODEL)))?1:0;
    new AlertDialog.Builder(this).setTitle("Gemini-Modell")
      .setSingleChoiceItems(choices,current,(d,which)->{
        prefs.edit().putString("geminiModel",
          which==1?FitRocketGeminiApi.MODEL_FLASH:FitRocketGeminiApi.MODEL_LITE).apply();
        d.dismiss();aiHub();
      }).setNegativeButton("Abbrechen",null).show();
  }

  private void aiHub(){
    shell("KI-Assistent");
    LinearLayout intro=card(Color.rgb(247,241,255));
    TextView title=label("✨ Dein FitRocket-Küchenassistent",21);title.setTypeface(null,1);intro.addView(title);
    intro.addView(label("Du beschreibst, was du möchtest oder gegessen hast. "
        +"Die KI schätzt Zutaten und Mengen – FitRocket berechnet danach die Punkte selbst.",14));
    intro.addView(label("Vor dem Speichern kannst du alle Zutaten und Nährwerte überprüfen.",13));
    page.addView(intro);

    LinearLayout tasks=card(Color.WHITE);tasks.addView(label("Was möchtest du machen?",17));
    action(tasks,"🍲 Rezept nach meinen Vorlieben",()->aiCompose("recipe"));
    action(tasks,"🍽 Gegessene Mahlzeit einschätzen",()->aiCompose("meal"));
    action(tasks,"🍎 Einzelnes Produkt einschätzen",()->aiCompose("food"));
    page.addView(tasks);

    LinearLayout taste=card(MINT);
    taste.addView(label("🌿 Deine Geschmacksvorlieben",17));
    String prefsText=prefs.getString("aiPreferences","");
    taste.addView(label(prefsText.isEmpty()
        ?"Beispiele: vegetarisch, eiweißreich, ohne Pilze, maximal 20 Minuten, kein Fisch."
        :prefsText,14));
    action(taste,"Vorlieben bearbeiten",()->aiPreferencesDialog());page.addView(taste);

    LinearLayout connection=card(Color.WHITE);
    connection.addView(label("🔐 Verbindung & Datenschutz",17));
    String provider=selectedAiProvider();
    connection.addView(label("Aktiver Anbieter: "+selectedAiProviderName(),16));
    action(connection,"KI-Anbieter wechseln",()->selectAiProvider());
    if(FitRocketAiSecrets.GEMINI.equals(provider)){
      boolean flash=FitRocketGeminiApi.MODEL_FLASH.equals(
        FitRocketGeminiApi.safeModel(
          prefs.getString("geminiModel",FitRocketGeminiApi.DEFAULT_MODEL)));
      connection.addView(label("Modell: "+(flash?"Gemini 3.8 Flash":"Gemini 3.5 Flash-Lite")
        +" · kostenlose Nutzung nur innerhalb der verfügbaren API-Limits",13));
      action(connection,"Gemini-Modell auswählen",()->selectGeminiModel());
      connection.addView(label("Hinweis: Google kann Eingaben der kostenlosen "
        +"API-Stufe zur Verbesserung seiner Produkte verwenden. "
        +"Bitte keine vertraulichen Gesundheitsinformationen eingeben.",12));
    }
    connection.addView(label(FitRocketAiSecrets.configured(this,provider)
        ?"Ein persönlicher "+selectedAiProviderName()+"-API-Schlüssel ist verschlüsselt gespeichert."
        :"Du benötigst noch einen eigenen "+selectedAiProviderName()+"-API-Schlüssel.",13));
    connection.addView(label("Übertragen werden nur die aktuelle Essensbeschreibung, "
        +"freiwillige Vorlieben und öffentliche Katalogeinträge; "
        +"keine Gewichte, Health-Connect-Daten oder Tagebuchhistorie.",12));
    action(connection,selectedAiProviderName()+"-Schlüssel einrichten / entfernen",()->aiKeyDialog());
    action(connection,"Ohne KI: Lebensmittel aus Nährwerten",()->aiManualFood());
    page.addView(connection);
  }

  private void aiPreferencesDialog(){
    LinearLayout content=new LinearLayout(this);content.setOrientation(1);
    content.setPadding(dp(18),dp(10),dp(18),dp(10));
    content.addView(label("Was magst du, was nicht, und wie soll FitRocket Rezepte planen?",14));
    EditText e=new EditText(this);
    e.setHint("z. B. proteinreich, vegetarisch, ohne Zwiebeln, 20 Minuten");
    e.setMinLines(3);e.setMaxLines(7);
    e.setInputType(android.text.InputType.TYPE_CLASS_TEXT|
        android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|
        android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    e.setText(prefs.getString("aiPreferences",""));content.addView(e);
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Geschmacksvorlieben")
      .setView(content).setPositiveButton("Speichern",null).setNegativeButton("Abbrechen",null).create();
    dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(btn->{
      String text=e.getText().toString().trim();
      if(text.length()>500){msg("Bitte maximal 500 Zeichen verwenden.");return;}
      prefs.edit().putString("aiPreferences",text).apply();dialog.dismiss();aiHub();
    }));dialog.show();
  }

  private void aiKeyDialog(){
    String provider=selectedAiProvider();
    boolean gemini=FitRocketAiSecrets.GEMINI.equals(provider);
    LinearLayout content=new LinearLayout(this);content.setOrientation(1);
    content.setPadding(dp(18),dp(10),dp(18),dp(10));
    content.addView(label("Dein "+selectedAiProviderName()+"-API-Schlüssel",17));
    if(gemini){
      content.addView(label("Einen Gemini-Schlüssel kostenlos in Google AI Studio erzeugen: "
          +"aistudio.google.com/apikey. Kostenlose API-Limits sind begrenzt "
          +"und können sich ändern.",13));
      Button link=button("🌐 Google AI Studio öffnen");
      link.setOnClickListener(v->{
        try{startActivity(new Intent(Intent.ACTION_VIEW,
           Uri.parse("https://aistudio.google.com/apikey")));}
        catch(Exception e){msg("Bitte https://aistudio.google.com/apikey im Browser öffnen.");}
      });content.addView(link);
      content.addView(label("Auf der kostenlosen API-Stufe können Inhalte von Google "
          +"zur Verbesserung seiner Produkte verwendet werden.",12));
    }else{
      content.addView(label("OpenAI-Schlüssel unter platform.openai.com/api-keys. "
         +"OpenAI-API-Nutzung wird unabhängig von ChatGPT abgerechnet.",13));
    }
    content.addView(label("Der Schlüssel wird im Android Keystore verschlüsselt und "
       +"nie in GitHub oder in Backups gespeichert. "
       +"Für öffentlich verteilte Apps wäre ein abgesicherter Server besser.",12));
    EditText e=edit(gemini?"Gemini API-Key (auch neue Auth-Schlüssel)":"sk-…", "",
        android.text.InputType.TYPE_CLASS_TEXT |
        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
    e.setSingleLine(true);
    // The generic edit helper / single-line mode may reset Android's password
    // transformation. Enforce it last to prevent readable keys in screenshots.
    e.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
    e.setSelectAllOnFocus(false);
    e.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
    content.addView(e);
    content.addView(label(FitRocketAiSecrets.configured(this,provider)
      ?"Ein Schlüssel für diesen Anbieter ist vorhanden. Du kannst ihn ersetzen."
      :"Noch kein Schlüssel für diesen Anbieter gespeichert.",13));
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle(selectedAiProviderName()+" einrichten")
        .setView(content).setPositiveButton("Speichern",null)
        .setNeutralButton("Schlüssel löschen",null).setNegativeButton("Abbrechen",null).create();
    dialog.setOnShowListener(v->{
      dialog.getButton(-1).setOnClickListener(btn->{
        try{
          FitRocketAiSecrets.set(this,provider,e.getText().toString().trim());
          e.setText("");dialog.dismiss();msg(selectedAiProviderName()+"-Schlüssel gespeichert.");
          aiHub();
        }catch(Exception error){msg("Speichern fehlgeschlagen: "+error.getMessage());}
      });
      dialog.getButton(-3).setOnClickListener(btn->
        new AlertDialog.Builder(this).setTitle("Schlüssel entfernen?")
          .setMessage("Nur der "+(gemini?"Gemini":"OpenAI")+"-Schlüssel wird gelöscht. "
             +"Rezepte, Vorlieben und Lebensmittel bleiben erhalten.")
          .setPositiveButton("Entfernen",(d,w)->{
            FitRocketAiSecrets.clear(this,provider);
            e.setText("");dialog.dismiss();aiHub();
          }).setNegativeButton("Abbrechen",null).show());
    });
    dialog.show();
  }

  private void aiCompose(String mode){
    String title="recipe".equals(mode)?"Rezeptidee erstellen":
      "meal".equals(mode)?"Ganzes Gericht beschreiben":"Lebensmittel einschätzen";
    shell("KI-Assistent");
    LinearLayout card=card(Color.WHITE);card.addView(label("✨ "+title,20));
    String example="recipe".equals(mode)
       ?"Was möchtest du essen? Zum Beispiel: proteinreich, vegetarisch, wenig Aufwand, mit Paprika und Reis."
       :"meal".equals(mode)
          ?"Beispiel: Zwei Eierpfannkuchen mit Mehl, Milch, etwas Butter und Marmelade. Schätze alle Zutaten zusammen."
          :"Beispiel: Ein Schoko-Protein-Pudding, etwa 200 g, ungesüßt.";
    card.addView(label(example,14));
    EditText input=new EditText(this);
    input.setHint("Beschreibe dein Essen oder deine Wünsche …");
    input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|
       android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|
       android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    input.setMinLines(4);input.setMaxLines(9);
    input.setPadding(dp(14),dp(12),dp(14),dp(12));
    input.setBackground(shape(Color.rgb(248,243,253),16));
    card.addView(input);
    if("recipe".equals(mode)){
      card.addView(label("Deine Vorlieben: "+
         (prefs.getString("aiPreferences","").isEmpty()?"noch nicht hinterlegt"
           :prefs.getString("aiPreferences","")),12));
      action(card,"Vorlieben bearbeiten",()->aiPreferencesDialog());
    }
    card.addView(label("KI: "+selectedAiProviderName()
      +" · Vorschläge und Nährwerte sind Schätzungen.",13));
    card.addView(label("Du prüfst alle geschätzten Mengen und Nährwerte, bevor etwas "
      +"in deinem Tagebuch oder Lebensmittelkatalog gespeichert wird.",12));
    action(card,"✨ Vorschlag mit KI erstellen",()->aiSubmit(mode,input.getText().toString()));
    page.addView(card);
    if(!FitRocketAiSecrets.configured(this,selectedAiProvider())){
      LinearLayout key=card(LAV);key.addView(label("🔐 KI noch nicht verbunden",17));
      key.addView(label("Für eine Anfrage benötigst du einen eigenen "
           +selectedAiProviderName()+"-API-Schlüssel.",13));
      action(key,"API-Schlüssel einrichten",()->aiKeyDialog());page.addView(key);
    }
    if("food".equals(mode)){
      LinearLayout manual=card(MINT);
      action(manual,"Lebensmittel ohne KI aus Nährwerten erstellen",()->aiManualFood());
      page.addView(manual);
    }
  }

  private void aiSubmit(String mode,String prompt){
    if(!FitRocketAiSecrets.configured(this,selectedAiProvider())){
      new AlertDialog.Builder(this).setTitle("KI-Schlüssel fehlt")
        .setMessage("Für "+selectedAiProviderName()+" benötigst du einen eigenen API-Schlüssel. "
          +"Für Lebensmittel kannst du Nährwerte auch komplett offline eingeben.")
        .setPositiveButton("Einrichten",(d,w)->aiKeyDialog())
        .setNegativeButton("Abbrechen",null).show();return;
    }
    String preferences=prefs.getString("aiPreferences","");
    if(prompt.trim().length()<5||prompt.length()>1400){
      msg("Bitte eine Beschreibung mit 5–1400 Zeichen eingeben.");return;
    }
    AlertDialog loading=new AlertDialog.Builder(this).setTitle("✨ "+selectedAiProviderName()+" erstellt Vorschlag")
       .setMessage("Rezept oder Lebensmittel werden vorgeschlagen. "
          +"Im nächsten Schritt prüfst du die Schätzung.")
       .setNegativeButton("Im Hintergrund abbrechen",(d,w)->{})
       .create();
    final boolean[] active={true};
    loading.setOnDismissListener(d->active[0]=false);
    loading.show();
    FitRocketAiService.request(this,mode,prompt,preferences,foods,
      new FitRocketAiService.Callback(){
        @Override public void onSuccess(JSONObject result){
          boolean current=active[0];loading.dismiss();
          if(current)aiReview(mode,result);
        }
        @Override public void onFailure(String message){
          boolean current=active[0];loading.dismiss();
          if(current)new AlertDialog.Builder(MainActivity.this).setTitle("KI-Anfrage fehlgeschlagen")
            .setMessage(message).setPositiveButton("OK",null).show();
        }
      });
  }


  private class AiIngredientEditor {
    final JSONObject original;
    final EditText name,grams,kcal,saturated,sugar,protein;
    final CheckBox useCatalog;
    final JSONObject matched;
    final LinearLayout container;

    AiIngredientEditor(JSONObject source,boolean forcedManual,LinearLayout target){
      original=source;
      matched=forcedManual?null:FitRocketAiRules.matchCatalog(
          source.optString("food_id"),source.optString("name"),foods);
      container=card(Color.WHITE);
      name=edit("Zutat",source.optString("name"),1);container.addView(name);
      grams=edit("Gramm",String.format(Locale.GERMAN,"%.0f",source.optDouble("grams",100)),8194);
      container.addView(label("Menge (g / ml)",12));container.addView(grams);
      if(forcedManual){grams.setText("100");grams.setEnabled(false);}

      if(matched!=null){
        useCatalog=new CheckBox(MainActivity.this);
        useCatalog.setText("FitRocket-Katalog verwenden: "+
            ("basic".equals(matched.optString("status"))?"0 Punkte":"bekannte Nährwerte"));
        useCatalog.setChecked(true);
        container.addView(useCatalog);
        container.addView(label("Bei abweichender Verarbeitung (z. B. Öl, Panade, Zucker) "
            +"Häkchen entfernen und Nährwerte selbst prüfen.",12));
      }else useCatalog=null;
      container.addView(label("Schätzungssicherheit: "+source.optString("confidence","niedrig")+
          " · "+source.optString("note",""),12));
      LinearLayout nutrition=new LinearLayout(MainActivity.this);nutrition.setOrientation(1);
      nutrition.addView(label("Angaben pro 100 g / 100 ml (bitte prüfen)",14));
      kcal=edit("Kalorien",source.optDouble("kcal_100g",0)<0?"":String.format(Locale.GERMAN,"%.1f",source.optDouble("kcal_100g",0)),8194);
      saturated=edit("Gesättigte Fette",source.optDouble("saturated_fat_100g",0)<0?"":String.format(Locale.GERMAN,"%.1f",source.optDouble("saturated_fat_100g",0)),8194);
      sugar=edit("Zucker",source.optDouble("sugar_100g",0)<0?"":String.format(Locale.GERMAN,"%.1f",source.optDouble("sugar_100g",0)),8194);
      protein=edit("Eiweiß",source.optDouble("protein_100g",0)<0?"":String.format(Locale.GERMAN,"%.1f",source.optDouble("protein_100g",0)),8194);
      String[] keys={"Kilokalorien","Gesättigte Fettsäuren (g)","Zucker (g)","Eiweiß (g)"};
      EditText[] fields={kcal,saturated,sugar,protein};
      for(int i=0;i<fields.length;i++){
        nutrition.addView(label(keys[i],12));nutrition.addView(fields[i]);
      }
      nutrition.setVisibility(forcedManual?View.VISIBLE:View.GONE);
      Button expand=button("Nährwerte prüfen / bearbeiten");
      expand.setOnClickListener(v->{
        boolean open=nutrition.getVisibility()==View.VISIBLE;
        nutrition.setVisibility(open?View.GONE:View.VISIBLE);
        expand.setText(open?"Nährwerte prüfen / bearbeiten":"Nährwerte ausblenden");
      });
      container.addView(expand);container.addView(nutrition);
      target.addView(container);
    }

    JSONObject toJson(boolean forceNewName,String targetName) throws JSONException{
      String newName=forceNewName?targetName:name.getText().toString().trim();
      JSONObject x=new JSONObject();
      x.put("name",newName);
      x.put("food_id",useCatalog!=null&&useCatalog.isChecked()?original.optString("food_id"):"");
      x.put("grams",forceNewName?100:num(grams.getText().toString()));
      x.put("kcal_100g",num(kcal.getText().toString()));
      x.put("saturated_fat_100g",num(saturated.getText().toString()));
      x.put("sugar_100g",num(sugar.getText().toString()));
      x.put("protein_100g",num(protein.getText().toString()));
      x.put("confidence",original.optString("confidence","niedrig"));
      x.put("note",original.optString("note",""));
      return x;
    }
  }

  private JSONObject buildAiReview(String mode,JSONObject original,EditText title,
          EditText servings,Spinner group,List<AiIngredientEditor> editors) throws JSONException {
    JSONObject draft=new JSONObject();
    draft.put("title",title.getText().toString().trim());
    draft.put("summary",original.optString("summary"));
    draft.put("instructions",original.optString("instructions"));
    draft.put("minutes",original.optInt("minutes",20));
    draft.put("group",group.getSelectedItem().toString());
    draft.put("servings",("recipe".equals(mode)
        ?Integer.parseInt(servings.getText().toString().trim()):1));
    draft.put("confidence",original.optString("confidence","niedrig"));
    draft.put("uncertainty",original.optString("uncertainty",""));
    JSONArray items=new JSONArray();
    for(AiIngredientEditor editor:editors){
      items.put(editor.toJson("food".equals(mode),draft.optString("title")));
    }
    draft.put("ingredients",items);
    return FitRocketAiRules.validateResponse(draft,mode);
  }

  private void aiReview(String mode,JSONObject output){
    shell("KI-Vorschau");
    LinearLayout hero=card(LAV);
    hero.addView(label("✨ KI-Vorschlag – überprüfe alles",20));
    hero.addView(label("FitRocket berechnet die Punkte selbst. Die Schätzung kann von "
       +"tatsächlichen Mengen und Verpackungswerten deutlich abweichen.",13));
    hero.addView(label("Sicherheit der KI-Einschätzung: "+output.optString("confidence","niedrig"),14));
    if(!output.optString("uncertainty").isEmpty())
      hero.addView(label("Unsicherheiten: "+output.optString("uncertainty"),13));
    page.addView(hero);

    LinearLayout setup=card(Color.WHITE);
    setup.addView(label("Titel",14));
    EditText title=edit("Titel",output.optString("title"),1);setup.addView(title);
    setup.addView(label("Mahlzeitengruppe",14));
    Spinner group=groupSpinner();String[] groups={"Frühstück","Mittagessen","Abendessen","Snack","Getränke"};
    for(int i=0;i<groups.length;i++)if(groups[i].equals(output.optString("group")))group.setSelection(i);
    setup.addView(group);
    EditText servings=edit("Portionen",String.valueOf(output.optInt("servings",1)),2);
    if("recipe".equals(mode)){
      setup.addView(label("Wie viele Portionen ergeben alle Zutaten zusammen?",13));
      setup.addView(servings);
    }
    page.addView(setup);
    if(bookingGroup!=null && "meal".equals(mode)){
      for(int i=0;i<groups.length;i++)if(groups[i].equals(bookingGroup))group.setSelection(i);
      setup.addView(label("Eintrag für "+bookingGroup+" am "+pretty(
          bookingDate==null?today():bookingDate),13));
    }
    JSONArray list=output.optJSONArray("ingredients");
    ArrayList<AiIngredientEditor> editors=new ArrayList<>();
    heading("Zutaten und geschätzte Nährwerte");
    for(int i=0;list!=null&&i<list.length();i++){
      JSONObject item=list.optJSONObject(i);if(item==null)continue;
      editors.add(new AiIngredientEditor(item,"food".equals(mode),page));
    }
    if("recipe".equals(mode) && !output.optString("instructions").isEmpty()){
      LinearLayout prep=card(Color.WHITE);
      prep.addView(label("👩‍🍳 Zubereitung · ca. "+output.optInt("minutes",20)+" Min.",17));
      prep.addView(label(output.optString("instructions"),14));page.addView(prep);
    }
    final TextView points=label("FitRocket berechnet deine Punkte …",19);
    points.setTypeface(null,1);
    LinearLayout checkout=card(MINT);checkout.addView(points);
    Runnable update=()->{
      try{
        JSONObject draft=buildAiReview(mode,output,title,servings,group,editors);
        int pp=FitRocketAiRules.pointsPerServing(draft,foods);
        points.setText(("recipe".equals(mode)?"Pro Portion: ca. ":"Geschätzt: ca. ")+pp+" FitRocket-Punkte");
      }catch(Exception error){points.setText("Angaben prüfen – Punkte aktuell nicht berechenbar.");}
    };
    TextWatcher watcher=new TextWatcher(){
      public void beforeTextChanged(CharSequence x,int a,int c,int f){}
      public void onTextChanged(CharSequence x,int a,int b,int c){update.run();}
      public void afterTextChanged(Editable x){}
    };
    title.addTextChangedListener(watcher);servings.addTextChangedListener(watcher);
    group.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
      public void onItemSelected(android.widget.AdapterView<?> p,View v,int position,long id){update.run();}
      public void onNothingSelected(android.widget.AdapterView<?> p){}
    });
    for(AiIngredientEditor field:editors){
      for(EditText input:new EditText[]{field.name,field.grams,field.kcal,
          field.saturated,field.sugar,field.protein})input.addTextChangedListener(watcher);
      if(field.useCatalog!=null)field.useCatalog.setOnCheckedChangeListener((x,isChecked)->update.run());
      if(!"food".equals(mode)){
        action(field.container,"− Diese Zutat entfernen",()->{
          editors.remove(field);
          page.removeView(field.container);
          update.run();
        });
      }
    }
    if(!"food".equals(mode)){
      Button addIngredient=button("＋ Fehlende Zutat ergänzen");
      addIngredient.setOnClickListener(v->{
        if(editors.size()>=12){msg("Es sind höchstens 12 Zutaten pro Vorschlag möglich.");return;}
        try{
          JSONObject fresh=new JSONObject();
          fresh.put("name","Neue Zutat");fresh.put("grams",100);fresh.put("food_id","");
          fresh.put("kcal_100g",-1);fresh.put("saturated_fat_100g",-1);
          fresh.put("sugar_100g",-1);fresh.put("protein_100g",-1);
          fresh.put("confidence","niedrig");fresh.put("note","Manuell ergänzt; Nährwerte eingeben");
          AiIngredientEditor field=new AiIngredientEditor(fresh,true,page);
          editors.add(field);
          page.removeView(field.container);
          page.addView(field.container,page.indexOfChild(checkout));
          for(EditText input:new EditText[]{field.name,field.grams,field.kcal,
              field.saturated,field.sugar,field.protein})input.addTextChangedListener(watcher);
          action(field.container,"− Diese Zutat entfernen",()->{
            editors.remove(field);page.removeView(field.container);update.run();
          });
          update.run();
        }catch(Exception e){msg("Zutat konnte nicht hinzugefügt werden.");}
      });
      page.addView(addIngredient);
    }
    update.run();
    checkout.addView(label("Alle Zutaten sind editierbar. Gespeichert wird erst nach deiner Bestätigung.",12));
    if("meal".equals(mode)||"recipe".equals(mode)){
      action(checkout,"🍽 Als Mahlzeit eintragen",()->{
        try{
          JSONObject draft=buildAiReview(mode,output,title,servings,group,editors);
          int score=FitRocketAiRules.pointsPerServing(draft,foods);
          addMeal(draft.optString("title")+( "recipe".equals(mode)?" (1 Portion)":""),score,
              "AI_MEAL","KI-Schätzung · überprüft",draft.optString("group"));
          // Preserve the reviewed composition for later inspection in the diary.
          if(!meals.isEmpty()){
            JSONObject saved=meals.get(meals.size()-1);
            saved.put("aiIngredients",draft.getJSONArray("ingredients"));
            saved.put("aiUncertainty",draft.optString("uncertainty",""));
            saved.put("aiEstimated",true);
            persist("meals",meals);
          }
          msg("Gericht mit "+score+" geschätzten Punkten eingetragen.");
          afterMealAdded();
        }catch(Exception e){msg("Bitte Zutaten und Nährwerte prüfen: "+e.getMessage());}
      });
    }
    if("recipe".equals(mode)){
      action(checkout,"💾 Rezept dauerhaft speichern",()->{
        try{
          JSONObject draft=buildAiReview(mode,output,title,servings,group,editors);
          int newCount=0;
          JSONArray arr=draft.getJSONArray("ingredients");
          for(int i=0;i<arr.length();i++){
            JSONObject x=arr.getJSONObject(i);
            if(FitRocketAiRules.matchCatalog(x.optString("food_id"),x.optString("name"),foods)==null)newCount++;
          }
          final int count=newCount;
          new AlertDialog.Builder(this).setTitle("Rezept speichern?")
            .setMessage("Das Rezept und "+count+" neue Zutaten werden in deiner lokalen FitRocket-Datenbank gespeichert. "
              +"Schätzwerte können später von echten Produktdaten abweichen.")
            .setPositiveButton("Speichern",(d,w)->{
              try{saveAiRecipe(draft);recipeScreen();msg("Rezept gespeichert.");}
              catch(Exception error){msg("Rezept konnte nicht gespeichert werden: "+error.getMessage());}
            }).setNegativeButton("Abbrechen",null).show();
        }catch(Exception e){msg("Bitte die Zutaten zuerst prüfen: "+e.getMessage());}
      });
    }
    if("food".equals(mode)){
      action(checkout,"💾 Als neues Lebensmittel speichern",()->{
        try{
          JSONObject draft=buildAiReview(mode,output,title,servings,group,editors);
          JSONObject ingredient=draft.getJSONArray("ingredients").getJSONObject(0);
          new AlertDialog.Builder(this).setTitle("Lebensmittel übernehmen?")
            .setMessage("Die Nährwerte dieses Produkts sind KI-Schätzungen. "
              +"Bei abgepackten Lebensmitteln ist das tatsächliche Etikett maßgeblich. "
              +"Möchtest du es nach Prüfung in deine persönliche Lebensmittelliste aufnehmen?")
            .setPositiveButton("Speichern",(d,w)->{
              try{JSONObject food=FitRocketAiRules.newFood(ingredient,"AI");
                saveAiFood(food);msg("Lebensmittel wurde hinzugefügt.");foodDialog(food);
              }catch(Exception err){msg("Lebensmittel konnte nicht gespeichert werden.");}
            }).setNegativeButton("Abbrechen",null).show();
        }catch(Exception e){msg("Bitte die Nährwerte prüfen: "+e.getMessage());}
      });
    }
    page.addView(checkout);
  }

  private void saveAiFood(JSONObject food) throws JSONException{
    JSONArray old=new JSONArray(prefs.getString("aiFoods","[]"));
    if(old.length()>=250)throw new IllegalStateException("Maximal 250 eigene KI-Lebensmittel");
    JSONArray newer=new JSONArray();newer.put(food);
    for(int i=0;i<old.length();i++)newer.put(old.getJSONObject(i));
    if(!prefs.edit().putString("aiFoods",newer.toString()).commit())
      throw new IllegalStateException("Speicherfehler");
    foods.add(0,food);
  }

  private void saveAiRecipe(JSONObject edited) throws JSONException{
    JSONArray ingredients=edited.getJSONArray("ingredients");
    JSONArray priorFoods=new JSONArray(prefs.getString("aiFoods","[]"));
    JSONArray priorRecipes=new JSONArray(prefs.getString("savedAiRecipes","[]"));
    if(priorRecipes.length()>=80)throw new IllegalStateException("Maximal 80 eigene Rezepte");
    JSONArray additions=new JSONArray(),links=new JSONArray();
    for(int i=0;i<ingredients.length();i++){
      JSONObject component=ingredients.getJSONObject(i);
      JSONObject existing=FitRocketAiRules.matchCatalog(
          component.optString("food_id"),component.optString("name"),foods);
      JSONObject linked=existing==null?FitRocketAiRules.newFood(component,"AI"):existing;
      if(existing==null)additions.put(linked);
      JSONObject line=new JSONObject();
      line.put("id",linked.getString("id"));
      line.put("grams",component.getDouble("grams"));
      links.put(line);
    }
    if(priorFoods.length()+additions.length()>250)
      throw new IllegalStateException("Zu viele eigene Lebensmittel");
    JSONObject recipe=new JSONObject();
    recipe.put("id","AIRECIPE"+UUID.randomUUID().toString().replace("-",""));
    recipe.put("name",edited.optString("title"));
    recipe.put("group",edited.optString("group"));
    recipe.put("emoji","✨");
    recipe.put("minutes",Math.max(1,edited.optInt("minutes",20)));
    recipe.put("servings",edited.optInt("servings",1));
    recipe.put("instructions",edited.optString("instructions"));
    recipe.put("ingredients",links);
    recipe.put("notes","Mit KI vorgeschlagen; Nährwerte vor dem Kochen prüfen.");

    JSONArray newFoods=new JSONArray();
    for(int i=0;i<additions.length();i++)newFoods.put(additions.getJSONObject(i));
    for(int i=0;i<priorFoods.length();i++)newFoods.put(priorFoods.getJSONObject(i));
    JSONArray newRecipes=new JSONArray();newRecipes.put(recipe);
    for(int i=0;i<priorRecipes.length();i++)newRecipes.put(priorRecipes.getJSONObject(i));
    if(!prefs.edit().putString("aiFoods",newFoods.toString())
      .putString("savedAiRecipes",newRecipes.toString()).commit())
        throw new IllegalStateException("Rezept konnte nicht gespeichert werden.");
    for(int i=0;i<additions.length();i++)foods.add(0,additions.getJSONObject(i));
    recipes.add(0,recipe);
  }

  private void aiManualFood(){
    shell("Eigenes Lebensmittel");
    LinearLayout content=card(Color.WHITE);
    content.addView(label("🍎 Lebensmittel nach Nährwertetikett erstellen",19));
    content.addView(label("Dafür brauchst du weder Internet noch einen KI-Schlüssel. "
        +"Gib die vier Angaben pro 100 g oder 100 ml vom Produkt ein.",13));
    EditText name=edit("Produktname","",1);content.addView(name);
    EditText kcal=edit("kcal je 100 g / ml","",8194);
    EditText saturated=edit("gesättigte Fette g je 100 g / ml","",8194);
    EditText sugar=edit("Zucker g je 100 g / ml","",8194);
    EditText protein=edit("Eiweiß g je 100 g / ml","",8194);
    for(EditText e:new EditText[]{kcal,saturated,sugar,protein})content.addView(e);
    TextView points=label("Punkte pro 100 g: –",17);content.addView(points);
    Runnable update=()->{
      try{points.setText("FitRocket: "+FitRocketAiRules.points(100,
        num(kcal.getText().toString()),num(saturated.getText().toString()),
        num(sugar.getText().toString()),num(protein.getText().toString()))+" P pro 100 g");}
      catch(Exception e){points.setText("Punkte pro 100 g: Bitte alle Werte eingeben.");}
    };
    TextWatcher watcher=new TextWatcher(){
      public void beforeTextChanged(CharSequence x,int a,int b,int c){}
      public void onTextChanged(CharSequence x,int a,int b,int c){update.run();}
      public void afterTextChanged(Editable x){}
    };
    for(EditText e:new EditText[]{kcal,saturated,sugar,protein})e.addTextChangedListener(watcher);
    action(content,"💾 Lebensmittel speichern",()->{
      try{
        JSONObject item=new JSONObject();
        item.put("name",name.getText().toString().trim());
        item.put("grams",100);item.put("food_id","");
        item.put("kcal_100g",num(kcal.getText().toString()));
        item.put("saturated_fat_100g",num(saturated.getText().toString()));
        item.put("sugar_100g",num(sugar.getText().toString()));
        item.put("protein_100g",num(protein.getText().toString()));
        JSONObject food=FitRocketAiRules.newFood(item,"OWN");
        food.put("source","Deine Nährwertangaben");
        food.put("notes","Selbst eingetragene Nährwerte. Herstellerangaben prüfen.");
        saveAiFood(food);
        msg("Eigenes Lebensmittel gespeichert.");
        foodDialog(food);
      }catch(Exception error){msg("Bitte den Namen und alle vier Nährwerte korrekt eingeben.");}
    });
    page.addView(content);
  }


  private String pendingRecipeImageId=null;
  private void showRecipeImage(LinearLayout parent,String uri){
    if(uri==null||uri.isEmpty())return;
    try{
      ImageView picture=new ImageView(this);
      if(uri.startsWith("content:"))picture.setImageURI(Uri.parse(uri));
      else{
        byte[] image=android.util.Base64.decode(uri,android.util.Base64.DEFAULT);
        picture.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(image,0,image.length));
      }
      picture.setScaleType(ImageView.ScaleType.CENTER_CROP);
      parent.addView(picture,new LinearLayout.LayoutParams(-1,dp(150)));
    }catch(Exception ignored){}
  }
  private void saveOwnRecipe(JSONObject recipe){
    try{
      JSONArray saved=new JSONArray(prefs.getString("savedAiRecipes","[]")),next=new JSONArray();
      next.put(recipe);
      for(int i=0;i<saved.length();i++){
        JSONObject item=saved.optJSONObject(i);
        if(item!=null&&!item.optString("id").equals(recipe.optString("id")))next.put(item);
      }
      prefs.edit().putString("savedAiRecipes",next.toString()).apply();
      loadRecipes();recipeScreen();
    }catch(Exception e){msg("Rezept konnte nicht gespeichert werden.");}
  }
  private void editOwnRecipe(JSONObject existing){
    final JSONObject source=existing==null?new JSONObject():existing;
    LinearLayout form=new LinearLayout(this);form.setOrientation(1);form.setPadding(dp(17),dp(9),dp(17),dp(9));
    EditText name=edit("Rezeptname",source.optString("name"),1);form.addView(name);
    EditText ingredients=edit("Zutaten mit Mengenangaben (eine pro Zeile)",source.optString("manualIngredients"),1);
    ingredients.setSingleLine(false);ingredients.setMinLines(5);form.addView(ingredients);
    EditText steps=edit("Zubereitung",source.optString("instructions"),1);
    steps.setSingleLine(false);steps.setMinLines(5);form.addView(steps);
    EditText minutes=edit("Zubereitungszeit in Minuten",String.valueOf(source.optInt("minutes",25)),2);form.addView(minutes);
    EditText servings=edit("Portionen",String.valueOf(source.optInt("servings",2)),2);form.addView(servings);
    EditText points=edit("Punkte je Portion (manuell prüfen)",String.valueOf(source.optInt("manualPoints",0)),2);form.addView(points);
    Spinner category=groupSpinner();form.addView(category);
    String[] groups={"Frühstück","Mittagessen","Abendessen","Snack","Getränke"};
    for(int i=0;i<groups.length;i++)if(groups[i].equals(source.optString("group")))category.setSelection(i);
    form.addView(label("Eigene Rezepte: Punkte bitte anhand der verwendeten Zutaten prüfen.",12));
    ScrollView scroll=new ScrollView(this);scroll.addView(form);
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle(existing==null?"Eigenes Rezept erstellen":"Rezept bearbeiten")
       .setView(scroll).setNegativeButton("Abbrechen",null).setPositiveButton("Speichern",null).create();
    dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(button->{
      try{
        String title=name.getText().toString().trim(),items=ingredients.getText().toString().trim(),
            instructions=steps.getText().toString().trim();
        int time=Integer.parseInt(minutes.getText().toString().trim()),portion=Integer.parseInt(servings.getText().toString().trim()),
            score=Integer.parseInt(points.getText().toString().trim());
        if(title.isEmpty()||items.isEmpty()||instructions.isEmpty()||time<1||time>1440||portion<1||portion>100||score<0||score>1000)
          throw new IllegalArgumentException();
        JSONObject recipe=new JSONObject(source.toString());
        recipe.put("id",source.optString("id","").isEmpty()?"OWN"+UUID.randomUUID().toString().replace("-",""):source.optString("id"));
        recipe.put("name",title);recipe.put("manualIngredients",items);
        recipe.put("instructions",instructions);recipe.put("minutes",time);recipe.put("servings",portion);
        recipe.put("manualPoints",score);recipe.put("group",groups[category.getSelectedItemPosition()]);
        recipe.put("emoji","🍽");recipe.put("ingredients",new JSONArray());
        dialog.dismiss();saveOwnRecipe(recipe);
        new AlertDialog.Builder(this).setTitle("Gerichtfoto").setMessage("Möchtest du ein Foto für dieses Rezept auswählen?")
          .setPositiveButton("Foto auswählen",(d,w)->selectRecipePhoto(recipe.optString("id")))
          .setNegativeButton("Später",null).show();
      }catch(Exception e){msg("Bitte Name, Zutaten, Zubereitung, Zeit, Portionen und Punkte gültig eingeben.");}
    }));
    dialog.show();
  }
  private void selectRecipePhoto(String id){
    pendingRecipeImageId=id;
    new AlertDialog.Builder(this).setTitle("Rezeptfoto hinzufügen")
      .setItems(new String[]{"📷 Foto mit Kamera aufnehmen","🖼 Bild aus Galerie wählen"},(dialog,choice)->{
        if(choice==0){
          Intent camera=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
          try{startActivityForResult(camera,85);}catch(Exception e){msg("Keine Kamera-App gefunden.");}
        }else{
          Intent pick=new Intent(Intent.ACTION_OPEN_DOCUMENT);
          pick.setType("image/*");pick.addCategory(Intent.CATEGORY_OPENABLE);
          startActivityForResult(pick,84);
        }
      }).show();
  }
  private String encodedRecipePhoto(InputStream stream) throws IOException {
    android.graphics.Bitmap bitmap=android.graphics.BitmapFactory.decodeStream(stream);
    if(bitmap==null)throw new IOException("Ungültiges Bild");
    int width=bitmap.getWidth(),height=bitmap.getHeight();
    float scale=Math.min(1f,960f/Math.max(width,height));
    if(scale<1f)bitmap=android.graphics.Bitmap.createScaledBitmap(bitmap,
        Math.max(1,Math.round(width*scale)),Math.max(1,Math.round(height*scale)),true);
    ByteArrayOutputStream bytes=new ByteArrayOutputStream();
    if(!bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG,72,bytes))
      throw new IOException("Bild konnte nicht komprimiert werden");
    if(bytes.size()>850000)throw new IOException("Bild zu groß");
    return android.util.Base64.encodeToString(bytes.toByteArray(),android.util.Base64.NO_WRAP);
  }
  private void applyRecipePhoto(String encoded){
    try{
      JSONArray saved=new JSONArray(prefs.getString("savedAiRecipes","[]"));
      boolean found=false;
      for(int i=0;i<saved.length();i++){
        JSONObject item=saved.optJSONObject(i);
        if(item!=null&&item.optString("id").equals(pendingRecipeImageId)){
          item.put("imageData",encoded);item.remove("imageUri");found=true;
        }
      }
      if(!found)throw new IllegalArgumentException("Rezept nicht gefunden");
      prefs.edit().putString("savedAiRecipes",saved.toString()).apply();
      pendingRecipeImageId=null;loadRecipes();recipeScreen();
    }catch(Exception e){msg("Rezeptfoto konnte nicht gespeichert werden.");}
  }
  private void importRecipeLink(){
    EditText link=edit("https://...","",1);
    new AlertDialog.Builder(this).setTitle("Rezept über URL importieren")
      .setMessage("Unterstützt Rezeptseiten mit Recipe-JSON-LD. Angaben vor dem Speichern überprüfen.")
      .setView(link).setNegativeButton("Abbrechen",null)
      .setPositiveButton("Abrufen",(d,w)->{
        String url=link.getText().toString().trim();
        if(!url.startsWith("https://")){msg("Bitte eine HTTPS-Rezeptadresse eingeben.");return;}
        new Thread(()->{
          try{
            java.net.HttpURLConnection conn=(java.net.HttpURLConnection)new java.net.URL(url).openConnection();
            conn.setConnectTimeout(8000);conn.setReadTimeout(8000);conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent","FitRocket/0.15.9 (Recipe Import)");
            if(conn.getResponseCode()!=200)throw new IOException("HTTP "+conn.getResponseCode());
            String html;
            try(InputStream stream=conn.getInputStream()){
              byte[] buffer=new byte[4096];ByteArrayOutputStream out=new ByteArrayOutputStream();int len;
              while((len=stream.read(buffer))!=-1){out.write(buffer,0,len);if(out.size()>750000)throw new IOException("Seite zu groß");}
              html=new String(out.toByteArray(),StandardCharsets.UTF_8);
            }finally{conn.disconnect();}
            java.util.regex.Matcher matches=java.util.regex.Pattern.compile(
              "<script[^>]*type=[\\\"']application/ld\\+json[\\\"'][^>]*>([\\s\\S]*?)</script>",
              java.util.regex.Pattern.CASE_INSENSITIVE).matcher(html);
            JSONObject found=null;
            while(matches.find()&&found==null){
              try{
                String raw=matches.group(1).trim();
                Object obj=raw.startsWith("[")?new JSONArray(raw):new JSONObject(raw);
                found=findLinkedRecipe(obj);
              }catch(Exception ignored){}
            }
            if(found==null)throw new IOException("Keine strukturierten Rezeptdaten gefunden");
            JSONObject recipe=found;
            runOnUiThread(()->{
              try{
                JSONArray raw=recipe.optJSONArray("recipeIngredient");
                StringBuilder ingredientText=new StringBuilder();
                if(raw!=null)for(int i=0;i<raw.length();i++)ingredientText.append(raw.optString(i)).append("\n");
                Object directions=recipe.opt("recipeInstructions");
                String directionsText=directions instanceof String?(String)directions:"";
                if(directions instanceof JSONArray){JSONArray arr=(JSONArray)directions;StringBuilder b=new StringBuilder();
                  for(int i=0;i<arr.length();i++){JSONObject step=arr.optJSONObject(i);
                    b.append(step!=null?step.optString("text"):arr.optString(i)).append("\n");}directionsText=b.toString();}
                JSONObject draft=new JSONObject();
                draft.put("name",recipe.optString("name"));draft.put("manualIngredients",ingredientText.toString());
                draft.put("instructions",directionsText);draft.put("minutes",25);draft.put("servings",2);
                draft.put("sourceUrl",url);draft.put("manualPoints",0);
                if(FitRocketAiSecrets.configured(this,selectedAiProvider())){
                  String details="Analysiere dieses extern importierte Rezept und schätze die Nährwerte pro 100 g jeder Zutat. " +
                    "Übernimm die Zutatenmengen, erfinde keine zusätzlichen Zutaten. " +
                    "Rezept: "+draft.optString("name")+"; Zutaten: "+draft.optString("manualIngredients")+
                    "; Zubereitung: "+draft.optString("instructions");
                  if(details.length()>1400)details=details.substring(0,1400);
                  aiSubmit("recipe",details);
                }else{
                  msg("Keine KI eingerichtet: Rezept wird zur manuellen Prüfung geöffnet.");
                  editOwnRecipe(draft);
                }
              }catch(Exception e){msg("Rezeptdaten konnten nicht übernommen werden.");}
            });
          }catch(Exception error){runOnUiThread(()->msg("Import nicht möglich: "+error.getMessage()));}
        }).start();
      }).show();
  }
  private JSONObject findLinkedRecipe(Object node){
    if(node instanceof JSONArray){
      JSONArray arr=(JSONArray)node;
      for(int i=0;i<arr.length();i++){JSONObject r=findLinkedRecipe(arr.opt(i));if(r!=null)return r;}
    }else if(node instanceof JSONObject){
      JSONObject obj=(JSONObject)node;Object type=obj.opt("@type");
      if("Recipe".equalsIgnoreCase(String.valueOf(type))||(type instanceof JSONArray&&((JSONArray)type).toString().contains("Recipe")))return obj;
      JSONObject r=findLinkedRecipe(obj.opt("@graph"));if(r!=null)return r;
    }
    return null;
  }

  private void recipeScreen(){
    shell("Kochbuch");
    LinearLayout intro=card(Color.rgb(255,249,239));
    intro.addView(label("🍲 Heute etwas Leckeres kochen?",19));
    intro.addView(label("FitRocket-Rezepte und deine selbst gespeicherten KI-Rezepte – mit Zutaten und geschätzten Punkten.",13));
    action(intro,"✨ Neues Rezept mit KI erstellen",()->aiCompose("recipe"));
    action(intro,"📝 Eigenes Rezept anlegen",()->editOwnRecipe(null));
    action(intro,"🔗 Rezept aus einem Link importieren",()->importRecipeLink());
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
      try{points=recipe.has("manualPoints")?recipe.optInt("manualPoints"):FitRocketRecipe.pointsPerServing(recipe,foods);}catch(Exception e){continue;}
      LinearLayout row=card(Color.WHITE);
      if(recipe.has("imageData"))showRecipeImage(row,recipe.optString("imageData"));
      else if(recipe.has("imageUri"))showRecipeImage(row,recipe.optString("imageUri"));
      LinearLayout nameRow=new LinearLayout(this);nameRow.setOrientation(0);nameRow.setGravity(Gravity.CENTER_VERTICAL);
      TextView name=label(recipe.optString("emoji","🍽")+"  "+recipe.optString("name"),17);name.setTypeface(null,1);
      nameRow.addView(name,new LinearLayout.LayoutParams(0,-2,1));
      nameRow.addView(chip("ca. "+points+" P",points==0?MINT:PEACH));
      row.addView(nameRow);
      row.addView(label(category+" · "+recipe.optInt("minutes")+" Min. · "+
          recipe.optInt("servings")+" Portion(en)",13));
      action(row,"Rezept & Zutaten ansehen ›",()->recipeDialog(recipe));
      if(recipe.optString("id").startsWith("OWN"))action(row,"✏️ Bearbeiten",()->editOwnRecipe(recipe));
      if(recipe.optString("id").startsWith("OWN")||recipe.optString("id").startsWith("AIRECIPE"))
        action(row,"📷 Rezeptfoto wählen / ändern",()->selectRecipePhoto(recipe.optString("id")));
      page.addView(row);count++;
    }
    if(count==0)page.addView(label("Für diese Kategorie sind noch keine Rezeptideen vorhanden.",14));
  }
  private void recipeDialog(JSONObject recipe){
    int points;
    try{points=recipe.has("manualPoints")?recipe.optInt("manualPoints"):FitRocketRecipe.pointsPerServing(recipe,foods);}
    catch(Exception e){msg("Für dieses Rezept fehlen Nährwerte.");return;}
    LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(20),dp(8),dp(20),dp(8));
    content.addView(label(recipe.optString("emoji","🍽")+" "+
        recipe.optString("name")+" · ca. "+points+" P pro Portion",18));
    content.addView(label("Zutaten für "+recipe.optInt("servings")+" Portion(en):",15));
    content.addView(label(recipe.has("manualIngredients")?recipe.optString("manualIngredients"):FitRocketRecipe.ingredients(recipe,foods),14));
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

  /** Compatibility for older navigation callbacks; the menu is now the Profile tab. */
  private void more(){profilePage();}


  private JSONArray safeArray(String key){
    try{return new JSONArray(prefs.getString(key,"[]"));}
    catch(Exception e){return new JSONArray();}
  }
  private JSONArray favoriteItems(){
    try{return new JSONArray(prefs.getString("favoriteMeals","[]"));}
    catch(Exception e){return new JSONArray();}
  }
  private JSONObject findFoodById(String id){
    if(id==null||id.isEmpty())return null;
    for(JSONObject food:foods)if(id.equals(food.optString("id")))return food;
    return null;
  }

  private String favoriteUnit(JSONObject favorite,JSONObject food){
    String stored=favorite.optString("quantityUnit","");
    if("ml".equals(stored)||"g".equals(stored))return stored;
    if(food!=null)return FitRocketFoodDisplay.unit(food);
    return "Getränke".equals(favorite.optString("group"))?"ml":"g";
  }

  private void actuallySaveFavorite(JSONObject meal,double reference,String unit){
    try{
      FitRocketFavoriteMath.positive(reference);
      JSONArray all=favoriteItems();
      if(all.length()>=300){msg("Maximal 300 Favoriten möglich.");return;}
      String id=meal.optString("foodId","");
      String title=meal.optString("baseName",
          FitRocketFavoriteMath.cleanName(meal.optString("name")));
      for(int i=0;i<all.length();i++){
        JSONObject fav=all.optJSONObject(i);
        if(fav!=null&&id.equals(fav.optString("foodId"))&&
            title.equalsIgnoreCase(fav.optString("name"))&&
            Math.abs(fav.optDouble("points")-pts(meal))<0.001&&
            Math.abs(fav.optDouble("referenceQuantity",-1)-reference)<0.01){
          msg("Dieser Favorit ist bereits gespeichert.");return;
        }
      }
      JSONObject favorite=new JSONObject();
      favorite.put("favoriteId",UUID.randomUUID().toString());
      favorite.put("name",title);
      favorite.put("points",pts(meal));
      favorite.put("group",meal.optString("group","Snack"));
      favorite.put("foodId",id);
      favorite.put("quality",meal.optString("quality",""));
      favorite.put("referenceQuantity",reference);
      if(meal.has("referencePoints"))favorite.put("referencePoints",
          meal.optDouble("referencePoints"));
      favorite.put("quantityUnit",unit);
      if(meal.has("aiIngredients"))favorite.put("aiIngredients",
        new JSONArray(meal.getJSONArray("aiIngredients").toString()));
      if(meal.has("aiUncertainty"))favorite.put("aiUncertainty",
        meal.optString("aiUncertainty"));
      all.put(favorite);
      if(!prefs.edit().putString("favoriteMeals",all.toString()).commit())
        throw new IllegalStateException("Speichern fehlgeschlagen");
      msg("★ Favorit gespeichert. Die Menge ist beim nächsten Mal frei wählbar.");
    }catch(Exception e){msg("Favorit konnte nicht gespeichert werden.");}
  }

  private void saveFavorite(JSONObject meal){
    JSONObject food=findFoodById(meal.optString("foodId"));
    double reference=FitRocketFavoriteMath.referenceQuantity(meal,food);
    String unit=favoriteUnit(meal,food);
    if(Double.isFinite(reference)&&reference>0){
      actuallySaveFavorite(meal,reference,unit);return;
    }
    // Old diary entries without quantity must never silently become "100 g".
    LinearLayout box=new LinearLayout(this);box.setOrientation(1);
    box.setPadding(dp(20),dp(10),dp(20),dp(10));
    box.addView(label("Welche Menge hatte dieser Eintrag ursprünglich?",15));
    box.addView(label("Diese Referenzmenge wird zusammen mit den "+
      pt(pts(meal))+" Punkten gespeichert. Bei der nächsten Verwendung "+
      "kannst du die Menge frei wählen.",13));
    EditText quantity=edit("Ursprüngliche Menge in "+unit,"",8194);
    box.addView(quantity);
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Favorit mit variabler Menge")
      .setView(box).setPositiveButton("Speichern",null)
      .setNegativeButton("Abbrechen",null).create();
    dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(btn->{
      try{
        double amount=FitRocketFavoriteMath.positive(num(quantity.getText().toString()));
        actuallySaveFavorite(meal,amount,unit);dialog.dismiss();
      }catch(Exception e){msg("Bitte eine gültige ursprüngliche Menge eingeben.");}
    }));
    dialog.show();
  }

  private void removeFavorite(JSONObject favorite){
    JSONArray all=favoriteItems(),next=new JSONArray();
    for(int i=0;i<all.length();i++){
      JSONObject item=all.optJSONObject(i);
      if(item==null)continue;
      boolean same=!favorite.optString("favoriteId").isEmpty()
         ?favorite.optString("favoriteId").equals(item.optString("favoriteId"))
         :item.toString().equals(favorite.toString());
      if(!same)next.put(item);
    }
    prefs.edit().putString("favoriteMeals",next.toString()).apply();
    favoritesScreen();
  }

  private void bookFavorite(JSONObject favorite,String group,double reference,double quantity){
    try{
      double points=FitRocketFavoriteMath.scaledPointsPrecise(
        favorite,findFoodById(favorite.optString("foodId")),
        reference,quantity,foods);
      JSONObject entry=new JSONObject();
      String name=favorite.optString("name");
      String unit=favoriteUnit(favorite,findFoodById(favorite.optString("foodId")));
      entry.put("name",name+" ("+String.format(Locale.GERMAN,"%.1f",quantity)+" "+unit+")");
      entry.put("points",points);
      entry.put("group",group);
      entry.put("date",bookingDate==null?today():bookingDate);
      entry.put("quality",favorite.optString("quality","Favorit"));
      entry.put("foodId",favorite.optString("foodId",""));
      entry.put("baseName",name);
      entry.put("quantity",quantity);
      entry.put("quantityUnit",unit);
      JSONArray ingredients=FitRocketFavoriteMath.scaledIngredients(favorite,reference,quantity);
      if(ingredients!=null){
        entry.put("aiIngredients",ingredients);
        entry.put("aiUncertainty",favorite.optString("aiUncertainty",""));
      }
      meals.add(entry);persist("meals",meals);
      msg("Favorit mit "+pt(points)+" Punkten eingetragen.");
      afterMealAdded();
    }catch(Exception e){msg("Menge oder Nährwerte prüfen: "+e.getMessage());}
  }

  private void bookFavoriteDialog(JSONObject fav){
    final String[] groups={"Frühstück","Mittagessen","Abendessen","Snack","Getränke"};
    JSONObject food=findFoodById(fav.optString("foodId"));
    final String unit=favoriteUnit(fav,food);
    double known=FitRocketFavoriteMath.referenceQuantity(fav,food);
    boolean missing=!(Double.isFinite(known)&&known>0);
    LinearLayout form=new LinearLayout(this);form.setOrientation(1);
    form.setPadding(dp(20),dp(10),dp(20),dp(10));
    form.addView(label(fav.optString("name"),18));
    if(missing)form.addView(label("Alter Favorit ohne gespeicherte Menge: "+
      "Bitte einmal angeben, auf wie viel "+unit+" sich die "+
      pt(pts(fav))+" Punkte ursprünglich bezogen.",13));
    EditText base=edit("Referenzmenge "+unit,
      missing?"":String.format(Locale.GERMAN,"%.1f",known),8194);
    if(missing){form.addView(label("Ursprüngliche Menge ("+unit+")",13));form.addView(base);}
    form.addView(label("Wie viel möchtest du heute eintragen ("+unit+")?",14));
    EditText amount=edit("Menge "+unit,
      missing?"":String.format(Locale.GERMAN,"%.1f",known),8194);
    form.addView(amount);
    Spinner group=groupSpinner();
    for(int j=0;j<groups.length;j++)
      if(groups[j].equals(fav.optString("group","Snack")))group.setSelection(j);
    form.addView(label("Mahlzeit",13));form.addView(group);
    TextView preview=label("Punkte werden zur neuen Menge berechnet.",16);
    form.addView(preview);
    Runnable update=()->{
      try{
        double ref=missing?num(base.getText().toString()):
          FitRocketFavoriteMath.referenceQuantity(fav,food);
        double qty=num(amount.getText().toString());
        double points=FitRocketFavoriteMath.scaledPointsPrecise(fav,food,ref,qty,foods);
        preview.setText(pt(qty)+" "+unit+" → "+pt(points)+" P");
      }catch(Exception e){preview.setText("Bitte gültige Mengen eingeben.");}
    };
    TextWatcher watcher=new TextWatcher(){
      public void beforeTextChanged(CharSequence s,int a,int b,int c){}
      public void onTextChanged(CharSequence s,int a,int b,int c){update.run();}
      public void afterTextChanged(Editable s){}
    };
    amount.addTextChangedListener(watcher);
    if(missing)base.addTextChangedListener(watcher);
    update.run();
    ScrollView scroll=new ScrollView(this);scroll.addView(form);
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Favorit hinzufügen")
       .setView(scroll).setPositiveButton("Eintragen",null)
       .setNegativeButton("Abbrechen",null).create();
    dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(btn->{
      try{
        double ref=missing?FitRocketFavoriteMath.positive(num(base.getText().toString()))
          :FitRocketFavoriteMath.referenceQuantity(fav,food);
        double qty=FitRocketFavoriteMath.positive(num(amount.getText().toString()));
        FitRocketFavoriteMath.scaledPointsPrecise(fav,food,ref,qty,foods);
        if(missing){
          fav.put("referenceQuantity",ref);fav.put("quantityUnit",unit);
          JSONArray updated=favoriteItems();
          for(int i=0;i<updated.length();i++){
            JSONObject saved=updated.optJSONObject(i);
            if(saved==null)continue;
            boolean match=!fav.optString("favoriteId").isEmpty()
              ?fav.optString("favoriteId").equals(saved.optString("favoriteId"))
              :fav.optString("name").equals(saved.optString("name"))&&
                Math.abs(pts(fav)-pts(saved))<0.001;
            if(match){
              saved.put("referenceQuantity",ref);saved.put("quantityUnit",unit);
              break;
            }
          }
          if(!prefs.edit().putString("favoriteMeals",updated.toString()).commit())
            throw new IllegalStateException("Referenzmenge nicht gespeichert");
        }
        dialog.dismiss();
        bookFavorite(fav,group.getSelectedItem().toString(),ref,qty);
      }catch(Exception ex){msg("Bitte beide Mengen korrekt eingeben.");}
    }));
    dialog.show();
  }

  private void favoritesScreen(){
    shell("Favoriten");
    LinearLayout intro=card(LAV);
    intro.addView(label("★ Deine gespeicherten Mahlzeiten",19));
    intro.addView(label("Wähle bei jedem Eintrag erneut die Menge in Gramm oder "+
      "Millilitern. Die Punkte werden automatisch angepasst.",13));
    page.addView(intro);
    JSONArray list=favoriteItems();
    if(list.length()==0){
      LinearLayout empty=card(Color.WHITE);
      empty.addView(label("Noch keine Favoriten vorhanden.",15));
      empty.addView(label("Tippe im Tagebuch auf eine Mahlzeit und wähle "+
          "„Als Favorit speichern“.",13));
      page.addView(empty);return;
    }
    for(int i=0;i<list.length();i++){
      JSONObject fav=list.optJSONObject(i);if(fav==null)continue;
      JSONObject food=findFoodById(fav.optString("foodId"));
      double ref=FitRocketFavoriteMath.referenceQuantity(fav,food);
      String unit=favoriteUnit(fav,food);
      LinearLayout box=card(Color.WHITE);
      box.addView(label("★ "+fav.optString("name"),16));
      box.addView(label(fav.optString("group","Snack")+
        (Double.isFinite(ref)?" · bisher "+String.format(Locale.GERMAN,"%.0f",ref)+
          " "+unit+" / "+pt(pts(fav))+" P":" · Referenzmenge einmal festlegen"),12));
      action(box,"Menge wählen und eintragen",()->bookFavoriteDialog(fav));
      action(box,"Aus Favoriten entfernen",()->new AlertDialog.Builder(this)
        .setTitle("Favorit entfernen?")
        .setMessage("Bisherige Tagebucheinträge bleiben erhalten.")
        .setPositiveButton("Entfernen",(d,w)->removeFavorite(fav))
        .setNegativeButton("Abbrechen",null).show());
      page.addView(box);
    }
  }

  private void weeklyReview(){
    shell("Wochenrückblick");
    JSONObject report=FitRocketInsights.week(LocalDate.now(),budget,waterGoal(),
      safeArray("meals"),waterDays());
    LinearLayout overall=card(LAV);
    TextView headline=label("📊 Dein Wochenrückblick",21);
    headline.setTypeface(null,1);overall.addView(headline);
    overall.addView(label(pretty(report.optString("from"))+" bis "+
      pretty(report.optString("through")),13));
    page.addView(overall);

    LinearLayout graph=card(Color.WHITE);
    TextView graphTitle=label("Punkte pro Tag",17);
    graphTitle.setTypeface(null,1);graph.addView(graphTitle);
    graph.addView(new FitRocketWeekChart(this,report.optJSONArray("daysDetail"),budget),
      new LinearLayout.LayoutParams(-1,dp(207)));
    TextView graphCaption=label("Die Linie zeigt dein Tagesbudget. Tage ohne Eintrag "+
        "werden nicht als Null-Punkte-Tage bewertet.",12);
    graphCaption.setTextColor(muted());graph.addView(graphCaption);
    page.addView(graph);

    LinearLayout totals=card(Color.WHITE);
    totals.addView(label("⭐ Dein Wochenkonto",17));
    totals.addView(label(pt(availableWeek())+" P verfügbar",22));
    totals.addView(label(weekly+" P Start   +"+pt(rolloversWeek())+
      " P Übertrag   +"+activityCredit()+" P Bewegung",13));
    totals.addView(label("−"+pt(spentWeek())+" P verwendet",13));
    page.addView(totals);

    LinearLayout water=card(Color.WHITE);
    water.addView(label("💧 Trinken",17));
    water.addView(label(report.optInt("waterGoalDays")+" von "+report.optInt("days")+
      " erfassten Kalendertagen mit erreichtem Trinkziel",14));
    water.addView(label(String.format(Locale.GERMAN,"%.2f Liter insgesamt",
      report.optInt("waterTotal")/1000.0),13));
    page.addView(water);

    heading("Die einzelnen Tage");
    JSONArray rows=report.optJSONArray("daysDetail");
    for(int i=0;rows!=null&&i<rows.length();i++){
      JSONObject day=rows.optJSONObject(i);if(day==null)continue;
      LinearLayout item=card(Color.WHITE);
      item.addView(label(pretty(day.optString("date"))+" · "+
        (day.optBoolean("logged")?pt(day.optDouble("points"))+" P gegessen":"Kein Mahlzeiteneintrag")+
        " · "+String.format(Locale.GERMAN,"%.2f L",day.optInt("waterMl")/1000.0),13));
      page.addView(item);
    }
  }

  private void waterReminderSettings(){
    LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(20),dp(8),dp(20),dp(8));
    CheckBox enable=new CheckBox(this);enable.setText("Wassererinnerungen einschalten");
    enable.setChecked(prefs.getBoolean("waterReminderEnabled",false));
    box.addView(enable);
    int start=prefs.getInt("waterReminderStart",9),end=prefs.getInt("waterReminderEnd",20);
    int hours=prefs.getInt("waterReminderInterval",2);
    box.addView(label("Erinnerungszeitraum (ganze Stunden, ohne Nachtruhe)",13));
    EditText first=edit("Beginn (0–22)",String.valueOf(start),2);
    EditText last=edit("Ende (1–23)",String.valueOf(end),2);
    EditText every=edit("Intervall in Stunden (1–6)",String.valueOf(hours),2);
    box.addView(label("Beginn (Uhrzeit)",13));box.addView(first);
    box.addView(label("Ende (Uhrzeit)",13));box.addView(last);
    box.addView(label("Alle wie viele Stunden?",13));box.addView(every);
    box.addView(label("Die Erinnerungen sind freiwillig. Android kann sie aus "
      +"Energiespargründen etwas verzögert anzeigen.",12));
    ScrollView scroll=new ScrollView(this);scroll.addView(box);
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("💧 Wassererinnerungen")
      .setView(scroll).setPositiveButton("Speichern",null)
      .setNegativeButton("Abbrechen",null).create();
    dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(btn->{
      try{
        int a=Integer.parseInt(first.getText().toString().trim());
        int b=Integer.parseInt(last.getText().toString().trim());
        int c=Integer.parseInt(every.getText().toString().trim());
        if(a<0||a>22||b<=a||b>23||c<1||c>6)throw new Exception();
        prefs.edit().putBoolean("waterReminderEnabled",enable.isChecked())
          .putInt("waterReminderStart",a)
          .putInt("waterReminderEnd",b)
          .putInt("waterReminderInterval",c).apply();
        FitRocketWaterReminder.schedule(this);
        if(enable.isChecked()&&Build.VERSION.SDK_INT>=33&&
            checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=
              android.content.pm.PackageManager.PERMISSION_GRANTED){
          requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},1301);
        }
        dialog.dismiss();profilePage();
      }catch(Exception e){msg("Bitte gültige Uhrzeiten und Intervalle eingeben.");}
    }));
    dialog.show();
  }

  private void toggleUpdateChecks(){
    boolean newValue=!prefs.getBoolean("updateChecks",true);
    prefs.edit().putBoolean("updateChecks",newValue).apply();
    profilePage();
  }

  private void maybeCheckUpdatesOnLaunch(){
    if(!prefs.getBoolean("updateChecks",true))return;
    long last=prefs.getLong("updateCheckTimestamp",0),now=System.currentTimeMillis();
    if(now-last<86400000L)return;
    prefs.edit().putLong("updateCheckTimestamp",now).apply();
    FitRocketUpdate.check(this,new FitRocketUpdate.Callback(){
      public void onSuccess(FitRocketUpdate.Release release){
        try{
          if(!FitRocketUpdate.newer(release.version,installedVersion()))return;
          new AlertDialog.Builder(MainActivity.this)
            .setTitle("FitRocket "+release.version+" verfügbar")
            .setMessage("Eine neue signierte Version ist auf GitHub verfügbar. "
                +"Möchtest du die Änderungen ansehen?")
            .setPositiveButton("Update ansehen",(d,w)->checkForUpdates())
            .setNegativeButton("Später",null).show();
        }catch(Exception ignored){}
      }
      public void onFailure(String message){
        // Background discovery is unobtrusive; manual checks show meaningful errors.
      }
    });
  }

  private void progressPage(){
    shell("Fortschritt");
    LinearLayout score=card(LAV);
    TextView week=label("⭐ "+pt(availableWeek())+" Wochenpunkte",21);week.setTypeface(null,1);
    score.addView(week);
    score.addView(label(weekly+" Start · +"+pt(rolloversWeek())+" gespart · +"+
        activityCredit()+" Bewegung · −"+pt(spentWeek())+" verbraucht",13));
    page.addView(score);
    LinearLayout recap=card(Color.WHITE);
    recap.addView(label("📊 Diese Woche im Überblick",18));
    JSONObject stats=FitRocketInsights.week(LocalDate.now(),budget,waterGoal(),
       safeArray("meals"),waterDays());
    recap.addView(new FitRocketWeekChart(this,stats.optJSONArray("daysDetail"),budget),
       new LinearLayout.LayoutParams(-1,dp(190)));
    TextView hint=label("Balken: gegessene Punkte · Strichlinie: Tagesziel",12);
    hint.setTextColor(muted());recap.addView(hint);
    action(recap,"Vollständigen Wochenrückblick ansehen",()->weeklyReview());
    page.addView(recap);

    LinearLayout weightCard=card(Color.WHITE);
    double latest=latestWeight(),goal=goalWeight(),first=earliestWeight();
    TextView weightTitle=label("⚖ Gewicht",17);weightTitle.setTypeface(null,1);
    weightCard.addView(weightTitle);
    weightCard.addView(label(latest>0
       ?String.format(Locale.GERMAN,"Aktuell %.1f kg",latest)
       :"Noch keine Messung",16));
    if(goal>0)weightCard.addView(label(String.format(Locale.GERMAN,"Ziel %.1f kg",goal),13));
    if(first>0 && latest>0)
      weightCard.addView(label(String.format(Locale.GERMAN,"Veränderung %+.1f kg",latest-first),13));
    Double average=FitRocketInsights.movingAverage(weights,LocalDate.now(),7);
    Double change=FitRocketInsights.periodChange(weights,LocalDate.now(),30);
    if(average!=null)weightCard.addView(label(String.format(Locale.GERMAN,
       "7-Tage-Durchschnitt: %.1f kg",average),13));
    if(change!=null)weightCard.addView(label(String.format(Locale.GERMAN,
       "Monatsvergleich (7-Tage-Mittel): %+.1f kg",change),13));
    if(!weights.isEmpty())
      weightCard.addView(new FitRocketWeightChart(this,weights,goal>0?goal:null),
          new LinearLayout.LayoutParams(-1,dp(185)));
    settingsRow(weightCard,"Gewichtsverlauf ansehen","Messung eintragen und Ziel festlegen",()->weightScreen());
    page.addView(weightCard);

    LinearLayout activity=card(Color.WHITE);
    activity.addView(label("🚶 Bewegung & Auswertung",17));
    settingsRow(activity,"Aktivitäten & Smartwatch",earnedWeek()+" Bewegungspunkte diese Woche",()->activityPage());
    settingsRow(activity,"Ernährungsstatistik","Wochen- und Monatsauswertung",()->statistics());
    settingsRow(activity,"Wasserbilanz",
      String.format(Locale.GERMAN,"Heute %.2f von %.2f L",waterMl(today())/1000.0,waterGoal()/1000.0),
      ()->waterScreen());
    page.addView(activity);
  }

  private void profilePage(){
    shell("Profil");
    LinearLayout identity=card(Color.WHITE);
    LinearLayout top=new LinearLayout(this);top.setOrientation(0);
    top.setGravity(Gravity.CENTER_VERTICAL);
    TextView avatar=chip("🌿",LAV);avatar.setTextSize(28);
    avatar.setGravity(Gravity.CENTER);
    top.addView(avatar,new LinearLayout.LayoutParams(dp(61),dp(61)));
    LinearLayout summary=new LinearLayout(this);summary.setOrientation(1);
    String name=prefs.getString("profile_name","").trim();
    TextView display=label(name.isEmpty()?"Dein Profil":name,21);display.setTypeface(null,1);
    summary.addView(display);
    TextView description=label(profileReady()?"Dein persönliches Bewegungsprofil":"Richte deine persönlichen Daten ein",12);
    description.setTextColor(muted());summary.addView(description);
    top.addView(summary,new LinearLayout.LayoutParams(0,-2,1));identity.addView(top);
    settingsRow(identity,"Profilname bearbeiten","Name nur auf diesem Gerät",()->profileNameDialog());
    page.addView(identity);

    LinearLayout personal=card(Color.WHITE);
    personal.addView(label("Meine Ziele",18));
    settingsRow(personal,"Körperdaten & Aktivitätsprofil","Alter, Größe und Gewicht",()->profileDialog());
    settingsRow(personal,"Gewichtsziel",goalWeight()>0?
      String.format(Locale.GERMAN,"%.1f kg",goalWeight()):"Zielgewicht festlegen",()->weightScreen());
    settingsRow(personal,"Punktebudget",
      budget+" pro Tag · "+weekly+" zum Wochenstart",()->settingsDialog());
    page.addView(personal);

    LinearLayout appearance=card(Color.WHITE);
    appearance.addView(label("Darstellung",18));
    String mode=FitRocketTheme.normalized(prefs.getString(FitRocketTheme.KEY,FitRocketTheme.SYSTEM));
    settingsRow(appearance,"Hell-/Dunkelmodus",
      FitRocketTheme.DARK.equals(mode)?"Dunkel":
        FitRocketTheme.LIGHT.equals(mode)?"Hell":"Systemeinstellung",()->themeDialog());
    settingsRow(appearance,"Erklärhinweise",
      showHints()?"An – kurze Erläuterungen werden angezeigt":"Aus – dauerhaft ausgeblendet",
      ()->toggleHints());
    page.addView(appearance);

    LinearLayout integrations=card(Color.WHITE);
    integrations.addView(label("KI & Verbindungen",18));
    settingsRow(integrations,"KI-Assistent & Vorlieben",
      selectedAiProviderName()+" · Rezeptideen und Schätzungen",()->aiHub());
    settingsRow(integrations,"Smartwatch & Health Connect",
      "Schritte und Training synchronisieren",()->activityPage());
    settingsRow(integrations,"Wassererinnerungen",
      prefs.getBoolean("waterReminderEnabled",false)?"Aktiv · mit Ruhezeiten":"Ausgeschaltet",
      ()->waterReminderSettings());
    page.addView(integrations);

    LinearLayout storage=card(Color.WHITE);
    storage.addView(label("Daten & Sicherheit",18));
    settingsRow(storage,"Backup exportieren","Dein Tagebuch lokal sichern",()->startExport());
    settingsRow(storage,"Backup importieren","Mit Vorschau und Datenprüfung",()->startImport());
    settingsRow(storage,"Favoriten & Mahlzeiten","Gespeicherte Gerichte erneut eintragen",()->favoritesScreen());
    page.addView(storage);

    LinearLayout about=card(Color.WHITE);
    about.addView(label("App & Hilfe",18));
    settingsRow(about,"FAQ – Häufige Fragen","Portionen, Punkte, Favoriten, KI und Updates",
      ()->faqScreen());
    settingsRow(about,"In-App-Update prüfen","Direkt herunterladen · Installieren bestätigen · v"+installedVersion(),()->checkForUpdates());
    settingsRow(about,"Automatische Updateprüfung",
       prefs.getBoolean("updateChecks",true)?"Bei App-Start gelegentlich prüfen":"Deaktiviert",
       ()->toggleUpdateChecks());
    settingsRow(about,"Neueste APK & Release-Notizen","GitHub Releases öffnen",
        ()->openSafeUrl(FitRocketUpdate.REPO+"/releases/latest"));
    settingsRow(about,"FitRocket-Projektseite","Informationen und Quellcode",
        ()->openSafeUrl(FitRocketUpdate.REPO));
    page.addView(about);
  }

  private void toggleHints(){
    boolean old=showHints();
    new AlertDialog.Builder(this).setTitle("Erklärhinweise")
      .setMessage(old?"Kurze Erklärungstexte dauerhaft ausblenden? Die FAQ bleibt verfügbar.":
        "Kurze Erklärungstexte in der App wieder anzeigen?")
      .setPositiveButton(old?"Ausblenden":"Anzeigen",(d,w)->{
        prefs.edit().putBoolean("showHints",!old).apply();profilePage();
      }).setNegativeButton("Abbrechen",null).show();
  }

  private void faqScreen(){
    shell("Hilfe & FAQ");
    LinearLayout intro=card(LAV);
    intro.addView(label("❔ Häufige Fragen",20));
    intro.addView(label("Tippe auf eine Frage für die Antwort.",13));page.addView(intro);
    final String[][] faq={
      {"Portion oder Gramm – wo ist der Unterschied?",
       "Im Lebensmitteldialog wählst du zwischen „Portionen (schnell)“ und „Menge in g/ml (genau)“. Die Portion nutzt die hinterlegte Referenz, bei Gramm bestimmst du die genaue Menge."},
      {"Kann ich 0,2 FitRocket-Punkte buchen?",
       "Ja. FitRocket rechnet bei Mengen proportional und speichert die Punkte auf 0,1 genau. Beispiel: 100 g = 1 P, 20 g = 0,2 P. Das Tagesbudget und das Wochenkonto berücksichtigen Zehntelpunkte."},
      {"Wie funktioniert eine Handvoll Nüsse?",
       "Beispielsweise kann 1 Handvoll = 25 g als Referenz gespeichert sein. Du kannst 1 Portion oder direkt 10 g wählen. Die neue Punktzahl wird proportional berechnet."},
      {"Was ist, wenn das Portionsgewicht unbekannt ist?",
       "Dann fragt FitRocket beim Wechsel zur Gramm-Eingabe nach dem Gewicht der Referenzportion. Diese Menge wird nicht geschätzt."},
      {"Was bedeutet 0 Punkte für Basislebensmittel?",
       "Die hinterlegten Basislebensmittel haben 0 Punkte. Zusätze wie Butter, Zucker, Käse und Öl müssen separat eingetragen werden."},
      {"Wie funktionieren Favoriten?",
       "Tippe im Tagebuch auf einen Eintrag und wähle „Als Favorit speichern“. Beim nächsten Hinzufügen kannst du die Menge ändern und die Punkte werden neu berechnet."},
      {"Wie lösche ich einen falschen Eintrag?",
       "Wische den Tagebucheintrag nach links und bestätige das Löschen. Eigene KI- oder Barcode-Produkte kannst du auch in der Suche entfernen."},
      {"Wie funktioniert das Wochenkonto?",
       "Das Wochenkonto startet montags mit 28 Punkten. Von einem abgeschlossenen Tag werden bis zu 4 ungenutzte Tagespunkte übertragen. Bewegungspunkte können optional hinzukommen."},
      {"Wie zuverlässig ist die KI?",
       "Sie schätzt Zutaten, Mengen und Nährwerte, kann sich aber irren. Kontrolliere alles vor dem Speichern. Die Punkte berechnet FitRocket lokal."},
      {"Wie funktioniert die Wasseranzeige?",
       "Ein Glas entspricht 250 ml. Du kannst Gläser antippen, über Plus und Minus ändern und dein Trinkziel individuell anpassen."},
      {"Wie aktualisiere ich FitRocket?",
       "Unter Profil → App & Hilfe → In-App-Update prüfen wird die signierte APK von GitHub heruntergeladen. Die Installation muss über den Android-Dialog bestätigt werden."},
      {"Bleiben meine Daten bei Updates?",
       "Ein normal signiertes Update erhält die Daten. Vor großen Änderungen empfiehlt sich ein JSON-Backup über Profil → Daten & Sicherheit."},
      {"Wie sichere ich mein Tagebuch?",
       "Profil → Daten & Sicherheit → Backup exportieren. Die JSON-Datei separat aufbewahren. API-Schlüssel werden nicht exportiert."},
      {"Wie aktiviere ich Erklärungstexte wieder?",
       "Profil → Darstellung → Erklärhinweise. Deine Wahl bleibt beim Neustart erhalten."},
      {"Woher kommen die Smartwatch-Werte?",
       "Health Connect kann Schritte und Training aus verbundenen Apps zusammenführen, wenn du die Berechtigung ausdrücklich erteilst."}
    };
    for(String[] item:faq){
      LinearLayout row=card(Color.WHITE);
      settingsRow(row,item[0],"Antwort ansehen",()->new AlertDialog.Builder(this)
        .setTitle(item[0]).setMessage(item[1])
        .setPositiveButton("Schließen",null).show());
      page.addView(row);
    }
  }

  private void profileNameDialog(){
    LinearLayout content=new LinearLayout(this);content.setOrientation(1);
    content.setPadding(dp(18),dp(8),dp(18),dp(8));
    content.addView(label("Wie soll dich FitRocket nennen?",14));
    EditText name=edit("Name (optional)",prefs.getString("profile_name",""),1);
    content.addView(name);
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Profilname")
      .setView(content).setNegativeButton("Abbrechen",null)
      .setPositiveButton("Speichern",null).create();
    dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(btn->{
      String value=name.getText().toString().trim();
      if(value.length()>40){msg("Bitte maximal 40 Zeichen verwenden.");return;}
      prefs.edit().putString("profile_name",value).apply();dialog.dismiss();profilePage();
    }));
    dialog.show();
  }

  private void themeDialog(){
    String[] choices={"Systemeinstellung","Hell","Dunkel"};
    String saved=FitRocketTheme.normalized(prefs.getString(FitRocketTheme.KEY,FitRocketTheme.SYSTEM));
    int chosen=FitRocketTheme.DARK.equals(saved)?2:FitRocketTheme.LIGHT.equals(saved)?1:0;
    new AlertDialog.Builder(this).setTitle("Darstellung")
      .setSingleChoiceItems(choices,chosen,(d,which)->{
        String value=which==2?FitRocketTheme.DARK:
             which==1?FitRocketTheme.LIGHT:FitRocketTheme.SYSTEM;
        d.dismiss();
        if(!value.equals(saved)){
          prefs.edit().putString(FitRocketTheme.KEY,value).apply();
          currentScreen="Profil";recreate();
        }
      }).setNegativeButton("Abbrechen",null).show();
  }

  private String installedVersion(){
    try{
      return getPackageManager().getPackageInfo(getPackageName(),0).versionName;
    }catch(Exception e){return "0.0.0";}
  }

  private void openSafeUrl(String target){
    if(!(target.equals(FitRocketUpdate.REPO) ||
        target.startsWith(FitRocketUpdate.REPO+"/releases/"))){
      msg("Unbekannter Download-Link.");return;
    }
    try{
      startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(target)));
    }catch(Exception e){msg("Kein Browser gefunden.");}
  }

  private boolean waitingForUpdatePermission=false;

  private void installDownloadedUpdate(java.io.File apk){
    if(!FitRocketInAppUpdate.hasInstallerPermission(this)){
      waitingForUpdatePermission=true;
      new AlertDialog.Builder(this).setTitle("Installation erlauben")
        .setMessage("Android muss FitRocket einmalig erlauben, Updates aus dieser App "
          +"zu installieren. Die neue APK wurde bereits innerhalb von FitRocket "
          +"heruntergeladen und auf App-Identität, Versionsnummer und Signatur geprüft. "
          +"Aktiviere „Aus dieser Quelle zulassen“ und kehre zur App zurück. "
          +"Android fragt danach vor der eigentlichen Installation nochmals um Zustimmung.")
        .setPositiveButton("Berechtigung öffnen",(d,w)->{
          try{FitRocketInAppUpdate.openPermissionSettings(this);}
          catch(Exception ex){
            waitingForUpdatePermission=false;
            msg("Android-Einstellungen konnten nicht geöffnet werden.");
          }
        })
        .setNegativeButton("Abbrechen",(d,w)->waitingForUpdatePermission=false)
        .show();
      return;
    }
    waitingForUpdatePermission=false;
    try{
      FitRocketInAppUpdate.launchInstaller(this,apk);
    }catch(Exception ex){
      new AlertDialog.Builder(this).setTitle("Update konnte nicht gestartet werden")
        .setMessage(ex.getMessage()==null?"Unbekannter Fehler.":ex.getMessage())
        .setPositiveButton("OK",null).show();
    }
  }

  private void downloadAndInstallUpdate(FitRocketUpdate.Release release){
    if(release.apkUrl.isEmpty()){
      new AlertDialog.Builder(this).setTitle("Kein APK-Download vorhanden")
        .setMessage("Das Release enthält keine passende FitRocket.apk. "
          +"Bitte die offizielle Release-Seite prüfen.")
        .setPositiveButton("Release öffnen",(d,w)->openSafeUrl(release.pageUrl))
        .setNegativeButton("Schließen",null).show();
      return;
    }
    ProgressDialog loading=new ProgressDialog(this);
    loading.setTitle("FitRocket "+release.version+" herunterladen");
    loading.setMessage("Sicheres Update wird direkt in der App geladen …");
    loading.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    loading.setMax(100);
    loading.setIndeterminate(true);
    loading.setCancelable(false);
    loading.show();
    FitRocketInAppUpdate.download(this,release,new FitRocketInAppUpdate.Callback(){
      @Override public void onProgress(int percent){
        if(!loading.isShowing())return;
        if(percent>=0){loading.setIndeterminate(false);loading.setProgress(percent);}
        else loading.setIndeterminate(true);
      }
      @Override public void onReady(java.io.File apk){
        loading.dismiss();
        new AlertDialog.Builder(MainActivity.this)
          .setTitle("Update ist bereit")
          .setMessage("FitRocket "+release.version+" wurde heruntergeladen. "
            +"Die APK gehört zu FitRocket und wurde vorab mit der "
            +"Signatur der installierten App verglichen. "
            +"Android benötigt noch deine Bestätigung zur Installation. "
            +"Deine App-Daten bleiben bei einem regulären Update erhalten.")
          .setPositiveButton("Jetzt installieren",(d,w)->installDownloadedUpdate(apk))
          .setNegativeButton("Später",null).show();
      }
      @Override public void onFailure(String reason){
        loading.dismiss();
        new AlertDialog.Builder(MainActivity.this)
          .setTitle("Update konnte nicht geladen werden")
          .setMessage(reason)
          .setPositiveButton("Release öffnen",(d,w)->openSafeUrl(release.pageUrl))
          .setNegativeButton("Schließen",null).show();
      }
    });
  }

  private void checkForUpdates(){
    AlertDialog loading=new AlertDialog.Builder(this)
      .setTitle("Updateprüfung").setMessage("Prüfe das aktuelle GitHub Release …")
      .setCancelable(false).create();loading.show();
    FitRocketUpdate.check(this,new FitRocketUpdate.Callback(){
      @Override public void onSuccess(FitRocketUpdate.Release release){
        loading.dismiss();
        boolean update;
        try{update=FitRocketUpdate.newer(release.version,installedVersion());}
        catch(Exception e){msg("Versionsvergleich fehlgeschlagen.");return;}
        if(!update){
          new AlertDialog.Builder(MainActivity.this)
            .setTitle("FitRocket ist aktuell")
            .setMessage("Installiert: v"+installedVersion()+
               "\nNeueste Veröffentlichung: "+release.version)
            .setPositiveButton("OK",null)
            .setNeutralButton("Release öffnen",(d,w)->openSafeUrl(release.pageUrl)).show();
          return;
        }
        String notes=release.notes.isEmpty()?"Neues Update verfügbar.":release.notes;
        new AlertDialog.Builder(MainActivity.this)
          .setTitle("Update "+release.version+" verfügbar")
          .setMessage("Installiert: v"+installedVersion()+
            "\n\n"+notes+"\n\nFitRocket kann die neue APK jetzt direkt herunterladen und "
            +"den Android-Installationsdialog öffnen. Kein Browser oder manuelles "
            +"Suchen einer APK nötig. Android benötigt weiterhin deine Zustimmung. "
            +"Ein Backup vor einem größeren Update ist empfehlenswert.")
          .setPositiveButton("In FitRocket aktualisieren",(d,w)->downloadAndInstallUpdate(release))
          .setNeutralButton("Release ansehen",(d,w)->openSafeUrl(release.pageUrl))
          .setNegativeButton("Später",null).show();
      }
      @Override public void onFailure(String message){
        loading.dismiss();
        new AlertDialog.Builder(MainActivity.this).setTitle("Updateprüfung")
          .setMessage(message)
          .setPositiveButton("GitHub öffnen",(d,w)->
            openSafeUrl(FitRocketUpdate.REPO+"/releases"))
          .setNegativeButton("Schließen",null).show();
      }
    });
  }

  private void waterScreen(){
    shell("Trinken");heading("Deine Wasserbilanz");
    page.addView(waterCard(today()));
    if(showHints())page.addView(label("Jedes Glas zählt 250 ml. Du bestimmst dein Tagesziel selbst.",13));
    LinearLayout reminder=card(Color.WHITE);
    settingsRow(reminder,"Wassererinnerungen",
       prefs.getBoolean("waterReminderEnabled",false)?"Eingeschaltet":"Ausgeschaltet",
       ()->waterReminderSettings());page.addView(reminder);
  }
  private void settingsDialog(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(25,5,25,5);EditText b=edit("Tagesbudget",String.valueOf(budget),2),w=edit("Wochenstart-Punkte",String.valueOf(weekly),2);
    l.addView(label("Tagesbudget (persönlich festlegen)",14));l.addView(b);
    l.addView(label("Wochenstart-Punkte (Standard 28)",14));l.addView(w);
    hint(l,"Montags startet das Wochenkonto neu; maximal 4 P Übertrag pro Tag.");new AlertDialog.Builder(this).setTitle("Budget").setView(l).setPositiveButton("Speichern",(d,n)->{try{int bb=Integer.parseInt(b.getText().toString()),ww=Integer.parseInt(w.getText().toString());if(bb<1||bb>200||ww<0||ww>500)throw new Exception();budget=bb;weekly=ww;prefs.edit().putInt("budget",bb).putInt("weekly",ww).apply();more();}catch(Exception e){msg("Ungültige Budgetwerte.");}}).setNegativeButton("Abbrechen",null).show();}
  private void statistics(){shell("Statistiken");LocalDate now=LocalDate.now(),start=now.minusDays(now.getDayOfWeek().getValue()-1);page.addView(label("Diese Woche",20));double weeklyUsed=0;for(int i=0;i<7;i++){LocalDate date=start.plusDays(i);double n=used(date.toString());weeklyUsed+=n;page.addView(label(date.getDayOfWeek().toString().substring(0,2)+" "+pretty(date.toString())+": "+pt(n)+" Punkte",15));}page.addView(label("Gesamt: "+pt(weeklyUsed)+" Punkte; Ø "+String.format(Locale.GERMAN,"%.1f",weeklyUsed/7.0)+" pro Kalendertag",15));page.addView(label("Dieser Monat",20));LocalDate first=now.withDayOfMonth(1);double total=0;int tracked=0,days=now.getDayOfMonth();for(int i=0;i<days;i++){String day=first.plusDays(i).toString();total+=used(day);if(count(day)>0)tracked++;}page.addView(label("Erfasste Punkte: "+pt(total)+" · Tage mit Einträgen: "+tracked+" / "+days,16));page.addView(label("Wochenkonto: "+weekly+" Start + "+pt(rolloversWeek())+" Tagesübertrag + "
   +activityCredit()+" Bewegungsbonus − "+pt(spentWeek())+" verbraucht = "+pt(availableWeek())+" Punkte übrig",15));page.addView(label("Ø je dokumentiertem Tag: "+(tracked>0?String.format(Locale.GERMAN,"%.1f",total/(double)tracked):"–"),16));if(showHints())page.addView(label("Achtung: Fehlende Tage gelten nicht als Null-Punkte-Tage.",13));if(!weights.isEmpty()){page.addView(label("Gewichtseinträge: "+weights.size(),16));for(int i=weights.size()-1;i>=Math.max(0,weights.size()-8);i--)page.addView(label(pretty(weights.get(i).optString("date"))+" · "+weights.get(i).optString("kg")+" kg",14));}}
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
    Double weeklyAverage=FitRocketInsights.movingAverage(weights,LocalDate.now(),7);
    Double monthlyShift=FitRocketInsights.periodChange(weights,LocalDate.now(),30);
    if(weeklyAverage!=null)summaryCard.addView(label(String.format(Locale.GERMAN,
      "Ø letzte 7 Tage: %.1f kg",weeklyAverage),15));
    if(monthlyShift!=null)summaryCard.addView(label(String.format(Locale.GERMAN,
      "Vergleich zum Vormonat (geglättet): %+.1f kg",monthlyShift),14));
    summaryCard.addView(label("Durchschnitt nur aus tatsächlich eingetragenen Messungen.",12));
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
        d.dismiss();profilePage();
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
  private double spentWeek(){
    return FitRocketPointLedger.weekSpent(LocalDate.now(),budget,d->used(d.toString()));
  }
  private double rolloversWeek(){
    return FitRocketPointLedger.weekRollovers(LocalDate.now(),budget,d->used(d.toString()));
  }
  private int activityCredit(){return FitRocketMath.credit(earnedWeek(),prefs.getInt("activityShare",100));}
  private double availableWeek(){
    return FitRocketPointLedger.weeklyAvailable(weekly,rolloversWeek(),activityCredit(),spentWeek());
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
    overview.addView(label("Wochenkonto: "+weekly+" Start + "+pt(rolloversWeek())+" Übertrag + "
        +activityCredit()+" Bewegung − "+pt(spentWeek())+" verbraucht = "+pt(availableWeek())+" übrig",14));
    overview.addView(label("Täglich höchstens 4 ungenutzte Punkte, Tagesüberträge erst am Folgetag; Bewegungspunkte zählen sofort. Jeden Montag beginnt das Konto neu.",12));
    overview.addView(label("Tagesbudget bleibt: "+budget+" Punkte",13));page.addView(overview);
    LinearLayout transfers=card(Color.WHITE);
    transfers.addView(label("🌟 Tagesüberträge dieser Woche",17));
    LocalDate startDay=weekStart(),currentDay=LocalDate.now();
    boolean daysFinished=false;
    for(LocalDate day=startDay;day.isBefore(currentDay);day=day.plusDays(1)){
      daysFinished=true;
      double saved=FitRocketPointLedger.dailyTransfer(budget,used(day.toString()));
      transfers.addView(label(pretty(day.toString())+" · +"+pt(saved)+" / 4 P",14));
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
    int chosen=prefs.getInt("activityShare",100);share.setSelection(chosen==100?2:chosen==50?1:0);
    settings.addView(label("Training und Schritte werden am selben Tag dem Wochenkonto gutgeschrieben (bei 100 %).",12));
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
    FitRocketWidget.refresh(this);
    if(waitingForUpdatePermission && FitRocketInAppUpdate.hasInstallerPermission(this)){
      waitingForUpdatePermission=false;
      java.io.File apk=FitRocketInAppUpdate.updateFile(this);
      // Called only after an explicit user-requested update permission flow.
      installDownloadedUpdate(apk);
    }
    if(prefs!=null && lastRenderedDate!=null && !today().equals(lastRenderedDate)){
      if("Tagebuch".equals(currentScreen)){
        // When midnight passes while today's diary is open, show the new day.
        // Preserve a deliberately selected historical date.
        if(lastRenderedDate.equals(diaryDate.toString()))diaryDate=LocalDate.now();
        history();
      }else if("Aktivität".equals(currentScreen))activityPage();
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
          else if("Tagebuch".equals(currentScreen))history();
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
    if(result!=RESULT_OK||data==null)return;
    if(request==85){
      android.os.Bundle extras=data.getExtras();
      android.graphics.Bitmap bitmap=extras==null?null:(android.graphics.Bitmap)extras.get("data");
      if(bitmap==null){msg("Die Kamera hat kein Foto geliefert.");return;}
      ByteArrayOutputStream bytes=new ByteArrayOutputStream();
      bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG,80,bytes);
      applyRecipePhoto(android.util.Base64.encodeToString(bytes.toByteArray(),android.util.Base64.NO_WRAP));
      return;
    }
    if(data.getData()==null)return;
    Uri uri=data.getData();
    if(request==84){
      try(InputStream photo=getContentResolver().openInputStream(uri)){
        applyRecipePhoto(encodedRecipePhoto(photo));
      }catch(Exception e){msg("Bild konnte nicht geladen werden.");}
      return;
    }
    if(request==EXPORT_REQUEST){
      try(OutputStream os=getContentResolver().openOutputStream(uri)){
        JSONObject payload=new JSONObject();
        payload.put("schemaVersion",7);payload.put("budget",budget);payload.put("weekly",weekly);
        payload.put("profileName",prefs.getString("profile_name",""));
        payload.put("appearance",prefs.getString(FitRocketTheme.KEY,FitRocketTheme.SYSTEM));
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
        payload.put("aiFoods",new JSONArray(prefs.getString("aiFoods","[]")));
        JSONArray backedRecipes=new JSONArray(prefs.getString("savedAiRecipes","[]"));
        for(int i=0;i<backedRecipes.length();i++){
          JSONObject recipe=backedRecipes.optJSONObject(i);
          if(recipe!=null && !recipe.has("imageData") && recipe.has("imageUri")){
            try(InputStream photo=getContentResolver().openInputStream(Uri.parse(recipe.optString("imageUri")))){
              recipe.put("imageData",encodedRecipePhoto(photo));
              recipe.remove("imageUri");
            }catch(Exception ignored){/* Old missing photos cannot be recovered */ }
          }
        }
        payload.put("savedAiRecipes",backedRecipes);
        payload.put("favoriteMeals",favoriteItems());
        payload.put("waterReminderEnabled",prefs.getBoolean("waterReminderEnabled",false));
        payload.put("waterReminderStart",prefs.getInt("waterReminderStart",9));
        payload.put("waterReminderEnd",prefs.getInt("waterReminderEnd",20));
        payload.put("waterReminderInterval",prefs.getInt("waterReminderInterval",2));
        payload.put("updateChecks",prefs.getBoolean("updateChecks",true));
        payload.put("showHints",showHints());
        payload.put("aiPreferences",prefs.getString("aiPreferences",""));
        if(goalWeight()>0)payload.put("weightGoal",goalWeight());
        payload.put("activityShare",prefs.getInt("activityShare",100));
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
        while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>90000000)throw new IOException("Backup zu groß");}
        JSONObject payload=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));
        String backupPreview=FitRocketBackupCheck.preview(payload);
        int schema=payload.optInt("schemaVersion",0);
        if((schema<1||schema>7)||!payload.has("meals")||!payload.has("weights"))throw new IllegalArgumentException();
        JSONArray ma=payload.getJSONArray("meals"),we=payload.getJSONArray("weights");
        JSONArray ac=payload.optJSONArray("activities");if(ac==null)ac=new JSONArray();
        final JSONArray importedActivities=ac;
        if(ma.length()>50000||we.length()>50000||ac.length()>50000)throw new IllegalArgumentException();
        int bb=payload.optInt("budget",23),ww=payload.optInt("weekly",FitRocketWeeklyWallet.DEFAULT_WEEKLY_POINTS);
        if(schema<=3 && ww==0)ww=FitRocketWeeklyWallet.DEFAULT_WEEKLY_POINTS;
        final int importedWeekly=ww;
        int share=payload.optInt("activityShare",100),baseline=payload.optInt("stepBaseline",3000),kpp=payload.optInt("kcalPerPoint",65);
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
        JSONArray aiProducts=payload.optJSONArray("aiFoods");
        if(aiProducts==null)aiProducts=new JSONArray();
        if(aiProducts.length()>250)throw new IllegalArgumentException("Zu viele eigene Lebensmittel");
        final JSONArray importedAiFoods=aiProducts;
        JSONArray recipeProducts=payload.optJSONArray("savedAiRecipes");
        if(recipeProducts==null)recipeProducts=new JSONArray();
        if(recipeProducts.length()>80)throw new IllegalArgumentException("Zu viele Rezepte");
        final JSONArray importedAiRecipes=recipeProducts;
        JSONArray favoriteBackup=payload.optJSONArray("favoriteMeals");
        if(favoriteBackup==null)favoriteBackup=new JSONArray();
        final JSONArray importedFavorites=favoriteBackup;
        boolean reminderEnabled=payload.optBoolean("waterReminderEnabled",false);
        int reminderStart=payload.optInt("waterReminderStart",9);
        int reminderEnd=payload.optInt("waterReminderEnd",20);
        int reminderInterval=payload.optInt("waterReminderInterval",2);
        if(reminderStart<0||reminderStart>22||reminderEnd<=reminderStart||
          reminderEnd>23||reminderInterval<1||reminderInterval>6)
          throw new IllegalArgumentException("Ungültige Erinnerungszeiten");
        final boolean importedReminderEnabled=reminderEnabled;
        final int importedReminderStart=reminderStart,importedReminderEnd=reminderEnd,
          importedReminderInterval=reminderInterval;
        final boolean importedUpdateChecks=payload.optBoolean("updateChecks",true);
        final boolean importedShowHints=payload.optBoolean("showHints",false);
        String preferencesBackup=payload.optString("aiPreferences","");
        if(preferencesBackup.length()>500)throw new IllegalArgumentException("Vorlieben zu lang");
        final String importedAiPreferences=preferencesBackup;
        String importedName=payload.optString("profileName",prefs.getString("profile_name",""));
        if(importedName.length()>40)throw new IllegalArgumentException("Profilname zu lang");
        final String restoredName=importedName;
        final String restoredAppearance=FitRocketTheme.normalized(
          payload.optString("appearance",prefs.getString(FitRocketTheme.KEY,FitRocketTheme.SYSTEM)));
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
          .setMessage(backupPreview+"\n\nBeim Import werden vorhandene lokale Mahlzeiten, "
            +"Gewichte, Aktivitäten und Favoriten ersetzt. API-Schlüssel werden nicht importiert. "
            +"Möchtest du fortfahren?")
          .setPositiveButton("Importieren",(d,w)->{
            try{
              android.content.SharedPreferences.Editor e=prefs.edit();
              e.putString("meals",ma.toString()).putString("weights",we.toString())
                .putString("activities",importedActivities.toString()).putString("stepDays",importedSteps.toString())
                .putString("waterDays",importedWater.toString()).putInt("waterGoal",importedWaterGoal)
                .putString("scannedProducts",importedScanned.toString())
                .putString("aiFoods",importedAiFoods.toString())
                .putString("savedAiRecipes",importedAiRecipes.toString())
                .putString("favoriteMeals",importedFavorites.toString())
                .putBoolean("waterReminderEnabled",importedReminderEnabled)
                .putInt("waterReminderStart",importedReminderStart)
                .putInt("waterReminderEnd",importedReminderEnd)
                .putInt("waterReminderInterval",importedReminderInterval)
                .putBoolean("updateChecks",importedUpdateChecks)
                .putBoolean("showHints",importedShowHints)
                .putString("aiPreferences",importedAiPreferences)
                .putString("profile_name",restoredName)
                .putString(FitRocketTheme.KEY,restoredAppearance)
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
              foods.clear();loadFoods();loadScannedFoods();loadAiFoods();loadRecipes();
              FitRocketWaterReminder.schedule(this);
              history();msg("Backup importiert.");
            }catch(Exception e){msg("Import konnte nicht abgeschlossen werden.");}
          }).setNegativeButton("Abbrechen",null).show();
      }catch(Exception e){msg("Die Backup-Datei ist ungültig.");}
    }
  }
    private class GlassView extends View {
    final String date;
    final Paint p=new Paint(3);
    GlassView(String date){super(MainActivity.this);this.date=date;}
    @Override protected void onDraw(Canvas c){
      super.onDraw(c);float w=getWidth(),h=getHeight(),cx=w/2,cy=h*.75f;
      double n=used(date);float ratio=(float)(n/Math.max(1,budget));
      float rad=Math.min(w*.34f,h*.62f);RectF arc=new RectF(cx-rad,cy-rad,cx+rad,cy+rad);
      p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(dp(19));
      p.setColor(dark()?Color.rgb(57,58,72):Color.rgb(250,235,231));c.drawArc(arc,190,160,false,p);
      p.setShader(new SweepGradient(cx,cy,new int[]{Color.rgb(119,211,154),Color.rgb(173,222,98),Color.rgb(255,184,122),Color.rgb(247,115,119),Color.rgb(119,211,154)},new float[]{0f,.34f,.62f,.84f,1f}));
      c.drawArc(arc,190,160*Math.min(1f,Math.max(0f,ratio)),false,p);p.setShader(null);p.setStyle(Paint.Style.FILL);
      p.setColor(ink());p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(dp(28));
      c.drawText(pt(n)+" / "+budget+" P",cx,cy-dp(25),p);
      p.setTypeface(Typeface.DEFAULT);p.setTextSize(dp(13));p.setColor(muted());c.drawText("Gegessen an diesem Tag",cx,cy-dp(4),p);
      if(ratio>1){p.setTextSize(dp(12));p.setColor(dark()?Color.rgb(255,163,154):Color.rgb(205,91,91));c.drawText("Etwas über deinem Ziel",cx,cy+dp(18),p);}
    }
  }
}
