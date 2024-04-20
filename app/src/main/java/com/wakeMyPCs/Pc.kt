package com.wakeMyPCs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pcs")
data class Pc(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "Name") val subject: String,
    @ColumnInfo(name = "MAC") val sender: String
)