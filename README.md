# Banking Platform

Микросервисное приложение "Банк" на Spring Boot и Spring Cloud.

Приложение состоит из фронта `bank-ui`, API Gateway, Service Discovery, Config Server и четырёх бизнес-сервисов:
- `accounts`
- `cash`
- `transfers`
- `notifications`

Пользователь может:
- просматривать и редактировать данные своего аккаунта;
- пополнять счёт и снимать деньги;
- переводить деньги другому пользователю.

## Технологии

- Java 21
- Maven (multimodule project)
- Spring Boot 3
- Spring Cloud
  - Spring Cloud Gateway
  - Spring Cloud Config
  - Eureka Client / Eureka Server
  - Spring Cloud Contract
- Spring Security + OAuth 2.0 / OIDC
- Keycloak
- Spring MVC / Thymeleaf (`bank-ui`)
- Spring Data JPA / Hibernate
- PostgreSQL
- Testcontainers
- JUnit 5 / Spring Boot Test / MockMvc / Mockito
- Docker / Docker Compose

## Архитектура

### Модули

- `bank-ui` — веб-интерфейс пользователя
- `gateway` — единая точка входа во внутренние API
- `config-server` — внешний конфиг для сервисов
- `eureka-server` — service discovery
- `accounts` — аккаунты и баланс
- `cash` — пополнение и снятие денег
- `transfers` — переводы между счетами
- `notifications` — логирование уведомлений о произведённых операциях
- `keycloak/` — инициализация realm/client'ов Keycloak

### Взаимодействие

- `bank-ui` ходит в backend только через `gateway`
- backend-сервисы регистрируются в `eureka-server`
- backend-сервисы получают конфиг из `config-server`
- межсервисные вызовы защищены OAuth 2.0 Client Credentials Flow
- пользовательская авторизация во фронте построена на Authorization Code Flow

### База данных

Используется одна PostgreSQL-база, но сервисы разделены по схемам:
- `accounts`
- `cash`
- `transfers`
- `notifications`

## Порты

При запуске через `docker-compose.yml` используются:

- `8080` — `bank-ui`
- `8081` — `config-server`
- `8082` — `eureka-server`
- `8083` — `keycloak`
- `8084` — `gateway`
- `8085` — `accounts`
- `8086` — `cash`
- `8087` — `transfers`
- `8088` — `notifications`
- `5432` — PostgreSQL

## Требования для запуска

- JDK 21
- Docker Desktop / Docker Engine

## Запуск через Docker Compose

### 1. Подготовить `.env`

Скопируйте `.env.example` в `.env`:

```bash
cp .env.example .env
```

Для Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

По умолчанию там уже есть рабочие значения для локального запуска, но при желании, к примеру, можно поменять пароли.

### 2. На первом запуске включить SQL-инициализацию

Перед самым первым запуском поменяйте `spring.sql.init.mode` с `never` на `always` в:

- [config-server/src/main/resources/config-repo/accounts.yml](config-server/src/main/resources/config-repo/accounts.yml)
- [config-server/src/main/resources/config-repo/cash.yml](config-server/src/main/resources/config-repo/cash.yml)
- [config-server/src/main/resources/config-repo/transfers.yml](config-server/src/main/resources/config-repo/transfers.yml)
- [config-server/src/main/resources/config-repo/notifications.yml](config-server/src/main/resources/config-repo/notifications.yml)

После того как таблицы и схемы будут созданы, можно вернуть значения обратно на `never`.

### 3. Собрать jar-файлы

```bash
./mvnw clean package -DskipTests
```

Для Windows PowerShell:

```powershell
.\mvnw.cmd clean package -DskipTests
```

### 4. Поднять систему

```bash
docker compose up --build -d
```

После старта приложение будет доступно по адресу:

```text
http://localhost:8080
```

## Локальный запуск без Docker Compose

Минимальная последовательность такая:

1. Запустить PostgreSQL
2. Запустить Keycloak
3. Запустить `config-server`
4. Запустить `eureka-server`
5. Запустить `gateway`
6. Запустить `accounts`, `cash`, `transfers`, `notifications`
7. Запустить `bank-ui`

### Сборка всего проекта

```bash
./mvnw clean package -DskipTests
```

Для Windows PowerShell:

```powershell
.\mvnw.cmd clean package -DskipTests
```

### Запуск отдельного модуля

Пример:

```bash
./mvnw -pl config-server spring-boot:run
```

или:

```bash
./mvnw -pl bank-ui spring-boot:run
```

Для Windows PowerShell:

```powershell
.\mvnw.cmd -pl config-server spring-boot:run
.\mvnw.cmd -pl bank-ui spring-boot:run
```

Если нужен запуск jar:

```bash
./mvnw clean package
java -jar bank-ui/target/bank-ui-1.0-SNAPSHOT.jar
```

## Тесты

В проекте есть:
- unit tests
- integration / e2e tests
- contract tests producer-side
- contract tests consumer-side

### Запуск всех тестов

```bash
./mvnw test
```

Для Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Для e2e-тестов backend-сервисов нужен рабочий Docker, потому что PostgreSQL поднимается через Testcontainers.

## Что ещё важно знать

- `gateway` пробрасывает пользовательский JWT в backend-сервисы.
- `bank-ui` использует `gateway`, а не прямые вызовы бизнес-сервисов.
- backend-сервисы вызывают друг друга напрямую.
- операции, о которых в реальном банке обычно отправлялись бы уведомления, в этом проекте просто логируются сервисом `notifications`.
- cleanup/retry/notification background processing реализован cron/fixed-delay задачами в соответствующих сервисах.

## Полезные команды

Прогнать тесты и собрать всё:

```bash
./mvnw clean package
```

Запустить тесты одного сервиса:

```bash
./mvnw -pl accounts test
```

Поднять всё в Docker:

```bash
./mvnw clean package -DskipTests
docker compose up --build -d
```

Остановить всё:

```bash
docker compose down
```
