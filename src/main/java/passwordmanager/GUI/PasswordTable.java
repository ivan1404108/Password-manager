package passwordmanager.GUI;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import passwordmanager.model.PasswordEntry;

/**
 * Компонент таблицы для отображения паролей.
 */
public class PasswordTable {

    /**
     * Создает и настраивает таблицу для отображения паролей.
     */
    public static TableView<PasswordEntry> createPasswordTable() {
        TableView<PasswordEntry> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Колонка с номером
        TableColumn<PasswordEntry, Integer> numberColumn = new TableColumn<>("#");
        numberColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createObjectBinding(() ->
                        table.getItems().indexOf(cellData.getValue()) + 1
                )
        );
        numberColumn.setStyle("-fx-alignment: CENTER;");
        numberColumn.setMinWidth(40);
        numberColumn.setMaxWidth(60);

        // Колонка с названием сервиса
        TableColumn<PasswordEntry, String> serviceColumn = new TableColumn<>("Сервис");
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        serviceColumn.setMinWidth(120);

        // Колонка с логином
        TableColumn<PasswordEntry, String> usernameColumn = new TableColumn<>("Логин/Email");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setMinWidth(150);

        // Колонка с паролем (скрытый по умолчанию)
        TableColumn<PasswordEntry, String> passwordColumn = new TableColumn<>("Пароль");
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        passwordColumn.setMinWidth(150);

        // Кастомная ячейка для отображения пароля
        passwordColumn.setCellFactory(column -> new TableCell<PasswordEntry, String>() {
            private final Button showButton = new Button("👁");
            private final HBox container = new HBox();
            private boolean isPasswordVisible = false;
            private String originalPassword = "";

            {
                showButton.setStyle("-fx-background-color: transparent; -fx-padding: 2 5 2 5;");
                showButton.setOnAction(event -> {
                    PasswordEntry entry = getTableView().getItems().get(getIndex());
                    if (entry != null) {
                        if (isPasswordVisible) {
                            // Скрываем пароль
                            setText("••••••••");
                            showButton.setText("👁");
                            isPasswordVisible = false;
                        } else {
                            // Показываем пароль
                            setText(entry.getPassword());
                            showButton.setText("🙈");
                            isPasswordVisible = true;
                        }
                    }
                });

                container.getChildren().add(showButton);
                container.setSpacing(5);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isPasswordVisible) {
                        setText(item);
                    } else {
                        setText("••••••••");
                    }
                    setGraphic(container);
                }
            }
        });

        // Колонка с методом шифрования
        TableColumn<PasswordEntry, String> encryptionColumn = new TableColumn<>("Шифрование");
        encryptionColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(() ->
                        cellData.getValue().getEncryptionType().getDescription()
                )
        );
        encryptionColumn.setMinWidth(100);

        // Добавляем все колонки в таблицу
        table.getColumns().addAll(
                numberColumn, serviceColumn, usernameColumn, passwordColumn, encryptionColumn
        );

        // Настраиваем стиль таблицы
        setupTableStyle(table);

        return table;
    }

    /**
     * Настраивает стиль таблицы.
     *
     * @param table таблица для настройки
     */
    private static void setupTableStyle(TableView<PasswordEntry> table) {
        // Альтернативная раскраска строк
        table.setRowFactory(tv -> new javafx.scene.control.TableRow<PasswordEntry>() {
            @Override
            protected void updateItem(PasswordEntry item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else {
                    // Подсвечиваем строки с незашифрованными паролями
                    if (item.getEncryptionType().name().equals("PLAIN")) {
                        setStyle("-fx-background-color: #fff3cd;"); // Желтый для предупреждения
                    } else {
                        // Чередование цветов для четных/нечетных строк
                        if (getIndex() % 2 == 0) {
                            setStyle("-fx-background-color: #f9f9f9;");
                        } else {
                            setStyle("-fx-background-color: white;");
                        }
                    }
                }
            }
        });

        // Контекстное меню для таблицы
        table.setContextMenu(createContextMenu(table));
    }

    /**
     * Создает контекстное меню для таблицы.
     *
     * @param table таблица для которой создается меню
     * @return контекстное меню
     */
    private static javafx.scene.control.ContextMenu createContextMenu(TableView<PasswordEntry> table) {
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();

        // Пункт меню "Копировать пароль"
        javafx.scene.control.MenuItem copyPasswordItem = new javafx.scene.control.MenuItem("Копировать пароль");
        copyPasswordItem.setOnAction(e -> {
            PasswordEntry selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(selected.getPassword());
                clipboard.setContent(content);
            }
        });

        // Пункт меню "Копировать логин"
        javafx.scene.control.MenuItem copyUsernameItem = new javafx.scene.control.MenuItem("Копировать логин");
        copyUsernameItem.setOnAction(e -> {
            PasswordEntry selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(selected.getUsername());
                clipboard.setContent(content);
            }
        });

        // Разделитель
        javafx.scene.control.SeparatorMenuItem separator = new javafx.scene.control.SeparatorMenuItem();

        // Пункт меню "Удалить"
        javafx.scene.control.MenuItem deleteItem = new javafx.scene.control.MenuItem("Удалить");
        deleteItem.setStyle("-fx-text-fill: red;");
        deleteItem.setOnAction(e -> {
            // Обработка удаления будет в MainWindow
        });

        contextMenu.getItems().addAll(copyPasswordItem, copyUsernameItem, separator, deleteItem);

        // Показываем меню только когда есть выбранный элемент
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<PasswordEntry> row = new javafx.scene.control.TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });

        return contextMenu;
    }
}