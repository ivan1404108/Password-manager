package passwordmanager.GUI;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import passwordmanager.model.EncryptionType;
import passwordmanager.model.PasswordEntry;

/**
 * Диалоговое окно для добавления нового пароля.
 * <p>
 * Предоставляет форму для ввода данных о пароле:
 * название сервиса, логин, пароль и выбор метода шифрования.
 *
 * @version 1.0
 * @since 2024
 * @see PasswordEntry
 */
public class PasswordDialog extends Dialog<PasswordEntry> {

    // Элементы формы
    private TextField serviceField;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField visiblePasswordField;
    private ComboBox<String> encryptionCombo;
    private CheckBox showPasswordCheck;

    /**
     * Конструктор диалогового окна.
     *
     * @param parentStage родительское окно
     */
    public PasswordDialog(Stage parentStage) {
        setTitle("Добавление нового пароля");
        setHeaderText("Введите данные для нового пароля");

        // Устанавливаем кнопки OK и Cancel
        ButtonType addButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Создаем форму
        GridPane grid = createForm();

        // Устанавливаем форму в диалог
        getDialogPane().setContent(grid);

        // Преобразуем результат в объект PasswordEntry
        setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return createPasswordEntryFromForm();
            }
            return null;
        });

        // Валидация формы
        setupFormValidation();
    }

    /**
     * Создает форму для ввода данных.
     *
     * @return GridPane с элементами формы
     */
    private GridPane createForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Поле для названия сервиса
        Label serviceLabel = new Label("Сервис:");
        serviceField = new TextField();
        serviceField.setPromptText("Например: Google, VK, GitHub");
        grid.add(serviceLabel, 0, 0);
        grid.add(serviceField, 1, 0);

        // Поле для логина/email
        Label usernameLabel = new Label("Логин/Email:");
        usernameField = new TextField();
        usernameField.setPromptText("Ваш логин для сервиса");
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);

        // Поле для пароля
        Label passwordLabel = new Label("Пароль:");
        passwordField = new PasswordField();
        passwordField.setPromptText("Пароль для сервиса");
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);

        // Кнопка показать/скрыть пароль
        showPasswordCheck = new CheckBox("Показать пароль");
        visiblePasswordField = new TextField();
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setPromptText("Пароль для сервиса");

        showPasswordCheck.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                visiblePasswordField.setText(passwordField.getText());
                visiblePasswordField.setManaged(true);
                visiblePasswordField.setVisible(true);
                passwordField.setManaged(false);
                passwordField.setVisible(false);
                GridPane.setConstraints(visiblePasswordField, 1, 2);
                grid.getChildren().add(visiblePasswordField);
            } else {
                passwordField.setText(visiblePasswordField.getText());
                passwordField.setManaged(true);
                passwordField.setVisible(true);
                visiblePasswordField.setManaged(false);
                visiblePasswordField.setVisible(false);
                GridPane.setConstraints(passwordField, 1, 2);
                grid.getChildren().add(passwordField);
            }
        });

        grid.add(showPasswordCheck, 1, 3);

        // Выбор метода шифрования
        Label encryptionLabel = new Label("Шифрование:");
        encryptionCombo = new ComboBox<>();
        encryptionCombo.getItems().addAll(
                EncryptionType.PLAIN.getDescription(),
                EncryptionType.BASE64.getDescription(),
                EncryptionType.SALTED.getDescription(),
                EncryptionType.FEISTEL.getDescription()
        );
        encryptionCombo.setValue(EncryptionType.SALTED.getDescription()); // Значение по умолчанию
        grid.add(encryptionLabel, 0, 4);
        grid.add(encryptionCombo, 1, 4);

        // Информация о выбранном шифровании
        Label infoLabel = new Label();
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        grid.add(infoLabel, 1, 5);

        // Обновляем информацию при изменении выбора
        encryptionCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.equals(EncryptionType.PLAIN.getDescription())) {
                infoLabel.setText("Пароль будет сохранен в открытом виде!");
            } else if (newVal.equals(EncryptionType.BASE64.getDescription())) {
                infoLabel.setText("Пароль будет закодирован в Base64");
            } else if (newVal.equals(EncryptionType.SALTED.getDescription())) {
                infoLabel.setText("Пароль будет зашифрован с добавлением <соли>");
            } else if (newVal.equals(EncryptionType.FEISTEL.getDescription())) {
                infoLabel.setText("Используется шифр Фейстеля");
            }
        });


        return grid;
    }

    /**
     * Настраивает валидацию формы.
     */
    private void setupFormValidation() {
        // Получаем кнопку OK
        Button addButton = (Button) getDialogPane().lookupButton(getDialogPane()
                .getButtonTypes().stream()
                .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                .findFirst().orElse(null));

        if (addButton == null) return;

        // Делаем кнопку неактивной до заполнения полей
        addButton.setDisable(true);

        // Слушаем изменения во всех полях
        Runnable checkFields = () -> {
            boolean serviceValid = !serviceField.getText().trim().isEmpty();
            boolean usernameValid = !usernameField.getText().trim().isEmpty();
            boolean passwordValid;

            if (showPasswordCheck.isSelected()) {
                passwordValid = !visiblePasswordField.getText().trim().isEmpty();
            } else {
                passwordValid = !passwordField.getText().trim().isEmpty();
            }

            addButton.setDisable(!(serviceValid && usernameValid && passwordValid));
        };

        serviceField.textProperty().addListener((obs, oldVal, newVal) -> checkFields.run());
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> checkFields.run());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> checkFields.run());
        visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> checkFields.run());

        // Первоначальная проверка
        checkFields.run();
    }

    /**
     * Создает объект PasswordEntry из данных формы.
     *
     * @return объект PasswordEntry или null в случае ошибки
     */
    private PasswordEntry createPasswordEntryFromForm() {
        try {
            // Получаем данные из формы
            String service = serviceField.getText().trim();
            String username = usernameField.getText().trim();

            // Получаем пароль (может быть в разных полях)
            String password;
            if (showPasswordCheck.isSelected()) {
                password = visiblePasswordField.getText();
            } else {
                password = passwordField.getText();
            }

            String encryptionDesc = encryptionCombo.getValue();

            // Преобразуем описание в EncryptionType
            EncryptionType encryptionType = EncryptionType.SALTED;
            for (EncryptionType type : EncryptionType.values()) {
                if (type.getDescription().equals(encryptionDesc)) {
                    encryptionType = type;
                    break;
                }
            }

            // Проверяем обязательные поля
            if (service.isEmpty() || username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Заполните все поля!");
                return null;
            }

            // Создаем временный объект PasswordEntry (пароль еще не зашифрован)
            return new PasswordEntry(service, username, password, encryptionType);

        } catch (Exception e) {
            System.err.println("Ошибка при создании PasswordEntry: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось создать запись: " + e.getMessage());
            return null;
        }
    }

    /**
     * Показывает диалоговое окно с сообщением.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}