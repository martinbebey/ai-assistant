# Scooter Share — Level 3

## Level 3

Add timestamps and **reservations** with TTL.  
Timestamps are unique and strictly increasing.

Keep Level 1–2 methods as they are (no time). New `*At` methods respect the clock.

A scooter can be:
- **free**
- **reserved** for a user until `reservedAt + ttl` (half-open: alive in `[reservedAt, reservedAt + ttl)`, free again at `reservedAt + ttl` if never started)
- **in use** (after `startRideAt`)

---

### `registerScooterAt(timestamp: Int, scooterId: String): Boolean`

Same as `registerScooter` (ignore time for existence). Returns `true` / `false`.

### `reserveAt(timestamp: Int, scooterId: String, userId: String, ttl: Int): Boolean`

Hold a free scooter for `userId` for `ttl` time units.

- Returns `false` if scooter missing.
- Returns `false` if in use.
- Returns `false` if already reserved **and that reservation is still alive** at `timestamp`.
- If a prior reservation **expired**, treat as free and allow reserve.
- Returns `true` on success.

### `startRideAt(timestamp: Int, scooterId: String, userId: String): Boolean`

- Returns `false` if scooter missing or already in use.
- If **reserved**, only `userId` of that reservation may start, and only while the reservation is still alive.
- If **free** (no live reservation), any user may start (same as Level 1).
- On success: scooter is in use by `userId`; clear reservation.

### `endRideAt(timestamp: Int, scooterId: String): Boolean`

Same as `endRide` (increments `completedRides`). Time unused except for consistency.

### `getRiderAt(timestamp: Int, scooterId: String): String?`

- If in use → current rider.
- If reserved and reservation alive → reserved `userId`.
- Else → `null`.

### `topScootersAt(timestamp: Int, n: Int): List<String>`

Same as `topScooters` (completed rides only; reservations don’t count).

---

## Examples

| Query | Result |
|-------|--------|
| `registerScooterAt(1, "s1")` | `true` |
| `reserveAt(2, "s1", "u1", 10)` | `true` — hold until 12 |
| `startRideAt(5, "s1", "u2")` | `false` — reserved for u1 |
| `startRideAt(5, "s1", "u1")` | `true` |
| `endRideAt(6, "s1")` | `true` |
| `reserveAt(7, "s1", "u2", 5)` | `true` — expires at 12 |
| `startRideAt(12, "s1", "u2")` | `false` — reservation expired |
| `startRideAt(12, "s1", "u3")` | `true` — free again |

---

Add the `*At` methods to `ScooterSimulation.kt`. When ready, say so and I’ll validate.
