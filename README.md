# FitRocket 🚀

Eine unabhängige Android-App für ein spielerisches Ernährungstagebuch mit Tagesbudget und historischem Blauen-Plan-Datenbestand. Kein offizielles WeightWatchers-Produkt.

## APK
[Actions → Android APK](https://github.com/Inrouder/FitRocket/actions/workflows/android-apk.yml) → erfolgreichen Lauf öffnen → unter **Artifacts** die Debug-APK als ZIP herunterladen, entpacken und `app-debug.apk` installieren.

## Version 0.2.0
- **269 Datensätze**: 236 historisch bestätigte ZeroPoint-Einträge, 10 Prüffälle, 23 ungeprüfte Punktebeispiele
- Durchsuchbare Lebensmittelauswahl mit mindestens 2 Zeichen
- Portionen und Punkte vor dem Buchen prüfen/anpassen
- Tagebuch und Budgets bleiben auf dem Gerät gespeichert
- Glasfüllstand und Überlauf-Anzeige
- Wochenübersicht

**Wichtig:** Bei Beispielen sind die Punktwerte nicht offiziell verifiziert. ZeroPoint gilt nur für die exakt passende Zubereitung, und Zusätze müssen separat erfasst werden. Prüffälle erfordern manuelle Punkteangaben. Die genaue historische SmartPoints-Rechenformel ist nicht enthalten.

## Entwicklungsstatus
Version 0.2.0 ist eine **Debug-/MVP-Version**. KI, Google Login, Bluetooth/Wearables, Health Connect, echte Troll-Animation, Push, Monatsstatistiken und Produktionssignierung fehlen noch.

## Build
JDK 17; Android SDK 35; Gradle 8.11.1; `gradle :app:assembleDebug`.
