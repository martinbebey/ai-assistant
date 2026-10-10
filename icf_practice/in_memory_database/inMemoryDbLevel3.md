# In-Memory Database — Level 3

## Level 3

Add **timestamps** and **TTL** (time-to-live) on fields.

Timestamps are unique and **strictly increasing**.  
Keep Level 1–2 methods working as before (no TTL). The new `*At` methods respect expiry.

TTL interval is **half-open**: field is alive for  
`[setTimestamp, setTimestamp + ttl)`  
— alive at `setTimestamp + ttl - 1`, **gone** at `setTimestamp + ttl`.

A field with **no TTL** (`setAt`) never expires.

---

### `setAt(key, field, value, timestamp): String`

Set/overwrite `field` with **no expiry**. Returns `""`.

### `setAtWithTtl(key, field, value, timestamp, ttl): String`

Set/overwrite `field` and give it TTL `ttl` starting at `timestamp`.  
Expires at `timestamp + ttl` (not inclusive). Returns `""`.  
Overwriting a field **replaces** its value **and** its expiry.

### `getAt(key, field, timestamp): String`

Like `get`, but only if the field is still alive at `timestamp`. Else `""`.

### `deleteAt(key, field, timestamp): String`

Like `delete`, but only if the field exists **and is alive** at `timestamp`.  
Returns `"true"` / `"false"`. Expired fields cannot be deleted (`"false"`).

### `scanAt(key, timestamp): String`

Like `scan`, but only **alive** fields at `timestamp`.

### `scanByPrefixAt(key, prefix, timestamp): String`

Like `scanByPrefix`, but only alive fields at `timestamp`.

---

## Examples

| Query | Result |
|-------|--------|
| `setAtWithTtl("A", "BC", "E", 1, 9)` | `""` — expires at **10** |
| `setAtWithTtl("A", "BC", "E", 5, 10)` | `""` — overwrite; now expires at **15** |
| `setAt("A", "BD", "F", 6)` | `""` — no expiry |
| `scanByPrefixAt("A", "B", 14)` | `"BC(E), BD(F)"` |
| `scanByPrefixAt("A", "B", 15)` | `"BD(F)"` — BC expired |

| Query | Result |
|-------|--------|
| `setAt("A", "B", "C", 1)` | `""` |
| `setAtWithTtl("X", "Y", "Z", 2, 15)` | `""` — expires at **17** |
| `getAt("X", "Y", 3)` | `"Z"` |
| `setAtWithTtl("A", "D", "E", 4, 10)` | `""` — D expires at **14** |
| `scanAt("A", 13)` | `"B(C), D(E)"` |
| `scanAt("X", 16)` | `"Y(Z)"` |
| `scanAt("X", 17)` | `""` — Y expired |
| `deleteAt("X", "Y", 20)` | `"false"` — already expired |

---

Hint: store more than a string per field (value + optional `expiresAt`).  
`scan` / `scanByPrefix` can reuse a helper: “fields of this key that are alive at `time`” (`time = null` = ignore TTL for L1/L2).

Add the six methods to `InMemoryDbSimulation.kt`. When ready, say so and I’ll validate.
