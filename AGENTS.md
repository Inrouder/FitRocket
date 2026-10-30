# FitRocket – verbindliche Projektregeln für KI-Coding-Agenten
Diese Datei ist Einstiegspunkt für spätere KI-Änderungen. Zuerst [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md), [docs/FORMULAS.md](docs/FORMULAS.md), [docs/SIGNING.md](docs/SIGNING.md) lesen.

## Auftrag bei jeder Änderung
- Repository **aktuell** lesen, keine veralteten Annahmen aus früheren Chat-Verläufen übernehmen.
- Zuerst betroffene Methoden, JSON-Keys, Tests und UI-Navigation identifizieren.
- Kleine, nachvollziehbare Änderungen; vorhandene Funktionen und Daten bewahren.
- Bei Verhalten/Schema/Navigation Tests erweitern und Dokumentation aktualisieren.
- Versionsnummer nur für tatsächliche App-Releases erhöhen. Kein signiertes Release behaupten, bevor CI, Signatur und Artefakt überprüft sind.
- Keine Build-Erfolge, Uploads, Screenshots oder Gerätetests erfinden.

## Unveränderliche Qualitätsregeln
1. **ZeroPoints-Invariante**: Katalogeinträge mit `status=basic` bleiben stets 0 Punkte bei jeder Menge. Änderungen im Rezept dürfen ausschließlich als lokale `nutritionOverride` gespeichert werden und niemals diesen Katalogeintrag modifizieren. Tests für 100 g, 10 kg und Rezepte verpflichtend.
2. **Punkte**: `FitRocketMath`, `FitRocketPointLedger`, `FitRocketAiRules` und `FitRocketRecipe` sind die maßgeblichen Berechnungsstellen. KI-Ausgaben als Schätzungen validieren und **nicht** deren eigene Punktezahlen übernehmen.
2. **Wochenkonto**: heutige Aktivität zählt nach gewähltem Anteil sofort; nur ungenutzte Tagespunkte kommen nach Tagesabschluss.
3. **Navigation**: Tagebuch, Suche, Kochbuch, Fortschritt, Profil; Beschriftung auch bei großen Schriften und 3-Tasten-Navigation sichtbar halten.
4. **Persistenz**: JSON-Keys und Import/Export rückwärtskompatibel behandeln; vorhandene Mahlzeiten, Rezepte, Favoriten und Bilder dürfen nicht verloren gehen.
5. **Privatsphäre**: kein API-Key/Keystore im Code oder Backup; keine Gesundheits-, Profil- und Tagebuchdaten ungefragt an KI-Provider.
6. **Netzwerk**: nur HTTPS für Rezeptimport, keine beliebigen geheimen internen Endpunkte; Remote-Inhalte als untrusted betrachten, Umfang/Zeit begrenzen, Nutzer prüft die KI-Ergebnisse.
7. **Rezeptfotos**: Galerie- und Kamerabilder sind Teil der Sicherungsanforderung; Backup-Wiederherstellung testen. Der aktuelle Kamera-Intent liefert nur Vorschauqualität.
8. **Android-UI**: Keine neuen Header-Logos auf Tagebuch; dezenten Raketenhintergrund, abgerundete Cards und genügend Platz für Navigation erhalten.

## Erforderlicher Abschlussbericht des KI-Agenten
Geänderte Dateien, sichtbares Nutzerverhalten, Datenschema-/Migrationsfolgen, Tests mit tatsächlichem Ergebnis, CI-Link/Commit und offene Einschränkungen. Bei fehlendem Testzugang ausdrücklich sagen, dass nicht getestet wurde.

## Technische Schulden
Große `MainActivity.java`; freie Rezeptzutaten mit manuellen Punkten; fragiler HTML-Rezeptimport; große Base64-Bild-Backups. Nicht als fertig gelöst deklarieren.
