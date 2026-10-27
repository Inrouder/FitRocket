# FitRocket-KI-Assistent v0.10.0

## Ziele
FitRocket soll unabhängig von externen Diätprogrammen bleiben. Die KI erstellt Vorschläge und Schätzungen, während **FitRocket die Punkte ausschließlich lokal anhand der FitRocketMath-Formel berechnet**.

### Drei Modi
1. **Rezept generieren:** Ein Nutzer beschreibt Geschmack, Zutaten, Ausschlüsse, Zeitbudget und Essenswünsche. Die KI liefert einen strukturierten Rezeptvorschlag mit Zutatenmengen, Nährwerten, Portionsanzahl und Zubereitung.
2. **Freitext-Mahlzeit einschätzen:** Ein Nutzer beschreibt, was gegessen wurde. Die KI leitet mögliche Einzelzutaten, Portionen und ernährungsbezogene Unsicherheiten ab. Alle Angaben werden vor Tagebucheintrag von der Person überprüft.
3. **Produkt/Lebensmittel einschätzen:** Die KI liefert ein einziges vorgeschlagenes Nährwertprofil. Vor dem Speichern müssen Angaben überprüft werden. Für Markenprodukte sind tatsächliche Etiketten genauer.

Zusätzlich können Lebensmittel komplett **offline** aus eigenen Nährwertangaben neu angelegt werden.

## Einrichtung in Android
**Mehr → KI-Assistent → API-Schlüssel einrichten**. Verwende einen persönlichen OpenAI-API-Schlüssel aus https://platform.openai.com/api-keys. Die OpenAI-API ist separat kostenpflichtig und nicht in ChatGPT Plus enthalten. Der verwendete Modellname wird in `FitRocketAiService.MODEL` konfiguriert (aktuell `gpt-5-mini`).

**Keine produktseitigen Anmeldeinformationen liegen im Git-Repository**, werden fest im APK gespeichert oder in Backups exportiert. Das vom Benutzer selbst eingegebene Geheimnis wird in Android Keystore mit AES-GCM verschlüsselt und in privaten App-Einstellungen gespeichert. Das ist für einen privaten Client-Prototyp gedacht. Bei öffentlicher Distribution sollte ein eigener, abgesicherter Backend-Proxy mit benutzerbasierter Authentifizierung, serverseitigem Schlüsselmanagement und Kostenlimits eingesetzt werden. OpenAI empfiehlt, API-Schlüssel nicht direkt in mobilen Apps bereitzustellen: https://developers.openai.com/api/reference/overview

## API und Daten
- Endpunkt: https://api.openai.com/v1/responses, HTTPS, feste Domain.
- Format: `text.format.type=json_schema` (strict), definiert in `app/src/main/assets/ai_result_schema.json`.
- Eigene Essensbeschreibung, eigene Vorlieben und eine Auswahl von Produktnamen und IDs aus der **lokalen öffentlichen Lebensmittelliste** werden an OpenAI übertragen.
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
