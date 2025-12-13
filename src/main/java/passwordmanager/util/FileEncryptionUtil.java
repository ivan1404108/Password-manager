package passwordmanager.util;

import passwordmanager.encryption.Base64Encryption;
import passwordmanager.encryption.EncryptionStrategy;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Утилита для шифрования/дешифрования файлов с использованием Base64
 *
 * <p>Предоставляет методы для работы с зашифрованными файлами.
 * Использует Base64 для кодирования/декодирования содержимого файлов.</p>
 *
 * @version 1.0
 * @since 2024
 * @see Base64Encryption
 */
public class FileEncryptionUtil {
    private static final EncryptionStrategy encryptor = new Base64Encryption();

    /**
     * Шифрует файл и сохраняет в новый файл
     *
     * @param inputFile исходный файл
     * @param outputFile зашифрованный файл
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static void encryptFile(File inputFile, File outputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String encryptedLine = encryptor.encrypt(line);
                if (encryptedLine != null) {
                    writer.write(encryptedLine);
                    writer.newLine();
                } else {
                    throw new IOException("Ошибка шифрования строки");
                }
            }
        }
    }

    /**
     * Дешифрует файл и сохраняет в новый файл
     *
     * @param inputFile зашифрованный файл
     * @param outputFile расшифрованный файл
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static void decryptFile(File inputFile, File outputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String decryptedLine = encryptor.decrypt(line);
                if (decryptedLine != null) {
                    writer.write(decryptedLine);
                    writer.newLine();
                } else {
                    throw new IOException("Ошибка дешифрования строки");
                }
            }
        }
    }

    /**
     * Шифрует строку и записывает в файл
     *
     * @param data данные для шифрования
     * @param outputFile файл для записи
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static void writeEncrypted(String data, File outputFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            String encrypted = encryptor.encrypt(data);
            if (encrypted != null) {
                writer.write(encrypted);
            } else {
                throw new IOException("Ошибка шифрования данных");
            }
        }
    }

    /**
     * Читает и дешифрует строку из файла
     *
     * @param inputFile файл с зашифрованными данными
     * @return расшифрованная строка или null в случае ошибки
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static String readEncrypted(File inputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8))) {
            String encrypted = reader.readLine();
            return encryptor.decrypt(encrypted);
        }
    }

    /**
     * Проверяет, зашифрован ли файл (простейшая проверка)
     *
     * <p>Проверяет, содержит ли первая строка файла только Base64 символы.</p>
     *
     * @param file файл для проверки
     * @return true если файл вероятно зашифрован, false в противном случае
     */
    public static boolean isFileEncrypted(File file) {
        if (!file.exists() || file.length() == 0) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();
            // Проверка на Base64 формат (может содержать A-Z, a-z, 0-9, +, /, =)
            return firstLine != null && firstLine.matches("^[A-Za-z0-9+/]+=*$");
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Шифрует весь файл как одну строку
     *
     * @param inputFile исходный файл
     * @param outputFile зашифрованный файл
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static void encryptFileAsSingleString(File inputFile, File outputFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFile);
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

            byte[] fileBytes = new byte[(int) inputFile.length()];
            int bytesRead = fis.read(fileBytes);

            if (bytesRead > 0) {
                String content = new String(fileBytes, 0, bytesRead, StandardCharsets.UTF_8);
                String encrypted = encryptor.encrypt(content);
                if (encrypted != null) {
                    writer.write(encrypted);
                } else {
                    throw new IOException("Ошибка шифрования файла");
                }
            }
        }
    }

    /**
     * Дешифрует файл, зашифрованный как одна строка
     *
     * @param inputFile зашифрованный файл
     * @param outputFile расшифрованный файл
     * @throws IOException если произошла ошибка ввода-вывода
     */
    public static void decryptFileFromSingleString(File inputFile, File outputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

            StringBuilder encryptedContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                encryptedContent.append(line);
            }

            String decrypted = encryptor.decrypt(encryptedContent.toString());
            if (decrypted != null) {
                writer.write(decrypted);
            } else {
                throw new IOException("Ошибка дешифрования файла");
            }
        }
    }
}