package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ProfileEntity::class, MacroButtonEntity::class, ExtensionEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun macroDao(): MacroDao
    abstract fun extensionDao(): ExtensionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anka_macro_pad.db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dbInstance = getInstance(context)
                    populateInitialData(dbInstance.macroDao())
                }
            }
        }

        suspend fun populateInitialData(dao: MacroDao) {
            val generalProfileId = dao.insertProfile(
                ProfileEntity(
                    name = "Genel",
                    iconName = "dashboard",
                    isSelected = true
                )
            ).toInt()

            dao.insertProfile(
                ProfileEntity(
                    name = "Yayıncılık",
                    iconName = "mic",
                    isSelected = false
                )
            )

            dao.insertProfile(
                ProfileEntity(
                    name = "Oyun",
                    iconName = "gamepad",
                    isSelected = false
                )
            )

            val defaultButtons = listOf(
                MacroButtonEntity(
                    profileId = generalProfileId,
                    title = "H",
                    subtext = "H",
                    iconName = "keyboard",
                    macroType = MacroType.KEY,
                    primaryValue = "H",
                    orderIndex = 0
                ),
                MacroButtonEntity(
                    profileId = generalProfileId,
                    title = "CTRL + C",
                    subtext = "Kopyala",
                    iconName = "copy",
                    macroType = MacroType.SHORTCUT,
                    primaryValue = "CTRL+C",
                    orderIndex = 1
                ),
                MacroButtonEntity(
                    profileId = generalProfileId,
                    title = "CTRL + V",
                    subtext = "Yapıştır",
                    iconName = "paste",
                    macroType = MacroType.SHORTCUT,
                    primaryValue = "CTRL+V",
                    orderIndex = 2
                ),
                MacroButtonEntity(
                    profileId = generalProfileId,
                    title = "ENTER",
                    subtext = "",
                    iconName = "enter",
                    macroType = MacroType.KEY,
                    primaryValue = "ENTER",
                    orderIndex = 3
                ),
                MacroButtonEntity(
                    profileId = generalProfileId,
                    title = "ALT + TAB",
                    subtext = "Görev Değiştir",
                    iconName = "swap",
                    macroType = MacroType.SHORTCUT,
                    primaryValue = "ALT+TAB",
                    orderIndex = 4
                ),
                MacroButtonEntity(
                    profileId = generalProfileId,
                    title = "F5",
                    subtext = "Yenile",
                    iconName = "refresh",
                    macroType = MacroType.KEY,
                    primaryValue = "F5",
                    orderIndex = 5
                ),
                MacroButtonEntity(
                    profileId = generalProfileId,
                    title = "Dosya",
                    subtext = "",
                    iconName = "folder",
                    macroType = MacroType.PROGRAM,
                    primaryValue = "explorer.exe",
                    orderIndex = 6
                ),
                MacroButtonEntity(
                    profileId = generalProfileId,
                    title = "Mikrofon",
                    subtext = "Aç / Kapat",
                    iconName = "mic",
                    macroType = MacroType.SHORTCUT,
                    primaryValue = "CTRL+SHIFT+M",
                    orderIndex = 7
                ),
                MacroButtonEntity(
                    profileId = generalProfileId,
                    title = "Ses",
                    subtext = "Aç / Kapat",
                    iconName = "volume",
                    macroType = MacroType.SHORTCUT,
                    primaryValue = "VOLUME_MUTE",
                    orderIndex = 8
                ),
                MacroButtonEntity(
                    profileId = generalProfileId,
                    title = "Ekran Kilitle",
                    subtext = "",
                    iconName = "screen_lock",
                    macroType = MacroType.SHORTCUT,
                    primaryValue = "WIN+L",
                    orderIndex = 9
                ),
                MacroButtonEntity(
                    profileId = generalProfileId,
                    title = "Ekran Görüntüsü",
                    subtext = "",
                    iconName = "scissors",
                    macroType = MacroType.SHORTCUT,
                    primaryValue = "WIN+SHIFT+S",
                    orderIndex = 10
                )
            )

            for (button in defaultButtons) {
                dao.insertButton(button)
            }
        }
    }
}
