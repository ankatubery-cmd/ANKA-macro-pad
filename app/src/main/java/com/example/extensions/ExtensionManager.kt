package com.example.extensions

import android.content.Context
import android.net.Uri
import com.example.data.ExtensionDao
import com.example.data.ExtensionEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ExtensionManifest(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val icon: String,
    val entry: String,
    val developer: String,
    val minAnkaVersion: String,
    val permissions: List<String>,
    val actions: List<ExtensionAction>
)

data class ExtensionAction(
    val id: String,
    val name: String,
    val icon: String = "extension",
    val description: String = "",
    val type: String = "SHORTCUT", // SHORTCUT, KEY, PROGRAM
    val value: String = ""
)

class ExtensionManager(
    private val context: Context,
    private val extensionDao: ExtensionDao
) {
    val extensionsBaseDir: File
        get() = File(context.filesDir, "extensions").apply { if (!exists()) mkdirs() }

    fun getExtensionDir(extensionId: String): File {
        return File(extensionsBaseDir, extensionId)
    }

    /**
     * Imports an extension package from a ZIP file Uri.
     */
    /**
     * Imports a user extension package from a ZIP file.
     *
     * Package format:
     *   extension.zip
     *   ├── manifest.json
     *   ├── index.html (or the file declared by "entry")
     *   ├── css/
     *   ├── js/
     *   └── assets/
     *
     * The package is copied into the app-private extensions directory and is
     * never installed as an Android APK.
     */
    suspend fun importFromZip(zipUri: Uri): Result<ExtensionEntity> {
        val tempDir = File(
            extensionsBaseDir,
            "_temp_import_${System.currentTimeMillis()}"
        )

        return try {
            tempDir.mkdirs()

            val inputStream = context.contentResolver.openInputStream(zipUri)
                ?: throw IllegalArgumentException("Dosya okunamadı.")

            inputStream.use {
                unzipStream(
                    inputStream = it,
                    targetDir = tempDir,
                    maxFiles = 250,
                    maxUncompressedBytes = 15L * 1024L * 1024L
                )
            }

            val manifestFile = findManifestFile(tempDir)
                ?: throw IllegalArgumentException("ZIP dosyasında manifest.json bulunamadı.")

            val manifest = parseManifest(manifestFile.readText())
                ?: throw IllegalArgumentException(
                    "manifest.json geçersiz. Zorunlu alanlar: id, name ve entry."
                )

            if (!isSafeExtensionId(manifest.id)) {
                throw IllegalArgumentException("Geçersiz uzantı ID'si: ${manifest.id}")
            }

            if (!isSafeRelativePath(manifest.entry)) {
                throw IllegalArgumentException("Geçersiz entry yolu: ${manifest.entry}")
            }

            val manifestParent = manifestFile.parentFile ?: tempDir
            val packageRoot = manifestParent.canonicalFile
            val entryFile = File(packageRoot, manifest.entry).canonicalFile

            if (!entryFile.path.startsWith(packageRoot.path + File.separator) &&
                entryFile != packageRoot
            ) {
                throw SecurityException("Entry dosyası uzantı klasörünün dışına çıkıyor.")
            }

            if (!entryFile.isFile) {
                throw IllegalArgumentException("Entry dosyası bulunamadı: ${manifest.entry}")
            }

            val targetDir = getExtensionDir(manifest.id)
            if (targetDir.exists()) {
                targetDir.deleteRecursively()
            }

            if (!manifestParent.renameTo(targetDir)) {
                copyDirectory(manifestParent, targetDir)
            }

            val installedEntry = File(targetDir, manifest.entry).canonicalFile
            val targetRoot = targetDir.canonicalFile
            if (!installedEntry.isFile ||
                !installedEntry.path.startsWith(targetRoot.path + File.separator)
            ) {
                targetDir.deleteRecursively()
                throw IllegalArgumentException("Uzantı paketi kurulamadı: entry dosyası eksik.")
            }

            val entity = ExtensionEntity(
                id = manifest.id,
                name = manifest.name,
                iconName = manifest.icon.ifBlank { "extension" },
                description = manifest.description,
                developer = manifest.developer.ifBlank { "Topluluk Geliştiricisi" },
                version = manifest.version.ifBlank { "1.0.0" },
                minAnkaVersion = manifest.minAnkaVersion.ifBlank { "1.0.0" },
                permissionsJson = JSONArray(manifest.permissions).toString(),
                category = "Uzantı Paketi",
                isInstalled = true,
                isEnabled = true,
                installedAt = System.currentTimeMillis(),
                macroPresetsJson = actionsToJson(manifest.actions)
            )

            extensionDao.insertOrUpdateExtension(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(
                Exception(
                    "Uzantı yüklenemedi: ${e.localizedMessage ?: "bilinmeyen hata"}",
                    e
                )
            )
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun parseManifest(jsonText: String): ExtensionManifest? {
        return try {
            val obj = JSONObject(jsonText)
            val id = obj.getString("id")
            val name = obj.getString("name")
            val version = obj.optString("version", "1.0.0")
            val desc = obj.optString("description", "")
            val icon = obj.optString("icon", "extension")
            val entry = obj.optString("entry", "index.html")
            val dev = obj.optString("developer", "Topluluk Geliştiricisi")
            val minAnkaVersion = obj.optString("minAnkaVersion", "1.0.0")

            val perms = mutableListOf<String>()
            val permsArray = obj.optJSONArray("permissions")
            if (permsArray != null) {
                for (i in 0 until permsArray.length()) {
                    perms.add(permsArray.getString(i))
                }
            }

            val actions = mutableListOf<ExtensionAction>()
            val actionsArray = obj.optJSONArray("actions")
            if (actionsArray != null) {
                for (i in 0 until actionsArray.length()) {
                    val aObj = actionsArray.getJSONObject(i)
                    actions.add(
                        ExtensionAction(
                            id = aObj.getString("id"),
                            name = aObj.getString("name"),
                            icon = aObj.optString("icon", "extension"),
                            description = aObj.optString("description", ""),
                            type = aObj.optString("type", "EXTENSION_ACTION"),
                            value = aObj.optString("value", "")
                        )
                    )
                }
            }

            ExtensionManifest(
                id = id,
                name = name,
                version = version,
                description = desc,
                icon = icon,
                entry = entry,
                developer = dev,
                minAnkaVersion = minAnkaVersion,
                permissions = perms,
                actions = actions
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun findManifestFile(dir: File): File? {
        val direct = File(dir, "manifest.json")
        if (direct.isFile) return direct

        val children = dir.listFiles() ?: return null
        children.filter { it.isDirectory && !it.name.startsWith("_") }.forEach { sub ->
            val manifest = File(sub, "manifest.json")
            if (manifest.isFile) return manifest
        }
        return null
    }

    private fun isSafeExtensionId(id: String): Boolean {
        return id.length in 1..64 && id.matches(Regex("[a-zA-Z0-9._-]+"))
    }

    private fun isSafeRelativePath(path: String): Boolean {
        if (path.isBlank() || path.startsWith("/") || path.startsWith("\\") ||
            path.contains(":") || path.contains("\\") || path.contains("../") ||
            path == ".." || path.startsWith("../") || path.endsWith("/..")
        ) return false
        return path.split('/').none { it.isBlank() || it == "." || it == ".." }
    }

    private fun copyDirectory(source: File, target: File) {
        if (!target.exists()) target.mkdirs()
        source.walkTopDown().forEach { file ->
            val relative = file.relativeTo(source)
            val destination = File(target, relative.path)
            if (file.isDirectory) {
                destination.mkdirs()
            } else {
                destination.parentFile?.mkdirs()
                file.copyTo(destination, overwrite = true)
            }
        }
    }

    private fun unzipStream(
        inputStream: InputStream,
        targetDir: File,
        maxFiles: Int,
        maxUncompressedBytes: Long
    ) {
        ZipInputStream(inputStream).use { zip ->
            val root = targetDir.canonicalFile
            val buffer = ByteArray(8192)
            var fileCount = 0
            var totalBytes = 0L

            while (true) {
                val entry = zip.nextEntry ?: break
                val entryName = entry.name.replace('\\', '/')

                if (entryName.startsWith("/") ||
                    entryName.split('/').any { it == ".." }
                ) {
                    throw SecurityException("ZIP içinde geçersiz dosya yolu bulundu.")
                }

                fileCount++
                if (fileCount > maxFiles) {
                    throw SecurityException("Uzantı paketi çok fazla dosya içeriyor.")
                }

                val newFile = File(root, entryName).canonicalFile
                if (newFile != root &&
                    !newFile.path.startsWith(root.path + File.separator)
                ) {
                    throw SecurityException("ZIP dosyası uzantı klasörünün dışına çıkmaya çalışıyor.")
                }

                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        var len: Int
                        while (zip.read(buffer).also { len = it } > 0) {
                            totalBytes += len
                            if (totalBytes > maxUncompressedBytes) {
                                throw SecurityException("Uzantı paketi izin verilen boyutu aşıyor.")
                            }
                            fos.write(buffer, 0, len)
                        }
                    }
                }
                zip.closeEntry()
            }
        }
    }

    /**
     * Returns the installed extension entry HTML file after validating the
     * manifest entry stays inside the extension directory.
     */
    fun getInstalledEntryFile(extensionId: String): File? {
        if (!isSafeExtensionId(extensionId)) return null
        val root = getExtensionDir(extensionId).canonicalFile
        val manifestFile = File(root, "manifest.json")
        if (!manifestFile.isFile) return null

        val entryPath = try {
            JSONObject(manifestFile.readText()).optString("entry", "index.html")
        } catch (_: Exception) {
            return null
        }

        if (!isSafeRelativePath(entryPath)) return null
        val entry = File(root, entryPath).canonicalFile
        if (!entry.path.startsWith(root.path + File.separator) || !entry.isFile) return null
        return entry
    }

    fun actionsToJson(actions: List<ExtensionAction>): String {
        val jsonArray = JSONArray()
        for (action in actions) {
            val obj = JSONObject()
            obj.put("id", action.id)
            obj.put("name", action.name)
            obj.put("icon", action.icon)
            obj.put("description", action.description)
            obj.put("type", action.type)
            obj.put("value", action.value)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    fun parseActions(actionsJson: String): List<ExtensionAction> {
        val list = mutableListOf<ExtensionAction>()
        if (actionsJson.isBlank()) return list
        try {
            val array = JSONArray(actionsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ExtensionAction(
                        id = obj.optString("id", ""),
                        name = obj.optString("name", "Aksiyon"),
                        icon = obj.optString("icon", "extension"),
                        description = obj.optString("description", ""),
                        type = obj.optString("type", "EXTENSION_ACTION"),
                        value = obj.optString("value", "")
                    )
                )
            }
        } catch (e: Exception) {
            // Safe fallback
        }
        return list
    }
}
