//======= Шифрование текста (AES-256) =======

package clipboard.core;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.prefs.Preferences;

public class EncryptionManager {
    private static final String ALGORITHM = "AES";
    private static final String KEY_PREF = "encryption_key";
    private static final String ENC_PREFIX = "ENC:";

    private SecretKey secretKey;
    private boolean enabled;
    private final Preferences prefs;

    public EncryptionManager() {
        this.prefs = Preferences.userNodeForPackage(EncryptionManager.class);
        loadKey();
    }
    // Зашифровано
    public void enable() {
        if (secretKey == null) {
            generateKey();
        }
        enabled = true;
    }
    // Не зашифровано
    public void disable() {
        enabled = false;
    }
    // Статус шифрования
    public boolean isEnabled() {
        return enabled;
    }
    // Шифрование
    public String encrypt(String data) {
        if (!enabled || secretKey == null || data == null) return data;

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return ENC_PREFIX + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка шифрования", e);
        }
    }
    // Дешифрование
    public String decrypt(String data) {
        if (!enabled || secretKey == null || data == null || !data.startsWith(ENC_PREFIX)) {
            return data;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = Base64.getDecoder().decode(data.substring(ENC_PREFIX.length()));
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            return "🔒 [Зашифровано]";
        }
    }
    // Создание ключа шифрования
    private void generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(256);
            secretKey = keyGen.generateKey();

            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            prefs.put(KEY_PREF, encodedKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Не удалось создать ключ", e);
        }
    }
    // Ключ дешифрования
    private void loadKey() {
        String encodedKey = prefs.get(KEY_PREF, null);
        if (encodedKey != null) {
            try {
                byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
                secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
            } catch (Exception e) {
                // Невалидный ключ - игнорируем
            }
        }
    }
}