# 🚀 FitRocket

**Dein Tag. Deine Balance.** Ein Android-Tagebuch für Ernährungspunkte, Mahlzeiten, Gewicht, Bewegung und Wasser – mit optionalem KI-Küchenassistenten.

[**📱 Neueste APK herunterladen**](https://github.com/Inrouder/FitRocket/releases/latest) · [**🌐 FitRocket-Startseite**](https://inrouder.github.io/FitRocket/) · [**📝 Alle Releases**](https://github.com/Inrouder/FitRocket/releases)

> Wenn GitHub Pages noch nicht aktiviert ist, funktioniert der APK-Download trotzdem direkt über GitHub Releases. Für die Website unter **Settings → Pages → Build and deployment → Source: GitHub Actions** auswählen.

## FitRocket v0.15.7 – Auch als 2×1-Widget

Ab sofort gibt es neben den bestehenden Größen auch **FitRocket – Kompakt 2×1**. Das schmale Querformat zeigt die Restpunkte und die getrunkene Wassermenge über einer dezenten Rakete.

## FitRocket v0.15.6 – Kompaktes 1×2-Widget

Unter **Android Widgets → FitRocket** findest du nun zusätzlich ein echtes **Kompakt 1×2-Widget** mit Tagesrestpunkten, Wasserstand und Raketenhintergrund. Die bisherigen Varianten bleiben erhalten.

## FitRocket v0.15.5 – Raketen-Widgets in zwei Formaten

Unter den Android-Widgets stehen jetzt **FitRocket – Horizontal** (breit, 4×2) und **FitRocket – Vertikal** (hochkant, 2×3) zur Auswahl. Beide zeigen Tagespunkte, bereits genutzte Punkte und Trinkmenge und haben eine dezente Rakete im Hintergrund.

## FitRocket v0.15.4 – Widget für den Android-Startbildschirm

Jetzt kannst du ein echtes **FitRocket-Widget** auf deinem Smartphone-Startbildschirm platzieren. Es zeigt die noch verfügbaren Tagespunkte, verbrauchte Punkte und deine getrunkene Menge inklusive Wasser-Fortschritt. Durch Tippen öffnest du FitRocket. Unter **Android-Startbildschirm → Widgets → FitRocket** hinzufügen.

## FitRocket v0.15.3 – Schlichterer Wochenrückblick

Die vier runden Statistik-Kennzahlen entfallen vollständig. Stattdessen zeigt die Fortschrittsseite weiterhin das übersichtliche Balkendiagramm mit Tagesbudget, die Wochenpunkte und die Gewichtsübersicht.

## FitRocket v0.15.2 – Designverbesserungen

- Im Dunkelmodus ist der Gewichtspunkt jetzt weiß mit lila Kontur.
- Die vier unteren Menüschaltflächen sind ungefähr 50 % größer.
- Wochenrückblick und Fortschritt besitzen farbige Kennzahlen und ein übersichtliches Diagramm der Punkte je Wochentag.
- Das Suchfeld bleibt weiß mit dunkler Schrift und wird nicht mehr durch Android rosa eingefärbt.

## FitRocket v0.15.1 – Tagebuch als Hauptseite

**Weniger Reiter, mehr Überblick:** Statt einer separaten Heute-Ansicht startet FitRocket jetzt unmittelbar im Tagebuch. Dort findest du den grafischen Punktebogen (Tagesverbrauch und Restpunkte), deine Wochenpunkte für heute, die Datumsnavigation, die fünf Mahlzeitengruppen und die Wasser-Challenge. Lebensmittelsuche und Favoriten sind direkt erreichbar.

**Neue Navigation:** **Tagebuch · Suche · Fortschritt · Profil**. Alle alten Daten und die integrierte Updatefunktion bleiben erhalten.

[Alle Änderungen in v0.15.1](docs/RELEASE_NOTES.md)

## FitRocket v0.15.0 – Variable Portionen, Gramm & FAQ

- **Portionen oder Gramm:** Schnell 1 Handvoll/Portion/Stück buchen oder eine genaue Menge in g/ml eingeben.
- **Exakte Zehntelpunkte:** 100 g = 1 Punkt → 20 g = 0,2 Punkte; sowohl Tages- als auch Wochenkonto berücksichtigen den Wert.
- **Hilfe auf Wunsch:** Über **Profil → Darstellung → Erklärhinweise** Texte dauerhaft aus- oder einschalten. Unter **Profil → App & Hilfe → FAQ** findest du 15 Antworten.
- **In-App-Updates** laden signierte APKs weiterhin direkt von GitHub herunter und übergeben sie sicher an den Android-Paketinstaller.
- [Änderungen im Detail](docs/RELEASE_NOTES.md).

## FitRocket v0.14.3 – Updates direkt aus der App

In **Profil → In-App-Update prüfen** kannst du eine neue signierte FitRocket-Version direkt herunterladen. Ein Download im Browser und die Suche nach der heruntergeladenen Datei entfallen.

Die App zeigt den Fortschritt, prüft Paket-ID, Versionscode und Signatur gegen die bereits installierte App und übergibt die geprüfte APK an Android. **Die Zustimmung im Android-Installationsdialog bleibt notwendig**, da FitRocket privat und nicht als privilegierter System-App-Store verteilt wird. Beim ersten Update musst du ggf. einmalig „Aus dieser Quelle zulassen“ aktivieren. Die App verwendet dafür keine Root-Rechte und keine Umgehung der Android-Sicherheit.

**Deine Daten bleiben bei einem regulären Update erhalten**. Die APK wird nur bei ausdrücklicher Zustimmung heruntergeladen. Es findet keine heimliche Installation statt. Die automatische Updateprüfung kann dich wie bisher auf neue Versionen hinweisen.

## Änderungen aus Version 0.14.2

## FitRocket 0.14.2 – Wischlöschung und flexible Favoriten

- Tagebuch: einen versehentlich gebuchten Eintrag nach links wischen, Sicherheitsabfrage bestätigen und Tages-/Wochenpunkte neu berechnen lassen.
- Eigene Lebensmittel aus Barcode-Scanner oder KI: in der Lebensmittel-Suche nach links wischen und löschen. Die 358 mitgelieferten Lebensmittel, insbesondere die Basislebensmittel, sind geschützt.
- Favoriten merken sich nun eine Referenzmenge, werden aber **bei jeder Buchung** mit frei wählbaren Gramm bzw. Millilitern neu berechnet.
- Nährwertbasierte Lebensmittel werden aus ihren Kalorien, Zucker, gesättigten Fettsäuren und Eiweiß pro 100 g/ml neu bewertet, statt alte Punkte zu kopieren.
- Für KI-Gerichte werden die Zutaten proportional skaliert, mit aktualisierter Zutatentabelle.
- Ältere Favoriten ohne gespeicherte Grammzahl fragen einmal nach der ursprünglichen Menge.
- Bestehende Tagebucheinträge und Backups bleiben erhalten; die neuen Referenzmengen werden mit den Favoriten exportiert.
- Zusätzliche Unit-Tests für Teig-/Zutatenmengen, Getränkemengen und alte Favoriten.

## Änderungen aus FitRocket v0.14.1 – Lesbares Suchfeld und richtige Portionsangaben

- **Lebensmittelsuche:** Immer weißes Eingabefeld mit dunkler Schrift und gut erkennbarem Suchhinweis – sowohl im Hell- als auch im Dunkelmodus.
- **Richtige Einheit:** Gekochte Nudeln, Reis und andere feste Lebensmittel werden mit **Gramm (g)** angezeigt, Getränke mit **Millilitern (ml)**. Die verwirrende allgemeine Angabe „100 g (beim Getränk 100 ml)“ entfällt aus der Nutzeroberfläche.
- **Buchung:** Auch bei der Mengenwahl und im Tagebuch erscheint die zum Lebensmittel passende Einheit.
- Keine Änderung der Nährwerte oder FitRocket-Punkteformel. Zusätzliche JUnit-Tests für Gramm, Milliliter und bestehende Portionsgrößen.

## Änderungen aus FitRocket v0.14.0 – Alltag & Komfort

Neu: Favoriten-Mahlzeiten speichern und erneut buchen, Wochenrückblick, Wassererinnerungen mit Ruhezeiten, Gewichtstrend, sichere Backup-Inhaltsvorschau und zurückhaltende automatische Updateprüfung. Alle Daten bleiben lokal gespeichert.

Das Release wird nur mit deinem dauerhaft eingerichteten Android-Keystore signiert. [Update-Zertifikat & Sicherheit](docs/SIGNING.md).

## Änderungen aus FitRocket v0.13.0 – Raketen-Icon und dauerhaftes Update-Zertifikat

**Neues App-Icon:** FitRocket hat ein adaptives Raketen-Icon mit dunklem Nachthimmel, orangem Antrieb und einem grünen Blatt. Es wird vom Android-Launcher in passender Form dargestellt.

**Dauerhafte Android-Updates:** GitHub Actions kann jetzt eine mit deinem **privaten, dauerhaft gültigen Signaturzertifikat** signierte Release-APK erstellen. Der Keystore wird ausschließlich aus GitHub Actions Secrets beim Build eingelesen. Solange keine vier Secrets hinterlegt sind, gibt es **nur Test-Builds**, aber keine neuen stabilen Veröffentlichungen.

**Einmalige Einrichtung:** [Schritt-für-Schritt-Anleitung: Zertifikat unter Windows erstellen und GitHub-Secrets eintragen](docs/SIGNING.md).

**Achtung beim Signaturwechsel:** Die bisher über CI verteilte Debug-App v0.12.1 hat höchstwahrscheinlich einen anderen Schlüssel. Beim ersten Wechsel auf die neue Release-Signatur musst du daher die alten Daten exportieren, die Debug-App deinstallieren, die neue signierte App installieren und das Backup importieren. Danach funktionieren Folgeupdates mit demselben Keystore.

**Bis der Signaturschlüssel eingerichtet ist, ist die offizielle stabile Version weiterhin v0.12.1**. Das ist Absicht, damit kein unsignierter oder anders signierter Build als stabiles Update bereitgestellt wird.

## Änderungen aus FitRocket v0.12.1

**Neu:** Korrigierte untere Navigation oberhalb der Android-Systemtasten und ganze Gerichte per KI berechnen. Beim Hinzufügen zu Frühstück, Mittagessen, Abendessen, Snack oder Getränke findest du jetzt **„Ganzes Gericht mit KI berechnen“**. Das Gericht wird in geschätzte Zutaten und Mengen zerlegt; die eigene FitRocket-Punkteformel berechnet den Gesamtwert. Ein bestätigtes Gericht wird als ein Tagebucheintrag mit später sichtbarer Zutatenliste gespeichert.

## Änderungen aus Version 0.12.0

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
