package passwordmanager.core;

import passwordmanager.exception.EmptyFieldException;
import passwordmanager.exception.PasswordMismatchException;
import passwordmanager.exception.PasswordTooShortException;
import passwordmanager.exception.UserAlreadyExistsException;
import passwordmanager.model.EncryptionType;
import passwordmanager.model.PasswordEntry;
import passwordmanager.model.User;

import java.util.Scanner;
import java.util.NoSuchElementException;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Главный менеджер приложения для управления паролями с поддержкой множества пользователей.
 * <p>
 * Класс отвечает за основной цикл работы приложения, переключение между режимами
 * (неавторизованный/авторизованный пользователь) и координацию работы всех компонентов системы.
 * Предоставляет консольный интерфейс для взаимодействия с пользователем.
 *
 * @version 2.0
 * @since 2024
 * @see UserManager
 * @see PasswordStorage
 */
public class PasswordManager {
    private final UserManager userManager;
    private PasswordStorage currentUserStorage;
    private User currentUser;
    private final Scanner scanner;
    private boolean isRunning;
    private static final Logger logger = LogManager.getLogger(PasswordManager.class);

    /**
     * Конструктор класса PasswordManager.
     * <p>
     * Инициализирует все компоненты приложения:
     * менеджер пользователей, сканер для ввода данных
     * @throws SecurityException если невозможно создать объект Scanner
     */
    public PasswordManager() {
        logger.info("Инициализация PasswordManager");
        this.userManager = new UserManager();
        this.scanner = new Scanner(System.in);
        this.isRunning = true;
        this.currentUser = null;
        this.currentUserStorage = null;
    }

    /**
     * Запускает главный цикл приложения.
     * <p>
     * Метод управляет основным потоком выполнения приложения
     *
     * @see #showMainMenu()
     * @see #showPasswordMenu()
     */
    public void run() {
        logger.info("Запуск главного цикла приложения");
        System.out.println("=== МЕНЕДЖЕР ПАРОЛЕЙ ===");
        System.out.println("Поддерживается множество пользователей\n");

        try {
            while (isRunning) {
                if (currentUser == null) {
                    showMainMenu();
                } else {
                    showPasswordMenu();
                }
            }
        } catch (Exception e) {
            logger.error("Критическая ошибка в главном цикле: {}", e.getMessage(), e);
            System.out.println("Критическая ошибка: " + e.getMessage());
        } finally {
            // Безопасное закрытие Scanner
            if (scanner != null) {
                try {
                    scanner.close();
                    logger.info("Scanner закрыт");
                } catch (Exception e) {
                    logger.warn("Ошибка при закрытии Scanner: {}", e.getMessage());
                }
            }
        }

        logger.info("Завершение главного цикла");
        System.out.println("\nПрограмма завершена.");
    }

    /**
     * Отображает главное меню для неавторизованных пользователей.
     * <p>
     * Предоставляет следующие опции:
     * 1. Регистрация нового пользователя
     * 2. Вход в существующий аккаунт
     * 3. Просмотр статистики системы
     * 4. Выход из приложения
     *
     * @see #handleRegistration()
     * @see #handleLogin()
     * @see #showStatistics()
     */
    private void showMainMenu() {
        System.out.println("\n=== ГЛАВНОЕ МЕНЮ ===");
        System.out.println("1. Регистрация");
        System.out.println("2. Вход");
        System.out.println("3. Статистика");
        System.out.println("4. Выход");
        System.out.print("Выберите действие: ");

        try {
            if (!scanner.hasNextLine()) {
                // Если нет ввода, ждем немного или завершаем
                Thread.sleep(100);
                if (!scanner.hasNextLine()) {
                    System.out.println("\nНет доступного ввода. Завершение работы.");
                    isRunning = false;
                    return;
                }
            }

            String choice = scanner.nextLine();

            if (choice == null || choice.trim().isEmpty()) {
                logger.info("Получена пустая строка.");
                return;
            }

            switch (choice.trim()) {
                case "1":
                    handleRegistration();
                    break;
                case "2":
                    handleLogin();
                    break;
                case "3":
                    showStatistics();
                    break;
                case "4":
                    isRunning = false;
                    System.out.println("\nЗавершение работы приложения.");
                    break;
                default:
                    System.out.println("Неверный выбор. Пожалуйста, выберите 1, 2, 3 или 4.");
            }

        } catch (NoSuchElementException e) {
            logger.error("Ошибка ввода (NoSuchElementException): {}", e.getMessage());
            System.out.println("\nОшибка ввода. Завершение работы.");
            isRunning = false;
        } catch (IllegalStateException e) {
            logger.error("Ошибка ввода (IllegalStateException): {}", e.getMessage());
            System.out.println("\nОшибка ввода. Завершение работы.");
            isRunning = false;
        } catch (Exception e) {
            logger.error("Неожиданная ошибка в showMainMenu(): {}", e.getMessage(), e);
            System.out.println("\nПроизошла ошибка: " + e.getMessage());
        }
    }

    /**
     * Отображает меню управления паролями для авторизованных пользователей.
     * <p>
     * Предоставляет следующие опции:
     * 1. Просмотр всех сохраненных паролей
     * 2. Добавление нового пароля
     * 3. Удаление существующего пароля
     * 4. Просмотр информации о текущем пользователе
     * 5. Выход из аккаунта
     *
     * @see #showAllPasswords()
     * @see #addNewPassword()
     * @see #deletePassword()
     * @see #showUserInfo()
     * @see #logout()
     */
    private void showPasswordMenu() {
        System.out.println("\n=== МЕНЮ ПАРОЛЕЙ ===");
        System.out.println("Пользователь: " + currentUser.getUsername());
        System.out.println("1. Показать все пароли");
        System.out.println("2. Добавить новый пароль");
        System.out.println("3. Удалить пароль");
        System.out.println("4. Мои данные");
        System.out.println("5. Выход из аккаунта");
        System.out.print("Выберите действие: ");

        try {
            String choice = scanner.nextLine();

            if (choice == null || choice.trim().isEmpty()) {
                logger.info("Получена пустая строка.");
                return;
            }

            switch (choice.trim()) {
                case "1":
                    showAllPasswords();
                    break;
                case "2":
                    addNewPassword();
                    break;
                case "3":
                    deletePassword();
                    break;
                case "4":
                    showUserInfo();
                    break;
                case "5":
                    logout();
                    break;
                default:
                    System.out.println("Неверный выбор. Пожалуйста, выберите от 1 до 5.");
            }

        } catch (NoSuchElementException e) {
            logger.error("Ошибка ввода в showPasswordMenu() (NoSuchElementException): {}", e.getMessage());
            System.out.println("\nОшибка ввода. Выход из аккаунта.");
            logout();
        } catch (IllegalStateException e) {
            logger.error("Ошибка ввода в showPasswordMenu() (IllegalStateException): {}", e.getMessage());
            System.out.println("\nОшибка ввода. Выход из аккаунта.");
            logout();
        } catch (Exception e) {
            logger.error("Неожиданная ошибка в showPasswordMenu(): {}", e.getMessage(), e);
            System.out.println("\nПроизошла ошибка: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает процесс регистрации нового пользователя.
     * <p>
     * Запрашивает у пользователя:
     * - Логин
     * - Пароль (с подтверждением)
     *
     * @throws EmptyFieldException если одно из полей пустое
     * @throws PasswordMismatchException если пароли не совпадают
     * @throws PasswordTooShortException если пароль слишком короткий
     * @throws UserAlreadyExistsException если пользователь с таким логином уже существует
     * @see UserManager#registerUser(String, String, String)
     */
    private void handleRegistration() {
        logger.info("Начало процесса регистрации");
        System.out.println("\n  РЕГИСТРАЦИЯ");

        try {
            System.out.print("Введите логин: ");
            String username = scanner.nextLine();

            System.out.print("Введите пароль (мин. 8 символов): ");
            String password = scanner.nextLine();

            System.out.print("Подтвердите пароль: ");
            String confirmPassword = scanner.nextLine();

            boolean success = userManager.registerUser(username, password, confirmPassword);

            if (success) {
                System.out.println(" Регистрация завершена!");
                System.out.println("Теперь вы можете войти в систему.");
                logger.info("Регистрация успешна для пользователя: {}", username);
            }

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            logger.error("Ошибка при регистрации: {}", e.getMessage());
        }
    }

    /**
     * Обрабатывает процесс входа пользователя в систему.
     * <p>
     * Запрашивает логин и пароль, проверяет их корректность и
     * при успешной аутентификации устанавливает текущего пользователя
     * и инициализирует хранилище паролей.
     *
     * @see UserManager#login(String, String)
     */
    private void handleLogin() {
        System.out.println("\n ВХОД ");

        System.out.print("Логин: ");
        String username = scanner.nextLine();

        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        logger.info("Попытка входа пользователя: {}", username);
        User user = userManager.login(username, password);

        if (user != null) {
            currentUser = user;
            currentUserStorage = new PasswordStorage(username);
            System.out.println("Добро пожаловать, " + username + "!");
            logger.info("Вход успешен для пользователя: {}", username);
        } else {
            logger.warn("Неудачная попытка входа для пользователя: {}", username);
        }
    }

    /**
     * Предоставляет пользователю выбор метода шифрования для пароля.
     * <p>
     *
     * @return выбранный тип шифрования
     * @see EncryptionType
     */
    private EncryptionType selectEncryptionTypeForPassword() {
        System.out.println("\nВыберите метод шифрования для этого пароля:");
        System.out.println("1. Открытый текст (не рекомендуется)");
        System.out.println("2. Base64 кодирование");
        System.out.println("3. Подсаливание (рекомендуется)");
        System.out.println("4. Шифр Фейстеля");
        System.out.print("Ваш выбор: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1": return EncryptionType.PLAIN;
            case "2": return EncryptionType.BASE64;
            case "3": return EncryptionType.SALTED;
            case "4": return EncryptionType.FEISTEL;
            default:
                System.out.println("Неверный выбор. Используется подсаливание.");
                return EncryptionType.SALTED;
        }
    }

    /**
     * Отображает все пароли текущего пользователя.
     * <p>
     * Если у пользователя нет сохраненных паролей, отображается
     * соответствующее сообщение. Показывает расшифрованные пароли.
     *
     * @see PasswordStorage#getAllPasswordsDecrypted()
     */
    private void showAllPasswords() {
        if (currentUserStorage == null) return;

        System.out.println("\n ВАШИ ПАРОЛИ");

        var passwords = currentUserStorage.getAllPasswordsDecrypted();

        if (passwords.isEmpty()) {
            System.out.println("Паролей нет. Добавьте первый пароль.");
            return;
        }

        for (int i = 0; i < passwords.size(); i++) {
            PasswordEntry entry = passwords.get(i);
            System.out.println((i + 1) + ". Сервис: " + entry.getServiceName() +
                    ", Логин: " + entry.getUsername() +
                    ", Пароль: " + entry.getPassword() +
                    ", Шифрование: " + entry.getEncryptionType().getDescription());
        }

        System.out.println("Всего паролей: " + passwords.size());
    }

    /**
     * Добавляет новый пароль в хранилище текущего пользователя.
     * <p>
     * Запрашивает у пользователя:
     * - Название сервиса
     * - Логин/email для сервиса
     * - Пароль для сервиса
     * - Метод шифрования для этого пароля
     *
     * @see PasswordStorage#addPassword(String, String, String, EncryptionType)
     */
    private void addNewPassword() {
        if (currentUserStorage == null) return;

        System.out.println("\n ДОБАВЛЕНИЕ ПАРОЛЯ");

        System.out.print("Название сервиса (Google, VK и т.д.): ");
        String service = scanner.nextLine();

        System.out.print("Логин/email: ");
        String username = scanner.nextLine();

        System.out.print("Пароль: ");
        String password = scanner.nextLine();

        EncryptionType encryptionType = selectEncryptionTypeForPassword();

        currentUserStorage.addPassword(service, username, password, encryptionType);
    }

    /**
     * Удаляет пароль по указанному номеру.
     * <p>
     *
     * @throws NumberFormatException если введено не числовое значение
     * @see #showAllPasswords()
     * @see PasswordStorage#removePassword(int)
     */
    private void deletePassword() {
        if (currentUserStorage == null) return;

        showAllPasswords();

        var passwords = currentUserStorage.getAllPasswords();
        if (passwords.isEmpty()) return;

        System.out.print("Введите номер пароля для удаления: ");
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;

            if (currentUserStorage.removePassword(index)) {
                System.out.println("Пароль удален");
            } else {
                System.out.println("Неверный номер");
            }
        } catch (NumberFormatException e) {
            System.out.println("Введите число");
        }
    }

    /**
     * Отображает информацию о текущем пользователе.
     * <p>
     * Показывает:
     * - Логин пользователя
     * - Количество сохраненных паролей
     * - Имя файла с данными
     */
    private void showUserInfo() {
        if (currentUser == null) return;

        System.out.println("\nМОИ ДАННЫЕ");
        System.out.println("Логин: " + currentUser.getUsername());
        System.out.println("Сохранено паролей: " + currentUserStorage.getPasswordCount());
        System.out.println("Файл данных: " + currentUserStorage.getDataFileName());
    }

    /**
     * Выполняет выход текущего пользователя из системы.
     * <p>
     * Сбрасывает текущего пользователя и его хранилище паролей,
     * возвращая приложение в состояние неавторизованного пользователя.
     */
    private void logout() {
        System.out.println("\nВыход из аккаунта " + currentUser.getUsername());
        logger.info("Выход пользователя: {}", currentUser.getUsername());
        currentUser = null;
        currentUserStorage = null;
    }

    /**
     * Отображает общую статистику системы.
     * <p>
     * Показывает:
     * - Количество зарегистрированных пользователей
     * - Минимальную длину пароля
     * - Статус файла пользователей
     *
     * @see UserManager#getUserCount()
     * @see UserManager#getMinPasswordLength()
     * @see UserManager#usersFileExists()
     */
    private void showStatistics() {
        System.out.println("\n СТАТИСТИКА СИСТЕМЫ ");
        System.out.println("Зарегистрировано пользователей: " + userManager.getUserCount());
        System.out.println("Минимальная длина пароля: " + userManager.getMinPasswordLength());
        System.out.println("Файл пользователей: " + (userManager.usersFileExists() ? "существует" : "не найден"));
    }
}