# Aktivpunkte in FitRocket v0.6.0

## Historische Referenzen
- https://www.weightwatchers.com/de/blog/fitpoints-verstehen
- https://www.weightwatchers.com/de/blog/alles-ueber-fitpointsr
- https://www.weightwatchers.com/de/blog/ww-fitpoints

WW bestätigt die Abhängigkeit von persönlichen Merkmalen, Aktivitätsart, Dauer und Intensität und erlaubt den Eintausch von Aktiv- gegen SmartPoints. Eine exakte algorithmische Formel ist öffentlich nicht bekannt.

## FitRocket-Prototyp
Aktive kcal ≈ max(0, MET − 1) × 3,5 × Körpergewicht_kg / 200 × Minuten.
Aktivpunkte ≈ round(aktive kcal / 65).
MET-Faktoren: niedrig 2,5; moderat 4,5; hoch 7,0. Diese sind Modellannahmen; nicht an jede Sportart spezifisch angepasst. Das ist bewusst kein exakter WW-FitPoints-Wert.

Option zur Wochenpuffer-Gutschrift: 0 %, 50 %, 100 %.
Gutschrift = abgerundet(Summe der Wochen-Aktivpunkte × Anteil).
Verfügbarer Wochenpuffer = max(0, Basis-Wochenextra + Gutschrift − Summe der wöchentlichen Tagesüberschreitungen).
Die feste tägliche Punktezahl bleibt gleich.

## Bekannte Grenzen
Alter, Größe und biologisches Geschlecht sind noch nicht in dieser vereinfachten Schätzformel berücksichtigt, anders als im offiziellen FitPoints-System. Für eine exakte Emulation ist der historische Algorithmus erforderlich. Die 3000-Schritte-Regel gilt für die damalige Aktivpunkte-Einlösung; FitRocket v0.6.0 erfasst keine Schritte und vergibt keine Schrittepunkte. Workouts werden manuell erfasst; beim späteren Watch-Import ist Deduplizierung nötig.

Benutzerdaten bleiben lokal und können als JSON exportiert/importiert werden. Ausgaben im Tagebuch können vom Wochenpuffer abgehen, aber die App sperrt keine Buchungen bei Überschreitung.
