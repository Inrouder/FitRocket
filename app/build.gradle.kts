plugins { id("com.android.application") }
android {
 namespace = "de.inrouder.fitrocket"
 compileSdk = 35
 defaultConfig { applicationId = "de.inrouder.fitrocket"; minSdk = 26; targetSdk = 35; versionCode = 11; versionName = "0.9.0" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {
 implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
 testImplementation("junit:junit:4.13.2")
 testImplementation("org.json:json:20240303")
}
