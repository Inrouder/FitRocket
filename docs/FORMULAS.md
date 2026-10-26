# FitRocket-Punkte: Berechnungsregeln

FitRocket ist ein eigenständiges Punktesystem. Alle Regeln im Folgenden lassen sich im Repository nachvollziehen und testen.

## 1. Ernährungs-Punkte
Für Lebensmittel mit Nährwertprofil werden pro eingegebener Menge Kilokalorien K, gesättigte Fettsäuren F (g), Zucker Z (g) und Protein E (g) berücksichtigt:

**P = max(0, round(0,026 × K + 0,25 × F + 0,10 × Z − 0,07 × E))**

Zunächst werden die Nährwerte je 100 g bzw. 100 ml auf die tatsächliche Menge umgerechnet, dann wird einmal auf ganze Punkte gerundet.

- **Basislebensmittel:** Festgesetzte 0 FitRocket-Punkte für naturbelassene Formen. Zusätze separat buchen.
- **Portionsvorschläge:** Vordefinierte Punktwerte für Referenzportionen. Nutzende können sie überschreiben.
- **Nährwertprodukte:** Rechenregel oben.
- **Eigene Einträge:** Punkte manuell festlegen.

Lebensmitteldaten können Hersteller-/Rezeptabweichungen haben. Die Nährwertformel ist eine FitRocket-Entscheidung und keine Kaloriengleichsetzung.

## 2. Persönlicher Ruhebedarf
Zur Personalisierung wird die Mifflin–St.-Jeor-Gleichung verwendet:

**Ruhe-kcal/Tag = 10 × kg + 6,25 × cm − 5 × Alter + Referenzkonstante**

Referenzkonstante: weibliches Stoffwechselprofil −161, männliches +5, Mittelwert −78. Alter, Größe und Gewicht verändern damit die Aktivitätsrechnung. Das ist eine Schätzung des Ruheumsatzes, kein medizinischer Messwert.

Wissenschaftliche Grundlage: https://pubmed.ncbi.nlm.nih.gov/2305711/

## 3. Training und MET
Jede Aktivität besitzt abhängig von der gewählten Intensität einen MET-Wert. Die App verwendet eine kleine eigene Tabelle mit Aktivitätsarten und drei Intensitätsstufen, die in `FitRocketMath.activityMet` hinterlegt ist. Sportartspezifische Richtwerte lassen sich mit dem 2024 Adult Compendium vergleichen: https://pacompendium.com/adult-compendium/

**Aktive kcal = (Ruhe-kcal/Tag ÷ 1440) × max(0, MET − 1) × Minuten**

Dieser Netto-Ansatz zieht die Ruheenergie der Trainingszeit ab.

**Bewegungspunkte = round(aktive kcal ÷ kcal-je-Punkt)**

Der Divisor ist in FitRocket einstellbar (30–150, Werkseinstellung 65). Der geschätzte Kalorienverbrauch und das Profil zum Trainingszeitpunkt werden im Eintrag gespeichert.

## 4. Schritte
Nutzende erfassen den gesamten Tageszähler sowie das Gehtempo. Standardwert für Grundschritte: 3000 pro Tag, individuell einstellbar.

**Anrechenbare Schritte = max(0, Tagesgesamtzahl − Grundschritte − bereits in Workouts berücksichtigte Schritte)**

Beim Aufzeichnen von Gehen oder Laufen schlägt FitRocket automatisch 100 bzw. 150 Schritte pro Minute zum Ausklammern vor; die Zahl kann korrigiert werden. Für andere Workouts kann sie ebenfalls angegeben werden.

**Schritt-Minuten = anrechenbare Schritte ÷ gewählte Schrittfrequenz**

**Aktive Schritt-kcal = (Ruhe-kcal/Tag ÷ 1440) × (3,5 − 1) × Schritt-Minuten**

Schritte werden je Tag zu Punkten gerundet, anschließend über die Woche addiert. Eine lückenlose technische Deduplizierung erfordert zukünftig Zeitstempel von Fitnessgeräten; bisher beruhen Schritt- und Trainingswerte auf manueller Eingabe.

## 5. Eigenständiges Wochenkonto mit Tagesüberträgen (v0.8.1)

- **Wochenstart: 28 Punkte** (Standardwert ab Migration), jeden Montag automatisch neu. Unter den Budgeteinstellungen anpassbar.
- Das feste tägliche Basisbudget bleibt bestehen und wird nicht durch Wochenpunkte erhöht.
- **Übertrag je abgeschlossenem Tag = min(4, max(0, Tagesbudget − Tagesverbrauch))**.
- Es werden nur abgeschlossene Tage der **aktuellen Kalenderwoche**, von Montag bis gestern, für den Übertrag berücksichtigt. Heute wird erst nach Tagesabschluss mitgerechnet. Tage ohne Essenseinträge haben 0 verbrauchte Tagespunkte und erhalten deshalb den vollen maximalen Übertrag.
- **Bewegungsgutschrift = floor(verdiente Wochen-Bewegungspunkte × freigegebener Anteil)**. Freigabeanteil bleibt 0 %, 50 % oder 100 %.
- **Wöchentliche Tagesüberschreitungen = Summe von max(0, Tagesverbrauch − Tagesbudget)** für Montag bis heute.
- **Verfügbare Wochenpunkte = max(0, Wochenstart + abgeschlossene Tagesüberträge + Bewegungsgutschrift − Tagesüberschreitungen)**.
- Wochenpunkte werden flexibel verwendet, indem Lebensmittel oberhalb des täglichen Basisbudgets eingetragen werden. Es ist kein manuelles Übertragshäkchen erforderlich.
- Die Punkte eines Vorwochenkontos und die Tagesüberträge der Vorwoche werden **nicht** in die neue Woche übernommen.
- Der Kontostand wird aus Tagebuch, Uhrzeit und Aktivitätskonto abgeleitet, ohne dauerhaft einzelne Tagesgutschriften zu verdoppeln. Rückwirkende Bearbeitungen vergangener Tage passen ihn an.

Bei der einmaligen Migration vorhandener v0.8.0-Installationen wird das Wochenstartguthaben auf 28 Punkte gesetzt. Danach bleibt eine manuelle Anpassung erhalten.

## 6. Daten und Rückwärtskompatibilität
- Trainierte Einheiten der Vorversion behalten ihre bereits gespeicherte Energieschätzung, falls vorhanden. Bei Wechsel des kcal-pro-Punkt-Faktors werden neuere Einheiten neu bewertet, ältere ohne Energieangabe behalten gespeicherte Punkte.
- Profiländerungen beeinflussen zukünftige Workouts und Schrittberechnungen; bereits dokumentierte Trainingseinheiten behalten ihre Energieaufnahme.
- Backup-Schema 3 (mit Aktivitäts- und Uhr-Schrittdaten): Mahlzeiten, Gewichte, Training, Schritte, Benutzerprofil und Punkteinstellungen. Import von Schema 1 ist möglich.
- Health-Connect-Daten werden bei freigegebenem Zugriff eingelesen; ein automatischer Abruf wird nur bei bestehender Verbindung ausgeführt.

## 7. Barcode-Produkte und Rezeptpunkte (v0.9.0)
Die Barcodeerfassung fragt bei Open Food Facts nur den öffentlichen Produktdatensatz ab. Für eine automatische FitRocket-Bewertung müssen **kcal, gesättigte Fettsäuren, Zucker und Protein je 100 g bzw. 100 ml** vorhanden sein. Fehlende Zahlen werden nicht als null interpretiert.

Alle vollständigen Nährwerte werden mit der unter Abschnitt 1 erläuterten eigenständigen FitRocket-Formel auf die tatsächliche Portionsmenge skaliert. Nach manueller Ergänzung fehlender Werte oder Prüfung der Herstellerangaben wird der Scanartikel im lokalen Katalog gespeichert.

Rezeptvorschläge bestehen aus Rezeptzutaten, die mit einer konkreten Produkt-ID und einer Menge in Gramm verknüpft sind. Basislebensmittel der Klasse `basic` zählen **0 Punkte**, `nutrition`-Produkte werden auf die Mengen einer Portion umgerechnet. Die Punkte der Zutaten werden summiert und im Rezept angezeigt. Wer Portionen verändert, multipliziert die Punkte pro Portion.

## 8. Wasser und Gewicht
Wasser wird in **250-ml-Schritten** je Kalendertag gespeichert; das Tagesziel ist individuell einstellbar. Wasser wird nicht als Essens-Punktwert verrechnet.

Gewichtsaufzeichnungen werden je Tag mit Datum und Kilogramm geführt. Die Verlaufsgrafik verbindet die gespeicherten Werte und zeigt optional eine Ziel-Linie. Sie dient der Darstellung, nicht der Prognose.
