# Banking System — Level 3

## Level 3

Support payments (withdrawals) with delayed cashback, and querying payment status.

### Constants

```text
MILLISECONDS_IN_1_DAY = 24 * 60 * 60 * 1000 = 86_400_000
```

Cashback is processed at timestamp `paymentTimestamp + MILLISECONDS_IN_1_DAY`.

---

### `pay(timestamp: Int, accountId: String, amount: Int): String?`

Withdraw `amount` from `accountId`.

- On success, returns a unique payment id: `"payment1"`, `"payment2"`, …  
  (ordinal across **all** accounts — global counter, not per-account).
- Returns `null` if the account does not exist.
- Returns `null` if insufficient funds.
- Immediately subtract `amount` from the balance.
- **2% cashback** (integer division / floor): `amount * 2 / 100`, refunded to the account **24 hours later**.
- Before **any** other work at a given `timestamp`, process all cashbacks due at that timestamp (cashbacks whose process time `<= timestamp`).
- `topSpenders` must treat `pay` amounts as **outgoing** (same as transfers). Cashback itself is **not** outgoing.

### `getPaymentStatus(timestamp: Int, accountId: String, payment: String): String?`

- Returns `null` if `accountId` does not exist.
- Returns `null` if `payment` does not exist, or belongs to a different account.
- Returns `"IN_PROGRESS"` if cashback has not been applied yet.
- Returns `"CASHBACK_RECEIVED"` once cashback has been applied.

Process due cashbacks at the start of this call as well (same rule: before other logic at this timestamp).

---

## Important timing rule

Whenever an operation runs at `timestamp`, **first** apply every pending cashback with  
`cashbackAt <= timestamp`, then perform the operation.

Example: at `4 + MILLISECONDS_IN_1_DAY`, cashback for a payment made at `4` is applied **before** whatever you do at that timestamp.

---

## Examples

| Query | Result |
|-------|--------|
| `createAccount(1, "account1")` | `true` |
| `createAccount(2, "account2")` | `true` |
| `deposit(3, "account1", 2000)` | `2000` |
| `pay(4, "account1", 1000)` | `"payment1"` |
| `pay(100, "account1", 1000)` | `"payment2"` |
| `getPaymentStatus(101, "non-existing", "payment1")` | `null` |
| `getPaymentStatus(102, "account2", "payment1")` | `null` — wrong account |
| `getPaymentStatus(103, "account1", "payment1")` | `"IN_PROGRESS"` |
| `topSpenders(104, 2)` | `["account1(2000)", "account2(0)"]` |
| `deposit(3 + DAY, "account1", 100)` | `100` — cashback for payment1 **not** due yet (`4+DAY`) |
| `getPaymentStatus(4 + DAY, "account1", "payment1")` | `"CASHBACK_RECEIVED"` |
| `deposit(5 + DAY, "account1", 100)` | `220` — cashback 20 already applied at `4+DAY` |
| `deposit(99 + DAY, "account1", 100)` | `320` — payment2 cashback not due yet |
| `deposit(100 + DAY, "account1", 100)` | `440` — cashback 20 for payment2 applied |

(`DAY` = `86_400_000`)

---

Add `pay` and `getPaymentStatus` to `Simulation.kt`.  
Also update `deposit` / `transfer` / `topSpenders` / etc. so they flush due cashbacks first.

When ready, say so and I’ll validate Level 3.
