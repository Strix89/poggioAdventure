# Specifica Algebrica

La struttura dati che più abbiamo utilizzato nel nostro progetto è la Lista, per questo si riporta la sua Specifica Algebrica di seguito.

## Specifica sintattica

***sorts***: list, item, boolean, integer

***operations***:

```
//Costruttori
newList() -> list
addFirst(item, list) -> list

// Osservatori
head(list) -> item
tail(list) -> list
isEmpty(list) -> boolean
size(list) -> integer
add(list, item) -> list
get(list, integer) -> item
```

## Tabella Costruttori/Osservazioni
Per definire il set minimo di equazioni, organizziamo le informazioni in una tabella che mostra il risultato di ogni osservatore applicato a ogni costruttore. Sia `l'` una lista generata da un costruttore, `l` una lista generica, `i` un item generico e `n` un intero.


| **Osservatore** (applicato a `l'`) | **Costruttore:** `l' = newList()` | **Costruttore:**`l' = addFirst(i, l)` |
| :--- | :--- | :--- |
| `head(l')` | `error` | `i` |
| `tail(l')` | `error` | `l` |
| `isEmpty(l')` | `true` | `false` |
| `size(l')` | `0` | `1 + size(l)` |
| `add(l', j)` | `addFirst(j, newList())` | `addFirst(i, add(l, j))` |
| `get(l', n)` | `error` | `if n == 0 then i else get(l, n - 1)` |


**Nota**: L'operatore `add` è stato definito in modo ricorsivo. Una definizione alternativa per `add(addFirst(i, l), item_to_add)` è `addFirst(i, add(l, item_to_add))`, che è più semplice e conduce alle stesse proprietà.

## Specifica semantica

Questa sezione definisce le proprietà degli operatori tramite un insieme minimale di equazioni (assiomi). Queste equazioni sono derivate direttamente dalla tabella precedente.

***declare***: *l: list, i, j: item, n: integer*;

```
head(addFirst(i, l)) = i
tail(addFirst(i, l)) = l
isEmpty(newList()) = true
isEmpty(addFirst(i, l)) = false
size(newList()) = 0
size(addFirst(i, l)) = 1 + size(l)
add(newList(), i) = addFirst(i, newList())
add(addFirst(i, l), j) = addFirst(i, add(l, j))
get(addFirst(i, l), n) = if n == 0 then i else get(tail(addFirst(i,l)), n-1)
```

Questo insieme di equazioni è:

**Completo**: Permette di determinare il risultato di qualsiasi sequenza di operazioni.
**Consistente**: Non permette di derivare contraddizioni (es. true = false).
**Minimale** (non ridondante): Nessuna equazione è derivabile dalle altre.

## Specifica di restrizione
Questa parte gestisce i casi d'errore, ovvero l'applicazione di operatori a stati non validi.

***restrictions***
```
head(newList()) = error
tail(newList()) = error
get(newList(), n) = error
get(l, n) = error if n < 0 or n >= size(l)
```