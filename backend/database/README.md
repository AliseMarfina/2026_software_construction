# База данных calorie_tracker

## Подключение
```
URL:      jdbc:postgresql://localhost:5432/calorie_tracker
User:     postgres
Password: <передаётся отдельно>
```

Если бэкенд в Docker — заменить `localhost` на `postgres` (имя контейнера).

## Запуск из корня репозитория

Откройте Docker Desktop, затем выполните:

```bash
docker compose -f backend/database/docker-compose.yml up -d
docker compose -f backend/database/docker-compose.yml ps
```

На пустом volume PostgreSQL последовательно выполнит `database/schema.sql`
и `backend/database/migrations/001_release_diary.sql`. Миграция приводит схему
к текущему контракту бэкенда, включая `products.updated_at` и поля дневника.
Данные сохраняются в volume `postgres-data`.

По умолчанию локальный пароль — `postgres`. Если задан `POSTGRES_PASSWORD`,
передайте тот же пароль бэкенду через `DB_PASSWORD`.
Порт 5432 должен быть свободен от другого локального PostgreSQL.

## Обновление существующей базы

При существующем volume скрипты инициализации автоматически не повторяются.
Сначала сохраните резервную копию (она содержит данные пользователей):

```bash
docker compose -f backend/database/docker-compose.yml exec -T postgres pg_dump -U postgres -d calorie_tracker -Fc > calorie-tracker-backup.dump
```

Храните копию вне Git. Затем примените миграцию:

```bash
docker compose -f backend/database/docker-compose.yml exec -T postgres psql -v ON_ERROR_STOP=1 -U postgres -d calorie_tracker < backend/database/migrations/001_release_diary.sql
```

Миграция сохраняет строки. Старым записям без типа назначается `snack`,
а отсутствующее время приёма пищи заполняется временем создания записи.
Повторное применение допускается. Для остановки без удаления данных:

```bash
docker compose -f backend/database/docker-compose.yml stop
```

## Проверить

```bash
docker exec -it postgres psql -U postgres -d calorie_tracker -c "\dt"
```

Ожидаемый вывод: `diary_entries`, `products`, `users`.

## Структура БД

- **users** — пользователи (email, password_hash)
- **products** — продукты (публичные из API + кастомные пользователей)
- **diary_entries** — записи дневника со снимком КБЖУ на момент записи

## Подготовка к демо

Перед демонстрацией нужно:
1. Зарегистрировать двух пользователей через UI (для Сценария 4)
2. Убедиться, что OpenFoodFacts API доступен
3. Если API недоступен — создать продукт вручную через UI (Сценарий 3)

## Примечания
- `schema.sql` содержит структуру
- Все данные создаются через API/UI
- Для GIN-индекса поиска требуется расширение `pg_trgm` (включено в schema.sql)
