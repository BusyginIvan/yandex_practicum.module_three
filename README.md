# Banking Platform

Микросервисное приложение "Банк" на Spring Boot.

Проект разворачивается в Kubernetes через Helm.  
Конфигурация приложений хранится в Helm chart'ах и попадает в контейнеры через `ConfigMap` и `Secret`.

## Состав системы

- `bank-ui` — пользовательский веб-интерфейс
- `gateway` — единая точка входа во внутренние API
- `accounts` — профиль и баланс
- `cash` — пополнение и снятие денег
- `transfers` — переводы
- `notifications` — логирование уведомлений об операциях
- `keycloak` — авторизация и аутентификация
- `postgres` — общая база данных

## Технологии

- Java 21
- Maven Wrapper
- Spring Boot 3
- Spring Cloud Gateway
- Spring Security + OAuth 2.0 / OIDC
- Keycloak
- Spring MVC / Thymeleaf
- Spring Data JPA / Hibernate
- PostgreSQL
- Helm
- Kubernetes
- Docker Desktop
- Testcontainers
- Spring Cloud Contract
- JUnit 5 / MockMvc / Mockito

## Архитектура

### Kubernetes

- один namespace: `banking-platform`
- один umbrella chart: `helm/`
- три дочерних chart'а:
  - `postgres`
  - `keycloak`
  - `apps` для `bank-ui`, `gateway`, `accounts`, `cash`, `transfers`, `notifications`
- `postgres` разворачивается как `StatefulSet`
- `bank-ui` и `keycloak` доступны снаружи через `NodePort`

### Конфигурация

- общий конфиг для Spring-приложений: [helm/application.yml](helm/application.yml)
- конфиги приложений: `helm/charts/apps/configs/*.yml`
- общий `ConfigMap` создаётся в зонтичном чарте
- service-specific `ConfigMap`-ы для приложений создаются в чарте `apps`
- секреты задаются в `helm/values.secrets.yaml`

### База данных

Используется одна PostgreSQL-база `bank`, разделённая по схемам:

- `accounts`
- `cash`
- `transfers`
- `notifications`

## Требования

- Docker Desktop
- включённый Kubernetes в Docker Desktop
- `helm`
- `task` (опционально)

Используйте Docker Desktop Kubernetes с provisioner = kubeadm. С kind NodePort-доступ через localhost:30080 и localhost:30081 может не работать.

## Установка инструментов

### Docker Desktop

Установить Docker Desktop:  
<https://www.docker.com/products/docker-desktop/>

В настройках Docker Desktop:

1. открыть `Settings -> Kubernetes`
2. включить `Enable Kubernetes`
3. выбрать **`kubeadm`**
4. дождаться, пока кластер поднимется

Проверка:

```bash
kubectl get nodes
```

### Helm

Если `helm` не установлен, поставить его любым удобным способом.

**Windows:**

```bash
winget install Helm.Helm
```

**macOS:**

```bash
brew install helm
```

**Linux:**

```bash
sudo snap install helm --classic
```

Если ни один из этих вариантов не подходит, можно поставить `helm` по официальной инструкции:  
<https://helm.sh/docs/intro/install/>

Проверка:

```bash
helm version
```

### Task

`Task` нужен для команд из `Taskfile.yml`.

**Windows:**

```bash
winget install Task.Task
```

**macOS:**

```bash
brew install go-task/tap/go-task
```

**Linux:**

```bash
sudo snap install task --classic
```

Если ни один из этих вариантов не подходит, можно поставить `Task` по официальной инструкции:  
<https://taskfile.dev/docs/installation>

Проверка:

```bash
task --version
```

## Подготовка секретов

Нужно создать локальный файл:

```text
helm/values.secrets.yaml
```

Для этого можно скопировать и переименовать шаблон [helm/values.secrets.example.yaml](helm/values.secrets.example.yaml).

По умолчанию в шаблоне уже есть рабочая структура и локальные значения для примера.

## Первый запуск

Перед первым запуском нужно поменять в [helm/application.yml](helm/application.yml) значение:

- `postgres.sql.init.mode: never` -> `postgres.sql.init.mode: always`

Это нужно, чтобы приложения выполнили инициализацию схем и данных в PostgreSQL.

## Основной запуск

Полный цикл:

```bash
task up
```

Эта команда:

1. собирает все jar-файлы
2. собирает Docker-образы
3. обновляет Helm release `banking-platform`

После запуска сервисы доступны по адресам:

- `http://localhost:30080` — `bank-ui`
- `http://localhost:30081` — `keycloak`

## Основные команды

Собрать всё:

```bash
task build
```

Задеплоить Helm release:

```bash
task deploy
```

Удалить Helm release:

```bash
task uninstall
```

Полностью удалить namespace `banking-platform` вместе со всеми ресурсами и данными:

```bash
task clean
```

Перезапустить все `Deployment` и `StatefulSet` в namespace:

```bash
task restart
```

Пересобрать и задеплоить всё:

```bash
task up
```

## Тесты

### Java-тесты

```bash
task test:java
```

В проекте есть:

- unit tests
- e2e / integration tests
- contract tests

Для backend e2e-тестов используется Testcontainers, поэтому Docker должен быть доступен.

### Helm smoke tests

После deploy можно прогнать chart-level smoke tests:

```bash
task test:helm
```

Эти тесты проверяют:

- доступность `actuator/health` у Spring-сервисов
- доступность OIDC metadata у `keycloak`
- готовность `postgres` через `pg_isready`

### Все тесты

```bash
task test
```

## Полезные команды Kubernetes

Посмотреть pod'ы:

```bash
kubectl get pods -n banking-platform
```

Посмотреть сервисы:

```bash
kubectl get svc -n banking-platform
```

Посмотреть логи pod'а:

```bash
kubectl logs -n banking-platform <pod-name>
```

Логи deployment:

```bash
kubectl logs -n banking-platform deploy/bank-ui
```

## Запасной вариант без Task

Если не хочется использовать `Taskfile`, базовые команды такие:

Сборка:

```bash
./mvnw clean package -DskipTests
docker build -t accounts:latest -f accounts/Dockerfile .
docker build -t bank-ui:latest -f bank-ui/Dockerfile .
docker build -t cash:latest -f cash/Dockerfile .
docker build -t gateway:latest -f gateway/Dockerfile .
docker build -t notifications:latest -f notifications/Dockerfile .
docker build -t transfers:latest -f transfers/Dockerfile .
docker build -t bank-keycloak:latest -f keycloak/Dockerfile .
```

Deploy:

```bash
helm dependency build helm
helm upgrade --install banking-platform helm --namespace banking-platform --create-namespace -f helm/values.yaml -f helm/values.secrets.yaml
```

Удалить только Helm release:

```bash
helm uninstall banking-platform -n banking-platform
```

Полностью удалить namespace со всеми ресурсами и данными:

```bash
kubectl delete namespace banking-platform
```
