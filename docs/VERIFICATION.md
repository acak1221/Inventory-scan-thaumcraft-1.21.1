# Сборка 1.0.2+1.21.1 без ограничения версии Thaumcraft

Дата: 01.11.2026. В метаданных Fabric и NeoForge требуемая версия Thaumcraft заменена на `*`. Minecraft по-прежнему строго 1.21.1, требования Java и загрузчиков сохранены. Код сканирования не менялся.

Сборка выполнена на Windows с Temurin Java 21.0.13+9 и Gradle 8.12.1. Использован Thaumcraft 4 Reload из коммита `4c38e3f3bb44c3455718d8f3e04073921ec2a932`, собранный в этой сессии. Его действительные SHA-256 переданы через `-Pthaumcraft_fabric_sha256` и `-Pthaumcraft_neoforge_sha256`; исходные закреплённые хеши не изменялись.

- `:common:test :fabric:build :neoforge:build`: BUILD SUCCESSFUL; 6 JUnit-тестов без ошибок и пропусков; проверки изоляции обоих итоговых JAR пройдены.
- `:fabric:runGameTest :neoforge:runGameTest`: BUILD SUCCESSFUL; по 13 успешных серверных GameTests на каждом загрузчике, отчёты в `<loader>/build/gametest-results.xml`.
- Прочитаны метаданные итоговых JAR: версия `1.0.2+1.21.1`, зависимость Thaumcraft `*`; Fabric refmap присутствует; классы Thaumcraft, Minecraft и тестовая обвязка не включены.

Другие версии Thaumcraft, игровой клиент и удалённый мультиплеер в этой сессии не проверялись. Снятие ограничения номера версии не гарантирует совместимость с изменённым API.

# Проверки версии 1.0.1+1.21.1

Дата: 27.10.2026. Перевод аддона на Thaumcraft 4 Reload `0.1.0-alpha.7` (сборка коммита основного репозитория `3f97bfef`). Код аддона не менялся: он собирается против API alpha.7 без правок. Изменены требуемая версия Thaumcraft в `fabric.mod.json` и `neoforge.mods.toml` (было ровно `0.1.0-alpha.6`, из-за чего 1.0.0 не загружается с alpha.7), закреплённые JAR и их SHA-256 в `gradle.properties` и `docs/provenance.json`, версия аддона и название проекта (FOREVA → Thaumcraft 4 Reload в текстах, LICENSE и метаданных; Java-пакеты `dev.foreva.*` и id `tcinventoryscan` не менялись).

Классы Thaumcraft, которые вызывает аддон, сверены между исходным коммитом alpha.6 (`bebe0a76`) и `3f97bfef`: `HoveredSlotAspectRow`, `ScanManager`, `ScanCallbacks`, `KnowledgeAccess`, `ItemThaumometer`, `ScanResult` не изменились; в `EntityAspectBindings`, `ObjectAspectResolver` и `PlayerKnowledge` только добавления, вызываемые аддоном методы те же.

| Область | Результат |
|---|---|
| Common JUnit | 6/6, без ошибок и пропусков |
| Production build | `:fabric:build :neoforge:build` PASS (включая проверку изоляции JAR) |
| Fabric dedicated GameTest | 13/13 со свежим миром; загружены `thaumcraft 0.1.0-alpha.7` и `tcinventoryscan 1.0.1+1.21.1` |
| NeoForge dedicated GameTest | 13/13 со свежим миром; загружен `Thaumcraft-neoforge-0.1.0-alpha.7` (SHA-256 `2f14df0d…`) |
| Fabric client | Настоящий клиент (Xvfb, 1280×720), новый survival-мир: таумометр на курсоре над булыжником — [надпись Scanning](images/fabric-alpha7-cobblestone-scanning.png), затем [аспекты Terra 1, Perditio 1 и уведомления об очках исследований](images/fabric-alpha7-cobblestone-scanned.png) |

Не проверено для 1.0.1: клиент NeoForge, сканирование в сундуке и собственного персонажа в клиенте (покрыты GameTest), мультиплеер. Клиент Fabric после снимков закрыт принудительно, штатный выход с сохранением мира не проверялся. Звук не проверялся (в контейнере нет аудиоустройства).

# Проверки версии 1.0.0+1.21.1 (Thaumcraft FOREVA alpha.6)

Дата: 25.10.2026. Новая самостоятельная реализация поведения приложенного `tcinventoryscan-mc1.7.10-1.0.11.jar`; оригинал не исполнялся и не включён в аддон. Его контракт разобран по bytecode, ключевые выводы независимо проверены: [ORIGINAL_BEHAVIOR.ru.md](ORIGINAL_BEHAVIOR.ru.md). Атрибуция: [THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md).

## Итоговые проверки

Все команды выполнены локально на Java 21 / Gradle 8.12.1 с настоящими Minecraft 1.21.1 и зависимостями. GitHub Actions не запускался.

| Область | Подтверждённый результат |
|---|---|
| Common JUnit | 6/6, без ошибок и пропусков; состояние клиента, nonce, поздние ответы, heartbeat, отмена и звук |
| Production build | Fabric и NeoForge PASS; финальная пересборка после исправления конфигурации mappings |
| Изоляция | В JAR нет классов Thaumcraft/Minecraft, вложенных JAR, GameTest helpers и тестовых регистраций; LICENSE/атрибуция присутствуют |
| NeoForge dedicated GameTest | 13/13, без ошибок/пропусков; все зарегистрированные сценарии присутствуют в свежем XML |
| Fabric dedicated GameTest | 13/13, без ошибок/пропусков; все зарегистрированные сценарии присутствуют в свежем XML |
| Normal NeoForge client | Реальный GL-клиент, штатные сохранение мира/выход, Gradle exit0 за 6m1s |
| Normal Fabric client | Реальный GL-клиент, штатные сохранение мира/выход, Gradle exit0 за 10m |
| Сохранения | Независимое чтение NBT после обоих клиентов: stone/glass/cobblestone просканированы; таумометр и все исходные предметы сохранены по 1; булыжник в сундуке остался |
| Основной мод | Исходники/ветки не менялись; точные внешние JAR и named API не изменены |

Финальные 26 native сценариев исполнены командой `:neoforge:runGameTest :fabric:runGameTest` за 52s, с normal save/stop обоих серверов. Это новые проверки аддона, не повторение прежнего полного набора Thaumcraft из 473/467 тестов. Fixture использует действительные ServerPlayer/меню/предметы и нативные зарегистрированные обработчики пакетов. Соединения GameTest созданы через EmbeddedChannel и loader mock/registration APIs: это не доказательство полноценного удалённого multiplayer handshake.

Проверяются 25 настоящих серверных тиков, невозможность ускорить скан 100 C2S-пакетами, наличие курсора, адрес/identity меню и слота, запрещённый crafting result, veto, замена предмета и его компонентов/количества, замена курсора, атомарность prerequisites, точные RP, повтор запроса/предмета, собственный персонаж и мгновенный отказ его повторного сканирования, отмена/истечение heartbeat, retirement permission callback, disconnect/reconnect и отсутствие расхода предметов.

## Физическая проверка

Оба клиента использовали отдельные копии собственных свежих 1.21.1 diagnostic-миров предыдущей задачи. Это переиспользованный creative-вход и диагностические команды, а не новое survival-прохождение. Для проверки обычного инвентаря выполнен `/gamemode survival`; выданы по одному таумометру, Stone, Dirt, Glass. В отдельном сундуке находился один Cobblestone. Выполнены обычные pickup/mouse hover/return действия через интерфейс, без вызова server scan engine тестовыми кнопками.

| Сцена | NeoForge | Fabric |
|---|---|---|
| Наведение таумометра на Stone | [Scanning](images/neoforge-stone-scanning.png) | [Scanning](images/fabric-stone-scanning.png) |
| Завершённый скан Cobblestone в сундуке | [Аспекты и уведомления](images/neoforge-chest-scanned.png) | [Аспекты и уведомления](images/fabric-chest-scanned.png) |
| Shift-подсказка инструмента | [Tooltip](images/neoforge-tooltip-help.png) | [Tooltip](images/fabric-tooltip-help.png) |

Дополнительно осмотрены завершённые Stone/Glass кадры, открытие Vitreus и штатные RP-уведомления. Снятые изображения не редактировались. Xorg1280×720, окно854×480, GUI Auto2, Mesa llvmpipe/OpenGL4.5. Звуковой вывод отключён: проверены код/cadence, а не слышимость звука.

Независимо прочитаны сохранённые `level.dat`, активный playerdata и chunk NBT сундука. Данные инвентаря/knowledge в level и playerdata согласованы. `.dat_old` уже содержит эти сканы и не является исходным RP-baseline; полный прирост всех RP относительно начала физической сессии отдельно не заявляется. Точные RP переходов проверены native тестами.

## Исправленные при проверке проблемы

- Устранён hardcoded Fabric refmap в общем авторском mixin JSON. Теперь Fabric получает платформенный refmap автоматически, NeoForge использует Mojang-имена без Fabric refmap. Строгие `require=1`, `allow=1`, `defaultRequire=1` сохранены. Первый неудачный NeoForge запуск и предыдущие артефакты сохранены в локальных ignored logs.
- В первоначальной тестовой обвязке исправлены разные namespace/refmap контракты loader API и двойной namespace шаблона NeoForge. Изменения касались test-only кода/metadata.
- В тестовой обвязке устранено изменение vanilla Fastutil scheduled-task map из nested fixture; один lifecycle ticker и собственная очередь сохраняют те же проверки и cleanup. Ожидание native доставки ответа ограничено двумя тиками без ослабления результата `ALREADY_SCANNED/progress0/noSCANNING`.
- Проверены и защищены повторный вход из callbacks, замена владельца слота и ранний повторный self scan. Не обновлялись оригинальные/внешние контрольные суммы ради зелёного результата.

## Отличия и ограничения

Окно CreativeModeInventoryScreen целиком исключено с явной подсказкой. Его vanilla cursor существует только на клиенте, поэтому он не соответствует server-authoritative протоколу. Обычные сундуки/контейнеры работают и у creative-игроков. Это сознательное отличие от оригинала; обходная синхронизация курсора и доверие клиентским стекам не добавлялись.

Не подтверждены: полное survival-прохождение, длительная эксплуатация, два удалённых игровых клиента, настоящие удалённые подключения с отсутствующим аддоном, все сторонние меню/аддоны/ресурс-паки и визуальная русская локализация в игровом клиенте. EN/RU ресурсы включены. Copy-returning/виртуальные слоты могут быть отвергнуты строгой проверкой identity. Не заявляется отсутствие всех ошибок.

Raw logs, XML, snapshots, screenshots и независимые receipts находятся в `.tmp/` локального workspace; игровые сохранения, оригинальный JAR и приватная декомпиляция в Git не включены.
