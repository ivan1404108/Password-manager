@echo off
chcp 65001 > nul
echo ========================================
echo    Менеджер паролей - Графический режим
echo ========================================
echo.
java --module-path "%PATH_TO_FX%" ^
     --add-modules javafx.controls,javafx.fxml ^
     -Dfile.encoding=UTF-8 ^
     -Dconsole.encoding=UTF-8 ^
     -Dsun.stdout.encoding=UTF-8 ^
     -jar "build/libs/PasswordManagerGUI-all.jar" gui
echo.
pause
