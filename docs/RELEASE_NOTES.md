# FitRocket v0.12.1 – Bessere Navigation und KI-Gerichte

## Android-Navigationsleiste
- Die FitRocket-Menüleiste berücksichtigt auf Android 11+ eigene Window Insets für die Systemgesten- und Navigationsleiste.
- Auch Android 15 mit Edge-to-Edge-Layout und Geräte mit den drei Android-Tasten werden berücksichtigt.
- Zusätzlicher unterer Sicherheitsabstand und bessere Bedienbarkeit im Dunkelmodus.

## Ganze Gerichte per KI erfassen
- Beim Hinzufügen zu **Frühstück, Mittagessen, Abendessen, Snack oder Getränke** gibt es jetzt die Option **„Ganzes Gericht mit KI berechnen“**.
- Auch auf der Lebensmittel-Suchseite verfügbar. Die Erfassung eines **einzelnen Produkts** bleibt separat möglich.
- Die KI zerlegt eine Beschreibung wie „Eierpfannkuchen mit Milch, Mehl, Eiern, Butter und Marmelade“ in Zutaten und schätzt die jeweiligen Mengen und Nährwerte.
- FitRocket addiert die Punkte der Zutaten selbst anhand seiner bestehenden Formel, statt einem von der KI genannten Gesamtwert zu vertrauen.
- Alle Zutaten und Portionsgrößen können vor der Buchung überprüft, geändert, entfernt oder ergänzt werden.
- Beim Speichern wird **ein** Gericht als Mahlzeit für den gewählten Tag und die gewählte Mahlzeitengruppe eingetragen.
- Die bestätigte Zutatenliste bleibt mit dem Tagebucheintrag gespeichert und kann später über **„Zutaten & Berechnung ansehen“** aufgerufen werden.
- Die bisherigen Basislebensmittel mit null Punkten behalten ihre Sonderbehandlung nur bei exakter Katalogübereinstimmung.

## Qualität
- Automatisierte Berechnungstests umfassen nun auch gemischte Gerichte und Portionsskalierung.
- Ein tatsächlicher KI-Aufruf erfordert weiterhin einen persönlich hinterlegten API-Schlüssel.
- Schätzwerte können stark von tatsächlichen Zutaten abweichen. Insbesondere Öl, Butter und Soßen überprüfen.

Die App-Daten bleiben bei normalem Update erhalten. Vor der Installation empfehlen wir ein JSON-Backup.
