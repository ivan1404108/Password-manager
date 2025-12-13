package passwordmanager;

import passwordmanager.core.PasswordManager;
import passwordmanager.GUI.GUIManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Главный класс приложения Менеджер Паролей.
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    /**
     * Точка входа в приложение.
     */
    public static void main(String[] args) {
        // Устанавливаем кодировку системы
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");

        logger.info("=== ЗАПУСК ПРИЛОЖЕНИЯ МЕНЕДЖЕР ПАРОЛЕЙ ===");

        // Всегда запускаем GUI режим, если доступен
        if (isJavaFXAvailable()) {
            logger.info("Запуск в режиме графического интерфейса");
            GUIManager.launchGUI();
        } else {
            logger.info("Запуск в режиме консольного интерфейса");
            runConsoleMode();
        }

        logger.info("=== ЗАВЕРШЕНИЕ РАБОТЫ ПРИЛОЖЕНИЯ ===");
    }

    private static void runConsoleMode() {
        try {
            PasswordManager manager = new PasswordManager();
            manager.run();
        } catch (Exception e) {
            logger.error("Ошибка в консольном режиме: {}", e.getMessage(), e);
            System.err.println("Критическая ошибка: " + e.getMessage());
            System.err.println("Попробуйте запустить с аргументом 'gui' для графического интерфейса");
        }
    }

    private static boolean isJavaFXAvailable() {
        try {
            Class.forName("javafx.application.Application");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}