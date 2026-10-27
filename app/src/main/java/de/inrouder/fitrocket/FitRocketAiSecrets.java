package de.inrouder.fitrocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Android Keystore encrypted, provider-isolated BYOK credentials.
 * Backwards compatible with the v0.10.0 OpenAI "cipher"/"iv" entries.
 * Never copy either secret into backups or the normal app preferences.
 */
public final class FitRocketAiSecrets {
    public static final String GEMINI = "gemini";
    public static final String OPENAI = "openai";
    private static final String ALIAS = "de.inrouder.fitrocket.ai_byok.v1";
    private static final String PREF = "fitrocket_ai_private";

    private FitRocketAiSecrets() {}

    private static void checkProvider(String provider) {
        if (!GEMINI.equals(provider) && !OPENAI.equals(provider))
            throw new IllegalArgumentException("Unbekannter KI-Anbieter.");
    }

    private static String cipherKey(String provider) {
        checkProvider(provider);
        return GEMINI.equals(provider) ? "gemini_cipher" : "cipher";
    }

    private static String ivKey(String provider) {
        checkProvider(provider);
        return GEMINI.equals(provider) ? "gemini_iv" : "iv";
    }

    private static SecretKey secretKey() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        if (store.containsAlias(ALIAS)) return (SecretKey) store.getKey(ALIAS, null);
        KeyGenerator generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }

    /**
     * Local input sanity check ONLY. Gemini issued newer authorization keys since May 2026
     * which can include dots; do not enforce old "AIza" prefixes or a narrow regex.
     * Google validates authenticity when the API request is made.
     * Restrict input to printable ASCII without whitespace/control characters so that
     * it cannot inject HTTP header lines, but permit dots and auth-key punctuation.
     */
    public static String validateKey(String provider, String apiKey) {
        checkProvider(provider);
        String key = apiKey == null ? "" : apiKey.trim();
        if (GEMINI.equals(provider)) {
            if (key.length() < 16 || key.length() > 500 ||
                !key.matches("[A-Za-z0-9._~+=/\\\\-]+"))
                throw new IllegalArgumentException(
                    "Bitte den vollständigen Gemini-Schlüssel ohne Leerzeichen einfügen. "
                    + "Auch neuere Google-Schlüssel mit Punkten sind erlaubt.");
        } else if (!key.startsWith("sk-") || key.length() > 500 || key.length() < 8 ||
                   !key.matches("[A-Za-z0-9._~+=/\\\\-]+")) {
            throw new IllegalArgumentException("Bitte einen OpenAI-API-Schlüssel (sk-…) eintragen.");
        }
        return key;
    }

    public static void set(Context context, String provider, String apiKey) throws Exception {
        String key = validateKey(provider, apiKey);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey());
        byte[] encrypted = cipher.doFinal(key.getBytes(StandardCharsets.UTF_8));

        SharedPreferences.Editor editor = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit();
        editor.putString(cipherKey(provider), Base64.encodeToString(encrypted, Base64.NO_WRAP));
        editor.putString(ivKey(provider), Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP));
        if (!editor.commit()) throw new IllegalStateException("API-Schlüssel konnte nicht gespeichert werden.");
    }

    public static String get(Context context, String provider) throws Exception {
        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String ciphertext = prefs.getString(cipherKey(provider), null);
        String iv = prefs.getString(ivKey(provider), null);
        if (ciphertext == null || iv == null) return null;

        byte[] encrypted = Base64.decode(ciphertext, Base64.DEFAULT);
        byte[] nonce = Base64.decode(iv, Base64.DEFAULT);
        if (nonce.length != 12 || encrypted.length < 17)
            throw new IllegalStateException("Verschlüsselter API-Schlüssel unlesbar.");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), new GCMParameterSpec(128, nonce));
        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }

    public static boolean configured(Context context, String provider) {
        SharedPreferences prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return prefs.contains(cipherKey(provider)) && prefs.contains(ivKey(provider));
    }

    public static void clear(Context context, String provider) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit();
        editor.remove(cipherKey(provider)).remove(ivKey(provider)).commit();
    }

    // Compatibility for previous OpenAI API keys, imported app users do not lose access.
    public static void set(Context context, String apiKey) throws Exception {
        set(context, OPENAI, apiKey);
    }
    public static String get(Context context) throws Exception {
        return get(context, OPENAI);
    }
    public static boolean configured(Context context) {
        return configured(context, OPENAI);
    }
    public static void clear(Context context) {
        clear(context, OPENAI);
    }
}
