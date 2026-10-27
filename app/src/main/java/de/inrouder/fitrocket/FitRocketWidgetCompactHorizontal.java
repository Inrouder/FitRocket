package de.inrouder.fitrocket;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/** Extra-short 2x1 homescreen widget, independent of 1x2, 2x3 and 4x2. */
public final class FitRocketWidgetCompactHorizontal extends AppWidgetProvider {
    @Override public void onUpdate(Context context,AppWidgetManager manager,int[] ids) {
        FitRocketWidget.updateWidgets(context,manager,ids,
            R.layout.fitrocket_widget_compact_horizontal);
    }
    @Override public void onReceive(Context context,Intent intent) {
        super.onReceive(context,intent);
        if(intent!=null && (Intent.ACTION_DATE_CHANGED.equals(intent.getAction()) ||
            Intent.ACTION_TIME_CHANGED.equals(intent.getAction()) ||
            Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction()) ||
            Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())))
            FitRocketWidget.refresh(context);
    }
}
