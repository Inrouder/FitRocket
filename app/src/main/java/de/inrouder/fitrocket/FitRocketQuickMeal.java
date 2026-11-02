package de.inrouder.fitrocket;

import org.json.JSONObject;

/** A one-off diary booking with user-supplied points; never alters catalog ZeroPoints. */
public final class FitRocketQuickMeal {
    private FitRocketQuickMeal() {}

    public static JSONObject create(String name,double pointsPerPortion,double portions,
                                    String date,String group) throws Exception {
        String title=name==null?"":name.trim();
        if(title.length()<2||title.length()>100)
            throw new IllegalArgumentException("Gerichtname: 2 bis 100 Zeichen.");
        if(!Double.isFinite(portions)||portions<=0||portions>100)
            throw new IllegalArgumentException("Portionen: mehr als 0, maximal 100.");
        double per=FitRocketPointLedger.validPoints(pointsPerPortion);
        double sum=FitRocketPointLedger.validPoints(per*portions);
        if(date==null||date.isEmpty()||group==null||group.isEmpty())
            throw new IllegalArgumentException("Datum und Tageszeit fehlen.");
        return new JSONObject().put("name",title)
            .put("points",sum)
            .put("pointsPerPortion",per)
            .put("portions",portions)
            .put("quantity",portions)
            .put("quantityUnit","Portionen")
            .put("foodId","")
            .put("quality","Spontan · manuell")
            .put("group",group)
            .put("date",date);
    }
}
