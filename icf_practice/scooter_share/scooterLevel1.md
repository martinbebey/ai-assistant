# Scooter Share — Level 1

## Instructions

Implement a simplified scooter-share system. Levels unlock only after the current level passes.

## System overview (planning only — implement Level 1 now)

- **Level 1:** Register scooters, start/end rides, look up current rider
- **Level 2:** Rank scooters by completed rides
- **Level 3:** Timed reservations with TTL
- **Level 4:** Rollback to a past timestamp

---

## Level 1

Scooters are identified by string ids. A scooter is either **free** or **in use** by one rider.

### `registerScooter(scooterId: String): Boolean`

- Returns `true` if registered.
- Returns `false` if that id already exists.

### `startRide(scooterId: String, userId: String): Boolean`

Start a ride on a free scooter.

- Returns `false` if the scooter does not exist.
- Returns `false` if the scooter is already in use.
- Returns `true` on success (scooter now held by `userId`).

### `endRide(scooterId: String): Boolean`

End the current ride.

- Returns `false` if the scooter does not exist or is not in use.
- Returns `true` on success (scooter becomes free).  
  Count this as **one completed ride** for Level 2 ranking.

### `getRider(scooterId: String): String?`

- Returns the current `userId` if the scooter is in use.
- Returns `null` if missing or free.

---

## Examples

| Query | Result |
|-------|--------|
| `registerScooter("s1")` | `true` |
| `registerScooter("s1")` | `false` |
| `startRide("s1", "u1")` | `true` |
| `getRider("s1")` | `"u1"` |
| `startRide("s1", "u2")` | `false` — already in use |
| `endRide("s1")` | `true` |
| `getRider("s1")` | `null` |
| `endRide("s1")` | `false` — already free |
| `startRide("missing", "u1")` | `false` |

---

Implement in `ScooterSimulation.kt`. Spec: `scooterLevel1.md`.  
When ready, say so and I’ll validate Level 1.
