package de.inrouder.fitrocket;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;
import org.robolectric.shadows.*;
import java.io.*;
import java.time.*;
import static org.junit.Assert.*;

/** Real date/time dialogs and local persistence, rendered with the same Android views as the app. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,qualifiers="w411dp-h850dp-xhdpi")
@LooperMode(LooperMode.Mode.PAUSED)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class FitRocketFastingFlowTest {
    private ActivityController<Activity> controller;
    private Activity activity;
    private SharedPreferences prefs;
    private LinearLayout root;
    private FitRocketFastingUi ui;
    @Before public void setup(){
        controller=Robolectric.buildActivity(Activity.class).setup();activity=controller.get();
        prefs=activity.getSharedPreferences("fitrocket",Context.MODE_PRIVATE);
        prefs.edit().clear().putBoolean("fastingReminderEnabled",true).putString("meals","unchanged diary").commit();
        root=new LinearLayout(activity);root.setOrientation(LinearLayout.VERTICAL);activity.setContentView(root);
        ui=new FitRocketFastingUi(activity,prefs);render();
    }
    @After public void cleanup(){ui.pause();ui.clear();controller.pause().stop().destroy();}
    private void render(){ui.detail(root,()->{});}
    private JSONObject state()throws Exception{return new JSONObject(prefs.getString("fasting",FitRocketFasting.empty().toString()));}
    private View find(View view,String text,boolean description){
        if(description?text.contentEquals(view.getContentDescription()==null?"":view.getContentDescription()):
            view instanceof Button&&text.contentEquals(((Button)view).getText()))return view;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){
            View found=find(((ViewGroup)view).getChildAt(i),text,description);if(found!=null)return found;
        }
        return null;
    }
    private void click(View view,String text){View button=find(view,text,false);assertNotNull("Missing button: "+text,button);assertTrue(button.performClick());idle();}
    private void clickDescription(View view,String text){View button=find(view,text,true);assertNotNull(text,button);button.performClick();idle();}
    private void idle(){Shadows.shadowOf(Looper.getMainLooper()).idle();}
    private AlertDialog latest(){idle();return ShadowAlertDialog.getLatestAlertDialog();}
    private Button action(AlertDialog dialog,int which){Button b=(Button)dialog.getWindow().getDecorView().findViewWithTag(which==DialogInterface.BUTTON_POSITIVE?"fastingConfirm":"fastingCancel");return b!=null?b:dialog.getButton(which);}
    private void positive(AlertDialog dialog){action(dialog,DialogInterface.BUTTON_POSITIVE).performClick();idle();}
    private void negative(AlertDialog dialog){action(dialog,DialogInterface.BUTTON_NEGATIVE).performClick();idle();}
    private void date(AlertDialog editor,LocalDate date){
        clickDescription(editor.getWindow().getDecorView(),"Datum wählen");
        DatePickerDialog picker=(DatePickerDialog)ShadowDialog.getLatestDialog();
        picker.getDatePicker().updateDate(date.getYear(),date.getMonthValue()-1,date.getDayOfMonth());positive(picker);
    }
    private TimePicker clockView(View view){
        if(view instanceof TimePicker)return (TimePicker)view;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){
            TimePicker found=clockView(((ViewGroup)view).getChildAt(i));if(found!=null)return found;
        }
        return null;
    }
    private void clock(AlertDialog editor,int hour,int minute){
        clickDescription(editor.getWindow().getDecorView(),"Uhrzeit wählen");
        TimePickerDialog picker=(TimePickerDialog)ShadowDialog.getLatestDialog();
        TimePicker clock=clockView(picker.getWindow().getDecorView());assertNotNull(clock);clock.setHour(hour);clock.setMinute(minute);positive(picker);
    }
    private long instant(LocalDate date,int hour,int minute){return date.atTime(hour,minute).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();}
    private void seed(long start){prefs.edit().putString("fasting",FitRocketFasting.start(FitRocketFasting.empty(),start).toString()).commit();render();}
    private void capture(View view,String name)throws Exception {capture(view,name,822,1450);}
    private void capture(View view,String name,int width,int height)throws Exception {
        view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));view.layout(0,0,width,height);
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);view.draw(new Canvas(bitmap));
        File dir=new File("build/outputs/pet-previews");assertTrue(dir.isDirectory()||dir.mkdirs());
        try(FileOutputStream out=new FileOutputStream(new File(dir,name))){assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));}
    }
    @Test public void startChoiceCancelAndStartNowOnlyPersistAfterConfirmation()throws Exception {
        String before=prefs.getString("fasting",null);click(root,"Fasten starten");negative(latest());assertEquals(before,prefs.getString("fasting",null));
        long now=System.currentTimeMillis();click(root,"Fasten starten");positive(latest());
        JSONObject active=state().getJSONObject("active");assertTrue(active.getLong("start")>=now);
        assertEquals(16*FitRocketFasting.HOUR,active.getLong("goal")-active.getLong("start"));
        assertEquals("unchanged diary",prefs.getString("meals",""));
    }
    @Test public void retroactiveStartUsesDateAndTimeAndCanCancelBothPickers()throws Exception {
        click(root,"Fasten starten");latest().getButton(DialogInterface.BUTTON_NEUTRAL).performClick();AlertDialog editor=latest();
        clickDescription(editor.getWindow().getDecorView(),"Datum wählen");negative((DatePickerDialog)ShadowDialog.getLatestDialog());
        clickDescription(editor.getWindow().getDecorView(),"Uhrzeit wählen");negative((TimePickerDialog)ShadowDialog.getLatestDialog());
        assertFalse(state().has("active"));
        LocalDate day=LocalDate.now().minusDays(1);date(editor,day);clock(editor,19,35);
        capture(editor.getWindow().getDecorView(),"fasting-retroactive-start.png");positive(editor);
        JSONObject active=state().getJSONObject("active");long start=instant(day,19,35);
        assertEquals(start,active.getLong("start"));assertEquals(start+16*FitRocketFasting.HOUR,active.getLong("goal"));
    }
    @Test public void movingStartUpdatesGoalAndReplacesPendingAlarm()throws Exception {
        LocalDate day=LocalDate.now().minusDays(1);long start=instant(day,20,0);seed(start);
        clickDescription(root,"Start bearbeiten");AlertDialog editor=latest();clock(editor,18,15);positive(editor);
        JSONObject active=state().getJSONObject("active");long moved=instant(day,18,15);
        assertEquals(moved,active.getLong("start"));assertEquals(moved+16*FitRocketFasting.HOUR,active.getLong("goal"));
        ShadowAlarmManager alarms=Shadows.shadowOf((AlarmManager)activity.getSystemService(Context.ALARM_SERVICE));
        assertEquals(1,alarms.getScheduledAlarms().size());
        Intent alarm=Shadows.shadowOf(alarms.getNextScheduledAlarm().operation).getSavedIntent();
        assertEquals(moved,alarm.getLongExtra("start",-1));assertEquals(moved+16*FitRocketFasting.HOUR,alarm.getLongExtra("goal",-1));
        clickDescription(root,"Start bearbeiten");editor=latest();clock(editor,19,0);positive(editor);
        assertEquals(instant(day,19,0)+16*FitRocketFasting.HOUR,state().getJSONObject("active").getLong("goal"));
    }
    @Test public void finishAndHistoryCorrectionRecordLongerActualTimeWithoutDuplication()throws Exception {
        LocalDate day=LocalDate.now().minusDays(2);long start=instant(day,20,0);seed(start);
        click(root,"Fasten beenden");AlertDialog editor=latest();date(editor,day.plusDays(1));clock(editor,13,10);positive(editor);
        assertFalse(state().has("active"));assertEquals(instant(day.plusDays(1),13,10),state().getJSONArray("history").getJSONObject(0).getLong("end"));
        click(root,"Endzeit ändern");editor=latest();clock(editor,15,45);positive(editor);
        JSONArray history=state().getJSONArray("history");assertEquals(1,history.length());
        assertEquals(instant(day.plusDays(1),15,45),history.getJSONObject(0).getLong("end"));assertEquals(start+16*FitRocketFasting.HOUR,history.getJSONObject(0).getLong("goal"));
        render();assertEquals(history.toString(),state().getJSONArray("history").toString());assertEquals("unchanged diary",prefs.getString("meals",""));
    }
    @Test public void invalidGoalAndEndBeforeBeginningKeepDialogAndState()throws Exception {
        LocalDate day=LocalDate.now();long start=instant(day,0,0);seed(start);String saved=state().toString();
        clickDescription(root,"Ziel bearbeiten");AlertDialog editor=latest();clock(editor,0,0);positive(editor);
        assertTrue(editor.isShowing());assertEquals(saved,state().toString());negative(editor);
        click(root,"Fasten beenden");editor=latest();date(editor,day.minusDays(1));positive(editor);
        assertTrue(editor.isShowing());assertEquals(saved,state().toString());negative(editor);
        assertTrue(state().has("active"));
    }
    @Test @Config(sdk=35) public void historyCorrectionPreservesCurrentAlarmAndNotification()throws Exception {
        long now=System.currentTimeMillis(),oldStart=now-48*FitRocketFasting.HOUR;
        JSONObject state=FitRocketFasting.finish(FitRocketFasting.start(FitRocketFasting.empty(),oldStart),oldStart+16*FitRocketFasting.HOUR);
        long current=now-17*FitRocketFasting.HOUR,goal=current+16*FitRocketFasting.HOUR;
        state=FitRocketFasting.start(state,current);prefs.edit().putString("fasting",state.toString()).commit();
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).grantPermissions("android.permission.POST_NOTIFICATIONS");
        new FitRocketFastingReminder().onReceive(activity,new Intent(FitRocketFastingReminder.ACTION).putExtra("start",current).putExtra("goal",goal));
        String active=state().getJSONObject("active").toString();render();
        ShadowNotificationManager notifications=Shadows.shadowOf((NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE));assertEquals(1,notifications.getAllNotifications().size());
        click(root,"Endzeit ändern");AlertDialog editor=latest();positive(editor);
        assertEquals(active,state().getJSONObject("active").toString());assertEquals(1,notifications.getAllNotifications().size());
    }
    @Test public void staleHistoryDialogCannotChangeNewlyInsertedEntry()throws Exception {
        long now=System.currentTimeMillis();JSONObject state=FitRocketFasting.finish(FitRocketFasting.start(FitRocketFasting.empty(),now-48*FitRocketFasting.HOUR),now-30*FitRocketFasting.HOUR);
        prefs.edit().putString("fasting",state.toString()).commit();render();click(root,"Endzeit ändern");AlertDialog editor=latest();
        state=FitRocketFasting.finish(FitRocketFasting.start(state,now-FitRocketFasting.HOUR),now);prefs.edit().putString("fasting",state.toString()).commit();
        positive(editor);assertTrue(editor.isShowing());assertEquals(state.toString(),state().toString());negative(editor);
    }
    @Test public void editDialogRefreshesTheCurrentPageAfterActivityResumeRebuildsIt()throws Exception {
        LocalDate day=LocalDate.now().minusDays(1);seed(instant(day,20,0));
        clickDescription(root,"Start bearbeiten");AlertDialog editor=latest();
        root=new LinearLayout(activity);root.setOrientation(LinearLayout.VERTICAL);activity.setContentView(root);render();
        clock(editor,19,0);positive(editor);
        assertEquals(instant(day,19,0),state().getJSONObject("active").getLong("start"));
        assertTrue(textContent(root).contains("19:00"));assertTrue(textContent(root).contains("11:00"));
    }
    private String textContent(View view){
        String value=view instanceof TextView?((TextView)view).getText().toString():"";
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)value+=" "+textContent(((ViewGroup)view).getChildAt(i));
        return value;
    }
    @Test public void olderStoredEntriesAreReachableForEndCorrection()throws Exception {
        long now=System.currentTimeMillis();JSONObject state=FitRocketFasting.empty();
        for(int i=10;i>=2;i--){long start=now-i*24*FitRocketFasting.HOUR;state=FitRocketFasting.finish(FitRocketFasting.start(state,start),start+16*FitRocketFasting.HOUR);}
        prefs.edit().putString("fasting",state.toString()).commit();render();assertEquals(7,countButtons(root,"Endzeit ändern"));
        click(root,"Alle Fastenzeiten anzeigen");assertEquals(9,countButtons(root,"Endzeit ändern"));
        click(root,"Weniger anzeigen");assertEquals(7,countButtons(root,"Endzeit ändern"));
    }
    private int countButtons(View view,String text){
        int count=view instanceof Button&&text.contentEquals(((Button)view).getText())?1:0;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)count+=countButtons(((ViewGroup)view).getChildAt(i),text);
        return count;
    }

    @Test @Config(sdk=35,qualifiers="w320dp-h640dp-xhdpi") public void darkTimeEditorKeepsActionsReachableWithLargeFont()throws Exception {
        prefs.edit().putString("appearance","dark").commit();activity.setTheme(android.R.style.Theme_Material_NoActionBar);
        android.content.res.Resources resources=activity.getResources();
        android.content.res.Configuration config=new android.content.res.Configuration(resources.getConfiguration());config.fontScale=2;
        resources.updateConfiguration(config,resources.getDisplayMetrics());
        seed(System.currentTimeMillis()-FitRocketFasting.HOUR);clickDescription(root,"Start bearbeiten");AlertDialog editor=latest();
        capture(editor.getWindow().getDecorView(),"fasting-dark-large-font.png",640,1280);
        for(int which:new int[]{DialogInterface.BUTTON_POSITIVE,DialogInterface.BUTTON_NEGATIVE}){
            Button button=action(editor,which);Rect visible=new Rect();assertTrue(button.getGlobalVisibleRect(visible));
            assertEquals("Dialog action clipped",button.getHeight(),visible.height());
        }
        View time=find(editor.getWindow().getDecorView(),"Uhrzeit wählen",true);
        ViewParent parent=time.getParent();while(parent!=null&&!(parent instanceof ScrollView))parent=parent.getParent();
        assertTrue(parent instanceof ScrollView);((ScrollView)parent).fullScroll(View.FOCUS_DOWN);idle();
        Rect visible=new Rect();assertTrue("Time picker must be reachable by scrolling",time.getGlobalVisibleRect(visible));
        for(int which:new int[]{DialogInterface.BUTTON_POSITIVE,DialogInterface.BUTTON_NEGATIVE}){
            Button button=action(editor,which);Rect actionBounds=new Rect();assertTrue(button.getGlobalVisibleRect(actionBounds));
            assertEquals("Scrolling must keep dialog actions fully visible",button.getHeight(),actionBounds.height());
        }
        assertTrue(action(editor,DialogInterface.BUTTON_POSITIVE).getMeasuredHeight()>0);
        assertNotNull(find(editor.getWindow().getDecorView(),"Datum wählen",true));
        assertNotNull(find(editor.getWindow().getDecorView(),"Uhrzeit wählen",true));
        String saved=state().toString();negative(editor);assertEquals(saved,state().toString());
    }

}
