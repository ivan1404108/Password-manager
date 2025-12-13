package passwordmanager.core;

import passwordmanager.encryption.EncryptionFactory;
import passwordmanager.encryption.EncryptionStrategy;
import passwordmanager.model.EncryptionType;
import passwordmanager.model.PasswordEntry;
import passwordmanager.util.FileEncryptionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


/**
 * Хранилище паролей для конкретного пользователя.
 * <p>
 * Класс отвечает за управление паролями отдельного пользователя, включая
 * их загрузку, сохранение, добавление и удаление. Все данные хранятся
 * в зашифрованном виде с использованием Base64 через утилиту FileEncryptionUtil.
 * Каждый пользователь имеет свой собственный зашифрованный файл для хранения паролей.
 *
 * @version 2.0
 * @since 2024
 * @see PasswordEntry
 * @see FileEncryptionUtil
 */
public class PasswordStorage {
    private final String username;
    private final String dataFile;
    private final String encryptedFile;
    private List<PasswordEntry> passwords;
    private static final Logger logger = LogManager.getLogger(PasswordStorage.class);

    /**
     * Создает хранилище паролей для указанного пользователя.
     * <p>
     * При создании автоматически генерируются имена файлов для хранения данных:
     * - passwords_[username].dat (незашифрованный файл, для обратной совместимости)
     * - passwords_[username].enc (зашифрованный файл, основной)
     *
     * @param username имя пользователя, для которого создается хранилище
     * @throws NullPointerException если username равен null
     */
    public PasswordStorage(String username) {
        this.username = username;
        this.dataFile = "passwords_" + username + ".dat";
        this.encryptedFile = "passwords_" + username + ".enc";
        this.passwords = new ArrayList<>();
        logger.info("Создание хранилища паролей для пользователя: {}", username);
        loadPasswords();
    }

    /**
     * Загружает пароли из зашифрованного файла пользователя.
     * <p>
     * Метод выполняет следующие действия:
     * 1. Проверяет существование зашифрованного файла
     * 2. Если зашифрованный файл не найден, проверяет наличие старого незашифрованного файла
     * 3. При обнаружении старого файла автоматически шифрует его
     * 4. Дешифрует файл во временный файл, загружает данные и удаляет временный файл
     *
     * @see #encryptOldFile(File, File)
     * @see #loadFromFile(File)
     * @see FileEncryptionUtil#decryptFile(File, File)
     */
    private void loadPasswords() {
        File encFile = new File(encryptedFile);

        if (!encFile.exists()) {
            logger.info("Файл паролей не найден: {}", encryptedFile);
            return;
        }

        try {
            // 1. Читаем Base64 из файла
            String base64Content;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(encFile), StandardCharsets.UTF_8))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                base64Content = content.toString();
            }

            // 2. Декодируем Base64 в байты
            byte[] fileBytes = Base64.getDecoder().decode(base64Content);

            // 3. Загружаем из байтов
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(fileBytes))) {
                int count = ois.readInt();
                passwords.clear();

                for (int i = 0; i < count; i++) {
                    String service = ois.readUTF();
                    String username = ois.readUTF();
                    String password = ois.readUTF();
                    String encryptionTypeStr = ois.readUTF();

                    EncryptionType encryptionType = EncryptionType.valueOf(encryptionTypeStr);
                    PasswordEntry entry = new PasswordEntry(service, username, password, encryptionType);
                    entry.initializeProperties();
                    passwords.add(entry);
                }

                logger.info("Загружено паролей: {}", count);
            }

        } catch (IOException | IllegalArgumentException e) {
            logger.error("Ошибка загрузки паролей: {}", e.getMessage());
            System.out.println("Ошибка загрузки паролей: " + e.getMessage());
            passwords.clear();
        }
    }

    /**
     * Загружает пароли из обычного (незашифрованного) файла.
     * <p>
     * Формат файла (новая версия):
     * - Первое значение: количество записей (int)
     * - Для каждой записи: название сервиса (UTF), логин (UTF), пароль (UTF), тип шифрования (UTF)
     *
     * @param file файл для загрузки данных
     * @throws IOException если произошла ошибка ввода-вывода при чтении файла
     * @throws EOFException если достигнут конец файла раньше ожидаемого
     * @see ObjectInputStream
     */
    private void loadFromFile(File file) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            int count = ois.readInt();
            passwords.clear();

            for (int i = 0; i < count; i++) {
                String service = ois.readUTF();
                String username = ois.readUTF();
                String password = ois.readUTF();
                String encryptionTypeStr = ois.readUTF();

                EncryptionType encryptionType = EncryptionType.valueOf(encryptionTypeStr);
                PasswordEntry entry = new PasswordEntry(service, username, password, encryptionType);
                entry.initializeProperties(); // Инициализируем JavaFX свойства
                passwords.add(entry);
            }
        } catch (EOFException e) {
            logger.warn("Достигнут конец файла раньше ожидаемого. Возможно, старая версия файла.");
            // Попробуем загрузить старую версию
            loadOldVersionFromFile(file);
        }
    }

    /**
     * Загружает пароли из старой версии файла (без типа шифрования).
     * <p>
     * Используется для обратной совместимости.
     *
     * @param file файл для загрузки данных
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void loadOldVersionFromFile(File file) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            ois.readInt(); // Пропускаем счетчик
            passwords.clear();

            // Пытаемся прочитать в старом формате
            while (true) {
                try {
                    String service = ois.readUTF();
                    String username = ois.readUTF();
                    String password = ois.readUTF();
                    // Для старых записей используем Base64 по умолчанию
                    passwords.add(new PasswordEntry(service, username, password, EncryptionType.BASE64));
                } catch (EOFException e) {
                    break; // Конец файла
                }
            }
            logger.info("Загружено {} паролей из старой версии файла", passwords.size());
        }
    }

    /**
     * Сохраняет пароли в зашифрованный файл.
     * <p>
     * Алгоритм сохранения:
     * 1. Сохраняет данные во временный незашифрованный файл
     * 2. Шифрует временный файл в основной зашифрованный файл
     * 3. Удаляет временный файл
     * 4. Удаляет старый незашифрованный файл (если существует)
     *
     * @see #saveToFile(File)
     * @see FileEncryptionUtil#encryptFile(File, File)
     */
    private void savePasswords() {
        try {
            // 1. Сохраняем во временный файл (бинарный формат)
            File tempFile = new File("temp_" + username + ".dat");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
                oos.writeInt(passwords.size());
                for (PasswordEntry entry : passwords) {
                    entry.prepareForSerialization();
                    oos.writeUTF(entry.getServiceName());
                    oos.writeUTF(entry.getUsername());
                    oos.writeUTF(entry.getPassword());
                    oos.writeUTF(entry.getEncryptionType().name());
                }
            }

            // 2. Шифруем ВЕСЬ файл как один Base64 блок
            File encFile = new File(encryptedFile);
            try (FileInputStream fis = new FileInputStream(tempFile);
                 BufferedWriter writer = new BufferedWriter(
                         new OutputStreamWriter(new FileOutputStream(encFile), StandardCharsets.UTF_8))) {

                byte[] fileBytes = new byte[(int) tempFile.length()];
                fis.read(fileBytes);
                String base64 = Base64.getEncoder().encodeToString(fileBytes);
                writer.write(base64);
            }

            // 3. Удаляем временный файл
            tempFile.delete();

            logger.info("Сохранено паролей: {}", passwords.size());
            System.out.println("Сохранено паролей: " + passwords.size());

        } catch (IOException e) {
            logger.error("Ошибка сохранения паролей: {}", e.getMessage());
            System.out.println("Ошибка сохранения паролей: " + e.getMessage());
        }
    }

    /**
     * Сохраняет пароли в обычный (незашифрованный) файл.
     * <p>
     * Формат сохранения (новая версия):
     * - Первое значение: количество записей (int)
     * - Для каждой записи: название сервиса, логин, пароль, тип шифрования (все в UTF)
     *
     * @param file файл для сохранения данных
     * @throws IOException если произошла ошибка ввода-вывода при записи файла
     * @see ObjectOutputStream
     */
    private void saveToFile(File file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeInt(passwords.size());

            for (PasswordEntry entry : passwords) {
                entry.prepareForSerialization(); // Подготавливаем к сериализации
                oos.writeUTF(entry.getServiceName());
                oos.writeUTF(entry.getUsername());
                oos.writeUTF(entry.getPassword());
                oos.writeUTF(entry.getEncryptionType().name());
            }

        } catch (IOException e) {
            logger.error("Ошибка создания временного файла: {}", e.getMessage());
            System.out.println("Ошибка создания временного файла: " + e.getMessage());
        }
    }

    /**
     * Шифрует старый незашифрованный файл в новый зашифрованный файл.
     * <p>
     * @param oldFile старый незашифрованный файл
     * @param newFile новый зашифрованный файл
     * @see FileEncryptionUtil#encryptFile(File, File)
     */
    private void encryptOldFile(File oldFile, File newFile) {
        try {
            FileEncryptionUtil.encryptFile(oldFile, newFile);
            logger.info("Старый файл зашифрован: {} → {}", oldFile.getName(), newFile.getName());
            System.out.println("Старый файл зашифрован: " + oldFile.getName() + " → " + newFile.getName());
            oldFile.delete();
        } catch (IOException e) {
            logger.error("Ошибка шифрования старого файла: {}", e.getMessage());
            System.out.println("Ошибка шифрования старого файла: " + e.getMessage());
        }
    }

    /**
     * Добавляет новый пароль в хранилище с указанным типом шифрования.
     * <p>
     * Шифрует пароль перед сохранением в соответствии с выбранным типом шифрования.
     *
     * @param service название сервиса (например, "Google", "VK", "GitHub")
     * @param username логин или email, используемый для входа в сервис
     * @param password пароль для указанного сервиса
     * @param encryptionType тип шифрования для этого пароля
     * @throws NullPointerException если любой из параметров равен null
     * @see PasswordEntry
     * @see EncryptionFactory
     */
    public void addPassword(String service, String username, String password, EncryptionType encryptionType) {
        try {
            // Шифруем пароль перед сохранением
            EncryptionStrategy strategy = EncryptionFactory.createStrategy(encryptionType);
            String encryptedPassword = strategy.encrypt(password);

            if (encryptedPassword == null) {
                System.out.println("Ошибка: Не удалось зашифровать пароль");
                return;
            }

            passwords.add(new PasswordEntry(service, username, encryptedPassword, encryptionType));
            savePasswords();
            logger.info("Добавлен пароль для сервиса '{}' для пользователя: {}, тип шифрования: {}",
                    service, this.username, encryptionType);
            System.out.println("Пароль для '" + service + "' добавлен (шифрование: " +
                    encryptionType.getDescription() + ").");

        } catch (Exception e) {
            logger.error("Ошибка при добавлении пароля: {}", e.getMessage());
            System.out.println("Ошибка при добавлении пароля: " + e.getMessage());
        }
    }

    /**
     * Возвращает копию списка всех паролей пользователя.
     * <p>
     * Возвращаемый список является копией внутреннего списка,
     * что предотвращает случайное изменение данных извне.
     *
     * @return неизменяемый список объектов PasswordEntry
     * @see ArrayList#ArrayList(Collection)
     */
    public List<PasswordEntry> getAllPasswords() {
        return new ArrayList<>(passwords);
    }

    /**
     * Возвращает список паролей с расшифрованными паролями.
     * <p>
     * Для каждого пароля выполняется дешифрование в соответствии с его типом шифрования.
     *
     * @return список PasswordEntry с расшифрованными паролями
     */
    public List<PasswordEntry> getAllPasswordsDecrypted() {
        List<PasswordEntry> decryptedList = new ArrayList<>();

        for (PasswordEntry entry : passwords) {
            try {
                EncryptionStrategy strategy = EncryptionFactory.createStrategy(entry.getEncryptionType());
                String decryptedPassword = strategy.decrypt(entry.getPassword());

                PasswordEntry decryptedEntry = new PasswordEntry(
                        entry.getServiceName(),
                        entry.getUsername(),
                        decryptedPassword != null ? decryptedPassword : "[ОШИБКА ДЕШИФРОВАНИЯ]",
                        entry.getEncryptionType()
                );
                decryptedList.add(decryptedEntry);

            } catch (Exception e) {
                logger.error("Ошибка дешифрования пароля для сервиса {}: {}",
                        entry.getServiceName(), e.getMessage());
                // Добавляем запись с сообщением об ошибке
                PasswordEntry errorEntry = new PasswordEntry(
                        entry.getServiceName(),
                        entry.getUsername(),
                        "[ОШИБКА ДЕШИФРОВАНИЯ: " + e.getMessage() + "]",
                        entry.getEncryptionType()
                );
                decryptedList.add(errorEntry);
            }
        }

        return decryptedList;
    }

    /**
     * Удаляет пароль по указанному индексу.
     *
     * @param index удаляемого пароля (начиная с 0)
     * @return true если пароль был успешно удален, false если индекс неверный
     */
    public boolean removePassword(int index) {
        if (index >= 0 && index < passwords.size()) {
            PasswordEntry removed = passwords.remove(index);
            savePasswords();
            logger.info("Удален пароль для сервиса '{}' для пользователя: {}", removed.getServiceName(), username);
            System.out.println("Удален пароль для: " + removed.getServiceName());
            return true;
        }
        return false;
    }

    /**
     * Возвращает количество сохраненных паролей.
     *
     * @return количество паролей в хранилище
     */
    public int getPasswordCount() {
        return passwords.size();
    }

    /**
     * Очищает все пароли пользователя.
     * <p>
     */
    public void clearAllPasswords() {
        passwords.clear();
        savePasswords();
        logger.info("Все пароли удалены для пользователя: {}", username);
        System.out.println("Все пароли удалены.");
    }

    /**
     * Возвращает имя зашифрованного файла данных.
     *
     * @return имя зашифрованного файла в формате "passwords_[username].enc"
     */
    public String getDataFileName() {
        return encryptedFile;
    }

    /**
     * Проверяет, зашифрован ли файл данных.
     * <p>
     * Проверка выполняется с помощью утилиты FileEncryptionUtil,
     * которая анализирует содержимое файла.
     *
     * @return true если файл зашифрован, false в противном случае
     * @see FileEncryptionUtil#isFileEncrypted(File)
     */
    public boolean isEncrypted() {
        File encFile = new File(encryptedFile);
        return FileEncryptionUtil.isFileEncrypted(encFile);
    }
}