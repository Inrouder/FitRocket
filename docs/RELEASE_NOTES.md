# FitRocket v0.18.0 – Pixel-Pet & Fastenziel-Erinnerungen

## Dein neuer Pixel-Begleiter

Im eigenen Reiter **Pet** wartet ein Ei auf dich. Gib deinem Begleiter einen Namen, füttere, streichle und bespiele ihn. Der Axolotl-Hund wächst durch **fünf Entwicklungsstufen**. Seine Stimmung reicht von glücklich bis zu einer zähnefletschenden Monsterform, wenn er mehrere abgeschlossene Tage kein Futter bekommt. Figur, Stimmungen, Zubehör und Garten sind eigens erstellte **PNG-Pixelgrafiken, keine SVGs oder Vektorgrafiken**.

Alle übrigen Tagespunkte werden **nach Tagesabschluss zusätzlich zu fiktivem Futter**. **Deine echte Tages- und Wochenpunkteberechnung bleibt vollständig erhalten.** Derselbe Tag kann nicht mehrfach Futter geben. Beim Einstieg zählt auch gestern; später werden abgeschlossene Tage seit dem letzten Einsammeln nachgeholt.

Füttern lässt dein Pet wachsen und bringt Spielsterne. Streicheln gibt einmal täglich 1 Stern, Spielen 2 Sterne. Damit kannst du **Hundeöhrchen, Raketenmütze, Sternenkrone und eine Spielzeugrakete** freischalten und dauerhaft ausrüsten. Ein Spielball gehört zur Grundausstattung. Die sechs Hauptreiter ordnen sich bei wenig Platz oder großer Schrift in Zeilen an.

## Benachrichtigung zum Fastenziel

Unter **Intervallfasten → Benachrichtigung zum Fastenziel** aktivieren und Android die Benachrichtigungen erlauben. FitRocket erinnert auch bei normal geschlossener App. Die Benachrichtigung öffnet direkt deinen Timer. Zeitänderungen ersetzen die Erinnerung; Beenden oder Abbrechen entfernt sie. Nach einem Geräteneustart oder App-Update wird sie erneut eingeplant.

Auf Android 12+ kannst du optional **Zeitgenaue Erinnerung zulassen**. Ohne diesen Alarmzugriff darf Android die Zustellung verzögern. Die App umgeht keine Android-Freigaben und läuft nicht mit einem dauerhaften Hintergrunddienst.

## Daten und Update

- Name, Futtervorrat, Entwicklung, Stimmung und freigeschaltete Ausstattung werden lokal gespeichert und mit dem JSON-Backup gesichert.
- Alte Backups bleiben importierbar; keine Bestandsmigration. Rezeptfotos, Favoriten, ZeroPoints und Aktivitätsregeln bleiben erhalten.
- Version **0.18.0**, Versionscode **48**, unveränderte App-ID und bestehende Signaturkonfiguration.
- Nach Veröffentlichung unter **Profil → In-App-Update prüfen** aktualisieren.

Details und Prüfszenarien: [Pixel-Pet und Fasten-Erinnerungen](PET_AND_FASTING_REMINDERS.md).

## Prüfstand

159 automatisierte Tests einschließlich Android-Ansichten, Grafik-Atlanten, Alarm-/Benachrichtigungsabläufen auf API 28/35 und Backup-Verhalten. Die Vorschau wurde unter Robolectric gerendert; sie ist kein echter Geräte-Screenshot. Die praktische Geräteprüfung unter Hersteller-Energiesparmodi bleibt offen.
