# Cloud / File Storage — Level 3

## Level 3

Add timestamps and optional TTL on files.

Timestamps are unique and strictly increasing.  
Keep Level 1–2 methods as they are (no TTL). The new `*At` methods respect expiry.

TTL: file is alive for `[uploadedAt, uploadedAt + ttl)`.  
Dead **at** `uploadedAt + ttl`.  
No TTL → never expires.

Copying a file: dest gets the **same size**; if source has TTL, dest’s remaining lifetime / expiry follows the usual rule for this prompt — treat dest as a **new upload at the copy timestamp** with the **same ttl duration** as source had (if source had no TTL, dest has no TTL). If source is expired at `timestamp`, copy throws (source not found / not alive).

---

### `fileUploadAt(timestamp: Int, fileName: String, size: Int, ttl: Int? = null)`

- Same as upload: throw if name already exists **and that file is still alive** at `timestamp`.
- If an expired file occupies the name, you may treat the name as free and upload over it.
- `ttl == null` → no expiry.

### `fileGetAt(timestamp: Int, fileName: String): Int?`

Size if file exists and is alive at `timestamp`; else `null`.

### `fileCopyAt(timestamp: Int, source: String, dest: String)`

- Throw if source missing or expired at `timestamp`.
- Overwrite dest if it exists (alive or replace).
- Dest is stored as of `timestamp` with source’s size and source’s TTL setting (null or same ttl length).

### `fileSearchAt(timestamp: Int, prefix: String): List<String>`

Same as `fileSearch`, but only **alive** files at `timestamp`.

---

## Examples

| Query | Result |
|-------|--------|
| `fileUploadAt(10, "a.txt", 100, ttl = 5)` | ok — alive `[10, 15)` |
| `fileGetAt(14, "a.txt")` | `100` |
| `fileGetAt(15, "a.txt")` | `null` — expired |
| `fileUploadAt(20, "b.txt", 50)` | ok — no TTL |
| `fileSearchAt(20, "")` | includes `"b.txt"`; not `"a.txt"` |

| Query | Result |
|-------|--------|
| `fileUploadAt(1, "x.txt", 200, ttl = 10)` | ok |
| `fileCopyAt(5, "x.txt", "y.txt")` | ok |
| `fileGetAt(5, "y.txt")` | `200` |
| `fileCopyAt(12, "x.txt", "z.txt")` | throws — x expired at 11 |

---

Add the four `*At` methods to `CloudSimulation.kt`. When ready, say so and I’ll validate.
