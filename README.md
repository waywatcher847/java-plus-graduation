# Приложение Explore With Me
___
Сервис афиши событий. Пользователи создают события, подают заявки на участие, комментируют, лайкают, подписываются друг на друга.

## *Архитектура*

Микросервисная архитектура с разделением по доменам. Каждый сервис владеет своей БД. 
Общение через OpenFeign.

* user-service - пользователи
* event-service - события, категории, подборки, локации
* request-service - заявки на участие
* socials-service - комментарии, рейтинги, подписки
* stats-service - статистика просмотров
* Config-server - внешняя конфигурация всех модулей
* Discovery-server - динамически поиск сетевых адресов запущенных сервисов
* Gateway-server - единая точка входа для всех клиентских запросов

/admin/* - административные операции

/users/{userId}/* - приватные эндпоинты пользователя

/events, /categories, /compilations, /locations - публичные

Все внешние вызовы обёрнуты в FallBackTemplate(Resilience4j):
* Circuit Breaker - изоляция падающих сервисов
* Retry - 2 попытки с задержкой 100мс
* Fallback - возврат пустых данных вместо падения
* BestEffort - для каскадных удалений (deleteUser) ошибки игнорируются

При удалении пользователя user-service вызывает /internal/*/users/{id} 
во всех зависимых сервисах через FallBackTemplate.bestEffort.

В request-service по eventId используются блокировки pg_advisory_xact_lock для обработки одновременных заявк.

## *Внутренний API*

Используется для межсервисного взаимодействия.

user-service 
* GET /internal/users/{id} - получить пользователя
* POST /internal/users/batch - масовая загрузка пользователей по списку ID

event-service
* GET /internal/events/{id} - получить событие (используется в request-service, socials-service)
* DELETE /internal/events/users/{id} - каскадная очистка при удалении пользователя 

request-service
* POST /internal/requests/counts - количество подтверждённых заявок по списку событий
* DELETE /internal/requests/users/{id} - каскадная очистка при удалении пользователя

socials-service
* POST /internal/socials/aggregate - собирает рейтингы, комментарии, подписоки в один вызов
* DELETE /internal/socials/users/{id} - каскадная очистка при удалении пользователя

## *Внешний API*

Swagger c внешним API можно найти по ссылкам ниже

* [core - основные сервисы](https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-main-service-spec.json)
* [stats - статистика](https://raw.githubusercontent.com/yandex-praktikum/java-explore-with-me/main/ewm-stats-service-spec.json)