# FitRocket 🚀

Android-Punktetagebuch mit animiertem Punkteglas und Troll, unabhängig von WeightWatchers.

## APK herunterladen
[GitHub Actions](https://github.com/Inrouder/FitRocket/actions/workflows/android-apk.yml) öffnen → erfolgreiches **Android APK** Build wählen → **Artifacts** → **FitRocket-v0.1.0-debug-apk** herunterladen → ZIP entpacken → `app-debug.apk` installieren.

## Funktionen 0.1.0
- Mahlzeiten manuell eintragen und Punkte zählen
- Tagesbudget festlegen, Punkteglas und Troll-Anzeige
- Wochenübersicht, lokaler Datenspeicher
- Beispielhafte Lebensmittelauswahl. Der Import der gesamten 269-Zeilen-Datenbank folgt nach vollständiger Integration.

## Entwicklungsumgebung
JDK 17, Android SDK 35, Gradle 8.11.1. `gradle :app:assembleDebug`.

## Hinweise
Nicht mit WeightWatchers verbunden. SmartPoints-Werte aus der historischen Vorlage sind teilweise nicht verifiziert. Keine medizinische Beratung. Daten werden lokal gespeichert. KI, Google-Anmeldung, Fitnessintegration, Push und Monatsstatistik sind noch ausstehend.
