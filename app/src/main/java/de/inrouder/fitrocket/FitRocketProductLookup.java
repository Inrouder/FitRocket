package de.inrouder.fitrocket;

import android.app.Activity;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Public product lookup; no token, no uploads, and no nutrition values invented. */
public final class FitRocketProductLookup {
    private FitRocketProductLookup(){}

    public interface Callback {
        void onProduct(Product product);
        void onError(String message);
    }

    public static final class Product {
        public final String code,name,brand,quantity;
        public final Double kcal,saturated,sugar,protein;

        public Product(String code,String name,String brand,String quantity,Double kcal,Double saturated,Double sugar,Double protein){
            this.code=code;this.name=name;this.brand=brand;this.quantity=quantity;
            this.kcal=kcal;this.saturated=saturated;this.sugar=sugar;this.protein=protein;
        }

        public boolean complete(){
            return kcal!=null&&saturated!=null&&sugar!=null&&protein!=null;
        }

        public int points(double grams){
            if(!complete()||!Double.isFinite(grams)||grams<=0||grams>10000)throw new IllegalArgumentException("Nährwerte oder Menge");
            double ratio=grams/100.0;
            return FitRocketMath.foodPoints(kcal*ratio,saturated*ratio,sugar*ratio,protein*ratio);
        }

        public JSONObject toFood() throws Exception {
            if(!complete())throw new IllegalStateException("Nährwerte unvollständig");
            JSONObject n=new JSONObject();
            n.put("kcal",kcal);n.put("saturatedFat_g",saturated);n.put("sugar_g",sugar);
            n.put("protein_g",protein);n.put("per",100);
            JSONObject f=new JSONObject();f.put("id","SCAN"+code);
            f.put("barcode",code);f.put("name",name);
            f.put("alias",brand);f.put("category","Gescannte Produkte");
            f.put("portion","100 g (bei Getränken 100 ml)");
            f.put("points",JSONObject.NULL);f.put("status","nutrition");
            f.put("nutrition",n);f.put("estimatedPoints100",points(100));
            f.put("notes","Nährwerte aus Open Food Facts bzw. von dir ergänzt. Verpackung bitte prüfen.");
            f.put("source","Open Food Facts");
            return f;
        }
    }

    public static boolean validBarcode(String barcode){
        return barcode!=null && barcode.matches("[0-9]{8,14}");
    }

    private static String safe(String value){
        if(value==null)return "";
        String clean=value.trim().replaceAll("[\\p{Cntrl}]"," ");
        return clean.length()>140?clean.substring(0,140):clean;
    }

    private static Double nutrient(JSONObject n,String key){
        if(n==null||!n.has(key)||n.isNull(key))return null;
        try {
            Object raw=n.get(key);
            double value=raw instanceof Number?((Number)raw).doubleValue():Double.parseDouble(raw.toString());
            return Double.isFinite(value)&&value>=0&&value<=2000?value:null;
        }catch(Exception e){return null;}
    }

    public static Product parse(String barcode,JSONObject response) throws Exception{
        JSONObject product=response.optJSONObject("product");
        if(product==null)throw new IllegalArgumentException("Kein Produkt für diesen Barcode gefunden.");
        JSONObject n=product.optJSONObject("nutriments");
        Double kcal=nutrient(n,"energy-kcal_100g");
        if(kcal==null){
            Double kj=nutrient(n,"energy-kj_100g");
            if(kj!=null)kcal=kj/4.184;
        }
        String name=product.optString("product_name_de","");
        if(name.trim().isEmpty())name=product.optString("product_name","");
        if(name.trim().isEmpty())name="Produkt "+barcode;
        return new Product(barcode,safe(name),safe(product.optString("brands","")),
            safe(product.optString("quantity","")),kcal,
            nutrient(n,"saturated-fat_100g"),nutrient(n,"sugars_100g"),nutrient(n,"proteins_100g"));
    }

    public static void lookup(Activity activity,String barcode,Callback callback){
        if(!validBarcode(barcode)){callback.onError("Bitte einen EAN-/UPC-Barcode (8–14 Ziffern) eingeben.");return;}
        new Thread(()->{
            HttpURLConnection connection=null;
            try{
                URL url=new URL("https://world.openfoodfacts.org/api/v3/product/"+barcode+
                    "?fields=code,product_name,product_name_de,brands,quantity,nutriments&lc=de");
                connection=(HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);connection.setReadTimeout(8000);
                connection.setRequestProperty("Accept","application/json");
                connection.setRequestProperty("User-Agent","FitRocket/0.9.0 (https://github.com/Inrouder/FitRocket)");
                int status=connection.getResponseCode();
                if(status==404)throw new IllegalArgumentException("Produkt nicht in Open Food Facts gefunden.");
                if(status!=200)throw new IllegalStateException("Produktdienst antwortet mit HTTP "+status);
                byte[] data;
                try(InputStream in=connection.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
                    byte[] buf=new byte[4096];int n;
                    while((n=in.read(buf))>=0){
                        out.write(buf,0,n);
                        if(out.size()>512000)throw new IllegalStateException("Produktantwort zu groß");
                    }
                    data=out.toByteArray();
                }
                Product product=parse(barcode,new JSONObject(new String(data,StandardCharsets.UTF_8)));
                activity.runOnUiThread(()->callback.onProduct(product));
            }catch(Exception e){
                String reason=e.getMessage()==null?"Verbindungsfehler":e.getMessage();
                activity.runOnUiThread(()->callback.onError(reason));
            }finally{if(connection!=null)connection.disconnect();}
        },"FitRocket-BarcodeLookup").start();
    }
}
