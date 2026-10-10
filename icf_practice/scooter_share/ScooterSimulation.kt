/**
 * Scooter Share — CodeSignal-style ICA practice (Kotlin).
 * Levels 1–3 implemented. Level 4 stub at the bottom.
 *
 * One userID: reserved = reservedAt set (alive); in use = userID set and reservedAt null.
 */
class ScooterSimulation {
    private var scooters: MutableMap<String, Scooter> = mutableMapOf()
    private val backups: MutableMap<Int, MutableMap<String, Scooter>> = mutableMapOf()

    private data class Scooter(
        var userID: String? = null,
        var completedRides: Int = 0,
        var reservedAt: Int? = null,
        var TTL: Int = 0,
        var registeredAt: Int? = null,
    )

    /** Drop an expired hold so the scooter is free again. */
    private fun clearExpiredReservation(scooter: Scooter, timestamp: Int) {
        val start = scooter.reservedAt ?: return
        if (timestamp >= start + scooter.TTL) {
            scooter.reservedAt = null
            scooter.TTL = 0
            scooter.userID = null
        }
    }

    private fun isInUse(scooter: Scooter): Boolean =
        scooter.userID != null && scooter.reservedAt == null

    private fun isReservedAlive(scooter: Scooter, timestamp: Int): Boolean {
        val start = scooter.reservedAt ?: return false
        return timestamp < start + scooter.TTL
    }

    // ---- Level 1 ----

    fun registerScooter(scooterId: String): Boolean {
        if (scooterId in scooters) return false
        scooters[scooterId] = Scooter()
        return true
    }

    fun startRide(scooterId: String, userId: String): Boolean {
        val scooter = scooters[scooterId] ?: return false
        if (scooter.userID != null) return false

        scooter.userID = userId
        return true
    }

    fun endRide(scooterId: String): Boolean {
        val scooter = scooters[scooterId] ?: return false
        if (scooter.userID == null) return false

        scooter.userID = null
        scooter.completedRides += 1
        return true
    }

    fun getRider(scooterId: String): String? {
        val scooter = scooters[scooterId] ?: return null
        return scooter.userID
    }

    // ---- Level 2 ----

    fun topScooters(n: Int): List<String> {
        return scooters.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Scooter>> { it.value.completedRides }
                    .thenBy { it.key }
            )
            .take(n)
            .map { "${it.key}(${it.value.completedRides})" }
    }

    // ---- Level 3 ----

    fun registerScooterAt(timestamp: Int, scooterId: String): Boolean {
        if (scooterId in scooters) return false
        scooters[scooterId] = Scooter(registeredAt = timestamp)

        backup(timestamp)
        return true
    }

    fun reserveAt(timestamp: Int, scooterId: String, userId: String, ttl: Int): Boolean {
        val scooter = scooters[scooterId] ?: return false
        clearExpiredReservation(scooter, timestamp)

        if (isInUse(scooter)) return false
        if (isReservedAlive(scooter, timestamp)) return false

        scooter.userID = userId
        scooter.reservedAt = timestamp
        scooter.TTL = ttl

        backup(timestamp)
        return true
    }

    fun startRideAt(timestamp: Int, scooterId: String, userId: String): Boolean {
        val scooter = scooters[scooterId] ?: return false
        clearExpiredReservation(scooter, timestamp)

        if (isInUse(scooter)) return false

        if (isReservedAlive(scooter, timestamp)) {
            if (scooter.userID != userId) return false
            // holder starts → in use; drop reservation
            scooter.reservedAt = null
            scooter.TTL = 0
            scooter.userID = userId

            backup(timestamp)
            return true
        }

        // free — any user
        scooter.userID = userId
        scooter.reservedAt = null
        scooter.TTL = 0

        backup(timestamp)
        return true
    }

    fun endRideAt(timestamp: Int, scooterId: String): Boolean {
        val scooter = scooters[scooterId] ?: return false
        if (!isInUse(scooter)) return false

        scooter.userID = null
        scooter.completedRides += 1
        backup(timestamp)
        return true
    }

    fun getRiderAt(timestamp: Int, scooterId: String): String? {
        val scooter = scooters[scooterId] ?: return null
        clearExpiredReservation(scooter, timestamp)

        if (isInUse(scooter)) return scooter.userID
        if (isReservedAlive(scooter, timestamp)) return scooter.userID
        return null
    }

    fun topScootersAt(timestamp: Int, n: Int): List<String> {
        return topScooters(n)
    }

    // ---- Level 4 ----

    private fun backup(timestamp: Int){
        val snapshot = mutableMapOf<String, Scooter>()

        for((id, scooter) in scooters){

                snapshot[id] = scooter.copy()
            
        }

        backups[timestamp] = snapshot
    }

    fun rollback(timestamp: Int) {
        val backupTime = backups.keys.filter{it <= timestamp}.maxOrNull() ?: return

        val backup = backups[backupTime]

        if (backup == null) return
        val restored: MutableMap<String, Scooter> = mutableMapOf()

        for((id, scooter) in backup){
            restored[id] = scooter.copy()
        }

        scooters = restored
    }
}
