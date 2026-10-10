# Scooter Share — Level 2

## Level 2

Rank scooters by how many rides they’ve completed (`endRide` successes).

### `topScooters(n: Int): List<String>`

Return up to `n` scooter ids with the highest `completedRides`.

Sort:
1. **Completed rides descending**
2. On a tie, **scooter id ascending** (lexicographic)

Format each entry as:

```text
"<scooterId>(<completedRides>)"
```

Example: `["s2(5)", "s1(5)", "s3(0)"]`

- Include scooters with `0` completed rides.
- If fewer than `n` scooters exist, return all of them (same format/order).

---

## Examples

| Query | Result |
|-------|--------|
| `registerScooter("s3")` | `true` |
| `registerScooter("s2")` | `true` |
| `registerScooter("s1")` | `true` |
| `topScooters(3)` | `["s1(0)", "s2(0)", "s3(0)"]` — all 0, alphabetical |
| `startRide("s2", "u1")` / `endRide("s2")` | |
| `startRide("s2", "u1")` / `endRide("s2")` | |
| `startRide("s1", "u2")` / `endRide("s1")` | |
| `topScooters(3)` | `["s2(2)", "s1(1)", "s3(0)"]` |
| `topScooters(1)` | `["s2(2)"]` |

---

Add `topScooters` to `ScooterSimulation.kt`. When ready, say so and I’ll validate Level 2.
