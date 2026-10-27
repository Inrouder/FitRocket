package de.inrouder.fitrocket;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/** Narrow 1x2 widget independent from the existing wide and vertical widgets. */
public final class FitRocketWidgetCompact extends AppWidgetProvider {
    @Override public void onUpdate(Context context,AppWidgetManager manager,int[] ids){
        FitRocketWidget.updateWidgets(context,manager,ids,R.layout.fitrocket_widget_compact);
    }
    @Override public void onReceive(Context context,Intent intent){
        super.onReceive(context,intent);
        if(intent!=null && (Intent.ACTION_DATE_CHANGED.equals(intent.getAction()) ||
            Intent.ACTION_TIME_CHANGED.equals(intent.getAction()) ||
            Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction()) ||
            Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())))
            FitRocketWidget.refresh(context);
    }
}
