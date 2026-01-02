#!/bin/bash
echo "===================================================="
echo "  Настройка базы данных Bank Cards Application"
echo "===================================================="
echo ""

echo "1. Остановка существующего контейнера..."
docker stop bankcards-db 2>/dev/null
docker rm bankcards-db 2>/dev/null

echo ""
echo "2. Запуск PostgreSQL контейнера..."
docker run -d \
  --name bankcards-db \
  -p 5433:5432 \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=bankcards \
  postgres:16

echo ""
echo "3. Ожидание запуска базы данных (15 секунд)..."
sleep 15

echo ""
echo "4. Создание таблиц и тестовых данных..."
echo "   Используется файл: init-database.sql"

docker exec -i bankcards-db psql -U postgres -d bankcards < init-database.sql

echo ""
echo "5. Проверка подключения к базе данных..."
docker exec bankcards-db psql -U postgres -d bankcards -c "SELECT 'Подключение успешно!' as status;"

echo ""
echo "===================================================="
echo "  База данных настроена!"
echo "===================================================="
echo ""
echo "  Данные для подключения:"
echo "  - Хост: localhost:5433"
echo "  - База данных: bankcards"
echo "  - Пользователь: postgres"
echo "  - Пароль: postgres"
echo ""
echo "  Тестовые пользователи (пароль для всех: password123):"
echo "  - Администратор: admin@bank.com"
echo "  - Пользователь 1: user1@bank.com"
echo "  - Пользователь 2: user2@bank.com"
echo ""
echo "  Для запуска приложения выполните: ./gradlew bootRun"
echo "===================================================="
echo ""