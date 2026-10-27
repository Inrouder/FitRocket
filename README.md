# FitRocket 🚀

FitRocket ist ein eigenständiges Android-Ernährungs- und Bewegungstagebuch mit einem eigenen FitRocket-Punktesystem. Die Daten bleiben auf deinem Gerät. Die App bietet warme Pastellfarben, übersichtliche Tageskarten und ein flexibel nutzbares Wochenkonto.

## FitRocket v0.11.0 herunterladen

1. Den [Android-APK-Workflow](https://github.com/Inrouder/FitRocket/actions/workflows/android-apk.yml) aufrufen.
2. Den aktuellsten erfolgreichen GitHub-Actions-Lauf auswählen.
3. Unter **Artifacts** das Paket `FitRocket-v0.11.0-debug-apk` herunterladen.
4. ZIP entpacken und `app-debug.apk` installieren.

Bei regulären Updates derselben Debug-Signatur bleiben gespeicherte Daten erhalten. Vor Neuinstallation oder Telefonwechsel immer **Mehr → Backup exportieren** verwenden. Eine Deinstallation ohne Backup löscht die lokalen FitRocket-Daten.


## Neu in FitRocket v0.11.0 – Google Gemini als Standard-KI

**FitRocket verwendet ab sofort Google Gemini** für personalisierte Rezepte, beschriebene Mahlzeiten und neue Lebensmittel. Die bestehende FitRocket-Punkteformel und 280 Basislebensmittel mit 0 Punkten bleiben unverändert.

- **Gemini 2.5 Flash-Lite** als Standardmodell für sparsame Anfragen innerhalb des verfügbaren kostenlosen API-Kontingents.
- **Gemini 2.5 Flash** als wählbare Alternative für komplexere Rezepte. Die Nutzung ist ebenfalls innerhalb verfügbarer Gratislimits möglich.
- **OpenAI bleibt optional:** Über **Mehr → KI-Assistent → KI-Anbieter wechseln** lässt sich der bisherige Dienst wieder auswählen.
- **Getrennte API-Schlüssel:** Gemini-Schlüssel aus Google AI Studio und bestehende OpenAI-Schlüssel werden unabhängig voneinander mit Android Keystore geschützt. Der bisherige OpenAI-Schlüssel bleibt beim Update erhalten.
- **Klare Fehlermeldungen** bei ungültigem Gemini-Schlüssel, HTTP 429 (Kontingent/Anfragerate), nicht verfügbarem Modell und Verbindungsproblemen.
- **Keine automatische Anmeldung:** Für Gemini benötigst du einen kostenlosen API-Schlüssel von https://aistudio.google.com/apikey. Ein normales Google- oder Gemini-App-Login reicht nicht aus.
- **Kein erzwungenes Bezahlen:** Du kannst bei ausgeschöpftem Gratis-Kontingent später erneut versuchen; FitRocket wechselt niemals automatisch auf ein Bezahlmodell oder einen anderen Anbieter.
- **Datenschutz:** Für Anfragen werden nur die eingegebene Beschreibung, freiwillig hinterlegte Vorlieben und öffentliche FitRocket-Kataloghinweise versendet. Nicht übertragen werden automatisch Gewicht, Tagebuchhistorie oder Health-Connect-Daten. Google darf Eingaben im kostenlosen API-Tarif unter Umständen zur Produktverbesserung verwenden.

Die neue Gemini-Anbindung verwendet die offizielle HTTPS-REST-Schnittstelle, JSON-Ausgabe und weiterhin die **lokale FitRocket-Prüfung von Zutaten und Punktwerten**. Die API ist kostenlos nur innerhalb ihrer aktuellen Nutzungsgrenzen. Siehe [KI-Dokumentation](docs/AI_ASSISTANT.md) und [Gemini-Preise](https://ai.google.dev/gemini-api/docs/pricing).

## KI-Funktionen aus Version 0.10.0

FitRocket hat jetzt einen optionalen Online-Küchenassistenten. **Die KI ermittelt keine FitRocket-Punkte selbst**: Sie schlägt nur Zutaten, Mengen und Nährwerte vor, die App rechnet anschließend mit dem vorhandenen FitRocket-Modell.

- **Rezeptideen nach Vorlieben:** Essen, Vorlieben und Ausschlüsse frei beschreiben. Die KI erstellt eine Zutatenliste und Zubereitungsanleitung; Punkte pro Portion werden lokal berechnet. Vorschläge lassen sich im integrierten Rezeptbuch speichern.
- **Gegessene Mahlzeiten erkennen:** Freitext wie „Reis mit Hähnchen, Gemüse und etwas Sahnesoße“ wird in geschätzte Zutaten und Mengen zerlegt. Nach Kontrolle kannst du die Mahlzeit im Tagebuch eintragen.
- **Neue Lebensmittel aus Beschreibungen:** Produkt oder Lebensmittel nennen; die KI schätzt kcal, gesättigte Fette, Zucker und Eiweiß je 100 g/ml. Du kannst die Werte korrigieren und sie nach ausdrücklicher Bestätigung im lokalen Lebensmittelkatalog speichern.
- **Vollständige Vorschau:** Zutaten ergänzen, entfernen, Namen, Mengen und die vier Nährwerte ändern; FitRocket-Punkte werden sofort neu berechnet.
- **0-Punkte-Grundzutaten bleiben erhalten:** 0 Punkte gelten nur, wenn die KI die unveränderte, exakt passende Kombination aus Produkt-ID und Namen im vorhandenen FitRocket-Basiskatalog verwendet. Andere Zutaten werden mit der eigenen Formel berechnet.
- **Offline ohne KI:** Eigene Lebensmittel anhand der Nährwertangaben der Verpackung erstellen und speichern.
- **Backup-Schema 5:** Eigene KI-Lebensmittel, gespeicherte KI-Rezepte und Geschmackswünsche werden exportiert/importiert. Der geheime API-Schlüssel wird niemals exportiert.

### Einrichtung
1. Unter **Mehr → KI-Assistent** den Anbieter (standardmäßig **Google Gemini**) auswählen und **API-Schlüssel einrichten** öffnen.
2. Einen **persönlichen OpenAI-API-Schlüssel** von https://platform.openai.com/api-keys eintragen.
3. Unter **Vorlieben bearbeiten** z. B. vegetarisch, proteinreich oder „ohne Pilze“ speichern.
4. Rezept, Mahlzeit oder Produkt beschreiben und das Ergebnis **vor dem Speichern überprüfen**.

**Wichtig zu Kosten und Datenschutz:** Die Nutzung der OpenAI API wird unabhängig von ChatGPT-Abonnements nach API-Verbrauch abgerechnet. Die App sendet nur die von dir eingegebene Anfrage, deine ausdrücklich gespeicherten Vorlieben und eine kleine Auswahl öffentlicher Lebensmittelbezeichnungen an die API – nicht automatisch Gewicht, Tagebuch oder Health-Connect-Daten. OpenAI kann die Anfragen nach seinen eigenen Datenschutz- und Aufbewahrungsbedingungen verarbeiten. Der Schlüssel liegt im Android Keystore verschlüsselt auf deinem Gerät, nicht in GitHub oder im Backup. Für eine öffentlich verteilte App wird ein Server-Proxy empfohlen, der den Schlüssel niemals an den Client herausgibt. Weitere Details unter [docs/AI_ASSISTANT.md](docs/AI_ASSISTANT.md).

**Ohne API-Schlüssel bleiben alle bisherigen App-Funktionen nutzbar.** Ein Live-Test der KI-API benötigt einen gültigen eigenen Schlüssel und ist nicht Bestandteil der Android-Unit-Tests.

## Funktionen aus Version 0.9.0

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
