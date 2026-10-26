# FitRocket v0.9.0 – Neue Bereiche

## Barcode scannen
- **Heute → Barcode** oder **Suche → Barcode scannen**
- Benötigt Google Play-Dienste für die Kameraansicht. Alternativ Nummer eingeben.
- Barcode-Feld erlaubt 8–14 numerische Zeichen (EAN/UPC).
- Über die Open-Food-Facts-API werden der Produktname und Nährwerte pro 100 g/100 ml abgefragt.
- Bei unvollständigen Daten erscheint eine Eingabemaske für alle vier benötigten Nährwerte; keine Punkteberechnung ohne vollständige Werte.
- Gefundene und ergänzte Artikel sind lokal wiederauffindbar.
- Beim nächsten Aufruf eines bekannten Barcodes kann der gespeicherte Artikel gewählt oder die Online-Version erneut geprüft werden.

## Rezeptvorschläge
- **Heute → Rezepte** oder **Mehr → Rezeptvorschläge**.
- Offline-Liste mit 13 Gerichten und Filtern nach Frühstück, Mittagessen, Abendessen und Snacks.
- Jedes Rezept referenziert existierende Lebensmittel-IDs; für Basislebensmittel gelten 0 FitRocket-Punkte, für die übrigen Zutaten die gespeicherten Nährwerte.
- Zutaten und Zubereitung können angeschaut und einzelne Portionen ins Tagebuch übernommen werden.
- Nicht gesondert angegebene Zusätze müssen separat erfasst werden.

## Wasser-Challenge
- Auf **Heute** und jedem Tagebuchdatum als eigene Karte sichtbar.
- Jedes Glas entspricht genau 250 ml. Auf ein volles Glas tippen setzt die Trinkmenge bis zum vorangehenden Glas zurück.
- Plus-/Minusschaltflächen unterstützen schnelle Eingabe.
- Tagesziel anpassbar; Standard 1500 ml.

## Gewicht
- **Heute → Gewicht & Verlauf** oder **Mehr → Gewicht & Verlauf**.
- Große Anzeige für Ausgangsgewicht, letztes gemessenes Gewicht und Zielgewicht.
- Verlaufslinie mit Datenpunkten sowie Zielgewicht-Linie.
- Gewichtseingabe mit Schieberegler und alternativ Zahlenfeld auf 0,1 kg.

## Tagebuch und Datensicherung
- Datumsnavigation nach links/rechts und via Kalender.
- Gruppen Frühstück, Mittagessen, Abendessen, Snack und Getränke.
- Einträge aus älteren Backupformaten ohne Mahlzeitengruppe erscheinen unter Snacks.
- JSON Backup-Schema 4 speichert Wassergläser, Wasserziel, Gewichtsziel und Barcode-Katalog zusätzlich zu allen bisherigen Einträgen.

Die Umsetzung orientiert sich visuell an einer übersichtlichen Kartenoberfläche. Die App ist eine eigenständige FitRocket-Entwicklung und keine Kopie eines fremden Programms.
