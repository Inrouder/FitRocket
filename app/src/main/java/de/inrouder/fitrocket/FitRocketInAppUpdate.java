package de.inrouder.fitrocket;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * User-initiated, in-app GitHub APK updater. Android retains final install consent.
 * No silent installation, root, or device-admin privileges.
 */
public final class FitRocketInAppUpdate {
    private static final int MAX_APK_BYTES=80*1024*1024;
    private static final String FILE_NAME="FitRocket-update.apk";
    private static final String AUTHORITY="de.inrouder.fitrocket.updateprovider";
    private FitRocketInAppUpdate(){}

    public interface Callback {
        void onProgress(int percent);
        void onReady(File apk);
        void onFailure(String error);
    }

    public static File updateFile(Activity activity) {
        return new File(new File(activity.getCacheDir(),"updates"),FILE_NAME);
    }

    public static boolean allowedDownloadUrl(String address) {
        return address!=null && address.matches(
            "^https://github\\.com/Inrouder/FitRocket/releases/download/v[0-9]+\\.[0-9]+\\.[0-9]+/FitRocket\\.apk$");
    }

    private static boolean trustedRedirect(URL url){
        String host=url.getHost().toLowerCase(java.util.Locale.ROOT);
        return "https".equalsIgnoreCase(url.getProtocol()) &&
            ("github.com".equals(host)
             || "release-assets.githubusercontent.com".equals(host)
             || "objects.githubusercontent.com".equals(host));
    }

    private static HttpURLConnection openDownload(String address) throws Exception {
        URL url=new URL(address);
        for(int redirects=0;redirects<=5;redirects++){
            if(!trustedRedirect(url))throw new SecurityException("Unsichere Downloadadresse.");
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(15000);conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent","FitRocket-InAppUpdater/1.0");
            conn.setRequestProperty("Accept","application/octet-stream");
            int status=conn.getResponseCode();
            if(status==301||status==302||status==303||status==307||status==308){
                String target=conn.getHeaderField("Location");
                URL next=target==null?null:new URL(url,target);
                conn.disconnect();
                if(next==null||!trustedRedirect(next))throw new SecurityException("Unbekannte Weiterleitung.");
                url=next;
                continue;
            }
            if(status!=200){
                conn.disconnect();throw new IllegalStateException("APK nicht erreichbar (HTTP "+status+").");
            }
            return conn;
        }
        throw new IllegalStateException("Zu viele Weiterleitungen.");
    }

    private static Signature[] signatures(PackageInfo info){
        if(info==null)return null;
        if(Build.VERSION.SDK_INT>=28){
            if(info.signingInfo==null)return null;
            return info.signingInfo.getApkContentsSigners();
        }
        return info.signatures;
    }

    private static boolean sameSigners(Signature[] installed,Signature[] archive){
        if(installed==null||archive==null||installed.length==0||installed.length!=archive.length)
            return false;
        // Order-independent certificate comparison; certificate content is not private.
        for(Signature candidate:installed){
            boolean found=false;
            for(Signature other:archive)if(candidate.equals(other)){found=true;break;}
            if(!found)return false;
        }
        return true;
    }

    public static void verifyUpdate(Activity activity,File apk) throws Exception {
        if(!apk.isFile()||apk.length()<4096||apk.length()>MAX_APK_BYTES)
            throw new IllegalArgumentException("Die heruntergeladene APK ist unvollständig.");
        PackageManager pm=activity.getPackageManager();
        int flags=Build.VERSION.SDK_INT>=28
          ?PackageManager.GET_SIGNING_CERTIFICATES:PackageManager.GET_SIGNATURES;
        PackageInfo archive=pm.getPackageArchiveInfo(apk.getAbsolutePath(),flags);
        PackageInfo current=pm.getPackageInfo(activity.getPackageName(),flags);
        if(archive==null||!activity.getPackageName().equals(archive.packageName))
            throw new SecurityException("Die APK gehört nicht zu FitRocket.");
        long installed=Build.VERSION.SDK_INT>=28?current.getLongVersionCode():current.versionCode;
        long candidate=Build.VERSION.SDK_INT>=28?archive.getLongVersionCode():archive.versionCode;
        if(candidate<=installed)throw new IllegalArgumentException(
            "Diese APK ist nicht neuer als die installierte Version.");
        if(!sameSigners(signatures(current),signatures(archive)))
            throw new SecurityException("Die App-Signatur stimmt nicht überein. "
                +"Installiere diese APK nicht; überprüfe die GitHub-Releasequelle.");
    }

    public static void download(Activity activity,FitRocketUpdate.Release release,Callback callback){
        if(!allowedDownloadUrl(release.apkUrl)){
            callback.onFailure("Kein vertrauenswürdiger FitRocket-APK-Download verfügbar.");return;
        }
        new Thread(()->{
            File file=updateFile(activity);
            File temporary=new File(file.getParentFile(),"FitRocket-update.part");
            HttpURLConnection conn=null;
            try{
                if(!file.getParentFile().exists()&&!file.getParentFile().mkdirs())
                    throw new IllegalStateException("Update-Ordner konnte nicht erstellt werden.");
                if(temporary.exists()&&!temporary.delete())
                    throw new IllegalStateException("Temporäre APK konnte nicht gelöscht werden.");
                conn=openDownload(release.apkUrl);
                long length=conn.getContentLengthLong();
                if(length>MAX_APK_BYTES)throw new IllegalArgumentException("APK-Datei zu groß.");
                byte[] buffer=new byte[16*1024];
                long received=0;int n,lastProgress=-1;
                try(InputStream in=conn.getInputStream();
                    FileOutputStream out=new FileOutputStream(temporary)){
                    while((n=in.read(buffer))!=-1){
                        received+=n;
                        if(received>MAX_APK_BYTES)throw new IllegalArgumentException("APK-Datei zu groß.");
                        out.write(buffer,0,n);
                        int progress=length>0?(int)Math.min(99,(received*100)/length):-1;
                        if(progress!=lastProgress){
                            lastProgress=progress;
                            final int value=progress;
                            activity.runOnUiThread(()->callback.onProgress(value));
                        }
                    }
                    out.getFD().sync();
                }
                if(length>0 && received!=length)throw new IllegalStateException("APK-Download unterbrochen.");
                if(received<4096)throw new IllegalStateException("APK-Download unvollständig.");
                verifyUpdate(activity,temporary);
                if(file.exists()&&!file.delete())
                    throw new IllegalStateException("Alte Update-Datei konnte nicht gelöscht werden.");
                if(!temporary.renameTo(file))
                    throw new IllegalStateException("Update-Datei konnte nicht bereitgestellt werden.");
                activity.runOnUiThread(()->callback.onReady(file));
            }catch(Exception ex){
                temporary.delete();
                String error=ex.getMessage()==null?"APK-Download fehlgeschlagen.":ex.getMessage();
                activity.runOnUiThread(()->callback.onFailure(error));
            }finally{if(conn!=null)conn.disconnect();}
        },"FitRocket-APK-Downloader").start();
    }

    public static boolean hasInstallerPermission(Activity activity){
        return Build.VERSION.SDK_INT<26||activity.getPackageManager().canRequestPackageInstalls();
    }

    public static void openPermissionSettings(Activity activity){
        if(Build.VERSION.SDK_INT<26)return;
        Intent intent=new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:"+activity.getPackageName()));
        activity.startActivity(intent);
    }

    public static void launchInstaller(Activity activity,File apk) throws Exception{
        verifyUpdate(activity,apk);
        Uri uri=FileProvider.getUriForFile(activity,AUTHORITY,apk);
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri,"application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
}
