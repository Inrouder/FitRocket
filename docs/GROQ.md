# Groq als KI-Anbieter (v0.18.5)

## Einrichtung

1. **Profil → KI-Assistent → KI-Anbieter wechseln → Groq** öffnen.
2. **Groq-Schlüssel einrichten / entfernen → Groq Console öffnen** antippen.
3. In https://console.groq.com/keys anmelden, unter **API Keys** einen persönlichen Schlüssel erstellen und kopieren.
4. In FitRocket einfügen und speichern. Das Feld ist verdeckt; der Schlüssel wird nie wieder im Klartext angezeigt. Keine Schlüssel in Chat, Screenshots oder GitHub teilen.
5. Standard **GPT-OSS 20B** belassen oder unter **Groq-Modell auswählen** ausdrücklich **GPT-OSS 120B** wählen. Anschließend eine Beschreibung senden und die vorgeschlagenen Zutaten/Nährwerte prüfen.

Groq ist ein eigener Anbieter, kein Google-Schlüssel und kein OpenAI-API-Konto. Der Modellname `openai/gpt-oss-*` bezeichnet ein bei Groq ausgeführtes offenes Modell; der Endpunkt liegt bei Groq.

## Kostenloser Tarif und Grenzen

Groq bietet einen Free Plan mit modell-/organisationsabhängigen Raten- und Tokenlimits. Die tatsächlichen Limits stehen in der eigenen Console; sie werden nicht als feste Zahlen in der App versprochen. Nutzung ist nur innerhalb dieses kostenlosen Tarifs und der verfügbaren Limits kostenlos. Wer sein Groq-Konto auf einen bezahlten Tarif umstellt, kann durch dieselben API-Aufrufe Kosten verursachen; FitRocket kann diesen Kontotarif weder erkennen noch begrenzen. Es gibt keine garantierte Verfügbarkeit und keinen gemessenen Zuverlässigkeitsvorsprung gegenüber Gemini. Die Einrichtung aktiviert keine Bezahlstufe.

## Optionale Reserve bei Gemini-Ausfällen

Bei ausgewähltem **Google Gemini** über **Groq-Reserve einrichten** einen eigenen Groq-Schlüssel speichern. Anschließend **Bei Gemini-Ausfall automatisch Groq verwenden** bewusst einschalten. Standard ist aus. Ohne gespeicherten Groq-Schlüssel lässt sich die Option nicht einschalten; beim Löschen des Groq-Schlüssels wird sie ausgeschaltet. Gemini bleibt der ausgewählte Hauptanbieter.

Nach einem ausdrücklichen HTTP 500/502/503/504 und den begrenzten Gemini-Wiederholungen darf genau einmal der gespeicherte Groq-Anbieter gestartet werden. Bei langem Retry-After oder erschöpftem Gemini-Zeitbudget kann dessen Transport schon früher mit diesem HTTP-Fehler enden. Die UI zeigt den Anbieterwechsel. Es werden dieselbe aktuelle Beschreibung, freiwillige Vorlieben und öffentliche Kataloghinweise an Groq gesendet. Keine Tagebuchhistorie, Körper-/Gewichtsdaten oder Health-Connect-Daten. Keine Reserve bei 400/401/403/404/429, Ablehnung/ungültiger Modellantwort, Socket-Timeout oder unklarem Verbindungsabbruch. Kein Wechsel zurück, kein automatischer OpenAI-Aufruf und kein zusätzlicher Modellsprung. Groq besitzt seinerseits höchstens drei Versuche bei denselben expliziten Serverfehlern, maximal also sechs POSTs bei freigegebener Reserve.

Die Grenzen von 120 Sekunden gelten je Anbieter, nicht für beide zusammen. Eine Reserve-Anfrage kann daher insgesamt länger dauern. **Abbrechen** verhindert weitere Wiederholungen, den Reserve-Start und späte Ergebnisanzeigen. Bereits laufende Netzwerkaufrufe können bis zum Rücklauf/Timeout bestehen bleiben. Ein bereits an Groq versandter Aufruf wird durch das Schließen des Dialogs nicht zurückgeholt.

## Protokoll, Daten und Sicherheit

- Fester Endpunkt `https://api.groq.com/openai/v1/chat/completions`, HTTPS, Bearer-Header, keine HTTP-Weiterleitungen. Google-Schlüssel werden ausschließlich als Google-Header an Google gesendet.
- `response_format.type=json_schema`, `json_schema.strict=true`, bestehendes Asset `ai_result_schema.json`. Beide auswählbaren GPT-OSS-Modelle unterstützen laut Groq dieses Format. Keine Streaming-/Tool-Aufrufe.
- Nur vollständiger Chat-Abschluss `stop` mit genau einer textuellen Antwort; danach unverändert `FitRocketAiRules.validateResponse`. Die KI liefert Schätzungen, keine verbindlichen Nährwerte oder Punktezahlen. Punkte/ZeroPoints werden weiterhin lokal bestimmt und Ergebnisse erst nach Nutzerprüfung gespeichert.
- Gemeinsamer `FitRocketAiTransport`: maximal drei Versuche je Anbieter, 2/4 Sekunden plus Zufallsanteil, Retry-After bis 15 Sekunden, 120 Sekunden logisches Budget, 90.000 Request- und 350.000 Response-Bytes. Keine Rohfehler, Prompts oder Schlüssel in Logs/Fehlermeldungen.
- Groq-Schlüssel separat in `fitrocket_ai_private` (`groq_cipher`, `groq_iv`), AES-GCM mit bestehendem Android-Keystore-Alias. Gemini-/OpenAI-Schlüssel bleiben erhalten. Lokale Eingabeprüfung akzeptiert vollständige `gsk_…`-Schlüssel ohne eingebettete Leer-/Steuerzeichen; nur Groq prüft die tatsächliche Berechtigung.
- Neue lokale Einstellungen `aiProvider=groq`, `groqModel` und `geminiGroqFallback` (Standard false). Wie bisherige Anbieter-/Modell-/Schlüsseldaten nicht im JSON-Backup. Keine Änderung am Schema 7, kein Löschen/Umschreiben von Mahlzeiten, Rezepten, Fotos, Favoriten oder Pet. Kein bestehender Schlüssel wird migriert.
- Groq hat eigene Regeln zur Verarbeitung/Aufbewahrung; Einstellungen unter **Console → Data Controls** prüfen. Keine pauschale Zusicherung, dass ein normaler API-Aufruf keinerlei Anbieteraufbewahrung verursacht.

## Prüfung und offene Grenzen

Automatisierte Tests arbeiten mit synthetischen Schlüsseln, Fake-Verbindungen und Android-Dialogen unter Robolectric. Sie sind keine Live-API- oder physischen Gerätetests. Ein echter Groq-Schlüssel und Vergleichsmessungen mit dem betroffenen Gemini-Projekt liegen nicht vor. Nach Einrichtung manuell alle drei KI-Modi, deutsche Antworten, 429-/Schlüsselfehler, Reserve bei 503, Abbruch und Hell-/Dunkeldarstellung mit großer Schrift prüfen. Android-Keystore-Speichern/Entschlüsseln mit echtem neuem Schlüssel sowie Update einer vorhandenen Installation auf dem Gerät prüfen.

Primärquellen (geprüft 25.11.2026):

- https://console.groq.com/docs/billing-faqs
- https://console.groq.com/docs/rate-limits
- https://console.groq.com/docs/structured-outputs
- https://console.groq.com/docs/openai
- https://console.groq.com/docs/your-data
