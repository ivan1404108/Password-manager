package passwordmanager.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordEntryTest {

    @Test
    void testPasswordEntryCreation() {
        PasswordEntry entry = new PasswordEntry(
                "Google",
                "user@gmail.com",
                "password123",
                EncryptionType.SALTED
        );

        assertEquals("Google", entry.getServiceName());
        assertEquals("user@gmail.com", entry.getUsername());
        assertEquals("password123", entry.getPassword());
        assertEquals(EncryptionType.SALTED, entry.getEncryptionType());
    }

    @Test
    void testSetters() {
        PasswordEntry entry = new PasswordEntry("", "", "", EncryptionType.PLAIN);

        entry.setServiceName("GitHub");
        entry.setUsername("dev");
        entry.setPassword("newpass");
        entry.setEncryptionType(EncryptionType.BASE64);

        assertEquals("GitHub", entry.getServiceName());
        assertEquals("dev", entry.getUsername());
        assertEquals("newpass", entry.getPassword());
        assertEquals(EncryptionType.BASE64, entry.getEncryptionType());
    }

    @Test
    void testIsValid() {
        PasswordEntry valid = new PasswordEntry("Service", "user", "pass", EncryptionType.SALTED);
        assertTrue(valid.isValid());

        PasswordEntry invalid1 = new PasswordEntry("", "user", "pass", EncryptionType.SALTED);
        assertFalse(invalid1.isValid());

        PasswordEntry invalid2 = new PasswordEntry("Service", "", "pass", EncryptionType.SALTED);
        assertFalse(invalid2.isValid());

        PasswordEntry invalid3 = new PasswordEntry("Service", "user", "", EncryptionType.SALTED);
        assertFalse(invalid3.isValid());
    }

    @Test
    void testContainsSearch() {
        PasswordEntry entry = new PasswordEntry("Google", "test@gmail.com", "mypass", EncryptionType.BASE64);

        assertTrue(entry.contains("goog")); // Поиск в service (case insensitive)
        assertTrue(entry.contains("GMAIL")); // Поиск в username
        assertTrue(entry.contains("base64")); // Поиск в encryption
        assertFalse(entry.contains("yahoo")); // Не должно найти
        assertTrue(entry.contains("")); // Пустой запрос возвращает true
    }
}