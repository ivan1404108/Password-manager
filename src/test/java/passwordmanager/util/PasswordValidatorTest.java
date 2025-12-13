package passwordmanager.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    @Test
    void testPasswordLength() {
        assertTrue(PasswordValidator.isLengthValid("12345678")); // Минимум
        assertTrue(PasswordValidator.isLengthValid("123456789")); // Больше минимума
        assertFalse(PasswordValidator.isLengthValid("1234567")); // Меньше
        assertFalse(PasswordValidator.isLengthValid("")); // Пустой
        assertFalse(PasswordValidator.isLengthValid(null)); // Null
    }

    @Test
    void testGetMinLength() {
        assertEquals(8, PasswordValidator.getMinLength());
    }

    @Test
    void testGetPasswordLength() {
        assertEquals(8, PasswordValidator.getPasswordLength("12345678"));
        assertEquals(0, PasswordValidator.getPasswordLength(""));
        assertEquals(0, PasswordValidator.getPasswordLength(null));
        assertEquals(12, PasswordValidator.getPasswordLength("longpassword"));
    }
}