package passwordmanager.exception;

/**
 * Исключение при попытке создать существующего пользователя
 */
public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}