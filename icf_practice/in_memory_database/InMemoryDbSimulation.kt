/**
 * In-Memory Database — full Levels 1–4 (merged from Level 4 solution).
 */
class InMemoryDbSimulation {

    private val database: MutableMap<String, MutableMap<String, Field>> = mutableMapOf()
    private val backups: MutableMap<Int, MutableMap<String, MutableMap<String, Field>>> = mutableMapOf()

    private data class Field(
        val contents: String,
        val timeStamp: Int? = null,
        val ttl: Int? = null,
    )

    private fun isAlive(field: Field, now: Int): Boolean {
        val ttl = field.ttl ?: return true
        val start = field.timeStamp ?: return true
        return now < start + ttl
    }

    private fun remainingTtl(field: Field, now: Int): Int? {
        val ttl = field.ttl ?: return null
        val start = field.timeStamp ?: return null
        return (start + ttl) - now
    }

    private fun snapshotAlive(
        source: MutableMap<String, MutableMap<String, Field>>,
        now: Int,
        rewriteTtlFromRemaining: Boolean,
    ): MutableMap<String, MutableMap<String, Field>> {
        val copy = mutableMapOf<String, MutableMap<String, Field>>()
        for ((key, record) in source) {
            val newRecord = mutableMapOf<String, Field>()
            for ((fieldName, field) in record) {
                if (!isAlive(field, now)) continue
                val stored = if (rewriteTtlFromRemaining) {
                    val remaining = remainingTtl(field, now)
                    Field(contents = field.contents, timeStamp = now, ttl = remaining)
                } else {
                    field.copy()
                }
                newRecord[fieldName] = stored
            }
            if (newRecord.isNotEmpty()) {
                copy[key] = newRecord
            }
        }
        return copy
    }

    // ---- Level 1 ----

    fun set(key: String, field: String, value: String): String {
        database.getOrPut(key) { mutableMapOf() }[field] = Field(contents = value)
        return ""
    }

    fun get(key: String, field: String): String {
        return database[key]?.get(field)?.contents ?: ""
    }

    fun delete(key: String, field: String): String {
        database[key]?.let { dbRecord ->
            if (dbRecord.contains(field)) {
                dbRecord.remove(field)
                return "true"
            }
        }
        return "false"
    }

    // ---- Level 2 ----

    fun scan(key: String): String {
        val record = database[key] ?: return ""
        return record.entries
            .sortedBy { it.key }
            .joinToString(", ") { "${it.key}(${it.value.contents})" }
    }

    fun scanByPrefix(key: String, prefix: String): String {
        val record = database[key] ?: return ""
        return record.entries
            .filter { it.key.startsWith(prefix) }
            .sortedBy { it.key }
            .joinToString(", ") { "${it.key}(${it.value.contents})" }
    }

    // ---- Level 3 ----

    fun setAt(key: String, field: String, value: String, timestamp: Int): String {
        database.getOrPut(key) { mutableMapOf() }[field] =
            Field(contents = value, timeStamp = timestamp, ttl = null)
        return ""
    }

    fun setAtWithTtl(
        key: String,
        field: String,
        value: String,
        timestamp: Int,
        ttl: Int,
    ): String {
        database.getOrPut(key) { mutableMapOf() }[field] =
            Field(contents = value, timeStamp = timestamp, ttl = ttl)
        return ""
    }

    fun getAt(key: String, field: String, timestamp: Int): String {
        val stored = database[key]?.get(field) ?: return ""
        return if (isAlive(stored, timestamp)) stored.contents else ""
    }

    fun deleteAt(key: String, field: String, timestamp: Int): String {
        val record = database[key] ?: return "false"
        val stored = record[field] ?: return "false"
        if (!isAlive(stored, timestamp)) return "false"
        record.remove(field)
        return "true"
    }

    fun scanAt(key: String, timestamp: Int): String {
        val record = database[key] ?: return ""
        return record.entries
            .filter { isAlive(it.value, timestamp) }
            .sortedBy { it.key }
            .joinToString(", ") { "${it.key}(${it.value.contents})" }
    }

    fun scanByPrefixAt(key: String, prefix: String, timestamp: Int): String {
        val record = database[key] ?: return ""
        return record.entries
            .filter { isAlive(it.value, timestamp) }
            .filter { it.key.startsWith(prefix) }
            .sortedBy { it.key }
            .joinToString(", ") { "${it.key}(${it.value.contents})" }
    }

    // ---- Level 4 ----

    fun backup(timestamp: Int): String {
        val snap = snapshotAlive(database, timestamp, rewriteTtlFromRemaining = true)
        backups[timestamp] = snap
        return snap.size.toString()
    }

    fun restore(timestamp: Int, timestampToRestore: Int): String {
        val backupAt = backups.keys.filter { it <= timestampToRestore }.maxOrNull()!!
        val snap = backups[backupAt]!!
        val restored = mutableMapOf<String, MutableMap<String, Field>>()
        for ((key, record) in snap) {
            val newRecord = mutableMapOf<String, Field>()
            for ((fieldName, field) in record) {
                val remaining = field.ttl
                newRecord[fieldName] = if (remaining == null) {
                    Field(contents = field.contents)
                } else {
                    Field(contents = field.contents, timeStamp = timestamp, ttl = remaining)
                }
            }
            restored[key] = newRecord
        }
        database.clear()
        database.putAll(restored)
        return ""
    }
}
