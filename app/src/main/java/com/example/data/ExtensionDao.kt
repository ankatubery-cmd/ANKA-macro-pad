package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtensionDao {
    @Query("SELECT * FROM extensions")
    fun getAllExtensions(): Flow<List<ExtensionEntity>>

    @Query("SELECT * FROM extensions WHERE isInstalled = 1")
    fun getInstalledExtensions(): Flow<List<ExtensionEntity>>

    @Query("SELECT * FROM extensions WHERE id = :id")
    suspend fun getExtensionById(id: String): ExtensionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateExtension(extension: ExtensionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(extensions: List<ExtensionEntity>)

    @Update
    suspend fun updateExtension(extension: ExtensionEntity)

    @Query("UPDATE extensions SET isInstalled = :isInstalled, isEnabled = :isEnabled, installedAt = :installedAt WHERE id = :id")
    suspend fun setInstallStatus(id: String, isInstalled: Boolean, isEnabled: Boolean, installedAt: Long)

    @Query("UPDATE extensions SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setEnabled(id: String, isEnabled: Boolean)

    @Query("DELETE FROM extensions WHERE id = :id")
    suspend fun deleteExtensionById(id: String)
}
