package de.inrouder.fitrocket;
public final class FitRocketNavigation {
 private FitRocketNavigation(){}
 public static final String START_SCREEN="Tagebuch";
 public static final String[] TABS={"🚀\nTagebuch","🔎\nSuche","👩‍🍳\nKochbuch","📊\nFortschritt","🐾\nPet","👤\nProfil"};
 public static int selectedTab(String heading){
  if(heading==null||"Tagebuch".equals(heading)||"Intervallfasten".equals(heading))return 0;
  if("Kochbuch".equals(heading)||"Rezepte".equals(heading)||"KI-Assistent".equals(heading)||"KI-Vorschau".equals(heading))return 2;
  if("Lebensmittel".equals(heading)||"Favoriten".equals(heading)||"Eigenes Lebensmittel".equals(heading))return 1;
  if("Fortschritt".equals(heading)||"Gewicht".equals(heading)||"Statistiken".equals(heading)||"Aktivität".equals(heading)||"Trinken".equals(heading))return 3;
  if("Pet".equals(heading))return 4;
  return 5;
 }
 /** Keep all six labels visible; compact devices use rows instead of truncating the sixth tab. */
 public static int columns(int widthDp,float fontScale){
  float scale=Math.max(1,fontScale);
  if(widthDp-36>=6*80*scale)return 6;
  return widthDp-36>=3*68*scale?3:2;
 }
}
