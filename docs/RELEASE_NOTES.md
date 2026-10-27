# FitRocket v0.13.0 – Dauerhafte Updates und Raketen-Icon

### Adaptive Rakete
FitRocket besitzt ein neues, eigenständiges Android-App-Icon mit Rakete, Raketenantrieb und einem dezenten Ernährungsakzent.

### Langfristige Updatefähigkeit
FitRocket v0.13.0 ist die erste Version, die **ausschließlich signiert mit einem eigenen dauerhaften Keystore** als offizielles GitHub Release veröffentlicht wird. Die Buildpipeline testet zunächst die App, erstellt danach bei vorhandenen Secrets die signierte Release-APK und prüft sie vor dem Upload. Ohne Signaturschlüssel gibt es nur einen Debug-Test-Build.

### Einmaliger Übergang für bestehende Installationen
Ältere GitHub-APK-Veröffentlichungen nutzten Debug-Signaturen. Android akzeptiert Updates nur mit demselben Zertifikat. Deshalb sind beim Wechsel auf v0.13.0 ggf. ein Backup, die Deinstallation der alten App und danach Import des Backups notwendig. Ab dann gelten der neue Schlüssel und die feste Signatur für alle Updates.

### Einrichtung
Die Erstellung des privaten Keystores und die GitHub-Secrets sind ausführlich unter `docs/SIGNING.md` beschrieben. Halte den Key privat und sichere ihn mehrfach.

Die Funktionen aus v0.12.1 bleiben unverändert: Mahlzeiten, KI-Gerichte, Wasser, Gewicht, Fortschritt, Hell-/Dunkelmodus, Updates und Backup.
