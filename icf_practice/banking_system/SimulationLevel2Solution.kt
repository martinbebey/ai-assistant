/**
 * Banking System — Level 2 reference solution.
 *
 * Builds on your Level 1 approach: track outgoing on Account, then rank.
 *
 * Note on your current Simulation.kt:
 * - `outTransfersTotal` must be `var` (you mutate it in transfer)
 * - Map → sort entries → take(n) → format strings (maps aren't indexable lists)
 */
class SimulationLevel2Solution {

    private data class Account(
        var balance: Int = 0,
        var outTransfersTotal: Int = 0,
    )

    private val accounts = mutableMapOf<String, Account>()

    fun createAccount(timestamp: Int, accountId: String): Boolean {
        if (accountId in accounts) return false
        accounts[accountId] = Account()
        return true
    }

    fun deposit(timestamp: Int, accountId: String, amount: Int): Int? {
        val account = accounts[accountId] ?: return null
        account.balance += amount
        return account.balance
    }

    fun transfer(
        timestamp: Int,
        sourceAccountId: String,
        targetAccountId: String,
        amount: Int,
    ): Int? {
        if (sourceAccountId == targetAccountId) return null
        val source = accounts[sourceAccountId] ?: return null
        val target = accounts[targetAccountId] ?: return null
        if (source.balance < amount) return null

        source.balance -= amount
        target.balance += amount
        source.outTransfersTotal += amount
        return source.balance
    }

    fun topSpenders(timestamp: Int, n: Int): List<String> {
        return accounts.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Account>> { it.value.outTransfersTotal }
                    .thenBy { it.key },
            )
            .take(n)
            .map { (id, account) -> "$id(${account.outTransfersTotal})" }
    }
}
