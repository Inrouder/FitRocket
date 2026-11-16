# FitRocket – Entwicklerhandbuch

## Voraussetzungen
Java/JDK 17, Android SDK Platform 35, Android Build Tools 35, Gradle 8.11.1 (GitHub Actions). Repository klonen und im Projektverzeichnis arbeiten. Die Paket-ID lautet `de.inrouder.fitrocket`.

## Lokale Prüfungen
```bash
python3 scripts/validate_catalog.py
python3 scripts/validate_ai_schema.py
gradle :app:testDebugUnitTest :app:assembleDebug --no-daemon --stacktrace
```
Unter Android Studio kann der Gradle-Sync und ein Debug-Build auf einem Emulator/Telefon erfolgen. Die Debug-APK ist **kein** signiertes stabiles Update für bestehende Release-Installationen.

## Vor jeder Änderung
1. Zielklasse/-methode und genutzte Persistenz-Keys identifizieren; ohne Test keine Migration annehmen.
2. Bei Nahrungsmitteln feste IDs nicht wiederverwenden oder Referenzen in Rezepten brechen.
3. Bei Punkten `FORMULAS.md` und bestehende Unit-Tests lesen. KI darf keine eigene Punkteformel erfinden.
4. Bei neuer UI Navigation, Dark Mode, Bildschirmgrößen und Insets beachten.
5. Bei neuen Feldern Export, Import, Datenmigrationslogik und alte Backups prüfen.
6. Tests ergänzen, Build und CI ansehen, Release nur mit unveränderter App-ID und dem bestehenden Signaturkeystore.

## Ein neues Feature einbauen
Betroffene Dateien und Datenfluss im Pull Request dokumentieren; Inputs validieren; externe Requests zeit-/größenbegrenzen und niemals im UI-Thread ausführen; Fehler und leere Zustände abfangen; Tests für Normal-, Rand- und Rückwärtskompatibilitätsfälle schreiben. Feature-Name und Nutzerverhalten in CHANGELOG sowie Dokumentation aufnehmen. Keine API-Schlüssel, JKS-Dateien, Tokens oder echten Nutzerdaten committen.

## Testszenarien (manuell)
- Navigation: Alle fünf Namen sind auch bei großer Android-Schrift sichtbar und nicht abgeschnitten, sowohl Gesten als auch 3-Tasten-Navbar.
- Rezepte: KI-Rezept, eigenes Rezept, Link-Import ohne/mit KI, ungültige URL und fehlende Zutaten.
- Bilder: Galerie, Kamera, Bearbeiten, Neuinstallation über Backup (Foto bleibt erhalten), nicht mehr zugängliche ältere content-URIs.
- Punkte: Änderung von Mengen, Tag-/Wochenwechsel, Bewegungsgutschrift am selben Tag, 0/50/100%-Freigabe.
- Sicherung: kompletter Export/Import inkl. Nutzerrezepte und Fotos, historischer Backup-Datensatz und fehlerhafte Datei.
- Releases: installierte signierte Altversion über neue signierte APK aktualisieren und kontrollieren, dass Daten erhalten bleiben.

## GitHub Actions und Signieren
Der Workflow `.github/workflows/android-apk.yml` läuft auf `main`, Pull Requests und manuell; er validiert Daten, testet und baut eine Debug-APK. Die stabile Release-APK wird nur mit vollständig konfigurierten Signatur-Secrets gebaut, per apksigner geprüft und veröffentlicht. Siehe [SIGNING.md](SIGNING.md). Workflow-Dateinamen/Artefaktnamen können ältere hartkodierte Versionsangaben enthalten; beim Release mit Versionseinstellungen abgleichen.

## Definition of Done
Implementierter Code + Tests + verifizierter CI-Build + nachvollziehbare Änderung in CHANGELOG + aktualisierte technische Dokumentation + bei Datenformatänderungen getesteter Backup-Roundtrip. Unbestätigten Build ausdrücklich als unbestätigt kennzeichnen.


## Prüfszenarien Intervallfasten / Punkteübernahme
- Heute-Tagebuch → Fasten öffnen → alle sechs Pläne; aktiver Planwechsel bestätigt die neue Zielzeit. Hauptnavigation und Zurück prüfen.
- Starten, Start/Ziel per Datum und Uhrzeit ändern; ungültige Reihenfolge, Zukunftsstart und mehr als 24 Stunden ohne Änderung abweisen. Ziel in der Vergangenheit zeigt „Ziel erreicht“.
- App schließen/Prozess beenden, erneut öffnen und Gerät drehen; Uhrzeit, Dauer und Plan bleiben erhalten. Mitternacht und Sommerzeitwechsel prüfen.
- Vor und nach Ziel beenden (ein Verlaufseintrag), abbrechen (kein Eintrag), danach neu starten. Dialogabbruch verändert keine Daten.
- Hell/Dunkel, schmales Display, große Systemschrift und Android-Systemnavigation prüfen. Alle Aktionsflächen bleiben durch Scrollen erreichbar.
- Profil → Punktebudget → Übernahme aus/an: aktuelle Woche neu berechnet, Zehntelpunkte und 4-Punkte-Grenze korrekt, Bewegung und Überschreitungen bleiben bestehen. Montag übernimmt nichts aus der Vorwoche.
- JSON-Roundtrip mit Verlauf, aktivem Fasten und deaktiviertem Übertrag; alte Backups ohne Zusatzfelder; fehlerhafte Fastendaten vor Ersetzung zurückweisen. Rezeptfotos/Favoriten weiterhin erhalten.


## Pixel-Pet und Fastenziel-Benachrichtigungen
Siehe [PET_AND_FASTING_REMINDERS.md](PET_AND_FASTING_REMINDERS.md) für Datenfelder und manuelle Szenarien. Unit-Tests prüfen insbesondere die Unabhängigkeit des realen Punktekontos, einmalige Futtergutschrift, Stimmungswechsel, Wachstum, Zubehör und Backup. Die PNG-Atlanten werden auf alle erforderlichen belegten Sprite-Felder geprüft. Android-Oberflächen-/Alarmtests verwenden Robolectric mit Android-Ressourcen; Native-Graphics-Renderings unter `app/build/outputs/pet-previews` sind JVM-Renderings und keine echten Geräte-Screenshots.

### Pet-Bewegung (v0.18.1)
`FitRocketPetMotionTest` prüft Laufphasen, Richtungswechsel, Interaktionsübergänge und pausierte Hintergrundzeit; `FitRocketPetRigTest` Kopfbefestigung und Gesichtsfreiraum. `FitRocketPetSceneTest` zeichnet dieselbe Canvas-Szene wie die App: alle fünf Stufen mit drei Kopfschmuckarten und allen Interaktionsposen sowie Bildfolgen für Füttern/Spielen/Streicheln. Die PNG-Kontaktbögen liegen unter `app/build/outputs/pet-previews/pet-outfit-*.png`, Sequenzen unter `pet-motion-*.png`. Diese nach jeder Änderung der Rasterausschnitte oder Kopf-Slots visuell prüfen. Die nicht gleichmäßigen Rasterabstände sind in `FitRocketPetScene.load` ausdrücklich hinterlegt. Die Android-Flusstests prüfen zusätzlich die Wiederverwendung der Szene und unveränderte Futter-/Tagebuchwerte beim wiederholten Pflegen/Anziehen. Für eine praktische Bewegungsprüfung weiterhin ein echtes Gerät verwenden.

### Fastenzeiten (v0.18.2)
Die Logiktests prüfen rückwirkenden Start, frühere/spätere Startkorrektur mit erhaltener individueller Dauer, Sommer-/Winterzeit, gewähltes tatsächliches Ende, Verlaufskorrektur und atomare Ablehnung ungültiger Zeiten. `FitRocketFastingFlowTest` verwendet echte Android-DatePicker-/TimePicker-/Bestätigungsdialoge unter Robolectric API 28/35; nach Buttonklicks den pausierten Main-Looper ausführen. Tests prüfen Abbruch, Vorschau, Persistenz, Alarmersetzung, Benachrichtigungserhalt beim Korrigieren alter Einträge, veraltete Dialoge und erneut aufgebaute Detailseiten. Native Dialog-Renderings liegen im bestehenden CI-Vorschauartefakt unter `app/build/outputs/pet-previews/fasting-*.png`; keine echten Geräte-Screenshots. Manuelle Szenarien: [FASTING_TIMES.md](FASTING_TIMES.md).
