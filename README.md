# Шаблон backend сервиса
| Информация           |                                                                                                                                                                                           |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Название             | Шаблон backend сервиса                                                                                                                                                                    |
| Техническое название | jvm-backend-template                                                                                                                                                                      |
| Описание             | Сервис содержит:<br/>- Типовую структуру проекта<br/>- Примеры http / kafka api<br/>- Взаимодействие с БД<br/>- Решение типовых задач<br/>- Примеры использования платформенных библиотек |
| GIT                  | [GitHub](https://github.com/nemodeveloper/jvm-backend-template)                                                                                                                           |
| URL                  |                                                                                                                                                                                           |
| Описание API         |                                                                                                                                                                                           |
| Структура DB         |                                                                                                                                                                                           |
| Статус               |                                                                                                                                                                                           |
| Maintainers          | Симанов А.Н [email](mailto:nemodev@yandex.ru) [telegram](tg://simanovan)                                                                                                                  |
| Архитектура          |                                                                                                                                                                                           |

## Стек разработки(в реальном проекте пункт не указывается)
- Docker / K8S
- Java 21 / Kotlin / Gradle 8.8
- SpringBoot 3.3.1
- Postgres / Kafka / Minio(S3) / Redis
- JUnit + TestContainers + Jacoco
- Metrics - Micrometer + Prometheus + Grafana
- Tracing - Micrometer observation / otlp + jaeger

## Настройка структуры своего сервиса(в реальном проекте пункт не указывается)
- 1 Скопируйте в свой проект каталоги:
- 1.1 dev - в своем проекте оставляйте то что вам нужно, так же меняем название docker container в docker-compose и env параметры
- 1.2 gradle / gradlew / gradlew.bat - для сборки проекта
- 1.3 src - придерживайтесь иерархии/семантике пакетов и классов
- 1.3.1 для генерации своего banner.txt используйте https://devops.datenkollektiv.de/banner.txt/index.html font star-wars
- 1.3.2 base package должен быть ru.nemodev.* на него завязаны платформенные библиотеки logging/db/etc
- 1.4 .gitignore
- 1.5 settings.gradle.kts - меняем внутри rootProject.name на название своего сервиса
- 1.6 build.gradle.kts - подключаем core и свои зависимости
- 1.7 README.md - секции заполняете уже исходя из своего сервиса

## Запуск
- 1 Запустить Springboot Application.kt в режиме debug, в настройках конфигурации запуска idea указать профиль dev
- 2 При запуске сервиса автоматически запускается окружение из dev/docker/docker-compose.yml
- 2.1 Иногда могут быть проблемы при запуске зависимых между собой контейнеров например kafka -> zookeeper
  В таком случае запускаем их в ручную из docker-compose файла
- 3 [s3-minio](http://localhost:9000) УЗ - admin/admin1234
- 4 [Swagger Open Api UI](http://localhost:8080/swagger-ui.html)
- 5 [AsyncApi UI](http://localhost:8080/springwolf/asyncapi-ui.html)

## Отладка
- 1 HTTP заглушки реализованы через Wiremock смотри каталог /dev/docker/wiremock
- 2 Kafka заглушки реализованы через mock конфигурации смотри пакет /config/mock 

## Metrics(указываем если у вас есть кастомные метрики)
- 1 Сервис автоматически генерирует prometheus метрики по пути /actuator/prometheus
- 2 Для просмотра метрик в dev/docker/docker-compose.yml добавьте prometheus/grafana
- 2.1 В файле /dev/docker/prometheus/prometheus.yml укажите название своего сервиса
- 3 [Grafana UI](http://localhost:3000) log/pass admin/admin
- 3.1 В меню перейдите в Dashboards выберите Spring Boot 3
- 4 Отдельными пунктами указываем кастомные метрики

## Tracing(указываем если у вас есть кастомные трейсы)
- 1 Сервис автоматически генерирует http/kafka/db/s3 trace span в формате otlp
- 2 Для просмотра trace в dev/docker/docker-compose.yml добавьте jaeger
- 3 [Jaeger UI](http://localhost:16686)
- 4 Отдельными пунктами указываем кастомные trace span
- 5 Более подробно о том как работает трассировка смотри в библиотеке [core-tracing](https://github.com/nemodeveloper/jvm-backend-platform/tree/main/core/tracing)
