# Pixel-Pet und Fasten-Benachrichtigungen (v0.18.0)

[Vorschau des Pet-Bereichs](images/pet-preview.png) – aus den echten Android-Ansichten unter Robolectric gerendert, kein Geräte-Screenshot.

## Pet-Minispiel

Unter dem eigenen Reiter **Pet** kannst du ein Ei adoptieren und deinem Begleiter einen Namen geben. Das Pet ist eine Mischung aus Axolotl und Fantasy-Hund nach der bereitgestellten Vorlage. Figur, Stimmungen, Zubehör und Garten sind PNG-Pixelgrafiken; es wurden dafür keine SVGs oder Vektorgrafiken erstellt.

- **Futter:** Alle ungenutzten Tagespunkte werden nach Tagesabschluss zusätzlich als fiktive Futterpunkte verfügbar. Es gilt kein 4-Punkte-Limit für diese Spielwährung. Das echte Tages- und Wochenkonto wird weder belastet noch verändert.
- Als Einstieg zählt auch der Tag vor der Adoption. Danach werden abgeschlossene Tage beim Öffnen des Pet-Bereichs automatisch eingesammelt, einschließlich verpasster Tage seit dem letzten Einsammeln. Jeder Tag wird genau einmal berücksichtigt.
- Das heutige Guthaben ist vorläufig, weil weitere Mahlzeiten hinzukommen können; es wird erst ab morgen zu Futter. Berechnungsgrundlage ist das beim Einsammeln eingestellte Tagesbudget. Rückwirkende Mahlzeitenkorrekturen erzeugen keine erneute Belohnung für bereits eingesammelte Tage.
- **Wachstum:** Ausschließlich verfütterte Spielpunkte zählen zur Entwicklung: Ei → kleiner Entdecker ab 8 → Wirbelwind ab 24 → treuer Begleiter ab 60 → Sternenwächter ab 120 verfütterten Punkten. Die Entwicklungsstufe geht nicht verloren.
- **Stimmung:** Ein abgeschlossener Tag ohne Füttern macht traurig, zwei machen grummelig, ab drei erscheint die zähnefletschende Monsterform. Füttern bessert die Stimmung; Streicheln und Spielen können kürzere schlechte Launen abmildern. Es gibt keinen Tod oder Verlust des Pets.
- **Sterne:** Je vollem verfütterten Futterpunkt gibt es 1 Stern; Zehntelpunkte sammeln sich korrekt. Streicheln bringt einmal täglich 1 Stern, Spielen einmal täglich 2 Sterne. Beides bleibt danach ohne zusätzliche Tagesbelohnung nutzbar.
- **Ausstattung:** Hundeöhrchen (5 Sterne), Raketenmütze (12), Sternenkrone (20) und Spielzeugrakete (10). Der Spielball gehört zur Grundausstattung. Freigeschaltete Gegenstände können beliebig gewechselt werden und werden nicht erneut bezahlt. Kopfschmuck und Spielzeug sind unabhängig.

### Daten und Grafikaufbau

`FitRocketPet` enthält die Android-unabhängige Spiellogik. SharedPreferences `pet` ist ein JSON-Objekt; `{}` bedeutet noch nicht adoptiert. Nach Adoption enthält es `name`, `adoptedOn`, `lastHarvestDate`, `foodTenths`, `fedTenths`, `stars`, `lastFedOn`, `pettedOn`, `playedOn`, `ownedItems`, `equippedHead`, `equippedToy`. Futterwerte sind ganzzahlige Zehntelpunkte. `lastHarvestDate` ist ein monotoner Cursor gegen doppelte Gutschriften, auch bei Uhr-Rückstellung, Neustart oder wiederholtem Öffnen. Es werden keine Mahlzeiten und keine echten Punkte verändert. Mutationen validieren Kopien und bewahren unbekannte optionale Felder.

`FitRocketPetUi` bietet Adoption, Namen, Füttern, Pflege und Ausstattung. `FitRocketPetView` zeichnet ausschließlich Bitmap-Ausschnitte: `fitrocket_pet_atlas.png` enthält 5 Spalten (Entwicklung) × 5 Zeilen (glücklich, entspannt, traurig, grummelig, Monster). `fitrocket_pet_items.png` hat 3 × 2 Felder (Öhrchen/Mütze/Krone; Ball/Rakete/Herz). `fitrocket_pet_garden.png` ist die Kulisse. Die PNGs werden ohne Texturfilter und mit reduziertem Decode-Speicher geladen, pro Prozess wiederverwendet und im Hintergrund nicht weiter animiert. Die Figur wippt leicht, Berührung zeigt Pixelherzen. Alle drei Bilder wurden eigens mit Bildgenerierung anhand der Referenz bzw. passender Beschreibungen erstellt.

Der sechste Hauptreiter ist **Pet**; **Profil** hat nun Index 5. Gespeicherte Seiten verwenden weiterhin ihre bisherigen Namen. Auf schmalen Displays oder bei großer Schrift ordnet die Navigation die sechs Reiter in Zeilen an, statt Beschriftungen abzuschneiden. Insets der Android-Systemnavigation werden im Dock berücksichtigt.

## Benachrichtigung am Fastenziel

Unter **Intervallfasten → Benachrichtigung zum Fastenziel** lässt sich die lokale Erinnerung steuern (standardmäßig an). Android 13+ benötigt die Benachrichtigungsfreigabe. Beim ersten Start eines Fastens fragt FitRocket diese bei Bedarf an; später führt der Freigabe-Button zur passenden Android-Einstellung.

`FitRocketFastingReminder` plant genau einen Alarm für das gespeicherte Fastenziel. Er funktioniert bei normal geschlossener App. Auf Android 12+ kann über **Zeitgenaue Erinnerung zulassen** optional der besondere Alarmzugriff freigegeben werden (`SCHEDULE_EXACT_ALARM`). Dann nutzt die App `setExactAndAllowWhileIdle`, sonst `setAndAllowWhileIdle`; Android darf die Erinnerung im Fallback verzögern. Das ist kein dauerhafter Hintergrunddienst und es gibt keine Umgehung der Android-Berechtigungen oder des bewussten Erzwingens eines App-Stopps.

Die Planung wird nach Start, Plan-/Zeitänderung, App-Rückkehr, Boot, App-Update und Uhrzeitwechsel abgeglichen. Beenden, Abbrechen und Deaktivieren entfernen den Alarm und die bestehende Zielbenachrichtigung. Die Benachrichtigung öffnet direkt den Fastentimer, auch wenn die Activity schon läuft.

`FitRocketFastingAlarmRules` prüft vor der Zustellung gespeicherten Start, Ziel und aktuelle Zeit. Veraltete Alarme nach Zieländerungen werden ignoriert. Das optionale Feld `active.notifiedGoal` verhindert eine wiederholte Meldung für dasselbe Ziel und bleibt über Neustarts erhalten. Eine bewusst geänderte Zielzeit darf erneut erinnern. Bei abgelehnten Benachrichtigungen wird kein Zustellmarker gesetzt; nach Freigabe und Rückkehr kann eine überfällige Meldung nachgeholt werden.

## Backup und Kompatibilität

Backup-Schema 7 erhält die optionalen Zusatzfelder `pet` und `fastingReminderEnabled`. Alle Pet-Futterwerte, Entwicklung, Stimmungstage, Namen und Gegenstände werden komplett exportiert/importiert. Alte Backups ohne diese Felder ergeben ein nicht adoptiertes Pet und aktivierte Fasten-Erinnerungen. Es gibt keine Bestandsmigration. Die Vorschau nennt Pet-Name/Stufe und den Benachrichtigungsstatus. Ungültige Spielwährung, unbekannte Ausstattung oder ungültige Einstellungen werden vor dem Import zurückgewiesen. Nach dem Import wird der Alarm passend zum wiederhergestellten Fasten geplant. Tatsächliche Android-Berechtigungen sind gerätespezifisch und werden nicht aus dem Backup übernommen.

Ältere App-Versionen ignorieren die optionalen Felder; ein erneuter Export durch alte Versionen erhält sie nicht. Rezeptfotos, Favoriten, Lebensmittel- und Bewegungsregeln werden nicht verändert.

## Prüfszenarien

- Tages-/Wochenkonto vor und nach Adoption, Einsammeln, Füttern, Spielen und Zubehörkauf vergleichen; gleiche Restpunkte bei aus- und eingeschalteter realer Punkteübernahme.
- Tagesgrenze, Zehntelpunkte, Überziehung, mehrfaches Öffnen, App-Neustart, verpasste Tage und Uhr-Rückstellung. Heutige Restpunkte noch nicht verfüttern; historische Korrektur nicht mehrfach belohnen.
- Alle fünf Entwicklungsstufen und fünf Stimmungen; Füttern, Streicheln und Spielen. Zubehör bezahlen, erneut wählen und über Backup erhalten.
- Hell/Dunkel, große Systemschrift, schmale Displays, Android-Gesten und 3-Tasten-Navigation. Alle sechs Reiter bleiben erreichbar.
- Fastenalarm in Zukunft und überfällig; Ziel verlängern/verkürzen, Start ändern, beenden, abbrechen, Erinnerung deaktivieren. Genau eine Benachrichtigung pro unverändertem Ziel.
- Benachrichtigungsfreigabe und -ablehnung, stummer Kanal, optionaler genauer Alarmzugriff, Energiesparmodus, normal geschlossene App und Geräteneustart. Benachrichtigung öffnet direkt den Timer.
- Backup-Roundtrip mit bestehendem Pet und aktivem Fasten; Alt-Backup ohne Zusatzfelder; ungültige Pet-Daten ohne lokale Ersetzung zurückweisen.


## Automatisierter Prüfstand
159 Tests decken Berechnungen, Spielregeln, Backup, PNG-Atlanten sowie Android-Oberfläche und Alarme ab. Robolectric verwendet Android 9 (API 28) und Android 15 (API 35), inklusive nativer Bitmap-Renderings und zweifacher Schriftgröße. Ein realer Geräte-/Doze-/Neustarttest bleibt zusätzlich sinnvoll. Die globale zusätzliche `lintDebug`-Prüfung ist wegen bestehender Befunde in `MainActivity`/`HealthSync` nicht fehlerfrei; diese Altlasten sind kein bestandener Check.
