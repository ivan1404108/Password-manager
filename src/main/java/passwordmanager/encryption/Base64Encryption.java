package passwordmanager.encryption;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Реализация шифрования с использованием алгоритма Base64.
 */
public class Base64Encryption implements EncryptionStrategy {

    @Override
    public String encrypt(String data) {
        try {
            if (data == null) {
                throw new IllegalArgumentException("Данные для шифрования не могут быть null");
            }
            // Используем UTF-8 кодировку
            return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.out.println("Ошибка Base64 шифрования: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String decrypt(String encryptedData) {
        try {
            if (encryptedData == null) {
                throw new IllegalArgumentException("Данные для дешифрования не могут быть null");
            }
            // Используем UTF-8 кодировку
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: Неверный формат Base64 данных");
            return null;
        } catch (Exception e) {
            System.out.println("Ошибка Base64 дешифрования: " + e.getMessage());
            return null;
        }
    }
}