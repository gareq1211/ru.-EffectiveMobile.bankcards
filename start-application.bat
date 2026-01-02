@echo off
echo ====================================================
echo   Запуск Bank Cards Application
echo ====================================================
echo.

echo 1. Проверка запущенной базы данных...
docker ps | findstr "bankcards-db" >nul
if errorlevel 1 (
    echo Ошибка: База данных не запущена!
    echo Запустите сначала setup-database.bat
    pause
    exit /b 1
)

echo.
echo 2. Сборка приложения (может занять некоторое время)...
call gradlew clean build -x test

echo.
echo 3. Запуск приложения...
echo    Приложение будет доступно по адресу: http://localhost:8082
echo    Swagger UI: http://localhost:8082/swagger-ui.html
echo    Авторизация: admin@bank.com / password123
echo.

echo ====================================================
echo   Информация для тестирования:
echo ====================================================
echo.
echo   1. Откройте браузер: http://localhost:8082/swagger-ui.html
echo.
echo   2. Получите JWT токен:
echo      - Endpoint: POST /auth/login
echo      - Body: {"email": "admin@bank.com", "password": "password123"}
echo      - Сохраните полученный token
echo.
echo   3. Используйте токен для авторизации:
echo      - Нажмите "Authorize" в Swagger UI
echo      - Введите: Bearer <ваш_токен>
echo.
echo   4. Тестируйте основные функции:
echo      - GET /cards/my - мои карты
echo      - POST /cards/transfers - перевод между картами
echo      - GET /users/me - информация о текущем пользователе
echo.
echo   5. Административные функции (только для admin@bank.com):
echo      - POST /cards - создание новой карты
echo      - GET /cards/admin/all - все карты в системе
echo      - GET /users - список всех пользователей
echo.
echo ====================================================
echo.

timeout /t 5 /nobreak >nul
java -jar "build\libs\bankcards-0.0.1-SNAPSHOT.jar"

pause
