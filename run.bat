@echo off
chcp 65001 > nul
echo ========================================
echo    Менеджер паролей
echo ========================================
echo.
echo Выберите режим:
echo   1 - Консольный режим
echo   2 - Графический интерфейс
echo.
set /p choice="Ваш выбор (1 или 2): "
if "%%choice%%"=="1" (
    java -Dfile.encoding=UTF-8 -jar "build/libs/PasswordManagerGUI-all.jar" console
) else if "%%choice%%"=="2" (
    rem Попробуем найти JavaFX автоматически
    where javafx-sdk-17 >nul 2>nul
    if %%errorlevel%%==0 (
        for /f "tokens=*" %%i in ('where javafx-sdk-17') do set FX_PATH=%%i
        java --module-path "%%FX_PATH%%\lib" --add-modules javafx.controls,javafx.fxml -Dfile.encoding=UTF-8 -jar "build/libs/PasswordManagerGUI-all.jar" gui
    ) else (
        echo JavaFX не найден! Установите JavaFX SDK 17+
        echo Скачать можно с: https://gluonhq.com/products/javafx/
        echo И укажите путь в переменной PATH_TO_FX
    )
) else (
    echo Неверный выбор
)
pause
