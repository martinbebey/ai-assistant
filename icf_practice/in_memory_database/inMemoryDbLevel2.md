# In-Memory Database — Level 2

## Level 2

Display a record’s fields, optionally filtered by prefix.

### `scan(key: String): String`

Return all fields of the record for `key`, sorted **lexicographically by field name**.

Format:

```text
"<field1>(<value1>), <field2>(<value2>), ..."
```

- Returns `""` if the record does not exist (or has no fields).

### `scanByPrefix(key: String, prefix: String): String`

Same format as `scan`, but only fields whose **name starts with** `prefix`.

- Fields still sorted lexicographically.
- Returns `""` if the record does not exist, or no fields match.

---

## Examples

| Query | Result |
|-------|--------|
| `set("A", "BC", "E")` | `""` |
| `set("A", "BD", "F")` | `""` |
| `set("A", "C", "G")` | `""` |
| `scanByPrefix("A", "B")` | `"BC(E), BD(F)"` |
| `scan("A")` | `"BC(E), BD(F), C(G)"` |
| `scanByPrefix("B", "B")` | `""` — key `B` does not exist |

---

Add `scan` and `scanByPrefix` to `InMemoryDbSimulation.kt`.  
When ready, say so and I’ll validate Level 2.
