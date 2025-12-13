package passwordmanager.exception;

/**
 * Исключение при пустых полях
 */
public class EmptyFieldException extends Exception {
    public EmptyFieldException(String message) {
        super(message);
    }
}