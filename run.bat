@echo off
echo Starting Bank Cards Application...
echo.

echo 1. Stopping existing containers...
docker-compose down

echo.
echo 2. Starting PostgreSQL database...
docker-compose up db -d

echo.
echo 3. Waiting for database to be ready...
timeout /t 10 /nobreak > nul

echo.
echo 4. Building application...
call gradlew clean build -x test

echo.
echo 5. Starting application...
java -jar "build\libs\bankcards-0.0.1-SNAPSHOT.jar"

pause