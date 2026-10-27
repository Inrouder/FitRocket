# FitRocket – Versionshinweise

## 0.11.1 – Korrektur der Gemini-Key-Eingabe
- Eingabefehler bei neueren Google-Gemini-Authentifizierungsschlüsseln behoben, inklusive Punkt im Schlüsselformat.
- Keine unzuverlässige Präfix- oder Zeichenformatprüfung mehr, die gültige Google-Schlüssel aussperrt; tatsächliche Authentifizierung wird über die Google-API geprüft.
- Weiterhin Schutz vor Leerzeichen und Steuerzeichen im HTTP-Header.
- Explizite Passwortmaskierung des Schlüsselfeldes gegen Offenlegung auf Screenshots.
- Acht JUnit-Tests für ältere und neuere Schlüsseltypen, Copy/Paste und ungültige Eingaben.

## 0.11.0 – Google Gemini, kostenlose API und Anbieterwahl
- Gemini 2.5 Flash-Lite als sparsame Voreinstellung für Rezeptgenerator, Mahlzeiten-Einschätzung und neue Lebensmittel.
- Gemini 2.5 Flash als optionales Qualitätsmodell auswählbar.
- OpenAI bleibt als bisheriger, getrennt konfigurierbarer Anbieter verfügbar.
- Android-Keystore-Schlüsselspeicher erweitert: Gemini-Schlüssel separat; zuvor gespeicherter OpenAI-Schlüssel bleibt erhalten.
- Gemini REST v1beta generateContent mit JSON-Schema-Ausgabe und lokaler FitRocket-Validierung.
- Verständliche Fehlerbehandlung für fehlende Berechtigung, falschen API-Schlüssel, ausgelastetes Kontingent, Modellverfügbarkeit und Dienstfehler.
- KI-Anbieter und Gemini-Modell über **Mehr → KI-Assistent** auswählbar.
- Keine KI-Zugangsgeheimnisse in GitHub/Backups; kein automatischer Wechsel auf kostenpflichtige Dienste.
- Zusätzliche Unit-Tests für Anfrageformat, Antwortparser und Datenschutz der Kataloghinweise.

## 0.10.0 – KI-Küchenassistent, Rezepte und Lebensmittelschätzung
- Online-KI für persönliche Rezepte, Beschreibungen von Mahlzeiten und Nährwert-Schätzung neuer Produkte.
- Eigene, auf dem Gerät geschützte API-Schlüssel-Eingabe (Android Keystore, AES-GCM), ohne Hardcoding und ohne Schlüssel in Backups.
- Strukturierte API-Antworten mit JSON-Schema; einheitliche Zutaten-/Nährwertformulare.
- Die Punkte stammen ausschließlich aus FitRocketMath, nicht aus einer Modellbehauptung.
- Nur exakt zugeordnete, unveränderte Katalog-Basislebensmittel erhalten 0 Punkte.
- Für jedes KI-Ergebnis editierbare Zutaten, Portionsmengen und Nährwerte.
- Fehlende Zutaten lassen sich ergänzen, falsch erkannte entfernen.
- KI-Rezepte können dauerhaft gespeichert und im normalen Rezeptbuch verwendet werden.
- Neue Lebensmittel landen nur nach ausdrücklicher Nutzerbestätigung in der lokalen Lebensmittelsuche.
- Offline-Formular für neue Lebensmittel anhand von vier Nährwertangaben.
- Gespeicherte Ernährungsvorlieben, begrenzte Eingabelängen, API-Timeouts und erkennbare Fehlermeldungen.
- Backupschema 5 für eigene KI-Lebensmittel und Rezepte, niemals für den API-Schlüssel.
- Automatisierte Tests für KI-Punkteregeln, Katalog-ID-Abgleich und strikte Antwortstruktur.

## 0.9.0 – Übersicht, Barcode, Wasser, Rezepte & Gewicht
- Heute-Ansicht neu gegliedert und Schnellaktionen ergänzt.
- Frühstück, Mittagessen, Abendessen, Snacks und Getränke als eigene Tagebuch-Bereiche.
- Tagebuch nach Datum navigierbar; ältere Mahlzeiten ohne Gruppenzuordnung bleiben sichtbar.
- Google-Code-Scanner und manuelle Barcode-Eingabe.
- Open-Food-Facts-v3-Produktabfrage mit Pflichtprüfung aller vier Nährwerte und Korrektureingabe.
- Gefundene Scanprodukte werden lokal gespeichert und in die Lebensmittelsuche integriert.
- 13 offline verfügbare Rezeptvorschläge mit konkreten Katalog-Zutaten und berechneten Punkten.
- Trinktagebuch mit bildlichen 250-ml-Gläsern, Plus/Minus und einstellbarem Ziel.
- Gewichtseintrag über Skala, grafische Zeitreihe und Zielgewicht.
- JSON-Backup auf Schema 4 erweitert: Wasser, Gewichtsziel, eigene gescannte Lebensmittel.
- Automatische Katalog- und Rezeptprüfung im GitHub-Workflow.
- Vorhandenes Wochenkonto mit 28 Startpunkten und bestehende Health-Connect-Funktionen bleiben erhalten.

## 0.8.1 – 28 Wochenpunkte und Tagesübertrag
- 28 Punkte stehen jeden Montag als neues Wochenstartguthaben bereit.
- Unverbrauchte Tagespunkte bis maximal 4 werden nach Abschluss des Tages dem aktuellen Wochenkonto gutgeschrieben.
- Gutschriften gelten für abgeschlossene Tage von Montag bis gestern, nicht für den laufenden Tag oder künftige Tage.
- Nicht dokumentierte Tage gelten als ungenutzt und erhalten den Tagesübertrag.
- Wochenverbrauch durch Tagesbudget-Überschreitungen wird abgezogen.
- Vorhandene Aktivitätspunkte-Gutschriften bleiben erhalten; Guthaben wird getrennt erläutert.
- Tagesbezogene Gutschriften im Aktivitätskonto aufgelistet, Änderungen an vergangenen Mahlzeiten werden erneut berechnet.
- Einmalige Umstellung des Wochenstartwerts auf 28 Punkte, bereits bestehende Tagebuchdaten bleiben unangetastet.
- JUnit-Tests für Wochenwechsel, Tagesabschluss, Begrenzung und Verbrauch.

## 0.8.0 – Smartwatch-Synchronisation
- Health Connect direkt über Android 14+ Framework-API lesend integriert.
- Schritte werden für die aktuelle Kalenderwoche täglich aggregiert.
- Trainingsaufzeichnungen aus verbundenen Fitness-Apps werden nach Dauer und Aktivitätstyp in FitRocket übernommen.
- Doppelte Trainingsimporte werden anhand der Quell-ID sowie Uhrzeit/Dauer vermieden.
- Schrittschätzung berücksichtigt bereits angerechnete Workout-Schritte.
- Schrittquelle kann zwischen synchronisierten und manuellen Tageswerten umgeschaltet werden.
- Auf Wunsch beim Öffnen nach mindestens 20 Minuten erneut abgleichen; manueller Sync jederzeit möglich.
- Health-Connect-Berechtigungen, Datenschutzhinweise und Löschung eingelesener Fitnessdaten.
- Backupschema 3 mit Schrittcaches, Ausblendeliste und Quellenauswahl; Import älterer Backups bleibt möglich.
- Automatisierte Unit-Tests der Deduplizierung.

## 0.7.0 – Persönliche Berechnungen und eigenes Punktemodell
- Vollständiges Bewegungsprofil: Alter, Größe, Gewicht, Stoffwechsel-Referenzprofil.
- Ruheenergiebedarf und aktivitätsbezogene Energie pro Einheit.
- Sportart, Intensität, Minuten und bereits gezählte Workout-Schritte.
- Schrittberechnung mit Grundwert, Gehtempo und Schutz vor Doppelzählung.
- Aktivitätsgutschrift ins Wochenbudget wählbar: 0 / 50 / 100 %.
- Einstellbarer Energie-pro-Punkt-Faktor und Grundschrittwert.
- In Wochenstatistik und Tagebuch bleiben tägliche Punkte und Wochenpuffer getrennt.
- Backup-Dateien beinhalten jetzt auch Profil, Schrittwerte und Berechnungsregeln.
- Lebensmittel auf eigenständige FitRocket-Kategorien und Formel umgestellt.
- Automatisierte Tests der Punktformeln im APK-Build.

## 0.6.0 – Aktivitätskonto
- Trainingsprotokoll, Aktivitätspunkte, Bewegungsgutschrift und Wochenkonto.

## 0.5.0 – Lebensmittel & Nährwerte
- Erweiterter Lebensmittelkatalog und Nährwert-Punkteberechnung.

## 0.4.x – Cozy Oberfläche
- Freundliche Pastellkarten, Punkteskala und verbesserte Menüabstände.

## 0.3.x – Alltag
- Lebensmittelsuche, Tagebuch, Gewicht, Monatsauswertung und Backup.

## 0.1–0.2 – Grundversion
- Lokales Tagebuch, Food-Katalog und automatischer Android-APK-Build.
