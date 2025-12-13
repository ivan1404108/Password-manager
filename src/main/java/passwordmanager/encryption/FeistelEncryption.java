package passwordmanager.encryption;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Реализация шифра Фейстеля.
 */
public class FeistelEncryption implements EncryptionStrategy {
    private static final int DEFAULT_ROUNDS = 8;
    private static final String KEY = "FeistelKey2024!@#";
    private static final byte PADDING_BYTE = (byte) 0x80; // Используем 0x80 как маркер дополнения

    @Override
    public String encrypt(String data) {
        try {
            if (data == null) {
                System.out.println("Ошибка: данные для шифрования null");
                return null;
            }

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = KEY.getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = feistelEncrypt(dataBytes, keyBytes, DEFAULT_ROUNDS);

            return bytesToHex(encrypted);

        } catch (Exception e) {
            System.out.println("Ошибка Feistel шифрования: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String decrypt(String encryptedData) {
        try {
            if (encryptedData == null) {
                System.out.println("Ошибка: данные для дешифрования null");
                return null;
            }

            byte[] encryptedBytes = hexToBytes(encryptedData);
            byte[] keyBytes = KEY.getBytes(StandardCharsets.UTF_8);
            byte[] decrypted = feistelDecrypt(encryptedBytes, keyBytes, DEFAULT_ROUNDS);

            // Убираем дополнение
            int originalLength = decrypted.length;
            for (int i = decrypted.length - 1; i >= 0; i--) {
                if (decrypted[i] == PADDING_BYTE) {
                    originalLength = i;
                    break;
                }
            }

            byte[] result = new byte[originalLength];
            System.arraycopy(decrypted, 0, result, 0, originalLength);

            return new String(result, StandardCharsets.UTF_8);

        } catch (Exception e) {
            System.out.println("Ошибка Feistel дешифрования: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private byte[] feistelEncrypt(byte[] data, byte[] key, int rounds) {
        // Дополняем данные до четного размера с маркером
        boolean needsPadding = data.length % 2 != 0;
        byte[] paddedData;

        if (needsPadding) {
            paddedData = new byte[data.length + 1];
            System.arraycopy(data, 0, paddedData, 0, data.length);
            paddedData[data.length] = PADDING_BYTE;
        } else {
            paddedData = data.clone();
        }

        int half = paddedData.length / 2;
        byte[] left = new byte[half];
        byte[] right = new byte[half];

        System.arraycopy(paddedData, 0, left, 0, half);
        System.arraycopy(paddedData, half, right, 0, half);

        for (int i = 0; i < rounds; i++) {
            byte[] temp = right.clone();
            byte[] f = roundFunction(right, key, i);

            // Убедимся что f имеет достаточную длину
            if (f.length < half) {
                byte[] extendedF = new byte[half];
                for (int j = 0; j < half; j++) {
                    extendedF[j] = f[j % f.length];
                }
                f = extendedF;
            }

            // XOR left с результатом функции раунда
            for (int j = 0; j < half; j++) {
                right[j] = (byte) (left[j] ^ f[j]);
            }

            left = temp;
        }

        byte[] result = new byte[paddedData.length];
        System.arraycopy(left, 0, result, 0, half);
        System.arraycopy(right, 0, result, half, half);

        return result;
    }

    private byte[] feistelDecrypt(byte[] data, byte[] key, int rounds) {
        int half = data.length / 2;
        byte[] left = new byte[half];
        byte[] right = new byte[half];

        System.arraycopy(data, 0, left, 0, half);
        System.arraycopy(data, half, right, 0, half);

        for (int i = rounds - 1; i >= 0; i--) {
            byte[] temp = left.clone();
            byte[] f = roundFunction(left, key, i);

            // Убедимся что f имеет достаточную длину
            if (f.length < half) {
                byte[] extendedF = new byte[half];
                for (int j = 0; j < half; j++) {
                    extendedF[j] = f[j % f.length];
                }
                f = extendedF;
            }

            // XOR right с результатом функции раунда
            for (int j = 0; j < half; j++) {
                left[j] = (byte) (right[j] ^ f[j]);
            }

            right = temp;
        }

        byte[] result = new byte[data.length];
        System.arraycopy(left, 0, result, 0, half);
        System.arraycopy(right, 0, result, half, half);

        return result;
    }

    private byte[] roundFunction(byte[] data, byte[] key, int round) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            // Используем & 0xFF чтобы получить беззнаковое значение
            int dataByte = data[i] & 0xFF;
            int keyByte = key[(i + round) % key.length] & 0xFF;
            result[i] = (byte) ((dataByte + keyByte) & 0xFF);
        }
        return result;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b & 0xFF));
        }
        return hex.toString();
    }

    private byte[] hexToBytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Неверная hex строка: " + hex);
        }

        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}