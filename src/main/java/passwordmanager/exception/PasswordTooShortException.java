package passwordmanager.exception;

/**
 * Исключение при слишком коротком пароле
 */
public class PasswordTooShortException extends Exception {
    public PasswordTooShortException(String message) {
        super(message);
    }
}