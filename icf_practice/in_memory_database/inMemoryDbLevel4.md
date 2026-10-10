# In-Memory Database — Level 4

## Level 4

Backup and restore the database. On restore, **recalculate TTL** from remaining lifespan.

Timestamps still strictly increase.

---

### `backup(timestamp: Int): String`

Save a snapshot of the DB **at this timestamp**.

- Only include fields that are **still alive** at `timestamp`.
- For TTL fields, store **remaining lifespan** = `expiresAt - timestamp`  
  (`expiresAt` = `timeStamp + ttl`).
- Fields with no TTL stay non-expiring in the snapshot.
- Returns the number of **non-empty** records in that snapshot, as a **string**  
  (count of keys that still have at least one alive field).  
  Example: `"1"`, `"2"`.

### `restore(timestamp: Int, timestampToRestore: Int): String`

Replace the live DB with the **latest backup whose backup time `<= timestampToRestore`**.  
It is guaranteed such a backup exists.

- Timeline only moves forward: new expiry is  
  `timestamp + remainingLifespan` from the snapshot  
  (example: remaining 6 at backup, restore at 10 → expires at `16`).
- No-TTL fields stay no-TTL.
- Returns `""`.

---

## Example

| Query | Result |
|-------|--------|
| `setAtWithTtl("A", "B", "C", 1, 10)` | `""` — alive `[1, 11)` |
| `backup(3)` | `"1"` |
| `setAt("A", "D", "E", 4)` | `""` |
| `backup(5)` | `"1"` |
| `deleteAt("A", "B", 8)` | `"true"` |
| `backup(9)` | `"1"` |
| `restore(10, 7)` | `""` — uses backup **at 5** (latest `<= 7`). B still has remaining 6 → expires at **16** |
| `setAt("B", "C", "D", 11)` | `""` |
| `backup(12)` | `"2"` |
| `scanAt("A", 15)` | `"B(C), D(E)"` |
| `scanAt("A", 16)` | `"D(E)"` |
| `scanAt("B", 17)` | `"C(D)"` |

---

Hint: keep `backups: MutableMap<Int, …>` of deep-copied records. On backup, compute remaining TTL from `timestamp`. On restore, copy that snapshot and rewrite `timeStamp`/`ttl` so expiry is `now + remaining`.

Implement `backup` and `restore` in `InMemoryDbSimulation.kt`. When ready, say so and I’ll validate.
