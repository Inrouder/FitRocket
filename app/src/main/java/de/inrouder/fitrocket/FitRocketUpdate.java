package de.inrouder.fitrocket;

import android.app.Activity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Safe, on-demand update discovery via a public GitHub Release. No silent APK install. */
public final class FitRocketUpdate {
    public static final String REPO="https://github.com/Inrouder/FitRocket";
    public static final String API="https://api.github.com/repos/Inrouder/FitRocket/releases/latest";
    private static final Pattern VERSION=Pattern.compile("^[vV]?(\\d+)\\.(\\d+)\\.(\\d+)(?:[-+].*)?$");

    private FitRocketUpdate(){}

    public static final class Release {
        public final String version,notes,apkUrl,pageUrl,date;
        public Release(String version,String notes,String apkUrl,String pageUrl,String date) {
            this.version=version;this.notes=notes;this.apkUrl=apkUrl;
            this.pageUrl=pageUrl;this.date=date;
        }
    }

    public interface Callback {
        void onSuccess(Release release);
        void onFailure(String message);
    }

    private static int[] version(String text) {
        Matcher m=VERSION.matcher(text==null?"":text.trim());
        if(!m.matches())throw new IllegalArgumentException("Ungültige Versionsnummer.");
        return new int[]{Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3))};
    }

    public static boolean newer(String candidate,String installed) {
        int[] a=version(candidate),b=version(installed);
        for(int i=0;i<3;i++){
            if(a[i]!=b[i])return a[i]>b[i];
        }
        return false;
    }

    private static String safeReleaseUrl(String url) {
        if(url==null)return "";
        if(url.startsWith(REPO+"/releases/download/") && url.endsWith(".apk"))return url;
        return "";
    }

    public static Release parse(JSONObject root) {
        if(root.optBoolean("draft",false)||root.optBoolean("prerelease",false))
            throw new IllegalArgumentException("Noch keine stabile Version verfügbar.");
        String tag=root.optString("tag_name","");
        version(tag);
        String html=root.optString("html_url","");
        if(!html.startsWith(REPO+"/releases/tag/"))
            throw new IllegalArgumentException("Ungültiger Release-Link.");
        String apk="";
        JSONArray assets=root.optJSONArray("assets");
        for(int i=0;assets!=null&&i<assets.length();i++){
            JSONObject item=assets.optJSONObject(i);
            if(item!=null){
                apk=safeReleaseUrl(item.optString("browser_download_url"));
                if(!apk.isEmpty())break;
            }
        }
        String notes=root.optString("body","").trim();
        if(notes.length()>1400)notes=notes.substring(0,1400)+"…";
        return new Release(tag,notes,apk,html,root.optString("published_at",""));
    }

    private static String read(InputStream source) throws Exception {
        try(InputStream in=source;ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] b=new byte[4096];int n;
            while((n=in.read(b))!=-1){
                out.write(b,0,n);
                if(out.size()>300000)throw new IllegalStateException("GitHub-Antwort zu groß.");
            }
            return new String(out.toByteArray(),StandardCharsets.UTF_8);
        }
    }

    public static void check(Activity activity,Callback callback) {
        new Thread(()->{
            HttpURLConnection connection=null;
            try{
                connection=(HttpURLConnection)new URL(API).openConnection();
                connection.setRequestProperty("Accept","application/vnd.github+json");
                connection.setRequestProperty("User-Agent","FitRocket-Android-Updater/1.0");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                int code=connection.getResponseCode();
                if(code==404)throw new IllegalStateException("Noch kein öffentliches GitHub-Release verfügbar.");
                if(code==403||code==429)throw new IllegalStateException("GitHub-Abfragelimit erreicht. Bitte später erneut versuchen.");
                if(code!=200)throw new IllegalStateException("GitHub nicht erreichbar (HTTP "+code+").");
                Release result=parse(new JSONObject(read(connection.getInputStream())));
                activity.runOnUiThread(()->callback.onSuccess(result));
            }catch(Exception e){
                String message=e.getMessage()==null?"Netzwerkverbindung nicht verfügbar.":e.getMessage();
                activity.runOnUiThread(()->callback.onFailure(message));
            }finally{if(connection!=null)connection.disconnect();}
        },"FitRocket-Update-Check").start();
    }
}
