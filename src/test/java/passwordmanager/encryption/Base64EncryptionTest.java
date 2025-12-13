package passwordmanager.encryption;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Base64EncryptionTest {

    @Test
    void testEncryptDecrypt() {
        Base64Encryption encryption = new Base64Encryption();
        String original = "MySecretPassword123!";

        String encrypted = encryption.encrypt(original);
        String decrypted = encryption.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void testEncryptNull() {
        Base64Encryption encryption = new Base64Encryption();
        // Вместо исключения возвращает null
        String result = encryption.encrypt(null);
        assertNull(result);
    }

    @Test
    void testDecryptNull() {
        Base64Encryption encryption = new Base64Encryption();
        // Вместо исключения возвращает null
        String result = encryption.decrypt(null);
        assertNull(result);
    }

    @Test
    void testDecryptInvalidBase64() {
        Base64Encryption encryption = new Base64Encryption();
        // Некорректный Base64
        String result = encryption.decrypt("This is not base64!");
        assertNull(result);
    }

    @Test
    void testSpecialCharacters() {
        Base64Encryption encryption = new Base64Encryption();
        String original = "Пароль с русскими символами! @#$%^&*()";

        String encrypted = encryption.encrypt(original);
        String decrypted = encryption.decrypt(encrypted);

        assertEquals(original, decrypted);
    }

    @Test
    void testEmptyString() {
        Base64Encryption encryption = new Base64Encryption();
        String original = "";

        String encrypted = encryption.encrypt(original);
        String decrypted = encryption.decrypt(encrypted);

        assertEquals(original, decrypted);
    }
}