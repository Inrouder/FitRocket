# FitRocket – Aktivitätskonto

## Bedienung
1. Unter **Mehr → Bewegungs- und Punkteübersicht** Profil anlegen.
2. **Training hinzufügen**: Sportart, Intensität, Dauer, gegebenenfalls enthaltene Schritte.
3. **Schritte eintragen**: Gesamtwert des Tages und Gehtempo.
4. Im Aktivitätskonto einstellen, welcher Anteil in den Wochenpuffer fließt.
5. Unter **Punkte-Regeln anpassen** Grundschritte und kcal-pro-Punkt-Faktor festlegen.

## Datenlogik
Die Rechenfunktionen liegen in `FitRocketMath.java`. Formeln und Grenzen in `docs/FORMULAS.md`.

Das Profil nutzt Alter, Körpergröße, Körpergewicht und gewähltes Stoffwechsel-Referenzprofil. Die Sportart bestimmt den MET-Faktor; die Intensität wählt die Stufe. Schritte oberhalb des eingestellten Tagesgrundwerts werden berücksichtigt und Workout-Schritte zur Vermeidung von Überschneidungen abgezogen.

Es handelt sich um ein **FitRocket-eigenes Schätzmodell**, keine direkte Energiemessung. Smartwatch- und Fitness-App-Schnittstellen sind derzeit nicht aktiviert.
