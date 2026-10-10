# Workspace Filesystem — Level 1

## Instructions

Implement a simplified in-memory filesystem. Levels unlock only after the current level passes.

## System overview (planning only — implement Level 1 now)

- **Level 1:** Create, size lookup, copy, delete
- **Level 2:** List files under a path prefix (ranked)
- **Level 3:** Timed ops + TTL
- **Level 4:** Rollback

Paths are **full string keys** (e.g. `"/docs/a.txt"`). Treat them as names — you do **not** need a directory tree for Level 1. Parent folders do not have to exist.

Design for extensibility (a small `FsFile` class beats `Map<String, Int>` once TTL arrives).

---

## Level 1

### `create(path: String, size: Int)`

Create a new file.

- If `path` **already exists**, throw `RuntimeException`.
- Otherwise store it. Return type is `Unit`.

### `getSize(path: String): Int?`

- Returns the file **size** if it exists.
- Returns `null` if it does not.

### `copy(source: String, dest: String)`

Copy `source` to `dest`.

- If `source` does not exist, throw `RuntimeException`.
- If `dest` already exists, **overwrite** it with source’s size.

### `delete(path: String): Boolean`

- Returns `true` if the file existed and was removed.
- Returns `false` if it did not exist.

---

## Examples

| Query | Result |
|-------|--------|
| `create("/docs/a.txt", 100)` | ok |
| `create("/docs/a.txt", 50)` | throws — already exists |
| `getSize("/docs/a.txt")` | `100` |
| `getSize("/missing")` | `null` |
| `copy("/docs/a.txt", "/docs/b.txt")` | ok — b is 100 |
| `copy("/docs/a.txt", "/docs/b.txt")` | ok — overwrite b |
| `copy("/missing", "/c.txt")` | throws — source missing |
| `delete("/docs/b.txt")` | `true` |
| `getSize("/docs/b.txt")` | `null` |
| `delete("/docs/b.txt")` | `false` |

---

Implement in `FsSimulation.kt`. When ready, say so and I’ll validate Level 1.
