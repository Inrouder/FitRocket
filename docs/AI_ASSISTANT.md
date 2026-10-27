# FitRocket-KI-Assistent v0.11.0

## Ziele
FitRocket soll unabhängig von externen Diätprogrammen bleiben. Die KI erstellt Vorschläge und Schätzungen, während **FitRocket die Punkte ausschließlich lokal anhand der FitRocketMath-Formel berechnet**.

### Drei Modi
1. **Rezept generieren:** Ein Nutzer beschreibt Geschmack, Zutaten, Ausschlüsse, Zeitbudget und Essenswünsche. Die KI liefert einen strukturierten Rezeptvorschlag mit Zutatenmengen, Nährwerten, Portionsanzahl und Zubereitung.
2. **Freitext-Mahlzeit einschätzen:** Ein Nutzer beschreibt, was gegessen wurde. Die KI leitet mögliche Einzelzutaten, Portionen und ernährungsbezogene Unsicherheiten ab. Alle Angaben werden vor Tagebucheintrag von der Person überprüft.
3. **Produkt/Lebensmittel einschätzen:** Die KI liefert ein einziges vorgeschlagenes Nährwertprofil. Vor dem Speichern müssen Angaben überprüft werden. Für Markenprodukte sind tatsächliche Etiketten genauer.

Zusätzlich können Lebensmittel komplett **offline** aus eigenen Nährwertangaben neu angelegt werden.


## Standardanbieter: Google Gemini

Ab v0.11.0 verwendet FitRocket standardmäßig **Google Gemini**. Das empfohlene Standardmodell ist `gemini-2.5-flash-lite` (kostenlose Nutzung innerhalb der aktuellen Freigrenzen). Alternativ ist `gemini-2.5-flash` auswählbar. Beide Modelle sind nur verfügbar, sofern Googles API-Kontingent sie zulässt.

### Einrichtung
1. In https://aistudio.google.com/apikey einen persönlichen **Gemini API Key** erstellen.
2. In der App **Mehr → KI-Assistent** öffnen.
3. **Google Gemini** als Anbieter belassen, bei Bedarf zwischen Flash-Lite und Flash wählen.
4. **Google Gemini-Schlüssel einrichten** öffnen und den Schlüssel speichern.
5. Rezept, Mahlzeit oder Lebensmittel beschreiben und die vorgeschlagenen Zutaten/Nährwerte prüfen, bevor gespeichert wird.

**Wichtig:** Die kostenlose API-Stufe ist begrenzt, verfügbare Modelle/Kontingente können sich ändern. Die Google-Gemini-App-Anmeldung allein berechtigt nicht zur API-Nutzung. Google darf Inhalte aus kostenlosen API-Anfragen unter Umständen zur Verbesserung seiner Produkte verwenden. Vertrauliche Gesundheitsdetails sollten daher nicht in Freitext-Anfragen eingegeben werden. Ein privater Testzugang mit verschlüsseltem, selbst eingegebenem API-Schlüssel ist kein vollwertiger Ersatz für einen Backend-Proxy bei öffentlicher Distribution.

### API-Protokoll und Fehler
- URL: `https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`
- Authentifizierung: `x-goog-api-key` ausschließlich zur Laufzeit aus Android Keystore, niemals als Quelltextkonstante.
- Antwort: `generationConfig.responseMimeType = application/json` mit `responseJsonSchema`. Anschließend prüft `FitRocketAiRules` die vollständigen Angaben und berechnet die Punkte lokal.
- HTTP 401/403: Schlüssel ungültig/nicht autorisiert; 404: Modell nicht verfügbar; 429: Kontingent oder Anfragerate erreicht; 5xx: vorübergehender Dienstfehler.
- Keine automatische kostenpflichtige Wiederholung; kein stiller Providerwechsel.

### OpenAI als Option
In **Mehr → KI-Assistent → KI-Anbieter wechseln** ist weiterhin OpenAI wählbar. Der alte OpenAI-Key bleibt beim Update erhalten. Beide Schlüssel werden verschlüsselt und getrennt gespeichert. Das Backup enthält **keinen** API-Schlüssel.

Quellen:
- https://ai.google.dev/api/generate-content
- https://ai.google.dev/gemini-api/docs/pricing
- https://ai.google.dev/gemini-api/docs/api-key

## Vorherige OpenAI-Konfiguration (optional)
Bei Auswahl von **OpenAI**: **Mehr → KI-Assistent → API-Schlüssel einrichten**. Verwende einen persönlichen OpenAI-API-Schlüssel aus https://platform.openai.com/api-keys. Die OpenAI-API ist separat kostenpflichtig und nicht in ChatGPT Plus enthalten. Der optionale OpenAI-Modellname wird in `FitRocketAiService.MODEL` konfiguriert (aktuell `gpt-5-mini`).

**Keine produktseitigen Anmeldeinformationen liegen im Git-Repository**, werden fest im APK gespeichert oder in Backups exportiert. Das vom Benutzer selbst eingegebene Geheimnis wird in Android Keystore mit AES-GCM verschlüsselt und in privaten App-Einstellungen gespeichert. Das ist für einen privaten Client-Prototyp gedacht. Bei öffentlicher Distribution sollte ein eigener, abgesicherter Backend-Proxy mit benutzerbasierter Authentifizierung, serverseitigem Schlüsselmanagement und Kostenlimits eingesetzt werden. OpenAI empfiehlt, API-Schlüssel nicht direkt in mobilen Apps bereitzustellen: https://developers.openai.com/api/reference/overview

## API und Daten
- Für Gemini: https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent (default).
- Für optionales OpenAI: https://api.openai.com/v1/responses, HTTPS, feste Domain.
- Format: `text.format.type=json_schema` (strict), definiert in `app/src/main/assets/ai_result_schema.json`.
- Eigene Essensbeschreibung, eigene Vorlieben und eine Auswahl von Produktnamen und IDs aus der **lokalen öffentlichen Lebensmittelliste** werden an den ausgewählten Anbieter (Gemini bzw. OpenAI) übertragen.
- **Nicht** automatisch gesendet: Gewichtsverlauf, persönliche Körperdaten, Health-Connect-Daten, gesamte Tagebuchhistorie oder gespeicherte API-Schlüssel.
- `store=false` im Responses-Request fordert an, den Antwortzustand nicht im API-Responses-Store vorzuhalten; hiervon unabhängig können anbieterbezogene technische Aufbewahrungsregeln gelten.
- Netzwerk läuft außerhalb des UI-Threads. Timeouts, Fehlerbehandlung sowie Eingabe- und Antwortgrößenlimits sind implementiert.
- Der Nutzer muss selbst die API-Zahlungsmethode und mögliche Kostenlimits verwalten.

## Sicherheit und Zuverlässigkeit
- Alle KI-Zutaten enthalten 4 numerische Werte pro 100 g/ml. Ein Vorschlag mit fehlenden oder negativen Nährwerten wird zurückgewiesen.
- Pro Ergebnis maximal 12 Zutaten und 12 Portionen. Keine stillen Änderung an Tagebuch oder Lebensmitteldatenbank durch einen KI-Aufruf.
- **Niemals** Modell-Punktwerte verwenden: FitRocketAiRules und FitRocketMath berechnen den Score.
- ZeroPoint nur bei unverändertem `basic`-Datensatz mit exakter ID und identischem normalisiertem Namen. Gegenprüfungen im Unit-Test.
- Bei `nutrition`-Katalogeinträgen werden die dort bereits hinterlegten Nährwerte bevorzugt.
- Jede KI-Antwort besitzt eine Unsicherheitsangabe; die gesamte Zutatenliste und Nährwerte sind vor dem Speichern korrigierbar.
- Bei individuell gespeicherten KI-Rezepten werden noch nicht bekannte Zutaten explizit bestätigt und im lokalen Katalog abgelegt.
- Offline können Produkte unabhängig vom Modell hinzugefügt werden.
- Die API hat ggf. begrenzte Verfügbarkeit, Gebühren oder Zugriffsberechtigungen. Ein kompilierter APK-Build ist **kein** Live-Test der API.

## Backups
Backup-Format Version 5 erweitert vorhandene v4-Inhalte um:
- `aiFoods`: Nährwertprofile selbst erstellter oder nach KI-Vorschlägen gespeicherter Produkte.
- `savedAiRecipes`: vom Nutzer akzeptierte KI-Rezepte mit lokal verknüpften Produkt-IDs.
- `aiPreferences`: freiwillig gespeicherte Ernährungsvorlieben.

**Nie im Backup:** API-Schlüssel oder Android-Keystore-Schlüsselmaterial.

## Teststrategie
- JUnit `FitRocketAiRulesTest`: korrekte Punktrechnung, Abwehr fingierter ZeroPoint-IDs, Beachtung lokaler Makros, Eingabevalidierung und Lebensmittelspeicherung.
- `scripts/validate_ai_schema.py`: prüft die Struktur und Required-Felder der JSON-Schema-Vorgabe.
- `scripts/validate_catalog.py`: prüft die vorhandenen Lebensmitteldaten und Rezeptverweise.
- GitHub Actions: diese Prüfungen, sämtliche Unit-Tests und APK-Erstellung.
- Echte Online-Antworten werden erst beim Test mit einem vom Nutzer eingerichteten, gültigen API-Schlüssel überprüfbar.
