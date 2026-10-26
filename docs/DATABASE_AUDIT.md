# Datenbankprüfung: Historischer WW Blauer Plan

## Quellen
- Offizielle historische US-Blue-ZeroPoint-Liste: https://www.weightwatchers.com/us/blog/blue-zeropoint-foods
- Offizielle WW UK Historie: https://www.weightwatchers.com/uk/blog/zero-list
- Unabhängig rekonstruierte Näherungsformel: https://calculover.com/de/health-fitness/diet-nutrition/weight-watchers-points/

## Bestand und Ergänzungen
- 236 bestehende deutsche historische ZeroPoint-Zuordnungen bleiben erhalten; vollständig einzeln mit deutschen WW-Originalen geprüft sind sie nicht.
- 23 ungeprüfte Punktebeispiele unverändert; 10 Prüffälle ohne verifizierte Nullpunkte.
- 14 ältere US-Fruchtergänzungen bleiben.
- 30 zusätzliche US-Blue-ZeroPoint-Lebensmittel unter IDs USBLUE001 ff. bleiben vom deutschen Quellenstatus getrennt.
- 45 neue generische Lebensmittel unter CALC001 ff. mit typischen Nährwertprofilen pro 100 g/ml (nicht markenspezifisch).

## Näherungsrechnung (NICHT offiziell veröffentlicht)
Punkte = max(0, gerundet[kcal × 0,0305 + gesättigte Fette[g] × 0,275 + Zucker[g] × 0,12 − Protein[g] × 0,098]).

Die vier Nährwerte werden zunächst auf die tatsächliche Portionsmenge skaliert und erst danach wird die Summe gerundet. Im UI steht ausdrücklich „ca.“. Die Formel ist eine öffentlich diskutierte Rekonstruktion, kein offizieller WW-Algorithmus. Verarbeitete Produkte variieren nach Hersteller.

## ZeroPoint-Vorrang
Wenn ein eindeutiger ZeroPoint-Eintrag im historischen Plan nach Zubereitung und Zusatzbedingungen passt, bleiben die Punkte 0. Saft, Smoothie, zugesetztes Öl, Panade, Zucker und Saucen müssen separat geprüft werden. Bei zweifelhafter Produktgleichheit nicht automatisch ZeroPoint vergeben.

## Einschränkungen
Aus dem verfügbaren öffentlichen Material kann keine vollständige offizielle deutsche WW-Markenproduktdatenbank rekonstruiert werden. Der aktuelle Katalog ist eine erweiterte historische Rekonstruktion. Die vorhandenen 23 Punktbeispiele sollten künftig mit etikettierten Produkten validiert werden. Typische Nährwerte für CALC-Einträge sind Startannahmen und müssen vor exakter Nutzung produktbezogen geprüft werden.
