package de.inrouder.fitrocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;

/**
 * Central palette and system/light/dark preference.
 * The Java UI uses a shared surface mapper, so all legacy cards are legible in dark mode.
 */
public final class FitRocketTheme {
    public static final String SYSTEM="system",LIGHT="light",DARK="dark";
    public static final String KEY="appearance";
    private FitRocketTheme(){}

    public static String normalized(String mode){
        if(DARK.equals(mode)||LIGHT.equals(mode))return mode;
        return SYSTEM;
    }

    public static boolean isDark(Context context){
        SharedPreferences prefs=context.getSharedPreferences("fitrocket",Context.MODE_PRIVATE);
        String mode=normalized(prefs.getString(KEY,SYSTEM));
        if(DARK.equals(mode))return true;
        if(LIGHT.equals(mode))return false;
        int night=context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return night==Configuration.UI_MODE_NIGHT_YES;
    }

    public static int background(Context c){
        return isDark(c)?Color.rgb(19,21,29):Color.rgb(249,247,248);
    }
    public static int surface(Context c){
        return isDark(c)?Color.rgb(33,36,47):Color.WHITE;
    }
    public static int text(Context c){
        return isDark(c)?Color.rgb(238,239,245):Color.rgb(35,45,69);
    }
    public static int secondary(Context c){
        return isDark(c)?Color.rgb(180,186,203):Color.rgb(112,120,141);
    }
    public static int accent(Context c){
        return isDark(c)?Color.rgb(203,170,240):Color.rgb(111,93,173);
    }
    public static int divider(Context c){
        return isDark(c)?Color.rgb(55,58,73):Color.rgb(236,235,240);
    }

    public static int map(Context c,int original){
        if(!isDark(c))return original;
        if(original==Color.TRANSPARENT)return original;
        int r=Color.red(original),g=Color.green(original),b=Color.blue(original);
        if(original==Color.WHITE || (r>245&&g>245&&b>245))return surface(c);
        if(r>235&&g>235&&b>235)return Color.rgb(37,39,51);
        // Pastel mint and green success surface.
        if(g>r+12 && g>b-10 && g>190)return Color.rgb(35,65,52);
        // Pastel lavender/bluish.
        if(b>r+10 && b>=g && b>170)return Color.rgb(53,47,76);
        // Warm peach and pinks.
        if(r>205 && r>g+12 && g>130)return Color.rgb(77,51,50);
        if(r>200&&g>190&&b>180)return Color.rgb(45,43,52);
        if(r>175&&g>175&&b>175)return Color.rgb(45,47,58);
        return original;
    }
}
