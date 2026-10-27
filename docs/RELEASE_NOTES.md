# FitRocket v0.14.2 – Favoriten und Löschgesten

## Neue Wischfunktion
Im **Tagebuch** kannst du einen falsch gebuchten Mahlzeiteneintrag nach links wischen. Nach der Löschbestätigung verschwindet nur dieser Eintrag; Tages- und Wochenpunkte werden neu berechnet.

In der **Lebensmittelsuche** lassen sich selbst hinzugefügte Produkte aus Barcode-Scanner oder KI ebenfalls durch Wischen nach links und Bestätigung entfernen. Die mitgelieferte Lebensmittelliste bleibt geschützt. Bestehende Tagebucheinträge bleiben auch nach dem Löschen eines persönlichen Produkts erhalten.

## Favoriten mit freier Portionsmenge
Ein als Favorit gespeichertes Lebensmittel ist nicht mehr auf die beim ersten Mal eingetragene Menge festgelegt. Stattdessen kannst du **bei jedem Hinzufügen** selbst 100 g, 175 g, 250 ml usw. angeben. FitRocket berechnet die neuen Punkte auf Grundlage der bekannten Nährwerte oder der hinterlegten Zutaten. Die ursprüngliche Referenzmenge wird gespeichert und kann bei alten Favoriten, falls unbekannt, einmalig ergänzt werden.

## Qualität
- Zusätzliche JUnit-Tests für variable Portionsmengen, KI-Zutaten und historische Favoriten.
- Bestehende Nährwerte, ZeroPoint-Regeln, Wochenpunkte und Signatur bleiben unverändert.
- Bei manuell vergebenen Punkten ohne Nährwerte skaliert FitRocket den gespeicherten Punktwert proportional zur bekannten Referenzmenge.
- Zum Schutz vor Fehlbedienung müssen Löschungen bestätigt werden.
