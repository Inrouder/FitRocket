package de.inrouder.fitrocket;
public final class FitRocketNavigation {
 private FitRocketNavigation(){}
 public static final String START_SCREEN="Tagebuch";
 public static final String[] TABS={"🚀\nTagebuch","🔎\nSuche","👩‍🍳\nKochbuch","📊\nFortschritt","👤\nProfil"};
 public static int selectedTab(String heading){
  if(heading==null||"Tagebuch".equals(heading))return 0;
  if("Kochbuch".equals(heading)||"Rezepte".equals(heading)||"KI-Assistent".equals(heading)||"KI-Vorschau".equals(heading))return 2;
  if("Lebensmittel".equals(heading)||"Favoriten".equals(heading)||"Eigenes Lebensmittel".equals(heading))return 1;
  if("Fortschritt".equals(heading)||"Gewicht".equals(heading)||"Statistiken".equals(heading)||"Aktivität".equals(heading)||"Trinken".equals(heading))return 3;
  return 4;
 }
}
