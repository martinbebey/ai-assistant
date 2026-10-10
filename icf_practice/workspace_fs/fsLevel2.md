# Workspace Filesystem — Level 2

## Level 2

List files whose **path starts with** `prefix`, ranked by size.

### `list(prefix: String): List<String>`

Return up to **10** matching paths.

Sort:
1. **Size descending** (largest first)
2. On a tie, **path ascending** (lexicographic)

Format each entry as:

```text
"<path>(<size>)"
```

- If fewer than 10 match, return all matches (same order).
- If none match, return an empty list.
- `prefix` can be a “directory” like `"/docs/"` — that’s still just `startsWith`, not a real folder tree.

---

## Examples

| Setup | |
|-------|--|
| `create("/docs/a.txt", 100)` | |
| `create("/docs/b.txt", 200)` | |
| `create("/other/c.txt", 300)` | |
| `create("/docs/d.txt", 200)` | |

| Query | Result |
|-------|--------|
| `list("/docs/")` | `["/docs/b.txt(200)", "/docs/d.txt(200)", "/docs/a.txt(100)"]` — size desc, then path |
| `list("/other/")` | `["/other/c.txt(300)"]` |
| `list("/nope")` | `[]` |

---

Add `list` to `FsSimulation.kt`. When ready, say so and I’ll validate.
