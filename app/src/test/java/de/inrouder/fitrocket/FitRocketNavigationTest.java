package de.inrouder.fitrocket;

import org.junit.Test;
import static org.junit.Assert.*;

/** Ensures diary is the one and only startup/home tab. */
public class FitRocketNavigationTest {
    @Test public void diaryIsTheDefaultStartScreen() {
        assertEquals("Tagebuch", FitRocketNavigation.START_SCREEN);
        assertEquals(0, FitRocketNavigation.selectedTab("Tagebuch"));
        assertEquals(0, FitRocketNavigation.selectedTab(null));
        assertEquals(0, FitRocketNavigation.selectedTab("Intervallfasten"));
    }

    @Test public void thereAreSixTabsIncludingCookbookAndPet() {
        assertEquals(6, FitRocketNavigation.TABS.length);
        assertEquals(4, FitRocketNavigation.selectedTab("Pet"));
        assertTrue(FitRocketNavigation.TABS[0].contains("Tagebuch"));
        for (String tab : FitRocketNavigation.TABS) {
            assertFalse(tab.contains("Heute"));
        }
    }

    @Test public void compactNavigationKeepsAllSixLabelsReachable() {
        assertEquals(3,FitRocketNavigation.columns(360,1));
        assertEquals(3,FitRocketNavigation.columns(411,1.3f));
        assertEquals(2,FitRocketNavigation.columns(320,2));
        assertEquals(6,FitRocketNavigation.columns(600,1));
    }

    @Test public void foodAndAiStillHighlightSearchTab() {
        assertEquals(1, FitRocketNavigation.selectedTab("Lebensmittel"));
        assertEquals(2, FitRocketNavigation.selectedTab("Rezepte"));
        assertEquals(2, FitRocketNavigation.selectedTab("Kochbuch"));
        assertEquals(2, FitRocketNavigation.selectedTab("KI-Assistent"));
        assertEquals(1, FitRocketNavigation.selectedTab("Favoriten"));
        assertEquals(1, FitRocketNavigation.selectedTab("Eigenes Lebensmittel"));
    }

    @Test public void progressAndProfileKeepTheirOwnTabs() {
        assertEquals(3, FitRocketNavigation.selectedTab("Fortschritt"));
        assertEquals(3, FitRocketNavigation.selectedTab("Gewicht"));
        assertEquals(3, FitRocketNavigation.selectedTab("Aktivität"));
        assertEquals(5, FitRocketNavigation.selectedTab("Profil"));
    }
}
