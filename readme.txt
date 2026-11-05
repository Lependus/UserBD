## Запуск системы через Docker Compose

### Требования

- Установленный и запущенный Docker Desktop в режиме Linux containers.
- Доступ к интернету для загрузки образов и Maven-зависимостей.
- Свободные порты: 8082, 8761, 8888 и 8025.

### Запуск

Открыть PowerShell в корневой папке проекта, где находится
docker-compose.yml.

Проверить конфигурацию:

docker compose config --quiet

Собрать образы и запустить всю систему:

docker compose up -d --build

Проверить состояние контейнеров:

docker compose ps -a

### Состав системы

- postgres — база данных пользователей.
- kafka — обмен событиями в режиме KRaft.
- kafka-init — создание топика user-events.
- config-server — централизованная выдача конфигурации.
- discovery-server — реестр сервисов Eureka.
- user-service — REST API пользователей.
- notification-service — обработка событий и отправка писем.
- api-gateway — общая точка входа к HTTP API.
- mailpit — приём и просмотр тестовых писем.

### Адреса

| Компонент | Адрес |
| API Gateway | http://localhost:8082 |
| Пользователи через Gateway | http://localhost:8082/users |
| Состояние Gateway | http://localhost:8082/actuator/health |
| Eureka | http://localhost:8761 |
| Config Server: настройки user-service для Docker | http://localhost:8888/user-service/docker |
| Mailpit | http://localhost:8025 |

### Конфигурация

Папка config-repo подключается к Config Server только для чтения.

- postgres:5432
- kafka:9092
- discovery-server:8761
- mailpit:1025

История писем Mailpit в текущей конфигурации не сохраняется
после удаления его контейнера.