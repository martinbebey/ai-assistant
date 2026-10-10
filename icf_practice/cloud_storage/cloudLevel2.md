# Cloud / File Storage — Level 2

## Level 2

Search files by name prefix and rank them.

### `fileSearch(prefix: String): List<String>`

Return up to **10** file names that **start with** `prefix`.

Sort:
1. **Size descending** (largest first)
2. On a tie, **file name ascending** (lexicographic)

- If fewer than 10 match, return all matches (same order).
- If none match, return an empty list.

---

## Examples

| Setup | |
|-------|--|
| `fileUpload("Foo.txt", 100)` | |
| `fileUpload("Bar.csv", 200)` | |
| `fileUpload("Baz.pdf", 300)` | |

| Query | Result |
|-------|--------|
| `fileSearch("Ba")` | `["Baz.pdf", "Bar.csv"]` — Baz larger, then Bar |
| `fileSearch("Q")` | `[]` |

---

Add `fileSearch` to `CloudSimulation.kt`. When ready, say so and I’ll validate Level 2.
