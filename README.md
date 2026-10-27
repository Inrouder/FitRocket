# 🚀 FitRocket

**Dein Tag. Deine Balance.** Ein Android-Tagebuch für Ernährungspunkte, Mahlzeiten, Gewicht, Bewegung und Wasser – mit optionalem KI-Küchenassistenten.

[**📱 Neueste APK herunterladen**](https://github.com/Inrouder/FitRocket/releases/latest) · [**🌐 FitRocket-Startseite**](https://inrouder.github.io/FitRocket/) · [**📝 Alle Releases**](https://github.com/Inrouder/FitRocket/releases)

> Wenn GitHub Pages noch nicht aktiviert ist, funktioniert der APK-Download trotzdem direkt über GitHub Releases. Für die Website unter **Settings → Pages → Build and deployment → Source: GitHub Actions** auswählen.

## FitRocket v0.12.0

**Neuer, aufgeräumter Aufbau:** Die App hat jetzt die fünf Bereiche **Heute**, **Suche**, **Tagebuch**, **Fortschritt** und **Profil**. Die Startseite zeigt nur den wichtigsten Tagesüberblick und die häufigsten Aktionen. Gewicht, Statistiken und Aktivitäten sind unter Fortschritt; Aussehen, persönliche Ziele, KI, Daten und Updates unter Profil.

**Hell und Dunkel:** Unter **Profil → Darstellung** kannst du **System / Hell / Dunkel** wählen. Die Wahl bleibt gespeichert.

**Updateprüfung:** Unter **Profil → Auf Updates prüfen** wird das neueste stabile GitHub Release geladen. Neue APKs werden ausdrücklich vom Nutzer im Browser heruntergeladen und über den Android-Installationsdialog installiert; die App führt keine stillen Updates durch.

**Neuste APK:** Das GitHub Actions-Workflow veröffentlicht nach erfolgreichen Tests automatisch das stabile Release **v0.12.0** und lädt den Build unter dem dauerhaften Dateinamen **FitRocket.apk** hoch. Die responsive [Download-Startseite](https://inrouder.github.io/FitRocket/) liest stets das neueste Release.

### Enthaltene Funktionen

- Eigenständiges **FitRocket-Punktesystem** und **280 Basislebensmittel mit 0 Punkten**.
- **28 Wochenstartpunkte** jeden Montag plus bis zu 4 ungenutzte Tagespunkte aus abgeschlossenen Tagen.
- Mahlzeiten nach Frühstück, Mittagessen, Abendessen, Snacks und Getränke.
- Barcode-Scanner über Google Code Scanner und Nährwertsuche über Open Food Facts.
- Rezeptvorschläge, geschätzte Punkte und eigenes Lebensmittelverzeichnis.
- Optionaler **Gemini-KI-Assistent** (eigener Google-AI-Studio-API-Schlüssel) sowie OpenAI als Alternative.
- Bildliche **250-ml-Wassergläser**, anpassbares Trinkziel.
- Gewichtsskala, Zielgewicht und Gewichtsverlauf.
- Aktivitäten, Schrittzähler und optional Health Connect (Android 14+).
- JSON-Backup und -Import für eigene Daten; API-Schlüssel werden **nicht** exportiert.

## Installation

1. Auf der [Releases-Seite](https://github.com/Inrouder/FitRocket/releases/latest) das neueste **FitRocket.apk** laden.
2. APK auf dem Android-Gerät öffnen und gegebenenfalls die Installation aus dieser Quelle erlauben.
3. Bei vorhandener FitRocket-Installation die APK normalerweise direkt aktualisieren. Vor einer Deinstallation unbedingt **Profil → Backup exportieren** nutzen.

**Hinweis zur Signatur:** Das Projekt wird derzeit mit der automatisch erzeugten Android-Debug-Signatur gebaut. Diese kann zwischen Build-Umgebungen variieren. Wenn Android ein Update wegen einer abweichenden Signatur verweigert, keine bestehenden App-Daten ohne vorherige Sicherung löschen. Für den langfristigen Update-Prozess ist als nächster Schritt eine dauerhafte Release-Signatur empfehlenswert.

## Projekt & Technik

- Java 17, Android SDK 35, minSdk 26
- Prüfungen: `python3 scripts/validate_catalog.py`, `python3 scripts/validate_ai_schema.py` und `gradle :app:testDebugUnitTest :app:assembleDebug`
- APK-Veröffentlichung: [GitHub Actions](.github/workflows/android-apk.yml)
- Website: [docs/index.html](docs/index.html) und [Pages-Workflow](.github/workflows/pages.yml)
- [Berechnungsregeln](docs/FORMULAS.md) · [Health Connect](docs/HEALTH_CONNECT.md) · [KI-Details](docs/AI_ASSISTANT.md)
- [Versionshistorie](CHANGELOG.md) · [Roadmap](docs/ROADMAP.md)

Die App ist ein unabhängiges Hobbyprojekt, keine medizinische Beratung.
