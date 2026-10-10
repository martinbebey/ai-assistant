# Scooter Share — Level 4

## Level 4

### `rollback(timestamp: Int)`

Restore the scooter fleet to how it looked **at** `timestamp`.

- Only scooters that **existed** at that timestamp remain (registered at or before `timestamp`).
- Rides, reservations, and registrations **after** `timestamp` are undone.
- `completedRides` must match how many rides had **ended** at or before `timestamp`.
- A reservation that was **alive** at `timestamp` stays reserved for the same user; keep absolute expiry (`reservedAt + TTL` unchanged), or equivalently rewrite remaining TTL the same way as cloud storage.
- A ride that was **in progress** at `timestamp` stays in use by that rider.
- Return type: `Unit` (no return value).

Timestamps of later `*At` calls continue to increase after the rollback.

Level 1–2 methods (no clock) do not need to participate in history — focus on `*At` mutations + `rollback`.

---

## Example

| Query | Result / state |
|-------|----------------|
| `registerScooterAt(1, "s1")` | `true` |
| `registerScooterAt(2, "s2")` | `true` |
| `reserveAt(3, "s1", "u1", 10)` | `true` — hold until 13 |
| `startRideAt(5, "s1", "u1")` | `true` |
| `endRideAt(6, "s1")` | `true` — s1 completedRides = 1 |
| `startRideAt(7, "s2", "u2")` | `true` |
| `registerScooterAt(8, "s3")` | `true` |
| `rollback(4)` | state as of 4: `s1` reserved for u1 (alive until 13); `s2` free; **no** `s3`; s1 rides = 0 |
| `getRiderAt(4, "s1")` | `"u1"` |
| `getRiderAt(4, "s2")` | `null` |
| `startRideAt(9, "s1", "u1")` | `true` — reservation still valid (expires 13) |
| `topScootersAt(10, 2)` | `["s1(0)", "s2(0)"]` |

---

You’ll need history: snapshots after each mutating `*At`, or a log you can replay up to `timestamp` (same idea as cloud `rollback`).

Add `rollback` to `ScooterSimulation.kt`. When ready, say so and I’ll validate.
