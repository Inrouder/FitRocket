package de.inrouder.fitrocket;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import org.json.JSONObject;

/** One local fasting-goal alarm, restored after reboot/update and replaced whenever the goal changes. */
public final class FitRocketFastingReminder extends BroadcastReceiver {
    public static final String ACTION="de.inrouder.fitrocket.FASTING_GOAL";
    public static final String OPEN_FASTING="fitrocketOpenFasting";
    public static final int PERMISSION_REQUEST=2603;
    private static final int REQUEST=2602;
    private static final String CHANNEL="fitrocket_fasting_goal";
    private static SharedPreferences prefs(Context c){return c.getSharedPreferences("fitrocket",Context.MODE_PRIVATE);}
    private static JSONObject state(Context c)throws Exception{
        return FitRocketFasting.validated(new JSONObject(prefs(c).getString(FitRocketFasting.KEY,FitRocketFasting.empty().toString())));
    }
    private static PendingIntent alarm(Context c,long start,long goal){
        Intent i=new Intent(c,FitRocketFastingReminder.class).setAction(ACTION)
            .putExtra("start",start).putExtra("goal",goal);
        return PendingIntent.getBroadcast(c,REQUEST,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    private static NotificationManager manager(Context c){return (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);}
    public static boolean allowed(Context c){
        if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=
            android.content.pm.PackageManager.PERMISSION_GRANTED)return false;
        NotificationManager nm=manager(c);
        if(nm==null||!nm.areNotificationsEnabled())return false;
        NotificationChannel channel=nm.getNotificationChannel(CHANNEL);
        return channel==null||channel.getImportance()!=NotificationManager.IMPORTANCE_NONE;
    }
    public static boolean exact(Context c){
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        return am!=null&&(Build.VERSION.SDK_INT<31||am.canScheduleExactAlarms());
    }
    public static void dismiss(Context c){NotificationManager nm=manager(c);if(nm!=null)nm.cancel(REQUEST);}
    public static void schedule(Context c){
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if(am==null)return;
        am.cancel(alarm(c,0,0));
        try{
            JSONObject state=state(c),active=state.optJSONObject("active");
            boolean enabled=prefs(c).getBoolean(FitRocketFastingAlarmRules.ENABLED_KEY,true);
            if(!enabled||active==null){NotificationManager nm=manager(c);if(nm!=null)nm.cancel(REQUEST);return;}
            long trigger=FitRocketFastingAlarmRules.nextTrigger(state,true,System.currentTimeMillis());
            if(trigger==0||!allowed(c))return;
            PendingIntent pending=alarm(c,active.optLong("start"),active.optLong("goal"));
            // Exact alarms are optional on Android 12+. The fallback still works without special access.
            if(exact(c)){
                try{am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,trigger,pending);return;}
                catch(SecurityException ignored){}
            }
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,trigger,pending);
        }catch(Exception ignored){/* Invalid data must neither crash boot nor emit a misleading notification. */}
    }
    public static void requestPermission(Activity activity){
        SharedPreferences p=prefs(activity);
        if(Build.VERSION.SDK_INT>=33&&activity.checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=
            android.content.pm.PackageManager.PERMISSION_GRANTED&&
            (!p.getBoolean("fastingNotificationAsked",false)||activity.shouldShowRequestPermissionRationale("android.permission.POST_NOTIFICATIONS"))){
            p.edit().putBoolean("fastingNotificationAsked",true).apply();
            activity.requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},PERMISSION_REQUEST);
        }else{
            NotificationManager nm=manager(activity);
            Intent settings=new Intent(nm!=null&&nm.areNotificationsEnabled()?Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS:Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE,activity.getPackageName()).putExtra(Settings.EXTRA_CHANNEL_ID,CHANNEL);
            if(nm!=null)nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Fastenziel erreicht",NotificationManager.IMPORTANCE_DEFAULT));
            try{activity.startActivity(settings);}catch(Exception e){activity.startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,activity.getPackageName()));}
        }
    }
    public static void requestExact(Activity activity){
        if(Build.VERSION.SDK_INT>=31){
            try{activity.startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+activity.getPackageName())));}
            catch(Exception e){android.widget.Toast.makeText(activity,"Öffne in den Android-Einstellungen den Zugriff auf Alarme und Erinnerungen.",android.widget.Toast.LENGTH_LONG).show();}
        }
    }
    @Override public void onReceive(Context context,Intent intent){
        if(intent==null)return;
        String action=intent.getAction();
        if(Intent.ACTION_BOOT_COMPLETED.equals(action)||Intent.ACTION_TIME_CHANGED.equals(action)||
            Intent.ACTION_TIMEZONE_CHANGED.equals(action)||Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)||
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED".equals(action)){
            schedule(context);return;
        }
        if(!ACTION.equals(action))return;
        try{
            JSONObject state=state(context);
            long start=intent.getLongExtra("start",-1),goal=intent.getLongExtra("goal",-1),now=System.currentTimeMillis();
            boolean enabled=prefs(context).getBoolean(FitRocketFastingAlarmRules.ENABLED_KEY,true);
            if(!FitRocketFastingAlarmRules.due(state,enabled,start,goal,now)){
                schedule(context);return;
            }
            if(!allowed(context))return;
            NotificationManager nm=manager(context);if(nm==null)return;
            nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Fastenziel erreicht",NotificationManager.IMPORTANCE_DEFAULT));
            Intent open=new Intent(context,MainActivity.class).putExtra(OPEN_FASTING,true)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent content=PendingIntent.getActivity(context,REQUEST,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            Notification notification=new Notification.Builder(context,CHANNEL)
                .setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle("FitRocket · Fastenziel erreicht")
                .setContentText("Dein geplantes Fasten ist geschafft. Öffne deinen Timer, um es zu beenden.")
                .setAutoCancel(true).setContentIntent(content).setOnlyAlertOnce(true).build();
            nm.notify(REQUEST,notification);
            prefs(context).edit().putString(FitRocketFasting.KEY,
                FitRocketFastingAlarmRules.notified(state,start,goal,now).toString()).commit();
        }catch(Exception ignored){}
    }
}
