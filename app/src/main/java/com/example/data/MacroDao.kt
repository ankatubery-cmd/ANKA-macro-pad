package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MacroDao {
    // Profiles
    @Query("SELECT * FROM profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles ORDER BY id ASC")
    suspend fun getAllProfilesSync(): List<ProfileEntity>

    @Query("SELECT * FROM profiles WHERE id = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: Int): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE isSelected = 1 LIMIT 1")
    fun getSelectedProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedProfileSync(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("UPDATE profiles SET isSelected = 0")
    suspend fun clearSelectedProfile()

    @Query("UPDATE profiles SET isSelected = 1 WHERE id = :profileId")
    suspend fun setSelectedProfile(profileId: Int)

    // Macro Buttons
    @Query("SELECT * FROM macro_buttons WHERE profileId = :profileId ORDER BY orderIndex ASC, id ASC")
    fun getButtonsForProfile(profileId: Int): Flow<List<MacroButtonEntity>>

    @Query("SELECT * FROM macro_buttons WHERE profileId = :profileId ORDER BY orderIndex ASC, id ASC")
    suspend fun getButtonsForProfileSync(profileId: Int): List<MacroButtonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButton(button: MacroButtonEntity): Long

    @Update
    suspend fun updateButton(button: MacroButtonEntity)

    @Delete
    suspend fun deleteButton(button: MacroButtonEntity)

    @Query("DELETE FROM macro_buttons WHERE id = :buttonId")
    suspend fun deleteButtonById(buttonId: Int)

    @Query("DELETE FROM macro_buttons WHERE profileId = :profileId")
    suspend fun deleteButtonsForProfile(profileId: Int)

    @Query("DELETE FROM macro_buttons WHERE extensionId = :extensionId")
    suspend fun deleteButtonsByExtensionId(extensionId: String)

    @Query("SELECT * FROM macro_buttons WHERE extensionId = :extensionId")
    suspend fun getButtonsByExtensionId(extensionId: String): List<MacroButtonEntity>
}
