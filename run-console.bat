@echo off
chcp 65001 > nul
echo ========================================
echo    Менеджер паролей - Консольный режим
echo ========================================
echo.
java -Dfile.encoding=UTF-8 ^
     -Dconsole.encoding=UTF-8 ^
     -Dsun.stdout.encoding=UTF-8 ^
     -jar "build/libs/PasswordManagerGUI-all.jar" console
echo.
pause
