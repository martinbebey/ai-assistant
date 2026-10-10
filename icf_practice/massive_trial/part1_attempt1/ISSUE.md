# parse_duration

Add `parse_duration(s: str) -> int` in `duration.py`.

It converts a duration string into **whole seconds**.

## Accepted input

- Tokens of the form `<integer><unit>` concatenated, units `h`, `m`, `s` (hours, minutes, seconds).
- Examples: `"1h"` → 3600, `"1h30m"` → 5400, `"90s"` → 90, `"2h1s"` → 7201.
- Tokens may appear in any order. Each unit at most once.
- Empty string or whitespace-only → `0`.

## Errors

Raise `ValueError` if:

- any character is not part of a valid token
- a unit is repeated
- the number is missing (`"h30m"`)
- the unit is missing (`"90"`)

Do **not** require a specific exception message.

Fractions (`"1.5h"`) are **out of scope** — treat as invalid.
