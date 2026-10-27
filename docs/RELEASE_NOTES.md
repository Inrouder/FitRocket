# FitRocket v0.12.0 – Klarer, ruhiger und jetzt mit Updates

Die App wurde grundlegend neu strukturiert, ohne deine Ernährungs- und Bewegungsdaten zu verändern.

### Neue Navigation
- Fünf Hauptbereiche: **Heute, Suche, Tagebuch, Fortschritt, Profil**.
- **Heute** zeigt kompakt Tages- und Wochenpunkte, schnelle Erfassung, Mahlzeiten, Wasser und Fortschritt.
- **Fortschritt** bündelt Gewichtsverlauf, Auswertungen, Bewegung, Smartwatch und Wasser.
- **Profil** enthält Name, persönliche Körperdaten, Ziele, Darstellung, KI-Verbindungen, Backups und Updates.

### Darstellung
- Drei Modi: **Systemeinstellung, Hell und Dunkel**.
- Farbpalette, Karten, Navigationsleiste und Diagramme an den Dunkelmodus angepasst.
- Kleinere, weniger dominante Schaltflächen und klar gegliederte Einstellungen.

### Updates und Downloadseite
- **Profil → Auf Updates prüfen** vergleicht deine Version mit dem aktuellen stabilen GitHub Release.
- Release-Notizen anzeigen und aktuelle APK bewusst im Browser herunterladen; keine heimliche Installation.
- APK ist dauerhaft als **FitRocket.apk** in den GitHub Releases verfügbar, anstelle nur zeitlich begrenzter Actions-Artefakte.
- Responsive Download-Startseite (GitHub Pages) mit Release-Version, aktuellem Download und Installationshinweisen.

### Daten und Kompatibilität
- Backup-Format v6 übernimmt jetzt auch Profilname und Darstellung; alte Backups bleiben importierbar.
- Vorhandene Mahlzeiten, Rezepte, Wochenpunkte, Gemini/OpenAI-Schlüssel, Health Connect und Gewichtseinträge bleiben bei einem normalen Update bestehen.
- Aktualisierte Unit-Tests für Versionen und APK-Release-Links.

**Installation:** Bestehende APK normalerweise einfach aktualisieren. Vor Neuinstallation oder Gerätewechsel im Profil ein JSON-Backup exportieren. Die GitHub Actions verwenden eine Debug-Signatur; eine abweichende Signatur kann eine direkte Aktualisierung verhindern.

Die APK wurde automatisiert kompiliert und getestet; die Bedienung auf einem realen Android-Gerät muss weiterhin praktisch geprüft werden.
