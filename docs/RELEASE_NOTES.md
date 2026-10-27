# FitRocket v0.15.0 – Portion oder Gramm, Zehntelpunkte und FAQ

## Lebensmittel ganz nach deiner Menge erfassen
- Produkte mit festgelegter Portion (z. B. 1 Handvoll Nüsse = 25 g oder 1 Portion Feta = 50 g) bieten jetzt zwei Varianten: **Portionen (schnell)** oder **Menge in Gramm/Millilitern (genau)**.
- Bei Lebensmitteln mit Nährwerten pro 100 g/ml ist ebenfalls eine genaue Gramm-/Milliliter-Eingabe oder eine schnelle 100-g-/100-ml-Referenzportion verfügbar.
- Die App berechnet die Punkte proportional und speichert sie mit **einer Nachkommastelle**. Beispiele: **100 g = 1 P → 20 g = 0,2 P**; **50 g Feta = 4 P → 20 g = 1,6 P**.
- Die Zehntelpunkte erscheinen im Tagebuch, der Tagesübersicht, Wochenbilanz und Auswertung. Die maximal 4 übertragbaren Tagespunkte werden ebenso auf eine Nachkommastelle genau berechnet.
- Ist das Gewicht einer alten Referenzportion nicht angegeben, fragt FitRocket gezielt danach, statt ein Grammgewicht zu raten. Basislebensmittel mit 0 Punkten können flexibel gewogen werden.

## Bessere Übersicht & Hilfe
- In **Profil → Darstellung → Erklärhinweise** lassen sich zusätzliche Tipps dauerhaft ausblenden oder wieder einblenden. Die Einstellung wird auch im Backup gespeichert.
- Die neue **Profil → App & Hilfe → FAQ** beantwortet 15 Fragen zu Portionen, Zehntelpunkten, Favoriten, Wochenpunkten, KI, Datenschutz, Backups und Updates.
- Im Normalmodus werden kurze Infotexte auf den wichtigsten Seiten reduziert, ohne wesentliche Eingabe- und Sicherheitshinweise zu entfernen.

## Updates direkt in FitRocket
Die bereits integrierte Updatefunktion unter **Profil → App & Hilfe → In-App-Update prüfen** bleibt erhalten: GitHub-APK herunterladen, Signatur und Version überprüfen und mit Android-Bestätigung installieren. Kein manueller APK-Download im Browser nötig.

## Kompatibilität
Die vorhandenen integerbasierten Tagebucheinträge und Favoriten bleiben gültig; neue Dezimalwerte werden als JSON-Zahl gespeichert und beim Backup/Import geprüft. Die bestehende Release-Signierung bleibt unverändert.
