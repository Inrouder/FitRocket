# FitRocket 🚀

**Dein persönliches Ernährungstagebuch mit Bewegungspunkten.** FitRocket arbeitet lokal auf Android, nutzt einen eigenen Nährwertscore und bietet eine gemütliche Oberfläche mit Tagesbilanz, Lebensmittelsuche und Wochenpuffer.

## APK installieren
1. [GitHub Actions – Android APK](https://github.com/Inrouder/FitRocket/actions/workflows/android-apk.yml) öffnen.
2. Den neuesten **erfolgreichen** Build auswählen.
3. Unter *Artifacts* das Paket `FitRocket-v0.8.0-debug-apk` herunterladen.
4. ZIP entpacken und `app-debug.apk` installieren.

Eine Debug-APK ist zum privaten Testen gedacht. Deine bereits lokal gespeicherten Mahlzeiten bleiben bei regulärem Update mit derselben App-ID erhalten; vor größeren Änderungen empfiehlt sich **Mehr → Backup exportieren**.

## Neue Funktionen in Version 0.8.0
- **Smartwatch & Health Connect (Android 14+):** Einmalige Freigabe, Schritte und Trainingseinheiten der aktuellen Woche einlesen.
- Importierte Trainings werden mit eindeutiger Datenquellen-Kennung aktualisiert, statt beim nächsten Abgleich nochmals verbucht.
- Schrittzahlen werden über Health Connect aggregiert; im Tagebuch können Uhr und manuelle Eingaben als bevorzugte Quelle gewählt werden.
- Optional automatische Synchronisation beim Öffnen (höchstens alle 20 Minuten), zusätzlich jederzeit manuell.
- Nur Lesezugriff auf Schritte und Trainingsdaten; Daten bleiben lokal, Verbindung und importierte Daten können entfernt werden.
- Workout-Überschneidungen werden mit Zeit und Dauer abgeglichen; Workout-Schritte werden bei den übrigen Tages-Schritten berücksichtigt.
- Neue Backups speichern importierte Uhr-Schritte und Eintragssperren, stellen Zugriffsrechte jedoch nicht automatisch wieder her.

## Bewegungsgrundlagen aus Version 0.7.0
- Eigenständige FitRocket-Rechenregeln statt externer Programme.
- Bewegungsprofil mit **Alter, Körpergröße, Gewicht und wählbarem Stoffwechsel-Referenzprofil**.
- Persönlicher Ruheenergiebedarf als Ausgangspunkt der Aktivitätsabschätzung.
- **9 Sportarten × 3 Intensitäten**, Trainingsdauer und geschätzte *aktive* Kilokalorien.
- **Schritttagebuch**, individuell einstellbare Grundschritte (Standard 3.000), Tempo (80/100/120 Schritte/Minute).
- Schutz vor Doppelzählung: geschätzte oder eingegebene Schritte aus Gehen/Laufen werden bei der Tages-Schrittgutschrift abgezogen.
- Gutschrift von Bewegungspunkten zu **0 %, 50 % oder 100 %** ins Wochenbudget.
- Tägliches Budget unverändert. Wochenpuffer berücksichtigt durch Essen überzogene Tagesbudgets.
- Einstellbarer Faktor **30–150 aktive kcal je Bewegungspunkt** (Standard 65).
- Vollständige JSON-Datensicherung inklusive Profil, Aktivität, Schritte und Punkte-Einstellungen. Importiert auch Backups älterer Versionen.
- 358 lokale Lebensmittel: 280 Basislebensmittel, 23 Portionsvorschläge, 45 Nährwertprofile und 10 manuell bewertbare Lebensmittel.
- GitHub Actions führt sowohl **Unit-Tests** der Formeln als auch die APK-Kompilierung aus.

## App-Bereiche
- **Heute:** Tagespunkte, verfügbare Punkte, Wochenpuffer und Bewegung.
- **Suche:** Lebensmittel auswählen, Portionsmenge ändern und eintragen.
- **Tagebuch:** Einträge ansehen, korrigieren und löschen.
- **Mehr:** Aktivitätskonto, Wochen-/Monatsstatistik, Gewicht, Punktebudget und Backup.

## Transparente Regeln
Die App nutzt ein selbst definiertes Punktesystem. Die Werte der Basislebensmittel und vorhandenen Vorschlagsportionen sind die FitRocket-Startdaten, nicht Produktmessungen. Marken- und Rezeptangaben können abweichen. Details findest du in [docs/FORMULAS.md](docs/FORMULAS.md).

**Bewegungsdaten sind derzeit manuell:** Es gibt noch keine automatische direkte Wear-OS-Uhr-App, keine automatische Schrittzählung ohne kompatible Datenquelle und keine Online-KI. Das System funktioniert offline; es fordert keinen Account und benötigt keinen Zugang zu privaten Fitnessdiensten.

## Entwicklung
- JDK 17
- Android SDK 35
- Gradle 8.11.1
- Testlauf: `gradle :app:testDebugUnitTest :app:assembleDebug`
- Package-ID: `de.inrouder.fitrocket`

FitRocket ist ein eigenständiges Open-Source-Projekt mit lokalen Nutzerdaten.
