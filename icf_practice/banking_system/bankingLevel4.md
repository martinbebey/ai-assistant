# Banking System — Level 4

## Level 4

Support merging accounts and querying historical balances.

### `mergeAccounts(timestamp: Int, accountId1: String, accountId2: String): Boolean`

Merge `accountId2` **into** `accountId1`. Returns `true` on success, `false` otherwise.

- Returns `false` if the ids are the same.
- Returns `false` if either account does not exist.
- Add `accountId2`'s **balance** into `accountId1`.
- Add `accountId2`'s **outgoing total** into `accountId1` (so `topSpenders` reflects both).
- Move pending cashbacks from `accountId2` onto `accountId1` (same due timestamps; still pay out to the survivor).
- Re-point all of `accountId2`'s payments to `accountId1` so `getPaymentStatus(accountId1, paymentX)` works.
- **Remove** `accountId2` from the system after the merge.
- Flush due cashbacks at the start of this call (same as other ops).

### `getBalance(timestamp: Int, accountId: String, timeAt: Int): Int?`

Return the account's balance **as of** `timeAt` (after any ops that ran at exactly `timeAt`).

- Returns `null` if the account did not exist at `timeAt`.
- If the account was later merged away, querying it at a time **after** the merge returns `null`.
- The survivor account should still answer historical queries for times when it existed (including inherited history from a merged-in account — see examples).
- Flush due cashbacks for the **query** `timestamp` first (current time of the call), but the returned value is the balance at `timeAt` (including cashbacks that would have applied by `timeAt`).

Hint: keep a balance history (list of `timestamp → balance after that op`) per account, and update it on every balance-changing event. On merge, combine histories carefully.

---

## Examples

| Query | Result |
|-------|--------|
| `createAccount(1, "account1")` | `true` |
| `createAccount(2, "account2")` | `true` |
| `deposit(3, "account1", 2000)` | `2000` |
| `deposit(4, "account2", 2000)` | `2000` |
| `pay(5, "account1", 500)` | `"payment1"` |
| `transfer(6, "account1", "account2", 500)` | `1500` |
| `mergeAccounts(7, "account1", "non-existing")` | `false` |
| `mergeAccounts(8, "account1", "account1")` | `false` |
| `mergeAccounts(9, "account1", "account2")` | `true` |
| `getBalance(10, "account1", 10)` | `3000` |
| `getBalance(11, "account2", 10)` | `null` — gone after merge |
| `getPaymentStatus(12, "account1", "payment1")` | `"IN_PROGRESS"` |
| `getPaymentStatus(13, "account2", "payment1")` | `null` — account2 gone |
| `getBalance(14, "account2", 1)` | `null` — not created yet |
| `getBalance(15, "account2", 9)` | `null` — already merged |
| `getBalance(16, "account1", 11)` | `3000` |
| `deposit(5 + DAY, "account1", 100)` | `3906` — cashback 10 from payment1 applied |

### Another example

| Query | Result |
|-------|--------|
| `createAccount(1, "account1")` | `true` |
| `deposit(2, "account1", 1000)` | `1000` |
| `pay(3, "account1", 300)` | `"payment1"` |
| `getBalance(4, "account1", 3)` | `700` |
| `getBalance(5 + DAY, "account1", 2 + DAY)` | `700` — cashback not due yet at `2+DAY` |
| `getBalance(6 + DAY, "account1", 3 + DAY)` | `706` — cashback 6 applied at `3+DAY` |

(`DAY` = `86_400_000`)

---

Implement `mergeAccounts` and `getBalance` in `Simulation.kt`.  
You’ll likely need new state (balance history, created-at / removed-at times).

When ready, say so — I’ll validate or open a Level 4 solution file.
