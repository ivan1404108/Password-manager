package passwordmanager.util;

/**
 * Утилита для проверки паролей
 */
public class PasswordValidator {
    private static final int MIN_LENGTH = 8;

    /**
     * Проверяет минимальную длину пароля
     */
    public static boolean isLengthValid(String password) {
        return password != null && password.length() >= MIN_LENGTH;
    }

    /**
     * Возвращает минимальную требуемую длину
     */
    public static int getMinLength() {
        return MIN_LENGTH;
    }

    /**
     * Возвращает текущую длину пароля
     */
    public static int getPasswordLength(String password) {
        return password == null ? 0 : password.length();
    }
}