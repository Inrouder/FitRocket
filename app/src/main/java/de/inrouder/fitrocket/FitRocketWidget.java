package de.inrouder.fitrocket;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RemoteViews;
import java.time.LocalDate;

/** Private offline homescreen widget with automatic refresh on updates and new days. */
public final class FitRocketWidget extends AppWidgetProvider {
    private static final String PREF="fitrocket";

    public static void refresh(Context context) {
        AppWidgetManager manager=AppWidgetManager.getInstance(context);
        ComponentName component=new ComponentName(context,FitRocketWidget.class);
        int[] ids=manager.getAppWidgetIds(component);
        if(ids!=null && ids.length>0)
            updateWidgets(context,manager,ids,R.layout.fitrocket_widget);
        ComponentName vertical=new ComponentName(context,FitRocketWidgetVertical.class);
        int[] narrow=manager.getAppWidgetIds(vertical);
        if(narrow!=null && narrow.length>0)
            updateWidgets(context,manager,narrow,R.layout.fitrocket_widget_vertical);
        ComponentName compact=new ComponentName(context,FitRocketWidgetCompact.class);
        int[] small=manager.getAppWidgetIds(compact);
        if(small!=null && small.length>0)
            updateWidgets(context,manager,small,R.layout.fitrocket_widget_compact);
    }

    public static void updateWidgets(Context context,AppWidgetManager manager,int[] ids,
                                     int layoutId){
        SharedPreferences p=context.getSharedPreferences(PREF,Context.MODE_PRIVATE);
        FitRocketWidgetData data=FitRocketWidgetData.fromJson(
            LocalDate.now().toString(),p.getInt("budget",23),
            p.getString("meals","[]"),p.getString("waterDays","{}"),
            p.getInt("waterGoal",1500));
        for(int id:ids){
            RemoteViews views=new RemoteViews(context.getPackageName(),layoutId);
            boolean isCompact=layoutId==R.layout.fitrocket_widget_compact;
            views.setTextViewText(R.id.widget_points,
                isCompact?data.remainingText()+" P":data.remainingText()+" P übrig");
            views.setTextViewText(R.id.widget_used,
                isCompact?"übrig":data.usedText()+" / "+data.budget+" P genutzt");
            views.setTextViewText(R.id.widget_water,
                isCompact?data.waterText()+" L":data.waterText()+" / "+data.waterGoalText()+" L");
            views.setProgressBar(R.id.widget_water_bar,100,data.waterPercent(),false);

            Intent open=new Intent(context,MainActivity.class);
            open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pending=PendingIntent.getActivity(context,0,open,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_root,pending);
            manager.updateAppWidget(id,views);
        }
    }

    @Override public void onUpdate(Context context,AppWidgetManager manager,int[] ids){
        updateWidgets(context,manager,ids,R.layout.fitrocket_widget);
    }

    @Override public void onReceive(Context context,Intent intent){
        super.onReceive(context,intent);
        String action=intent==null?"":intent.getAction();
        if(Intent.ACTION_DATE_CHANGED.equals(action)
            || Intent.ACTION_TIME_CHANGED.equals(action)
            || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
            || Intent.ACTION_BOOT_COMPLETED.equals(action))
            refresh(context);
    }
}
