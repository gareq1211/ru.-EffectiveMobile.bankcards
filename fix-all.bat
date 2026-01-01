@echo off
echo === COMPLETE SYSTEM RESET ===

echo 1. Stopping everything...
docker-compose down -v
taskkill /F /IM java.exe 2>nul

echo 2. Cleaning Gradle...
rd /s /q build 2>nul
rd /s /q .gradle 2>nul

echo 3. Starting fresh database...
docker-compose up -d db
timeout /t 10 /nobreak

echo 4. Creating database...
docker exec bankcards-db psql -U postgres -c "CREATE DATABASE bankcards;" 2>nul || echo Database exists

echo 5. Testing connection...
docker exec bankcards-db psql -U postgres -d bankcards -c "SELECT 'Database READY' as status;"

echo 6. Building application (without tests)...
call gradlew clean build -x test

echo 7. Creating simple config...
(
echo spring:
echo   datasource:
echo     url: jdbc:postgresql://localhost:5432/bankcards
echo     username: postgres
echo     password: postgres
echo   jpa:
echo     hibernate:
echo       ddl-auto: create
echo   liquibase:
echo     enabled: false
) > application-simple.yml

echo 8. Starting application...
echo === LOOK AT THE LOGS BELOW ===
call gradlew bootRun --args='--spring.config.location=classpath:/application-simple.yml'