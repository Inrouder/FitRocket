package de.inrouder.fitrocket;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class FitRocketUpdateTest {
    @Test public void onlyOffersNewerVersions() {
        assertTrue(FitRocketUpdate.newer("v0.13.0","0.12.0"));
        assertTrue(FitRocketUpdate.newer("1.0.0","0.99.99"));
        assertFalse(FitRocketUpdate.newer("v0.12.0","0.12.0"));
        assertFalse(FitRocketUpdate.newer("v0.11.9","0.12.0"));
        assertFalse(FitRocketUpdate.newer("v0.12.0","0.12.1"));
    }

    @Test public void parsesLatestReleaseAndFindsApk() throws Exception {
        JSONObject root=new JSONObject().put("tag_name","v0.12.0")
            .put("html_url","https://github.com/Inrouder/FitRocket/releases/tag/v0.12.0")
            .put("body","Verbessertes Profil")
            .put("draft",false).put("prerelease",false)
            .put("assets",new JSONArray()
                .put(new JSONObject().put("browser_download_url",
                    "https://github.com/Inrouder/FitRocket/releases/download/v0.12.0/FitRocket-v0.12.0.apk")));
        FitRocketUpdate.Release release=FitRocketUpdate.parse(root);
        assertEquals("v0.12.0",release.version);
        assertEquals("Verbessertes Profil",release.notes);
        assertTrue(release.apkUrl.endsWith(".apk"));
    }

    @Test public void ignoresUntrustedAssetHost() throws Exception {
        JSONObject root=new JSONObject().put("tag_name","v0.12.0")
            .put("html_url","https://github.com/Inrouder/FitRocket/releases/tag/v0.12.0")
            .put("assets",new JSONArray().put(new JSONObject().put(
                "browser_download_url","https://untrusted.example/app.apk")));
        assertTrue(FitRocketUpdate.parse(root).apkUrl.isEmpty());
    }

    @Test(expected=IllegalArgumentException.class)
    public void refusesUntrustedReleaseLink() throws Exception {
        FitRocketUpdate.parse(new JSONObject().put("tag_name","v0.12.0")
            .put("html_url","https://phishing.example/release"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void refusesDraft() throws Exception {
        FitRocketUpdate.parse(new JSONObject().put("tag_name","v0.12.0")
            .put("html_url","https://github.com/Inrouder/FitRocket/releases/tag/v0.12.0")
            .put("draft",true));
    }

    @Test(expected=IllegalArgumentException.class)
    public void refusesInvalidVersion() {
        FitRocketUpdate.newer("foobar","0.12.0");
    }
}
