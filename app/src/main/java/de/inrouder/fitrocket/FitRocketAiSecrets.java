package de.inrouder.fitrocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Encrypts the personally supplied API key using Android Keystore; never exported. */
public final class FitRocketAiSecrets {
    private static final String ALIAS="de.inrouder.fitrocket.ai_byok.v1";
    private static final String PREF="fitrocket_ai_private";
    private FitRocketAiSecrets(){}

    private static SecretKey secretKey() throws Exception {
        KeyStore store=KeyStore.getInstance("AndroidKeyStore");store.load(null);
        if(store.containsAlias(ALIAS))return (SecretKey)store.getKey(ALIAS,null);
        KeyGenerator generator=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS,
            KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build());
        return generator.generateKey();
    }

    public static void set(Context context,String apiKey) throws Exception{
        if(apiKey==null||!apiKey.trim().startsWith("sk-")||apiKey.length()>500)
            throw new IllegalArgumentException("Ein OpenAI-API-Schlüssel beginnt üblicherweise mit sk-.");
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,secretKey());
        byte[] output=cipher.doFinal(apiKey.trim().getBytes(StandardCharsets.UTF_8));
        SharedPreferences.Editor edit=context.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit();
        edit.putString("cipher",Base64.encodeToString(output,Base64.NO_WRAP));
        edit.putString("iv",Base64.encodeToString(cipher.getIV(),Base64.NO_WRAP));
        if(!edit.commit())throw new IllegalStateException("Schlüssel konnte nicht gespeichert werden.");
    }

    public static String get(Context context) throws Exception{
        SharedPreferences preferences=context.getSharedPreferences(PREF,Context.MODE_PRIVATE);
        String cipherData=preferences.getString("cipher",null),iv=preferences.getString("iv",null);
        if(cipherData==null||iv==null)return null;
        byte[] encrypted=Base64.decode(cipherData,Base64.DEFAULT);
        byte[] nonce=Base64.decode(iv,Base64.DEFAULT);
        if(nonce.length!=12||encrypted.length<17)throw new IllegalStateException("Schlüssel beschädigt");
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,secretKey(),new GCMParameterSpec(128,nonce));
        return new String(cipher.doFinal(encrypted),StandardCharsets.UTF_8);
    }

    public static boolean configured(Context context){
        return context.getSharedPreferences(PREF,Context.MODE_PRIVATE).contains("cipher");
    }

    public static void clear(Context context){
        context.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().clear().commit();
        // Key material remains in system Keystore but is useless without the encrypted ciphertext.
    }
}
