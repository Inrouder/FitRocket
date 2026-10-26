# FitRocket: Smartwatch & Health Connect

## So richtest du deine Uhr ein
1. Die Fitness-App deiner Uhr muss Schritte und Workouts an **Health Connect** auf dem Android-Smartphone weitergeben. Manche Apps benötigen dafür eine eigene Option in ihren Einstellungen.
2. Öffne FitRocket und gehe zu **Mehr → Bewegungs- und Punkteübersicht**.
3. Lege dein Bewegungsprofil an (Alter, Größe, Gewicht, Referenzprofil).
4. Tippe auf **Health Connect verbinden** und erlaube **Schritte** und **Training**.
5. Tippe auf **Jetzt synchronisieren**. Danach kannst du die Schrittquelle zwischen Uhr und manuell umschalten.

## Umfang
- Unterstützt Android 14 (API 34) und neuer mit vorhandenem Health-Connect-Framework.
- Die Android-Versionen 8 bis 13 können FitRocket weiterhin manuell nutzen; die Health-Connect-Synchronisation wird auf ihnen nicht angeboten.
- Es ist keine Bluetooth-Direktverbindung zur Smartwatch und keine eigenständige Wear-OS-App erforderlich.
- Importiert **Schritte und Trainingssessions**, keine Herzfrequenz, Schlafdaten, Gewichtsverläufe oder GPS-Routen.
- Der aktuelle Importzeitraum ist Montag bis Sonntag der laufenden Kalenderwoche.
- Synchronisiert beim manuellen Antippen und optional beim erneuten Öffnen nach mindestens 20 Minuten, sofern die App verbunden ist.
- Auf Android 14+ werden bei erteilter Freigabe auch verfügbare Smartphone-Schritte berücksichtigt. Die App kann daher nicht garantieren, dass alle Schritte ausschließlich von einer Uhr stammen.

## Berechnungen
- Für Schritte verwendet FitRocket die durch Health Connect aggregierten Tageswerte (unter Berücksichtigung der Datenquellenpriorität von Health Connect).
- Für Sessions nimmt FitRocket den Trainingstyp und die Dauer; die Aktivitätsenergie wird daraus mit dem eigenen FitRocket-Modell geschätzt.
- Die Werte werden **nicht** als gemessene Trainingskalorien oder offizielle Hersteller-Punkte dargestellt.
- Für bekannte Geh- und Lauftrainings werden geschätzte Schritte (100 bzw. 150/min) vom allgemeinen Schrittbudget abgezogen; bei manuellen Trainings können diese Werte bearbeitet werden.
- Zeit- und identitätsbasierte Dublettenprüfung verhindert viele doppelte Trainingseinträge. Eine technische Garantie gegen alle Doppeleinträge ist damit nicht gegeben, etwa bei stark abweichenden Quellenuhren.
- Angezeigte FitRocket-Punkte basieren weiterhin auf deinem Bewegungsprofil und deinen FitRocket-Regeln.

## Datenschutz
Die Integration verlangt **nur** die zwei Leseberechtigungen `android.permission.health.READ_STEPS` und `android.permission.health.READ_EXERCISE`. Es wird nichts in Health Connect geschrieben. Die Daten verbleiben lokal im FitRocket-Speicher und können Teil eines von dir erzeugten JSON-Backups werden.

Die Berechtigung wird nur nach ausdrücklicher Aktion angefragt. Beim Trennen löscht FitRocket die synchronisierten Schritte und importierten Trainings, während manuelle Einträge erhalten bleiben. Das Widerrufen der Android-Berechtigungen erfolgt separat in den Health-Connect-Einstellungen.

Beim Einspielen eines Backups wird die Android-Datenfreigabe bewusst **nicht** automatisch aktiviert. Die Verbindung muss erneut zugelassen werden.

## Implementierung
- `HealthSync.java`: native Android-Health-Connect-API, aggregierte Tages-Schritte und paginierte ExerciseSessionRecords.
- `FitRocketHealthType.java`: Zuordnung der Fitnessaktivitäten zu FitRocket-Typen.
- `FitRocketSyncRules.java`: deterministische Dubletten- und Wochengrenzenlogik.
- `HealthPrivacyActivity.java`: Datenschutzerklärung für die Android-Health-Berechtigungsanzeige.
- `MainActivity.java`: Freigabe, Abgleich, Datenquellen, lokaler Speicher, Trennung und Backup.

Offizielle Android-Dokumentation:
- https://developer.android.com/health-and-fitness/health-connect/get-started
- https://developer.android.com/health-and-fitness/health-connect/read-data
- https://developer.android.com/reference/android/health/connect/HealthConnectManager
