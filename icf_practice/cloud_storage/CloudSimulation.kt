/**
 * Cloud / File Storage — CodeSignal-style ICA practice (Kotlin).
 *
 * Implement Level 1 first. Later levels add methods to this same class.
 */
class CloudSimulation {

    // Level 1
    // Hint: Map<fileName, size> or a CloudFile class (better for TTL later)
    private var cloudFiles: MutableMap<String, CloudFile> = mutableMapOf()
    private val backups: MutableMap<Int, MutableMap<String, CloudFile>> = mutableMapOf()

    private data class CloudFile(
        var size: Int,
        val uploadedAt: Int,
        val TTL: Int? = null,
    )

    private fun isAlive(file: CloudFile, currentTime: Int): Boolean{
            val ttl = file.TTL ?: return true
            return currentTime < file.uploadedAt + ttl
    }

    fun fileUpload(fileName: String, size: Int) {
        val fileToUpload = CloudFile(size, uploadedAt = 0, TTL = null)

        if(!cloudFiles.contains(fileName)){
            cloudFiles.getOrPut(fileName){fileToUpload}
        }

        else{
            throw RuntimeException ("already exists")
        }
    }

    fun fileGet(fileName: String): Int? {
        return cloudFiles[fileName]?.size
    }

    fun fileCopy(source: String, dest: String) {
        val sourceFile = cloudFiles.get(source) ?: null

        if(sourceFile == null) throw RuntimeException("file not found")

        cloudFiles[dest] = CloudFile(sourceFile.size, uploadedAt = 0)
    }

    // Level 2
    fun fileSearch(prefix: String): List<String>{
        return cloudFiles.entries
            .filter{it.key.startsWith(prefix)}
            .sortedWith(
                compareByDescending<Map.Entry<String, CloudFile>> {it.value.size}
                    .thenBy{it.key}
            )
            .take(10)
            .map{it.key}
    }

    // Level 3
    fun fileUploadAt(timestamp: Int, fileName: String, size: Int, ttl: Int? = null){
        val fileToUpload = CloudFile(
            size = size,
            TTL = ttl,
            uploadedAt = timestamp
        )

        if(!cloudFiles.contains(fileName) || !isAlive(cloudFiles[fileName]!!, timestamp)){
            cloudFiles[fileName] = fileToUpload
            backup(timestamp)
        }

        else{
            throw RuntimeException ("already exists")
        }
    }

    fun fileGetAt(timestamp: Int, fileName: String): Int?{
        val file = cloudFiles[fileName] ?: null

        if(file != null && isAlive(file, timestamp)) return file.size

        return null
    }

    fun fileCopyAt(timestamp: Int, source: String, dest: String){
        val sourceFile = cloudFiles.get(source) ?: null

        if(sourceFile == null || !isAlive(sourceFile, timestamp)) throw RuntimeException("source file not found")

        cloudFiles[dest] = CloudFile(sourceFile.size, timestamp, sourceFile.TTL)
        backup(timestamp)
    }

    fun fileSearchAt(timestamp: Int, prefix: String): List<String>{
        return cloudFiles.entries
            .filter{it.key.startsWith(prefix) && isAlive(it.value, timestamp)}
            .sortedWith(
                compareByDescending<Map.Entry<String, CloudFile>> {it.value.size}
                    .thenBy{it.key}
            )
            .take(10)
            .map{it.key}
    }

    // Level 4
    private fun backup(timestamp: Int) {
        val snap = mutableMapOf<String, CloudFile>()
        for ((name, file) in cloudFiles) {
            if (!isAlive(file, timestamp)) continue
            if (file.uploadedAt > timestamp) continue
            // deep copy — don't mutate live objects
            snap[name] = file.copy()
        }
        backups[timestamp] = snap
    }

    fun rollback(timestamp: Int){
        val at = backups.keys.filter { it <= timestamp }.maxOrNull() ?: return

        val snap = backups[at]!!
        val restored = mutableMapOf<String, CloudFile>()

        for ((name, file) in snap) {
            restored[name] = file.copy()
        }

        cloudFiles = restored
    }
}
