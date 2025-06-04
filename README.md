# Database Sync Server

Сервер для синхронизации базы данных Room между устройствами.

## Требования

- Java 11 или выше
- Maven

## Запуск

1. Клонируйте репозиторий
2. Перейдите в директорию проекта
3. Выполните команду:
```bash
mvn spring-boot:run
```

Сервер будет доступен по адресу: http://localhost:8080

## API Endpoints

- GET /database/download - Скачать базу данных
- POST /database/upload - Загрузить базу данных

## Развертывание на Render.com

1. Создайте новый Web Service на Render.com
2. Подключите репозиторий GitHub
3. Укажите следующие настройки:
   - Build Command: `mvn clean package`
   - Start Command: `java -jar target/database-sync-server-1.0-SNAPSHOT.jar` 