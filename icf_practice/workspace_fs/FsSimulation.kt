/**
 * Workspace Filesystem — CodeSignal-style ICA practice (Kotlin).
 *
 * Implement Level 1 first. Later levels add methods to this same class.
 */
class FsSimulation {

    private var systemFiles: MutableMap<String, FileData> = mutableMapOf()
    private var backups: MutableMap<Int, MutableMap<String, FileData>> = mutableMapOf()

    private data class FileData(
        var fileSize: Int,
        var timeStamp: Int = 0,
        var TTL: Int? = null
    )

    private fun isAlive(timestamp: Int, data: FileData): Boolean{
        if(data.TTL == null) return true
    
        return (data.timeStamp + data.TTL) > timestamp
    }

    fun create(path: String, size: Int){
        val newFileData = FileData(size)

        if(path in systemFiles.keys) throw RuntimeException("file already exists")

        else{
            systemFiles.getOrPut(path){newFileData}
        }
    }

    fun getSize(path: String): Int?{

        return systemFiles[path]?.fileSize
    }

    fun copy(source: String, dest: String){
        if(source !in systemFiles.keys) throw RuntimeException("source does not exist")

        if(dest in systemFiles.keys){
            val destFileData = systemFiles[dest]!!
            destFileData.fileSize = systemFiles[source]!!.fileSize

            systemFiles[dest] = destFileData
        }
        else{
            val sourceFileData = FileData(systemFiles[source]!!.fileSize)
            systemFiles[dest] = sourceFileData
        }
    }

    fun delete(path: String): Boolean{
        if(path !in systemFiles.keys) return false

        systemFiles.remove(path)

        return true
    }

    fun list(prefix: String): List<String>{
        return systemFiles.entries
            .filter{it.key.startsWith(prefix)}
            .sortedWith(
                compareByDescending<Map.Entry<String, FileData>>{it.value.fileSize}
                    .thenBy{it.key}
            
            )
            .take(10)
            .map{"${it.key}(${it.value.fileSize})"}
    }

    fun createAt(timestamp: Int, path: String, size: Int, ttl: Int? = null){
        val fileData = systemFiles[path] ?: null

        if(fileData != null && isAlive(timestamp, fileData)) throw RuntimeException("file already exists")

        val newFileData = FileData(timeStamp = timestamp, fileSize = size, TTL = ttl)

        systemFiles[path] = newFileData

        backup(timestamp)

    }

    fun getSizeAt(timestamp: Int, path: String): Int?{
        val fileData = systemFiles[path] ?: null

        if(fileData != null && isAlive(timestamp, fileData)) return fileData.fileSize
        else return null
    }

    fun copyAt(timestamp: Int, source: String, dest: String){
        val sourceData = systemFiles[source] ?: null

        if(sourceData == null || !isAlive(timestamp, sourceData)) throw RuntimeException("invalid file")

        val newFileData = FileData(timeStamp = timestamp, fileSize = sourceData.fileSize, TTL = sourceData.TTL)

        systemFiles[dest] = newFileData

        backup(timestamp)
    }

    fun deleteAt(timestamp: Int, path: String): Boolean{
        val file = systemFiles[path] ?: null

        if(file != null && isAlive(timestamp, file)){
            systemFiles.remove(path)

            backup(timestamp)
            return true
        }

        return false
    }

    fun listAt(timestamp: Int, prefix: String): List<String>{
        return systemFiles.entries
            .filter{it.key.startsWith(prefix) && isAlive(timestamp, it.value)}
            .sortedWith(
                compareByDescending<Map.Entry<String, FileData>>{it.value.fileSize}
                    .thenBy{it.key}
            
            )
            .take(10)
            .map{"${it.key}(${it.value.fileSize})"}
    }

    private fun backup(timestamp: Int){
        val backupFiles = mutableMapOf<String, FileData>()

        for((path, data) in systemFiles){
            if(!isAlive(timestamp, data)) continue
            backupFiles[path] = data.copy()
        }

        backups[timestamp] = backupFiles
    }

    fun rollback(timestamp: Int){
        val bestTime = backups.keys.filter{it <= timestamp}.maxOrNull() ?: return

        val snapshot = backups[bestTime] ?: return 
        val restoredFiles = mutableMapOf<String, FileData>()

        for((path, data) in snapshot){
            restoredFiles[path] = data.copy()
        }

        systemFiles = restoredFiles
    }
}
