# Fastenzeiten auswählen und korrigieren (v0.18.2)

[Android-Rendering des Startdialogs](images/fasting-start.png) – unter Robolectric erstellt, kein Geräte-Screenshot.

## Bedienung

1. **Fasten starten** fragt „Wann hast du mit dem Fasten begonnen?“: **Ab jetzt**, **Früher begonnen** oder **Abbrechen**.
2. **Ab jetzt** speichert den aktuellen Zeitpunkt. **Früher begonnen** öffnet Datum und Uhrzeit mit einer Vorschau für Beginn und Ziel. Vorgeschlagen wird zunächst eine Stunde zuvor; erst **Fasten starten** bestätigt die angezeigte Auswahl.
3. Bei laufendem Fasten unter **Begonnen → Bearbeiten** Datum/Uhrzeit korrigieren. Das Ziel verschiebt sich um denselben Zeitraum; eine manuell angepasste Dauer bleibt erhalten. Beispiel: 20:00 bis 12:00 wird mit Beginn 19:00 zu 19:00 bis 11:00. Eine Korrektur auf 21:00 ergibt 21:00 bis 13:00, sofern 21:00 bereits vergangen ist.
4. **Ziel → Bearbeiten** bleibt für eine bewusste Änderung der geplanten Dauer verfügbar.
5. Nach Zielerreichung läuft der Timer weiter. **Fasten beenden** zeigt das tatsächliche Ende mit Datum/Uhrzeit und Gesamtdauer zur Prüfung. Voreinstellung ist der Zeitpunkt beim Öffnen des Dialogs. Auch länger als geplant gefastete Zeit wird vollständig gespeichert; bei früherem Aufhören das tatsächliche Ende wählen. **Weiterfasten** verwirft den Dialogentwurf.
6. Im Verlauf über **Endzeit ändern** den tatsächlichen Endzeitpunkt später korrigieren, etwa von 12:00 auf 14:30, wenn das Fasten länger dauerte. Beginn und geplantes Ziel bleiben erhalten, die Dauer und „Ziel erreicht“-Anzeige werden neu berechnet. **Alle Fastenzeiten anzeigen** macht auch ältere gespeicherte Einträge erreichbar (maximal 100).

Die Zeitfelder lassen sich bei großer Schrift scrollen; die Bestätigungstasten bleiben im festen unteren Bereich sichtbar. Datum und Uhrzeit sind zunächst ein Entwurf. Ein Abbruch der Auswahl oder des Bestätigungsdialogs ändert nichts. Ungültige Werte zeigen eine Meldung im offenen Dialog: kein Beginn/Ende in der Zukunft und kein Ende vor dem Beginn. Das geplante Ziel liegt weiterhin nach dem Beginn und höchstens 24 Stunden später. Die tatsächliche Dauer darf länger sein. Angezeigt wird die lokale Gerätezeit; gespeicherte Unix-Millisekunden erhalten reale Zeitabstände auch beim Wechsel zwischen Sommer- und Winterzeit.

## Erinnerungen, Daten und Sicherung

Ein korrigierter Beginn verschiebt auch die Zielerinnerung. Alte Alarmdaten werden ungültig; die neue Erinnerung bezieht sich auf das neue Ziel. Liegt das Ziel bereits zurück, greifen die bestehenden Regeln für eine überfällige Erinnerung. Beim Beenden oder Abbrechen wird der aktive Alarm entfernt.

Eine Endkorrektur an einem abgeschlossenen Eintrag verändert ein inzwischen neues laufendes Fasten, dessen Alarm oder bereits zugestellte Zielbenachrichtigung nicht. Ein Dialog verweigert das Speichern, wenn sein Eintrag inzwischen verschoben/ersetzt wurde, damit kein anderer Verlaufseintrag versehentlich geändert wird.

Es bleibt beim vorhandenen SharedPreferences-JSON `fasting`: `planHours`, optional `active` mit `start`/`goal`/`planHours` und `history` mit zusätzlichem `end`. Ein vorhandener `notifiedGoal`-Marker wird bei tatsächlicher aktiver Zeitänderung entfernt. Keine neuen Felder, kein Schemawechsel, keine Migration. Alt-Backups bleiben importierbar; korrigierte Zeiten und unbekannte Zusatzfelder überleben den JSON-Roundtrip. Ernährungs-/Wochenpunkte, Pet-Futter und andere App-Daten werden nicht verändert.

## Prüfungen

Automatisierte Logik-, Backup-, Alarm- und Android-Dialogtests decken die Abläufe und ungültige Eingaben ab. Die Dialoge verwenden dieselben Android-Ansichten wie die App. Robolectric-Renderings sind keine Geräte-Screenshots; ein physischer Gerätetest bleibt offen. Bestehende globale Lint-Befunde sind weiterhin dokumentierte Altlasten.

Manuell prüfen:

- Ab jetzt / früher starten, Datum und Uhrzeit wählen, beide Picker und den Gesamtdialog abbrechen.
- Beginn früher und später korrigieren; Vorschau, gespeichertes Ziel und Erinnerung vergleichen. Auch eine individuell geänderte Zieldauer erhalten.
- Vor und nach Zielerreichung beenden; tatsächliche Endzeit wählen. Anschließend im Verlauf verlängern und wieder kürzen, ohne zusätzliche Einträge.
- Ungültige Reihenfolge, heutige zukünftige Uhrzeit, Gerätezeit-Rückstellung und Sommer-/Winterzeit prüfen.
- Endzeit eines alten Eintrags bei neuem laufendem Fasten ändern; dessen Timer, Alarm und Benachrichtigung erhalten.
- Sieben/mehr als sieben Einträge, Hell-/Dunkelmodus, große Schrift, kleine Displays und beide Android-Navigationsarten.
- App-Rückkehr, Prozessneustart und Backup-Roundtrip: gespeicherte Zeiten bleiben bestehen. Offene, noch nicht bestätigte Dialogentwürfe sind keine gespeicherten Fastenzeiten.
