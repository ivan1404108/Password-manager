package passwordmanager.GUI;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import passwordmanager.core.PasswordStorage;
import passwordmanager.model.User;
import passwordmanager.model.PasswordEntry;
import passwordmanager.model.EncryptionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Главное окно приложения для управления паролями.
 * <p>
 * Отображает интерфейс для работы с сохраненными паролями:
 * просмотр, добавление, удаление. Также показывает информацию
 * о текущем пользователе.
 *
 * @version 1.0
 * @since 2024
 * @see PasswordStorage
 * @see PasswordEntry
 */
public class MainWindow {
    private static final Logger logger = LogManager.getLogger(MainWindow.class);

    private final Stage stage;
    private final User user;
    private final PasswordStorage storage;
    private TableView<PasswordEntry> passwordTable;

    /**
     * Конструктор главного окна.
     *
     * @param stage главная сцена приложения
     * @param user авторизованный пользователь
     * @param storage хранилище паролей пользователя
     */
    public MainWindow(Stage stage, User user, PasswordStorage storage) {
        this.stage = stage;
        this.user = user;
        this.storage = storage;
        stage.setTitle("Менеджер паролей - " + user.getUsername());
    }

    /**
     * Показывает окно.
     */
    public void show() {
        BorderPane root = createUI();
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();
        refreshPasswordTable();
    }

    /**
     * Создает пользовательский интерфейс окна.
     *
     * @return корневой контейнер интерфейса
     */
    private BorderPane createUI() {
        BorderPane root = new BorderPane();

        // Верхняя панель - информация о пользователе
        root.setTop(createTopPanel());

        // Центр - таблица с паролями
        root.setCenter(createCenterPanel());

        // Нижняя панель - кнопки действий
        root.setBottom(createBottomPanel());

        return root;
    }

    /**
     * Создает верхнюю панель с информацией о пользователе.
     *
     * @return верхняя панель
     */
    private HBox createTopPanel() {
        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(10));
        topPanel.setSpacing(10);
        topPanel.setStyle("-fx-background-color: #f0f0f0;");

        Label userLabel = new Label("Пользователь: " + user.getUsername());
        userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label countLabel = new Label();
        countLabel.setStyle("-fx-font-size: 14px;");
        countLabel.setText("Паролей: " + storage.getPasswordCount());

        // Кнопка обновления
        Button refreshButton = new Button("Обновить");
        refreshButton.setOnAction(e -> refreshPasswordTable());

        topPanel.getChildren().addAll(userLabel, countLabel, refreshButton);

        return topPanel;
    }

    /**
     * Создает центральную панель с таблицей паролей.
     *
     * @return центральная панель
     */
    private VBox createCenterPanel() {
        VBox centerPanel = new VBox();
        centerPanel.setPadding(new Insets(10));
        centerPanel.setSpacing(10);

        // Таблица паролей
        passwordTable = PasswordTable.createPasswordTable();

        // Прокручиваемая область для таблицы
        ScrollPane scrollPane = new ScrollPane(passwordTable);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        centerPanel.getChildren().add(scrollPane);

        return centerPanel;
    }

    /**
     * Создает нижнюю панель с кнопками действий.
     *
     * @return нижняя панель
     */
    private HBox createBottomPanel() {
        HBox bottomPanel = new HBox();
        bottomPanel.setPadding(new Insets(10));
        bottomPanel.setSpacing(10);
        bottomPanel.setStyle("-fx-background-color: #f0f0f0;");

        // Кнопка добавления пароля
        Button addButton = new Button("Добавить пароль");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addButton.setOnAction(e -> showAddPasswordDialog());

        // Кнопка удаления пароля
        Button deleteButton = new Button("Удалить выбранное");
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> deleteSelectedPassword());

        // Кнопка выхода
        Button logoutButton = new Button("Выход");
        logoutButton.setOnAction(e -> GUIManager.logout(stage));

        bottomPanel.getChildren().addAll(addButton, deleteButton, logoutButton);

        return bottomPanel;
    }

    /**
     * Обновляет данные в таблице паролей.
     */
    private void refreshPasswordTable() {
        try {
            List<PasswordEntry> passwords = storage.getAllPasswordsDecrypted();
            passwordTable.getItems().clear();
            passwordTable.getItems().addAll(passwords);

            logger.info("Обновлена таблица паролей, записей: {}", passwords.size());
        } catch (Exception e) {
            logger.error("Ошибка при обновлении таблицы паролей: {}", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить пароли: " + e.getMessage());
        }
    }

    /**
     * Показывает диалог добавления нового пароля.
     */
    private void showAddPasswordDialog() {
        PasswordDialog dialog = new PasswordDialog(stage);
        dialog.showAndWait().ifPresent(result -> {
            if (result != null) {
                try {
                    storage.addPassword(
                            result.getServiceName(),
                            result.getUsername(),
                            result.getPassword(),
                            result.getEncryptionType()
                    );
                    refreshPasswordTable();
                    showAlert(Alert.AlertType.INFORMATION, "Успех", "Пароль успешно добавлен!");
                } catch (Exception e) {
                    logger.error("Ошибка при добавлении пароля: {}", e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось добавить пароль: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Удаляет выбранный пароль из таблицы.
     */
    private void deleteSelectedPassword() {
        PasswordEntry selected = passwordTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Внимание", "Выберите пароль для удаления");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удаление пароля");
        confirm.setContentText("Вы уверены, что хотите удалить пароль для сервиса: " +
                selected.getServiceName() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    int selectedIndex = passwordTable.getSelectionModel().getSelectedIndex();
                    if (storage.removePassword(selectedIndex)) {
                        refreshPasswordTable();
                        showAlert(Alert.AlertType.INFORMATION, "Успех", "Пароль удален");
                    }
                } catch (Exception e) {
                    logger.error("Ошибка при удалении пароля: {}", e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить пароль: " + e.getMessage());
                }
            }
        });
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