# Cloud / File Storage — Level 1

## Instructions

Implement a simplified cloud file host. Levels unlock only after the current level passes.

## System overview (planning only)

- **Level 1:** upload, get size, copy
- **Level 2:** search by prefix (top by size)
- **Level 3:** TTL (`*At` variants)
- **Level 4:** rollback to a timestamp

Design for extensibility (a small `CloudFile` class beats a bare `Map<String, Int>` once TTL arrives).

---

## Level 1

Store files by **name** → **size**. No directories required for Level 1 (names are flat strings).

### `fileUpload(fileName: String, size: Int)`

Upload a file.

- If `fileName` **already exists**, throw `RuntimeException`.
- Otherwise store it. Return type is `Unit` (no value).

### `fileGet(fileName: String): Int?`

- Returns the file **size** if it exists.
- Returns `null` if it does not.

### `fileCopy(source: String, dest: String)`

Copy `source` to `dest`.

- If `source` does not exist, throw `RuntimeException`.
- If `dest` already exists, **overwrite** it with source’s size.

---

## Examples

| Query | Result |
|-------|--------|
| `fileUpload("a.txt", 100)` | ok |
| `fileUpload("a.txt", 50)` | throws — already exists |
| `fileGet("a.txt")` | `100` |
| `fileGet("missing")` | `null` |
| `fileCopy("a.txt", "b.txt")` | ok — `b.txt` is 100 |
| `fileCopy("a.txt", "b.txt")` | ok — overwrite `b.txt` |
| `fileCopy("missing", "c.txt")` | throws — source missing |

---

Implement in `CloudSimulation.kt`. Spec: `cloudLevel1.md`.  
When ready, say so and I’ll validate Level 1.
