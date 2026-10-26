# FitRocket 🚀

FitRocket ist ein eigenständiges Android-Ernährungs- und Bewegungstagebuch mit einem eigenen FitRocket-Punktesystem. Die Daten bleiben auf deinem Gerät. Die App bietet warme Pastellfarben, übersichtliche Tageskarten und ein flexibel nutzbares Wochenkonto.

## FitRocket v0.9.0 herunterladen

1. Den [Android-APK-Workflow](https://github.com/Inrouder/FitRocket/actions/workflows/android-apk.yml) aufrufen.
2. Den aktuellsten erfolgreichen GitHub-Actions-Lauf auswählen.
3. Unter **Artifacts** das Paket `FitRocket-v0.9.0-debug-apk` herunterladen.
4. ZIP entpacken und `app-debug.apk` installieren.

Bei regulären Updates derselben Debug-Signatur bleiben gespeicherte Daten erhalten. Vor Neuinstallation oder Telefonwechsel immer **Mehr → Backup exportieren** verwenden. Eine Deinstallation ohne Backup löscht die lokalen FitRocket-Daten.

## Neue Funktionen – Version 0.9.0

### Tagesübersicht und Mahlzeiten
- Übersichtlichere **Heute**-Ansicht mit Punktekreis, Schnellaktionen, Wochenkonto, Wasser und Gewicht.
- Mahlzeiten nach **Frühstück, Mittagessen, Abendessen, Snacks und Getränken** getrennt.
- Einträge und Punktesumme pro Mahlzeitengruppe; Button `+` öffnet die Lebensmittelsuche mit vorausgewählter Mahlzeit.
- **Tagebuch** mit Kalender, Pfeilnavigation und gruppierten Einträgen.
- Vorhandene ungruppierte ältere Mahlzeiten werden unter Snacks aufgeführt.

### Barcode und Artikel
- **Barcode scannen** über Google Code Scanner (`play-services-code-scanner:16.1.0`).
- Alternative **Barcode manuell eingeben**, falls Google Play-Dienste nicht verfügbar sind.
- Produktnamen und Nährwertdaten über **Open Food Facts API v3**, bei Internetverbindung.
- FitRocket berechnet die Punkte nach dem eigenen Nahrungsscore aus kcal, gesättigtem Fett, Zucker und Protein.
- Fehlende Nährwerte werden **nicht** als 0 interpretiert: Fehlende Werte können vom Nährwertetikett ergänzt werden.
- Gespeicherte Scanprodukte erscheinen offline in der normalen Lebensmittelsuche.
- Bei Daten von Open Food Facts die Herstellerangaben prüfen: Nutzerbeiträge können Fehler enthalten.

### Rezeptideen
- **13 offline gespeicherte Rezeptvorschläge**, nach Mahlzeit filterbar.
- Genaue Zutatenmengen aus dem vorhandenen Lebensmitteldatenbestand, Zubereitungsanleitung und geschätzte Punkte je Portion.
- Rezept kann mit wählbarer Portionsanzahl ins Tagebuch übernommen werden.
- Zusätzliche Soßen, Öl und Varianten ggf. separat erfassen.

### Wasser
- **Bildliche 250-ml-Gläser** zum Antippen und Befüllen, Plus-/Minus-Tasten.
- Wasserbilanz pro Datum, auch im Tagebuch.
- Einstellbares Tagesziel (250–6000 ml in 250-ml-Schritten), Standard 1500 ml.

### Gewicht
- **Gewichtsskala** als Schieberegler plus genaue Eingabe in kg.
- Individuelles Zielgewicht, Start- und aktuelles Gewicht.
- Grafischer Gewichtsverlauf mit Messpunkten und Ziel-Linie.
- Bei vorhandenem Aktivitätsprofil werden neue Gewichtswerte für künftige Bewegungsberechnungen übernommen.

### Bestehende Funktionen
- **280 Basislebensmittel mit 0 FitRocket-Punkten**, insgesamt 358 vorinstallierte Lebensmittel.
- **28 Wochenstartpunkte** jeden Montag, dazu bis zu vier ungenutzte Tagespunkte vom abgeschlossenen Tag.
- Persönliche Bewegungsberechnung, Training, Schritte und optionale Synchronisation mit Health Connect (Android 14+).
- Wahlweise 0 %, 50 % oder 100 % der Bewegungspunkte zum Wochenbudget hinzufügen.
- JSON-Backup: ab Version 0.9.0 auch Wassereinträge, Trinkziel, Wunschgewicht und gescannte Produkte. Ältere Backups werden weiterhin angenommen.

## Berechnungen
Die [FitRocket-Formeln](docs/FORMULAS.md) beschreiben die eigenen Ernährungs- und Bewegungs-Punkteregeln. ZeroPoint-Basislebensmittel bleiben bei 0, Zutaten und Zusätze werden separat behandelt.

## Datenschutz und Datenquellen
- Bei Nutzung der **Barcode-Produktsuche** wird die Barcode-Ziffernfolge an Open Food Facts übertragen; weder dein Tagebuch noch Profil- oder Gewichtsdaten werden dabei versendet.
- Open Food Facts ist eine offene, von Nutzenden gepflegte Lebensmitteldatenbank: https://openfoodfacts.org/ – Datenlizenz: https://opendatacommons.org/licenses/odbl/
- Google Code Scanner nutzt die Google Play-Dienste und benötigt keine von FitRocket selbst angeforderte Kameraberechtigung. Siehe: https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner
- Health Connect benötigt separate Android-Freigaben; Details: [Health Connect](docs/HEALTH_CONNECT.md).
- Die App lädt keine Lebensmittelbilder von externen Diensten herunter und lädt keine privaten Gesundheitsdaten in eine Cloud hoch.
- Die automatische Android-System-Sicherung ist deaktiviert. Eigenständiger Datenexport ist möglich.

## Entwicklung
- Android Java 17, SDK 35, minSdk 26, targetSdk 35
- Debug-Build via Gradle 8.11.1
- Test- und Build-Befehl: `gradle :app:testDebugUnitTest :app:assembleDebug`
- Katalog-Prüfung: `python3 scripts/validate_catalog.py`
- Package-ID `de.inrouder.fitrocket`

Entwickelt in diesem [GitHub-Repository](https://github.com/Inrouder/FitRocket).
