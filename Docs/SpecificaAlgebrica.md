# Specifica Algebrica di GameMap

## Descrizione della Classe

La classe `GameMap` gestisce la struttura spaziale del mondo di gioco organizzato su più piani. Fornisce operazioni per l'aggiunta e ricerca di stanze, il collegamento bidirezionale tra stanze e la manipolazione delle immagini degli NPC presenti. La struttura interna utilizza una lista di piani, dove ogni piano contiene le stanze associate.

## Specifica Sintattica

**sorts:** `gamemap`, `room`, `floor`, `obscure`, `name`, `direction`, `id`, `floors`

**operations:**

- `newmap() → gamemap`
- `addroom(gamemap, room, floor) → gamemap`
- `linkfloors(gamemap, room, room, direction) → gamemap`
- `getroombynane(gamemap, name) → room`
- `findroombyid(gamemap, id) → room`
- `getallfloors(gamemap) → floors`
- `alteratenpcimagеs(gamemap, obscure) → gamemap`

_Note: I sort `room`, `floor`, `direction`, `name`, `id`, `floors` e `obscure` sono ausiliari alla definizione di `gamemap`._

## Specifica Semantica

**declare** `gm: gamemap`, `r: room`, `id: id`, `fl: floor`, `dir: direction`, `name: name`, `obs: obscure`, `fls: floors`

- `getRoombyName(newmap(), name) = null`
- `getRoombyName(addRoom(gm, r, fl), name) = getRoombyName(gm, name)`
- `findRoomById(newmap(), id) = null`
- `findRoomById(addroom(gm, r, fl), id) = findRoomById(gm, id)`
- `getAllFloors(newmap()) = emptylist`
- `getAllFloors(addRoom(gm, r, fl)) = getAllFloors(gm)`
- `alterateNpcImages(newmap(), obs) = newmap()`
- `alterateNpcImages(addRoom(gm, r, fl), obs) = addRoom(alterateNpcImages(gm, obs), r, fl)`

## Specifica di Restrizione

**restrictions**

- `linkFloors(newmap(), r1, r2, dir) = error`
- `addRoom(gm, r, fl) = error` se `fl < 0 OR fl > fls`

_dove 'error' è un elemento speciale indefinito._

## Costruttori e Osservatori

### Costruttori (criterio di minimalità)

- `newmap()` - costruttore base
- `addRoom(gamemap, room, floor)` - costruttore generativo

### Osservatori

- `getRoomByName(gamemap, name)`
- `findRoomById(gamemap, id)`
- `getAllFloors(gamemap)`
- `linkFloors(gamemap, room, room, direction)`
- `alterateNpcImages(gamemap, obscure)`

## Tabella Costruttori-Osservatori


| **osservatori**               | **Costruttore**            | **Costruttore**                              |
| ------------------------------| -------------------------- | -------------------------------------------- |
|                               |  `newmap()`                | `addRoom(gm, r, fl)`                       |
| ----------------------------- | -------------------------- | -------------------------------------------- |
| `getRoomByName(gm, name)`     | `null`                     | `getRoomByName(gm, name)`                    |
| `findRoomById(gm, id)`        | `null`                     | `findRoomById(gm, id)`                       |
| `getAllFloors(gm)`            | `emptylist`                | `getAllFloors(gm)`                           |
| `linkFloors(gm, r1, r2, dir)` | `error`                    | `linkFloors(gm, r1, r2, dir)`                |
| `alterateNpcImages(gm, obs)`  | `newmap()`                 | `addRoom(alterateNpcImages(gm, obs), r, fl)` |
