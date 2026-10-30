## Microservices patterns

В системе используются пять самостоятельных Spring Boot приложений:

| Приложение          | Порт | Назначение                              |
|---------------------|------|-----------------------------------------|
| user-service        | 8080 | REST API пользователей и PostgreSQL     |
| notification-service| 8081 | Обработка событий Kafka и отправка писем |
| discovery-server    | 8761 | Реестр сервисов Eureka                  |
| api-gateway         | 8082 | Общая точка входа к HTTP API             |
| config-server       | 8888 | Централизованная выдача настроек         |

Исходный проект user-service находится в папке UserBD.
Порты соответствуют текущим настройкам и могут быть изменены.

## Внешняя конфигурация

Настройки приложений хранятся в отдельной папке config-repo.
Она находится вне src/main/resources приложений.

## Порядок запуска

1. Запустить PostgreSQL, Kafka и Mailpit.
2. Запустить config-server.
3. Запустить discovery-server.
4. Запустить user-service и notification-service.
5. Дождаться регистрации обоих сервисов в Eureka со статусом UP.
6. Запустить api-gateway и дождаться обновления его реестра сервисов.

Eureka доступна по адресу:

http://localhost:8761

Mailpit доступен по адресу:

http://localhost:8025

При создании контейнера Mailpit используется переменная:

MP_SMTP_DISABLE_RDNS=true

Она отключает обратный DNS-поиск, который в текущем локальном
окружении вызывал задержку SMTP-приветствия примерно на 10 секунд.

## API Gateway и Service Discovery

Основной адрес для HTTP-запросов клиента:

http://localhost:8082

Адреса localhost:8080 и localhost:8081 используются для прямой
диагностики сервисов. Такие запросы обходят Gateway и его
Circuit Breaker.

Обмен событиями между user-service и notification-service
происходит через Kafka и не проходит через Gateway.

## Circuit Breaker

В Gateway настроены два независимых Circuit Breaker:

userCircuitBreaker — для API пользователей.
notificationCircuitBreaker — для API уведомлений.

При ошибке обращения к сервису, превышении тайм-аута или открытом
Circuit Breaker Gateway вызывает соответствующий fallback:

/fallback/users
/fallback/notifications

Fallback возвращает HTTP 503 и JSON с сообщением о временной
недоступности сервиса.

События доступны по адресу:

http://localhost:8082/actuator/circuitbreakerevents

Сведения о Circuit Breaker доступны по адресу:

http://localhost:8082/actuator/circuitbreakers

## Технологии

- Java 17, Maven, Spring Boot 3.5.16.
- Spring Web — REST API.
- Spring Data JPA и PostgreSQL — хранение пользователей.
- Bean Validation — проверка входных данных.
- Apache Kafka и Spring Kafka — обмен событиями между сервисами.
- Spring Mail — отправка писем по SMTP.
- Mailpit — локальный приём и просмотр писем.
- JUnit 5, Mockito, MockMvc — тестирование.
- Testcontainers — PostgreSQL для интеграционных тестов.
- Embedded Kafka и GreenMail — Kafka и почтовый сервер внутри тестов.

## Что необходимо для работы

- JDK 17.
- Maven или встроенный Maven в IntelliJ IDEA.
- Docker Desktop для запуска Kafka и Mailpit.
- PostgreSQL: установленный локально или запущенный в Docker.

Каждое приложение нужно открыть как отдельный Maven-проект
и загрузить зависимости из его `pom.xml`.

Перед запуском приложений должны работать PostgreSQL, Kafka и Mailpit.

### Kafka

Первый запуск:

```powershell
docker run -d --name userbd-kafka -p 127.0.0.1:9092:9092 apache/kafka:3.9.1
```

После готовности Kafka создать топик:

```powershell
docker exec userbd-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic user-events --partitions 1 --replication-factor 1
```

### Mailpit

Первый запуск:

```powershell
docker run -d --name notification-mailpit -p 127.0.0.1:1025:1025 -p 127.0.0.1:8025:8025 axllent/mailpit
```

Для повторного запуска уже созданных контейнеров:

```powershell
docker start userbd-kafka notification-mailpit
```

### PostgreSQL

В user-service настроено подключение:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=root
```

Эти значения предназначены для локального учебного запуска.
При необходимости их нужно изменить под свою БД.

## Как реализовано взаимодействие

Общая цепочка:

```text
HTTP-запрос на создание или удаление пользователя
    → user-service
    → PostgreSQL
    → событие в Kafka
    → notification-service
    → письмо в Mailpit
```

В user-service класс `UserService` сохраняет или удаляет пользователя
и публикует внутреннее событие Spring через `ApplicationEventPublisher`.

Событие содержит операцию и email:

```json
{
  "operation": "CREATED",
  "email": "alex@example.com"
}
```

Возможные операции:

- `CREATED` — пользователь создан.
- `DELETED` — пользователь удалён.

`UserEventAfterCommitListener` обрабатывает внутреннее событие
после успешного завершения транзакции БД. Затем `UserEventProducer`
отправляет сообщение в топик Kafka `user-events`.

Если транзакция откатилась, отправка в Kafka не выполняется.

В notification-service класс `UserEventListener` получает сообщение,
проверяет данные через `Validator` и вызывает `NotificationService`.

`NotificationService` выбирает текст по операции, создаёт
`SimpleMailMessage` и отправляет его через `JavaMailSender`.

## Адреса и порты

| Компонент | Адрес | Назначение |
| user-service | `http://localhost:8080` | API пользователей |
| notification-service | `http://localhost:8081` | API уведомлений |
| PostgreSQL | `localhost:5432` | База данных |
| Kafka | `localhost:9092` | Обмен событиями |
| Mailpit SMTP | `localhost:1025` | Приём писем от приложения |
| Mailpit Web | `http://localhost:8025` | Просмотр писем в браузере |

У Mailpit два разных порта: приложение отправляет письма на `1025`,
а пользователь просматривает их в браузере на `8025`.