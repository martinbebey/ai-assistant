/**
 * Banking System — Level 3 reference solution.
 *
 * Follows your approach:
 * - Account holds balance + outgoing + pending cashbacks
 * - Flush due cashbacks before doing the real work
 * - pay() withdraws, bumps outgoing, schedules 2% cashback
 *
 * Fixes vs your draft:
 * - cashbackDue must be `var` / a real MutableMap (not emptyList)
 * - delay is 86_400_000 ms (1 day), not 48
 * - payment ids are "payment1", "payment2", … (global counter), not "payment$timestamp"
 * - flush must run on deposit / transfer / topSpenders / getPaymentStatus too
 * - track payments for getPaymentStatus
 */
class SimulationLevel3Solution {

    companion object {
        const val MILLISECONDS_IN_1_DAY = 86_400_000
    }

    private data class Account(
        var balance: Int = 0,
        var outTransfersTotal: Int = 0,
        // cashbackDueAtTimestamp → cashback amount (can sum if multiple land same ms)
        val cashbackDue: MutableMap<Int, Int> = mutableMapOf(),
    )

    private data class Payment(
        val accountId: String,
        val cashbackAt: Int,
        var cashbackReceived: Boolean = false,
    )

    private val accountsList = mutableMapOf<String, Account>()
    private val payments = mutableMapOf<String, Payment>() // "payment1" → Payment
    private var paymentCounter = 0

    /** Apply every cashback with due time <= timestamp, across all accounts. */
    private fun processDueCashbacks(timestamp: Int) {
        for (account in accountsList.values) {
            val dueKeys = account.cashbackDue.keys.filter { it <= timestamp }
            for (dueAt in dueKeys) {
                val amount = account.cashbackDue.remove(dueAt) ?: continue
                account.balance += amount
            }
        }
        // Mark matching payments as received
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
        accountsList[accountId] = Account()
        return true
    }

    fun deposit(timestamp: Int, accountId: String, amount: Int): Int? {
        processDueCashbacks(timestamp)
        val account = accountsList[accountId] ?: return null
        account.balance += amount
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
        processDueCashbacks(timestamp) // your idea: settle due cashback first

        val account = accountsList[accountId] ?: return null
        if (account.balance < amount) return null

        account.balance -= amount
        account.outTransfersTotal += amount // pays count as outgoing; cashback does NOT undo this

        val cashback = amount * 2 / 100
        val cashbackAt = timestamp + MILLISECONDS_IN_1_DAY
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
}
