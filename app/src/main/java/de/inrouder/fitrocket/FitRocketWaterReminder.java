package de.inrouder.fitrocket;

import android.app.*;
import android.content.*;
import android.os.Build;
import java.time.*;
import java.util.Calendar;

/** User-controlled, inexact local reminders; does not run while disabled. */
public final class FitRocketWaterReminder extends BroadcastReceiver {
 public static final String CHANNEL="fitrocket_water_reminders";
 public static final String ACTION="de.inrouder.fitrocket.WATER_REMINDER";
 private static final int REQUEST=2601;
 public static void schedule(Context c){
  cancel(c);
  android.content.SharedPreferences prefs=c.getSharedPreferences("fitrocket",Context.MODE_PRIVATE);
  if(!prefs.getBoolean("waterReminderEnabled",false))return;
  int first=prefs.getInt("waterReminderStart",9),end=prefs.getInt("waterReminderEnd",20);
  int interval=prefs.getInt("waterReminderInterval",2);
  if(first<0||end>23||end<=first||interval<1||interval>6)return;
  Calendar calendar=Calendar.getInstance();
  int hour=calendar.get(Calendar.HOUR_OF_DAY);
  int next=first;
  while(next<=end && (next<hour||(next==hour&&calendar.get(Calendar.MINUTE)>0)))next+=interval;
  if(next>end){
   calendar.add(Calendar.DAY_OF_YEAR,1);next=first;
  }
  calendar.set(Calendar.HOUR_OF_DAY,next);
  calendar.set(Calendar.MINUTE,0);calendar.set(Calendar.SECOND,0);calendar.set(Calendar.MILLISECOND,0);
  AlarmManager mgr=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  if(mgr!=null)mgr.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),intent(c));
 }
 public static void cancel(Context c){
  AlarmManager mgr=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  if(mgr!=null)mgr.cancel(intent(c));
 }
 private static PendingIntent intent(Context c){
  Intent i=new Intent(c,FitRocketWaterReminder.class).setAction(ACTION);
  return PendingIntent.getBroadcast(c,REQUEST,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
 }
 @Override public void onReceive(Context context,Intent intent){
  if(intent==null)return;
  if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
   schedule(context);return;
  }
  if(!ACTION.equals(intent.getAction()))return;
  android.content.SharedPreferences prefs=context.getSharedPreferences("fitrocket",Context.MODE_PRIVATE);
  if(!prefs.getBoolean("waterReminderEnabled",false))return;
  // Continue the inexact schedule even when notifications are denied.
  schedule(context);
  if(Build.VERSION.SDK_INT>=33 &&
    context.checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=
      android.content.pm.PackageManager.PERMISSION_GRANTED)return;
  try{
   NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
   if(nm==null)return;
   if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(
     new NotificationChannel(CHANNEL,"Wassererinnerungen",NotificationManager.IMPORTANCE_DEFAULT));
   Intent open=new Intent(context,MainActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
   PendingIntent content=PendingIntent.getActivity(context,REQUEST,open,
      PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   Notification.Builder b=Build.VERSION.SDK_INT>=26?
     new Notification.Builder(context,CHANNEL):new Notification.Builder(context);
   b.setSmallIcon(android.R.drawable.ic_popup_reminder)
    .setContentTitle("FitRocket · Trinkpause")
    .setContentText("Ein Glas Wasser? Du kannst 250 ml im Tagebuch eintragen.")
    .setAutoCancel(true).setContentIntent(content);
   nm.notify(REQUEST,b.build());
  }catch(Exception ignored){}
 }
}
