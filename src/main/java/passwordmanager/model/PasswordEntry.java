package passwordmanager.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Класс для хранения записи о пароле.
 * Содержит информацию о сервисе, логине и пароле.
 *
 * @version 2.0
 * @since 2024
 */
public class PasswordEntry {
    private final StringProperty serviceName;
    private final StringProperty username;
    private final StringProperty password;
    private EncryptionType encryptionType;

    // Временные поля для сериализации (не использовать в JavaFX напрямую)
    private transient String tempServiceName;
    private transient String tempUsername;
    private transient String tempPassword;

    /**
     * Конструктор записи о пароле.
     *
     * @param serviceName название сервиса
     * @param username логин/email пользователя
     * @param password пароль для сервиса
     * @param encryptionType тип шифрования пароля
     */
    public PasswordEntry(String serviceName, String username, String password, EncryptionType encryptionType) {
        this.serviceName = new SimpleStringProperty(serviceName);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.encryptionType = encryptionType;

        // Сохраняем также в временные поля для сериализации
        this.tempServiceName = serviceName;
        this.tempUsername = username;
        this.tempPassword = password;
    }

    // Методы для JavaFX Property

    public StringProperty serviceNameProperty() {
        return serviceName;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    // Геттеры с проверкой

    public String getServiceName() {
        return serviceName.get();
    }

    public String getUsername() {
        return username.get();
    }

    public String getPassword() {
        return password.get();
    }

    public EncryptionType getEncryptionType() {
        return encryptionType;
    }

    // Сеттеры

    public void setServiceName(String serviceName) {
        this.serviceName.set(serviceName);
        this.tempServiceName = serviceName;
    }

    public void setUsername(String username) {
        this.username.set(username);
        this.tempUsername = username;
    }

    public void setPassword(String password) {
        this.password.set(password);
        this.tempPassword = password;
    }

    public void setEncryptionType(EncryptionType encryptionType) {
        this.encryptionType = encryptionType;
    }

    /**
     * Возвращает описание типа шифрования.
     * Используется для отображения в JavaFX TableView.
     *
     * @return описание типа шифрования
     */
    public String getEncryptionDescription() {
        return encryptionType != null ? encryptionType.getDescription() : "Неизвестно";
    }

    /**
     * Метод для инициализации свойств после десериализации.
     * Вызывается после загрузки из файла.
     */
    public void initializeProperties() {
        if (tempServiceName != null) {
            serviceName.set(tempServiceName);
        }
        if (tempUsername != null) {
            username.set(tempUsername);
        }
        if (tempPassword != null) {
            password.set(tempPassword);
        }
    }

    /**
     * Метод для подготовки к сериализации.
     * Вызывается перед сохранением в файл.
     */
    public void prepareForSerialization() {
        tempServiceName = serviceName.get();
        tempUsername = username.get();
        tempPassword = password.get();
    }

    /**
     * Возвращает строковое представление записи (пароль скрыт).
     *
     * @return строка в формате "Сервис: [название], Логин: [логин], Шифрование: [тип]"
     */
    @Override
    public String toString() {
        return "Сервис: " + getServiceName() +
                ", Логин: " + getUsername() +
                ", Шифрование: " + getEncryptionDescription();
    }

    /**
     * Возвращает полное строковое представление записи (включая пароль).
     * Используется только для отладки.
     *
     * @return полная строка с паролем
     */
    public String toStringWithPassword() {
        return "Сервис: " + getServiceName() +
                ", Логин: " + getUsername() +
                ", Пароль: " + getPassword() +
                ", Шифрование: " + getEncryptionDescription();
    }

    /**
     * Проверяет, содержит ли запись поисковый запрос.
     * Используется для фильтрации в GUI.
     *
     * @param query поисковый запрос
     * @return true если запись соответствует запросу
     */
    public boolean contains(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String lowerQuery = query.toLowerCase();
        return (getServiceName() != null && getServiceName().toLowerCase().contains(lowerQuery)) ||
                (getUsername() != null && getUsername().toLowerCase().contains(lowerQuery)) ||
                (getPassword() != null && getPassword().toLowerCase().contains(lowerQuery)) ||
                (getEncryptionDescription() != null && getEncryptionDescription().toLowerCase().contains(lowerQuery));
    }

    /**
     * Создает копию записи о пароле.
     *
     * @return новая копия PasswordEntry
     */
    public PasswordEntry copy() {
        return new PasswordEntry(getServiceName(), getUsername(), getPassword(), encryptionType);
    }

    /**
     * Проверяет, является ли запись валидной.
     *
     * @return true если запись валидна
     */
    public boolean isValid() {
        return getServiceName() != null && !getServiceName().trim().isEmpty() &&
                getUsername() != null && !getUsername().trim().isEmpty() &&
                getPassword() != null && !getPassword().trim().isEmpty() &&
                encryptionType != null;
    }

    /**
     * Сравнивает записи по названию сервиса.
     *
     * @param other другая запись для сравнения
     * @return результат сравнения
     */
    public int compareByService(PasswordEntry other) {
        if (getServiceName() == null && other.getServiceName() == null) return 0;
        if (getServiceName() == null) return -1;
        if (other.getServiceName() == null) return 1;
        return getServiceName().compareToIgnoreCase(other.getServiceName());
    }

    /**
     * Сравнивает записи по имени пользователя.
     *
     * @param other другая запись для сравнения
     * @return результат сравнения
     */
    public int compareByUsername(PasswordEntry other) {
        if (getUsername() == null && other.getUsername() == null) return 0;
        if (getUsername() == null) return -1;
        if (other.getUsername() == null) return 1;
        return getUsername().compareToIgnoreCase(other.getUsername());
    }

    /**
     * Сравнивает записи по типу шифрования.
     *
     * @param other другая запись для сравнения
     * @return результат сравнения
     */
    public int compareByEncryption(PasswordEntry other) {
        if (encryptionType == null && other.encryptionType == null) return 0;
        if (encryptionType == null) return -1;
        if (other.encryptionType == null) return 1;
        return encryptionType.getDescription().compareTo(other.encryptionType.getDescription());
    }
}