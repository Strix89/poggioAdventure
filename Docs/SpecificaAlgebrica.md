# Specifica Algebrica di gameMap

## Descrizione della Classe

La classe `gameMap` rappresenta la mappa di gioco strutturata su livelli multipli che definisce l'architettura spaziale dell'ambiente di gioco. Essa implementa funzionalità per inserire e ottenere stanze, creare collegamenti bidirezionali tra stanze e di omettere o meno le immagini degli NPC presenti in queste utlime. L'organizzazione interna si basa su una collezione stratificata di piani, ciascuno dei quali raccoglie le stanze corrispondenti.

## Specifica Sintattica

**sorts:** `gameMap`, `room`, `floor`, `obscure`, `name`, `direction`, `id`, `floors`

**operations:**

- `newMap() → gameMap`
- `addRoom(gameMap, room, floor) → gameMap`
- `linkFloors(gameMap, room, room, direction) → gameMap`
- `getRoomByName(gameMap, name) → room`
- `findRoomById(gameMap, id) → room`
- `getAllFloors(gameMap) → floors`
- `alterateNpcImagеs(gameMap, obscure) → gameMap`

_Note: I sort `room`, `floor`, `direction`, `name`, `id`, `floors` e `obscure` sono ausiliari alla definizione di `gameMap`._

## Specifica Semantica

**declare** `gm: gameMap`, `r: room`, `id: id`, `fl: floor`, `dir: direction`, `name: name`, `obs: obscure`, `fls: floors`

- `getRoombyName(newMap(), name) = null`
- `getRoombyName(addRoom(gm, r, fl), name) = getRoombyName(gm, name)`
- `findRoomById(newMap(), id) = null`
- `findRoomById(addRoom(gm, r, fl), id) = findRoomById(gm, id)`
- `getAllFloors(newMap()) = null`
- `getAllFloors(addRoom(gm, r, fl)) = getAllFloors(gm)`
- `alterateNpcImages(newMap(), obs) = newMap()`
- `alterateNpcImages(addRoom(gm, r, fl), obs) = addRoom(alterateNpcImages(gm, obs), r, fl)`

## Specifica di Restrizione

**restrictions**

- `linkFloors(newMap(), r1, r2, dir) = error`
- `addRoom(gm, r, fl) = error` se `fl < 0 OR fl > fls`

_dove 'error' è un elemento speciale indefinito._

## Costruttori e Osservatori

### Costruttori (criterio di minimalità)

- `newMap()` - costruttore base
- `addRoom(gameMap, room, floor)` - costruttore generativo

### Osservatori

- `getRoomByName(gameMap, name)`
- `findRoomById(gameMap, id)`
- `getAllFloors(gameMap)`
- `linkFloors(gameMap, room, room, direction)`
- `alterateNpcImages(gameMap, obscure)`

## Tabella Costruttori-Osservatori


| **osservatori**               | **Costruttore**            | **Costruttore**                              |
| ------------------------------| -------------------------- | -------------------------------------------- |
|                               |  `newMap()`                | `addRoom(gm, r, fl)`                         |
| `getRoomByName(gm, name)`     | `null`                     | `getRoomByName(gm, name)`                    |
| `findRoomById(gm, id)`        | `null`                     | `findRoomById(gm, id)`                       |
| `getAllFloors(gm)`            | `null`                | `getAllFloors(gm)`                           |
| `linkFloors(gm, r1, r2, dir)` | `error`                    | `linkFloors(gm, r1, r2, dir)`                |
| `alterateNpcImages(gm, obs)`  | `newMap()`                 | `addRoom(alterateNpcImages(gm, obs), r, fl)` |
