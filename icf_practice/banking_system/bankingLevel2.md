# Banking System — Level 2

## Level 2

The bank wants to identify people who are not keeping money in their accounts.
Implement ranking based on **outgoing** transactions.

### `topSpenders(timestamp: Int, n: Int): List<String>`

Return the identifiers of the top `n` accounts with the highest outgoing totals —
money **transferred out** (and, from Level 3 onward, also withdrawn/paid).

- Sort by outgoing total **descending**.
- On a tie, sort by `accountId` **ascending** (alphabetical).
- Format each entry as: `"<accountId>(<totalOutgoing>)"`  
  Example: `["account1(2500)", "account3(1500)", "account2(0)"]`
- If fewer than `n` accounts exist, return all of them (same format).
- Accounts with `0` outgoing still count and appear in the ranking.
- Cashback (Level 3) does **not** count as outgoing.

---

## What to track now

Outgoing for an account = sum of amounts successfully **transferred from** that account  
(plus payments in Level 3 — design so you can add that later).

Deposits and incoming transfers do **not** increase outgoing.

---

## Examples

| Query | Result |
|-------|--------|
| `createAccount(1, "account3")` | `true` |
| `createAccount(2, "account2")` | `true` |
| `createAccount(3, "account1")` | `true` |
| `deposit(4, "account3", 2000)` | `2000` |
| `deposit(5, "account2", 3000)` | `3000` |
| `deposit(6, "account3", 4000)` | `6000` |
| `topSpenders(7, 3)` | `["account1(0)", "account2(0)", "account3(0)"]` — all 0, so alphabetical |
| `transfer(8, "account3", "account2", 500)` | `5500` |
| `transfer(9, "account3", "account1", 1000)` | `4500` |
| `transfer(10, "account1", "account2", 2500)` | `500` |
| `topSpenders(11, 3)` | `["account1(2500)", "account3(1500)", "account2(0)"]` |

---

Add `topSpenders` to `Simulation.kt`. When ready, say so and I’ll validate Level 2.
