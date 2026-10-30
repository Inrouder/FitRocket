# FitRocket – Versionshinweise

## 0.16.10 – Persönliche Notizen für Favoriten
- Beim Speichern eines Favoriten eine optionale mehrzeilige Notiz hinzufügen.
- Notizen in der Favoritenübersicht anzeigen, nachträglich bearbeiten oder entfernen.
- Die Notiz wird beim erneuten Buchen angezeigt, aber nicht in Punkte oder Nährwerte eingerechnet.
- Speichern im bestehenden `favoriteMeals`-JSON, inklusive Export und Import; alte Favoriten bleiben kompatibel.
- Validierung auf maximal 600 Zeichen und Tests für Notizen, Backup-Roundtrip und unveränderte Punkte.


## 0.16.9 – Audit- und ZeroPoints-Korrekturen
- Der originale ZeroPoints-Katalog bleibt auch bei großen Mengen und Rezepten bei 0 Punkten.
- Gemeinsame Zehntelpunkt-Berechnung für Lebensmittel, Rezepte, KI-Vorschau und Favoriten.
- Eigenrezepte verknüpfen kanonische Zutaten-IDs; fehlende Zutaten werden nicht automatisch als Null bewertet.
- Verhindert neue Dubletten; historische Dubletten im Suchergebnis ausgeblendet ohne IDs zu löschen.
- Schutz für referenzierte Zutaten, Fehlerhinweise statt verschwundener Rezepte, stabile Bildschirmwiederherstellung.
- Rezeptfotos aus SharedPreferences in private Dateien ausgelagert; weiterhin mit Backup gesichert.
- Aktuelle Trainingspunkte und Schritt-Snapshots stabilisiert, Health-Connect-Duplikate nach Sportart unterschieden.


## 0.16.8 – Lebensmittel-Duplikate und Rezeptportionen
- KI-Rezepte referenzieren vorhandene Lebensmittel anhand validierter ID oder exakt normalisiertem Namen, statt dieselben erneut anzulegen.
- Innerhalb eines Rezepts neu angelegte Zutaten werden ebenfalls wiederverwendet.
- Punkte je Portion: Mengen aller Zutaten auf die Portionsanzahl teilen, Nährwerte aufsummieren und einmal je Portion runden.
- Die KI-Vorschau verwendet ebenfalls bestehende Katalogwerte, wenn passende Lebensmittel vorhanden sind.

## 0.16.7 – Gebuchte Mahlzeiten verschieben
- Bereits gebuchte Lebensmittel über die Eintragsoption „Tageszeit ändern“ zwischen Frühstück, Mittagessen, Abendessen, Snack und Getränke verschieben.
- Datum, Portion und Punkte bleiben unverändert; die Tagesübersicht wird sofort aktualisiert.

## 0.16.6 – Gewichtsdiagramm besser lesbar
- Dunkle, klare Verbindungslinie mit kreisförmigen Messpunkten und Datumsachse entsprechend tatsächlicher Messzeitpunkte.
- Gestrichelte Zielgewichtslinie mit Gewichtsangabe und vier bis fünf gut verteilten Datumsmarkierungen.
- Angepasste Kontraste für helles und dunkles Design; Vorschau in Fortschritt vergrößert.

## 0.16.5 – Eigene Rezepte per Wischen löschen
- Selbst angelegte und gespeicherte KI-Rezepte im Kochbuch nach links wischen, Löschung anschließend bestätigen.
- Mitgelieferte Standardrezepte bleiben geschützt und können weiterhin nur ausgeblendet werden.
- Bisher gebuchte Mahlzeiten bleiben unverändert. Die gespeicherten Rezeptdaten inklusive Foto werden aus dem lokalen Kochbuch entfernt.

## 0.16.4 – Kochbuch-Kategorien und Standardrezepte
- Mitgelieferte Rezepte per Schaltfläche aus dem persönlichen Kochbuch ausblenden und auf Wunsch gesammelt wiederherstellen.
- Eigene frei benannte Kochbuch-Kategorien für Standard-, KI- und eigene Rezepte; Filter über Kategorieauswahl.
- Kategorien und ausgeblendete Standardrezepte werden im JSON-Backup gespeichert und wiederhergestellt.

## 0.16.3 – Aktuelle Update-Informationen
- GitHub Releases erhalten künftig zur jeweiligen App-Version passende Release Notes.
- Die Build-Pipeline prüft, ob die Versionshinweise für die aktuelle Version vorhanden sind.
- Fehlende Versionshistorie ab 0.15.8 nachgetragen; alte Update-Beschreibungen werden nicht mehr für neue Releases wiederverwendet.

## 0.16.2 – Lebensmittelsuche aufgeräumt
- Suche, Barcode/Produktnummer, KI-Küchenhelfer und Trefferliste als getrennte, abgerundete Karten.
- Mehr Abstand und bessere Lesbarkeit; bisherige Scan-, KI- und Suchfunktionen bleiben erreichbar.

## 0.16.1 – Rezeptfotos, Kamera und Backup
- Rezeptbilder über Galerie oder Android-Kamera (Vorschaubild) erfassen.
- Komprimierte Rezeptfotos im JSON-Backup speichern; ältere Bild-URIs beim Export nach Möglichkeit einbetten.
- Navigationshöhe und Beschriftungen weiter korrigiert.

## 0.16.0 – Raketenhintergrund und Navigation
- Dezenter Raketen-Vektorhintergrund mit farblichem Verlauf.
- Schwebende, abgerundete Navigation mit getrennten einzeiligen Labels und größeren Symbolen.
- Bestehenden KI-Rezeptprozess für erkannte Zutaten aus Rezeptlinks eingebunden.

## 0.15.9 – Neues Kochbuch
- Eigenständiger fünfter Hauptreiter Kochbuch.
- KI-Rezepte, manuelle Rezepte, Recipe-JSON-LD-Linkimport und lokale Rezeptbilder.
- Stärker abgerundete Karten und überarbeitetes Navigationsthema.

## 0.15.8 – Lebensmittel und Bewegungspunkte
- Widersprüchliche pauschale Einträge für alle Nudeln und Reisarten entfernt.
- Aktivitätsgutschrift standardmäßig zu 100 Prozent und am gleichen Tag für das Wochenkonto verfügbar.


## 0.15.7 – Zusätzliches 2×1-Raketenwidget
- Flaches 2×1-Widget mit Restpunkten links und Trinkmenge rechts.
- Dekorativer halbtransparenter Raketenhintergrund und kompakte Beschriftung.
- Vier unabhängig auswählbare Widget-Varianten, alle mit denselben lokalen Live-Werten.

## 0.15.6 – Echtes 1×2-Homescreen-Widget
- Neues Mini-Widget mit einer Spalte und zwei Reihen, zusätzlich zu den bestehenden horizontalen und vertikalen Widgets.
- Restpunkte, getrunkene Liter, Wasserbalken und halbtransparente Rakete.
- Automatische Tagesaktualisierung und Daten-Synchronisierung mit den anderen Widgets.

## 0.15.5 – Vertikales und horizontales Raketen-Widget
- Eigenständiges Hochkant-Widget (2×3) zusätzlich zum breiten 4×2-Widget.
- Beide Widgets im FitRocket-Weltraumdesign mit transparenter Rakete, weißer Punktzahl und Wasseranzeige.
- Gemeinsame Live-Aktualisierung aller platzierten Widgets bei Punkten, Trinkmenge und Systemereignissen.
- Keine Änderung der Ernährungsdaten und Punkteformel.

## 0.15.4 – Homescreen-Widget für Punkte und Trinkmenge
- Android-App-Widget zeigt verbleibende und genutzte Tagespunkte sowie Trinkmenge mit Ziel-Fortschritt.
- Aktualisierung nach neuen Mahlzeiten und Wasseränderungen, beim Öffnen der App und über Systemaktualisierungen.
- Startet FitRocket direkt durch Tippen; lokal und ohne Netzwerk oder neue Berechtigungen.
- Tests für Tageswechsel, Dezimalpunkte und Wasserziele.

## 0.15.3 – Wochenrückblick entschlackt
- Runde Kennzahlen aus Fortschritt und Wochenrückblick entfernt.
- Das Balkendiagramm und das Wochenpunktekonto bleiben erhalten; keine Änderung von Daten oder Punkten.

## 0.15.2 – Größere Navigation, lesbares Gewichtsdiagramm, grafischer Wochenrückblick
- Der neueste Gewichtspunkt ist im Dunkelmodus weiß mit lila Kontur.
- Untere Navigationsschaltflächen etwa 50 % größer: Höhe 55→83 dp, Text 11→16 sp.
- Neuer Wochenrückblick und Fortschrittsüberblick mit vier runden, farbigen Kennzahlen und einem 7-Tage-Punkte-Balkendiagramm.
- Tagesbudget als gestrichelte Zielmarke; Tage ohne Einträge werden nicht als Nulltage gewertet.
- Lebensmittel-Suchfeld in beiden Designs weiß mit dunkler Schrift; alte Android-Tönung entfernt.
- Bestehende lokale Daten, Punktformel und Signatur bleiben erhalten.

## 0.15.1 – Tagebuch ersetzt Heute als Hauptseite
- Separaten Heute-Bildschirm vollständig entfernt. FitRocket öffnet direkt das Tagebuch.
- Vier statt fünf Hauptreiter: Tagebuch, Suche, Fortschritt und Profil.
- Punktekreis für den gewählten Kalendertag direkt über den Mahlzeiten; Tagesverbrauch und Tagesrest sichtbar.
- Verfügbare Wochenpunkte auf der heutigen Tagebuchseite; bei vergangenen Tagen stattdessen das Tagesziel zur Vermeidung irreführender Kontostände.
- Datum über Pfeile und Kalender wählbar; Punktebogen und Mahlzeiten aktualisieren sich gemeinsam.
- Direkte Schnellzugriffe auf Lebensmittelsuche und Favoriten im Tagebuch.
- Alle Buchungs-, Lösch-, Import- und Synchronisierungsrücksprünge führen auf die neue Tagebuch-Hauptseite.
- Ausgewähltes Datum über Bildschirmdrehung beibehalten; bei Mitternacht wird der neue Tag angezeigt, sofern zuvor der aktuelle Tag gewählt war.
- Bestehende Lebensmittel, Favoriten, Punktestände, Profile und Backups bleiben unverändert.
- Automatisierte Tests für neue Startseite und Vier-Reiter-Navigation.

## 0.15.0 – Portionen, Zehntelpunkte und FAQ
- Portion oder Gramm/Milliliter als auswählbare Erfassungsart für alle bekannten Lebensmittel.
- Geprüfte Referenzmengen in g/ml und frei einstellbare Portionsfaktoren.
- Dezimalwerte für die tatsächliche Buchung und die Tages-/Wochenbilanz (z. B. 0,2 P).
- Nährwertbasierte Produkte auf Basis der 100-g-/100-ml-Referenz proportional berechnet.
- Favoriten-Zutaten und Mengen mit genauer Zehntelpunkteberechnung.
- Das Wochenkonto behält den täglichen Übertrag von maximal 4 P bei, jetzt dezimalgenau.
- Hilfetexte dauerhaft über einen Schalter im Profil aus- und einblendbar, im Backup enthalten.
- Integrierte FAQ-Seite mit 15 praxisnahen Antworten.
- Dezimalvalidierung für ältere und neue JSON-Backups; zusätzliche Tests für Gramm, Portion, Favoriten und Wochenpunkte.
- Die sichere In-App-Updatefunktion und bestehende App-Signatur bleiben erhalten.

## 0.14.3 – Komfortable sichere In-App-Updates
- Neue APK vom offiziellen GitHub Release direkt im FitRocket-App-Speicher herunterladen, mit Fortschrittsanzeige.
- Nur fest definierte HTTPS-GitHub-Release-Adressen und vertrauenswürdige CDN-Weiterleitungen akzeptieren.
- Paket-ID, höhere Versionsnummer und identisches Android-Signaturzertifikat vor Installation kontrollieren.
- Über Androids FileProvider an den Systeminstaller übergeben; kein Browser und keine manuelle APK-Suche.
- Bei Bedarf einmalig Berechtigung für Installation „Aus dieser Quelle“ anfordern; Android-Installation weiter nur nach Bestätigung.
- Keine automatische Installation ohne Einwilligung oder privilegierte Berechtigungen.

## 0.14.2 – Wischen zum Löschen und variable Favoriten
- Tagebucheinträge per Wisch nach links mit Sicherheitsabfrage löschen.
- Eigene KI-/Barcode-Lebensmittel ebenfalls per Wisch nach links aus der persönlichen Liste entfernen; feste FitRocket-Katalogeinträge sind geschützt.
- Favoriten erhalten eine gespeicherte Referenzmenge und frei wählbare Gramm-/Millilitermengen beim nächsten Hinzufügen.
- Punkte aus Nährwerten oder bestätigten KI-Zutaten neu berechnen, statt eingefrorenen Favoriten-Punktwert wiederverwenden.
- Rückwärtskompatibel: Bei alten Favoriten ohne Mengeninformation wird die ursprüngliche Menge einmal erfragt.
- Mehrere Unit-Tests für Mengenskalierung und alte Einträge.

## 0.14.1 – Lebensmittelsuche mit gutem Kontrast und korrekten Portionen
- Eingabefeld der Suche dauerhaft weiß mit dunkler Schrift und gut sichtbarem Hinweis.
- Einheitendarstellung aus der Lebensmittelkategorie: feste Produkte in g, Getränke in ml.
- Keine generische Getränkezusatzbeschreibung mehr bei Nudeln und anderen festen Lebensmitteln.
- Menge und Nährwerte im Erfassungsdialog eindeutig beschriftet; gebuchte Essensmenge mit korrekter Einheit.
- Sechs zusätzliche automatisierte Tests für Portionsdarstellung und unveränderte bestehende Portionen.

## 0.14.0 – Favoriten, Wochenrückblick und Komfort
- Gespeicherte Mahlzeiten lassen sich als Favoriten erneut ins Tagebuch übernehmen.
- Wochenrückblick zeigt tatsächliche erfasste Tage, Verbrauch und Wasserbilanz.
- Wassererinnerungen mit Ruhezeiten und freiwilliger Benachrichtigungsfreigabe.
- Gewichtstrend: 7-Tage-Durchschnitt und geglätteter Monatsvergleich.
- Backup-Format 7 mit Vorschau und Datensatzvalidierung vor dem Überschreiben.
- Dezente automatische GitHub-Updateprüfung maximal einmal täglich, abschaltbar.
- Bestehender Android-Release-Signaturschlüssel bleibt unverändert.

## 0.13.0 – Raketen-Icon und eigenes Signaturzertifikat
- Eigenes adaptives Android-App-Icon mit Rakete und gesundem Food-Akzent.
- Dauerhafte Release-Signatur mit privatem Keystore aus vier GitHub-Actions-Secrets vorbereitet.
- Offizielle GitHub Releases werden ab dieser Version ausschließlich nach erfolgreichem signiertem Build veröffentlicht.
- GitHub-Runner prüft die APK-Signatur mittels Android apksigner vor Veröffentlichung.
- Fehlende Secrets: Debug-Artefakt nur für Entwicklung, kein stabiles Release. Unvollständige Secrets: CI-Fehler statt unsicherer Veröffentlichung.
- Signierdateien und Kennwort-Dateien in .gitignore aufgenommen.
- Ausführliche Windows-PowerShell-Anleitung und Migrationshinweis für bereits installierte Debug-Versionen.

## 0.12.1 – Systemnavigation und KI-Gerichte
- Navigation auf Android 15/Edge-to-Edge und Drei-Tasten-Geräten mit getrennten Window Insets für Kopf- und Fußbereich korrigiert.
- KI-Erfassung nach Frühstück/Mittagessen/Abendessen/Snack/Getränke analysiert jetzt vollständige Gerichte mit mehreren Zutaten.
- Einzelne neue Lebensmittel separat vom KI-Gerichtemodus erfassbar.
- KI-Gericht wird als ein Eintrag mit selbst berechneter Punktsumme, Mahlzeitengruppe, Datum und überprüfbarer Zutatenliste gespeichert.
- In der Tagesansicht lässt sich die Zutaten-/Punkteaufschlüsselung später ansehen.
- Mehrere neue Tests für zusammengesetzte Gerichte und Portionen.

## 0.12.0 – Neue App-Navigation, Hell/Dunkel, Profil, Releases und Updates
- Fünf Hauptbereiche: Heute, Suche, Tagebuch, Fortschritt und Profil.
- Heute-Seite verschlankt: kompakte Punktestatusanzeige, schnelle Erfassung, Mahlzeitenübersicht, Wasser und Fortschrittslinks.
- Fortschritt bündelt Gewicht, Statistik, Aktivitäten und Wasserbilanz.
- Profil erhält Name, Körperdaten, Ziele, Darstellung, KI- und Smartwatch-Einstellungen, Backups, Versionsangabe und Updateprüfung.
- Hell-/Dunkelmodus inkl. Systemeinstellung; Karten, Navigation und Diagramme dunkeltauglich.
- Release-Checker mit Versionsvergleich, geprüften GitHub-Links und explizitem APK-Download statt stiller Installation.
- GitHub Actions erstellt dauerhaftes stabiles Release mit FitRocket.apk nach erfolgreichen Tests.
- Neue GitHub-Pages-Website mit dynamischem Download der aktuellen Release-APK.
- Backup-Schema 6 um Profilnamen und Darstellungswahl erweitert, bisherige Backups weiterhin lesbar.
- Unit-Tests für Versionsvergleich, gültige APK-URLs und Release-Metadaten.

## 0.11.2 – Neue Gemini-Modelle für neue API-Projekte
- Behoben: HTTP 404 bei neuen Gemini-Projekten durch die bisherige Standardauswahl von Gemini 2.5 Flash-Lite.
- Gemäß Googles Modellübersicht aktualisiert: Gemini 3.5 Flash-Lite als Standard und Gemini 3.8 Flash als Qualitätsoption.
- Vorhandene 2.5-Modell-Einstellungen werden ohne Löschen des API-Schlüssels automatisch auf aktuelle Modelle abgebildet.
- Modellwahl und KI-Assistent zeigen die aktuellen Modellnamen an.
- Verbesserte Fehlermeldung, falls ein Modell weiterhin nicht für einen API-Schlüssel verfügbar ist.
- Zusätzliche Tests für alte Einstellungen und den 404-Fehler.

## 0.11.1 – Korrektur der Gemini-Key-Eingabe
- Eingabefehler bei neueren Google-Gemini-Authentifizierungsschlüsseln behoben, inklusive Punkt im Schlüsselformat.
- Keine unzuverlässige Präfix- oder Zeichenformatprüfung mehr, die gültige Google-Schlüssel aussperrt; tatsächliche Authentifizierung wird über die Google-API geprüft.
- Weiterhin Schutz vor Leerzeichen und Steuerzeichen im HTTP-Header.
- Explizite Passwortmaskierung des Schlüsselfeldes gegen Offenlegung auf Screenshots.
- Acht JUnit-Tests für ältere und neuere Schlüsseltypen, Copy/Paste und ungültige Eingaben.

## 0.11.0 – Google Gemini, kostenlose API und Anbieterwahl
- Gemini 2.5 Flash-Lite als sparsame Voreinstellung für Rezeptgenerator, Mahlzeiten-Einschätzung und neue Lebensmittel.
- Gemini 2.5 Flash als optionales Qualitätsmodell auswählbar.
- OpenAI bleibt als bisheriger, getrennt konfigurierbarer Anbieter verfügbar.
- Android-Keystore-Schlüsselspeicher erweitert: Gemini-Schlüssel separat; zuvor gespeicherter OpenAI-Schlüssel bleibt erhalten.
- Gemini REST v1beta generateContent mit JSON-Schema-Ausgabe und lokaler FitRocket-Validierung.
- Verständliche Fehlerbehandlung für fehlende Berechtigung, falschen API-Schlüssel, ausgelastetes Kontingent, Modellverfügbarkeit und Dienstfehler.
- KI-Anbieter und Gemini-Modell über **Mehr → KI-Assistent** auswählbar.
- Keine KI-Zugangsgeheimnisse in GitHub/Backups; kein automatischer Wechsel auf kostenpflichtige Dienste.
- Zusätzliche Unit-Tests für Anfrageformat, Antwortparser und Datenschutz der Kataloghinweise.

## 0.10.0 – KI-Küchenassistent, Rezepte und Lebensmittelschätzung
- Online-KI für persönliche Rezepte, Beschreibungen von Mahlzeiten und Nährwert-Schätzung neuer Produkte.
- Eigene, auf dem Gerät geschützte API-Schlüssel-Eingabe (Android Keystore, AES-GCM), ohne Hardcoding und ohne Schlüssel in Backups.
- Strukturierte API-Antworten mit JSON-Schema; einheitliche Zutaten-/Nährwertformulare.
- Die Punkte stammen ausschließlich aus FitRocketMath, nicht aus einer Modellbehauptung.
- Nur exakt zugeordnete, unveränderte Katalog-Basislebensmittel erhalten 0 Punkte.
- Für jedes KI-Ergebnis editierbare Zutaten, Portionsmengen und Nährwerte.
- Fehlende Zutaten lassen sich ergänzen, falsch erkannte entfernen.
- KI-Rezepte können dauerhaft gespeichert und im normalen Rezeptbuch verwendet werden.
- Neue Lebensmittel landen nur nach ausdrücklicher Nutzerbestätigung in der lokalen Lebensmittelsuche.
- Offline-Formular für neue Lebensmittel anhand von vier Nährwertangaben.
- Gespeicherte Ernährungsvorlieben, begrenzte Eingabelängen, API-Timeouts und erkennbare Fehlermeldungen.
- Backupschema 5 für eigene KI-Lebensmittel und Rezepte, niemals für den API-Schlüssel.
- Automatisierte Tests für KI-Punkteregeln, Katalog-ID-Abgleich und strikte Antwortstruktur.

## 0.9.0 – Übersicht, Barcode, Wasser, Rezepte & Gewicht
- Heute-Ansicht neu gegliedert und Schnellaktionen ergänzt.
- Frühstück, Mittagessen, Abendessen, Snacks und Getränke als eigene Tagebuch-Bereiche.
- Tagebuch nach Datum navigierbar; ältere Mahlzeiten ohne Gruppenzuordnung bleiben sichtbar.
- Google-Code-Scanner und manuelle Barcode-Eingabe.
- Open-Food-Facts-v3-Produktabfrage mit Pflichtprüfung aller vier Nährwerte und Korrektureingabe.
- Gefundene Scanprodukte werden lokal gespeichert und in die Lebensmittelsuche integriert.
- 13 offline verfügbare Rezeptvorschläge mit konkreten Katalog-Zutaten und berechneten Punkten.
- Trinktagebuch mit bildlichen 250-ml-Gläsern, Plus/Minus und einstellbarem Ziel.
- Gewichtseintrag über Skala, grafische Zeitreihe und Zielgewicht.
- JSON-Backup auf Schema 4 erweitert: Wasser, Gewichtsziel, eigene gescannte Lebensmittel.
- Automatische Katalog- und Rezeptprüfung im GitHub-Workflow.
- Vorhandenes Wochenkonto mit 28 Startpunkten und bestehende Health-Connect-Funktionen bleiben erhalten.

## 0.8.1 – 28 Wochenpunkte und Tagesübertrag
- 28 Punkte stehen jeden Montag als neues Wochenstartguthaben bereit.
- Unverbrauchte Tagespunkte bis maximal 4 werden nach Abschluss des Tages dem aktuellen Wochenkonto gutgeschrieben.
- Gutschriften gelten für abgeschlossene Tage von Montag bis gestern, nicht für den laufenden Tag oder künftige Tage.
- Nicht dokumentierte Tage gelten als ungenutzt und erhalten den Tagesübertrag.
- Wochenverbrauch durch Tagesbudget-Überschreitungen wird abgezogen.
- Vorhandene Aktivitätspunkte-Gutschriften bleiben erhalten; Guthaben wird getrennt erläutert.
- Tagesbezogene Gutschriften im Aktivitätskonto aufgelistet, Änderungen an vergangenen Mahlzeiten werden erneut berechnet.
- Einmalige Umstellung des Wochenstartwerts auf 28 Punkte, bereits bestehende Tagebuchdaten bleiben unangetastet.
- JUnit-Tests für Wochenwechsel, Tagesabschluss, Begrenzung und Verbrauch.

## 0.8.0 – Smartwatch-Synchronisation
- Health Connect direkt über Android 14+ Framework-API lesend integriert.
- Schritte werden für die aktuelle Kalenderwoche täglich aggregiert.
- Trainingsaufzeichnungen aus verbundenen Fitness-Apps werden nach Dauer und Aktivitätstyp in FitRocket übernommen.
- Doppelte Trainingsimporte werden anhand der Quell-ID sowie Uhrzeit/Dauer vermieden.
- Schrittschätzung berücksichtigt bereits angerechnete Workout-Schritte.
- Schrittquelle kann zwischen synchronisierten und manuellen Tageswerten umgeschaltet werden.
- Auf Wunsch beim Öffnen nach mindestens 20 Minuten erneut abgleichen; manueller Sync jederzeit möglich.
- Health-Connect-Berechtigungen, Datenschutzhinweise und Löschung eingelesener Fitnessdaten.
- Backupschema 3 mit Schrittcaches, Ausblendeliste und Quellenauswahl; Import älterer Backups bleibt möglich.
- Automatisierte Unit-Tests der Deduplizierung.

## 0.7.0 – Persönliche Berechnungen und eigenes Punktemodell
- Vollständiges Bewegungsprofil: Alter, Größe, Gewicht, Stoffwechsel-Referenzprofil.
- Ruheenergiebedarf und aktivitätsbezogene Energie pro Einheit.
- Sportart, Intensität, Minuten und bereits gezählte Workout-Schritte.
- Schrittberechnung mit Grundwert, Gehtempo und Schutz vor Doppelzählung.
- Aktivitätsgutschrift ins Wochenbudget wählbar: 0 / 50 / 100 %.
- Einstellbarer Energie-pro-Punkt-Faktor und Grundschrittwert.
- In Wochenstatistik und Tagebuch bleiben tägliche Punkte und Wochenpuffer getrennt.
- Backup-Dateien beinhalten jetzt auch Profil, Schrittwerte und Berechnungsregeln.
- Lebensmittel auf eigenständige FitRocket-Kategorien und Formel umgestellt.
- Automatisierte Tests der Punktformeln im APK-Build.

## 0.6.0 – Aktivitätskonto
- Trainingsprotokoll, Aktivitätspunkte, Bewegungsgutschrift und Wochenkonto.

## 0.5.0 – Lebensmittel & Nährwerte
- Erweiterter Lebensmittelkatalog und Nährwert-Punkteberechnung.

## 0.4.x – Cozy Oberfläche
- Freundliche Pastellkarten, Punkteskala und verbesserte Menüabstände.

## 0.3.x – Alltag
- Lebensmittelsuche, Tagebuch, Gewicht, Monatsauswertung und Backup.

## 0.1–0.2 – Grundversion
- Lokales Tagebuch, Food-Katalog und automatischer Android-APK-Build.
