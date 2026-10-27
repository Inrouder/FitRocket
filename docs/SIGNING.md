# FitRocket dauerhaft signieren (Android-Updates)

**Stand: FitRocket 0.13.0.** Diese Anleitung richtet sich an die private Verteilung von FitRocket als APK über GitHub Releases, nicht an Google Play App Signing.

## Wichtig vor dem Einrichten

Android akzeptiert ein Update derselben App-ID `de.inrouder.fitrocket` nur, wenn die neue APK mit **derselben langfristigen Signatur** unterzeichnet wurde und `versionCode` höher ist. Außerdem muss das Paket denselben `applicationId` behalten.

Die bisherigen FitRocket-Versionen bis v0.12.1 wurden als **Debug-APKs** mit kurzlebigen CI-Debug-Schlüsseln verteilt. Der neue persönliche Release-Key stimmt nicht mit diesen Zertifikaten überein.

**Darum ist beim ersten Wechsel auf eine mit deinem neuen Key signierte FitRocket.apk wahrscheinlich ein EINMALIGER Neuinstallationsschritt erforderlich:**

1. In der bisherigen App **Profil → Daten & Sicherung → Backup exportieren** ausführen und die JSON-Datei außerhalb des Handys sichern.
2. Prüfen, dass das Backup wirklich existiert (Dateigröße > 0).
3. Nach Einrichtung des GitHub-Keystores eine offiziell signierte FitRocket-APK (v0.13.0 oder neuer) herunterladen.
4. Bisherige Debug-App **erst nach erfolgreicher Sicherung** deinstallieren.
5. Die neue signierte App installieren.
6. Backup in FitRocket importieren; Gemini-Schlüssel gegebenenfalls erneut in der App hinterlegen, denn API-Schlüssel sind absichtlich **nicht im JSON-Backup**.
7. Alle künftigen GitHub Releases werden mit **dem gleichen Keystore** signiert und können ohne Deinstallation aktualisiert werden.

**Den privaten Keystore, die Passwörter und den privaten Key niemals in GitHub-Code, in den Chat oder in ein geteiltes Archiv hochladen.** Ein eigenes sicheres Offline-Backup des Keystores anlegen. Wird der App-Signaturschlüssel verloren, kann ein direkter APK-Updatepfad nicht ohne Weiteres fortgesetzt werden.

## 1. Keystore auf deinem Windows-PC erzeugen

Voraussetzung: Java JDK mit `keytool` (zum Beispiel das JDK von Android Studio).

In **PowerShell** im gewünschten privaten Sicherungsordner:

```powershell
keytool -genkeypair -v -storetype PKCS12 -keystore FitRocket-release.jks -alias fitrocket -keyalg RSA -keysize 3072 -validity 12000
```

Bei Abfrage ein starkes Passwort vergeben und sicher im Passwortmanager aufbewahren. Bei PKCS12 ist das Key-Passwort normalerweise identisch mit dem Keystore-Passwort. Das Zertifikat ist ca. 32 Jahre gültig.

Danach die **SHA-256-Zertifikatskennung** (öffentlich und ungefährlich, anders als der private Schlüssel) zur späteren Kontrolle notieren:

```powershell
keytool -list -v -keystore .\FitRocket-release.jks -alias fitrocket
```

**Zwei getrennte sichere Sicherungen** der `FitRocket-release.jks` anfertigen, zum Beispiel verschlüsselte externe Datenträger oder verschlüsselter Tresor, unabhängig von GitHub. Auch die Passwörter sichern.

## 2. Keystore als Base64 in eine GitHub-Actions-Secret umwandeln

Windows PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes((Resolve-Path ".\FitRocket-release.jks"))) | Set-Clipboard
```

Der Base64-Wert enthält das vollständige **private** Keystore-Material. Gib ihn **nur** unter `Settings → Secrets and variables → Actions` im eigenen GitHub-Repository als Secret ein. Nicht in den Chat, kein Git-Commit.

Öffne https://github.com/Inrouder/FitRocket/settings/secrets/actions und lege genau diese vier **Repository secrets** an:

| GitHub Secret | Inhalt |
|---|---|
| `FITROCKET_KEYSTORE_B64` | Die kopierte Base64-Zeichenfolge der Datei `FitRocket-release.jks` |
| `FITROCKET_STORE_PASSWORD` | Das Keystore-Passwort |
| `FITROCKET_KEY_ALIAS` | `fitrocket` |
| `FITROCKET_KEY_PASSWORD` | Das Passwort des Schlüssels (bei PKCS12 meist identisch) |

Die Secrets **nicht** als GitHub Variables eintragen. Achte auf die exakte Schreibweise.

Nach dem Einfügen den sensiblen Zwischenablage-Inhalt auf dem PC durch harmlosen Text ersetzen.

## 3. Signierten GitHub-Build auslösen

1. Öffne https://github.com/Inrouder/FitRocket/actions/workflows/android-apk.yml
2. Wähle **Run workflow → main → Run workflow**.
3. Die Pipeline prüft die Katalogdaten, führt Tests aus und erstellt eine Debug-Test-APK.
4. Sind **alle vier Secrets** vorhanden, dekodiert der GitHub-Runner den Keystore nur temporär, erstellt über Gradle `assembleRelease` eine signierte Release-APK und verifiziert deren Signatur mit `apksigner`.
5. Erst **danach** veröffentlicht der Workflow `FitRocket.apk` im GitHub Release `v0.13.0`. Die öffentliche Downloadseite verlinkt jeweils das aktuellste Release.
6. Wenn die Secrets vollständig **fehlen**, wird nur eine Debug-Test-APK als Actions-Artefakt bereitgestellt – **kein** stabiler Download. Sind sie unvollständig, schlägt der Workflow bewusst fehl.

Die Geheimnisse werden nicht in GitHub Actions ausgegeben und der temporäre Keystore wird zum Abschluss des Jobs gelöscht. Ungeachtet dessen bleiben Repository-Rechte, Workflow-Code-Reviews und der Schutz der GitHub-Accounts wichtig.

## 4. Jede spätere Version aktualisieren

Bei jeder neuen App-Version müssen in `app/build.gradle.kts` beide Werte angehoben werden:

- `versionCode` streng größer als vorher, zum Beispiel 19 statt 18.
- `versionName` die neue Version, zum Beispiel `0.13.1`.

Außerdem vor dem Build die in `.github/workflows/android-apk.yml` verwendeten **Artefaktnamen** und die `docs/RELEASE_NOTES.md` auf die neue Version anpassen. Künftig kann das automatisiert werden.

**Nie den Signatur-Keystore austauschen.** Der Signaturschlüssel muss für Updates identisch bleiben. Ein anderes Passwort bei unverändertem Schlüssel ist in Ordnung; ein anderer Schlüssel nicht.

Der Update-Checker in FitRocket führt zur aktuellen GitHub Release-Seite, von der die neue APK installiert werden kann. Android verlangt gegebenenfalls eine Zustimmung zur Installation aus externen Quellen. Die App installiert Updates nicht ohne Einwilligung und kann die Android-Sicherheitsprüfung nicht umgehen.

Offizielle Referenz: https://developer.android.com/studio/publish/app-signing
