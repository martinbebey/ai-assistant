# Workspace Filesystem — Level 3

## Level 3

Add timestamps and optional TTL.  
Timestamps are unique and strictly increasing.

Keep Level 1–2 methods as they are (no time). New `*At` methods respect expiry.

TTL: file is alive in `[createdAt, createdAt + ttl)`.  
Dead **at** `createdAt + ttl`.  
No TTL (`null`) → never expires.

---

### `createAt(timestamp: Int, path: String, size: Int, ttl: Int? = null)`

- Throw if `path` already exists **and that file is still alive** at `timestamp`.
- If an expired file occupies the path, treat as free and create over it.
- `ttl == null` → no expiry.

### `getSizeAt(timestamp: Int, path: String): Int?`

Size if the file exists and is alive; else `null`.

### `copyAt(timestamp: Int, source: String, dest: String)`

- Throw if source is missing or expired at `timestamp`.
- Overwrite dest (alive or dead).
- Dest is a **new** file at `timestamp` with source’s size and the **same ttl duration** as source (`null` if source had none).

### `deleteAt(timestamp: Int, path: String): Boolean`

- `true` if the file exists **and is alive** and was removed.
- `false` if missing or already expired (do not need to remove a corpse, but ignoring it is fine).

### `listAt(timestamp: Int, prefix: String): List<String>`

Same as `list`, but only **alive** files at `timestamp`.

---

## Examples

| Query | Result |
|-------|--------|
| `createAt(10, "/a.txt", 100, ttl = 5)` | ok — alive `[10, 15)` |
| `getSizeAt(14, "/a.txt")` | `100` |
| `getSizeAt(15, "/a.txt")` | `null` — expired |
| `createAt(20, "/b.txt", 50)` | ok — no TTL |
| `listAt(20, "/")` | `["/b.txt(50)"]` — `/a.txt` dead |

| Query | Result |
|-------|--------|
| `createAt(1, "/x.txt", 200, ttl = 10)` | ok |
| `copyAt(5, "/x.txt", "/y.txt")` | ok |
| `getSizeAt(5, "/y.txt")` | `200` |
| `copyAt(12, "/x.txt", "/z.txt")` | throws — x expired at 11 |
| `deleteAt(5, "/y.txt")` | `true` |
| `deleteAt(5, "/y.txt")` | `false` |

---

Add the `*At` methods to `FsSimulation.kt`. When ready, say so and I’ll validate.
