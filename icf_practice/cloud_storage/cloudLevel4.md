# Cloud / File Storage — Level 4

## Level 4

### `rollback(timestamp: Int)`

Restore the file storage to how it looked **at** `timestamp`.

- Only files that **existed and were alive** at that timestamp remain.
- Operations after `timestamp` (uploads/copies) are undone.
- **TTLs are recalculated:** for each kept file that had a TTL, compute  
  `remaining = (uploadedAt + ttl) - timestamp`  
  then store it as `uploadedAt = timestamp`, `TTL = remaining`  
  so it still expires at the same absolute time relative to the restored timeline.
- Files with no TTL stay with no TTL.
- Return type is `Unit` (or you may return a status string in some variants — for us: no return value).

Timestamps of later `*At` calls continue to increase after the rollback.

---

## Example

| Query | Result / state |
|-------|----------------|
| `fileUploadAt(10, "a.txt", 100)` | ok |
| `fileUploadAt(20, "b.txt", 50, ttl = 30)` | ok — expires at 50 |
| `fileCopyAt(25, "a.txt", "c.txt")` | ok |
| `fileUploadAt(40, "d.txt", 10)` | ok |
| `rollback(30)` | state as of 30: `a`, `b`, `c` — **not** `d` |
| | `b` remaining = 50 - 30 = 20 → expires at 30 + 20 = 50 |
| `fileGetAt(30, "d.txt")` | `null` |
| `fileGetAt(49, "b.txt")` | `50` |
| `fileGetAt(50, "b.txt")` | `null` |

---

You’ll need history: either snapshots after each mutating `*At`, or a log you can replay up to `timestamp`.

Add `rollback` to `CloudSimulation.kt`. When ready, say so and I’ll validate.
