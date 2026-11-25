# Gemini-Fehler 503 und Wiederholungen (v0.18.4)

## Befund

Google beschreibt HTTP 503 bei der Gemini API als vorübergehende Überlastung bzw. Nichtverfügbarkeit des Dienstes. Kontingent-/Ratenlimits werden getrennt unter 429 beschrieben; 503 allein belegt weder einen falschen Schlüssel noch ein erschöpftes Kontingent. Eine Ursache der aktuell häufigeren Fehler für ein bestimmtes Nutzerprojekt ist aus dem FitRocket-Quellcode nicht nachweisbar. Dafür wären Zeitpunkte, ausgewähltes Modell und Google-Projektprotokolle nötig; private API-Schlüssel gehören nicht in Chat, Repository oder Fehlermeldungen.

In FitRocket bis 0.18.3 wurde nur ein einziges POST gesendet. Jeder 503 führte sofort zur Fehlermeldung, ohne die von Google empfohlene Pause/Wiederholung. Rohe Fehlerantworten wurden absichtlich nicht angezeigt. Beide auswählbaren Modell-IDs (`gemini-3.5-flash-lite`, `gemini-3.8-flash`) sind in der geprüften offiziellen Modellliste enthalten; die Modellauswahl wird durch dieses Update nicht verändert. Es wurde keine globale aktuelle Störung und kein projektspezifischer Ausfall durch einen Live-Aufruf bestätigt.

Primärquellen:

- [Google: Handling 429 / 503 errors from the Gemini API](https://discuss.ai.google.dev/t/handling-429-503-errors-from-the-gemini-api/124640)
- [Google: Retry strategy / Troubleshooting](https://ai.google.dev/gemini-api/docs/troubleshooting#retry-strategy)
- [Google: Modelle](https://ai.google.dev/gemini-api/docs/models)

## Verhalten ab 0.18.4

- Bei 500/502/503/504 bis zu drei Versuche insgesamt, also höchstens zwei automatische Wiederholungen. Erfolgt direkt eine gültige Antwort, bleibt es bei einem Aufruf.
- Wartepausen von 2 bzw. 4 Sekunden plus kleinem Zufallsanteil. Googles `Retry-After` wird berücksichtigt. Ist die verlangte Pause länger als 15 Sekunden, wird die Anfrage beendet, statt zu früh erneut zu senden.
- Zeitbudget von 120 Sekunden für weitere Versuche und verbleibende Verbindungs-/Lesephasen. Keine Endlosschleife. Einzelne bereits blockierende Übertragungsphasen werden nicht aktiv per Timer abgebrochen.
- Der Ladedialog zeigt an, dass nach kurzer Pause erneut versucht wird. **Abbrechen** verhindert weitere Versuche und eine späte Ergebnisanzeige. Ein bereits laufender Provideraufruf kann bis zum Rücklauf/Timeout bestehen bleiben.
- Bei anhaltendem Fehler erscheinen HTTP-Code, ausgewähltes Modell und Anzahl Versuche. Es wird keine rohe Serverantwort angezeigt.
- Kein automatisches Wiederholen bei Kontingent-/Schlüsselfehlern oder anderen nicht freigegebenen Statuscodes. Bei Verbindungsabbruch/Socket-Timeout wird nicht automatisch erneut gesendet, weil unklar ist, ob der ursprüngliche Aufruf bereits verarbeitet wurde. Ungültige erfolgreiche Modellantworten werden ebenfalls nicht neu generiert.
- Dieselben Eingaben, derselbe Schlüssel und das ausgewählte Modell bleiben innerhalb einer Wiederholung erhalten. Kein automatischer Wechsel zu einem anderen bzw. teureren Modell oder zu OpenAI. HTTP-Weiterleitungen werden nicht verfolgt.

Eine dauerhaft überlastete Google-API kann die App damit nicht reparieren. Dann später erneut versuchen oder die vorhandene Modellauswahl unter KI-Anbieter/Modell bewusst ändern. Eine kostenpflichtige Projektstufe garantiert keine 503-freie Verfügbarkeit. Ob konkrete Fehlversuche vom Anbieter angerechnet werden, wird von FitRocket nicht zugesichert.

## Daten und Prüfungen

Keine neuen Speicher-/Backup-Felder, keine Migration und keine Änderung an Punktelogik, Mahlzeiten oder Rezepten. Die App speichert weiterhin erst nach Nutzerprüfung eines Vorschlags. Es werden nur die bisherigen ausdrücklich freigegebenen Eingaben gesendet; keine Tagebuchhistorie oder Gesundheitsdaten. Fehler-, Retry- und Telemetrielogs mit Schlüsseln/Prompts werden nicht angelegt.

Offline-HTTP-Tests prüfen Wiederholungen, Grenzen, Abbruch, Retry-After, Fehlerklassen und Datenschutz. Ein Test mit dem betroffenen Nutzerprojekt und ein physischer Gerätetest stehen aus.

Manuell prüfen:

1. Normale Gemini-Antwort: genau ein Vorschlag zur Prüfung, keine automatische Speicherung.
2. Simulierter 503, danach Erfolg: Ladehinweis aktualisiert sich, nur die abschließende erfolgreiche Antwort wird angezeigt.
3. Drei 503: eine verständliche abschließende Fehlermeldung mit Modell/Versuchszahl.
4. 429/403/404: keine automatische Wiederholung und passende Meldung.
5. Während der Pause abbrechen oder Activity schließen: kein weiterer Versuch und keine späte Anzeige.
6. Langer Retry-After, Netzunterbrechung und langsame Antwort: Grenzen einhalten, nicht endlos weiterfragen.
7. Bestehende ZeroPoints, Rezeptvalidierung, Schlüsselverschlüsselung und Backups unverändert prüfen.
