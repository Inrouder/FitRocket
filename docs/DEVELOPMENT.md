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
