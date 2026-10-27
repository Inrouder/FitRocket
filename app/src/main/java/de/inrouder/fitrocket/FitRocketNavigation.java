package de.inrouder.fitrocket;

/**
 * Main navigation: the meal diary replaces the former separate "Heute" screen.
 * Kept independent of Activity to make the chosen start page testable.
 */
public final class FitRocketNavigation {
    private FitRocketNavigation() {}

    public static final String START_SCREEN="Tagebuch";

    public static final String[] TABS={
        "▣\nTagebuch", "⌕\nSuche", "▤\nFortschritt", "◉\nProfil"
    };

    public static int selectedTab(String heading) {
        if (heading==null || "Tagebuch".equals(heading)) return 0;
        if ("Lebensmittel".equals(heading) || "Rezepte".equals(heading)
            || "Favoriten".equals(heading) || "Eigenes Lebensmittel".equals(heading)
            || "KI-Assistent".equals(heading) || "KI-Vorschau".equals(heading))
            return 1;
        if ("Fortschritt".equals(heading) || "Gewicht".equals(heading)
            || "Statistiken".equals(heading) || "Aktivität".equals(heading)
            || "Trinken".equals(heading))
            return 2;
        return 3;
    }
}
