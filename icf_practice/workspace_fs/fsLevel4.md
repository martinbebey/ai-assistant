# Workspace Filesystem — Level 4

## Level 4

### `rollback(timestamp: Int)`

Restore the filesystem to how it looked **at** `timestamp`.

- Only files that **existed and were alive** at that timestamp remain.
- Creates / copies / deletes **after** `timestamp` are undone.
- Keep absolute expiry (`createdAt + TTL` unchanged), or rewrite remaining TTL the same way as cloud storage — either is fine as long as a file still dies at the original `createdAt + ttl`.
- Files with no TTL stay with no TTL.
- Return type: `Unit`.

Timestamps of later `*At` calls continue to increase after the rollback.

Level 1–2 methods (no clock) do not need history — snapshot after each mutating `*At`.

---

## Example

| Query | Result / state |
|-------|----------------|
| `createAt(10, "/a.txt", 100)` | ok |
| `createAt(20, "/b.txt", 50, ttl = 30)` | ok — expires at 50 |
| `copyAt(25, "/a.txt", "/c.txt")` | ok |
| `createAt(40, "/d.txt", 10)` | ok |
| `rollback(30)` | as of 30: `/a`, `/b`, `/c` — **not** `/d` |
| `getSizeAt(30, "/d.txt")` | `null` |
| `getSizeAt(49, "/b.txt")` | `50` |
| `getSizeAt(50, "/b.txt")` | `null` |

---

Same idea as cloud/scooter: deep-copy snapshots after successful mutating `*At`, restore latest backup `≤ timestamp`.

Add `rollback` to `FsSimulation.kt`. When ready, say so and I’ll validate.
