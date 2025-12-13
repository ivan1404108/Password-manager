package passwordmanager.GUI;

import javafx.application.Application;
import javafx.stage.Stage;
import passwordmanager.core.UserManager;
import passwordmanager.core.PasswordStorage;
import passwordmanager.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Главный менеджер графического интерфейса приложения.
 * <p>
 * Координирует работу всех окон и управляет переключением между ними.
 * Является точкой входа для GUI режима приложения.
 *
 * @version 1.0
 * @since 2024
 * @see LoginWindow
 * @see MainWindow
 */
public class GUIManager {
    private static final Logger logger = LogManager.getLogger(GUIManager.class);
    private static User currentUser;
    private static PasswordStorage currentStorage;
    private static final UserManager userManager = new UserManager();

    /**
     * Запускает графический интерфейс приложения.
     * <p>
     * Инициализирует JavaFX приложение и отображает окно входа.
     */
    public static void launchGUI() {
        // Запускаем JavaFX приложение
        Application.launch(GUIApplication.class);
    }

    /**
     * Внутренний класс для запуска JavaFX приложения.
     */
    public static class GUIApplication extends Application {
        @Override
        public void start(Stage primaryStage) {
            logger.info("Запуск JavaFX приложения");
            showLoginWindow(primaryStage);
        }
    }

    /**
     * Показывает окно входа/регистрации.
     *
     * @param primaryStage главная сцена приложения
     */
    public static void showLoginWindow(Stage primaryStage) {
        LoginWindow loginWindow = new LoginWindow(primaryStage, userManager);
        loginWindow.show();
    }

    /**
     * Показывает главное окно приложения после успешного входа.
     *
     * @param primaryStage главная сцена приложения
     * @param user авторизованный пользователь
     */
    public static void showMainWindow(Stage primaryStage, User user) {
        currentUser = user;
        currentStorage = new PasswordStorage(user.getUsername());

        MainWindow mainWindow = new MainWindow(primaryStage, user, currentStorage);
        mainWindow.show();
    }

    /**
     * Выполняет выход из аккаунта.
     *
     * @param primaryStage главная сцена приложения
     */
    public static void logout(Stage primaryStage) {
        logger.info("Выход пользователя: {}", currentUser != null ? currentUser.getUsername() : "null");
        currentUser = null;
        currentStorage = null;
        showLoginWindow(primaryStage);
    }

    /**
     * Возвращает текущего авторизованного пользователя.
     *
     * @return текущий пользователь или null если пользователь не авторизован
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Возвращает хранилище паролей текущего пользователя.
     *
     * @return хранилище паролей или null если пользователь не авторизован
     */
    public static PasswordStorage getCurrentStorage() {
        return currentStorage;
    }
}