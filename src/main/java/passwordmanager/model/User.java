package passwordmanager.model;

/**
 * Класс для хранения данных пользователя приложения.
 */
public class User {
    private final String username;
    private String encryptedPassword;

    /**
     * Конструктор пользователя.
     *
     * @param username логин пользователя
     * @param encryptedPassword зашифрованный пароль
     */
    public User(String username, String encryptedPassword) {
        this.username = username;
        this.encryptedPassword = encryptedPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    @Override
    public String toString() {
        return "Пользователь: " + username;
    }
}