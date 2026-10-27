package de.inrouder.fitrocket;

import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketInAppUpdateTest {
    @Test public void onlyOfficialReleaseApksAreAllowed(){
        assertTrue(FitRocketInAppUpdate.allowedDownloadUrl(
            "https://github.com/Inrouder/FitRocket/releases/download/v0.14.3/FitRocket.apk"));
        assertTrue(FitRocketInAppUpdate.allowedDownloadUrl(
            "https://github.com/Inrouder/FitRocket/releases/download/v10.2.33/FitRocket.apk"));
    }

    @Test public void noUntrustedOrArbitraryLinks(){
        assertFalse(FitRocketInAppUpdate.allowedDownloadUrl(
            "https://github.com.evil.example/Inrouder/FitRocket/releases/download/v0.14.3/FitRocket.apk"));
        assertFalse(FitRocketInAppUpdate.allowedDownloadUrl(
            "http://github.com/Inrouder/FitRocket/releases/download/v0.14.3/FitRocket.apk"));
        assertFalse(FitRocketInAppUpdate.allowedDownloadUrl(
            "https://github.com/SomeoneElse/FitRocket/releases/download/v0.14.3/FitRocket.apk"));
        assertFalse(FitRocketInAppUpdate.allowedDownloadUrl(
            "https://github.com/Inrouder/FitRocket/releases/download/v0.14.3/other.apk"));
        assertFalse(FitRocketInAppUpdate.allowedDownloadUrl(
            "https://github.com/Inrouder/FitRocket/releases/download/v0.14.3/FitRocket.apk?other=true"));
        assertFalse(FitRocketInAppUpdate.allowedDownloadUrl(null));
    }
}
