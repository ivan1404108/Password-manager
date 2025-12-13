package passwordmanager.core;

import passwordmanager.exception.*;
import passwordmanager.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Менеджер пользователей приложения.
 * <p>
 * Класс отвечает за управление учетными записями пользователей:
 * регистрацию, авторизацию, хранение и загрузку пользовательских данных.
 * Все пользователи сохраняются в файл users.dat в сериализованном виде.
 *
 * @version 2.0
 * @since 2024
 * @see User
 */
public class UserManager {
    private static final String USERS_FILE = "users.dat";
    private final Map<String, User> users;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Logger logger = LogManager.getLogger(UserManager.class);
    private static final String SALT_PREFIX = "PM2024!"; // Статическая часть соли

    /**
     * Конструктор менеджера пользователей.
     * <p>
     * Инициализирует внутреннюю структуру для хранения пользователей
     * и загружает существующих пользователей из файла.
     *
     * @see #loadUsers()
     */
    public UserManager() {
        logger.info("Инициализация UserManager");
        this.users = new HashMap<>();
        loadUsers();
    }

    /**
     * Загружает пользователей из файла users.dat.
     * <p>
     * Формат файла:
     * - Первое значение: количество пользователей (int)
     * - Для каждого пользователя: логин (UTF), зашифрованный пароль (UTF)
     *
     * Если файл не существует, создается новая пустая база пользователей.
     *
     * @throws IOException если произошла ошибка ввода-вывода при чтении файла
     * @throws ClassNotFoundException если формат файла поврежден
     * @see ObjectInputStream
     */
    private void loadUsers() {
        logger.info("Загрузка пользователей из файла: {}", USERS_FILE);
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            logger.warn("Файл пользователей не найден. Будет создан новый.");
            System.out.println("Файл пользователей не найден. Будет создан новый.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            int userCount = ois.readInt();

            for (int i = 0; i < userCount; i++) {
                String username = ois.readUTF();
                String encryptedPassword = ois.readUTF();
                User user = new User(username, encryptedPassword);
                users.put(username, user);
            }


        } catch (Exception e) {
            logger.error("Ошибка загрузки пользователей: {}", e.getMessage());
            System.out.println("Ошибка при загрузке пользователей: " + e.getMessage());
        }
    }

    /**
     * Сохраняет всех пользователей в файл users.dat.
     * <p>
     * Формат сохранения:
     * - Первое значение: количество пользователей (int)
     * - Для каждого пользователя: логин, зашифрованный пароль (все в UTF)
     *
     * @throws IOException если произошла ошибка ввода-вывода при записи файла
     * @see ObjectOutputStream
     */
    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeInt(users.size());

            for (User user : users.values()) {
                oos.writeUTF(user.getUsername());
                oos.writeUTF(user.getEncryptedPassword());
            }

            logger.info("Сохранено пользователей в файл: {}", users.size());
            System.out.println("Сохранено пользователей: " + users.size());

        } catch (IOException e) {
            logger.error("Ошибка сохранения пользователей: {}", e.getMessage());
            System.out.println("Ошибка сохранения пользователей: " + e.getMessage());
        }
    }

    /**
     * Регистрирует нового пользователя в системе.
     * <p>
     * Выполняет проверки:
     * 1. Непустые поля логина, пароля и подтверждения пароля
     * 2. Минимальная длина пароля (8 символов)
     * 3. Совпадение пароля и подтверждения пароля
     * 4. Отсутствие пользователя с таким логином
     *
     * @param username логин нового пользователя
     * @param password пароль пользователя
     * @param confirmPassword подтверждение пароля
     * @return true если регистрация прошла успешно, false в случае ошибки
     * @throws EmptyFieldException если одно из обязательных полей пустое
     * @throws PasswordTooShortException если пароль короче минимальной длины
     * @throws PasswordMismatchException если пароль и подтверждение не совпадают
     * @throws UserAlreadyExistsException если пользователь с таким логином уже существует
     */
    public boolean registerUser(String username, String password, String confirmPassword)
            throws EmptyFieldException, PasswordMismatchException,
            UserAlreadyExistsException, PasswordTooShortException {

        logger.info("Начало регистрации пользователя: {}", username);

        if (username == null || username.trim().isEmpty()) {
            logger.warn("Пустой логин при регистрации");
            throw new EmptyFieldException("Логин не может быть пустым");
        }

        if (password == null || password.trim().isEmpty()) {
            logger.warn("Пустой пароль при регистрации");
            throw new EmptyFieldException("Пароль не может быть пустым");
        }

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            logger.warn("Пустое подтверждение пароля");
            throw new EmptyFieldException("Подтверждение пароля не может быть пустым");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            logger.warn("Слишком короткий пароль: {} символов", password.length());
            throw new PasswordTooShortException(
                    String.format("Пароль должен содержать минимум %d символов. Сейчас: %d",
                            MIN_PASSWORD_LENGTH, password.length())
            );
        }

        if (!password.equals(confirmPassword)) {
            logger.warn("Пароли не совпадают при регистрации: {}", username);
            throw new PasswordMismatchException("Пароли не совпадают");
        }

        if (users.containsKey(username)) {
            logger.warn("Попытка повторной регистрации: {}", username);
            throw new UserAlreadyExistsException("Пользователь '" + username + "' уже существует");
        }

        try {
            // Используем простое salted хеширование для паролей пользователей
            String encryptedPassword = hashPassword(password);
            User newUser = new User(username, encryptedPassword);
            users.put(username, newUser);

            saveUsers();

            createUserPasswordFile(username);

            logger.info("Пользователь успешно зарегистрирован: {}", username);
            System.out.println("Пользователь '" + username + "' успешно зарегистрирован");
            System.out.println("Длина пароля: " + password.length() + " символов ✓");
            return true;

        } catch (Exception e) {
            logger.error("Ошибка при регистрации пользователя {}: {}", username, e.getMessage());
            System.out.println("Ошибка при регистрации: " + e.getMessage());
            return false;
        }
    }

    /**
     * Хеширует пароль пользователя с солью.
     *
     * @param password пароль для хеширования
     * @return хешированный пароль с солью
     */
    private String hashPassword(String password) {
        try {
            // Динамическая соль: статический префикс + имя пользователя
            String dynamicSalt = SALT_PREFIX + password.length();

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String combined = dynamicSalt + password;
            byte[] hash = md.digest(combined.getBytes(StandardCharsets.UTF_8));

            // Кодируем в Base64 для удобного хранения
            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            logger.error("Ошибка хеширования пароля: {}", e.getMessage());
            throw new RuntimeException("Ошибка при обработке пароля");
        }
    }

    /**
     * Выполняет авторизацию пользователя.
     * <p>
     * Проверяет существование пользователя и корректность пароля.
     *
     * @param username логин пользователя
     * @param password пароль пользователя
     * @return объект User если авторизация успешна, null в противном случае
     * @throws IllegalArgumentException если логин или пароль пустые
     */
    public User login(String username, String password) {
        logger.info("Попытка входа пользователя: {}", username);

        try {
            if (username == null || username.trim().isEmpty()) {
                logger.warn("Пустой логин при входе");
                System.out.println("Ошибка: Логин не может быть пустым");
                return null;
            }

            if (password == null || password.trim().isEmpty()) {
                logger.warn("Пустой пароль при входе");
                System.out.println("Ошибка: Пароль не может быть пустым");
                return null;
            }

            User user = users.get(username);
            if (user == null) {
                logger.warn("Пользователь не найден: {}", username);
                System.out.println("Ошибка: Пользователь '" + username + "' не найден");
                return null;
            }

            String hashedInput = hashPassword(password);

            if (hashedInput.equals(user.getEncryptedPassword())) {
                logger.info("Авторизация успешна: {}", username);
                System.out.println("Авторизация успешна! Добро пожаловать, " + username);
                return user;
            } else {
                logger.warn("Неверный пароль для пользователя: {}", username);
                System.out.println("Ошибка: Неверный пароль");
                return null;
            }

        } catch (Exception e) {
            logger.error("Ошибка при входе пользователя {}: {}", username, e.getMessage());
            System.out.println("Ошибка при входе: " + e.getMessage());
            return null;
        }
    }

    /**
     * Создает файл для хранения паролей пользователя.
     *
     * @param username имя пользователя для которого создается файл
     * @throws IOException если не удалось создать файл
     */
    private void createUserPasswordFile(String username) {
        String filename = getPasswordFileName(username);
        File file = new File(filename);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeInt(0); // Начальное количество паролей
            logger.info("Создан файл паролей: {}", filename);
            System.out.println("Создан файл паролей: " + filename);
        } catch (IOException e) {
            logger.error("Ошибка создания файла паролей: {}", e.getMessage());
            System.out.println("Ошибка создания файла паролей: " + e.getMessage());
        }
    }

    /**
     * Генерирует имя файла для хранения паролей пользователя.
     *
     * @param username имя пользователя
     * @return имя файла в формате "passwords_[username].dat"
     */
    public String getPasswordFileName(String username) {
        return "passwords_" + username + ".dat";
    }

    /**
     * Проверяет существование пользователя с указанным логином.
     *
     * @param username логин для проверки
     * @return true если пользователь существует, false в противном случае
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    /**
     * Возвращает количество зарегистрированных пользователей.
     *
     * @return количество пользователей в системе
     */
    public int getUserCount() {
        return users.size();
    }

    /**
     * Возвращает минимальную допустимую длину пароля.
     *
     * @return минимальная длина пароля в символах
     */
    public int getMinPasswordLength() {
        return MIN_PASSWORD_LENGTH;
    }

    /**
     * Проверяет существование файла с данными пользователей.
     *
     * @return true если файл users.dat существует, false в противном случае
     */
    public boolean usersFileExists() {
        return new File(USERS_FILE).exists();
    }

    /**
     * Отображает информацию о файлах системы
     * <p>
     * Показывает:
     * - Статус файла users.dat
     * - Для каждого пользователя: статус файла с паролями
     */
    public void showFileInfo() {
        logger.info("Отображение информации о файлах");
        System.out.println("\n--- ИНФОРМАЦИЯ О ФАЙЛАХ ---");
        System.out.println("Файл пользователей: " + USERS_FILE +
                " (существует: " + new File(USERS_FILE).exists() + ")");

        for (String username : users.keySet()) {
            String passFile = "passwords_" + username + ".dat";
            File file = new File(passFile);
            System.out.println("Пользователь " + username + ": " + passFile +
                    " (существует: " + file.exists() + ")");
        }
    }
}