# Banking System — Level 1

## Instructions

Implement a simplified banking system. The task has several levels.
A later level unlocks only after the current level is correct. You keep access
to the current and all previous levels.

## System overview (all levels — for planning only)

- **Level 1:** Create accounts, deposit, transfer between accounts.
- **Level 2:** Rank accounts by outgoing transaction volume.
- **Level 3:** Payments with delayed cashback; payment status.
- **Level 4:** Merge accounts; historical balance queries.

Do **not** implement Levels 2–4 yet. Design Level 1 so you can extend it.

## Note

Every operation has a `timestamp` (milliseconds, `Int`). Timestamps are unique,
strictly increasing, in `[1, 10^9]`. Operations arrive in increasing order.

---

## Level 1

Initially there are no accounts.

### `createAccount(timestamp: Int, accountId: String): Boolean`

Create a new account with the given id if it does not already exist.

- Returns `true` if created successfully.
- Returns `false` if an account with `accountId` already exists.

### `deposit(timestamp: Int, accountId: String, amount: Int): Int?`

Deposit `amount` into `accountId`.

- Returns the account balance **after** the deposit.
- Returns `null` if the account does not exist.

### `transfer(timestamp: Int, sourceAccountId: String, targetAccountId: String, amount: Int): Int?`

Transfer `amount` from `sourceAccountId` to `targetAccountId`.

- Returns the remaining balance of `sourceAccountId` if successful.
- Returns `null` if either account does not exist.
- Returns `null` if source and target are the same.
- Returns `null` if source has insufficient funds.

---

## Examples

| Query | Result |
|-------|--------|
| `createAccount(1, "account1")` | `true` |
| `createAccount(2, "account1")` | `false` — already exists |
| `createAccount(3, "account2")` | `true` |
| `deposit(4, "non-existing", 2700)` | `null` |
| `deposit(5, "account1", 2700)` | `2700` |
| `transfer(6, "account1", "account2", 2701)` | `null` — insufficient funds |
| `transfer(7, "account1", "account2", 200)` | `2500` |

---

Implement in `Simulation.kt`. When ready, say so and I’ll validate Level 1.
