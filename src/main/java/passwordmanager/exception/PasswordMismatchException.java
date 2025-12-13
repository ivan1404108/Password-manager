package passwordmanager.exception;

/**
 * Исключение при несовпадении паролей
 */
public class PasswordMismatchException extends Exception {
    public PasswordMismatchException(String message) {
        super(message);
    }
}
