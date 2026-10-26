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

## 5. Wochenkonto
- Training + Schrittgutschriften werden pro Kalenderwoche addiert.
- Freigabeanteil: 0, 50 oder 100 %.
- **Freigeschaltete Bewegungspunkte = floor(Bewegungspunkte × Anteil)**
- **Wöchentliche Tagesüberschreitungen = Σ max(0, verbrauchte Tagespunkte − Tagesbudget)**
- **Wochenguthaben übrig = max(0, Wochen-Extra + freigeschaltete Bewegungspunkte − Überschreitungen)**

Das tägliche Basisbudget bleibt unverändert. Wochenwechsellogik: Montag bis Sonntag, rückwirkende Tagebuchänderungen fließen ein.

## 6. Daten und Rückwärtskompatibilität
- Trainierte Einheiten der Vorversion behalten ihre bereits gespeicherte Energieschätzung, falls vorhanden. Bei Wechsel des kcal-pro-Punkt-Faktors werden neuere Einheiten neu bewertet, ältere ohne Energieangabe behalten gespeicherte Punkte.
- Profiländerungen beeinflussen zukünftige Workouts und Schrittberechnungen; bereits dokumentierte Trainingseinheiten behalten ihre Energieaufnahme.
- Backup-Schema 2: Mahlzeiten, Gewichte, Training, Schritte, Benutzerprofil und Punkteinstellungen. Import von Schema 1 ist möglich.
- Die App nutzt keine automatischen Fitnessdaten, solange keine Gerätefreigabe und passende Schnittstelle implementiert sind.
