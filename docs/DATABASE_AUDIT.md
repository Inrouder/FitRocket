# FitRocket-Lebensmitteldatenbank

Die lokale Datei `app/src/main/assets/foods.json` enthält **358 Lebensmittel** für ein eigenständiges Ernährungstagebuch.

| Kategorie im Datensatz | Anzahl | Bedeutung |
|---|---:|---|
| `basic` | 280 | Naturbelassene Basiszutaten mit 0 FitRocket-Punkten |
| `portion` | 23 | Vordefinierter Portionswert, manuell anpassbar |
| `nutrition` | 45 | Nährwerte je 100 g/ml; Punkte werden berechnet |
| `custom` | 10 | Punktwert wird selbst festgelegt |

**Grundregeln:** Die Mengen- und Zubereitungsform muss passen. Zucker, Panade, Öle, Saucen und Zusätze werden separat erfasst. Berechnete Nährwertpunkte folgen `docs/FORMULAS.md`. Die IDs bleiben für bestehende Tagebücher und Backups stabil.

Die Datenbank ist kein vollständiger Supermarktkatalog. Zur Verbesserung sind markenspezifische Einträge anhand von Nährwertetiketten vorgesehen. Quellenfelder und alte Markenprogramm-Beschreibungen wurden aus der Nutzer-Datenbasis entfernt.
