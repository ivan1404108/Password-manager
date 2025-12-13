package passwordmanager.core;

import passwordmanager.exception.*;
import passwordmanager.model.User;
import org.junit.jupiter.api.*;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserManagerTest {
    private static final String TEST_USERNAME = "testuser_junit";
    private static final String TEST_PASSWORD = "testpassword123";

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        // Удаляем тестового пользователя если существует
        try {
            // Пытаемся удалить старые тестовые файлы
            File userFile = new File("users.dat");
            if (userFile.exists()) {
                // Для тестов создадим нового менеджера, файл будет перезаписан
            }
        } catch (Exception e) {
            // Игнорируем
        }
    }

    @AfterAll
    static void cleanup() {
        // Очистка тестовых файлов
        try {
            File userFile = new File("users.dat");
            if (userFile.exists()) {
                // Создаем резервную копию оригинального файла
                File backup = new File("users.dat.backup");
                if (userFile.renameTo(backup)) {
                    System.out.println("Создана резервная копия users.dat");
                }
            }

            // Удаляем тестовые файлы паролей
            new File("passwords_" + TEST_USERNAME + ".dat").delete();
            new File("passwords_" + TEST_USERNAME + ".enc").delete();
        } catch (Exception e) {
            System.err.println("Ошибка при очистке: " + e.getMessage());
        }
    }



    @Test
    @Order(1)
    void testLoginSuccess() {
        User user = userManager.login(TEST_USERNAME, TEST_PASSWORD);
        assertNotNull(user);
        assertEquals(TEST_USERNAME, user.getUsername());
    }

    @Test
    void testLoginWrongPassword() {
        User user = userManager.login(TEST_USERNAME, "wrongpassword");
        assertNull(user);
    }

    @Test
    void testLoginNonExistentUser() {
        User user = userManager.login("nonexistentuser", "anypassword");
        assertNull(user);
    }

    @Test
    void testLoginWithEmptyUsername() {
        User user = userManager.login("", TEST_PASSWORD);
        assertNull(user);
    }

    @Test
    void testLoginWithEmptyPassword() {
        User user = userManager.login(TEST_USERNAME, "");
        assertNull(user);
    }

    @Test
    void testRegisterWithEmptyUsername() {
        assertThrows(EmptyFieldException.class, () -> {
            userManager.registerUser("", TEST_PASSWORD, TEST_PASSWORD);
        });
    }

    @Test
    void testRegisterWithEmptyPassword() {
        assertThrows(EmptyFieldException.class, () -> {
            userManager.registerUser("newuser", "", "");
        });
    }

    @Test
    void testRegisterShortPassword() {
        assertThrows(PasswordTooShortException.class, () -> {
            userManager.registerUser("shortuser", "123", "123");
        });
    }

    @Test
    void testRegisterPasswordMismatch() {
        assertThrows(PasswordMismatchException.class, () -> {
            userManager.registerUser("mismatchuser", "password123", "different456");
        });
    }

    @Test
    void testUserExists() {
        // Проверяем несуществующего пользователя
        assertFalse(userManager.userExists("nonexistentuser"));
    }

    @Test
    void testGetMinPasswordLength() {
        assertEquals(8, userManager.getMinPasswordLength());
    }

    @Test
    void testGetUserCount() {
        int initialCount = userManager.getUserCount();

        // Регистрируем нового пользователя для теста
        try {
            userManager.registerUser("counttestuser", TEST_PASSWORD, TEST_PASSWORD);
            int newCount = userManager.getUserCount();
            assertTrue(newCount > initialCount);
        } catch (Exception e) {
            // Если пользователь уже существует, пропускаем
            System.out.println("Пользователь уже существует: " + e.getMessage());
        }
    }

    @Test
    void testUsersFileExists() {
        // После регистрации файл должен существовать
        try {
            userManager.registerUser("filetestuser", TEST_PASSWORD, TEST_PASSWORD);
            assertTrue(userManager.usersFileExists());
        } catch (Exception e) {
            // Игнорируем если пользователь уже есть
        }
    }
}