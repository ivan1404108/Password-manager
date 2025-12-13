package passwordmanager.encryption;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SaltedEncryptionTest {

    @Test
    void testSaltedEncryption() {
        SaltedEncryption encryption = new SaltedEncryption();
        String original = "MyPassword123";

        String encrypted = encryption.encrypt(original);
        String decrypted = encryption.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void testSamePasswordDifferentEncryption() {
        SaltedEncryption encryption = new SaltedEncryption();
        String password = "test";

        String encrypted1 = encryption.encrypt(password);
        String encrypted2 = encryption.encrypt(password);

        // Соль должна создавать разные результаты
        assertNotEquals(encrypted1, encrypted2);

        // Но оба должны расшифровываться обратно
        assertEquals(password, encryption.decrypt(encrypted1));
        assertEquals(password, encryption.decrypt(encrypted2));
    }

    @Test
    void testEmptyString() {
        SaltedEncryption encryption = new SaltedEncryption();
        String original = "";

        String encrypted = encryption.encrypt(original);
        String decrypted = encryption.decrypt(encrypted);

        assertEquals(original, decrypted);
    }
}