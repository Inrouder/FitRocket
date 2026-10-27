plugins { id("com.android.application") }
val fitrocketSigningPath = System.getenv("FITROCKET_KEYSTORE_PATH")?.takeIf { it.isNotBlank() }
val fitrocketSigningStorePassword = System.getenv("FITROCKET_STORE_PASSWORD")?.takeIf { it.isNotBlank() }
val fitrocketSigningAlias = System.getenv("FITROCKET_KEY_ALIAS")?.takeIf { it.isNotBlank() }
val fitrocketSigningKeyPassword = System.getenv("FITROCKET_KEY_PASSWORD")?.takeIf { it.isNotBlank() }
val fitrocketReleaseSigningEnabled = listOf(
 fitrocketSigningPath, fitrocketSigningStorePassword,
 fitrocketSigningAlias, fitrocketSigningKeyPassword
).all { it != null }

android {
 namespace = "de.inrouder.fitrocket"
 compileSdk = 35
 defaultConfig { applicationId = "de.inrouder.fitrocket"; minSdk = 26; targetSdk = 35; versionCode = 18; versionName = "0.13.0" }
 signingConfigs {
  create("fitrocketRelease") {
   if (fitrocketReleaseSigningEnabled) {
    storeFile = file(fitrocketSigningPath!!)
    storePassword = fitrocketSigningStorePassword
    keyAlias = fitrocketSigningAlias
    keyPassword = fitrocketSigningKeyPassword
   }
  }
 }
 buildTypes {
  getByName("release") {
   isMinifyEnabled = false
   if (fitrocketReleaseSigningEnabled)
    signingConfig = signingConfigs.getByName("fitrocketRelease")
  }
 }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {
 implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
 testImplementation("junit:junit:4.13.2")
 testImplementation("org.json:json:20240303")
}
