package passwordmanager.encryption;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Реализация шифрования с добавлением "соли" (salt).
 * <p>
 * Соль - это случайные данные, добавляемые к паролю перед кодированием.
 * Используется Base64 кодирование с добавлением случайной соли.
 *
 * @version 1.0
 * @since 2024
 */
public class SaltedEncryption implements EncryptionStrategy {
    private static final int SALT_LENGTH = 16;
    private static final String DELIMITER = ":";
    private final SecureRandom random = new SecureRandom();

    /**
     * Шифрует данные с добавлением соли.
     * Формат: Base64(соль + данные)
     *
     * @param data исходные данные для шифрования
     * @return строка в формате "Base64(соль:данные)" или null в случае ошибки
     */
    @Override
    public String encrypt(String data) {
        try {
            if (data == null) {
                throw new IllegalArgumentException("Данные для шифрования не могут быть null");
            }

            // Генерируем случайную соль
            byte[] saltBytes = new byte[SALT_LENGTH];
            random.nextBytes(saltBytes);
            String salt = Base64.getEncoder().encodeToString(saltBytes);

            // Комбинируем соль и данные
            String combined = salt + DELIMITER + data;

            // Кодируем в Base64
            byte[] encodedBytes = Base64.getEncoder().encode(combined.getBytes(StandardCharsets.UTF_8));
            return new String(encodedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            System.out.println("Ошибка salted шифрования: " + e.getMessage());
            return null;
        }
    }

    /**
     * Дешифрует данные с солью.
     *
     * @param encryptedData зашифрованные данные в формате Base64
     * @return исходные данные или null в случае ошибки
     */
    @Override
    public String decrypt(String encryptedData) {
        try {
            if (encryptedData == null) {
                throw new IllegalArgumentException("Данные для дешифрования не могут быть null");
            }

            // Декодируем из Base64
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData.getBytes(StandardCharsets.UTF_8));
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);

            // Разделяем соль и данные
            String[] parts = decodedString.split(DELIMITER, 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Неверный формат зашифрованных данных");
            }

            // Возвращаем исходные данные (соль игнорируем)
            return parts[1];

        } catch (Exception e) {
            System.out.println("Ошибка salted дешифрования: " + e.getMessage());
            return null;
        }
    }
}