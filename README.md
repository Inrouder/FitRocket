# FitRocket 🚀

Cozy Android-Ernährungstagebuch mit lokaler Lebensmitteldatenbank.

## Version 0.4.0
- Pastellfarbene Benutzeroberfläche nach dem freigegebenen Vier-Screens-Mockup
- Heute: Halbkreis-Punkteanzeige Grün–Orange–Rot, kompakte Tagesbilanz, Schnellzugriff
- Suche: abgerundete Lebensmittellisten und Portionsdialoge
- Tagebuch: Mahlzeitenkarten, Bearbeiten und Löschen
- Mehr: Statistik, Gewicht, Budget, JSON-Backup
- 283 lokale Datenbankeinträge, bestehende Daten bleiben erhalten

## Installation
Öffne [Actions – Android APK](https://github.com/Inrouder/FitRocket/actions/workflows/android-apk.yml), wähle einen erfolgreichen Lauf, lade das Artifact **FitRocket-v0.4.0-debug-apk**, entpacke das ZIP und installiere `app-debug.apk`.

## Weiterhin geplant
Eigener Hintergrund-Illustrationsasset, echte Troll-Animation, LLM-KI, Google-Anmeldung, Wearables, Benachrichtigungen und signierter Release.
Punktwerte ohne historischen Originalnachweis bleiben nur Richtwerte; die Datenbank behält ihre Statusfelder.

## Datenbank v0.6.0
358 Einträge: 283 bisherige, 30 ergänzte historische US-ZeroPoint-Einträge, 45 generische Nährwertprofile. Die 45 Punktewerte sind berechnete Annäherungen, keine offiziellen WW-Werte. Originalquellen und Regeln in docs/DATABASE_AUDIT.md.

## Bewegung (v0.6.0)
Unter „Mehr → Aktivitäten & Bonuspunkte“ oder auf „Heute“ Training erfassen. Intensität und Minuten auswählen und ein Körpergewicht eingeben. Die App schätzt aktive Kalorien anhand eines MET-Faktors über dem Ruheverbrauch. Daraus erfolgt ein Schätzwert bei ca. 65 aktiven kcal je Punkt. Die vereinfachte Näherung ist kein offizieller WW-FitPoints-Rechner.

Es gibt die Einstellung 0 / 50 / 100 % Gutschrift ins **Wochen-Extra**, standardmäßig 0 %. Tagesbudget bleibt unverändert; der Wochenpuffer berücksichtigt bereits ausgegebene Tagesüberschreitungen. Punkte gelten nur in der aktuellen Kalenderwoche, Aktivitäten bleiben im Verlauf gespeichert. Direkte Schritte-/Smartwatch-Synchronisation und ein 3000-Schritte-Schwellenwert sind nicht implementiert, um doppelte Bewegungszählung zu vermeiden.

Historische WW-Referenzen: https://www.weightwatchers.com/de/blog/fitpoints-verstehen und https://www.weightwatchers.com/de/blog/alles-ueber-fitpointsr.
