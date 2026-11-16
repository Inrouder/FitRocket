# FitRocket v0.17.0 – Intervallfasten & optionale Punkteübernahme

## Intervallfasten

Im heutigen Tagebuch und unter **Profil → Intervallfasten** findest du die Pläne **12:12, 14:10, 16:8, 17:7, 18:6 und 20:4**. Starte nach deiner letzten Mahlzeit, passe Beginn und Ziel bei Bedarf an und verfolge die vergangene Zeit sowie deinen Fortschritt. Der Timer bleibt auch nach dem Schließen der App erhalten.

**Fasten beenden** speichert deine tatsächliche Dauer im lokalen Verlauf, auch wenn du früher aufhörst. **Fasten abbrechen** verwirft das laufende Fasten ohne Verlaufseintrag. Die Darstellung nutzt die FitRocket-Cards und unterstützt Hell- und Dunkelmodus. Die Zeitangaben messen keine körperlichen Fastenphasen und beeinflussen deine Ernährungspunkte nicht.

## Übrige Tagespunkte übernehmen oder darauf verzichten

Unter **Profil → Punktebudget → Übrige Tagespunkte übernehmen** kannst du die Übernahme ein- oder ausschalten. Standard bleibt **an**: Nach Tagesabschluss kommen bis zu **4 übrige Tagespunkte ins Wochenkonto**. Ausgeschaltet verfallen diese Restpunkte. Die Auswahl berechnet die gesamte aktuelle Woche sofort neu. Dein Tagesbudget und die gewählte Bewegungsgutschrift bleiben davon unabhängig.

## Daten und Update

- Fastenplan, laufendes Fasten, Verlauf und Übernahme-Einstellung werden mit dem JSON-Backup exportiert und wiederhergestellt. Alte Sicherungen bleiben importierbar; keine Bestandsmigration.
- App-ID bleibt `de.inrouder.fitrocket`; Version **0.17.0**, Versionscode **47**.
- Der bestehende GitHub-Workflow erstellt die Release-APK ausschließlich mit der eingerichteten dauerhaften Signatur und prüft sie vor der Veröffentlichung.
- Nach Veröffentlichung unter **Profil → In-App-Update prüfen** nach der neuen Version suchen. Android bestätigt die Installation wie bisher.

## Prüfstand

Die Funktionsänderungen haben 127 Unit-Tests sowie den Debug-Build und die Katalog-/Schemaprüfungen bestanden. Beim Release werden diese Prüfungen erneut ausgeführt. Die visuelle Prüfung auf einem Android-Gerät bleibt offen; der lokale Emulator erreichte keinen stabilen Systemstart.
