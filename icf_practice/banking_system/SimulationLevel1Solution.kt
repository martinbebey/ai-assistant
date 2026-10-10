/**
 * Banking System — Level 1 reference solution.
 *
 * Study this, then implement your own version in Simulation.kt
 * (or copy/adapt — your call for practice).
 */
class SimulationLevel1Solution {

    private data class Account(var balance: Int = 0)

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
        return source.balance
    }
}
