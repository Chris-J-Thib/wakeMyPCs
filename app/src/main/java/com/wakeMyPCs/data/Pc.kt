package com.wakeMyPCs.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pcs")
data class Pc(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "Name")
    val Name: String,
    @ColumnInfo(name = "MAC")
    val MAC: String
)