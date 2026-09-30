# ER-диаграмма базы данных

Документ описывает структуру базы данных проекта по подсчёту КБЖУ с интеграцией OpenFoodFacts.

---

## 1. Обзор

База данных состоит из **4 таблиц**:

| Таблица | Назначение |
|---|---|
| `User` | Аккаунты пользователей |
| `Product` | Продукты из OpenFoodFacts и созданные вручную |
| `DiaryEntry` | Записи о съеденном |

---

## 2. Схема связей

**Описание связей:**

| Связь | Смысл |
|---|---|
| `User → UserGoal` | 1 : N  У пользователя несколько целей на разные периоды |
| `User → DiaryEntry` | 1 : N  Пользователь ведёт много записей |
| `User → Product` | 1 : N Пользователь может создать много продуктов (nullable) |
| `Product → DiaryEntry` | 1 : N  Один продукт используется во многих записях |

---

## 3. Таблицы и атрибуты

### 3.1. User

| Поле | Тип | Ограничения | Описание |
|---|---|---|---|
| `id` | int | PK, increment | Идентификатор |
| `email` | varchar(255) | NOT NULL, UNIQUE | Email для входа |
| `password_hash` | varchar(255) | NOT NULL | Хэш пароля |
| `role` | varchar(20) | NOT NULL | `user` или `admin` |
| `created_at` | timestamp | NOT NULL | Дата регистрации |

### 3.2. Product

| Поле | Тип | Ограничения | Описание |
|---|---|---|---|
| `id` | int | PK, increment | Идентификатор |
| `name` | varchar(255) | NOT NULL | Название продукта |
| `calories_per_100g` | float | NOT NULL | Калории на 100 г |
| `proteins_per_100g` | float | NOT NULL | Белки на 100 г |
| `fats_per_100g` | float | NOT NULL | Жиры на 100 г |
| `carbs_per_100g` | float | NOT NULL | Углеводы на 100 г |
| `source` | varchar(20) | NOT NULL | `openfoodfacts` или `user` |
| `external_id` | varchar(255) | NULL | ID в OpenFoodFacts |
| `created_by_user_id` | int | FK → User.id, NULL | Кто создал (для пользовательских) |
| `status` | varchar(20) | NOT NULL | `pending`, `approved`, `rejected` |
| `created_at` | timestamp | NOT NULL | Дата создания |
| `updated_at` | timestamp | NOT NULL | Дата обновления |

### 3.3. DiaryEntry

| Поле | Тип | Ограничения | Описание |
|---|---|---|---|
| `id` | int | PK, increment | Идентификатор |
| `user_id` | int | FK → User.id, NOT NULL | Владелец записи |
| `product_id` | int | FK → Product.id, NOT NULL | Съеденный продукт |
| `date` | date | NOT NULL | Дата приёма |
| `meal_type` | varchar(20) | NOT NULL | `breakfast`, `lunch`, `dinner`, `snack` |
| `eaten_at` | timestamp | NULL | Точное время (опционально) |
| `grams` | float | NOT NULL | Вес порции |
| `calories` | float | NOT NULL | Снимок: калории на порцию |
| `proteins` | float | NOT NULL | Снимок: белки на порцию |
| `fats` | float | NOT NULL | Снимок: жиры на порцию |
| `carbs` | float | NOT NULL | Снимок: углеводы на порцию |
| `product_updated` | boolean | NOT NULL, default false | Продукт обновился в API |
| `created_at` | timestamp | NOT NULL | Дата создания |
| `updated_at` | timestamp | NOT NULL | Дата обновления |

---

## 4. Индексы

| Индекс | Таблица | Зачем |
|---|---|---|
| `User(email)` UNIQUE | User | Быстрый вход, запрет дубликатов |
| `(source, external_id)` UNIQUE | Product | Запрет дубликатов API-продуктов |
| `name` | Product | Поиск дубликатов через LIKE |
| `(status, name)` | Product | Показ approved + свои pending |
| `(user_id, date)` | DiaryEntry | Сводка за день и статистика |
| `product_id` | DiaryEntry | Замена дубликатов |

---

## 5. Ключевые решения

### 5.1. Одна таблица Product

API-продукты и пользовательские хранятся вместе, различаются полем `source`. Это упрощает связи и запросы: `DiaryEntry` ссылается на одну таблицу.

### 5.2. Снимок КБЖУ в DiaryEntry

В `DiaryEntry` хранятся `calories`, `proteins`, `fats`, `carbs` на момент записи. Если продукт обновится в API, история не «поедет».

### 5.3. Флаг product_updated

Если API обновил продукт, а КБЖУ в дневнике — старые, ставится `product_updated = true`. Пользователь сам решает, пересчитывать или нет.

### 5.4. meal_type — поле, а не таблица

Приём пищи — просто enum-поле. Нет дополнительных атрибутов, отдельная таблица не нужна.

---

## 6. Логика приложения

1. **Поиск продукта:** сначала БД (`status IN ('approved','pending') AND lower(name) LIKE %query%`), если мало — API.
2. **Создание продукта:** нормализация названия → LIKE-поиск дубликатов → «возможно, вы имели в виду этот продукт» → `status = pending`.
3. **Добавление в дневник:** снимок КБЖУ на момент записи.
4. **Редактирование граммовки:** пересчёт из текущего продукта, сброс `product_updated`.
5. **Обновление продукта в API:**
   - КБЖУ совпали (с погрешностью) → авто-замена ссылок в `DiaryEntry`.
   - КБЖУ не совпали → `product_updated = true`, пользователь решает.
6. **Валидация КБЖУ:** `|calories - (4*Б + 9*Ж + 4*У)| <= 10%`.
7. **Статистика:** агрегат по `DiaryEntry` за период; дни без записей — «нет данных».
