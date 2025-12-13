package passwordmanager.model;

/**
 * Перечисление доступных методов шифрования
 */
public enum EncryptionType {
    PLAIN("Открытый текст"),           // Без шифрования
    BASE64("Base64 кодирование"),      // Base64 кодирование
    SALTED("Подсаливание"),            // С добавлением соли
    FEISTEL("Шифр Фейстеля");          // Шифр Фейстеля

    private final String description;

    EncryptionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}