package de.inrouder.fitrocket;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;
import org.robolectric.shadows.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,qualifiers="w411dp-h850dp-xhdpi")
@LooperMode(LooperMode.Mode.PAUSED)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class FitRocketAndroidFlowTest {
    private ActivityController<MainActivity> controller;
    private SharedPreferences prefs;
    @Before public void prepare(){
        prefs=RuntimeEnvironment.getApplication().getSharedPreferences("fitrocket",Context.MODE_PRIVATE);
        prefs.edit().clear().putBoolean("updateChecks",false).putBoolean("weekly28MigrationDone",true)
            .putBoolean("immediateActivityCreditMigrationDone",true).putInt("weekly",28).putInt("budget",23)
            .putString("meals","[{\"name\":\"Testmahlzeit\",\"points\":3,\"date\":\""+LocalDate.now().minusDays(1)+"\"}]")
            .putString("appearance","light").commit();
    }
    private MainActivity start(){controller=Robolectric.buildActivity(MainActivity.class).setup();return controller.get();}
    @After public void cleanup(){if(controller!=null)controller.pause().stop().destroy();}
    private List<TextView> find(View view,String value){
        List<TextView> out=new ArrayList<>();if(view instanceof TextView&&value.contentEquals(((TextView)view).getText()))out.add((TextView)view);
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)out.addAll(find(((ViewGroup)view).getChildAt(i),value));return out;
    }
    private View root(){return controller.get().getWindow().getDecorView();}
    private void click(String value){
        for(TextView candidate:find(root(),value)){
            View view=candidate;
            while(view!=null){if(view.isClickable()){assertTrue(view.performClick());Shadows.shadowOf(Looper.getMainLooper()).idle();return;}view=view.getParent() instanceof View?(View)view.getParent():null;}
        }
        fail("No clickable label: "+value);
    }
    private JSONObject pet()throws Exception {return new JSONObject(prefs.getString("pet","{}"));}
    private void capture(String file)throws Exception {
        View view=controller.get().findViewById(android.R.id.content);
        view.measure(View.MeasureSpec.makeMeasureSpec(822,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1700,View.MeasureSpec.EXACTLY));view.layout(0,0,822,1700);
        Bitmap bitmap=Bitmap.createBitmap(822,1700,Bitmap.Config.ARGB_8888);view.draw(new Canvas(bitmap));
        File dir=new File("build/outputs/pet-previews");assertTrue(dir.isDirectory()||dir.mkdirs());
        try(FileOutputStream out=new FileOutputStream(new File(dir,file))){assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));}
        assertNotEquals("Render unexpectedly empty",bitmap.getPixel(20,20),bitmap.getPixel(350,500));
    }
    @Test public void petCanBeAdoptedFedAndRestoredWithoutChangingDiary()throws Exception {
        start();String meals=prefs.getString("meals","");click("Pet");click("Ei adoptieren");
        assertEquals(200,pet().getInt("foodTenths"));click("Füttern");
        AlertDialog dialog=ShadowAlertDialog.getLatestAlertDialog();assertTrue(dialog.isShowing());dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertEquals(190,pet().getInt("foodTenths"));assertEquals(10,pet().getInt("fedTenths"));assertEquals(meals,prefs.getString("meals",""));
        click("Spielen");click("Streicheln");assertEquals(4,pet().getInt("stars"));
        capture("pet-light.png");controller.recreate();assertFalse(find(root(),"Novi").isEmpty());assertEquals(190,pet().getInt("foodTenths"));
    }
    @Test public void maturePetAndMonsterRenderWithAccessories()throws Exception {
        LocalDate now=LocalDate.now();JSONObject state=FitRocketPet.adopt(FitRocketPet.empty(),now.minusDays(10),"Luna");
        state=FitRocketPet.harvest(state,now,23,d->0);state=FitRocketPet.feed(state,600,now.minusDays(4));state=FitRocketPet.equip(state,"dog_ears");
        prefs.edit().putString("pet",state.toString()).putString("appearance","dark").commit();
        start();click("Pet");assertFalse(find(root(),"Monsterlaune · Zähnchen voraus!").isEmpty());capture("pet-dark-monster.png");
        assertEquals("dog_ears",pet().getString("equippedHead"));
        click("Profil");state=FitRocketPet.feed(state,600,now);state=FitRocketPet.equip(state,"star_crown");state=FitRocketPet.equip(state,"rocket_toy");
        prefs.edit().putString("pet",state.toString()).commit();click("Pet");capture("pet-guardian.png");
        assertEquals(4,FitRocketPet.stage(pet()));assertFalse(find(root(),"Glücklich").isEmpty());
    }
    @Test public void largeFontKeepsAllSixNavigationLabelsVisible()throws Exception {
        android.content.res.Resources resources=RuntimeEnvironment.getApplication().getResources();
        android.content.res.Configuration config=new android.content.res.Configuration(resources.getConfiguration());config.fontScale=2;
        resources.updateConfiguration(config,resources.getDisplayMetrics());
        start();assertEquals(2f,controller.get().getResources().getConfiguration().fontScale,0.01f);click("Pet");click("Ei adoptieren");capture("pet-large-font.png");
        for(String value:new String[]{"Tagebuch","Suche","Kochbuch","Fortschritt","Pet","Profil"}){
            boolean found=false;
            for(TextView label:find(root(),value)){
                View view=label.getParent() instanceof View?(View)label.getParent():label;
                if(value.contentEquals(view.getContentDescription()==null?"":view.getContentDescription())){
                    assertTrue(label.getMeasuredHeight()>0);assertTrue(label.getMeasuredWidth()>0);found=true;
                }
            }
            assertTrue("Navigation label missing: "+value,found);
        }
    }
    @Test public void goalAlarmSchedulesReplacesCancelsAndNotificationOpensTimer()throws Exception {
        MainActivity activity=start();long now=System.currentTimeMillis();JSONObject state=FitRocketFasting.start(FitRocketFasting.empty(),now);
        prefs.edit().putString("fasting",state.toString()).commit();FitRocketFastingReminder.schedule(activity);
        ShadowAlarmManager alarms=Shadows.shadowOf((AlarmManager)activity.getSystemService(Context.ALARM_SERVICE));
        assertEquals(1,alarms.getScheduledAlarms().size());assertEquals(now+16*FitRocketFasting.HOUR,alarms.getNextScheduledAlarm().triggerAtTime);
        state=FitRocketFasting.edit(state,now,now+17*FitRocketFasting.HOUR,now);prefs.edit().putString("fasting",state.toString()).commit();FitRocketFastingReminder.schedule(activity);
        assertEquals(1,alarms.getScheduledAlarms().size());assertEquals(now+17*FitRocketFasting.HOUR,alarms.getNextScheduledAlarm().triggerAtTime);
        prefs.edit().putString("fasting",FitRocketFasting.cancel(state).toString()).commit();FitRocketFastingReminder.schedule(activity);assertEquals(0,alarms.getScheduledAlarms().size());
        long start=now-16*FitRocketFasting.HOUR-10000,goal=now-10000;
        prefs.edit().putString("fasting",FitRocketFasting.start(FitRocketFasting.empty(),start).toString()).commit();
        Intent due=new Intent(FitRocketFastingReminder.ACTION).putExtra("start",start).putExtra("goal",goal);
        new FitRocketFastingReminder().onReceive(activity,due);
        ShadowNotificationManager notifications=Shadows.shadowOf((NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE));
        assertEquals(1,notifications.getAllNotifications().size());Notification notification=notifications.getAllNotifications().get(0);
        assertEquals("FitRocket · Fastenziel erreicht",notification.extras.getString(Notification.EXTRA_TITLE));
        Intent open=Shadows.shadowOf(notification.contentIntent).getSavedIntent();assertTrue(open.getBooleanExtra(FitRocketFastingReminder.OPEN_FASTING,false));
        controller.newIntent(open);assertFalse(find(root(),"Intervallfasten").isEmpty());
        assertFalse(FitRocketFastingAlarmRules.due(new JSONObject(prefs.getString("fasting","{}")),true,start,goal,now));
    }
    @Test @Config(sdk=35) public void notificationPermissionAndInexactFallbackOnNewAndroid()throws Exception {
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).denyPermissions("android.permission.POST_NOTIFICATIONS");
        MainActivity activity=start();long now=System.currentTimeMillis();
        prefs.edit().putString("fasting",FitRocketFasting.start(FitRocketFasting.empty(),now).toString()).commit();
        assertFalse(FitRocketFastingReminder.allowed(activity));FitRocketFastingReminder.schedule(activity);
        ShadowAlarmManager alarms=Shadows.shadowOf((AlarmManager)activity.getSystemService(Context.ALARM_SERVICE));assertEquals(0,alarms.getScheduledAlarms().size());
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).grantPermissions("android.permission.POST_NOTIFICATIONS");
        alarms.setCanScheduleExactAlarms(false);FitRocketFastingReminder.schedule(activity);
        assertTrue(FitRocketFastingReminder.allowed(activity));assertEquals(1,alarms.getScheduledAlarms().size());
    }
}
