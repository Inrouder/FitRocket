plugins { id("com.android.application") }
android {
 namespace = "de.inrouder.fitrocket"
 compileSdk = 35
 defaultConfig { applicationId = "de.inrouder.fitrocket"; minSdk = 26; targetSdk = 35; versionCode = 8; versionName = "0.7.0" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies { testImplementation("junit:junit:4.13.2") }
