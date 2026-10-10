/**
 * Banking System — CodeSignal-style ICA practice (Kotlin).
 * Full Levels 1–4.
 */
class BankingSimulation {

    private val millisecondsInOneDay = 86_400_000

    private data class Account(
        var balance: Int = 0,
        var outTransfersTotal: Int = 0,
        var createdAt: Int = 0,
        val cashbackDue: MutableMap<Int, Int> = mutableMapOf(),
        // timestamp → balance AFTER the op at that timestamp
        val balanceHistory: MutableMap<Int, Int> = mutableMapOf(),
    ) {
        fun recordBalance(timestamp: Int) {
            balanceHistory[timestamp] = balance
        }
    }

    private data class Payment(
        var accountId: String,
        val cashbackAt: Int,
        var cashbackReceived: Boolean = false,
    )

    private val accountsList = mutableMapOf<String, Account>()
    private val payments = mutableMapOf<String, Payment>()
    private var paymentCounter = 0

    private fun processDueCashbacks(timestamp: Int) {
        for (account in accountsList.values) {
            val dueKeys = account.cashbackDue.keys.filter { it <= timestamp }.sorted()
            for (dueAt in dueKeys) {
                val amount = account.cashbackDue.remove(dueAt) ?: continue
                account.balance += amount
                // Record at cashback due time (not the caller timestamp)
                account.recordBalance(dueAt)
            }
        }
        for (payment in payments.values) {
            if (!payment.cashbackReceived && payment.cashbackAt <= timestamp) {
                payment.cashbackReceived = true
            }
        }
    }

    // ---- Level 1 ----

    fun createAccount(timestamp: Int, accountId: String): Boolean {
        processDueCashbacks(timestamp)
        if (accountId in accountsList) return false
        val account = Account(createdAt = timestamp)
        account.recordBalance(timestamp) // 0
        accountsList[accountId] = account
        return true
    }

    fun deposit(timestamp: Int, accountId: String, amount: Int): Int? {
        processDueCashbacks(timestamp)
        val account = accountsList[accountId] ?: return null
        account.balance += amount
        account.recordBalance(timestamp)
        return account.balance
    }

    fun transfer(
        timestamp: Int,
        sourceAccountId: String,
        targetAccountId: String,
        amount: Int,
    ): Int? {
        processDueCashbacks(timestamp)
        if (sourceAccountId == targetAccountId) return null
        val source = accountsList[sourceAccountId] ?: return null
        val target = accountsList[targetAccountId] ?: return null
        if (source.balance < amount) return null

        source.balance -= amount
        target.balance += amount
        source.outTransfersTotal += amount
        source.recordBalance(timestamp)
        target.recordBalance(timestamp)
        return source.balance
    }

    // ---- Level 2 ----

    fun topSpenders(timestamp: Int, n: Int): List<String> {
        processDueCashbacks(timestamp)
        return accountsList.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Account>> { it.value.outTransfersTotal }
                    .thenBy { it.key },
            )
            .take(n)
            .map { (id, account) -> "$id(${account.outTransfersTotal})" }
    }

    // ---- Level 3 ----

    fun pay(timestamp: Int, accountId: String, amount: Int): String? {
        processDueCashbacks(timestamp)
        val account = accountsList[accountId] ?: return null
        if (account.balance < amount) return null

        account.balance -= amount
        account.outTransfersTotal += amount
        account.recordBalance(timestamp)

        val cashback = amount * 2 / 100
        val cashbackAt = timestamp + millisecondsInOneDay
        account.cashbackDue[cashbackAt] =
            account.cashbackDue.getOrDefault(cashbackAt, 0) + cashback

        paymentCounter += 1
        val paymentId = "payment$paymentCounter"
        payments[paymentId] = Payment(accountId = accountId, cashbackAt = cashbackAt)
        return paymentId
    }

    fun getPaymentStatus(timestamp: Int, accountId: String, payment: String): String? {
        processDueCashbacks(timestamp)
        if (accountId !in accountsList) return null
        val record = payments[payment] ?: return null
        if (record.accountId != accountId) return null
        return if (record.cashbackReceived) "CASHBACK_RECEIVED" else "IN_PROGRESS"
    }

    // ---- Level 4 ----

    fun mergeAccounts(timestamp: Int, accountId1: String, accountId2: String): Boolean {
        processDueCashbacks(timestamp)
        if (accountId1 == accountId2) return false
        val account1 = accountsList[accountId1] ?: return false
        val account2 = accountsList[accountId2] ?: return false

        account1.balance += account2.balance
        account1.outTransfersTotal += account2.outTransfersTotal
        account1.createdAt = minOf(account1.createdAt, account2.createdAt)

        // Move pending cashbacks onto survivor
        for ((dueAt, amount) in account2.cashbackDue) {
            account1.cashbackDue[dueAt] =
                account1.cashbackDue.getOrDefault(dueAt, 0) + amount
        }

        // Inherit history (map is fine — timestamps unique per op; rare same-ts
        // collision from a transfer between the two keeps account1's entry)
        for ((ts, bal) in account2.balanceHistory) {
            if (ts !in account1.balanceHistory) {
                account1.balanceHistory[ts] = bal
            }
        }

        // Repoint payments from account2 → account1
        for (payment in payments.values) {
            if (payment.accountId == accountId2) {
                payment.accountId = accountId1
            }
        }

        account1.recordBalance(timestamp)
        accountsList.remove(accountId2)
        return true
    }

    fun getBalance(timestamp: Int, accountId: String, timeAt: Int): Int? {
        processDueCashbacks(timestamp)
        // Merged-away / never-existed accounts are gone from the map → null
        val account = accountsList[accountId] ?: return null
        if (timeAt < account.createdAt) return null

        val latestTs = account.balanceHistory.keys
            .filter { it <= timeAt }
            .maxOrNull()
            ?: return null
        return account.balanceHistory[latestTs]
    }
}
