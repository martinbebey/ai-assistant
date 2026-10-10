# In-Memory Database — Level 1

## Instructions

Implement a simplified in-memory database. Levels unlock only after the current level passes.

## System overview (planning only — implement Level 1 now)

- **Level 1:** set / get / delete fields on records
- **Level 2:** scan / scanByPrefix
- **Level 3:** TTL (time-to-live)
- **Level 4:** backup / restore

Design for extensibility (nested maps or a small `Record` class help).

---

## Level 1

Records are keyed by a string `key`. Each record holds string field → string value pairs.

### `set(key: String, field: String, value: String): String`

Insert or replace `field` → `value` on the record for `key`.  
Create the record if it does not exist.

- Always returns `""` (empty string).

### `get(key: String, field: String): String`

Return the value for `field` on record `key`.

- Returns `""` if the record or field does not exist.

### `delete(key: String, field: String): String`

Remove `field` from record `key`.

- Returns `"true"` if the field was deleted.
- Returns `"false"` if the key or field does not exist.

---

## Examples

| Query | Result | State after |
|-------|--------|-------------|
| `set("A", "B", "E")` | `""` | `{A: {B: E}}` |
| `set("A", "C", "F")` | `""` | `{A: {B: E, C: F}}` |
| `get("A", "B")` | `"E"` | |
| `get("A", "D")` | `""` | |
| `delete("A", "B")` | `"true"` | `{A: {C: F}}` |
| `delete("A", "D")` | `"false"` | `{A: {C: F}}` |

---

Implement in `InMemoryDbSimulation.kt`. Spec file: `inMemoryDbLevel1.md`.  
When ready, say so and I’ll validate Level 1.
