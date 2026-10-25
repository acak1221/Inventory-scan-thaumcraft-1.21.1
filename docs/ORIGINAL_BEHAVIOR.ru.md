# TC Inventory Scanning 1.0.11: проверенный контракт

Артефакт: `/workspace/attachments/665726c8-4256-40b7-ab6a-193fde380c0f/tcinventoryscan-mc1.7.10-1.0.11.jar`.
SHA-256: `8f7ddf60d19903c99b6e72f7ae52fad7484999e72affc6ff0559b0502ec7dfad`.
Исследованы все10 классов через `javap -p -c -v`; JAR не исполнялся. Bytecode/resources сохранены рядом,
вне репозитория Thaumcraft. Наблюдения статические, исходный клиент1.7.10 не запускался.

## Контракт интерфейса

Все имена классов ниже относительно `net.blay09.mods.tcinventoryscan`.

1. `TCInventoryScanning`: modid `tcinventoryscan`, имя `TC Inventory Scanning`, зависимость аннотации
   `required-after:Thaumcraft`, acceptableRemoteVersions=`*`. Метаданные:1.0.11, MC1.7.10, автор
   BlayTheNinth; `url`/`updateUrl` пустые. `CommonProxy.init` регистрирует FML events,
   `ClientProxy.init` дополнительно Forge GUI/tooltip events и client ticks; `postInit` получает
   `GameRegistry.findItem("Thaumcraft","ItemThaumometer")`.
2. `ClientProxy.clientTick`: инструмент требуется именно в cursor/carried stack
   (`InventoryPlayer.getItemStack`), а не в mainhand/hotbar. Кнопки мыши не перехватываются;
   достаточно навести инструмент на предмет, без клика. Предметы/таумометр не расходуются.
3. `ClientProxy.onDrawScreen`: работает после отрисовки любого `GuiContainer`, без whitelist
   инвентарей. `getSlotAtPosition` выбирает слот. Для начала нужны непустой stack,
   `slot.canTakeStack(player)`, slot != lastScannedSlot, и `!(slot instanceof SlotCrafting)`.
   Исключение относится к классу crafting-result и его потомкам, не ко всем output slots.
   У furnace/machine output нет общего запрета; применяются фактические canTake/class проверки.
   Creative GUI отдельно не исключено, но перенумерация creative/catalogue slots отсутствует;
   реальная работоспособность зависит от их отображения на серверный контейнер.
4. ScanResult предмета: type1, numeric itemID, damage/meta, entity=null, phenomena="".
   NBT/count в scan identity не входят. Prefix у всех вызовов ScanManager=`@`.
   `isValidScanTarget` отсеивает уже изученные цели; правила аспектов/родительских исследований,
   награды, открытия и обычные сообщения об ошибке принадлежат TC4 `ScanManager.completeScan`.
5. Таймер: SCAN_TICKS=50 вызовов ClientTickEvent; phase не проверяется. FML публикует START и END,
   поэтому при20TPS это25 игровых тиков≈1.25с, не2.5с. Переключение Slot object сбрасывает счётчик
   и текущий ScanResult. Потеря cursor-таумометра сбрасывает также lastScannedSlot.
   После завершения/невалидной цели запоминается Slot object, поэтому тот же слот повторно
   не пробуется до другого прохода состояния. Изменение stack внутри того же Slot не является
   самостоятельной причиной сброса старого currentScan.
6. Звук, обе ветки slot/self: `ticksHovered > 5 && ticksHovered % 2 == 0`, после инкремента.
   `thaumcraft:cameraticks`, volume0.2, pitch0.45+randomFloat*0.1, позиция игрока,
   distanceDelay=false. При двух callback на тик: первый звук примерно на3-м игровом тике,
   далее каждый игровой тик. SOUND_TICKS=5 — порог начала, не интервал. Отдельного scan-success
   звука/ресурса аддон не добавляет.
7. `renderScanningProgress`: золотой текст `Scanning` у mouseX,mouseY−30 с shadow; точка при
   progress≥.25, две≥.5, три≥.75. Progress=ticksHovered/50. Это текст с точками, не progress bar.
   Показ только при ненулевом currentScan. При завершении currentScan очищается.
8. Даже при непустом cursor stack `onDrawScreen` вручную вызывает normal `renderToolTip` предмета
   под курсором и TC4 `ClientTickEventsFML.renderAspectsInGui`. Поэтому известные аспекты
   показываются принудительно, без общего Shift/showTags gate. TC4 ряд показывает только
   изученные предметы (`@` или `#`), сортирует по количеству, рисует _back и цветные иконки/
   количества; неизвестные игроку аспекты скрываются за `_unknown`.
9. Self scan: только `GuiInventory`, прямоугольник `[guiLeft+26,guiLeft+78)` ×
   `[guiTop+8,guiTop+78)`, то есть52×70px модели игрока. ScanResult type2/entity=тот же игрок.
   После изучения `renderPlayerAspects` использует TC4 generateEntityAspects, сортировку по
   количеству, _back размером20px и неизвестные иконки. X сначала mouseX+17, затем перед
   каждой иконкой+=18; Y=mouseY−26. Сдвиг X/Y−8, либо−16 при Thaumcraft.aspectShift.
10. `onTooltip`: только для Thaumometer и включённого аддона. Золотая строка
    `Inventory Scanning available`; при левом/правом Shift (LWJGL42/54) две dark-aqua строки
    `Hover items with the Thaumometer` / `on the cursor to scan.`. Только en_US.lang;
    textures, собственных звуков, keybinding или config-файла/класса нет.

## Сеть и достигаемость

`NetworkHandler.init`: канал `tcinventoryscan`, C2S ScanSlot discriminator0 (один int slotNumber),
C2S ScanSelf1 (пустой payload), C2S Hello2/S2C Hello3 (один int version).
`CommonProxy.playerJoined` посылает Hello(1). `HandlerHello` вызывает proxy.receivedHello;
на клиенте это ставит isEnabled=true, helloTimeout=0 и возвращает Hello(1) серверу.
Version поле декодируется, но фактически не сравнивается. До Hello GUI/сканирование выключено.
При подключении timeout=1200 client callbacks; только при наличии player он уменьшается;
при двух фазах около30с. Сообщение timeout ошибочно ссылается на Crafting Tweaks — артефакт оригинала.

По достижении50 callbacks сначала вызывается client `ScanManager.completeScan(...,"@")`.
Только если true, отправляется ScanSlot(slotNumber) либо ScanSelf. JAR1.0.11 ловит и подавляет
`StackOverflowError` вокруг этих complete/send участков, затем сбрасывает таймер/scan.

`HandlerScanSlot.onMessage`: actor берётся из server context; current openContainer неnull;
0≤slot<size; slot stack неnull; canTakeStack; !SlotCrafting. Сервер перечитывает фактический
stack и вызывает TC4 completeScan. `HandlerScanSelf` напрямую сканирует actor. Сервер не проверяет
наличие инструмента, cursor state, задержку, windowID, target snapshot или scan-session.
Это исходные ограничения реализации, а не рекомендуемый контракт безопасности нового порта.

## Подтверждённые исходные дефекты, не переносить случайно

- `onDrawScreen` при входе в portrait не очищает старый mouseSlot. Если тот непустой/takeable и
  не lastScannedSlot, `clientTick` сначала попадает в slot-ветку, но currentScan уже self(type2).
  После50 callbacks клиент завершает self и отправляет ScanSlot старого слота; сервер изучает
  предмет вместо self. Правильная self-ветка работает, когда старый слот отсутствует/невалиден/
  уже lastScannedSlot. Независимо подтверждено nf_tests по bytecode.
- Нет привязки timer/currentScan к текущему screen/container и смене stack внутри Slot.
- ScanSlot не содержит windowID; запоздалый пакет использует уже текущий серверный контейнер.
- Для серверных наград отсутствуют инструмент/таймер/session guards; перенос требует явной
  серверной авторитетности, а не копирования этих пробелов.

## Source/licence

В самом предоставленном JAR нет LICENSE и source URL. Исторический публичный source mirror:
https://github.com/Searge-DP/TCInventoryScanning (MC1.7.10, TC4.2.3.5 dependency).
Его LICENSE — MIT, copyright(c)2015 Christopher "BlayTheNinth" Baker,
blob `349098aec267997fcfb4f0e93849134c70265c13`:
https://github.com/Searge-DP/TCInventoryScanning/blob/master/LICENSE .
ClientProxy blob `71f57c9c04e4149312e1d0b788b13c2c8f04b41a` совпадает по основному GUI/таймингу,
но не содержит добавленных в предоставленном JAR catches StackOverflowError; это вспомогательный
источник и атрибуция, не подмена точного binary oracle. Исходный кандидат
https://github.com/BlayTheNinth/TCInventoryScanning сейчас отвечает404 через GitHub connector.
Чужие JAR/assets не встраивались в production; исходные notice/license должны сопровождать
производные существенные части исходного аддона.
