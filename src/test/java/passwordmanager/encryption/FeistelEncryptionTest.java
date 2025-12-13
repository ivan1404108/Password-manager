package passwordmanager.encryption;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FeistelEncryptionTest {

    @Test
    void testFeistelEncryptDecrypt() {
        FeistelEncryption encryption = new FeistelEncryption();
        String original = "TestPassword123";

        String encrypted = encryption.encrypt(original);
        String decrypted = encryption.decrypt(encrypted);

        assertNotNull(encrypted);
        assertTrue(encrypted.matches("^[0-9a-f]+$")); // Проверяем hex формат
        assertEquals(original, decrypted);
    }

    @Test
    void testDifferentPasswords() {
        FeistelEncryption encryption = new FeistelEncryption();

        String[] passwords = {
                "short",
                "verylongpasswordwithmanychars",
                "1234567890",
                "!@#$%^&*()",
                "пароль123"
        };

        for (String password : passwords) {
            String encrypted = encryption.encrypt(password);
            assertNotNull(encrypted, "Encryption failed for: " + password);

            String decrypted = encryption.decrypt(encrypted);
            assertEquals(password, decrypted, "Decryption failed for: " + password);
        }
    }

    @Test
    void testEmptyString() {
        FeistelEncryption encryption = new FeistelEncryption();
        String original = "";

        String encrypted = encryption.encrypt(original);
        String decrypted = encryption.decrypt(encrypted);

        assertEquals(original, decrypted);
    }

    @Test
    void testEncryptNull() {
        FeistelEncryption encryption = new FeistelEncryption();
        String result = encryption.encrypt(null);
        assertNull(result);
    }

    @Test
    void testDecryptNull() {
        FeistelEncryption encryption = new FeistelEncryption();
        String result = encryption.decrypt(null);
        assertNull(result);
    }

    @Test
    void testDecryptInvalidHex() {
        FeistelEncryption encryption = new FeistelEncryption();
        // Некорректный hex
        String result = encryption.decrypt("Not a hex string!");
        assertNull(result);
    }

    @Test
    void testHexFormat() {
        FeistelEncryption encryption = new FeistelEncryption();
        String original = "Test";

        String encrypted = encryption.encrypt(original);
        // Проверяем что это валидный hex
        assertTrue(encrypted.matches("^[0-9a-f]+$"));

        // Проверяем что длина четная (hex пары)
        assertEquals(0, encrypted.length() % 2);
    }
}