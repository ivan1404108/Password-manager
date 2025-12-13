package passwordmanager.GUI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import passwordmanager.core.UserManager;
import passwordmanager.model.User;
import passwordmanager.exception.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Окно входа и регистрации пользователя.
 * <p>
 * Предоставляет интерфейс для авторизации существующих пользователей
 * и регистрации новых. Содержит две вкладки: "Вход" и "Регистрация".
 *
 * @version 1.0
 * @since 2024
 * @see UserManager
 */
public class LoginWindow {
    private static final Logger logger = LogManager.getLogger(LoginWindow.class);

    private final Stage stage;
    private final UserManager userManager;
    private TabPane tabPane;

    /**
     * Конструктор окна входа.
     *
     * @param stage главная сцена приложения
     * @param userManager менеджер пользователей
     */
    public LoginWindow(Stage stage, UserManager userManager) {
        this.stage = stage;
        this.userManager = userManager;
        stage.setTitle("Менеджер паролей - Вход");
    }

    /**
     * Показывает окно.
     */
    public void show() {
        createUI();
        stage.setScene(new Scene(tabPane, 400, 350));
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Создает пользовательский интерфейс окна.
     */
    private void createUI() {
        tabPane = new TabPane();

        Tab loginTab = createLoginTab();
        Tab registerTab = createRegisterTab();

        tabPane.getTabs().addAll(loginTab, registerTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    }

    /**
     * Создает вкладку "Вход".
     *
     * @return вкладка с формой входа
     */
    private Tab createLoginTab() {
        Tab tab = new Tab("Вход");
        tab.setClosable(false);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // Заголовок
        Label titleLabel = new Label("Вход в систему");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        grid.add(titleLabel, 0, 0, 2, 1);

        // Поле логина
        Label usernameLabel = new Label("Логин:");
        grid.add(usernameLabel, 0, 1);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Введите логин");
        grid.add(usernameField, 1, 1);

        // Поле пароля
        Label passwordLabel = new Label("Пароль:");
        grid.add(passwordLabel, 0, 2);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Введите пароль");
        grid.add(passwordField, 1, 2);

        // Кнопка входа
        Button loginButton = new Button("Войти");
        loginButton.setDefaultButton(true);
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        grid.add(loginButton, 1, 3);

        // Обработчик кнопки входа
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

        // Обработчик нажатия Enter в полях
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

        tab.setContent(grid);
        return tab;
    }

    /**
     * Создает вкладку "Регистрация".
     *
     * @return вкладка с формой регистрации
     */
    private Tab createRegisterTab() {
        Tab tab = new Tab("Регистрация");
        tab.setClosable(false);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // Заголовок
        Label titleLabel = new Label("Регистрация нового пользователя");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        grid.add(titleLabel, 0, 0, 2, 1);

        // Поле логина
        Label usernameLabel = new Label("Логин:");
        grid.add(usernameLabel, 0, 1);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Придумайте логин");
        grid.add(usernameField, 1, 1);

        // Поле пароля
        Label passwordLabel = new Label("Пароль:");
        grid.add(passwordLabel, 0, 2);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Минимум 8 символов");
        grid.add(passwordField, 1, 2);

        // Поле подтверждения пароля
        Label confirmLabel = new Label("Подтверждение:");
        grid.add(confirmLabel, 0, 3);

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Повторите пароль");
        grid.add(confirmField, 1, 3);

        // Кнопка регистрации
        Button registerButton = new Button("Зарегистрироваться");
        registerButton.setDefaultButton(true);
        registerButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        grid.add(registerButton, 1, 4);

        // Обработчик кнопки регистрации
        registerButton.setOnAction(e -> handleRegistration(
                usernameField.getText(),
                passwordField.getText(),
                confirmField.getText()
        ));

        tab.setContent(grid);
        return tab;
    }

    /**
     * Обрабатывает попытку входа пользователя.
     *
     * @param username введенный логин
     * @param password введенный пароль
     */
    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Ошибка", "Заполните все поля");
            return;
        }

        try {
            User user = userManager.login(username, password);
            if (user != null) {
                logger.info("Успешный вход пользователя: {}", username);
                GUIManager.showMainWindow(stage, user);
            } else {
                showAlert(Alert.AlertType.ERROR, "Ошибка входа", "Неверный логин или пароль");
            }
        } catch (Exception e) {
            logger.error("Ошибка при входе: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
        }
    }

    /**
     * Обрабатывает попытку регистрации пользователя.
     *
     * @param username введенный логин
     * @param password введенный пароль
     * @param confirmPassword подтверждение пароля
     */
    private void handleRegistration(String username, String password, String confirmPassword) {
        try {
            boolean success = userManager.registerUser(username, password, confirmPassword);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Успех",
                        "Регистрация завершена! Теперь вы можете войти в систему.");

                // Переключаемся на вкладку входа
                tabPane.getSelectionModel().select(0);
            }
        } catch (EmptyFieldException | PasswordTooShortException |
                 PasswordMismatchException | UserAlreadyExistsException e) {
            showAlert(Alert.AlertType.WARNING, "Ошибка регистрации", e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка при регистрации: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Ошибка", e.getMessage());
        }
    }

    /**
     * Показывает диалоговое окно с сообщением.
     *
     * @param type тип сообщения
     * @param title заголовок окна
     * @param message текст сообщения
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}