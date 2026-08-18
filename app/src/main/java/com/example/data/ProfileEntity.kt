package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val iconName: String = "folder",
    val isSelected: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
