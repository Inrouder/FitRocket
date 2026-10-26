# FitRocket – Versionshinweise

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
