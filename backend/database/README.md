# База данных calorie_tracker

## Подключение
```
URL:      jdbc:postgresql://localhost:5432/calorie_tracker
User:     postgres
Password: <передаётся отдельно>
```

Если бэкенд в Docker — заменить `localhost` на `postgres` (имя контейнера).

## Восстановление БД

```bash
# 1. Создать базу
docker exec -it postgres psql -U postgres -c "CREATE DATABASE calorie_tracker;"

# 2.
docker exec -i postgres psql -U postgres -d calorie_tracker < database/schema.sql
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
