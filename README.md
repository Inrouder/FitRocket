# FitRocket 🚀

Unabhängige Android-App mit spielerischem Punkteglas, Ernährungstagebuch und historischer ZeroPoint-Datenbasis. **Nicht offiziell mit WeightWatchers verbunden.**

## APK installieren
[Android APK – GitHub Actions](https://github.com/Inrouder/FitRocket/actions/workflows/android-apk.yml) → erfolgreichen Workflow-Lauf öffnen → **Artifacts** → `FitRocket-v0.3.0-debug-apk` → ZIP entpacken → `app-debug.apk` installieren. Es ist eine **Debug-APK**, keine release-signierte Store-Version.

## Version 0.3.0
- 283 Lebensmittel; ursprüngliche 269 Datensätze unverändert weitergeführt
- 14 neue Obst-/Fruchteinträge auf Grundlage historischer US-ZeroPoint-Listen; **separat als US-Historie** gekennzeichnet, nicht als verifizierte deutsche Einträge
- Durchsuchbare Lebensmitteldatenbank (Name, alternative Bezeichnung, Kategorie)
- Portionsfaktor, individuelle Punktkorrektur, Frühstück/Mittag/Abend/Snack/Getränke
- Lokales Tagesjournal, Verlauf, Einträge bearbeiten/löschen
- Tagesbudget und frei konfigurierbares Wochen-Extra
- Wochen- und Monatsübersicht (fehlende Tage werden nicht als 0-Punkte-Tage gewertet)
- Gewichtseinträge, JSON-Backup und bestätigungspflichtiger JSON-Import
- Punk­teglas mit vereinfachtem Überlaufeffekt
- Experimentelle Offline-Texterkennung: **regelbasiert, kein KI-Modell**

### Noch nicht implementiert
Echte animierte Troll-Figur, Google-Anmeldung, LLM-KI, Smartwatch/Health Connect, Push-Hinweise, automatische historische WW-Punkteformel, App-Release-Signierung.

## Datenqualität
236 Einträge aus der deutschen historischen Liste, 23 ungeprüfte Beispiele und 10 Prüffälle; Erweiterungen tragen den Kennzeichner `historical_us`. Ungeprüfte Werte sind keine bestätigten historischen WW-Punkte. ZeroPoint gilt nicht für Säfte, Smoothies, Trockenobst oder Zusätze. Mengen und Zubereitungen müssen vom Nutzer geprüft werden.

## Build
JDK 17; Android SDK 35; Gradle 8.11.1. `gradle :app:assembleDebug`.

## Datenschutz
Mahlzeiten, Budgets und Gewicht werden lokal gespeichert. Die Version hat keinen Netzwerkzugriff und sendet keine Ernährungsdaten an Dritte.
