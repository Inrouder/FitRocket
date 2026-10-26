package de.inrouder.fitrocket;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.ScrollView;
import android.widget.TextView;

/** Shown by Android's Health Connect permission screen via View Permission Usage. */
public class HealthPrivacyActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView content = new ScrollView(this);
        TextView view = new TextView(this);
        view.setTextColor(Color.rgb(38,46,76));
        view.setBackgroundColor(Color.rgb(255,249,244));
        view.setTextSize(17);
        int p=(int)(24*getResources().getDisplayMetrics().density);
        view.setPadding(p,p,p,p);
        view.setText("FitRocket – Datenschutz bei Fitnessdaten\n\n"
          + "Mit deiner ausdrücklichen Freigabe liest FitRocket Schrittzahlen und Trainingseinheiten "
          + "aus Health Connect, um Bewegungspunkte und deinen Wochenpuffer zu berechnen.\n\n"
          + "Der Abruf erfolgt nur, wenn du FitRocket verbindest und synchronisierst. "
          + "Die Werte bleiben lokal auf deinem Gerät und können in deinem FitRocket-Backup enthalten sein. "
          + "FitRocket übermittelt die eingelesenen Daten nicht an eigene Server.\n\n"
          + "Du kannst den Zugriff jederzeit in den Android-Einstellungen unter Health Connect widerrufen. "
          + "In FitRocket kannst du die verbundenen Daten ebenfalls entfernen. "
          + "Für die Fitnessintegration sind keine Schreibrechte erforderlich.");
        content.addView(view);setContentView(content);
    }
}
